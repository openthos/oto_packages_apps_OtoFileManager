package org.openthos.filemanager.component;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.openthos.filemanager.BaseDialog;
import org.openthos.filemanager.R;
import org.openthos.filemanager.system.Constants;
import org.openthos.filemanager.system.Util;
import org.openthos.filemanager.utils.OperateUtils;
import org.openthos.filemanager.utils.SambaUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ShareDialog extends BaseDialog {
    private Context mContext;
    private String mPath;
    private CheckBox mAllowCheckBox;

    public ShareDialog(Context context) {
        super(context);
        mContext = context;
    }

    public ShareDialog(Context context, String path) {
        super(context);
        mContext = context;
        mPath = path;
    }

    public ShareDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected ShareDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_share_configuration);
        getWindow().setBackgroundDrawable(mContext.getResources().getDrawable(R.color.transparent));
        initTitle();
        initBody(new File(mPath));
        initFoot();
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {

    }

    private void initTitle() {
        ImageView titleImage = (ImageView) findViewById(R.id.title_image);
        TextView titleText = (TextView) findViewById(R.id.title_text);
        titleText.setText((mContext.getResources().getString(R.string.dialog_share_directory)));
    }

    private void initBody(File file) {
        TextView workgroup = (TextView) findViewById(R.id.workgroup);
        TextView shareName = (TextView) findViewById(R.id.tv_sharename);
        mAllowCheckBox = (CheckBox) findViewById(R.id.checkbox);
        shareName.setText(file.getName());

        workgroup.setText("WORKGROUP");
    }

    private void initFoot() {
        TextView confirm = (TextView) findViewById(R.id.confirm);
        TextView cancel = (TextView) findViewById(R.id.cancel);
        View.OnClickListener click= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.confirm:
                        // judge smb is open ?
                        String writePath = mPath;
                        if (Constants.SDCARD_PATH.contains("0")) {
                            writePath = Constants.SDCARD_PATH.replace("0", "legacy")
                                    + mPath.substring(Constants.SDCARD_PATH.length(),
                                    mPath.length());
                        }
                        boolean allowAnonymousAccess = mAllowCheckBox.isChecked();
                        BufferedWriter writer = null;
                        if (SambaUtils.SAMBA_RUNNING_FILE.exists()) {
                            // write conf to file
                            try {
                                Process pro = Runtime.getRuntime().exec(new String[] {"su", "-c"});
                                File sambaDir = new File("/data/data/samba/");
                                File sambaConfDir = new File("/data/data/samba/etc/");
                                File sambaConfFile = new File(sambaConfDir, "smb.conf");
                                writer = new BufferedWriter(
                                        new FileWriter(sambaConfFile));
                                writer.write("[global]");
                                writer.newLine();
                                writer.write("workgroup  = WORKGROUP");
                                writer.newLine();
                                writer.write("public = yes");
                                writer.newLine();
                                if (allowAnonymousAccess) {
                                    writer.write("security = user");
                                    writer.newLine();
                                    writer.write("map to guest = bad user");
                                    writer.newLine();
                                }
                                writer.write("server string = Samba Server");
                                writer.newLine();
                                writer.write("server role = standalone server");
                                writer.newLine();
                                writer.write("[share]");
                                writer.newLine();
                                writer.write("comment = share");
                                writer.newLine();
                                writer.write("path = " + writePath);
                                writer.newLine();
                                writer.write("public = yes");
                                writer.newLine();
                                writer.write("writable = yes");
                                writer.newLine();
                                writer.write("browseable = yes");
                                writer.newLine();
                                if (allowAnonymousAccess) {
                                    writer.write("guest ok = yes");
                                    writer.newLine();
                                }
                                SambaUtils.restartLocalNetworkShare();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (writer != null) {
                                    try {
                                        writer.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            android.widget.Toast.makeText(mContext, mContext.getResources()
                                    .getString(R.string.toast_open_share), 0).show();
                        }

                        dismiss();
                        break;
                    case R.id.cancel:
                        dismiss();
                        break;
                }
            }
        };
        confirm.setOnClickListener(click);
        cancel.setOnClickListener(click);
    }
}
