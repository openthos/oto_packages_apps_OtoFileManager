package org.openthos.filemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalCache {
    private static LocalCache localCache;
    private static SharedPreferences sp;
    private LocalCache(Context context) {
        sp = context.getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE);
    }

    public static LocalCache getInstance(Context context) {
        if (localCache == null) {
            localCache = new LocalCache(context);
        }
        return localCache;
    }

    public static void setViewTag(String tag){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("viewTag", tag);
        editor.apply();
    }

    public static String getViewTag(){
        return  sp.getString("viewTag", null);
    }

    public static void setSearchText(String query){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("searchText", query);
        editor.apply();
    }

    public static String getSearchText(){
        return  sp.getString("searchText", null);
    }
}
