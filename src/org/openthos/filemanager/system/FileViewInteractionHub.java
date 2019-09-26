package org.openthos.filemanager.system;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import org.openthos.filemanager.BaseActivity;
import org.openthos.filemanager.BaseFragment;
import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.R;
import org.openthos.filemanager.bean.FileInfo;
import org.openthos.filemanager.bean.Mode;
import org.openthos.filemanager.bean.PathBean;
import org.openthos.filemanager.component.CreateFileDialog;
import org.openthos.filemanager.component.MenuDialog;
import org.openthos.filemanager.component.PropertyDialog;
import org.openthos.filemanager.component.ShareMenuDialog;
import org.openthos.filemanager.fragment.SystemSpaceFragment;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.utils.OperateUtils;
import org.openthos.filemanager.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class FileViewInteractionHub implements FileOperationHelper.IOperationProgressListener {
    private static final int FILE_NAME_LEGAL = 0;
    private static final int FILE_NAME_NULL = 1;
    private static final int FILE_NAME_ILLEGAL = 2;
    private static final int FILE_NAME_WARNING = 3;
    private static final String LOG_TAG = "FileViewInteractionHub";
    private IFileInteractionListener mFileViewListener;
    private ArrayList<FileInfo> mCheckedFileNameList = new ArrayList<>();
    private FileOperationHelper mFileOperationHelper;
    private FileSortHelper mFileSortHelper;
    private ProgressDialog progressDialog;
    private Context mContext;
    private MenuDialog menuDialog;
    private MainActivity mMainActivity;

    private boolean mIsBlank = true;
    private boolean mIsDirectory = false;
    private boolean mIsProtected = true;
    private int mCompressFileState;
    private boolean mConfirm;


    public FileViewInteractionHub(IFileInteractionListener fileViewListener) {
        assert (fileViewListener != null);
        mFileViewListener = fileViewListener;
        mFileOperationHelper = new FileOperationHelper(this);
        mContext = mFileViewListener.getContext();
        mFileSortHelper = ((BaseActivity) mContext).getFileSortHelper();
        mMainActivity = (MainActivity) mContext;
        setMode(mMainActivity.mMode);
    }

    public void sortCurrentList() {
        mFileViewListener.sortCurrentList(mFileSortHelper);
    }

    public void addDialogSelectedItem(FileInfo fileInfo) {
        mCheckedFileNameList.add(fileInfo);
    }

    public void removeDialogSelectedItem(FileInfo fileInfo) {
        mCheckedFileNameList.remove(fileInfo);
    }

    public ArrayList<FileInfo> getSelectedFileList() {
        return mCheckedFileNameList;
    }

    public ArrayList<FileInfo> getCheckedFileList() {
        return mFileOperationHelper.getFileList();
    }

    // operation finish notification
    @Override
    public void onFinish() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        mFileViewListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clearSelection();
                refreshFileList();
            }
        });
    }

    public FileInfo getItem(int pos) {
        return mFileViewListener.getItem(pos);
    }

    public boolean isInSelection() {
        return mCheckedFileNameList.size() > 0;
    }

    public boolean isMultiChosen() {
        return mCheckedFileNameList.size() > 1;
    }

    public boolean isMoveState() {
        return mFileOperationHelper.isMoveState() || mFileOperationHelper.canPaste();
    }

    public void onOperationShowSysFiles() {
        Settings.instance().setShowDotAndHiddenFiles(!Settings.instance()
                .getShowDotAndHiddenFiles());
        refreshFileList();
    }

    public void onOperationSelectAll() {
        mCheckedFileNameList.clear();
        for (FileInfo f : mFileViewListener.getAllFiles()) {
            f.Selected = true;
            mCheckedFileNameList.add(f);
        }
        mFileViewListener.onDataChanged();
    }

    public boolean onOperationUpLevel() {
        if (!mRoot.equals(mCurrentPath)) {
            mCurrentPath = new File(mCurrentPath).getParent();
            mMainActivity.setHistory(new PathBean(mRoot, mCurrentPath));
            mMainActivity.setCurPath(mCurrentPath);
            refreshFileList();
            return true;
        }
        return false;
    }

    public void onOperationCreateFolder() {
        TextInputDialog dialog = new TextInputDialog(
                mContext,
                mContext.getString(R.string.operation_create_folder),
                mContext.getString(R.string.operation_create_folder_message),
                mContext.getString(R.string.new_folder_name),
                new TextInputDialog.OnFinishListener() {
                    @Override
                    public boolean onFinish(String text) {
                        return doCreateFolder(text);
                    }
                }
        );
        dialog.show();
    }

    public void onOperationCreateFile() {
        CreateFileDialog dialog = new CreateFileDialog(
                mContext,
                mContext.getString(R.string.operation_create_file),
                mContext.getString(R.string.operation_create_file_message),
                mContext.getString(R.string.new_file_name),
                new CreateFileDialog.OnFinishListener() {
                    @Override
                    public boolean onFinish(String text) {
                        return doCreateFile(text);
                    }
                }
        );
        dialog.show();
    }

    private boolean doCreateFolder(final String text) {
        switch (isValidFileName(text)) {
            case FILE_NAME_LEGAL:
                return createFolder(text);
            case FILE_NAME_NULL:
                OperateUtils.showConfirmAlertDialog(mContext, R.string.file_name_not_null);
                return false;
            case FILE_NAME_ILLEGAL:
                OperateUtils.showConfirmAlertDialog(mContext, R.string.file_name_illegal);
                return false;
            case FILE_NAME_WARNING:
                DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mConfirm = createFolder(text);
                    }
                };
                DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mConfirm = false;
                    }
                };
                OperateUtils.showChooseAlertDialog(mContext, R.string.file_name_warning, ok, cancel);
                break;
        }
        return mConfirm;
    }

    private boolean createFolder(String text) {
        if (mFileOperationHelper.CreateFolder(mCurrentPath, text)) {
            mFileViewListener.addSingleFile(Util.GetFileInfo(Util.makePath(mCurrentPath, text)));
            clearSelection();
        } else {
            OperateUtils.showConfirmAlertDialog(mContext, R.string.fail_to_create_folder);
            clearSelection();
            return false;
        }

        return true;
    }

    private boolean doCreateFile(final String text) {
        switch (isValidFileName(text)) {
            case FILE_NAME_LEGAL:
                return createFile(text);
            case FILE_NAME_NULL:
                OperateUtils.showConfirmAlertDialog(mContext, R.string.file_name_not_null);
                return false;
            case FILE_NAME_ILLEGAL:
                OperateUtils.showConfirmAlertDialog(mContext, R.string.file_name_illegal);
                return false;
            case FILE_NAME_WARNING:
                DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mConfirm = createFile(text);
                    }
                };
                DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mConfirm = false;
                    }
                };
                OperateUtils.showChooseAlertDialog(mContext, R.string.file_name_warning, ok, cancel);
                break;
        }
        return mConfirm;
    }

    private boolean createFile(String text) {
        if (mFileOperationHelper.CreateFile(mMainActivity, mCurrentPath, text)) {
            mFileViewListener.addSingleFile(Util.GetFileInfo(Util.makePath(mCurrentPath, text)));
            clearSelection();
        } else {
            OperateUtils.showConfirmAlertDialog(mContext, R.string.fail_to_create_folder);
            clearSelection();
            return false;
        }
        return true;
    }

    //TODO
    public void onSortChanged(FileSortHelper.SortMethod s) {
        if (mFileSortHelper.getSortMethod() != s) {
            mFileSortHelper.setSortMethog(s);
            sortCurrentList();
        }
    }

    public void onOperationCopyPath() {
        if (getSelectedFileList().size() == 1) {
            copy(getSelectedFileList().get(0).filePath);
        }
        clearSelection();
    }

    private void copy(CharSequence text) {
        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(
                Context.CLIPBOARD_SERVICE);
        cm.setText(text);
    }

    public void initFileList() {
        clearSelection();
        ((BaseFragment) mFileViewListener).clearSelectList();
        mFileViewListener.onRefreshFileList(mCurrentPath, mFileSortHelper);
    }

    public void refreshFileList() {
        if (mFileViewListener instanceof SystemSpaceFragment) {
            mMainActivity.setNavigationBar(Util.getDisplayPath(mContext, mCurrentPath));
            initFileList();
        } else {
            mFileViewListener.onRefreshFileList(null, null);
        }
    }

    public void onOperationSend() {
        ArrayList<FileInfo> selectedFileList = getSelectedFileList();
        for (FileInfo f : selectedFileList) {
            if (f.IsDir) {
                OperateUtils.showConfirmAlertDialog(mContext, R.string.error_info_cant_send_folder);
                return;
            }
        }

        Intent intent = IntentBuilder.buildSendFile(selectedFileList);
        if (intent != null) {
            try {
                mFileViewListener.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(LOG_TAG, "fail to view file: " + e.toString());
            }
        }
        clearSelection();
    }

    public void onOperationRename() {
        if (getSelectedFileList().size() == 0)
            return;

        final FileInfo f = getSelectedFileList().get(0);

        TextInputDialog dialog = new TextInputDialog(mContext,
                mContext.getString(R.string.operation_rename),
                mContext.getString(R.string.operation_rename_message),
                f.fileName,
                new TextInputDialog.OnFinishListener() {
                    @Override
                    public boolean onFinish(String text) {
                        return doRename(f, text);
                    }
                }
        );

        dialog.show();
    }

    private boolean doRename(final FileInfo f, final String text) {
        switch (isValidFileName(text)) {
            case FILE_NAME_LEGAL:
                return rename(f, text);
            case FILE_NAME_NULL:
                OperateUtils.showConfirmAlertDialog(mContext, R.string.file_name_not_null);
                return false;
            case FILE_NAME_ILLEGAL:
                OperateUtils.showConfirmAlertDialog(mContext, R.string.file_name_illegal);
                return false;
            case FILE_NAME_WARNING:
                DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mConfirm = rename(f, text);
                    }
                };
                DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mConfirm = false;
                    }
                };
                OperateUtils.showChooseAlertDialog(
                        mContext, R.string.file_name_warning, ok, cancel);
                break;
        }
        return mConfirm;
    }

    private boolean rename(FileInfo f, String text) {
        String newPath = Util.makePath(Util.getPathFromFilepath(f.filePath), text);
        if (f.filePath.equals(newPath)) {
            return true;
        }
        if (mFileOperationHelper.Rename(f, text)) {
            f.fileName = text;
            mFileViewListener.onDataChanged();
        } else {
            OperateUtils.showConfirmAlertDialog(mContext, R.string.fail_to_rename);
            return false;
        }
        refreshFileList();
        return true;
    }

    private int isValidFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return FILE_NAME_NULL;
        } else {
            if (fileName.indexOf("/") != -1) {
                return FILE_NAME_ILLEGAL;
            }
            if (!Pattern.compile("[^@#\\$\\^&*\\(\\)\\[\\]]*").matcher(fileName).matches()) {
                return FILE_NAME_WARNING;
            }
            return FILE_NAME_LEGAL;
        }
    }

    private void notifyFileSystemChanged(String path) {
        if (path == null)
            return;
        final File f = new File(path);
        /*Build.VERSION_CODES.KITKAT*/
        if (Build.VERSION.SDK_INT >= 19) {
            String[] paths;
            paths = new String[]{path};
            MediaScannerConnection.scanFile(mContext, paths, null, null);
        } else {
            final Intent intent;
            if (f.isDirectory()) {
                intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
                intent.setClassName("com.android.providers.media",
                        "com.android.providers.media.MediaScannerReceiver");
                intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
                Log.v(LOG_TAG, "directory changed, send broadcast:" + intent.toString());
            } else {
                intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(new File(path)));
                Log.v(LOG_TAG, "file changed, send broadcast:" + intent.toString());
            }
            mContext.sendBroadcast(intent);
        }
    }

    public void onOperationDelete() {
        doOperationDelete(getSelectedFileList());
    }

    public void onOperationDeleteDirect() {
        doOperationDeleteDirect(getSelectedFileList());
    }

    public void onOperationCleanRecycle() {
        doOperationDeleteDirect(new ArrayList<FileInfo>(mFileViewListener.getAllFiles()));
    }

    public void onOperationRestore(boolean isAllFile) {
        ArrayList<String[]> fileInfo = new ArrayList<>();
        if (isAllFile) {
            Cursor c = MainActivity.getResolver().query(
                    MainActivity.getUri(), null, null, null, null);
            if (c != null) {
                while (c.moveToNext()) {
                    fileInfo.add(new String[]{c.getString(c.getColumnIndex("source")),
                            c.getString(c.getColumnIndex("filename"))});
                }
            }
        } else {
            String[] selectInfos = new String[getSelectedFileList().size()];
            for (int i = 0; i < getSelectedFileList().size(); i++) {
                selectInfos[i] = getSelectedFileList().get(i).fileName;
            }
            Cursor c = MainActivity.getResolver().query(
                    MainActivity.getUri(), null, null, null, null);
            ArrayList<String[]> dbInfos = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    dbInfos.add(new String[]{c.getString(c.getColumnIndex("source")),
                            c.getString(c.getColumnIndex("filename"))});
                }
            }
            for (String[] info : dbInfos) {
                for (String selectInfo : selectInfos) {
                    if (info[1].equals(selectInfo)) {
                        fileInfo.add(info);
                    }
                }
            }
        }
        if (fileInfo.size() > 0) {
            new RestoreThread(fileInfo).start();
        }
    }

    class RestoreThread extends Thread {
        ArrayList<String[]> mFileInfo;

        public RestoreThread(ArrayList<String[]> fileInfo) {
            mFileInfo = fileInfo;
        }

        @Override
        public void run() {
            super.run();
            for (String[] info : mFileInfo) {
                FileOperationHelper.MoveFile(mMainActivity,
                        new File(FileOperationHelper.RECYCLE_PATH1, info[1]).getAbsolutePath(),
                        info[0], false);
            }
        }
    }

    public void onOperationDelete(int position) {
        FileInfo file = mFileViewListener.getItem(position);
        if (file == null) {
            return;
        }
        ArrayList<FileInfo> selectedFileList = new ArrayList<FileInfo>();
        selectedFileList.add(file);
        doOperationDelete(selectedFileList);
    }

    private void doOperationDelete(final ArrayList<FileInfo> selectedFileList) {
        final ArrayList<FileInfo> selectedFiles = new ArrayList<>(selectedFileList);
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        if (selectedFiles.size() == 0) {
            return;
        }
        if (selectedFiles.size() > 1) {
            FileInfo recycle = null;
            for (FileInfo file : selectedFiles) {
                if (file.filePath.equals(FileOperationHelper.RECYCLE_PATH1)
                        || file.filePath.equals(FileOperationHelper.RECYCLE_PATH2)
                        || file.filePath.equals(FileOperationHelper.RECYCLE_PATH3)) {
                    recycle = file;
                }
            }
            if (recycle != null) {
                selectedFiles.remove(recycle);
            }
        }
        String path = selectedFiles.get(0).filePath;
        if (path.equals(FileOperationHelper.RECYCLE_PATH1)
                || path.equals(FileOperationHelper.RECYCLE_PATH2)
                || path.equals(FileOperationHelper.RECYCLE_PATH3)) {
            //clean Recycle
            dialog.setMessage(mContext.getString(R.string.delete_dialog_clean));
        } else if (path.contains(FileOperationHelper.RECYCLE_PATH1)
                || path.contains(FileOperationHelper.RECYCLE_PATH2)
                || path.contains(FileOperationHelper.RECYCLE_PATH3)
                || (path.split("/").length > 3 && path.startsWith("/storage/usb"))) {
            //delete file
            dialog.setMessage(mContext.getString(R.string.delete_dialog_delete));
        } else {
            //move to Recycle
            dialog.setMessage(mContext.getString(R.string.delete_dialog_move));
        }

        dialog.setPositiveButton(R.string.confirm, new DeleteClickListener(selectedFiles))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new OperateUtils.BaseKeyEvent());
        dialog.show();
    }

    private void doOperationDeleteDirect(final ArrayList<FileInfo> selectedFileList) {
        final ArrayList<FileInfo> selectedFiles = new ArrayList<>(selectedFileList);
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        if (selectedFiles.size() == 0) {
            return;
        }
        if (selectedFiles.size() > 1) {
            FileInfo recycle = null;
            for (FileInfo file : selectedFiles) {
                if (file.filePath.equals(FileOperationHelper.RECYCLE_PATH1)
                        || file.filePath.equals(FileOperationHelper.RECYCLE_PATH2)
                        || file.filePath.equals(FileOperationHelper.RECYCLE_PATH3)) {
                    recycle = file;
                }
            }
            if (recycle != null) {
                selectedFiles.remove(recycle);
            }
        }
        String path = selectedFiles.get(0).filePath;
        if (path.equals(FileOperationHelper.RECYCLE_PATH1)
                || path.equals(FileOperationHelper.RECYCLE_PATH2)
                || path.equals(FileOperationHelper.RECYCLE_PATH3)) {
            //clean Recycle
            dialog.setMessage(mContext.getString(R.string.delete_dialog_clean));
        } else {
            //delete file
            dialog.setMessage(mContext.getString(R.string.delete_dialog_delete));
        }

        dialog.setPositiveButton(R.string.confirm, new DeleteDirectClickListener(selectedFiles))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new OperateUtils.BaseKeyEvent());
        dialog.show();
    }

    private class DeleteDirectClickListener implements DialogInterface.OnClickListener {
        ArrayList<FileInfo> mFiles;

        public DeleteDirectClickListener(ArrayList<FileInfo> selectedFiles) {
            super();
            mFiles = selectedFiles;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            new DeleteDirectThread(mFiles).start();
        }
    }

    private class DeleteDirectThread extends Thread {
        ArrayList<FileInfo> mFiles;

        public DeleteDirectThread(ArrayList<FileInfo> selectedFiles) {
            super();
            mFiles = selectedFiles;
        }

        @Override
        public void run() {
            super.run();
            mFileOperationHelper.deleteDirectFile(mMainActivity, mFiles);
        }
    }

    private class DeleteClickListener implements DialogInterface.OnClickListener {
        ArrayList<FileInfo> mFiles;

        public DeleteClickListener(ArrayList<FileInfo> selectedFiles) {
            super();
            mFiles = selectedFiles;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            new DeleteThread(mFiles).start();
        }
    }

    private class DeleteThread extends Thread {
        ArrayList<FileInfo> mFiles;

        public DeleteThread(ArrayList<FileInfo> selectedFiles) {
            super();
            mFiles = selectedFiles;
        }

        @Override
        public void run() {
            super.run();
            mFileOperationHelper.deleteFile(mMainActivity, mFiles);
        }
    }

    public void onOperationInfo() {
        if (getSelectedFileList().size() == 0)
            return;
        FileInfo file = getSelectedFileList().get(0);
        if (file == null)
            return;
        PropertyDialog propertyDialog = new PropertyDialog(mContext, file.filePath);
        propertyDialog.showDialog();
    }

    public void onOperationShare() {
        if (getSelectedFileList().size() == 0)
            return;

        FileInfo file = getSelectedFileList().get(0);
        if (file == null)
            return;

        ShareMenuDialog shareDialog = new ShareMenuDialog(mContext, file.filePath);
        shareDialog.showDialog();
    }

    public void onOperationCompress() {
        if (getSelectedFileList().size() == 0) {
            return;
        }

        FileInfo file = getSelectedFileList().get(0);
        if (file == null) {
            return;
        }
        String path = "";
        for (FileInfo info : getSelectedFileList()) {
            path += Constants.EXTRA_DELETE_FILE_HEADER + info.filePath;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra(Constants.COMPRESS_PATH_TAG, path);
        ComponentName cn = new ComponentName(
                "org.openthos.filemanager", "org.openthos.compress.CompressActivity");
        intent.setComponent(cn);
        mContext.startActivity(intent);
    }

    public void onOperationDecompress() {
        if (getSelectedFileList().size() == 0) {
            return;
        }
        final FileInfo file = getSelectedFileList().get(0);
        if (file == null) {
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.setDataAndType(Uri.fromFile(new File(file.filePath)),
                Constants.getMIMEType(new File(file.filePath)));
        ComponentName cn = new ComponentName(
                "org.openthos.filemanager", "org.openthos.compress.DecompressActivity");
        intent.setComponent(cn);
        mContext.startActivity(intent);
    }

    private Mode mCurrentMode;
    private String mCurrentPath;
    private String mRoot;
    private SystemSpaceFragment.SelectFilesCallback mSelectFilesCallback;

    public boolean isFileSelected(String filePath) {
        return mFileOperationHelper.isFileSelected(filePath);
    }

    public void onListItemClick(MotionEvent event, FileInfo fileInfo) {
        if (fileInfo == null) {
            return;
        }
        if (!fileInfo.IsDir) {
            switch (getMode()) {
                case PICK:
                    mFileViewListener.onPick(fileInfo);
                    break;
                case VIEW:
                    viewFile(fileInfo, event);
                    break;
                case SETWALLPAPER:
                    mFileViewListener.setWallpaper(fileInfo);
                    break;
            }
        } else {
            openSelectFolder(fileInfo.filePath);
            mMainActivity.setHistory(new PathBean(mRoot, fileInfo.filePath));
        }
    }

    public void openSelectFolder(String filePath) {
        mCurrentPath = filePath;
        refreshFileList();
        mMainActivity.setCurPath(filePath);
    }

    public void onOperationOpen(MotionEvent event) {
        if (getSelectedFileList().size() != 0) {
            FileInfo fileInfo = getSelectedFileList().get(0);
            onListItemClick(event, fileInfo);
        }
    }

    public void setMode(Mode m) {
        mCurrentMode = m;
    }

    public Mode getMode() {
        return mCurrentMode;
    }

    public void setRootPath(String path) {
        mRoot = path;
    }

    public String getCurrentPath() {
        return mCurrentPath;
    }

    public void setCurrentPath(String path) {
        mCurrentPath = path;
    }

    public boolean canGoBack() {
        return !mCurrentPath.equals(mRoot);
    }

    public boolean isSelectedAll() {
        return mFileViewListener.getItemCount() != 0
                && mCheckedFileNameList.size() == mFileViewListener.getItemCount();
    }

    public void clearSelection() {
        if (mCheckedFileNameList.size() > 0) {
            for (FileInfo f : mCheckedFileNameList) {
                if (f == null) {
                    continue;
                }
                f.Selected = false;
            }
            mCheckedFileNameList.clear();
            mFileViewListener.onDataChanged();
        }
    }

    private void viewFile(FileInfo lFileInfo, MotionEvent event) {
        try {
            IntentBuilder.viewFile(mContext, lFileInfo.filePath, event);
        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, "fail to view file: " + e.toString());
        }
    }

    public boolean onBackPressed() {
        if (isInSelection()) {
            clearSelection();
        } else if (!onOperationUpLevel()) {
            return false;
        }
        return true;
    }


    @Override
    public void onFileChanged(String path) {
        notifyFileSystemChanged(path);
    }


    public void showContextDialog(FileViewInteractionHub fileViewInteractionHub,
                                  MotionEvent event) {
        menuDialog = new MenuDialog(mContext, fileViewInteractionHub, event);
        menuDialog.showDialog((int) event.getRawX(), (int) event.getRawY());
    }

    public void setIsBlank(boolean isBlank) {
        mIsBlank = isBlank;
    }

    public void setIsDirectory(boolean isDirectory) {
        mIsDirectory = isDirectory;
    }

    public void setCompressFileState(int compressFileState) {
        mCompressFileState = compressFileState;
    }

    public void setIsProtected(boolean isProtected) {
        mIsProtected = isProtected;
    }

    public boolean isDirectory() {
        return mIsDirectory;
    }

    public boolean isBlank() {
        return mIsBlank;
    }

    public int getCompressFileState() {
        return mCompressFileState;
    }

    public boolean isProtected() {
        return mIsProtected;
    }
}
