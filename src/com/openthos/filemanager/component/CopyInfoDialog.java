package com.openthos.filemanager.component;

import android.app.Activity;
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
import android.widget.Toast;

import com.openthos.filemanager.R;
import com.openthos.filemanager.BaseDialog;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by xu on 2016/12/06.
 */
public class CopyInfoDialog extends BaseDialog {
    private Activity mContext;
    private TextView mTextMessage;
    private TextView mTextTitle;
    private static CopyInfoDialog dialog = null;
    private GifView mGif;
    private int mRawId;

    public CopyInfoDialog(Activity context) {
        super(context);
        mContext = context;
    }

    public static CopyInfoDialog getInstance(Activity activity) {
        if (dialog == null) {
            return new CopyInfoDialog(activity);
        } else {
            return dialog;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = View.inflate(mContext, R.layout.dialog_copy_info, null);
        setContentView(v);
        mTextMessage = (TextView) v.findViewById(R.id.text_message);
        mTextTitle = (TextView) v.findViewById(R.id.text_title);
        mGif = (GifView) v.findViewById(R.id.gif);
    }

    public void showDialog(int rawId) {
        mRawId = rawId;
        if (mGif == null) {
            mGif = (GifView) View.inflate(mContext, R.layout.dialog_copy_info, null)
                                     .findViewById(R.id.gif);
        }
        mGif.setMovieResource(mRawId);
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        show();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
    }

    public void changeMsg(final String s) {
        if (mGif == null) {
            mGif = (GifView) View.inflate(mContext, R.layout.dialog_copy_info, null)
                                     .findViewById(R.id.gif);
        }
        mGif.setMovieResource(mRawId);
        if ("main".equals(Thread.currentThread().getName())) {
            mTextMessage.setText(s);
        } else {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextMessage.setText(s);
                }
            });
        }
    }

    public void changeTitle(final String s) {
        if (mGif == null) {
            mGif = (GifView) View.inflate(mContext, R.layout.dialog_copy_info, null)
                                     .findViewById(R.id.gif);
        }
        mGif.setMovieResource(mRawId);
        mTextTitle.setText(s);
    }
}
