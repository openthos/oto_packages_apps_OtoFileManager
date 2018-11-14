package org.openthos.filemanager.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.openthos.filemanager.bean.Disk;
import org.openthos.filemanager.bean.Volume;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.utils.SambaUtils;
/**
 * Created by Wang Zhixu on 4/18/17.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        autoMount(context);
        initSamba();
    }

    private void autoMount(Context context) {
        String result = refreshAutoMountData(context);
        if (!result.equals("[]")) {
            context.getSharedPreferences(Constants.MAIN_SP, Context.MODE_PRIVATE)
                    .edit().putString(Constants.SP_AUTOMOUNT, result).commit();
        }
    }

    private void initSamba() {
        //samba client
        new Thread(){
            @Override
            public void run() {
                super.run();
                SambaUtils.initSambaClientEnvironment();
            }
        }.start();
        // samba server
        // judge smb is open last shutdown ?
        if (SambaUtils.SAMBA_RUNNING_FILE.exists()) {
            SambaUtils.startLocalNetworkShare();
        }
    }

    public static String refreshAutoMountData(Context context) {
        ArrayList<Volume> mVolumes = new ArrayList<>();
        ArrayList<Disk> mDisks = new ArrayList<>();
        Process pro;
        BufferedReader in = null;
        try {
            String[] commands = new String[]{"ls", "/dev/block/"};
            pro = Runtime.getRuntime().exec(commands);
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains("sd")) {
                    if (line.length() > 3) {
                        Volume v = new Volume();
                        v.setBlock(line);
                        mVolumes.add(v);
                    } else {
                        Disk d = new Disk();
                        d.setBlock(line);
                        mDisks.add(d);
                    }
                } else if (line.contains("nvme0n")) {
                    if (line.length() > 7) {
                        Volume v = new Volume();
                        v.setBlock(line);
                        mVolumes.add(v);
                    } else {
                        Disk d = new Disk();
                        d.setBlock(line);
                        mDisks.add(d);
                    }
                }
            }
            commands = new String[]{"su", "-c", "mount"};
            pro = Runtime.getRuntime().exec(commands);
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            while ((line = in.readLine()) != null) {
                for (Volume v : mVolumes) {
                    if (line.contains(v.getBlock())) {
                        mVolumes.remove(v);
                        break;
                    }
                }
            }
            for (Disk d : mDisks) {
                commands = new String[]{"su", "-c", "sgdisk --print /dev/block/" + d.getBlock()};
                pro = Runtime.getRuntime().exec(commands);
                in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                String result = d.getBlock();
                if (d.getBlock().startsWith("nvme")) {
                    result = result + "p";
                }
                boolean isPrint = false;
                while ((line = in.readLine()) != null) {
                    if (line.contains("Number")) {
                        isPrint = true;
                    } else if (isPrint) {
                        String lower = line.toLowerCase();
                        if (!(lower.contains("swap")
                                || lower.contains("efi")
                                || lower.contains("microsoft reserved")
                                || line.substring(50, 56).equals("EF00"))) {
                            String temp = result + line.trim().substring(0,4).trim();
                            for (Volume v : mVolumes) {
                                if (v.getBlock().equals(temp)) {
                                    v.setLength(line.substring(38, 50).trim());
                                }
                            }
                        }
                    }
                }
            }
            commands = new String[]{"su", "-c", "blkid"};
            pro = Runtime.getRuntime().exec(commands);
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            while ((line = in.readLine()) != null) {
                if (!(line.contains("swap") || line.contains("EFI") || line.contains("Recovery"))) {
                    String[] values = line.substring(11).split(" ");
                    for (Volume v : mVolumes) {
                        if (values[0].contains(v.getBlock())) {
                            for (String value : values) {
                                if (value.contains("TYPE=\"")) {
                                    v.setType(value.replace("TYPE=\"", "").replace("\"", ""));
                                } else if (value.contains("LABEL=\"")) {
                                    v.setName(value.replace("LABEL=\"", "").replace("\"", ""));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ArrayList<Volume> tmpmVolumes = new ArrayList<>();
        for (Volume v : mVolumes) {
            if (!TextUtils.isEmpty(v.getLength()) && !TextUtils.isEmpty(v.getType())) {
                tmpmVolumes.add(v);
                if (TextUtils.isEmpty(v.getName())) {
                    v.setName(v.getType().toUpperCase() + "/" + v.getBlock());
                }
            }
        }
        mVolumes.clear();
        mVolumes.addAll(tmpmVolumes);
        String result = "[";
        for (Volume v : mVolumes) {
            result = result + "{\"block\":\"" + v.getBlock() + "\",\"ismount\":"
                    + "false" + ",\"type\":\"" + v.getType() + "\",\"name\":\"" + v.getName() + "\"},";
        }
        if (result.substring(result.length() - 1).equals(",")) {
            result = result.substring(0, result.length() - 1);
        }
        result = result + "]";
        return result;
    }
}
