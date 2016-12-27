package com.openthos.filemanager.utils;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.openthos.filemanager.system.SeafileSQLiteHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.R.attr.id;

/**
 * Created by Wang Zhixu on 12/23/16.
 */

public class SeafileUtils {
    public static final String SEAFILE_PROOT_BASEPATH = "/data";
    public static final String SEAFILE_CONFIG_PATH = "/data/seafile-config";
    public static final String SEAFILE_DATA_PATH = "/sdcard/.seafile-data";

    public static final String SEAFILE_NET_NAME = ".ccnet";

    public static final String SEAFILE_COMMAND_SEAFILE = "seaf-cli ";
    public static final String SEAFILE_COMMAND_PROOT = "./data/sea/proot.sh -b ";
    public static final String SEAFILE_COMMAND_PROOT_BASE = "./data/sea/proot.sh ";

    public static final String SEAFILE_BASE_ARG = "-b";
    public static final String SEAFILE_BASE_URL = "-s https://dev.openthos.org/ ";
    public static String SEAFILE_BASE_ROOT_PATH = "/data/seafile-config:/data/seafile-config ";

    public static final int SEAFILE_ID_LENGTH = 36;

    public static String mUserId = "";
    public static String mUserPassword = "";

    public static String getUserAccount() {
        return "-u " + mUserId + " -p " + mUserPassword;
    }

