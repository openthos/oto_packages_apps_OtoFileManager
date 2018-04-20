package org.openthos.filemanager.component;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.RemoteException;

import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.BaseDialog;
import org.openthos.filemanager.R;
import org.openthos.filemanager.bean.SeafileLibrary;
import org.openthos.filemanager.system.Constants;
import org.openthos.filemanager.system.TextInputDialog;
import org.openthos.filemanager.utils.SeafileUtils;
import org.openthos.filemanager.utils.OperateUtils;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SeafileDialog extends BaseDialog implements View.OnClickListener {
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

    public SeafileDialog(Context context, boolean isItem, SeafileLibrary library, int pos) {
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
                TextInputDialog dialog = new TextInputDialog(mMainActivity,
                        mMainActivity.getString(R.string.operation_create_folder),
                        mMainActivity.getString(R.string.operation_create_folder_message),
                        "My Library",
                        new TextInputDialog.OnFinishListener() {
                            @Override
                            public boolean onFinish(final String text) {
                                for (int i = 0;
                                              i < mMainActivity.mLibrarys.size(); i++) {
                                    if (mMainActivity.mLibrarys.get(i)
                                                                       .libraryName.equals(text)) {
                                        OperateUtils.showConfirmAlertDialog(mMainActivity,
                                                                         R.string.fail_seafile_name);
                                        return false;
                                    }
                                }
                                if (!Pattern.compile("[0-9a-zA-Z ]+").matcher(text).matches()) {
                                    OperateUtils.showConfirmAlertDialog(mMainActivity,
                                                                R.string.fail_seafile_name_by_error);
                                    return false;
                                }
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        //create(text);
                                    }
                                }.start();
                                return true;
                            }
                        }
                );
                dialog.show();
                break;
            case R.id.cloud_sync:
                mLibrary.isSync = SeafileUtils.SYNC;
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        sync();
                    }
                }.start();
                break;
            case R.id.cloud_desync:
                mLibrary.isSync = SeafileUtils.UNSYNC;
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        desync();
                    }
                }.start();
                break;
        }
        dismiss();
    }

//    private void create(String text) {
//        try {
//            String id = mMainActivity.mISeafileService.create(text);
//            int isSync = mMainActivity.mISeafileService.insertLibrary(mMainActivity.mUserId, id, text);
//            SeafileLibrary seafileLibrary = new SeafileLibrary();
//            seafileLibrary.libraryId = id;
//            seafileLibrary.libraryName = text;
//            seafileLibrary.isSync = isSync;
//            mMainActivity.mLibrarys.add(seafileLibrary);
//            mMainActivity.mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
//            if (isSync == SeafileUtils.SYNC) {
//                mMainActivity.mISeafileService.sync(id, new File(mMainActivity.mFile, text)
//                        .getAbsolutePath());
//            }
//        } catch (RemoteException e) {
//        }
//    }

    private void sync() {
        try {
            mMainActivity.mISeafileService.sync((String) mLibrary.libraryId, mLibrary.libraryName,
                    "/sdcard/seafile/" + SeafileUtils.mUserId + "/" +  mLibrary.libraryName);
            mMainActivity.mLibrarys.set(mPos, mLibrary);
            mMainActivity.mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
        } catch (RemoteException e) {
        }
    }

    private void desync() {
        try {
            mMainActivity.mISeafileService.desync((String) mLibrary.libraryId, mLibrary.libraryName,
                    SeafileUtils.SEAFILE_DATA_PATH + "/" + SeafileUtils.mUserId
                    + "/" + mLibrary.libraryName);
            mMainActivity.mLibrarys.set(mPos, mLibrary);
            mMainActivity.mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
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
