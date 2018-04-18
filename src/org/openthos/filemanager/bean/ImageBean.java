package org.openthos.filemanager.bean;

public class ImageBean {
    private String topImagePath;
    private String folderName;
    private int imageCounts;
    private long iconSize;

    public long getIconSize() {
        return iconSize;
    }

    public void setIconSize(long iconSize) {
        this.iconSize = iconSize;
    }

    public String getTopImagePath() {
        return topImagePath;
    }

    public void setTopImagePath(String topImagePath) {
        this.topImagePath = topImagePath;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getImageCounts() {
        return imageCounts;
    }

    public void setImageCounts(int imageCounts) {
        this.imageCounts = imageCounts;
    }
}
