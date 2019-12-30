package org.openthos.filemanager.system;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.content.FileProvider;

import org.openthos.filemanager.bean.FileInfo;
import org.openthos.filemanager.component.TextSelectMenuDialog;
import org.openthos.filemanager.component.OpenWithDialog;
import org.openthos.filemanager.utils.Constants;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class IntentBuilder {

    public static void viewFile(final Context context, final String filePath, MotionEvent event) {
        String type = Constants.getMIMEType(new File(filePath));
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            List<ResolveInfo> resolveInfoList = new ArrayList<>();
            PackageManager manager = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = null;
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(context,
                        "org.openthos.support.filemanager.fileprovider", new File(filePath));
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(new File(filePath));
            }
            intent.setDataAndType(uri, type);
            resolveInfoList = manager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfoList.size() > 0) {
                intent.putExtra(Constants.PACKAGENAME_TAG, Constants.APPNAME_OTO_LAUNCHER);
                context.startActivity(intent);
            } else {
                OpenWithDialog openWithDialog = new OpenWithDialog(context, filePath);
                openWithDialog.showDialog();
            }
        } else {
            TextSelectMenuDialog dialog = new TextSelectMenuDialog(context, filePath);
            if (event != null) {
                dialog.showDialog((int) event.getRawX(), (int) event.getRawY());
            } else {
                dialog.showDialog();
            }
        }
    }

    public static Intent buildSendFile(ArrayList<FileInfo> files) {
        ArrayList<Uri> uris = new ArrayList<>();

        String mimeType = "*/*";
        for (FileInfo file : files) {
            if (file.IsDir)
                continue;

            File fileIn = new File(file.filePath);
            mimeType = Constants.getMIMEType(fileIn);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }

        if (uris.size() == 0)
            return null;

        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? Intent.ACTION_SEND_MULTIPLE
                : Intent.ACTION_SEND);

        if (multiple) {
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }

        return intent;
    }

    private static String getMimeType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        if (dotPosition == -1)
            return "*/*";

        String ext = filePath.substring(dotPosition + 1, filePath.length()).toLowerCase();
        String mimeType = Constants.getMIMEType(new File(filePath));
        if (ext.equals("mtz")) {
            mimeType = "application/miui-mtz";
        }

        return mimeType != null ? mimeType : "*/*";
    }
}
