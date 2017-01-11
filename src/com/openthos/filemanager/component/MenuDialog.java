package com.openthos.filemanager.component;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.app.Activity;
import android.widget.LinearLayout;
import android.view.Display;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;

import java.util.ArrayList;

public class MenuDialog extends Dialog implements View.OnClickListener {
    private TextView mDialogOpen;
    private TextView mDialogOpenWith;
    private TextView mDialogCompress;
    private TextView mDialogDecompress;
    private TextView mDialogCopy;
    private TextView mDialogPaste;
    private TextView mDialogRename;
    private TextView mDialogDelete;
    private TextView mDialogMove;
    private TextView mDialogSend;
    private TextView mDialogSort;
    private TextView mDialogCopyPath;
    private TextView mDialogInfo;
    private TextView mDialogNewFolder;
    private TextView mDialogNewFile;
    private TextView mDialogVisiableFile;
    private LinearLayout mLinearLayout;
    private int mDialogWidth;
    private int mDialogHeight;
    private Context mContext;
    private FileViewInteractionHub mFileViewInteractionHub;
    private static boolean isCopy = false;
    private int newX;
    private int newY;
    private MenuSecondDialog menuSecondDialog;
    private MotionEvent mMotionEvent;
    private String PERMISS_DIR_SDCARD = "/sdcard";
    private String PERMISS_DIR_STORAGE_SDCARD = "/storage/sdcard";
    //private String PERMISS_DIR_STORAGE_USB = "/storage/usb";
    private String PERMISS_DIR_STORAGE_EMULATED_LEGACY = "/storage/emulated/legacy";
    private String PERMISS_DIR_STORAGE_EMULATED_0 = "/storage/emulated/0";

