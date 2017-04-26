package com.openthos.filemanager.bean;

public class Volume {
    private String mBlock;
    private String mType;
    private String mLength;
    private boolean mIsMount;
    private String mPath;
    private String mName;

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getBlock() {
        return mBlock;
    }

    public void setBlock(String block) {
        mBlock = block;
    }

    public String getLength() {
        return mLength;
    }

    public void setLength(String length) {
        mLength = length;
    }

    public boolean isMount() {
        return mIsMount;
    }

    public void setIsMount(boolean isMount) {
        mIsMount = isMount;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

}
