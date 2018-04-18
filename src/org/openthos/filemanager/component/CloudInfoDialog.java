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
import org.openthos.filemanager.system.Constants;
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
            int isSync = ((MainActivity) mContext).mLibrarys.get(0).isSync;
            if (isSync == SeafileUtils.SYNC) {
                mRbSync.setChecked(true);
                mRbDesync.setChecked(false);
            } else if (isSync == SeafileUtils.UNSYNC) {
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
                    ((MainActivity) mContext).mLibrarys.get(0).isSync = SeafileUtils.SYNC;
                    try {
                        ((MainActivity) mContext).mISeafileService.sync(
                                (String) (((MainActivity) mContext).mLibrarys.get(0).libraryId),
                                ((MainActivity) mContext).mLibrarys.get(0).libraryName,
                                "/" + SeafileUtils.mUserId + "/"
                                + ((MainActivity) mContext).mLibrarys.get(0).libraryName);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    ((MainActivity) mContext).mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
                    break;
                case R.id.rb_desync:
                    ((MainActivity) mContext).mLibrarys.get(0).isSync = SeafileUtils.UNSYNC;
                    try {
                        ((MainActivity) mContext).mISeafileService.desync(
                                (String) (((MainActivity) mContext).mLibrarys.get(0).libraryId),
                                ((MainActivity) mContext).mLibrarys.get(0).libraryName,
                                "/" + SeafileUtils.mUserId + "/"
                                + ((MainActivity) mContext).mLibrarys.get(0).libraryName);
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
