package org.openthos.filemanager.bean;

public class PathBean {
    public String root;
    public String path;

    public PathBean(String path) {
        this.root = path;
        this.path = path;
    }

    public PathBean(String root, String path) {
        this.root = root;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PathBean)
            return ((PathBean) o).root.equals(root) && ((PathBean) o).path.equals(path);
        return false;
    }
}
