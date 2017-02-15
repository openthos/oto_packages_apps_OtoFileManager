package com.openthos.filemanager.system;

public class FileInfo {
    public String fileName;
    public String filePath;
    public long fileSize;
    public boolean IsDir;
    public int Count;
    public long ModifiedDate;
    public boolean Selected;
    public boolean canRead;
    public boolean canWrite;
    public boolean isHidden;
    public int fileIconId;
    public long dbId; // id in the database, if is from database
    public float left;
    public float top;
    public float right;
    public float bottom;
}
