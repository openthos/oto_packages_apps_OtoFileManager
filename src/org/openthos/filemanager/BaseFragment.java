package org.openthos.filemanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openthos.filemanager.system.FileInfo;
import org.openthos.filemanager.system.FileViewInteractionHub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BaseFragment extends Fragment implements UiInterface{

    public View rootView;
    public FragmentManager mManager;
    public String usbDeviceIsAttached;
    public Context context;
    public MainActivity mMainActivity;
    public int index;

    public String sdOrSystem;
    public String directorPath;
    public ArrayList<FileInfo> mFileInfoList;
    public FileViewInteractionHub mFileViewInteractionHub;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                                      @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutId(),container,false);
        mManager = getFragmentManager();
        mMainActivity = (MainActivity) getActivity();

        initView();
        initData();
        initListener();
        return rootView;
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public BaseFragment(FragmentManager manager, String usbDeviceIsAttached, MainActivity context) {
        mManager = manager;
        this.usbDeviceIsAttached = usbDeviceIsAttached;
        this.context = context;
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public BaseFragment() {
        super();
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public BaseFragment(FragmentManager manager) {
        mManager = manager;
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public BaseFragment(String sdSpaceFragment, String directPath, ArrayList<FileInfo> fileInfoList) {
        this.sdOrSystem = sdSpaceFragment;
        mFileInfoList = fileInfoList;
        this.directorPath = directPath;
    }

    public void enter() { }

    public abstract void enter(String tag, String path);

    protected abstract void initData();
    protected abstract void initListener();
    protected abstract void initView();
    public abstract int getLayoutId();
    public abstract void processDirectionKey(int keyCode);
    public abstract void showMenu();
    public abstract void clearSelectList();

}
