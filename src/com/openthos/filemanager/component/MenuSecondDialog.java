package com.openthos.filemanager.component;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.system.FileSortHelper;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.fragment.SystemSpaceFragment;

public class MenuSecondDialog extends Dialog implements View.OnClickListener {
    private TextView dialog_sort_name;
    private TextView dialog_sort_size;
    private TextView dialog_sort_time;
    private TextView dialog_sort_type;

    FileViewInteractionHub mFileViewInteractionHub;

    public MenuSecondDialog(Context context, int i,
                            FileViewInteractionHub mFileViewInteractionHub) {
        super(context);
        this.mFileViewInteractionHub = mFileViewInteractionHub;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_sort_dialog);
        initView();
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
                mFileViewInteractionHub.clearSelection();
                mFileViewInteractionHub.refreshFileList();
            }
        });
        initData();
    }

    private void initData() {
        dialog_sort_name.setOnClickListener(this);
        dialog_sort_size.setOnClickListener(this);
        dialog_sort_time.setOnClickListener(this);
        dialog_sort_type.setOnClickListener(this);
    }

    private void initView() {
        dialog_sort_name = (TextView) findViewById(R.id.dialog_sort_name);
        dialog_sort_size = (TextView) findViewById(R.id.dialog_sort_size);
        dialog_sort_time = (TextView) findViewById(R.id.dialog_sort_time);
        dialog_sort_type = (TextView) findViewById(R.id.dialog_sort_type);
    }

    public void showSecondDialog(int x, int y, int height, int width) {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        show();
        lp.width = width;
        lp.height = height;
        lp.x = x + 220;
        lp.y = y + 50;
        dialogWindow.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_sort_name:
                setSortPositive(FileSortHelper.SortMethod.name);
                mFileViewInteractionHub.onSortChanged(FileSortHelper.SortMethod.name);
                this.dismiss();
                break;
            case R.id.dialog_sort_size:
                setSortPositive(FileSortHelper.SortMethod.size);
                mFileViewInteractionHub.onSortChanged(FileSortHelper.SortMethod.size);
                this.dismiss();
                break;
            case R.id.dialog_sort_time:
                setSortPositive(FileSortHelper.SortMethod.date);
                mFileViewInteractionHub.onSortChanged(FileSortHelper.SortMethod.date);
                this.dismiss();
                break;
            case R.id.dialog_sort_type:
                setSortPositive(FileSortHelper.SortMethod.type);
                mFileViewInteractionHub.onSortChanged(FileSortHelper.SortMethod.type);
                this.dismiss();
                break;
            default:
                break;
        }
    }

    private void setSortPositive(Enum sort) {
        SystemSpaceFragment fragment = (SystemSpaceFragment)(mFileViewInteractionHub.
                                                     getMainActivity().mCurFragment);
        fragment.setSortTag(sort, !fragment.getSortTag(sort));
    }
}
