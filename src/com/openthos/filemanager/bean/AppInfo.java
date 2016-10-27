package com.openthos.filemanager.bean;

import android.graphics.drawable.Drawable;
public class AppInfo {
    String appName;
    String packageName;
    Drawable appIcon;

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getIcon() {
        return appIcon;
    }

    public void setIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
}
