package com.openthos.filemanager.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

/**
 * Created by root on 1/18/18.
 */

public class SambaUtils {

    private void upload() {
        try {
            File f = new File("/sdcard/nihao.txt");
            SmbFile smbFile = null;
            smbFile = new SmbFile("smb://testking: @DESKTOP-M45K4DV/hello/nihao.txt");
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
            SmbFileOutputStream out = new SmbFileOutputStream(smbFile);
            int fileLength = 1024 * 1024 * 8;
            int length = 0;
            byte buffer[] = new byte[fileLength];
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.flush();
            out.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void download() {
        try {
            SmbFile smbFile = new SmbFile("smb://testking: @DESKTOP-M45K4DV/hello/nihao.txt");

            File f = new File("/sdcard/nihao.txt");
            SmbFileInputStream in = new SmbFileInputStream(smbFile);
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
            int fileLength = 1024 * 1024 * 8;
            int length = 0;
            byte buffer[] = new byte[fileLength];
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);

            }
            in.close();
            out.flush();
            out.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<String> scanNet() {
        ArrayList<String> list = new ArrayList<>();
        try {
            SmbFile mainSmb = new SmbFile("smb://");
            SmbFile[] groups = mainSmb.listFiles();
            for (int i = 0; i < groups.length; i++) {
                SmbFile workgroupSmb = new SmbFile("smb://" + groups[i].getName());
//                SmbFile workgroupSmb = new SmbFile("smb://testking: @DESKTOP-M45K4DV/");
//                SmbFile workgroupSmb = new SmbFile("smb://WORKGROUP/");
                SmbFile[] points = workgroupSmb.listFiles();
                for (int j = 0; j < points.length; j++) {
                    list.add(points[j].getName());
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static final int SAMBA_OK = 0x00000000;
    public static final int SAMBA_WRONG_ACCOUNT = 0x00000001;
    public static final int SAMBA_WRONG_NETWORK = 0x00000002;
    public static final int SAMBA_NOT_FOUND = 0x00000003;

    public static int connect(ArrayList<String> list, String acconut, String password, String path) {
        list.clear();
        try {
            SmbFile point = new SmbFile("smb://" + acconut + ":" + password + "@" + path);
            Log.i("wwww smb", point.getPath());
            SmbFile[] files = point.listFiles();
            for (int i = 0; i < files.length; i++) {
                list.add(files[i].getName());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
            if (e.getLocalizedMessage().contains("unknown user name or bad password")) {
                return SAMBA_WRONG_ACCOUNT;
            } else if (e.getLocalizedMessage().contains("Failed to connect to server")) {
                return SAMBA_WRONG_NETWORK;
            } else if (e.getLocalizedMessage().contains("The system cannot find the file specified")){
                return SAMBA_NOT_FOUND;
            }
        }
        return SAMBA_OK;
    }

    private void listFile() {
        String path = "smb://testking: @192.168.0.60/hello/";
        SmbFile sFile;
        try {
            sFile = new SmbFile(path);
            for (int i = 0; i < sFile.listFiles().length; i++) {
                Log.i("wwww", sFile.listFiles()[i].getName());
                Log.i("wwww", sFile.listFiles()[i].length() + "");
                Log.i("wwww", sFile.listFiles()[i].getPath());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
    }

}
