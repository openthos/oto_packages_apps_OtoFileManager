package org.openthos.filemanager.system;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import org.openthos.filemanager.R;
import org.openthos.filemanager.bean.FileInfo;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.utils.LocalCache;
import org.openthos.filemanager.utils.IconHolder;
import org.openthos.filemanager.utils.Util;

public class FileListItem {
    public static void setupFileListItemInfo(Context context, View view,
                                             FileInfo fileInfo, IconHolder iconHolder,
                                             FileViewInteractionHub fileViewInteractionHub) {
        if (fileViewInteractionHub.isMoveState()) {
            fileInfo.Selected = fileViewInteractionHub.isFileSelected(fileInfo.filePath);
        }

        ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
        if (Constants.VIEW_TAG_LIST.equals(LocalCache.getViewTag())) {
            Util.setText(view, R.id.file_name, fileInfo.fileName,
                         context.getResources().getColor(R.color.file_name_color));
            Util.setText(view, R.id.file_count, (fileInfo.IsDir ?
                         context.getResources().getString(R.string.file_type_folder) :
                         context.getResources().getString(R.string.file_type_file)),
                         context.getResources().getColor(R.color.file_name_color));
            Util.setText(view, R.id.modified_time,
                         Util.formatDateString(context, fileInfo.ModifiedDate),
                         context.getResources().getColor(R.color.file_name_color));
            Util.setText(view, R.id.file_size,
                         fileInfo.IsDir ? String.valueOf(fileInfo.Count) + " "
                         + context.getResources().getString(R.string.items) :
                         Util.convertStorage(fileInfo.fileSize),
                         context.getResources().getColor(R.color.file_name_color));
            lFileImage.setVisibility(View.VISIBLE);
            if (fileInfo.IsDir) {
                lFileImage.setImageResource(R.mipmap.folder);
            } else {
                iconHolder.loadDrawable(lFileImage, fileInfo.filePath);
            }
        } else {
            Util.setText(view, R.id.file_name, fileInfo.fileName,
                         context.getResources().getColor(R.color.file_name_color));
            lFileImage.setBackground(null);
            if (fileInfo.IsDir) {
                lFileImage.setImageResource(R.mipmap.folder);
            } else {
                iconHolder.loadDrawable(lFileImage, fileInfo.filePath);
            }
        }
    }
}
