package org.openthos.filemanager.bean;

/**
 * Created by wm on 18-3-15.
 */

public class FolderBean {
    private boolean isCollected;
    private boolean isSystemFolder;
    private String title;
    private int iconRes;
    private String path;
    private int smallIconRes;

    public FolderBean() {
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setIsCollected(boolean isCollected) {
        this.isCollected = isCollected;
    }

    public boolean isSystemFolder() {
        return isSystemFolder;
    }

    public void setIsSystemFolder(boolean systemFolder) {
        isSystemFolder = systemFolder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public int getSmallIconRes() {
        return smallIconRes;
    }

    public void setSmallIconRes(int smallIconRes) {
        this.smallIconRes = smallIconRes;
    }

    @Override
    public String toString() {
        return "FolderBean{" +
                "isCollected=" + isCollected +
                ", isSystemFolder=" + isSystemFolder +
                ", title='" + title + '\'' +
                ", iconRes=" + iconRes +
                ", path='" + path + '\'' +
                ", smallIconRes=" + smallIconRes +
                '}';
    }
}
