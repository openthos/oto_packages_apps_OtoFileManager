package com.openthos.filemanager.component;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.openthos.filemanager.R;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.utils.SeafileUtils;

public class CloudDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private TextView mTvCreate;
    private TextView mTvSync;
    private TextView mTvDesync;
    private LinearLayout mLinearLayout;
    private int mDialogWidth;
    private int mDialogHeight;

    public CloudDialog(Context context) {
        super(context);
        mContext = context;
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
        mTvSync.setOnClickListener(this);
        mTvDesync.setOnClickListener(this);

    }

    private void initView() {
        mTvCreate = (TextView) findViewById(R.id.cloud_create);
        mTvSync = (TextView) findViewById(R.id.cloud_sync);
        mTvDesync = (TextView) findViewById(R.id.cloud_desync);
        mLinearLayout = (LinearLayout) findViewById(R.id.cloud_ll);
        mLinearLayout.measure(0, 0);
        mDialogWidth = mLinearLayout.getMeasuredWidth();
        mDialogHeight = mLinearLayout.getMeasuredHeight();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cloud_create:
                //SeafileUtils.create("fileName", mId, mPassword);
                Toast.makeText(mContext, "Create", Toast.LENGTH_SHORT).show();
                break;
            case R.id.cloud_sync:
                //SeafileUtils.sync(id, fileName, userName, userPassword);
                Toast.makeText(mContext, "Sync", Toast.LENGTH_SHORT).show();
                break;
            case R.id.cloud_desync:
                //SeafileUtils.desync(fileName);
                Toast.makeText(mContext, "Sync", Toast.LENGTH_SHORT).show();
                break;
        }
        dismiss();
    }

    public void showDialog(int x, int y) {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        show();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager m = ((Activity)mContext ).getWindowManager();
        Display d = m.getDefaultDisplay();
        int dialogPadding = (int) mContext.getResources().getDimension(R.dimen.left_margrin_text);
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
