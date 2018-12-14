package org.openthos.filemanager.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;

import static android.R.attr.id;

import java.io.File;

/**
 * Created by Wang Zhixu on 12/23/16.
 */

public class SeafileUtils {
    public static final String TAG = "SeafileUtils";
    public static final String SEAFILE_DATA_PATH = "/system/linux/sea/data/seafile/";

    public static String mUserId = "";

    public static final int UNSYNC = 0;
    public static final int SYNC = 1;
    public static final String FILEMANAGER_SEAFILE_NAME = "DATA";

    public static boolean isExistsAccount() {
        return !TextUtils.isEmpty(mUserId);
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
