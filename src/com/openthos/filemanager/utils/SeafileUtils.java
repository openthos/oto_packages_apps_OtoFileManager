package com.openthos.filemanager.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.text.TextUtils;
import android.provider.Settings;

import com.openthos.filemanager.system.SeafileSQLiteHelper;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.id;

/**
 * Created by Wang Zhixu on 12/23/16.
 */

public class SeafileUtils {
    public static final String SEAFILE_PROOT_BASEPATH = "/data";
    public static final String SEAFILE_CONFIG_PATH = "/data/seafile-config";
    public static final String SEAFILE_DATA_PATH = "/sdcard/.seafile-data";
    public static final String SEAFILE_PATH = "/system/opt/sea.tar.bz";
    public static final String SEAFILE_DATA_PATH_REAlLY = "/data/sea/data";

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

    public static final int UNSYNC = 0;
    public static final int SYNC = 1;
    public static final String SEAFILE_DATA = "seeafile_data";
    public static final String SETTING_SEAFILE_PATH = "/data/sea/data/sdcard/cloudFolder";
    public static final String SETTING_SEAFILE_PROOT_PATH = "/sdcard/cloudFolder";
    public static final String SETTING_SEAFILE_NAME = "cloudFolder";
    public static final String FILEMANAGER_SEAFILE_NAME = "DATA";

    public static String getUserAccount() {
        return "-u " + mUserId + " -p " + mUserPassword;
    }

    public static boolean isExistsAccount() {
        return !TextUtils.isEmpty(mUserId) || !TextUtils.isEmpty(mUserPassword);
    }

    public static boolean isNetworkOn(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifi != null && wifi.isAvailable()
                && wifi.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            return true;
        }
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(mobile != null && mobile.isAvailable()
                && mobile.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            return true;
        }
        return false;
    }
}
