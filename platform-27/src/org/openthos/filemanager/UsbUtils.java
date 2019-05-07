package org.openthos.filemanager;

import android.app.Activity;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;

import java.util.Collections;
import java.util.List;

public class UsbUtils {

    public static void umount(Activity activity, String usbPath) {
        StorageManager storageManager = activity.getSystemService(StorageManager.class);
        final List<VolumeInfo> volumes = storageManager.getVolumes();
        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
        for (VolumeInfo vol : volumes) {
            if (usbPath.equals(vol.getPath().getPath())) {
                storageManager.unmount(vol.getId());
                break;
            }
        }
    }

    public static void format(Activity activity, String usbPath) {
        StorageManager storageManager = activity.getSystemService(StorageManager.class);
        final List<VolumeInfo> volumes = storageManager.getVolumes();
        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
        for (VolumeInfo vol : volumes) {
            if (usbPath.equals(vol.getPath().getPath())) {
                storageManager.partitionPublic(vol.getDiskId());
                break;
            }
        }
    }
}
