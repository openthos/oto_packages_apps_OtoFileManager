package org.openthos.filemanager.component;

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
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.R;
import org.openthos.filemanager.utils.T;
import org.openthos.filemanager.utils.SambaUtils;
import org.openthos.filemanager.system.Constants;

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

                        TextView tv = (TextView) view;
                        String text = (String) tv.getText();
                        if (text.equals(mMainActivity.getString(R.string.operation_open_share))) {
                            SambaUtils.restartLocalNetworkShare();
                            tv.setText(mMainActivity.getString(R.string.operation_stop_share));
                        } else {
                            SambaUtils.stopLocalNetworkShare();
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
        BufferedReader in = null;
        ZipInputStream zipInputStream = null;
        try {
            String outputDirectory = "/data/data/";
            Process pro = Runtime.getRuntime().exec(
                    new String[] {"su", "-c", "busybox mkdir -m 777 /data/data/samba"});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
            }
            File file = new File(outputDirectory);
            InputStream inputStream = mMainActivity.getAssets().open("samba.zip");
            zipInputStream = new ZipInputStream(inputStream);
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
            SambaUtils.initSambaPermission();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
