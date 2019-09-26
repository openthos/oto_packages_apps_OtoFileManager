package org.openthos.filemanager.component;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openthos.filemanager.BaseMenuDialog;
import org.openthos.filemanager.R;
import org.openthos.filemanager.utils.SambaUtils;

public class ModifyPasswdDialog extends BaseMenuDialog{
    private Context mContext;
    private EditText mEtPassward;
    private String mAccount;

    public ModifyPasswdDialog(Context context, String account) {
        super(context);
        mContext = context;
        mAccount = account;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_modify_passwd);
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
        TextView titleText = (TextView) findViewById(R.id.title_text);
        titleText.setText(mContext.getResources().getString(R.string.dialog_modify_password));
    }

    private void initBody() {
        TextView mTvAccount = (TextView) findViewById(R.id.tv_account);
        mTvAccount.setText(mAccount);
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
                        SambaUtils.modifyPasswd(mAccount, mEtPassward.getText().toString());
                        dismiss();
                        break;
                    case R.id.cancel:
                        dismiss();
                        break;
                }
            }
        };
        confirm.setOnClickListener(click);
        cancel.setOnClickListener(click);
    }
}
