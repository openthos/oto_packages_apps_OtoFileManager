package org.openthos.filemanager.component;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.RemoteException;

import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.BaseMenuDialog;
import org.openthos.filemanager.R;
import org.openthos.filemanager.bean.SeafileLibrary;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.system.TextInputDialog;
import org.openthos.filemanager.utils.SeafileUtils;
import org.openthos.filemanager.utils.OperateUtils;

import java.util.regex.Pattern;

public class SeafileMenuDialog extends BaseMenuDialog implements View.OnClickListener {
    private MainActivity mMainActivity;
    private TextView mTvCreate;
    private TextView mTvSync;
    private TextView mTvDesync;
    private LinearLayout mLinearLayout;
    private int mDialogWidth;
    private int mDialogHeight;
    private boolean mIsItem;
    private SeafileLibrary mLibrary;
    private int mPos;

    public SeafileMenuDialog(Context context, boolean isItem, SeafileLibrary library, int pos) {
        super(context);
        mMainActivity = (MainActivity) context;
        mIsItem = isItem;
        mLibrary = library;
        mPos = pos;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_dialog);
        initView();
        initLisener();
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {

    }

    private void initView() {
        mTvCreate = (TextView) findViewById(R.id.cloud_create);
        mTvSync = (TextView) findViewById(R.id.cloud_sync);
        mTvDesync = (TextView) findViewById(R.id.cloud_desync);
        if (!mIsItem) {
            mTvSync.setTextColor(Color.LTGRAY);
            mTvDesync.setTextColor(Color.LTGRAY);
        }
        if (!SeafileUtils.isExistsAccount()){
            mTvCreate.setTextColor(Color.LTGRAY);
        }
        mLinearLayout = (LinearLayout) findViewById(R.id.cloud_ll);
        mLinearLayout.measure(0, 0);
        mDialogWidth = mLinearLayout.getMeasuredWidth();
        mDialogHeight = mLinearLayout.getMeasuredHeight();
    }

    private void initLisener() {
        mTvCreate.setVisibility(View.GONE);
        if (!SeafileUtils.isExistsAccount()){
            return;
        }
        mTvCreate.setOnClickListener(this);
        if (mIsItem) {
            mTvSync.setOnClickListener(this);
            mTvDesync.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cloud_create:
                break;
            case R.id.cloud_sync:
                mLibrary.isSync = true;
                sync();
                break;
            case R.id.cloud_desync:
                mLibrary.isSync = false;
                desync();
                break;
        }
        dismiss();
    }

    private void sync() {
        try {
            if (mMainActivity.mISeafileService.initFinished()) {
                mMainActivity.mISeafileService.syncData();
                mMainActivity.mSeafileFragment.getList().set(mPos, mLibrary);
                mMainActivity.mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
            } else {
                Toast.makeText(mMainActivity,
                        mMainActivity.getString(R.string.toast_data_init), 0).show();
            }
        } catch (RemoteException e) {
        }
    }

    private void desync() {
        try {
            if (mMainActivity.mISeafileService.initFinished()) {
                mMainActivity.mISeafileService.desyncData();
                mMainActivity.mSeafileFragment.getList().set(mPos, mLibrary);
                mMainActivity.mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
            } else {
                Toast.makeText(mMainActivity,
                        mMainActivity.getString(R.string.toast_data_init), 0).show();
            }
        } catch (RemoteException e) {
        }
    }

    public void showDialog(int x, int y) {
        super.showDialog(x, y);
        if (!SeafileUtils.isExistsAccount()) {
            Toast.makeText(mMainActivity,
                    mMainActivity.getString(R.string.bind_openthosid), 0).show();
        }
    }
}
