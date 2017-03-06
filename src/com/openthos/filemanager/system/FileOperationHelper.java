package com.openthos.filemanager.system;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
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

import com.openthos.filemanager.MainActivity;

public class FileOperationHelper {
    private static final String LOG_TAG = "FileOperation";
    private static final String SUFFIX_TXT = "txt";
    private static final String SUFFIX_DOC = "doc";
    private static final String SUFFIX_XLS = "xls";
    private static final String TAG = FileOperationHelper.class.getSimpleName();
    private final ArrayList<FileInfo> mCurFileNameList = new ArrayList<>();
    private boolean mMoving;
    private IOperationProgressListener mOperationListener;
    private FilenameFilter mFilter;
    public static final String RECYCLE_PATH1 = "/storage/emulated/0/Recycle";
    public static final String RECYCLE_PATH2 = "/storage/emulated/legacy/Recycle";
    public static final String RECYCLE_PATH3 = "/sdcard/Recycle";

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
        if (!dir.exists()) {
            String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
            if (SUFFIX_TXT.equals(end)) {
                Util.exec(new String[]{"cp", "-i", "/data/create/ben.txt",
                                        dir.getAbsolutePath()});
            } else if (SUFFIX_DOC.equals(end)) {
                Util.exec(new String[]{"cp", "-i", "/data/create/wen.doc",
                                        dir.getAbsolutePath()});
            } else if (SUFFIX_XLS.equals(end)) {
                Util.exec(new String[]{"cp", "-i", "/data/create/biao.xls",
                                        dir.getAbsolutePath()});
            } else {
                Util.exec(new String[]{"cp", "-i", "/data/create/yan.ppt",
                                        dir.getAbsolutePath()});
            }
        } else {
            return false;
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
        synchronized (mCurFileNameList) {
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
                synchronized (mCurFileNameList) {
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
        synchronized (mCurFileNameList) {
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
        if (!new File(newPath).exists()) {
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

    private static void copyOrMoveFile(String command, String arg,
                                       String srcFile, String destDir, boolean isRefreah) {
        copyOrMoveFile(command, arg, srcFile, destDir, isRefreah, false);
    }

    private static void copyOrMoveFile(String command, String arg, String srcFile,
                                       String destDir, boolean isRefreah, boolean isRecycle) {
        Process pro;
        BufferedReader in = null;
        File f = new File(destDir, new File(srcFile).getName());
        File destFile = f;
        File sourceFile = new File(srcFile);
        try {
            if (sourceFile.isDirectory()) {
                if (f.exists()) {
                    for (int i = 2; ; i++) {
                        File current = new File(f.getAbsolutePath() + "." + i);
                        if (!current.exists()) {
                            destFile = new File(destDir, current.getName());
                            break;
                        }
                    }
                }
            } else if (sourceFile.isFile()) {
                if (f.exists()) {
                    String suffix;
                    if (f.getAbsolutePath().contains(".")) {
                        suffix = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf("."),
                                                               f.getAbsolutePath().length());
                    } else {
                        suffix = "";
                    }
                    for (int i = 2; ; i++) {
                        File current = new File(f.getAbsolutePath().replace(suffix, "") + "."
                                                + i + suffix);
                        if (!current.exists()) {
                            destFile = new File(destDir, current.getName());
                            break;
                        }
                    }
                }
            }
            pro = Runtime.getRuntime().exec(new String[]{command, arg, srcFile,
                    destFile.getAbsolutePath()});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_SHOW);
            int i = 0;
            while ((line = in.readLine()) != null) {
                if (i == 0) {
                    MainActivity.mHandler.sendMessage(Message.obtain(MainActivity.mHandler,
                            Constants.COPY_INFO, line));
                    i = 10;
                } else {
                    i--;
                }
            }
            MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_HIDE);
            if (command.contains("cp")) {
                if (destFile.getParent().equals(Constants.DESKTOP_PATH)) {
                    MainActivity.mHandler.sendMessage(Message.obtain(MainActivity.mHandler,
                            Constants.DESKTOP_SHOW_FILE, destFile.getAbsolutePath()));
                }
            } else if (command.contains("mv")) {
                if (destFile.getParent().equals(Constants.DESKTOP_PATH)) {
                    MainActivity.mHandler.sendMessage(Message.obtain(MainActivity.mHandler,
                            Constants.DESKTOP_SHOW_FILE, destFile.getAbsolutePath()));
                }
                if (sourceFile.getParent().equals(Constants.DESKTOP_PATH)) {
                    MainActivity.mHandler.sendMessage(Message.obtain(MainActivity.mHandler,
                            Constants.DESKTOP_DELETE_FILE, sourceFile.getAbsolutePath()));
                }
            }
        } catch (IOException e) {
            MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_HIDE);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (isRefreah) {
            MainActivity.mHandler.sendMessage(
                               Message.obtain(MainActivity.mHandler, Constants.REFRESH, destDir));
        } else {
            MainActivity.mHandler.sendMessage(
                    Message.obtain(MainActivity.mHandler, Constants.REFRESH,
                            new File(srcFile).getParent()));
        }
        if (isRecycle) {
            if (destFile.exists()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("source", new File(srcFile).getParent());
                contentValues.put("filename", destFile.getName());
                MainActivity.getResolver().insert(MainActivity.getUri(), contentValues);
            }
        } else if (srcFile.contains(RECYCLE_PATH1)
                || srcFile.contains(RECYCLE_PATH2)
                || srcFile.contains(RECYCLE_PATH3)) {
            MainActivity.getResolver().delete(
                    MainActivity.getUri(), "filename = \"" + sourceFile.getName() + "\"", null);
        }
    }

    private void CopyFile(FileInfo f, String dest) {
        String command = "/system/bin/cp";
        String arg = "-v";
        File file = new File(f.filePath);
        if (file.isDirectory()) {
            arg = "-rv";
        }
        copyOrMoveFile(command, arg, f.filePath, dest, true);
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

    private void copyFileList(ArrayList<FileInfo> files) {
        synchronized (mCurFileNameList) {
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
                } else if (line.contains("fuse")) {
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

    public static void CopyFile(String sourcefile, String dest) {
        if (sourcefile.equals(dest)) {
            return;
        }
        String command = "/system/bin/cp";
        String arg = "-v";
        File file = new File(sourcefile);
        if (file.isDirectory()) {
            arg = "-rv";
        }
        copyOrMoveFile(command, arg, sourcefile, dest, true);
    }

    private boolean MoveFile(FileInfo f, String dest) {
        String command = "/system/bin/mv";
        String arg = "-v";
        copyOrMoveFile(command, arg, f.filePath, dest, true);
        return false;
    }

    public static boolean MoveFile(String sourcefile, String dest, boolean isRefreah) {
        MoveFile(sourcefile, dest, isRefreah, false);
        return false;
    }

    public static boolean MoveFile(String sourcefile, String dest,
                                   boolean isRefreah, boolean isRecycle) {
        if (new File(sourcefile).getParent().equals(dest)) {
            return false;
        }
        String command = "/system/bin/mv";
        String arg = "-v";
        copyOrMoveFile(command, arg, sourcefile, dest, isRefreah, isRecycle);
        return false;
    }

    public void deleteFile(ArrayList<FileInfo> selectedFiles) {
        for (int i = 0; i < selectedFiles.size(); i++) {
            String path = selectedFiles.get(i).filePath;
            if (path.equals(RECYCLE_PATH1)
                    || path.equals(RECYCLE_PATH2)
                    || path.equals(RECYCLE_PATH3)) {
                //clean Recycle
                delete(new File(path), true);
            } else if (path.contains(RECYCLE_PATH1)
                    || path.contains(RECYCLE_PATH2)
                    || path.contains(RECYCLE_PATH3)
                    || (path.split("/").length > 3 && path.startsWith("/storage/usb"))) {
                //delete file
                delete(new File(path), false);
            } else {
                //move to Recycle
                MoveFile(path, RECYCLE_PATH1, false, true);
            }
        }
    }

    public void deleteDirectFile(ArrayList<FileInfo> selectedFiles) {
        for (int i = 0; i < selectedFiles.size(); i++) {
            String path = selectedFiles.get(i).filePath;
            if (path.equals(RECYCLE_PATH1)
                    || path.equals(RECYCLE_PATH2)
                    || path.equals(RECYCLE_PATH3)) {
                //clean Recycle
                delete(new File(path), true);
            } else {
                //delete file
                delete(new File(path), false);
            }
        }
    }

    private static void delete(File file, boolean isReCreate) {
        if (file.exists()) {
            Process pro;
            BufferedReader in = null;
            String command = "/system/bin/rm";
            String arg = "";
            if (file.isFile()) {
                arg = "-v";
            } else if (file.isDirectory()) {
                arg = "-rv";
            }
            try {
                pro = Runtime.getRuntime().exec(new String[]{command, arg, file.getAbsolutePath()});
                in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                String line;
                MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_SHOW);
                int i = 0;
                while ((line = in.readLine()) != null) {
                    if (i == 0) {
                        MainActivity.mHandler.sendMessage(Message.obtain(MainActivity.mHandler,
                                Constants.COPY_INFO, line));
                        i = 10;
                    } else {
                        i--;
                    }
                }
                MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_HIDE);
                if (file.getAbsolutePath().contains(Constants.DESKTOP_PATH)) {
                    MainActivity.mHandler.sendMessage(Message.obtain(MainActivity.mHandler,
                            Constants.DESKTOP_DELETE_FILE, file.getAbsolutePath()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_HIDE);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (isReCreate) {
                file.mkdir();
            } else {
                MainActivity.mHandler.sendMessage(
                        Message.obtain(MainActivity.mHandler, Constants.REFRESH, file.getParent()));
            }
            if (file.getPath().equals(RECYCLE_PATH1)
                    || file.getPath().equals(RECYCLE_PATH2)
                    || file.getPath().equals(RECYCLE_PATH3)) {
                MainActivity.getResolver().delete(MainActivity.getUri(), null, null);
            } else if (file.getPath().contains(RECYCLE_PATH1)
                    || file.getPath().contains(RECYCLE_PATH2)
                    || file.getPath().contains(RECYCLE_PATH3)) {
                MainActivity.getResolver().delete(
                        MainActivity.getUri(), "filename = \"" + file.getName() + "\"", null);
            }

        }
    }

    public static void compress(String path, CompressFormatType type) {
        File f = new File(path);
        String arg = "";
        String suffix = "";
        BufferedReader in = null;
        boolean isOk = false;
        switch (type) {
            case TAR:
                arg = "-r";
                suffix = Constants.SUFFIX_TAR;
                break;
            case GZIP:
                arg = "-w";
                suffix = Constants.SUFFIX_TAR_GZIP;
                break;
            case BZIP2:
                arg = "-w";
                suffix = Constants.SUFFIX_TAR_BZIP2;
                break;
            case ZIP:
                arg = "-w";
                suffix = Constants.SUFFIX_ZIP;
                break;
        }
        String tarPath = path + suffix;
        try {
            Process pro = Runtime.getRuntime().exec(
                    new String[]{"/system/bin/7za", "a", tarPath, arg, path, "-bb3", "-y"});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_SHOW);
            String line;
            while ((line = in.readLine()) != null) {
                MainActivity.mHandler.sendMessage(Message.obtain(MainActivity.mHandler,
                            Constants.COPY_INFO, line));
            }
            MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_HIDE);
            MainActivity.mHandler.sendMessage(
                Message.obtain(MainActivity.mHandler, Constants.ONLY_REFRESH, f.getParent()));
        } catch (IOException e) {
            e.printStackTrace();
            MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_HIDE);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // command
    public static void decompress(String path) {
        File f = new File(path);
        BufferedReader in = null;
        Process pro;
        try {
            if (path.toLowerCase().endsWith(Constants.SUFFIX_RAR)) {
                pro = Runtime.getRuntime().exec(new String[]{"/system/bin/unrar", "x", "-y", path},
                        null, new File(f.getParent()));
            } else {
                pro = Runtime.getRuntime().exec(new String[]{"/system/bin/7za", "x",
                                                        path, "-o" + f.getParent(), "-bb3", "-y"});
            }
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_SHOW);
            String line;
            while ((line = in.readLine()) != null) {
                MainActivity.mHandler.sendMessage(Message.obtain(MainActivity.mHandler,
                            Constants.COPY_INFO, line));
            }
            MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_HIDE);
            MainActivity.mHandler.sendMessage(
                Message.obtain(MainActivity.mHandler, Constants.ONLY_REFRESH, f.getParent()));
        } catch (IOException e) {
            e.printStackTrace();
            MainActivity.mHandler.sendEmptyMessage(Constants.COPY_INFO_HIDE);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String[] list(String file) {
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        boolean isPrint = false;
        boolean isRar = file.toLowerCase().endsWith(Constants.SUFFIX_RAR);
        ArrayList<String> fileList = new ArrayList<>();
        try {
            if (!isRar) {
                pro = runtime.exec(new String[]{"/system/bin/7za", "l", file});
            } else {
                pro = runtime.exec(new String[]{"/system/bin/unrar", "v", file});
            }
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            int row = 1;
            while ((line = in.readLine()) != null) {
                if (isPrint) {
                    if (line.contains("-----")) {
                        isPrint = false;
                        continue;
                    }
                    if (!isRar) {
                        line = line.substring(Constants.INDEX_7Z_FILENAME);
                        System.out.println(line);
                        if (line.contains("/")) {
                            line = line.replace(line.substring(line.indexOf("/")), "");
                            if (!fileList.contains(line)) {
                                fileList.add(line);
                            }
                        } else {
                            fileList.add(line);
                        }
                    } else if (row % 2 != 0) {
                        fileList.add(line.substring(1));
                    }
                    row++;
                }
                if (line.contains("-----")) {
                    isPrint = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileList.toString().substring(1, fileList.toString().length() - 1).split(", ");
    }
}
