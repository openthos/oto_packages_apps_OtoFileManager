package com.openthos.filemanager.system;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileOperationHelper {
    private static final String LOG_TAG = "FileOperation";
    private static final String TAG = FileOperationHelper.class.getSimpleName();
    private final ArrayList<FileInfo> mCurFileNameList = new ArrayList<>();
    private boolean mMoving;
    private IOperationProgressListener mOperationListener;
    private FilenameFilter mFilter;

    public interface IOperationProgressListener {
        void onFinish();
        void onFileChanged(String path);
    }

    public FileOperationHelper(IOperationProgressListener l) {
        mOperationListener = l;
    }

    public void setFilenameFilter(FilenameFilter f) {
        mFilter = f;
    }

    public boolean CreateFolder(String path, String name) {
        Log.v(LOG_TAG, "CreateFolder >>> " + path + "," + name);
        File f = new File(Util.makePath(path, name));
        if (f.exists())
            return false;

        return f.mkdir();
    }

    public boolean CreateFile(String path, String name) {
        Log.v(LOG_TAG, "CreateFile >>> " + path);
        File dir = new File(Util.makePath(path, name));
        if (!dir.exists()){
            try {
              return  dir.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void Copy(ArrayList<FileInfo> files) {
        copyFileList(files);
    }

    public boolean Paste(String path) {
        if (mCurFileNameList.size() == 0)
            return false;

        final String _path = path;
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                for (FileInfo f : mCurFileNameList) {
                    CopyFile(f, _path);
                }

                mOperationListener.onFileChanged(Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath());

                clear();
            }
        });

        return true;
    }

    public boolean canPaste() {
        return mCurFileNameList.size() != 0;
    }

    public void StartMove(ArrayList<FileInfo> files) {
        if (mMoving)
            return;

        mMoving = true;
        copyFileList(files);
    }

    public boolean isMoveState() {
        return mMoving;
    }

    public boolean canMove(String path) {
        for (FileInfo f : mCurFileNameList) {
            if (!f.IsDir)
                continue;

            if (Util.containsPath(f.filePath, path))
                return false;
        }

        return true;
    }

    public void clear() {
        synchronized(mCurFileNameList) {
            mCurFileNameList.clear();
        }
    }

    public boolean EndMove(String path) {
        if (!mMoving)
            return false;
        mMoving = false;

        if (TextUtils.isEmpty(path))
            return false;

        final String _path = path;
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                    for (FileInfo f : mCurFileNameList) {
                        MoveFile(f, _path);
                    }

                    mOperationListener.onFileChanged(Environment
                            .getExternalStorageDirectory()
                            .getAbsolutePath());

                    clear();
                }
        });

        return true;
    }

    public ArrayList<FileInfo> getFileList() {
        return mCurFileNameList;
    }

    private void asnycExecute(Runnable r) {
        final Runnable _r = r;
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                synchronized(mCurFileNameList) {
                    _r.run();
                }
                if (mOperationListener != null) {
                    mOperationListener.onFinish();
                }

                return null;
            }
        }.execute();
    }

    public boolean isFileSelected(String path) {
        synchronized(mCurFileNameList) {
            for (FileInfo f : mCurFileNameList) {
                if (f.filePath.equalsIgnoreCase(path))
                    return true;
            }
        }
        return false;
    }

    public boolean Rename(FileInfo f, String newName) {
        if (f == null || newName == null) {
            Log.e(LOG_TAG, "Rename: null parameter");
            return false;
        }

        File file = new File(f.filePath);
        String newPath = Util.makePath(Util.getPathFromFilepath(f.filePath), newName);
        final boolean needScan = file.isFile();
        try {
            boolean ret = file.renameTo(new File(newPath));
            if (ret) {
                if (needScan) {
                    mOperationListener.onFileChanged(f.filePath);
                }
                mOperationListener.onFileChanged(newPath);
            }
            return ret;
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Fail to rename file," + e.toString());
        }
        return false;
    }

    public boolean Delete(ArrayList<FileInfo> files) {
        copyFileList(files);
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                for (FileInfo f : mCurFileNameList) {
                    DeleteFile(f);
                }

                mOperationListener.onFileChanged(Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath());

                clear();
            }
        });
        return true;
    }
    //删除1个文件
    protected void DeleteFile(FileInfo f) {
        if (f == null) {
            Log.e(LOG_TAG, "DeleteFile: null parameter");
            return;
        }

        File file = new File(f.filePath);
        boolean directory = file.isDirectory();
        if (directory) {
            for (File child : file.listFiles(mFilter)) {
                if (Util.isNormalFile(child.getAbsolutePath())) {
                    DeleteFile(Util.GetFileInfo(child, mFilter, true));
                }
            }
        }

        file.delete();
        Log.v(LOG_TAG, "DeleteFile >>> " + f.filePath);
    }

    private static void copyOrMoveFile(String command, String arg, String srcFile, String destDir) {
        try {
            File f = new File(destDir, new File(srcFile).getName());
            File destFile = f;
            if (f.exists()) {
                for (int i = 2; ; i++) {
                    File current = new File(f.getAbsolutePath() + "." + i);
                    if (!current.exists()) {
                        destFile= new File(destDir, current.getName());
                        break;
                    }
                }
            }
            Runtime.getRuntime().exec(new String[] {command, arg, srcFile,
                                                    destFile.getAbsolutePath()});
        } catch (IOException e) {
        }
    }

    private void CopyFile(FileInfo f, String dest) {
        String command = "/system/xbin/cp";
        String arg = "-v";
        File file = new File(f.filePath);
        if (file.isDirectory()){
            arg = "-rv";
        }
        copyOrMoveFile(command, arg, f.filePath, dest);
//        if (f == null || dest == null) {
//            Log.e(LOG_TAG, "CopyFile: null parameter");
//            return;
//        }

//        File file = new File(f.filePath);
//        if (file.isDirectory()) {

            // directory exists in destination, rename it
//            String destPath = Util.makePath(dest, f.fileName);
//            File destFile = new File(destPath);
//            int i = 1;
//            while (destFile.exists()) {
//                destPath = Util.makePath(dest, f.fileName + " " + i++);
//                destFile = new File(destPath);
//            }

//            for (File child : file.listFiles(mFilter)) {
//                if (!child.isHidden() && Util.isNormalFile(child.getAbsolutePath())) {
//                    CopyFile(Util.GetFileInfo(child, mFilter,
//                                  Settings.instance().getShowDotAndHiddenFiles()), destPath);
//                }
//            }
//        } else {
//            String destFile = Util.copyFile(f.filePath, dest);
//        }
//        Log.v(LOG_TAG, "CopyFile >>> " + f.filePath + "," + dest);
    }

    private boolean MoveFile(FileInfo f, String dest) {
        String command = "/system/xbin/mv";
        String arg = "-v";
        copyOrMoveFile(command, arg, f.filePath, dest);
//        Log.v(LOG_TAG, "MoveFile >>> " + f.filePath + "," + dest);

//        if (dest == null) {
//            Log.e(LOG_TAG, "CopyFile: null parameter");
//            return false;
//        }

//        File file = new File(f.filePath);
//        String newPath = Util.makePath(dest, f.fileName);
//        try {
//            return file.renameTo(new File(newPath));
//        } catch (SecurityException e) {
//            Log.e(LOG_TAG, "Fail to move file," + e.toString());
//        }
        return false;
    }

    private void copyFileList(ArrayList<FileInfo> files) {
        synchronized(mCurFileNameList) {
            mCurFileNameList.clear();
            for (FileInfo f : files) {
                mCurFileNameList.add(f);
            }
        }
    }

    public static List<File> getALLMemoryFile() {
        try {
            List<File> list = new ArrayList<>();
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            String mount = "";
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
                if (line.contains("fat")) {
                    String columns[] = line.split(" ");
                    if (columns.length > 1) {
                        mount = mount.concat(columns[1] + "*");
                    }
                }
                else if (line.contains("fuse")) {
                    String columns[] = line.split(" ");
                    if (columns.length > 1) {
                        mount = mount.concat(columns[1] + "*");
                    }
                }
            }
            String[] paths = mount.split("\\*");
            for (String s : paths) {
                File f = new File(s);
                list.add(f);
            }
            return list;
        } catch (Exception e) {
            Log.e(TAG, "e.toString()=" + e.toString());
            return null;
        }
    }
}
