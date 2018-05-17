package org.openthos.filemanager.component;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.RadioButton;

import org.openthos.filemanager.R;
import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.utils.SeafileUtils;

public class CloudInfoDialog extends Dialog {
    private Context mContext;
    private TextView mTvAccount, mTvConfirm;
    private RadioButton mRbSync, mRbDesync;

    public CloudInfoDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_cloud_info);
        getWindow().setBackgroundDrawable(mContext.getResources().
                                          getDrawable(R.color.transparent));
        initView();
    }

    private void initView() {
        mTvAccount = (TextView) findViewById(R.id.tv_account);
        mRbSync = (RadioButton) findViewById(R.id.rb_sync);
        mRbDesync = (RadioButton) findViewById(R.id.rb_desync);
        mTvConfirm = (TextView) findViewById(R.id.tv_confirm);

        refreshView();

        ClickListener clickListener = new ClickListener();
        mRbSync.setOnClickListener(clickListener);
        mRbDesync.setOnClickListener(clickListener);
        mTvConfirm.setOnClickListener(clickListener);
    }

    public void refreshView() {
        if (SeafileUtils.isExistsAccount()) {
            mRbSync.setEnabled(true);
            mRbDesync.setEnabled(true);
            mTvAccount.setText(SeafileUtils.mUserId);
            if (((MainActivity) mContext).mSeafileFragment.getList().get(0).isSync) {
                mRbSync.setChecked(true);
                mRbDesync.setChecked(false);
            } else {
                mRbSync.setChecked(false);
                mRbDesync.setChecked(true);
            }
        } else {
            mTvAccount.setText(mContext.getResources().getString(R.string.account_non));
            mRbSync.setEnabled(false);
            mRbDesync.setEnabled(false);
        }

    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rb_sync:
                    try {
                        ((MainActivity) mContext).mISeafileService.syncData();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    ((MainActivity) mContext).mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
                    break;
                case R.id.rb_desync:
                    try {
                        ((MainActivity) mContext).mISeafileService.desyncData();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    ((MainActivity) mContext).mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
                    break;
                case R.id.tv_confirm:
                    dismiss();
                    break;
            }
        }
    }

    public void showDialog() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        show();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
    }
}
