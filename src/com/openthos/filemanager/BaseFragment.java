package com.openthos.filemanager;

import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {
    public abstract boolean canGoBack();
    public abstract void goBack();
}
