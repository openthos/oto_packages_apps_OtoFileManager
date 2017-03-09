package com.openthos.filemanager.component;

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

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.BaseDialog;
import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.SeafileAccount;
import com.openthos.filemanager.bean.SeafileLibrary;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.TextInputDialog;
import com.openthos.filemanager.utils.SeafileUtils;
import com.openthos.filemanager.utils.OperateUtils;

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

    private void initView() {
        mTvCreate = (TextView) findViewById(R.id.cloud_create);
        mTvSync = (TextView) findViewById(R.id.cloud_sync);
        mTvDesync = (TextView) findViewById(R.id.cloud_desync);
        if (!mIsItem) {
            mTvSync.setTextColor(Color.LTGRAY);
            mTvDesync.setTextColor(Color.LTGRAY);
        }
        if (!SeafileUtils.isExistsAccount() || mMainActivity.isInitSeafile()
                                                                     || mMainActivity.isSeafile()){
            mTvCreate.setTextColor(Color.LTGRAY);
        }
        mLinearLayout = (LinearLayout) findViewById(R.id.cloud_ll);
        mLinearLayout.measure(0, 0);
        mDialogWidth = mLinearLayout.getMeasuredWidth();
        mDialogHeight = mLinearLayout.getMeasuredHeight();
    }

    private void initLisener() {
        mTvCreate.setVisibility(View.GONE);
        if (!SeafileUtils.isExistsAccount() || mMainActivity.isInitSeafile()
                                                                 || mMainActivity.isSeafile()){
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
                                              i < mMainActivity.mAccount.mLibrarys.size(); i++) {
                                    if (mMainActivity.mAccount.mLibrarys.get(i)
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
                                        create(text);
                                    }
                                }.start();
                                return true;
                            }
                        }
                );
                dialog.show();
                break;
            case R.id.cloud_sync:
                mMainActivity.mConsole.updateSync(mMainActivity.mAccount.mUserId,
                        mLibrary.libraryId,
                        mLibrary.libraryName,
                        SeafileUtils.SYNC);
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
                mMainActivity.mConsole.updateSync(mMainActivity.mAccount.mUserId,
                        mLibrary.libraryId,
                        mLibrary.libraryName,
                        SeafileUtils.UNSYNC);
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

    private void create(String text) {
        String id = SeafileUtils.create(text);
        int isSync = mMainActivity.mConsole.insertLibrary(
                                                      mMainActivity.mAccount.mUserId, id, text);
        SeafileLibrary seafileLibrary = new SeafileLibrary();
        seafileLibrary.libraryId = id;
        seafileLibrary.libraryName = text;
        seafileLibrary.isSync = isSync;
        mMainActivity.mAccount.mLibrarys.add(seafileLibrary);
        MainActivity.mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
        if (isSync == SeafileUtils.SYNC) {
            SeafileUtils.sync(id, new File(mMainActivity.mAccount.mFile, text)
                    .getAbsolutePath());
        }
    }

    private void sync() {
        SeafileUtils.sync((String) mLibrary.libraryId, new File(mMainActivity.mAccount.mFile,
                                                 mLibrary.libraryName).getAbsolutePath());
        mMainActivity.mAccount.mLibrarys.set(mPos, mLibrary);
        mMainActivity.mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
    }

    private void desync() {
        SeafileUtils.desync(new File(mMainActivity.mAccount.mFile,
                                                 mLibrary.libraryName).getAbsolutePath());
        mMainActivity.mAccount.mLibrarys.set(mPos, mLibrary);
        mMainActivity.mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
    }

    public void showDialog(int x, int y) {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        show();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager m = mMainActivity.getWindowManager();
        Display d = m.getDefaultDisplay();
        int dialogPadding = (int) mMainActivity.getResources().getDimension(
                                                                  R.dimen.left_margrin_text);
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
        dialogWindow.setAttributes(lp);
        if (mMainActivity.isInitSeafile()) {
           Toast.makeText(mMainActivity,
                                     mMainActivity.getString(R.string.init_seafile), 0).show();
        } else if (!SeafileUtils.isExistsAccount()) {
           Toast.makeText(mMainActivity,
                                     mMainActivity.getString(R.string.bind_openthosid), 0).show();
        } else if (mMainActivity.isSeafile()) {
           Toast.makeText(mMainActivity,
                                     mMainActivity.getString(R.string.init_seafile_data), 0).show();
        }
    }
}
