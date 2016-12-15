package com.openthos.filemanager.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SeafileUtils{
    public static final String SEAFILE_CONFIGPATH = "/data/";
    public static final String SEAFILE_CONFIGNAME = "seafile-config";
    public static final String SEAFILE_NETNAME = ".ccnet";
    public static final String SEAFILE_BASE_COMMAND ="seaf-cli";
    public static final String SEAFILE_BASE_COMMAND1 ="./data/sea/proot.sh";
    public static final String SEAFILE_BASE_COMMAND2 ="-b";
    public static final String SEAFILE_BASE_COMMAND3 ="/data/data/test:/data";
    public static final String SEAFILE_BASE_URL = "https://dev.openthos.org/";
    public static final int SEAFILE_ID_LENGTH = 36;

    public static void main(String[] args) {
        //init();
        //start();
        //stop();
        //System.out.println(listRemote("potatomagic@163.com", "kiss5potato"));
        //System.out.println(create("zhaoliu", "potatomagic@163.com", "kiss5potato"));
        //download("e700ed4c-bda8-4c20-af8c-28fa6c4923f3", SEAFILE_CONFIGPATH + "sea",
        //                 "potatomagic@163.com", "kiss5potato");
        //sync("e700ed4c-bda8-4c20-af8c-28fa6c4923f3", SEAFILE_CONFIGPATH + "sea/zhangsan",
        //                 "potatomagic@163.com", "kiss5potato");
        //status();
        //desync(SEAFILE_CONFIGPATH + "sea/zhangsan");
    }

    public static void desync(String fileName) {
        String arg0 = "desync";
        String arg1 = "-d";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        try {
            pro = runtime.exec(new String[]{SEAFILE_BASE_COMMAND1, SEAFILE_BASE_COMMAND2,
                               SEAFILE_BASE_COMMAND3, SEAFILE_BASE_COMMAND, arg0, arg1, fileName});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
               System.out.println(line);
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

    public static void status() {
        String arg0 = "status";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        try {
            pro = runtime.exec(new String[]{SEAFILE_BASE_COMMAND1, SEAFILE_BASE_COMMAND2,
                                     SEAFILE_BASE_COMMAND3, SEAFILE_BASE_COMMAND, arg0});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
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

    public static void sync(String id, String fileName, String userName, String userPassword) {
        String arg0 = "sync";
        String arg1 = "-l";
        String arg2 = "-s";
        String arg3 = "-d";
        String arg4 = "-u";
        String arg5 = "-p";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        try {
            pro = runtime.exec(new String[]{SEAFILE_BASE_COMMAND1, SEAFILE_BASE_COMMAND2,
                     SEAFILE_BASE_COMMAND3, SEAFILE_BASE_COMMAND, arg0, arg1, id,
                     arg2, SEAFILE_BASE_URL, arg3, fileName, arg4, userName, arg5, userPassword});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
               System.out.println(line);
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

    public static void download(String id, String fileName, String userName, String userPassword) {
        String arg0 = "download";
        String arg1 = "-l";
        String arg2 = "-s";
        String arg3 = "-d";
        String arg4 = "-u";
        String arg5 = "-p";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        try {
            pro = runtime.exec(new String[]{SEAFILE_BASE_COMMAND1, SEAFILE_BASE_COMMAND2,
                     SEAFILE_BASE_COMMAND3, SEAFILE_BASE_COMMAND, arg0, arg1, id, arg2,
                     SEAFILE_BASE_URL, arg3, fileName, arg4, userName, arg5, userPassword});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
               System.out.println(line);
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

    public static String create(String fileName, String userName, String userPassword) {
        String arg0 = "create";
        String arg1 = "-n";
        String arg2 = "-s";
        String arg3 = "-u";
        String arg4 = "-p";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        String id = "";
        try {
            pro = runtime.exec(new String[]{SEAFILE_BASE_COMMAND1, SEAFILE_BASE_COMMAND2,
                               SEAFILE_BASE_COMMAND3, SEAFILE_BASE_COMMAND, arg0, arg1, fileName,
                               arg2, SEAFILE_BASE_URL, arg3, userName, arg4, userPassword});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                id = line;
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
        return id;
    }

    public static String listRemote(String userName, String userPassword) {
        String arg0 = "list-remote";
        String arg1 = "-s";
        String arg2 = "-u";
        String arg3 = "-p";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        StringBuffer sb = new StringBuffer();
        try {
            pro = runtime.exec(new String[]{SEAFILE_BASE_COMMAND1, SEAFILE_BASE_COMMAND2,
                               SEAFILE_BASE_COMMAND3, SEAFILE_BASE_COMMAND, arg0, arg1,
                               SEAFILE_BASE_URL, arg2, userName, arg3, userPassword});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            sb.append("[");
            while ((line = in.readLine()) != null) {
                if(line.length() > SEAFILE_ID_LENGTH) {
                   String id = line.substring(line.length() - SEAFILE_ID_LENGTH);
                   sb.append("{\"id\":\"" + id);
                   String name = line.replace(" " + id, "");
                   sb.append("\",\"name\":\""+ name +"\"},");
                }
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append("]");
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
        return sb.toString();
    }

    public static void stop() {
        String arg0 = "stop";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        try {
            pro = runtime.exec(new String[]{SEAFILE_BASE_COMMAND1, SEAFILE_BASE_COMMAND2,
                               SEAFILE_BASE_COMMAND3, SEAFILE_BASE_COMMAND, arg0});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void start() {
        String arg0 = "start";
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        try {
            pro = runtime.exec(new String[]{SEAFILE_BASE_COMMAND1, SEAFILE_BASE_COMMAND2,
                               SEAFILE_BASE_COMMAND3, SEAFILE_BASE_COMMAND, arg0});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
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

    public static void init() {
        String arg0 = "init";
        String arg1 = "-d";
        File net = new File(SEAFILE_CONFIGPATH, SEAFILE_NETNAME);
        File config = new File(SEAFILE_CONFIGPATH, SEAFILE_CONFIGNAME);
        if (net.exists()){
            delete(net);
        }
        if (!config.exists()) {
            config.mkdir();
        }
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        try {
            pro = runtime.exec(new String[]{SEAFILE_BASE_COMMAND1, SEAFILE_BASE_COMMAND2,
                               SEAFILE_BASE_COMMAND3, SEAFILE_BASE_COMMAND, arg0, arg1,
                               SEAFILE_CONFIGPATH + SEAFILE_CONFIGNAME});
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
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

    public static void delete(File file) {
        if (file.exists()) {
            String command = "rm";
            String arg = "";
            if (file.isFile()) {
                arg = "-v";
            } else if (file.isDirectory()) {
               arg = "-rv";
            }
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(new String[]{command, arg, file.getAbsolutePath()});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
