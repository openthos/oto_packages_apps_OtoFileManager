package org.openthos.filemanager.utils;

public class LocalCache {
    private static String viewTag = Constants.VIEW_TAG_GRID;

    public static void setViewTag(String tag){
        viewTag = tag;
    }

    public static String getViewTag(){
        return viewTag;
    }
}
