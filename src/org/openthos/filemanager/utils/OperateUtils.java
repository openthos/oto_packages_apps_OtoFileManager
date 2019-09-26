package org.openthos.filemanager.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import org.openthos.filemanager.R;
import org.openthos.filemanager.MainActivity;

public class OperateUtils {
    public static void showChooseAlertDialog(Context context, int messageId,
                       OnClickListener ok, OnClickListener cancel) {
        AlertDialog dialog = new AlertDialog.Builder(context)
             .setMessage(context.getResources().getString(messageId))
             .setPositiveButton(context.getResources().getString(R.string.dialog_delete_yes), ok)
             .setNegativeButton(context.getResources().getString(R.string.dialog_delete_no), cancel)
             .create();
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setOnKeyListener(new BaseKeyEvent());
        dialog.show();
    }

    public static void showConfirmAlertDialog(Context context, int messageId) {
        AlertDialog dialog = new AlertDialog.Builder(context)
             .setMessage(context.getResources().getString(messageId))
             .setPositiveButton(context.getResources().getString(R.string.dialog_delete_yes), null)
             .create();
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setOnKeyListener(new BaseKeyEvent());
        dialog.show();
    }

    public static class BaseKeyEvent implements DialogInterface.OnKeyListener {

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            MainActivity.setState(event.isCtrlPressed(), event.isShiftPressed());
            return false;
        }
    }
}
