package com.openthos.filemanager.component;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.content.pm.ApplicationInfo;

import com.openthos.filemanager.R;
import com.openthos.filemanager.system.Constants;

import java.io.File;

public class TextSelectDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private String filePath;

    public TextSelectDialog(Context mContext, int dialog, String filePath) {
        super(mContext, dialog);
        this.context = mContext;
        this.filePath = filePath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_select_dialog);
        initView();
    }

    private void initView() {
        TextView dialog_type_text = (TextView) findViewById(R.id.dialog_type_text);
        TextView dialog_type_audio = (TextView) findViewById(R.id.dialog_type_audio);
        TextView dialog_type_video = (TextView) findViewById(R.id.dialog_type_video);
        TextView dialog_type_image = (TextView) findViewById(R.id.dialog_type_image);
        dialog_type_text.setOnClickListener(this);
        dialog_type_audio.setOnClickListener(this);
        dialog_type_video.setOnClickListener(this);
        dialog_type_image.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String selectType = "*/*";
        switch (view.getId()) {

            case R.id.dialog_type_text:
                selectType = "text/plain";
                TextSelectDialog.this.dismiss();
                break;
            case R.id.dialog_type_audio:
                selectType = "audio/*";
                TextSelectDialog.this.dismiss();
                break;
            case R.id.dialog_type_video:
                selectType = "video/*";
                TextSelectDialog.this.dismiss();
                break;
            case R.id.dialog_type_image:
                selectType = "image/*";
                TextSelectDialog.this.dismiss();
                break;
            default:
                break;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), selectType);
        intent.putExtra(Constants.PACKAGENAME_TAG, Constants.APPNAME_OTO_LAUNCHER);
        context.startActivity(intent);
    }

    public void showTextDialog(int x, int y) {
        show();
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        int dialogPadding = (int) context.getResources().getDimension(R.dimen.left_margrin_text);
        lp.x = x + dialogPadding * 2;
        lp.y = y + dialogPadding * 2;
        dialogWindow.setAttributes(lp);
    }
}
