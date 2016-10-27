package com.openthos.filemanager.system;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.Window;

import com.openthos.filemanager.R;
import com.openthos.filemanager.view.TextSelectDialog;

import java.io.File;
import java.util.ArrayList;

public class IntentBuilder {
    private static final int TEXT_TYPE = 2;
    public static void viewFile(final Context context, final String filePath, MotionEvent event) {
        String type = getMimeType(filePath);
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
            context.startActivity(intent);
        } else {
            // unknown MimeType
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
//            dialogBuilder.setTitle(R.string.dialog_select_type);
//
//            CharSequence[] menuItemArray = new CharSequence[]{
//                    context.getString(R.string.dialog_type_text),
//                    context.getString(R.string.dialog_type_audio),
//                    context.getString(R.string.dialog_type_video),
//                    context.getString(R.string.dialog_type_image)};
//            dialogBuilder.setItems(menuItemArray,
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            String selectType = "*/*";
//                            switch (which) {
//                                case 0:
//                                    selectType = "text/plain";
//                                    break;
//                                case 1:
//                                    selectType = "audio/*";
//                                    break;
//                                case 2:
//                                    selectType = "video/*";
//                                    break;
//                                case 3:
//                                    selectType = "image/*";
//                                    break;
//                            }
//                            Intent intent = new Intent();
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            intent.setAction(Intent.ACTION_VIEW);
//                            intent.setDataAndType(Uri.fromFile(new File(filePath)), selectType);
//                            context.startActivity(intent);
//                        }
//                    });

            TextSelectDialog dialog = new TextSelectDialog(context, R.style.menu_dialog,filePath);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.showTextDialog((int) event.getX(), (int) event.getY(),180,140);
        }
    }

    public static Intent buildSendFile(ArrayList<FileInfo> files) {
        ArrayList<Uri> uris = new ArrayList<>();

        String mimeType = "*/*";
        for (FileInfo file : files) {
            if (file.IsDir)
                continue;

            File fileIn = new File(file.filePath);
            mimeType = getMimeType(file.fileName);
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
        String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
        if (ext.equals("mtz")) {
            mimeType = "application/miui-mtz";
        }

        return mimeType != null ? mimeType : "*/*";
    }
}