    private static void exec(String[] commands) {
        Process pro;
        BufferedReader in = null;
        try {
            pro = Runtime.getRuntime().exec(commands);
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                Log.i("wwwww", line);
                if (line.contains("Started: seafile daemon")) {
                    break;
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
    }

    public static void main(String[] args) {
        //init();
        //start();
        //stop();
        //System.out.println(listRemote("potatomagic@163.com", "kiss5potato"));
        //System.out.println(create("zhaoliu", "potatomagic@163.com", "kiss5potato"));
        //download("e700ed4c-bda8-4c20-af8c-28fa6c4923f3", SEAFILE_CONFIGPATH + "sea",
        //                 "potatomagic@163.com", "kiss5potato");
        //sync("e700ed4c-bda8-4c20-af8c-28fa6c4923f3", SEAFILE_CONFIGPATH + "sea/zhangsan",
        //                 "potatomagic@163.com", "kiss5potato");
        //status();
        //desync(SEAFILE_CONFIGPATH + "sea/zhangsan");
    }

    public static void init() {
        File config = new File(SEAFILE_CONFIG_PATH);
        if (!config.exists()) {
            config.mkdirs();
        }
        exec(new String[]{"su","-c",SEAFILE_COMMAND_PROOT
                + SEAFILE_BASE_ROOT_PATH + SEAFILE_COMMAND_SEAFILE + "init -d "
                + config.getAbsolutePath()});
    }

    public static void start() {
        exec(new String[]{"su","-c", SEAFILE_COMMAND_PROOT
                + SEAFILE_BASE_ROOT_PATH + SEAFILE_COMMAND_SEAFILE + "start"});
    }

    public static void stop() {
        exec(new String[]{"su","-c",SEAFILE_COMMAND_PROOT
                + SEAFILE_BASE_ROOT_PATH + SEAFILE_COMMAND_SEAFILE + "stop"});
    }


    public static String listRemote() {
        Process pro;
        BufferedReader in = null;
        StringBuffer sb = new StringBuffer();
        try {
            pro = Runtime.getRuntime().exec(new String[]{"su","-c",SEAFILE_COMMAND_PROOT
                    + SEAFILE_BASE_ROOT_PATH + SEAFILE_COMMAND_SEAFILE + "list-remote "
                    + SEAFILE_BASE_URL + getUserAccount()});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            sb.append("[");
            boolean isOut = false;
            while ((line = in.readLine()) != null) {
                if (line.contains("Name") && line.contains("ID")) {
                    isOut = true;
                    continue;
                }
                if (line.contains("ISO(ota)")) {
                    isOut = false;
                    continue;
                }
                if (isOut) {
                    if (line.length() > SEAFILE_ID_LENGTH) {
                        String id = line.substring(line.length() - SEAFILE_ID_LENGTH);
                        sb.append("{\"id\":\"" + id);
                        String name = line.replace(" " + id, "");
                        sb.append("\",\"name\":\"" + name + "\"},");
                    }
                }
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append("]");
            Log.i("wwwww", sb.toString());
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
        return sb.toString();
    }

    public static void download(String libraryid, String filePath) {
        filePath = filePath.trim().replace(" ", "\\ ");
        Process pro;
        BufferedReader in = null;
        try {
            pro = Runtime.getRuntime().exec(new String[]{"su","-c", SEAFILE_COMMAND_PROOT_BASE
                    + "mkdir -p " + SEAFILE_PROOT_BASEPATH + filePath});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                Log.i("wwwwww", line);
            }
            pro = Runtime.getRuntime().exec(new String[]{"su","-c",SEAFILE_COMMAND_PROOT +
                    SEAFILE_BASE_ROOT_PATH + " -b "
                    + filePath + ":" + SEAFILE_PROOT_BASEPATH + filePath + " "
                    + SEAFILE_COMMAND_SEAFILE + "download -l " + libraryid + " -d "
                    + SEAFILE_PROOT_BASEPATH + filePath + " "
                    + SEAFILE_BASE_URL + getUserAccount()});
            Log.i("wwwwww", SEAFILE_COMMAND_PROOT +
                    SEAFILE_BASE_ROOT_PATH + " -b "
                    + filePath + ":" + SEAFILE_PROOT_BASEPATH + filePath + " "
                    + SEAFILE_COMMAND_SEAFILE + "download -l " + libraryid + " -d "
                    + SEAFILE_PROOT_BASEPATH + filePath + " "
                    + SEAFILE_BASE_URL + getUserAccount());
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            while ((line = in.readLine()) != null) {
                Log.i("wwwwww", line);
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


    public static void desync(String fileName) {
        String arg0 = "desync";
        String arg1 = "-d";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        try {
            pro = runtime.exec(new String[]{SEAFILE_COMMAND_PROOT, SEAFILE_BASE_ARG,
                    "", SEAFILE_COMMAND_SEAFILE, arg0, arg1, fileName});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
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


    public static void status() {
        String arg0 = "status";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        try {
            pro = Runtime.getRuntime().exec(new String[]{SEAFILE_COMMAND_PROOT, SEAFILE_BASE_ARG,
                    "", SEAFILE_COMMAND_SEAFILE, arg0});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
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

    public static void sync(String id, String fileName, String userName, String userPassword) {
        String arg0 = "sync";
        String arg1 = "-l";
        String arg2 = "-s";
        String arg3 = "-d";
        String arg4 = "-u";
        String arg5 = "-p";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        try {
            pro = runtime.exec(new String[]{SEAFILE_COMMAND_PROOT, SEAFILE_BASE_ARG,
                    "", SEAFILE_COMMAND_SEAFILE, arg0, arg1, id,
                    arg2, SEAFILE_BASE_URL, arg3, fileName, arg4, userName, arg5, userPassword});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
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


    public static String create(String fileName, String userName, String userPassword) {
        String arg0 = "create";
        String arg1 = "-n";
        String arg2 = "-s";
        String arg3 = "-u";
        String arg4 = "-p";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        String id = "";
        try {
            pro = runtime.exec(new String[]{SEAFILE_COMMAND_PROOT, SEAFILE_BASE_ARG,
                    "", SEAFILE_COMMAND_SEAFILE, arg0, arg1, fileName,
                    arg2, SEAFILE_BASE_URL, arg3, userName, arg4, userPassword});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                id = line;
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
        return id;
    }


    public static void delete(File file) {
        if (file.exists()) {
            String command = "rm";
            String arg = "";
            if (file.isFile()) {
                arg = "-v";
            } else if (file.isDirectory()) {
                arg = "-rv";
            }
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(new String[]{command, arg, file.getAbsolutePath()});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class SeafileSQLConsole {
        SeafileSQLiteHelper mSeafileSQLiteHelper;

        public SeafileSQLConsole(Context context) {
            mSeafileSQLiteHelper = new SeafileSQLiteHelper(context, SeafileSQLiteHelper.NAME,
                    null, SeafileSQLiteHelper.VERSION);
            SQLiteDatabase db = mSeafileSQLiteHelper.getWritableDatabase();
            db.close();
        }

        public int queryAccountId(String account) {
            int id = 0;
            SQLiteDatabase db = mSeafileSQLiteHelper.getWritableDatabase();
            Cursor c = db.rawQuery("select * from seafileaccount where username like ?",
                    new String[]{account});
            while (c.moveToNext()) {
                id = c.getInt(c.getColumnIndex("id"));
            }
            c.close();
            if (id == 0) {
                db.execSQL("insert into seafileaccount (username) " +
                        "values ('" + account + "')");
                id = queryAccountId(account);
            }
            db.close();
            return id;
        }

        public int queryFile(int userId, String libraryId, String libraryName) {
            int isSync = -1;
            SQLiteDatabase db = mSeafileSQLiteHelper.getWritableDatabase();
            Cursor c = db.rawQuery("select * from seafilefile where userid like ?"
                            + " and libraryid like ? and libraryname like ?",
                    new String[]{userId + "", libraryId, libraryName});
            while (c.moveToNext()) {
                isSync = c.getInt(c.getColumnIndex("isSync"));
            }
            c.close();
            if (isSync == -1) {
                db.execSQL("insert into seafilefile (userid,libraryid,libraryname,isSync) "
                        + "values (" + userId + ",'" + libraryId
                        + "' ,'" + libraryName + "'," + 0 + ")");
                isSync = queryFile(userId, libraryId, libraryName);
            }
            db.close();
            return isSync;
        }
    }
}
