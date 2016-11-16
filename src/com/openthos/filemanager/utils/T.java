package com.openthos.filemanager.utils;

import android.content.Context;
import android.widget.Toast;

public class T {
    private T() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean mIsShow = true;

    public static void showShort(Context context, CharSequence message) {
        if (mIsShow)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showShort(Context context, String message) {
        if (mIsShow)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(Context context, CharSequence message) {
        if (mIsShow)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showLong(Context context, int message) {
        if (mIsShow)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
