package com.openthos.filemanager.component;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.utils.T;

public class PopOnClickLintener implements View.OnClickListener {
    private static final String VIEW_OR_DISMISS = "view_or_dismiss";
    private static final String SETTING_POPWINDOW_TAG = "iv_setting";
    private String mMenu_tag;
    private MainActivity mMainActivity;
    private FragmentManager mManager;

    public PopOnClickLintener(String menu_tag, MainActivity mainActivity, FragmentManager manager) {
        this.mMenu_tag = menu_tag;
        this.mMainActivity = mainActivity;
        this.mManager = manager;
    }

    @Override
    public void onClick(View view) {
        if (SETTING_POPWINDOW_TAG.equals(mMenu_tag)) {
            switch (view.getId()) {
                case R.id.pop_setting_view:
                    if (mManager.getBackStackEntryCount() < 1) {
                        T.showShort(mMainActivity,
                                    mMainActivity.getString(R.string.operation_not_support));
                    }
                    sendBroadcastMessage("iv_menu", VIEW_OR_DISMISS);
                    mMainActivity.DismissPopwindow();
                    break;
                case R.id.pop_cloud_view:
                    mMainActivity.showCloudInfoDialog();
                    mMainActivity.DismissPopwindow();
                    break;
                case R.id.pop_share_toggle:
                    String chmod = "";
                    try {
                        Process pro = Runtime.getRuntime().exec(new String[] {"su", "-c"});
                        File sambaDir = new File("/data/data/samba/");
                        if (!sambaDir.exists()) {
                            deCompressSamba();
                        }

                        Runtime.getRuntime().exec(new String[] {
                                "su", "-c", "chmod 777 /data/data/samba/samba.sh"});
                        TextView tv = (TextView) view;
                        String text = (String) tv.getText();
                        if (text.equals(mMainActivity.getString(R.string.operation_open_share))) {
                            Runtime.getRuntime().exec(new String[] {
                                    "su", "-c", "/data/data/samba/samba.sh restart"});
                            tv.setText(mMainActivity.getString(R.string.operation_stop_share));
                        } else {
                            Runtime.getRuntime().exec(new String[] {
                                    "su", "-c", "/data/data/samba/samba.sh stop"});
                            tv.setText(mMainActivity.getString(R.string.operation_open_share));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMainActivity.DismissPopwindow();
                    break;
                case R.id.pop_add_users:
                    AddUsersDialog addUsersDialog = new AddUsersDialog(mMainActivity);
                    addUsersDialog.showDialog();
                    mMainActivity.DismissPopwindow();
                    break;
                default:
                    break;
            }
        }
    }

    private void deCompressSamba() {
        String outputDirectory = "/data/data/";
        File file = new File(outputDirectory);
        try {
            InputStream inputStream = mMainActivity.getAssets().open("samba.zip");
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry entry = zipInputStream.getNextEntry();
            byte[] buffer = new byte[1024 * 1024];
            int count = 0;
            while (entry != null) {
                if (entry.isDirectory()) {
                    file = new File(outputDirectory + File.separator + entry.getName());
                    file.mkdir();
                } else {
                    file = new File(outputDirectory + File.separator + entry.getName());
                    file.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(file);
                    while ((count = zipInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, count);
                    }
                    outputStream.close();
                }
                entry = zipInputStream.getNextEntry();
            }
            zipInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendBroadcastMessage(String name, String tag) {
        Intent intent = new Intent();
        if (name.equals("iv_menu")) {
            intent.setAction("com.switchmenu");
            intent.putExtra("pop_menu", tag);
        }
        mMainActivity.sendBroadcast(intent);
    }
}
