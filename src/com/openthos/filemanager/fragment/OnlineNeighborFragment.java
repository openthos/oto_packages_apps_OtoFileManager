package com.openthos.filemanager.fragment;

import android.view.View;
import android.widget.TextView;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.R;

public class OnlineNeighborFragment extends BaseFragment{
    private TextView tv_internet;

    @Override
    protected void initView() {
        tv_internet = (TextView) rootView.findViewById(R.id.tv_internet);
        tv_internet.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void initListener() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.personal_fragment_layout;
    }

    @Override
    public boolean canGoBack() {
        return false;
    }

    @Override
    public void goBack() {
    }

    @Override
    protected void enter(String tag, String path) {
    }

    @Override
    public void processDirectionKey(int keyCode) {
    }

    @Override
    public void showMenu() {
    }
}
