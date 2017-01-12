package com.openthos.filemanager.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Wang Zhixu on 12/23/16.
 */

public class SeafileAccount {
    public static final String LIBRARY_ID="libraryid";
    public static final String LIBRARY_NAME="libraryname";
    public static final String LIBRARY_ISSYNC="isSync";
    public String mUserName;
    public int mUserId;
    public ArrayList<HashMap<String, String>> mLibrarys;
    public File mFile;

    public SeafileAccount(){
        mLibrarys = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < mLibrarys.size(); i++) {
            sb.append("{\"id\":\"" + mLibrarys.get(i).get(LIBRARY_ID));
            sb.append("\",\"name\":\"" + mLibrarys.get(i).get(LIBRARY_NAME) + "\"},");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append("]");
        return sb.toString();
    }
}
