package com.openthos.filemanager.component;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.BaseDialog;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.BaseDialogAdapter;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;

import java.util.ArrayList;

public class MenuDialog extends BaseDialog implements ListView.OnItemClickListener {
    private int mX;
    private int mY;
    private MotionEvent mMotionEvent;
    private boolean mCanCopy;
    private SortDialog mSortDialog;

    public MenuDialog(Context context, FileViewInteractionHub fileViewInteractionHub,
                      MotionEvent motionEvent) {
        super(context);
        mActivity = (MainActivity) context;
        mFileViewInteractionHub = fileViewInteractionHub;
        mMotionEvent = motionEvent;
    }

    @Override
    protected void initData() {
        String sourcePath = "";
        try {
            sourcePath = (String)
                    ((ClipboardManager)
                            mActivity.getSystemService(Context.CLIPBOARD_SERVICE)).getText();
        } catch (ClassCastException e) {
            sourcePath = "";
        }
        if (!TextUtils.isEmpty(sourcePath)
                && (sourcePath.startsWith(Constants.EXTRA_FILE_HEADER)
                || sourcePath.startsWith(Constants.EXTRA_CROP_FILE_HEADER))) {
            mCanCopy = true;
        } else {
            mCanCopy = false;
        }
        mDatas = new ArrayList();
        if (mActivity.isRecycle()) {
            if (mFileViewInteractionHub.isBlank()) {
                prepareData(mActivity.getResources().getStringArray(R.array.recycle_blank));
            } else if (mFileViewInteractionHub.isMultiChosen()) {
                prepareData(mActivity.getResources().getStringArray(R.array.recycle_multi));
            } else {
                prepareData(mActivity.getResources().getStringArray(R.array.recycle_single));
            }
        } else if (mFileViewInteractionHub.isBlank()) {
            if (mFileViewInteractionHub.isProtected()) {
                prepareData(
                        mActivity.getResources().getStringArray(R.array.protected_blank_menu));
            } else {
                prepareData(mActivity.getResources().getStringArray(R.array.common_blank_menu));
            }
        } else {
            if (mFileViewInteractionHub.isProtected()) {
                if (mFileViewInteractionHub.isMultiChosen()) {
                    prepareData(mActivity.getResources()
                            .getStringArray(R.array.protected_multi_chosen_menu));
                } else if (mFileViewInteractionHub.isDirectory()) {
                    prepareData(mActivity.getResources()
                            .getStringArray(R.array.protected_folder_menu));
                } else {
                    prepareData(mActivity.getResources()
                            .getStringArray(R.array.protected_file_menu));
                }
            } else {
                if (mFileViewInteractionHub.isMultiChosen()) {
                    prepareData(mActivity.getResources()
                            .getStringArray(R.array.common_multi_chosen_menu));
                } else if (mFileViewInteractionHub.isDirectory()) {
                    prepareData(mActivity.getResources()
                            .getStringArray(R.array.common_folder_menu));
                } else {
                    prepareData(
                            mActivity.getResources().getStringArray(R.array.common_file_menu));
                }
            }
        }
        mListView.setAdapter(new BaseDialogAdapter(getContext(),
                mDatas, mFileViewInteractionHub, mCanCopy));
    }

    @Override
    protected void initListener() {
        mListView.setOnItemClickListener(this);
    }

    private void prepareData(String[] sArr) {
        for (int i = 0; i < sArr.length; i++) {
            mDatas.add(sArr[i]);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String content = (String) view.getTag();
        if (mActivity.getString(R.string.operation_open).equals(content)) {
            mFileViewInteractionHub.onOperationOpen(mMotionEvent);
        } else if (mActivity.getString(R.string.operation_open_with).equals(content)) {
            showOpenWith();
        } else if (mActivity.getString(R.string.operation_copy).equals(content)) {
            mCanCopy = true;
            mActivity.copy();
        } else if (mActivity.getString(R.string.operation_paste).equals(content)) {
            if (mCanCopy) {
                mActivity.paste();
            }
        } else if (mActivity.getString(R.string.operation_rename).equals(content)) {
            mFileViewInteractionHub.onOperationRename();
        } else if (mActivity.getString(R.string.operation_delete).equals(content)) {
            mFileViewInteractionHub.onOperationDelete();
        } else if (mActivity.getString(R.string.operation_move).equals(content)) {
            mCanCopy = true;
            mActivity.cut();
        } else if (mActivity.getString(R.string.operation_send).equals(content)) {
            mFileViewInteractionHub.onOperationSend();
        } else if (mActivity.getString(R.string.menu_item_sort).equals(content)) {
            mSortDialog = new SortDialog(mActivity, mFileViewInteractionHub);
            mSortDialog.showDialog(
                    mX + 1 + 1 + getWindow().getDecorView().getWidth()
                            - getWindow().getDecorView().getPaddingLeft()
                            - getWindow().getDecorView().getPaddingRight(),
                    mY + mListView.getPaddingTop() +
                            (getWindow().getDecorView().getHeight()
                                    - mListView.getPaddingTop() - mListView.getPaddingBottom())
                                    / mDatas.size() * i);
            return;
        } else if (mActivity.getString(R.string.operation_copy_path).equals(content)) {
            mFileViewInteractionHub.onOperationCopyPath();
        } else if (mActivity.getString(R.string.operation_info).equals(content)) {
            mFileViewInteractionHub.onOperationInfo();
        } else if (mActivity.getString(R.string.operation_create_folder).equals(content)) {
            mFileViewInteractionHub.onOperationCreateFolder();
        } else if (mActivity.getString(R.string.operation_create_file).equals(content)) {
            mFileViewInteractionHub.onOperationCreateFile();
        } else if (mActivity.getString(R.string.operation_show_sys).equals(content)) {
            mFileViewInteractionHub.onOperationShowSysFiles();
        } else if (mActivity.getString(R.string.operation_delete_permanent).equals(content)) {
            mFileViewInteractionHub.onOperationDeleteDirect();
        } else if (mActivity.getString(R.string.operation_compress).equals(content)) {
            mFileViewInteractionHub.onOperationCompress();
        } else if (mActivity.getString(R.string.operation_decompress).equals(content)) {
            mFileViewInteractionHub.onOperationDecompress();
        } else if (mActivity.getString(R.string.recycle_all_clean).equals(content)) {
            mFileViewInteractionHub.onOperationCleanRecycle();
        } else if (mActivity.getString(R.string.recycle_all_restore).equals(content)) {
            mFileViewInteractionHub.onOperationRestore(true);
        } else if (mActivity.getString(R.string.recycle_restore).equals(content)) {
            mFileViewInteractionHub.onOperationRestore(false);
        } else if (mActivity.getString(R.string.recycle_delete).equals(content)) {
        } else if (mActivity.getString(R.string.recycle_cut).equals(content)) {
        } else if (mActivity.getString(R.string.recycle_detail).equals(content)) {
        }

        dismiss();
    }

    private void showOpenWith() {
        ArrayList<FileInfo> selectedFileList = mFileViewInteractionHub.getSelectedFileList();
        if (selectedFileList.size() != 0
                && !selectedFileList.get(selectedFileList.size() - 1).IsDir) {
            String filePath = selectedFileList.get(selectedFileList.size() - 1).filePath;
            OpenWithDialog openWithDialog = new OpenWithDialog(mActivity, filePath);
            openWithDialog.showDialog();
        }
    }

    @Override
    public void showDialog(int x, int y) {
        super.showDialog(x, y);
        mX = x;
        mY = y;
    }
}
