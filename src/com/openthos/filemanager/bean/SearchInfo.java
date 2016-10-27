package com.openthos.filemanager.bean;

public class SearchInfo {

    public String fileName;
    public String filePath;
    public long fileSize;
    public int Count;
    public boolean IsDir;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getCount() {
        return Count;
    }

    public void setCount(int count) {
        Count = count;
    }

    public boolean isDir() {
        return IsDir;
    }

    public void setDir(boolean dir) {
        IsDir = dir;
    }
}
