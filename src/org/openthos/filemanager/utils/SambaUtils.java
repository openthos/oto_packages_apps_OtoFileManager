package org.openthos.filemanager.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

/**
 * Created by root on 1/18/18.
 */

public class SambaUtils {

    public final static File BASE_DIRECTORY
            = new File(Environment.getExternalStorageDirectory(), "samba");
    public static final File SAMBA_RUNNING_FILE = new File("/data/data/samba/var/run/smbd.pid");

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

    public static boolean download(String acconut, String password, String path) {
        try {
            String tempPath = "smb://" + acconut + ":" + password + "@" + path;
            tempPath = tempPath.replace("smb://:@", "smb://");
            SmbFile smbFile = new SmbFile(tempPath);
            File f = new File(BASE_DIRECTORY, path);
            File parent = new File(f.getParent());
            if (!parent.exists()) {
                parent.mkdirs();
            }
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
            return true;
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
        return false;
    }

    public static ArrayList<String> scanNet2() {
        ArrayList<String> list = new ArrayList<>();
        try {
            SmbFile mainSmb = new SmbFile("smb://");
            SmbFile[] groups = mainSmb.listFiles();
            for (int i = 0; i < groups.length; i++) {
                SmbFile workgroupSmb = new SmbFile("smb://" + groups[i].getName());
                SmbFile[] points = workgroupSmb.listFiles();
                for (int j = 0; j < points.length; j++) {
                    list.add(points[j].getName());
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
//            if (e.getLocalizedMessage().contains("MSBROWSER")) {
//                return list;
//            }
        }
        return list;
    }

    private static ArrayList<String> sambaPoints = new ArrayList<>();

    public static ArrayList<String> scanNet() {
        String ip = getLocAddress();
        String newip = "";
        ArrayList<UDPThread> udpLists = new ArrayList();
        ArrayList<SambaThread> sambaLists = new ArrayList<>();
        ArrayList<String> points = new ArrayList<>();
        sambaPoints.clear();
        if (!ip.equals("")) {
            String ipseg = ip.substring(0, ip.lastIndexOf(".") + 1);
            for (int i = 2; i < 255; i++) {
                newip = ipseg + String.valueOf(i);
                if (newip.equals(ip)) continue;
                udpLists.add(new UDPThread(newip));
            }
            for (UDPThread thread : udpLists)
                thread.start();
            while (!udpLists.isEmpty()) {
                for (UDPThread ut : udpLists) {
                    if (!ut.isAlive()) {
                        udpLists.remove(ut);
                        break;
                    }
                }
            }
            Process pro;
            BufferedReader in = null;
            try {
                pro = Runtime.getRuntime().exec(new String[]{"su", "-c", "more /proc/net/arp"});
                in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.contains("00:00:00:00:00:00") || line.contains("address")) {
                        continue;
                    }
                    //192.168.0.2      0x1         0x2         a0:63:91:d4:f9:87     *        wlan0
                    points.add(line.split("\\s+")[0]);
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
        }

        for (String point : points)
            sambaLists.add(new SambaThread(point));
        for (SambaThread thread : sambaLists)
            thread.start();
        while (!sambaLists.isEmpty()) {
            for (SambaThread thread : sambaLists) {
                if (!thread.isAlive()) {
                    sambaLists.remove(thread);
                    break;
                }
            }
        }
        return sambaPoints;
    }

    private static String getLocAddress() {
        String ipaddress = "";

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface networks = en.nextElement();
                Enumeration<InetAddress> address = networks.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!ip.isLoopbackAddress() && ip instanceof Inet4Address) {
                        ipaddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipaddress;
    }

    public static final int SAMBA_INIT = 0x10000001;
    public static final int SAMBA_OK = 0x00000000;
    public static final int SAMBA_WRONG_ACCOUNT = 0x00000001;
    public static final int SAMBA_WRONG_NETWORK = 0x00000002;
    public static final int SAMBA_NOT_FOUND = 0x00000003;
    public static final int SAMBA_ACCESS_DENIED = 0x00000004;

    public static int connect(ArrayList<String> list, String acconut, String password, String path) {
        list.clear();
        try {
            String tempPath = "smb://" + acconut + ":" + password + "@" + path;
            tempPath = tempPath.replace("smb://:@", "smb://");
            SmbFile point = new SmbFile(tempPath);
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
            } else if (e.getLocalizedMessage().contains("Access is denied")){
                return SAMBA_ACCESS_DENIED;
            }
        }
        return SAMBA_OK;
    }


    private static class SambaThread extends Thread {
        private String target_ip;
        private int result = SAMBA_INIT;

        public SambaThread(String target_ip) {
            this.target_ip = target_ip;
        }

        @Override
        public synchronized void run() {
            try {
                String tempPath = "smb://" + target_ip + "/";
                SmbFile point = new SmbFile(tempPath);
                SmbFile[] files = point.listFiles();
                for (int i = 0; i < files.length; i++) {
//                    Log.i("wwww", files[i].getName());
                }
                result = SAMBA_OK;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SmbException e) {
                e.printStackTrace();
                if (e.getLocalizedMessage().contains("Logon failure: unknown user name or bad password")) {
                    result = SAMBA_WRONG_ACCOUNT;
                } else if (e.getLocalizedMessage().contains("Failed to connect to server")) {
                    result = SAMBA_WRONG_NETWORK;
                } else if (e.getLocalizedMessage().contains("The system cannot find the file specified")) {
                    result = SAMBA_WRONG_NETWORK;
                } else if (e.getLocalizedMessage().contains("recvfrom failed: ECONNRESET (Connection reset by peer)")) {
                    result = SAMBA_WRONG_NETWORK;
                } else if (e.getLocalizedMessage().contains("Connection timeout")) {
                    result = SAMBA_WRONG_NETWORK;
                }
            }
            if (result == SAMBA_OK || result == SAMBA_WRONG_ACCOUNT) {
                sambaPoints.add(target_ip + "/");
            }
        }
    }


    private static class UDPThread extends Thread {
        private String target_ip = "";

        public final byte[] NBREQ = {(byte) 0x82, (byte) 0x28, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x1, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x20, (byte) 0x43, (byte) 0x4B, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x0, (byte) 0x0, (byte) 0x21,
                (byte) 0x0, (byte) 0x1};

        public static final short NBUDPP = 137;

        public UDPThread(String target_ip) {
            this.target_ip = target_ip;
        }

        @Override
        public synchronized void run() {
            if (target_ip == null || target_ip.equals("")) return;
            DatagramSocket socket = null;
            InetAddress address = null;
            DatagramPacket packet = null;
            try {
                address = InetAddress.getByName(target_ip);
                packet = new DatagramPacket(NBREQ, NBREQ.length, address, NBUDPP);
                socket = new DatagramSocket();
                socket.setSoTimeout(200);
                socket.send(packet);
                socket.close();
            } catch (SocketException se) {
            } catch (UnknownHostException e) {
            } catch (IOException e) {
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }
    }

    public static void initSambaClientEnvironment() {
        Process pro;
        BufferedReader in = null;
        boolean isMounted = false;
        String path = "/storage/samba ";
        try {
            pro = Runtime.getRuntime().exec(new String[]{"su", "-c", "mount"});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains(path)) {
                    isMounted = true;
                    break;
                }
            }
            in.close();
            in = null;
            if (!isMounted) {
                File f = new File("/sdcard/samba");
                if (f.exists()) {
                    pro = Runtime.getRuntime().exec(new String[]{"rm", "-r", f.getAbsolutePath()});
                    in = new BufferedReader(new InputStreamReader(pro.getErrorStream()));
                    while ((line = in.readLine()) != null) {
                    }
                }
                f.mkdir();
                pro = Runtime.getRuntime().exec(new String[]{"su", "-c",
                        "busybox mount --bind " + f.getAbsolutePath() + " " + path.trim()});
                in = new BufferedReader(new InputStreamReader(pro.getErrorStream()));
                while ((line = in.readLine()) != null) {
                    Log.i("OtoFileManager", line);
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
    }

    public static void initSambaPermission() {
        try {
            Runtime.getRuntime().exec(new String[] {"su", "-c", "chmod 777 "
                    + "/data/data/samba /data/data/samba/samba.sh "
                    + "/data/data/samba/smbpasswd.sh /data/data/samba/pdbedit.sh "
                    + "/data/data/samba/var /data/data/samba/var/run "});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addUserAndPasswd(String userName, String pwd) {
        Process pro;
        try {
            pro = Runtime.getRuntime().exec(new String[] {
                    "su", "-c", "/data/data/samba/smbpasswd.sh" + " " + userName + " " + pwd});
            pro.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllUsers() {
        List<String> allUserList = new ArrayList<>();
        Process pro;
        BufferedReader in = null;
        try {
            pro = Runtime.getRuntime().exec(new String[] {
                    "su", "-c", "/data/data/samba/pdbedit.sh -L"});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("/data/data/samba")) {
                    continue;
                }
                allUserList.add(line.split(":")[0]);
            }
            pro.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
        return allUserList;
    }

    public static void removeUser(String userName) {
        Process pro;
        try {
            pro = Runtime.getRuntime().exec(new String[] {
                    "su", "-c", "/data/data/samba/pdbedit.sh" + " -x " + userName});
            pro.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void modifyPasswd(String userName, String pwd) {
        Process pro;
        try {
            pro = Runtime.getRuntime().exec(new String[] {
                    "su", "-c", "/data/data/samba/smbpasswd.sh" + " " + userName + " " + pwd});
            pro.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void startLocalNetworkShare() {
        try {
            Runtime.getRuntime().exec(new String[] {
                    "su", "-c", "/data/data/samba/samba.sh start"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopLocalNetworkShare() {
        try {
            Runtime.getRuntime().exec(new String[] {
                    "su", "-c", "/data/data/samba/samba.sh stop"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void restartLocalNetworkShare() {
        try {
            Runtime.getRuntime().exec(new String[] {
                    "su", "-c", "/data/data/samba/samba.sh restart"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
