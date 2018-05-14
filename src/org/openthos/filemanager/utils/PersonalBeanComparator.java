package org.openthos.filemanager.utils;

import org.openthos.filemanager.bean.PersonalBean;

import java.util.Comparator;

public class PersonalBeanComparator implements Comparator<PersonalBean> {
    @Override
    public int compare(PersonalBean leftBean, PersonalBean rightBean) {
        if (leftBean.isSystemFolder() == rightBean.isSystemFolder()) {
            return leftBean.getPath().compareToIgnoreCase(rightBean.getPath());
        } else if (leftBean.isSystemFolder()) {
            return -1;
        } else {
            return 1;
        }
    }
}
