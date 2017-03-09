package com.openthos.filemanager.component;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.BaseDialog;
import com.openthos.filemanager.R;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;
import java.util.ArrayList;
import static android.R.color.holo_purple;
import static android.R.color.transparent;

public class MenuFirstDialog extends BaseDialog
                             implements View.OnHoverListener, ListView.OnItemClickListener{
    private Context mContext;
    private MainActivity mMainActivity;
    private FileViewInteractionHub mFileViewInteractionHub;
    private int newX;
    private int newY;
    private MotionEvent mMotionEvent;
    private ListView mListView;
    private ArrayList mData;
    private static boolean isCopy;
    private MenuSecondDialog menuSecondDialog;
    private int mDialogHeight;
    private int mDialogWidth = 176;
    private int singleHeight = 40;
    private int fix = 20;

    public MenuFirstDialog(Context context, FileViewInteractionHub fileViewInteractionHub,
             MotionEvent motionEvent) {
        super(context);
        mContext = context;
        mMainActivity = (MainActivity) mContext;
        mFileViewInteractionHub = fileViewInteractionHub;
        mMotionEvent = motionEvent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_base);
        initView();
        initData();
        initListener();
    }

    public void initView() {
        mListView = (ListView) findViewById(R.id.dialog_base_lv);
    }

    public void initData() {
        String sourcePath = "";
        try {
            sourcePath = (String)
                ((ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE)).getText();
        } catch (ClassCastException e) {
            sourcePath = "";
        }
        if (!TextUtils.isEmpty(sourcePath)
                && (sourcePath.startsWith(Intent.EXTRA_FILE_HEADER)
                || sourcePath.startsWith(Intent.EXTRA_CROP_FILE_HEADER))) {
            isCopy = true;
        } else {
            isCopy = false;
        }

        mData = new ArrayList();
        if (mMainActivity.isRecycle()) {
            if (mFileViewInteractionHub.isBlank()) {
                prepareData(mContext.getResources().getStringArray(R.array.recycle_blank));
            } else if (mFileViewInteractionHub.isMultiChosen()) {
                prepareData(mContext.getResources().getStringArray(R.array.recycle_multi));
            } else {
                prepareData(mContext.getResources().getStringArray(R.array.recycle_single));
            }
        } else if (mFileViewInteractionHub.isBlank()) {
            if (mFileViewInteractionHub.isProtected()) {
                prepareData(mContext.getResources().getStringArray(R.array.protected_blank_menu));
            } else {
                prepareData(mContext.getResources().getStringArray(R.array.common_blank_menu));
            }
        } else {
            if (mFileViewInteractionHub.isProtected()) {
                if (mFileViewInteractionHub.isMultiChosen()) {
                    prepareData(mContext.getResources()
                            .getStringArray(R.array.protected_multi_chosen_menu));
                } else if (mFileViewInteractionHub.isDirectory()) {
                    prepareData(mContext.getResources()
                                            .getStringArray(R.array.protected_folder_menu));
                } else {
                    prepareData(mContext.getResources()
                                            .getStringArray(R.array.protected_file_menu));
                }
            } else {
                if (mFileViewInteractionHub.isMultiChosen()) {
                    prepareData(mContext.getResources()
                            .getStringArray(R.array.common_multi_chosen_menu));
                } else if (mFileViewInteractionHub.isDirectory()) {
                    prepareData(mContext.getResources()
                                            .getStringArray(R.array.common_folder_menu));
                } else {
                    prepareData(mContext.getResources().getStringArray(R.array.common_file_menu));
                }
            }
        }
        BaseDialogAdapter mAdapter = new BaseDialogAdapter();
        mListView.setAdapter(mAdapter);
        mDialogHeight = mData.size() * singleHeight - fix;
    }

    protected void initListener() {
        mListView.setOnItemClickListener(this);
    }

    public void prepareData(String[] sArr) {
        for (int i = 0; i < sArr.length; i++) {
            mData.add(sArr[i]);
        }
    }

    class BaseDialogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = View.inflate(mContext, R.layout.dialog_base_item, null);
            TextView mTvDialogItem = (TextView) view.findViewById(R.id.dialog_base_item);
            String content = mData.get(i).toString();
            mTvDialogItem.setText(content);
            boolean isSetHoverListener = true;
            if (content.equals(mContext.getString(R.string.operation_paste).toString())) {
                if (isCopy) {
                    mTvDialogItem.setTextColor(Color.BLACK);
                } else {
                    mTvDialogItem.setTextColor(Color.LTGRAY);
                    isSetHoverListener = false;
                }
            } else if (content.equals(mContext.getString(R.string.operation_compress))) {
                switch (mFileViewInteractionHub.getCompressFileState()) {
                    case Constants.COMPRESSIBLE:
                    case Constants.COMPRESSIBLE_DECOMPRESSIBLE:
                        mTvDialogItem.setTextColor(Color.BLACK);
                        break;
                    case Constants.DECOMPRESSIBLE:
                        mTvDialogItem.setTextColor(Color.LTGRAY);
                        isSetHoverListener = false;
                        break;
                    default:
                        break;
                }
            } else if (content.equals(mContext.getString(R.string.operation_decompress))) {
                switch (mFileViewInteractionHub.getCompressFileState()) {
                    case Constants.COMPRESSIBLE:
                        mTvDialogItem.setTextColor(Color.LTGRAY);
                        isSetHoverListener = false;
                        break;
                    case Constants.DECOMPRESSIBLE:
                    case Constants.COMPRESSIBLE_DECOMPRESSIBLE:
                        mTvDialogItem.setTextColor(Color.BLACK);
                        break;
                    default:
                        break;
                }
            }
            if (isSetHoverListener) {
                view.setOnHoverListener(MenuFirstDialog.this);
                view.setTag(content);
            } else {
                view.setTag("");
            }
            return view;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String content = (String) view.getTag();
        if (mContext.getString(R.string.operation_open).equals(content)) {
            mFileViewInteractionHub.onOperationOpen(mMotionEvent);
        } else if (mContext.getString(R.string.operation_open_with).equals(content)) {
            showOpenWith();
        } else if (mContext.getString(R.string.operation_copy).equals(content)) {
            isCopy = true;
            mMainActivity.copy();
        } else if (mContext.getString(R.string.operation_paste).equals(content)) {
            if (isCopy) {
                mMainActivity.paste();
            }
        } else if (mContext.getString(R.string.operation_rename).equals(content)) {
            mFileViewInteractionHub.onOperationRename();
        } else if (mContext.getString(R.string.operation_delete).equals(content)) {
            mFileViewInteractionHub.onOperationDelete();
        } else if (mContext.getString(R.string.operation_move).equals(content)) {
            isCopy = true;
            mMainActivity.cut();
        } else if (mContext.getString(R.string.operation_send).equals(content)) {
            mFileViewInteractionHub.onOperationSend();
        } else if (mContext.getString(R.string.menu_item_sort).equals(content)) {
            menuSecondDialog = new MenuSecondDialog(
                    mContext, R.style.menu_dialog,mFileViewInteractionHub);
            menuSecondDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            menuSecondDialog.showSecondDialog(newX,newY,210,160);
        } else if (mContext.getString(R.string.operation_copy_path).equals(content)) {
            mFileViewInteractionHub.onOperationCopyPath();
        } else if (mContext.getString(R.string.operation_info).equals(content)) {
            mFileViewInteractionHub.onOperationInfo();
        } else if (mContext.getString(R.string.operation_create_folder).equals(content)) {
            mFileViewInteractionHub.onOperationCreateFolder();
        } else if (mContext.getString(R.string.operation_create_file).equals(content)) {
            mFileViewInteractionHub.onOperationCreateFile();
        } else if (mContext.getString(R.string.operation_show_sys).equals(content)) {
            mFileViewInteractionHub.onOperationShowSysFiles();
        } else if (mContext.getString(R.string.operation_delete_permanent).equals(content)) {
            mFileViewInteractionHub.onOperationDeleteDirect();
        } else if (mContext.getString(R.string.operation_compress).equals(content)) {
            mFileViewInteractionHub.onOperationCompress();
        } else if (mContext.getString(R.string.operation_decompress).equals(content)) {
            mFileViewInteractionHub.onOperationDecompress();
        } else if (mContext.getString(R.string.recycle_all_clean).equals(content)) {
            mFileViewInteractionHub.onOperationCleanRecycle();
        } else if (mContext.getString(R.string.recycle_all_restore).equals(content)) {
            mFileViewInteractionHub.onOperationRestore(true);
        } else if (mContext.getString(R.string.recycle_restore).equals(content)) {
            mFileViewInteractionHub.onOperationRestore(false);
        } else if (mContext.getString(R.string.recycle_delete).equals(content)) {

        } else if (mContext.getString(R.string.recycle_cut).equals(content)) {

        } else if (mContext.getString(R.string.recycle_detail).equals(content)) {

        }
        dismiss();
    }

    @Override
    public boolean onHover(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_HOVER_ENTER:
                view.setBackgroundColor(mContext.getResources().getColor(holo_purple));
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                view.setBackgroundColor(mContext.getResources().getColor(transparent));
                break;
        }
        return false;
    }

    private void showOpenWith() {
        ArrayList<FileInfo> selectedFileList = mFileViewInteractionHub.getSelectedFileList();
        if (selectedFileList.size() != 0
               && !selectedFileList.get(selectedFileList.size() - 1).IsDir) {
            String filePath = selectedFileList.get(selectedFileList.size() - 1).filePath;
            OpenWithDialog openWithDialog = new OpenWithDialog(mContext, filePath);
            openWithDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            openWithDialog.showDialog();
        }
    }

    public void showDialog(int x, int y) {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        show();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager m = ((Activity) mContext).getWindowManager();
        Display d = m.getDefaultDisplay();
        int dialogPadding = (int) mContext.getResources().getDimension(R.dimen.left_margrin_text);
        if (x > (d.getWidth() - mDialogWidth)) {
            lp.x = x - mDialogWidth + dialogPadding;
        } else {
            lp.x = x + dialogPadding;
        }
        if (y > (d.getHeight() - mDialogHeight - Constants.BAR_Y)) {
            lp.y = d.getHeight() - mDialogHeight - Constants.BAR_Y + dialogPadding;

        } else {
            lp.y = y + dialogPadding;
        }
        newX = x;
        newY = y;
        dialogWindow.setAttributes(lp);
    }
}
