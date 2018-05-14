package org.openthos.filemanager.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;

import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.utils.Constants;

public class UsbConnectionReceiver extends BroadcastReceiver {
    MainActivity activity;

    public IntentFilter filter = new IntentFilter();

    public UsbConnectionReceiver(Context context) {
        activity = (MainActivity) context;
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
    }

    public Intent registerReceiver() {
        return activity.registerReceiver(this, this.filter);
    }

    public void unregisterReceiver() {
        activity.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String dataString = intent.getDataString();
        Message msg =  new Message();
        switch (action) {
            case Intent.ACTION_MEDIA_CHECKING:
                msg.what = Constants.USB_CHECKING;
                break;
            case Intent.ACTION_MEDIA_MOUNTED:
                msg.what = Constants.USB_MOUNT;
                break;
            case Intent.ACTION_MEDIA_EJECT:
                msg.what = Constants.USB_EJECT;
                break;
        }
        msg.obj = dataString.substring(7, dataString.length());
        activity.mHandler.sendMessage(msg);
    }
}