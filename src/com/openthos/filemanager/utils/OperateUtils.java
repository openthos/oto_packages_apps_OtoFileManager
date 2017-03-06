package com.openthos.filemanager.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.openthos.filemanager.R;

public class OperateUtils {
    public static void showChooseAlertDialog(Context context, int messageId,
                       DialogInterface.OnClickListener ok, DialogInterface.OnClickListener cancel) {
        AlertDialog dialog = new AlertDialog.Builder(context)
             .setMessage(context.getResources().getString(messageId))
             .setPositiveButton(context.getResources().getString(R.string.dialog_delete_yes), ok)
             .setNegativeButton(context.getResources().getString(R.string.dialog_delete_no), cancel)
             .create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }
}
