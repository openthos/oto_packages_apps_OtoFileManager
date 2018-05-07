package org.openthos.filemanager.component;

import org.openthos.filemanager.bean.FolderBean;

import java.util.Comparator;

public class FolderBeanComparator implements Comparator<FolderBean> {
    @Override
    public int compare(FolderBean leftBean, FolderBean rightBean) {
        if (leftBean.isSystemFolder() == rightBean.isSystemFolder()) {
            return leftBean.getPath().compareToIgnoreCase(rightBean.getPath());
        } else if (leftBean.isSystemFolder()) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object object) {
        return false;
    }
}
