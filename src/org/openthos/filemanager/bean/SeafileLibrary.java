package org.openthos.filemanager.bean;

/**
 * Created by Wang Zhixu on 01/19/17.
 */

public class SeafileLibrary {
    public String libraryName;
    public boolean isSync;

    public SeafileLibrary(String name, boolean isSync) {
        libraryName = name;
        this.isSync = isSync;
    }
}
