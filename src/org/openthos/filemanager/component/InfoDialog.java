package org.openthos.filemanager.component;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.openthos.filemanager.R;
import org.openthos.filemanager.BaseMenuDialog;

/**
 * Created by xu on 2016/12/06.
 */
public class InfoDialog extends BaseMenuDialog {
    private Activity mContext;
    private TextView mTextMessage;
    private TextView mTextTitle;
    private static InfoDialog dialog = null;
    private GifView mGif;
    private int mRawId;

    public InfoDialog(Activity context) {
        super(context);
        mContext = context;
    }

    public static InfoDialog getInstance(Activity activity) {
        if (dialog == null) {
            return new InfoDialog(activity);
        } else {
            return dialog;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = View.inflate(mContext, R.layout.dialog_copy_info, null);
        setContentView(v);
        mTextMessage = (TextView) v.findViewById(R.id.text_message);
        mTextTitle = (TextView) v.findViewById(R.id.text_title);
        mGif = (GifView) v.findViewById(R.id.gif);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {

    }

    public void showDialog(int rawId) {
        mRawId = rawId;
        if (mGif == null) {
            mGif = (GifView) View.inflate(mContext, R.layout.dialog_copy_info, null)
                                     .findViewById(R.id.gif);
        }
        mGif.setMovieResource(mRawId);
        showDialog();
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
