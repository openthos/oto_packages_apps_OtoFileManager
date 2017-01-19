package com.openthos.filemanager.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import com.openthos.filemanager.bean.SeafileLibrary;

/**
 * Created by Wang Zhixu on 12/23/16.
 */

public class SeafileAccount {
    public String mUserName;
    public int mUserId;
    public ArrayList<SeafileLibrary> mLibrarys;
    public File mFile;

    public SeafileAccount(){
        mLibrarys = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < mLibrarys.size(); i++) {
            sb.append("{\"id\":\"" + mLibrarys.get(i).libraryId);
            sb.append("\",\"name\":\"" + mLibrarys.get(i).libraryName + "\"},");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append("]");
        return sb.toString();
    }
}