    public MenuDialog(Context context, int id, FileViewInteractionHub fileViewInteractionHub,
                                                MotionEvent motionEvent) {
        super(context);
        mContext = context;
        mFileViewInteractionHub = fileViewInteractionHub;
        mMotionEvent = motionEvent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_dialog);
        initView();
        /*setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
                mFileViewInteractionHub.clearSelection();
                mFileViewInteractionHub.refreshFileList();
            }
        });*/
        initData();
    }

    private void initData() {
        String path = mFileViewInteractionHub.getCurrentPath();
        boolean hasPermission = false;
        if (path.startsWith(PERMISS_DIR_SDCARD)
               || path.startsWith(PERMISS_DIR_STORAGE_SDCARD)
               || path.startsWith(Constants.PERMISS_DIR_STORAGE_USB)
               || path.startsWith(PERMISS_DIR_STORAGE_EMULATED_LEGACY)
               || path.startsWith(Constants.PERMISS_DIR_SEAFILE)
               || path.startsWith(PERMISS_DIR_STORAGE_EMULATED_0)) {
            hasPermission = true;
        }
        mDialogCopy.setTextColor(hasPermission ? Color.BLACK :Color.LTGRAY);
        mDialogPaste.setTextColor(hasPermission ? Color.BLACK :Color.LTGRAY);
        mDialogMove.setTextColor(hasPermission ? Color.BLACK :Color.LTGRAY);
        mDialogRename.setTextColor(hasPermission ? Color.BLACK :Color.LTGRAY);
        mDialogDelete.setTextColor(hasPermission ? Color.BLACK :Color.LTGRAY);
        mDialogSend.setTextColor(hasPermission ? Color.BLACK :Color.LTGRAY);
        mDialogSort.setTextColor(hasPermission ? Color.BLACK :Color.LTGRAY);
        mDialogNewFolder.setTextColor(hasPermission ? Color.BLACK :Color.LTGRAY);
        mDialogNewFile.setTextColor(hasPermission ? Color.BLACK :Color.LTGRAY);
        if (hasPermission) {
            mDialogCopy.setOnClickListener(this);
            mDialogMove.setOnClickListener(this);
            mDialogRename.setOnClickListener(this);
            mDialogDelete.setOnClickListener(this);
            mDialogSend.setOnClickListener(this);
            mDialogSort.setOnClickListener(this);
            mDialogNewFolder.setOnClickListener(this);
            mDialogNewFile.setOnClickListener(this);
        }
        mDialogCopyPath.setOnClickListener(this);
        mDialogVisiableFile.setOnClickListener(this);
        mDialogCompress.setOnClickListener(this);
        mDialogDecompress.setOnClickListener(this);
        mDialogInfo.setOnClickListener(this);
        mDialogOpen.setOnClickListener(this);
        mDialogOpenWith.setOnClickListener(this);
        String sourcePath = "";
        try {
            sourcePath = (String)
                ((ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE)).getText();
        } catch (ClassCastException e) {
            sourcePath = "";
        }
        if (hasPermission) {
            if (!TextUtils.isEmpty(sourcePath)
                  && (sourcePath.startsWith(Intent.EXTRA_FILE_HEADER)
                       || sourcePath.startsWith(Intent.EXTRA_CROP_FILE_HEADER))) {
                mDialogPaste.setTextColor(Color.BLACK);
                mDialogPaste.setOnClickListener(this);
                isCopy = false;
            } else {
                mDialogPaste.setTextColor(Color.LTGRAY);
            }
        }
    }

    private void initView() {
        mDialogOpen = (TextView) findViewById(R.id.dialog_open);
        mDialogOpenWith = (TextView) findViewById(R.id.dialog_open_with);
        mDialogCopy = (TextView) findViewById(R.id.dialog_copy);
        mDialogPaste = (TextView) findViewById(R.id.dialog_paste);
        mDialogRename = (TextView) findViewById(R.id.dialog_rename);
        mDialogDelete = (TextView) findViewById(R.id.dialog_delete);
        mDialogMove = (TextView) findViewById(R.id.dialog_move);
        mDialogSend = (TextView) findViewById(R.id.dialog_send);
        mDialogSort = (TextView) findViewById(R.id.dialog_sort);
        mDialogCopyPath = (TextView) findViewById(R.id.dialog_copy_path);
        mDialogInfo = (TextView) findViewById(R.id.dialog_info);
        mDialogNewFolder = (TextView) findViewById(R.id.dialog_new_folder);
        mDialogNewFile = (TextView) findViewById(R.id.dialog_new_file);
        mDialogVisiableFile = (TextView) findViewById(R.id.dialog_visibale_file);
        mDialogCompress = (TextView) findViewById(R.id.dialog_compress);
        mDialogDecompress = (TextView) findViewById(R.id.dialog_decompress);
        mLinearLayout = (LinearLayout) findViewById(R.id.dialog_ll);
        mLinearLayout.measure(0, 0);
        mDialogWidth = mLinearLayout.getMeasuredWidth();
        mDialogHeight = mLinearLayout.getMeasuredHeight();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_open:
                mFileViewInteractionHub.onOperationOpen(mMotionEvent);
                mFileViewInteractionHub.clearSelection();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_open_with:
                showOpenWith();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_copy:
                try {
                    //mFileViewInteractionHub.doOnOperationCopy();
                    isCopy = true;
                    MainActivity.mHandler.sendEmptyMessage(Constants.COPY);
                    mFileViewInteractionHub.dismissContextDialog();
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.dialog_paste:
                //mFileViewInteractionHub.getSelectedFileList();
                //mFileViewInteractionHub.onOperationButtonConfirm();
                MainActivity.mHandler.sendEmptyMessage(Constants.PASTE);
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_rename:
                mFileViewInteractionHub.onOperationRename();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_delete:
                mFileViewInteractionHub.onOperationDelete();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_move:
                isCopy = true;
                //mFileViewInteractionHub.onOperationMove();
                MainActivity.mHandler.sendEmptyMessage(Constants.CUT);
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_send:
                mFileViewInteractionHub.onOperationSend();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_sort:
                mFileViewInteractionHub.dismissContextDialog();
                menuSecondDialog = new MenuSecondDialog
                                   (mContext, R.style.menu_dialog,mFileViewInteractionHub);
                menuSecondDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                menuSecondDialog.showSecondDialog(newX,newY,210,160);
                break;
            case R.id.dialog_info:
                mFileViewInteractionHub.onOperationInfo();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_copy_path:
                mFileViewInteractionHub.onOperationCopyPath();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_new_folder:
                mFileViewInteractionHub.onOperationCreateFolder();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_new_file:
                mFileViewInteractionHub.onOperationCreateFile();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_visibale_file:
                mFileViewInteractionHub.onOperationShowSysFiles();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_compress:
                mFileViewInteractionHub.onOperationCompress();
                mFileViewInteractionHub.dismissContextDialog();
                break;
            case R.id.dialog_decompress:
                mFileViewInteractionHub.onOperationDecompress();
                mFileViewInteractionHub.dismissContextDialog();
                break;
        }
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
        show();
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
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
