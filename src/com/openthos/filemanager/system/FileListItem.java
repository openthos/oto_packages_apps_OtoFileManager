package com.openthos.filemanager.system;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.IconHolder;
import com.openthos.filemanager.system.FileCategoryHelper;

public class FileListItem {
    public static void setupFileListItemInfo(Context context, View view,
                                             FileInfo fileInfo, IconHolder iconHolder,
                                             FileViewInteractionHub fileViewInteractionHub) {
        // if in moving mode, show selected file always
        if (fileViewInteractionHub.isMoveState()) {
            fileInfo.Selected = fileViewInteractionHub.isFileSelected(fileInfo.filePath);
        }

//        ImageView checkbox = (ImageView) view.findViewById(R.id.file_checkbox);
//        if (fileViewInteractionHub.getMode() == FileViewInteractionHub.Mode.Pick) {
//            checkbox.setVisibility(View.GONE);
//        } else {
//            checkbox.setVisibility(fileViewInteractionHub.canShowCheckBox()
//                    ? View.VISIBLE : View.GONE);
//            if ("grid".equals(LocalCache.getViewTag())) {
//                checkbox.setBackgroundResource(fileInfo.Selected
//                        ? R.mipmap.btn_check_on_holo_light
//                        : R.mipmap.btn_check_off_holo_light);
//            }
//            checkbox.setImageResource(fileInfo.Selected ? R.mipmap.btn_check_on_holo_light
//                    : R.mipmap.btn_check_off_holo_light);
//            checkbox.setTag(fileInfo);
//            view.setSelected(fileInfo.Selected);
//        }

        ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
        if ("list".equals(LocalCache.getViewTag())) {
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

//    public static class FileItemOnClickListener implements View.OnClickListener {
//        private FileViewInteractionHub mFileViewInteractionHub;
//
//        public FileItemOnClickListener(FileViewInteractionHub fileViewInteractionHub) {
//            mFileViewInteractionHub = fileViewInteractionHub;
//        }
//
//        @Override
//        public void onClick(View v) {
//            ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
//            assert (img != null && img.getTag() != null);
//            FileInfo tag = (FileInfo) img.getTag();
//            tag.Selected = !tag.Selected;
////            ll_tag.Selected = !ll_tag.Selected;
//            if (mFileViewInteractionHub.onCheckItem(tag, v)) {
//                if ("grid".equals(LocalCache.getViewTag())) {
//                    img.setBackgroundResource(tag.Selected ? R.mipmap.btn_check_on_holo_light
//                            : R.mipmap.btn_check_off_holo_light);
//                }
//                img.setImageResource(tag.Selected ? R.mipmap.btn_check_on_holo_light
//                        : R.mipmap.btn_check_off_holo_light);
//            } else {
//                tag.Selected = !tag.Selected;
//            }
//        }
//    }

//    public static class ModeCallback implements ActionMode.Callback {
//        private Menu mMenu;
//        private Context mContext;
//        private FileViewInteractionHub mFileViewInteractionHub;
//
//        private void initMenuItemSelectAllOrCancel() {
//            boolean isSelectedAll = mFileViewInteractionHub.isSelectedAll();
//            mMenu.findItem(R.id.action_cancel).setVisible(isSelectedAll);
//            mMenu.findItem(R.id.action_select_all).setVisible(!isSelectedAll);
//        }
//
//        private void scrollToSDcardTab() {
//            ActionBar bar = ((FileExplorerTabActivity) mContext).getActionBar();
//            if (bar.getSelectedNavigationIndex() != Util.SDCARD_TAB_INDEX) {
//                bar.setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
//            }
//        }
//
//        public ModeCallback(Context context,
//                            FileViewInteractionHub fileViewInteractionHub) {
//            mContext = context;
//            mFileViewInteractionHub = fileViewInteractionHub;
//        }
//
//        @Override
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            MenuInflater inflater = ((Activity) mContext).getMenuInflater();
//            mMenu = menu;
//            inflater.inflate(R.menu.operation_menu, mMenu);
//            initMenuItemSelectAllOrCancel();
//            return true;
//        }
//
//        @Override
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//            mMenu.findItem(R.id.action_copy_path).setVisible(
//                    mFileViewInteractionHub.getSelectedFileList().size() == 1);
//            mMenu.findItem(R.id.action_cancel).setVisible(
//                    mFileViewInteractionHub.isSelected());
//            mMenu.findItem(R.id.action_select_all).setVisible(
//                    !mFileViewInteractionHub.isSelectedAll());
//            return true;
//        }
//
//        @Override
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.action_delete:
//                    mFileViewInteractionHub.onOperationDelete();
//                    mode.finish();
//                    break;
//                case R.id.action_copy:
//                    ((FileViewActivity) ((FileExplorerTabActivity) mContext)
//                            .getFragment(Util.SDCARD_TAB_INDEX))
//                            .copyFile(mFileViewInteractionHub.getSelectedFileList());
//                    mode.finish();
//                    scrollToSDcardTab();
//                    break;
//                case R.id.action_move:
//                    ((FileViewActivity) ((FileExplorerTabActivity) mContext)
//                            .getFragment(Util.SDCARD_TAB_INDEX))
//                            .moveToFile(mFileViewInteractionHub.getSelectedFileList());
//                    mode.finish();
//                    scrollToSDcardTab();
//                    break;
//                case R.id.action_send:
//                    mFileViewInteractionHub.onOperationSend();
//                    mode.finish();
//                    break;
//                case R.id.action_copy_path:
//                    mFileViewInteractionHub.onOperationCopyPath();
//                    mode.finish();
//                    break;
//                case R.id.action_cancel:
//                    mFileViewInteractionHub.clearSelection();
//                    initMenuItemSelectAllOrCancel();
//                    mode.finish();
//                    break;
//                case R.id.action_select_all:
//                    mFileViewInteractionHub.onOperationSelectAll();
//                    initMenuItemSelectAllOrCancel();
//                    break;
//            }
//            Util.updateActionModeTitle(mode, mContext, mFileViewInteractionHub
//                    .getSelectedFileList().size());
//            return false;
//        }
//
//        @Override
//        public void onDestroyActionMode(ActionMode mode) {
//            mFileViewInteractionHub.clearSelection();
//            ((FileExplorerTabActivity) mContext).setActionMode(null);
//        }
//    }
}
