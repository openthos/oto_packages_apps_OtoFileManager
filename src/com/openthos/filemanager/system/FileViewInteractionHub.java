package com.openthos.filemanager.system;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.ListView;

import com.openthos.filemanager.BaseActivity;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.component.MenuDialog;
import com.openthos.filemanager.component.PropertyDialog;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.T;
import com.openthos.filemanager.fragment.SystemSpaceFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileViewInteractionHub implements FileOperationHelper.IOperationProgressListener {
    private static final String LOG_TAG = "FileViewInteractionHub";
    private IFileInteractionListener mFileViewListener;
    public static Map<String,Integer> saveMulti = new HashMap<>();
    private ArrayList<FileInfo> mCheckedFileNameList = new ArrayList<>();
    private FileOperationHelper mFileOperationHelper;
    private FileSortHelper mFileSortHelper;
    private ProgressDialog progressDialog;
    private Context mContext;
    private CopyOrMove copyOrMoveMode;
    private int selectedDialogItem;
    private MenuDialog menuDialog;

    public enum Mode {
        View, Pick
    }

    public enum CopyOrMove {
        Copy, Move
    }

    public FileViewInteractionHub(IFileInteractionListener fileViewListener) {
        assert (fileViewListener != null);
        mFileViewListener = fileViewListener;
        setup();
        mFileOperationHelper = new FileOperationHelper(this);
        mContext = mFileViewListener.getContext();
        mFileSortHelper = ((BaseActivity)mContext).getFileSortHelper();
    }

    private void showProgress(String msg) {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
    }

    public void sortCurrentList() {
        mFileViewListener.sortCurrentList(mFileSortHelper);
    }

    public boolean canShowCheckBox() {
//        mConfirmOperationBar.getVisibility() != View.VISIBLE
        return true;
    }
//
//    private void showConfirmOperationBar(boolean show) {
//        mConfirmOperationBar.setVisibility(show ? View.VISIBLE : View.GONE);
//    }

    public void addDialogSelectedItem(FileInfo fileInfo) {
        mCheckedFileNameList.add(fileInfo);
    }

    public void addDragSelectedItem(int position) {
        if (mCheckedFileNameList.size() == 0) {
            selectedDialogItem = position;
            if (selectedDialogItem != -1) {
                FileInfo fileInfo = mFileViewListener.getItem(selectedDialogItem);
                if (fileInfo != null) {
                    fileInfo.Selected = true;
                    mCheckedFileNameList.add(fileInfo);
                }
            }
        }
    }

    public void removeDialogSelectedItem(FileInfo fileInfo){
        mCheckedFileNameList.remove(fileInfo);
    }

    public ArrayList<FileInfo> getSelectedFileList() {
        return mCheckedFileNameList;
    }

    public ArrayList<FileInfo> getCheckedFileList() {
        return mFileOperationHelper.getFileList();
    }

    public void onOperationDragConfirm(String filePath) {
        if (isSelectingFiles()) {
            mSelectFilesCallback.selected(mCheckedFileNameList);
            mSelectFilesCallback = null;
            clearSelection();
        } else if (mFileOperationHelper.isMoveState()) {
            if (mFileOperationHelper.EndMove(filePath)) {
                showProgress(mContext.getString(R.string.operation_moving));
            }
        } else {
            onOperationDragPaste(filePath);
        }
    }

    public void onOperationDragPaste(String filePath) {
        if (mFileOperationHelper.Paste(filePath)) {
            showProgress(mContext.getString(R.string.operation_pasting));
        }
    }

    public void setCheckedFileList(ArrayList<FileInfo> fileInfoList, CopyOrMove copyOrMove) {
        if (fileInfoList != null && fileInfoList.size() > 0)
            mCheckedFileNameList.addAll(fileInfoList);
        switch (copyOrMove) {
            case Move:
                onOperationMove();
                break;
            default:
            case Copy:
                doOnOperationCopy();
                break;
        }
    }

    public CopyOrMove getCurCopyOrMoveMode() {
        return copyOrMoveMode;
    }

    public boolean canPaste() {
        return mFileOperationHelper.canPaste();
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
//                showConfirmOperationBar(false);
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

    public boolean isMoveState() {
        return mFileOperationHelper.isMoveState() || mFileOperationHelper.canPaste();
    }

    private void setup() {
        setupFileListView();
//        setupOperationPane();
    }

//    // buttons
//    private void setupOperationPane() {
//        mConfirmOperationBar = mFileViewListener.getViewById(R.id.moving_operation_bar);
//        setupClick(mConfirmOperationBar, R.id.button_moving_confirm);
//        setupClick(mConfirmOperationBar, R.id.button_moving_cancel);
//    }

//    private void setupClick(View v, int id) {
//        //TODO
//        View button = (v != null ? v.findViewById(id) : mFileViewListener.getViewById(id));
//        if (button != null)
//            button.setOnClickListener(buttonClick);
//    }

//    private View.OnClickListener buttonClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.button_operation_copy:
//                    doOnOperationCopy();
//                    break;
//                case R.id.button_operation_move:
//                    onOperationMove();
//                    break;
//                case R.id.button_operation_send:
//                    onOperationSend();
//                    break;
//                case R.id.button_operation_delete:
//                    onOperationDelete();
//                    break;
//                case R.id.button_operation_cancel:
//                    onOperationSelectAllOrCancel();
//                    break;
//                case R.id.button_moving_confirm:
//                    onOperationButtonConfirm();
//                    break;
//                case R.id.button_moving_cancel:
//                    onOperationButtonCancel();
//                    break;
//            }
//        }
//
//    };

    public void onOperationReferesh() {
        refreshFileList();
    }

    private void onOperationSetting() {
        Intent intent = new Intent(mContext, FileManagerPreferenceActivity.class);
        try {
            mContext.startActivity(intent);
            clearSelection();
        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, "fail to start setting: " + e.toString());
        }
    }

    public void onOperationShowSysFiles() {
        Settings.instance().setShowDotAndHiddenFiles(!Settings.instance()
                                                     .getShowDotAndHiddenFiles());
        refreshFileList();
    }

    public void onOperationSelectAllOrCancel() {
        if (!isSelectedAll()) {
            onOperationSelectAll();
        } else {
            clearSelection();
        }
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
        if (mFileViewListener.onOperation(Constants.OPERATION_UP_LEVEL)) {
            return true;
        }
        if (!mRoot.equals(mCurrentPath)) {
            mCurrentPath = new File(mCurrentPath).getParent();
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

    private boolean doCreateFolder(String text) {
        if (TextUtils.isEmpty(text)) {
            clearSelection();
            return false;
        }

        if (mFileOperationHelper.CreateFolder(mCurrentPath, text)) {
            mFileViewListener.addSingleFile(Util.GetFileInfo(Util.makePath(mCurrentPath, text)));
            if ("list".equals(LocalCache.getViewTag())) {
                mFileListView.setSelection(mFileListView.getCount() - 1);
            } else if ("grid".equals(LocalCache.getViewTag())) {
                mFileGridView.setSelection(mFileGridView.getCount() - 1);
            }
            clearSelection();
        } else {
            new AlertDialog.Builder(mContext)
                           .setMessage(mContext.getString(R.string.fail_to_create_folder))
                           .setPositiveButton(R.string.confirm, null).create().show();
            clearSelection();
            return false;
        }

        return true;
    }

    private boolean doCreateFile(String text) {
        if (TextUtils.isEmpty(text)) {
            clearSelection();
            return false;
        }

        if (mFileOperationHelper.CreateFile(mCurrentPath, text)) {
            mFileViewListener.addSingleFile(Util.GetFileInfo(Util.makePath(mCurrentPath, text)));
            if ("list".equals(LocalCache.getViewTag())) {
                mFileListView.setSelection(mFileListView.getCount() - 1);
            } else if ("grid".equals(LocalCache.getViewTag())) {
                mFileGridView.setSelection(mFileGridView.getCount() - 1);
            }
            clearSelection();
        } else {
            new AlertDialog.Builder(mContext)
                           .setMessage(mContext.getString(R.string.fail_to_create_folder))
                           .setPositiveButton(R.string.confirm, null).create().show();
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

    public void doOnOperationCopy() {
        copyOrMoveMode = CopyOrMove.Copy;
        onOperationCopy(getSelectedFileList());
    }

    public void onOperationCopy(ArrayList<FileInfo> files) {
        mFileOperationHelper.Copy(files);
//        clearSelection();
//        showConfirmOperationBar(true);
//        View confirmButton = mConfirmOperationBar.findViewById(R.id.button_moving_confirm);
//        confirmButton.setEnabled(false);
        // refresh to hide selected files
//        refreshFileList();
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

    public void onOperationPaste() {
        if (mFileOperationHelper.Paste(mCurrentPath)) {
            showProgress(mContext.getString(R.string.operation_pasting));
        }
    }

    public void onOperationMove() {
        mFileOperationHelper.StartMove(getSelectedFileList());
        clearSelection();
//        showConfirmOperationBar(true);
//        View confirmButton = mConfirmOperationBar.findViewById(R.id.button_moving_confirm);
//        confirmButton.setEnabled(false);
        // refresh to hide selected files
        refreshFileList();
        copyOrMoveMode = CopyOrMove.Move;
    }

    public void refreshFileList() {
        clearSelection();
        updateNavigationPane();
        mFileViewListener.onRefreshFileList(mCurrentPath, mFileSortHelper);

        // update move operation button state
//        updateConfirmButtons();
    }

//    private void updateConfirmButtons() {
//        if (mConfirmOperationBar.getVisibility() == View.GONE)
//            return;
//
//        Button confirmButton = (Button) mConfirmOperationBar
//                                        .findViewById(R.id.button_moving_confirm);
//        int text = R.string.operation_paste;
//        if (isSelectingFiles()) {
//            confirmButton.setEnabled(mCheckedFileNameList.size() != 0);
//            text = R.string.operation_send;
//        } else if (isMoveState()) {
//            confirmButton.setEnabled(mFileOperationHelper.canMove(mCurrentPath));
//        }
//
//        confirmButton.setText(text);
//    }

    private void updateNavigationPane() {
        ((MainActivity) mContext).setNavigationBar(mFileViewListener.getDisplayPath(mCurrentPath));
    }

    public void onOperationSend() {
        ArrayList<FileInfo> selectedFileList = getSelectedFileList();
        for (FileInfo f : selectedFileList) {
            if (f.IsDir) {
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                                         .setMessage(R.string.error_info_cant_send_folder)
                                         .setPositiveButton(R.string.confirm, null).create();
                dialog.show();
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
        int pos = selectedDialogItem;
        if (pos == -1)
            return;

        if (getSelectedFileList().size() == 0)
            return;

        final FileInfo f = getSelectedFileList().get(0);
        clearSelection();

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

    private boolean doRename(final FileInfo f, String text) {
        if (TextUtils.isEmpty(text))
            return false;

        if (mFileOperationHelper.Rename(f, text)) {
            f.fileName = text;
            mFileViewListener.onDataChanged();
        } else {
            new AlertDialog.Builder(mContext)
                           .setMessage(mContext.getString(R.string.fail_to_rename))
                           .setPositiveButton(R.string.confirm, null).create().show();
            return false;
        }
        refreshFileList();
        return true;
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
        if (selectedFileList.size() == 0) {
            return;
        }
        String path = selectedFiles.get(0).filePath;
        if (path.equals(FileOperationHelper.RECYCLE_PATH1)
                || path.equals(FileOperationHelper.RECYCLE_PATH2)
                || path.equals(FileOperationHelper.RECYCLE_PATH3)) {
            //clean Recycle
            dialog.setMessage(mContext.getString(R.string.delete_dialog_clean));
        } else if (path.contains(FileOperationHelper.RECYCLE_PATH1)
                || path.contains(FileOperationHelper.RECYCLE_PATH2)
                || path.contains(FileOperationHelper.RECYCLE_PATH3)) {
            //delete file
            dialog.setMessage(mContext.getString(R.string.delete_dialog_delete));
        } else {
            //move to Recycle
            dialog.setMessage(mContext.getString(R.string.delete_dialog_move));
        }

        dialog.setPositiveButton(R.string.confirm, new DeleteClickListener(path))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearSelection();
                    }
                }).create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void doOperationDeleteDirect(final ArrayList<FileInfo> selectedFileList) {
        final ArrayList<FileInfo> selectedFiles = new ArrayList<>(selectedFileList);
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        if (selectedFileList.size() == 0) {
            return;
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

        dialog.setPositiveButton(R.string.confirm, new DeleteDirectClickListener(path))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearSelection();
                    }
                }).create();
        dialog.setCancelable(false);
        dialog.show();
    }

    class DeleteDirectClickListener implements DialogInterface.OnClickListener {
        String mPath;

        public DeleteDirectClickListener(String path) {
            super();
            mPath = path;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            new DeleteDirectThread(mPath).start();
        }
    }

    class DeleteDirectThread extends Thread {
        String mPath;

        public DeleteDirectThread(String path) {
            super();
            mPath = path;
        }

        @Override
        public void run() {
            super.run();
            FileOperationHelper.deleteDirectFile(mPath);
        }
    }

    class DeleteClickListener implements DialogInterface.OnClickListener {
        String mPath;

        public DeleteClickListener(String path) {
            super();
            mPath = path;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            new DeleteThread(mPath).start();
        }
    }

    class DeleteThread extends Thread {
        String mPath;

        public DeleteThread(String path) {
            super();
            mPath = path;
        }

        @Override
        public void run() {
            super.run();
            FileOperationHelper.deleteFile(mPath);
        }
    }

    public void onOperationInfo() {
        if (getSelectedFileList().size() == 0)
            return;

        FileInfo file = getSelectedFileList().get(0);
        if (file == null)
            return;

        //InformationDialog dialog = new InformationDialog(mContext, file, mFileViewListener
        //        .getFileIconHelper());
        //dialog.show();
        PropertyDialog propertyDialog = new PropertyDialog(mContext, file.filePath);
        propertyDialog.showDialog();
        clearSelection();
    }

    public void onOperationButtonConfirm() {
        if (isSelectingFiles()) {
            mSelectFilesCallback.selected(mCheckedFileNameList);
            mSelectFilesCallback = null;
            clearSelection();
        } else if (mFileOperationHelper.isMoveState()) {
            if (mFileOperationHelper.EndMove(mCurrentPath)) {
                showProgress(mContext.getString(R.string.operation_moving));
            }
        } else {
            onOperationPaste();
        }
    }

    public void onOperationButtonCancel() {
        mFileOperationHelper.clear();
//        showConfirmOperationBar(false);
        if (isSelectingFiles()) {
            mSelectFilesCallback.selected(null);
            mSelectFilesCallback = null;
            clearSelection();
        } else if (mFileOperationHelper.isMoveState()) {
            // refresh to show previously selected hidden files
            mFileOperationHelper.EndMove(null);
            refreshFileList();
        } else {
            refreshFileList();
        }
    }

    // File List view setup
    private GridView mFileGridView;
    private ListView mFileListView;

    private void setupFileListView() {
        final String title = LocalCache.getViewTag();
        if ("list".equals(title)) {
            mFileListView = (ListView) mFileViewListener.getViewById(R.id.file_path_list);
        } else if ("grid".equals(title)) {
            mFileGridView = (GridView) mFileViewListener.getViewById(R.id.file_path_grid);
        }
    }

    private FileViewInteractionHub.Mode mCurrentMode;
    private String mCurrentPath;
    private String mRoot;
    private SystemSpaceFragment.SelectFilesCallback mSelectFilesCallback;
    public boolean isFileSelected(String filePath) {
        return mFileOperationHelper.isFileSelected(filePath);
    }

    public void onListItemClick(int position, String doubleTag,
                                MotionEvent event, FileInfo fileInfo) {
        if (fileInfo == null) {
            Log.e(LOG_TAG, "file does not exist on position:" + position);
            return;
        }

//        if (isInSelection()) {
//            boolean selected = lFileInfo.Selected;
//
////            ImageView checkBox = (ImageView) view.findViewById(R.id.file_checkbox);
//            LinearLayout ll_grid_item_bg = (LinearLayout) view.findViewById
//                                                          (R.id.ll_grid_item_bg);
//            if (selected) {
//                mCheckedFileNameList.remove(lFileInfo);
//                ll_grid_item_bg.setSelected(false);
////                checkBox.setImageResource(R.mipmap.btn_check_off_holo_light);
//            } else {
//                mCheckedFileNameList.add(lFileInfo);
//                ll_grid_item_bg.setSelected(false);
////                checkBox.setImageResource(R.mipmap.btn_check_on_holo_light);
//            }
//            lFileInfo.Selected = !selected;
//            return;
//        }

        if (!fileInfo.IsDir && doubleTag != null) {
            if (mCurrentMode == Mode.Pick) {
                mFileViewListener.onPick(fileInfo);
            } else {
                viewFile(fileInfo,event);
            }
        } else if (doubleTag != null && Constants.DOUBLE_TAG.equals(doubleTag)) {
//            mCheckedFileNameList.remove(lFileInfo);  //
            mCurrentPath = getAbsoluteName(mCurrentPath, fileInfo.fileName);
            refreshFileList();
        }
    }

    public void onOperationOpen(MotionEvent event) {
        if (getSelectedFileList().size() != 0) {
            FileInfo fileInfo = getSelectedFileList().get(0);
            onListItemClick(selectedDialogItem, Constants.DOUBLE_TAG, event, fileInfo);
        }
    }

    public void setBackground(int position, FileInfo lFileInfo) {
        if (lFileInfo == null) {
            Log.e(LOG_TAG, "file does not exist on position:" + position);
            return;
        }
        if (!lFileInfo.Selected ) {
            lFileInfo.Selected = true;

            mCheckedFileNameList.add(lFileInfo);
//            view.setSelected(true);
        } else {
            lFileInfo.Selected = false;
            mCheckedFileNameList.remove(lFileInfo);
//            view.setSelected(false);
        }
//        lFileInfo.Selected = !selected;

        L.e("mCheckedFileNameList", mCheckedFileNameList.size() + "");
    }

    public void setMode(Mode m) {
        mCurrentMode = m;
    }

    public Mode getMode() {
        return mCurrentMode;
    }

    public void setRootPath(String path) {
        mRoot = path;
        mCurrentPath = path;
    }

    public String getRootPath() {
        return mRoot;
    }

    public String getCurrentPath() {
        return mCurrentPath;
    }

    public void setCurrentPath(String path) {
        mCurrentPath = path;
    }

    private String getAbsoluteName(String path, String name) {
        return path.equals(Constants.SD_PATH) ? path + name : path + File.separator + name;
    }

    // check or uncheck
    public boolean onCheckItem(FileInfo f, View v) {
        if (isMoveState())
            return false;

        if (isSelectingFiles() && f.IsDir)
            return false;

        if (f.Selected) {
            mCheckedFileNameList.add(f);
        } else {
            mCheckedFileNameList.remove(f);
        }
        return true;
    }

    private boolean isSelectingFiles() {
        return mSelectFilesCallback != null;
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

//    public void copyFile(ArrayList<FileInfo> files) {
//        mFileOperationHelper.Copy(files);
//    }
//
//    public void moveFileFrom(ArrayList<FileInfo> files) {
//        mFileOperationHelper.StartMove(files);
//        showConfirmOperationBar(true);
//        updateConfirmButtons();
//        // refresh to hide selected files
//        refreshFileList();
//    }

    @Override
    public void onFileChanged(String path) {
        notifyFileSystemChanged(path);
    }
//
//    public void startSelectFiles(SystemSpaceFragment.SelectFilesCallback callback) {
//        mSelectFilesCallback = callback;
//        showConfirmOperationBar(true);
//        updateConfirmButtons();
//    }

    public void MouseScrollAction(MotionEvent event) {
        if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
            L.i("fortest::onGenericMotionEvent", "down");
        }
        else {
            L.i("fortest::onGenericMotionEvent", "up");
        }
    }

    public void shownContextDialog(FileViewInteractionHub mFileViewInteractionHub,
                                   MotionEvent event) {
        menuDialog = new MenuDialog(mContext, R.style.menu_dialog, mFileViewInteractionHub, event);
        menuDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        menuDialog.showDialog((int) event.getRawX(), (int) event.getRawY());
//                menuDialog.setEnablePaste(canPaste);
    }

    public void dismissContextDialog() {
        menuDialog.dismiss();
    }

//    // menu
//    private static final int MENU_SEARCH = 1;
//    //     private static final int MENU_NEW_FOLDER = 2;
////    MENU_SORT = 3;
//    private static final int MENU_SORT = 0;
//    private static final int MENU_SEND = 7;
//    private static final int MENU_RENAME = 8;
//    private static final int MENU_DELETE = 9;
//    private static final int MENU_INFO = 10;
//    private static final int MENU_SORT_NAME = 11;
//    private static final int MENU_SORT_SIZE = 12;
//    private static final int MENU_SORT_DATE = 13;
//    private static final int MENU_SORT_TYPE = 14;
//    private static final int MENU_REFRESH = 15;
//    private static final int MENU_SELECTALL = 16;
//    private static final int MENU_SETTING = 17;
//    private static final int MENU_EXIT = 18;
//
//    public View.OnCreateContextMenuListener mListViewContextMenuListener
//                                            = new View.OnCreateContextMenuListener() {
//        @Override
//        public void onCreateContextMenu(ContextMenu menu, View v,
//                                        ContextMenu.ContextMenuInfo menuInfo) {
//            if (isInSelection() || isMoveState())
//                return;
//
//            AdapterView.AdapterContextMenuInfo info
//                                               = (AdapterView.AdapterContextMenuInfo) menuInfo;
//
//            SubMenu sortMenu = menu.addSubMenu(0, MENU_SORT, 0, R.string.menu_item_sort).setIcon(
//                    R.drawable.ic_menu_sort);
//            addMenuItem(sortMenu, MENU_SORT_NAME, 0, R.string.menu_item_sort_name);
//            addMenuItem(sortMenu, MENU_SORT_SIZE, 1, R.string.menu_item_sort_size);
//            addMenuItem(sortMenu, MENU_SORT_DATE, 2, R.string.menu_item_sort_date);
//            addMenuItem(sortMenu, MENU_SORT_TYPE, 3, R.string.menu_item_sort_type);
//            sortMenu.setGroupCheckable(0, true, true);
//            sortMenu.getItem(0).setChecked(true);
//
//            addMenuItem(menu, Constants.MENU_COPY, 0, R.string.operation_copy);
//            addMenuItem(menu, Constants.MENU_COPY_PATH, 0, R.string.operation_copy_path);
//            addMenuItem(menu, Constants.MENU_PASTE, 0,
//                    R.string.operation_paste);
//            addMenuItem(menu, Constants.MENU_MOVE, 0, R.string.operation_move);
//            addMenuItem(menu, MENU_SEND, 0, R.string.operation_send);
//            addMenuItem(menu, MENU_RENAME, 0, R.string.operation_rename);
//            addMenuItem(menu, MENU_DELETE, 0, R.string.operation_delete);
//            addMenuItem(menu, MENU_INFO, 0, R.string.operation_info);
//            addMenuItem(menu, Constants.MENU_NEW_FOLDER, 0, R.string.operation_folder);
//            if (!canPaste()) {
//                MenuItem menuItem = menu.findItem(Constants.MENU_PASTE);
//                if (menuItem != null)
//                    menuItem.setEnabled(false);
//            }
//        }
//    };
//
//    private void addMenuItem(Menu menu, int itemId, int order, int string) {
//        addMenuItem(menu, itemId, order, string, -1);
//    }
//
//    private void addMenuItem(Menu menu, int itemId, int order, int string, int iconRes) {
//        if (!mFileViewListener.shouldHideMenu(itemId)) {
//            MenuItem item = menu.add(0, itemId, order, string)
//                                .setOnMenuItemClickListener(menuItemClick);
//            if (iconRes > 0) {
//                item.setIcon(iconRes);
//            }
//        }
//    }
//
//    private MenuItem.OnMenuItemClickListener menuItemClick
//                                             = new MenuItem.OnMenuItemClickListener() {
//
//        @Override
//        public boolean onMenuItemClick(MenuItem item) {
//            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)
//                                                      item.getMenuInfo();
//            mListViewContextMenuSelectedItem = info != null ? info.position : -1;
//
//            int itemId = item.getItemId();
//            if (mFileViewListener.onOperation(itemId)) {
//                return true;
//            }
//
//            addContextMenuSelectedItem();
//
//            switch (itemId) {
//                case Constants.MENU_NEW_FOLDER:
//                    onOperationCreateFolder();
//                    break;
//                case MENU_REFRESH:
//                    onOperationReferesh();
//                    break;
//                case MENU_SELECTALL:
//                    onOperationSelectAllOrCancel();
//                    break;
//                case Constants.MENU_SHOWHIDE:
//                    onOperationShowSysFiles();
//                    break;
//                case MENU_SETTING:
//                    onOperationSetting();
//                    break;
//                case MENU_EXIT:
//                    ((MainActivity) mContext).finish();
//                    break;
//                // sort
//                case MENU_SORT_NAME:
//                    item.setChecked(true);
//                    onSortChanged(FileSortHelper.SortMethod.name);
//                    break;
//                case MENU_SORT_SIZE:
//                    item.setChecked(true);
//                    onSortChanged(FileSortHelper.SortMethod.size);
//                    break;
//                case MENU_SORT_DATE:
//                    item.setChecked(true);
//                    onSortChanged(FileSortHelper.SortMethod.date);
//                    break;
//                case MENU_SORT_TYPE:
//                    item.setChecked(true);
//                    onSortChanged(FileSortHelper.SortMethod.type);
//                    break;
//
//                case Constants.MENU_COPY:
//                    doOnOperationCopy();
//                    break;
//                case Constants.MENU_COPY_PATH:
//                    onOperationCopyPath();
//                    break;
//                case Constants.MENU_PASTE:
//                    onOperationPaste();
//                    break;
//                case Constants.MENU_MOVE:
//                    onOperationMove();
//                    break;
//                case MENU_SEND:
//                    onOperationSend();
//                    break;
//                case MENU_RENAME:
//                    onOperationRename();
//                    break;
//                case MENU_DELETE:
//                    onOperationDelete();
//                    break;
//                case MENU_INFO:
//                    onOperationInfo();
//                    break;
//                default:
//                    return false;
//            }
//
//            mListViewContextMenuSelectedItem = -1;
//            return true;
//        }
//    };
//
//    private int mListViewContextMenuSelectedItem;
//
//    public void addContextMenuSelectedItem() {
//        if (mCheckedFileNameList.size() == 0) {
//            int pos = mListViewContextMenuSelectedItem;
//            if (pos != -1) {
//                FileInfo fileInfo = mFileViewListener.getItem(pos);
//                if (fileInfo != null) {
//                    mCheckedFileNameList.add(fileInfo);
//                }
//            }
//        }
//    }
//
//    public void onListItemClick(AdapterView<?> parent, View view, int position, long id) {
//        FileInfo lFileInfo = mFileViewListener.getItem(position);
//
//        if (lFileInfo == null) {
//            Log.e(LOG_TAG, "file does not exist on position:" + position);
//            return;
//        }
//
//        if (isInSelection()) {
//            boolean selected = lFileInfo.Selected;
//            ImageView checkBox = (ImageView) view.findViewById(R.id.file_checkbox);
//            if (selected) {
//                mCheckedFileNameList.remove(lFileInfo);
//                checkBox.setImageResource(R.mipmap.btn_check_off_holo_light);
//            } else {
//                mCheckedFileNameList.add(lFileInfo);
//                checkBox.setImageResource(R.mipmap.btn_check_on_holo_light);
//            }
//            lFileInfo.Selected = !selected;
//            return;
//        }
//
//        if (!lFileInfo.IsDir) {
//            if (mCurrentMode == Mode.Pick) {
//                mFileViewListener.onPick(lFileInfo);
//            } else {
//                viewFile(lFileInfo);
//            }
//            return;
//        }
//
//        mCurrentPath = getAbsoluteName(mCurrentPath, lFileInfo.fileName);
//        refreshFileList();
//    }
}
