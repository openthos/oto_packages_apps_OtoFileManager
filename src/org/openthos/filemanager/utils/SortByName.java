package org.openthos.filemanager.utils;

import java.io.File;

public class SortByName {
    File[] tempFiles;
    File[] sortedFiles;

    public File[] sort(File[] files) {
        if (null == files || files.length == 0)
            return null;

        int i = 0, j = 0;
        tempFiles = noHideFile(files);
        int length = tempFiles.length;
        sortedFiles = new File[length];
        File sortedName, tempName;

        sortedFiles[0] = tempFiles[0];
        for (i = 0; i < length; i++) {
            tempName = tempFiles[i];
            for (j = 0; j < i; j++) {
                sortedName = sortedFiles[j];
                if (!compareStr(sortedName.getName(), tempName.getName())) {
                    continue;
                } else
                    break;
            }
            for (int flag = i; flag > j; flag--) {
                sortedFiles[flag] = sortedFiles[flag - 1];
            }

            sortedFiles[j] = tempFiles[i];
        }
        sortedFiles = sortFile(sortedFiles);
        return sortedFiles;
    }

    public File[] sortFile(File[] files) {
        if (files == null)
            return null;
        int length = files.length;
        File[] sortedFile = new File[length];
        int num = 0;
        for (int i = 0; i < length; i++) {
            if (files[i].isDirectory()) {
                sortedFile[num] = files[i];
                num++;
            }
        }
        for (int i = 0; i < length; i++) {
            if (files[i].isFile()) {
                sortedFile[num] = files[i];
                num++;
            }
        }
        return sortedFile;
    }

    private boolean compareStr(String str1, String str2) {
        if (str1 == null || str1.length() == 0)
            return false;
        if (str2 == null || str2.length() == 0)
            return true;
        boolean str1_Letter, str1_Num;
        boolean str2_Letter, str2_Num;
        str1_Letter = matchLetter(str1);
        str1_Num = matchNum(str1);
        str2_Letter = matchLetter(str2);
        str2_Num = matchNum(str2);

        if (str1_Num) {
            if (!str2_Num)
                return false;
            else {
                if (str1.charAt(0) == str2.charAt(0)) {
                    String str1_cut = str1.substring(1, str1.length());
                    String str2_cut = str2.substring(1, str2.length());
                    return compareStr(str1_cut, str2_cut);
                }
                return str1.charAt(0) > str2.charAt(0);
            }
        } else if (!str1_Letter) {
            if (str2_Num)
                return true;
            if (str2_Letter)
                return false;
            if (str1.charAt(0) == str2.charAt(0)) {
                String str1_cut = str1.substring(1, str1.length());
                String str2_cut = str2.substring(1, str2.length());
                return compareStr(str1_cut, str2_cut);
            }
            return str1.charAt(0) > str2.charAt(0);
        } else if (str1_Letter) {
            if (str2_Num)
                return true;
            if (!str2_Letter)
                return true;
            if (str1.charAt(0) == str2.charAt(0)) {
                String str1_cut = str1.substring(1, str1.length());
                String str2_cut = str2.substring(1, str2.length());
                if (str1_cut == null || str1_cut.length() == 0)
                    return false;
                if (str2_cut == null || str2_cut.length() == 0)
                    return true;
                return compareStr(str1_cut, str2_cut);
            }
            return str1.toLowerCase().charAt(0) > str2.toLowerCase().charAt(0);
        }
        return false;
    }

    public boolean matchLetter(String str) {
        if (str == null || str.length() == 0)
            return false;

        char c = str.charAt(0);
        if (c >= 'A' && c <= 'z')
            return true;
        return false;
    }

    public boolean matchNum(String str) {
        if (str == null || str.length() == 0)
            return false;

        char c = str.charAt(0);
        if (c >= '0' && c <= '9')
            return true;

        return false;
    }

    public File[] noHideFile(File[] files) {
        if (files == null || files.length == 0)
            return null;
        int length = files.length;
        int hideNum = hideFileNum(files);
        File[] noHideFile = new File[length - hideNum];
        int num = 0;
        for (int i = 0; i < length; i++) {
            if (files[i].getName().startsWith("."))
                continue;
            else {
                noHideFile[num] = files[i];
                num++;
            }
        }
        return noHideFile;
    }

    public int hideFileNum(File[] files) {
        if (files == null)
            return 0;
        int num = 0;
        int length = files.length;
        for (int i = 0; i < length; i++) {
            if (files[i].getName().startsWith("."))
                num++;
        }
        return num;
    }
}
