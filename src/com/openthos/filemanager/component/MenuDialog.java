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

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.system.Constants;

public class MenuDialog extends Dialog implements View.OnClickListener {
    private TextView dialog_copy;
    private TextView dialog_paste;
    private TextView dialog_rename;
    private TextView dialog_delete;
    private TextView dialog_move;
    private TextView dialog_send;
    private TextView dialog_sort;
    private TextView dialog_copy_path;
    private TextView dialog_info;
    private TextView dialog_new_folder;
    private TextView dialog_new_file;
    private TextView dialog_visibale_file;
    private LinearLayout mLinearLayout;
    private int mDialogWidth;
    private int mDialogHeight;
    private Context context;
    private FileViewInteractionHub mFileViewInteractionHub;
    private static boolean isCopy = false;
    private int newX;
    private int newY;
    private MenuSecondDialog menuSecondDialog;

    public MenuDialog(Context mContext, int id, FileViewInteractionHub mFileViewInteractionHub) {
        super(mContext);
        this.context = mContext;
        this.mFileViewInteractionHub = mFileViewInteractionHub;
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
        dialog_copy.setOnClickListener(this);
        String sourcePath = "";
        try {
            sourcePath = (String)
                 ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).getText();
        } catch (ClassCastException e) {
            sourcePath = "";
        }
        if (!TextUtils.isEmpty(sourcePath)
              && (sourcePath.startsWith(Intent.EXTRA_FILE_HEADER)
                   || sourcePath.startsWith(Intent.EXTRA_CROP_FILE_HEADER))) {
            dialog_paste.setTextColor(Color.BLACK);
            dialog_paste.setOnClickListener(this);
            isCopy = false;
        } else {
            dialog_paste.setTextColor(Color.LTGRAY);
        }
        dialog_rename.setOnClickListener(this);
        dialog_delete.setOnClickListener(this);
        dialog_move.setOnClickListener(this);
        dialog_send.setOnClickListener(this);
        dialog_sort.setOnClickListener(this);
        dialog_info.setOnClickListener(this);
        dialog_new_folder.setOnClickListener(this);
        dialog_new_file.setOnClickListener(this);
        dialog_copy_path.setOnClickListener(this);
        dialog_visibale_file.setOnClickListener(this);
    }

    private void initView() {
        dialog_copy = (TextView) findViewById(R.id.dialog_copy);
        dialog_paste = (TextView) findViewById(R.id.dialog_paste);
        dialog_rename = (TextView) findViewById(R.id.dialog_rename);
        dialog_delete = (TextView) findViewById(R.id.dialog_delete);
        dialog_move = (TextView) findViewById(R.id.dialog_move);
        dialog_send = (TextView) findViewById(R.id.dialog_send);
        dialog_sort = (TextView) findViewById(R.id.dialog_sort);
        dialog_copy_path = (TextView) findViewById(R.id.dialog_copy_path);
        dialog_info = (TextView) findViewById(R.id.dialog_info);
        dialog_new_folder = (TextView) findViewById(R.id.dialog_new_folder);
        dialog_new_file = (TextView) findViewById(R.id.dialog_new_file);
        dialog_visibale_file = (TextView) findViewById(R.id.dialog_visibale_file);
        mLinearLayout = (LinearLayout) findViewById(R.id.dialog_ll);
        mLinearLayout.measure(0, 0);
        mDialogWidth = mLinearLayout.getMeasuredWidth();
        mDialogHeight = mLinearLayout.getMeasuredHeight();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
                                   (context, R.style.menu_dialog,mFileViewInteractionHub);
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
        }
    }

    public void showDialog(int x, int y) {
        show();
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager m = ((Activity) context).getWindowManager();
        Display d = m.getDefaultDisplay();
        int dialogPadding = (int) context.getResources().getDimension(R.dimen.left_margrin_text);
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
