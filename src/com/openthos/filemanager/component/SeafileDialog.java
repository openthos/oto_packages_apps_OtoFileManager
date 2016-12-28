package com.openthos.filemanager.component;

import android.app.Activity;
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
import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.SeafileAccount;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.utils.SeafileUtils;

import java.util.HashMap;

public class SeafileDialog extends Dialog implements View.OnClickListener {
    private MainActivity mMainActivity;
    private TextView mTvCreate;
    private TextView mTvSync;
    private TextView mTvDesync;
    private LinearLayout mLinearLayout;
    private int mDialogWidth;
    private int mDialogHeight;
    private boolean mIsItem;
    private HashMap<String, String> mLibrary;


    public SeafileDialog(Context context, boolean isItem, HashMap<String, String> library) {
        super(context);
        mMainActivity = (MainActivity) context;
        mIsItem = isItem;
        mLibrary = library;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_dialog);
        initView();
        initLisener();
    }

    private void initLisener() {
        mTvCreate.setOnClickListener(this);
        if (mIsItem) {
            mTvSync.setOnClickListener(this);
            mTvDesync.setOnClickListener(this);
        }
    }

    private void initView() {
        mTvCreate = (TextView) findViewById(R.id.cloud_create);
        mTvSync = (TextView) findViewById(R.id.cloud_sync);
        mTvDesync = (TextView) findViewById(R.id.cloud_desync);
        if (!mIsItem) {
            mTvSync.setTextColor(Color.LTGRAY);
            mTvDesync.setTextColor(Color.LTGRAY);
        }
        mLinearLayout = (LinearLayout) findViewById(R.id.cloud_ll);
        mLinearLayout.measure(0, 0);
        mDialogWidth = mLinearLayout.getMeasuredWidth();
        mDialogHeight = mLinearLayout.getMeasuredHeight();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cloud_create:
                break;
            case R.id.cloud_sync:
                mMainActivity.mConsole.updateSync(mMainActivity.mAccount.mUserId,
                        mLibrary.get(SeafileAccount.LIBRARY_ID),
                        mLibrary.get(SeafileAccount.LIBRARY_NAME),
                        SeafileUtils.SYNC);
                break;
            case R.id.cloud_desync:
                mMainActivity.mConsole.updateSync(mMainActivity.mAccount.mUserId,
                        mLibrary.get(SeafileAccount.LIBRARY_ID),
                        mLibrary.get(SeafileAccount.LIBRARY_NAME),
                        SeafileUtils.UNSYNC);
                break;
        }
        dismiss();
    }

    public void showDialog(int x, int y) {
        show();
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
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
    }
}
