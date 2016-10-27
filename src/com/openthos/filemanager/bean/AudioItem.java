package com.openthos.filemanager.bean;

public class AudioItem {
    private String name;
    private long size;
    private String data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "AudioItem{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", data='" + data + '\'' +
                '}';
    }
}
