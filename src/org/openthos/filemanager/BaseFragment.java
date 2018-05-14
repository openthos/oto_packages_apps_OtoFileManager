package org.openthos.filemanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment implements UiInterface {

    public View rootView;
    public MainActivity mMainActivity;
    public FragmentManager mManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        rootView = inflater.inflate(getLayoutId(), container, false);
        mMainActivity = (MainActivity) getActivity();
        mManager = getFragmentManager();
        initView();
        initData();
        initListener();
        return rootView;
    }

    public BaseFragment() {
        super();
    }

    public void enter() {
    }

//    public abstract void enter(String tag, String path);

    protected abstract void initData();

    protected abstract void initListener();

    protected abstract void initView();

    public abstract int getLayoutId();

    public abstract void processDirectionKey(int keyCode);

    public abstract void showMenu();

    public abstract void clearSelectList();

}
