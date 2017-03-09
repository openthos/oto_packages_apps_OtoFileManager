package com.openthos.filemanager.component;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.BaseDialog;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class UsbPropertyDialog extends BaseDialog {
    private Context mContext;
    private String[] mUsbs;

    public UsbPropertyDialog(Context context, String[] usbs) {
        super(context);
        mContext = context;
        mUsbs = usbs;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_usb_property);
        getWindow().setBackgroundDrawable(mContext.getResources().
                                          getDrawable(R.color.transparent));
        initBody();
        initFoot();
    }

    private void initBody() {
        TextView size = (TextView) findViewById(R.id.size);
        TextView sizeOnDisk = (TextView) findViewById(R.id.size_on_disk);
        size.setText(mUsbs[1]);
        sizeOnDisk.setText(mUsbs[3]);
    }

    private void initFoot() {
        TextView confirm = (TextView) findViewById(R.id.confirm);
        View.OnClickListener click= new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        };
        confirm.setOnClickListener(click);
    }

    public void showDialog() {
        show();
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
    }
}
