package com.openthos.filemanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.openthos.filemanager.bean.ImageBean;
import com.openthos.filemanager.bean.SearchInfo;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BaseFragment extends Fragment implements UiInterface{

    public View rootView;
    public FragmentManager mManager;
    public String usbDeviceIsAttached;
    public Context context;
    public MainActivity mMainActivity;
    public HashMap<String, List<String>> mGruopMap;
    public List<ImageBean> list;
    public int index;

    public ArrayList<SearchInfo> mSearchList = new ArrayList<>();
    public String sdOrSystem;
    public String directorPath;
    public ArrayList<FileInfo> mFileInfoList;
    public FileViewInteractionHub.CopyOrMove mCopyOrMove;

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

    @SuppressLint("ValidFragment")
    public BaseFragment(HashMap<String, List<String>> mGruopMap, List<ImageBean> list, int index) {
        this.mGruopMap = mGruopMap;
        this.list = list;
        this.index = index;
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public BaseFragment(FragmentManager manager) {
        mManager = manager;
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public BaseFragment(FragmentManager manager, ArrayList<SearchInfo> mFileList) {
        this.mSearchList = mFileList;
        mManager = manager;
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public BaseFragment(String sdSpaceFragment, String directPath, ArrayList<FileInfo> fileInfoList,
                                                FileViewInteractionHub.CopyOrMove copyOrMove) {
        this.sdOrSystem = sdSpaceFragment;
        mFileInfoList = fileInfoList;
        mCopyOrMove = copyOrMove;
        this.directorPath = directPath;
    }

    protected abstract void initData();
    protected abstract void initListener();
    protected abstract void initView();
    public abstract int getLayoutId();
}
