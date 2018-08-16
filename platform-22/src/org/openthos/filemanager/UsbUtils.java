package org.openthos.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;
import android.os.storage.IMountService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.StorageVolume;
import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.os.storage.ExternalStorageMountter;
import android.app.ActivityThread;

public class UsbUtils {

    private static IMountService getMountService() {
        IMountService mountService = null;
        if (mountService == null) {
            IBinder iBinder = ServiceManager.getService("mount");
            if (iBinder != null) {
                mountService = IMountService.Stub.asInterface(iBinder);
            }
        }
        return mountService;
    }

    public static void umount(Activity activity, String usbPath) {
        try {
            Intent umountIntent = new Intent(ExternalStorageMountter.UMOUNT_ONLY);
            umountIntent.setComponent(ExternalStorageMountter.COMPONENT_NAME);
            StorageVolume[] vols = getMountService().getVolumeList(
                    activity.getUserId(), ActivityThread.currentPackageName(), 0);
            StorageVolume vol = null;
            for (StorageVolume i : vols) {
                if (i.getPath().equals(usbPath)) {
                    vol = i;
                    break;
                }
            }
            if (vol != null) {
                umountIntent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, vol);
                activity.startService(umountIntent);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void format(Activity activity, String usbPath) {
        try {
            Intent formatIntent = new Intent(ExternalStorageFormatter.FORMAT_ONLY);
            formatIntent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
            StorageVolume[] vols = getMountService().getVolumeList(
                    activity.getUserId(), ActivityThread.currentPackageName(), 0);
            StorageVolume vol = null;
            for (StorageVolume i : vols) {
                if (i.getPath().equals(usbPath)) {
                    vol = i;
                    break;
                }
            }
            if (vol != null) {
                formatIntent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, vol);
                activity.startService(formatIntent);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
