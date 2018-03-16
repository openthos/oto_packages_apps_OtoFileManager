package com.openthos.filemanager.component;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.openthos.filemanager.R;
import com.openthos.filemanager.BaseDialog;
import com.openthos.filemanager.utils.SambaUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class AddUsersDialog extends BaseDialog{
    private Context mContext;
    private String mPath;
    private EditText mEtAccount, mEtPassward;

    public AddUsersDialog(Context context) {
        super(context);
        mContext = context;
    }

    public AddUsersDialog(Context context, String path) {
        super(context);
        mContext = context;
        mPath = path;
    }

    public AddUsersDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected AddUsersDialog(Context context, boolean cancelable, OnCancelListener listener) {
        super(context, cancelable, listener);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_users);

        getWindow().setBackgroundDrawable(mContext.getResources().getDrawable(R.color.transparent));
        setCancelable(false);
        initTitle();
        initBody();
        initFoot();
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {

    }

    private void initTitle() {
        ImageView titleImage = (ImageView) findViewById(R.id.title_image);
        TextView titleText = (TextView) findViewById(R.id.title_text);
        titleText.setText((mContext.getResources().getString(R.string.operation_add_share_user)));
    }

    private void initBody() {
        mEtAccount = (EditText) findViewById(R.id.et_account);
        mEtPassward = (EditText) findViewById(R.id.et_passward);
    }

    private void initFoot() {
        TextView confirm = (TextView) findViewById(R.id.confirm);
        TextView cancel = (TextView) findViewById(R.id.cancel);
        View.OnClickListener click= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.confirm:
                        SambaUtils.addUserAndPasswd(mEtAccount.getText().toString(),
                                mEtPassward.getText().toString());
                        break;
                    case R.id.cancel:
                        break;
                }
                dismiss();
            }
        };
        confirm.setOnClickListener(click);
        cancel.setOnClickListener(click);
    }
}
