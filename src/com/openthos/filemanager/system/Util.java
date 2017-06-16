package com.openthos.filemanager.system;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;

import com.openthos.filemanager.R;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.system.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class Util {
    private static final String LOG_TAG = "Util";

    public static ArrayList<String> execUsb() {
        ArrayList<String> list = new ArrayList<>();
        File file = new File("/storage");
        File[] files = file.listFiles();
        String usbPath;
        for (File child : files) {
            usbPath = child.getPath();
            if (!(usbPath.equals("/storage/emulated") || usbPath.equals("/storage/sdcard0")
                    || usbPath.equals("/storage/sdcard1") || usbPath.equals("/storage/self")
                    || usbPath.startsWith("/storage/disk"))
                && isNormalFile(usbPath) && shouldShowFile(usbPath) && child.canRead()) {
                list.add(usbPath);
            }
        }
        return list;
    }

    public static String[] execDisk(String[] args) {
        String []strs = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream inIs = null;
        try {
            process = processBuilder.start();
            inIs = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inIs);
            BufferedReader buff= new BufferedReader(inputStreamReader);
            String line=null;
            while ((line = buff.readLine()) != null) {
                Log.e("line:",line);

                if (line.startsWith("/storage/disk")){
                    strs = line.split("\\s+");
                    L.d(LOG_TAG, Arrays.toString(strs) +"");
                }
            }
            buff.close();
            inputStreamReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return strs;
    }

    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static int getCompressFileState(String path) {
        if (path != null && path.contains(".")) {
            String endStr = path.substring(path.lastIndexOf(".")).toLowerCase();
            switch (endStr) {
                case Constants.SUFFIX_TAR:
                    return Constants.COMPRESSIBLE_DECOMPRESSIBLE;
                case Constants.SUFFIX_TAR_BZIP2:
                case Constants.SUFFIX_TAR_GZIP:
                case Constants.SUFFIX_ZIP:
                case Constants.SUFFIX_RAR:
                case Constants.SUFFIX_7z:
                    return Constants.DECOMPRESSIBLE;
                default:
                    return Constants.COMPRESSIBLE;
            }
        } else {
            return Constants.COMPRESSIBLE;
        }
    }

    // if path1 contains path2
    public static boolean containsPath(String path1, String path2) {
        String path = path2;
        while (path != null) {
            if (path.equalsIgnoreCase(path1))
                return true;

            if (path.equals(Constants.ROOT_PATH))
                break;
            path = new File(path).getParent();
        }

        return false;
    }

    public static String makePath(String path1, String path2) {
        if (path1.endsWith(File.separator))
            return path1 + path2;

        return path1 + File.separator + path2;
    }

    public static String getSdDirectory() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static boolean isNormalFile(String fullName) {
        String ANDROID_SECURE = "/mnt/sdcard/.android_secure";
        return !fullName.equals(ANDROID_SECURE);
    }

    public static FileInfo GetFileInfo(String filePath) {
        File lFile = new File(filePath);
        if (!lFile.exists())
            return null;

        FileInfo lFileInfo = new FileInfo();
        lFileInfo.canRead = lFile.canRead();
        lFileInfo.canWrite = lFile.canWrite();
        lFileInfo.isHidden = lFile.isHidden();
        lFileInfo.fileName = Util.getNameFromFilepath(filePath);
        lFileInfo.ModifiedDate = lFile.lastModified();
        lFileInfo.IsDir = lFile.isDirectory();
        lFileInfo.filePath = filePath;
        lFileInfo.fileSize = lFile.length();
        return lFileInfo;
    }

    public static FileInfo GetFileInfo(File f, FilenameFilter filter, boolean showHidden) {
        FileInfo lFileInfo = new FileInfo();
        String filePath = f.getPath();
        File lFile = new File(filePath);
        lFileInfo.canRead = lFile.canRead();
        lFileInfo.canWrite = lFile.canWrite();
        lFileInfo.isHidden = lFile.isHidden();
        lFileInfo.fileName = f.getName();
        lFileInfo.ModifiedDate = lFile.lastModified();
        lFileInfo.IsDir = lFile.isDirectory();
        lFileInfo.filePath = filePath;
        if (lFileInfo.IsDir) {
            int lCount = 0;
            File[] files = lFile.listFiles(filter);

            // null means we cannot access this dir
            if (files == null) {
                return null;
            }

            for (File child : files) {
                if ((!child.isHidden() || showHidden)
                        && Util.isNormalFile(child.getAbsolutePath())) {
                    lCount++;
                }
            }
            lFileInfo.Count = lCount;

        } else {

            lFileInfo.fileSize = lFile.length();

        }
        return lFileInfo;
    }

    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
        return null;
    }

    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    public static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return "";
    }

    public static String getPathFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(0, pos);
        }
        return "";
    }

    public static String getNameFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(pos + 1);
        }
        return "";
    }

    // return new file path if successful, or return null
    public static String copyFile(String src, String dest) {
        File file = new File(src);
        if (!file.exists() || file.isDirectory()) {
            Log.v(LOG_TAG, "copyFile: file not exist or is directory, " + src);
            return null;
        }
        FileInputStream fi = null;
        FileOutputStream fo = null;
        try {
            fi = new FileInputStream(file);
            File destPlace = new File(dest);
            if (!destPlace.exists()) {
                if (!destPlace.mkdirs())
                    return null;
            }

            String destPath = Util.makePath(dest, file.getName());
            File destFile = new File(destPath);
            int i = 1;
            while (destFile.exists()) {
                String destName = Util.getNameFromFilename(file.getName()) + " " + i++ + "."
                        + Util.getExtFromFilename(file.getName());
                destPath = Util.makePath(dest, destName);
                destFile = new File(destPath);
            }

            if (!destFile.createNewFile())
                return null;

            fo = new FileOutputStream(destFile);
            int count = 102400;
            byte[] buffer = new byte[count];
            int read;
            while ((read = fi.read(buffer, 0, count)) != -1) {
                fo.write(buffer, 0, read);
            }

            // TODO: set access privilege
            return destPath;
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "copyFile: file not found, " + src);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "copyFile: " + e.toString());
        } finally {
            try {
                if (fi != null)
                    fi.close();
                if (fo != null)
                    fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // does not include sd card folder
    private static String[] SysFileDirs = new String[]{
            "miren_browser/imagecaches"
    };

    public static boolean shouldShowFile(String path) {
        return shouldShowFile(new File(path));
    }

    public static boolean shouldShowFile(File file) {
        boolean show = Settings.instance().getShowDotAndHiddenFiles();
        if (show)
            return true;

        if (file.isHidden())
            return false;

        if (file.getName().startsWith("."))
            return false;

        String sdFolder = getSdDirectory();
        for (String s : SysFileDirs) {
            if (file.getPath().startsWith(makePath(sdFolder, s)))
                return false;
        }

        return true;
    }

    public static boolean setText(View view, int id, String text, int color) {
        TextView textView = (TextView) view.findViewById(id);
        if (textView == null)
            return false;

        textView.setText(text);
        textView.setTextColor(color);
        return true;
    }

    public static boolean setBackground(View view, int id, int bg) {
        LinearLayout linearLayout = (LinearLayout) view.findViewById(id);
        if (linearLayout == null)
            return false;
        linearLayout.setBackgroundResource(bg);
        return true;
    }

    // comma separated number
    public static String convertNumber(long number) {
        return String.format("%,d", number);
    }

    // storage, G M K B
    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    public static class SDCardInfo {
        public long total;

        public long free;
    }

    public static class UsbMemoryInfo {
        public long usbTotal;

        public long usbFree;
    }

    public static class SystemInfo {
        public long romMemory;

        public long avilMemory;
    }

    public static SDCardInfo getSDCardInfo() {
//        String sDcString = Environment.getExternalStorageState();
//
//        if (sDcString.equals(Environment.MEDIA_MOUNTED)) {
            File pathFile = Environment.getExternalStorageDirectory();
            try {
                android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());
                long nTotalBlocks = statfs.getBlockCount();
                long nBlocSize = statfs.getBlockSize();
                long nAvailaBlock = statfs.getAvailableBlocks();
                long nFreeBlock = statfs.getFreeBlocks();
                SDCardInfo info = new SDCardInfo();
                info.total = nTotalBlocks * nBlocSize;
                info.free = nAvailaBlock * nBlocSize;

                return info;
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, e.toString());
            }
//        }
        return null;
    }

    public static SystemInfo getRomMemory() {
        try {
            SystemInfo systemInfo = new SystemInfo();
            //Total rom memory
            systemInfo.romMemory = getTotalInternalMemorySize();

            //Available rom memory
            File path = Environment.getRootDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            systemInfo.avilMemory = blockSize * availableBlocks;
            return systemInfo;
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, e.toString());
        }
        return null;
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getRootDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    public static UsbMemoryInfo getUsbMemoryInfo(String usbPath) {
        try {
            android.os.StatFs statfs = new android.os.StatFs(usbPath);
            long nTotalBlocks = statfs.getBlockCount();
            long nBlocSize = statfs.getBlockSize();
            long nAvailaBlock = statfs.getAvailableBlocks();
            long nFreeBlock = statfs.getFreeBlocks();

            UsbMemoryInfo usbInfo = new UsbMemoryInfo();
            usbInfo.usbTotal = nTotalBlocks * nBlocSize;
            usbInfo.usbFree = nAvailaBlock * nBlocSize;

            return usbInfo;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatDateString(Context context, long time) {
        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat
                .getTimeFormat(context);
        Date date = new Date(time);
        return dateFormat.format(date) + " " + timeFormat.format(date);
    }

    public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
        {
            add("text/plain");
            add("text/plain");
            add("application/pdf");
            add("application/msword");
            add("application/vnd.ms-excel");
            add("application/vnd.ms-excel");
        }
    };

   public static String formatFileSize(long fileSize) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileSize < Constants.SIZE_KB) {
            fileSizeString = df.format((double) fileSize) + "B";
        } else if (fileSize < Constants.SIZE_MB) {
            fileSizeString = df.format((double) fileSize / Constants.SIZE_KB) + "K";
        } else if (fileSize < Constants.SIZE_GB) {
            fileSizeString = df.format((double) fileSize / Constants.SIZE_MB) + "M";
        } else if (fileSize < Constants.SIZE_TB){
            fileSizeString = df.format((double) fileSize / Constants.SIZE_GB) + "G";
        }else {
            fileSizeString = df.format((double) fileSize / Constants.SIZE_TB) + "T";
        }
        if (fileSizeString.equals(".00B")) {
            fileSizeString = "0.00B";
        }
        return fileSizeString;
    }

    public static void exec(String[] commands) {
        Process pro;
        BufferedReader in = null;
        try {
            pro = Runtime.getRuntime().exec(commands);
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                continue;
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
    }

    public static String getUsbName(Context context, String path) {
        return path.replace("/storage/", "");
    }

    public static String sZipFileMimeType = "application/zip";

    public static int CATEGORY_TAB_INDEX = 0;
    public static int SDCARD_TAB_INDEX = 1;
}
