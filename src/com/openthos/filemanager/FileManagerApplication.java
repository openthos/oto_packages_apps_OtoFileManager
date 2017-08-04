package com.openthos.filemanager;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

import com.openthos.filemanager.system.Constants;

import java.util.ArrayList;

/**
 * Created by root on 8/3/17.
 */

public class FileManagerApplication extends Application {
    public Handler handler;
    private ArrayList<MainActivity> activities = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == Constants.ONLY_REFRESH) {
                    for (MainActivity activity : activities) {
                        if (activity.mCurPath != null && activity.mCurPath.equals(msg.obj)) {
                            activity.mHandler.sendMessage(Message.obtain(activity.mHandler,
                                    Constants.REFRESH_BY_OBSERVER, msg.obj));
                        }
                    }
                }
            }
        };
    }

    public void addActivity(MainActivity activity) {
        activities.add(activity);
    }

    public void removeActivity(MainActivity activity) {
        activities.remove(activity);
    }
}
