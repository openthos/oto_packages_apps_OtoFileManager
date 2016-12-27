package com.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.view.Window;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.SeafileAdapter;
import com.openthos.filemanager.bean.SeafileAccount;
import com.openthos.filemanager.component.CloudDialog;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressLint("ValidFragment")
public class SeafileFragment extends BaseFragment {

    private GridView mGvCloud;
    private SeafileAdapter mAdapter;
    private ArrayList<HashMap<String, String>> mList;

    @Override
    public int getLayoutId() {
        return R.layout.cloud_fragment_layout;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void initView() {
        mGvCloud = (GridView) rootView.findViewById(R.id.gv_cloud_service);
        mList = new ArrayList<>();
        mAdapter = new SeafileAdapter(mMainActivity, mList);
        mGvCloud.setAdapter(mAdapter);
    }

    protected void initData() {
    }

    @Override
    protected void initListener() {
        mGvCloud.setOnGenericMotionListener(new GridViewOnGenericMotionListener());
    }

    public ArrayList<HashMap<String, String>> getList() {
        return mList;
    }

    public SeafileAdapter getAdapter() {
        return mAdapter;
    }

    public void setData(ArrayList<HashMap<String, String>> librarys) {
        mList = librarys;
        mAdapter.setData(librarys);
    }


    public boolean canGoBack() {
        return false;
    }

    public void goBack() {
    }


    class GridViewOnGenericMotionListener implements View.OnGenericMotionListener {

        @Override
        public boolean onGenericMotion(View view, MotionEvent motionEvent) {
            switch (motionEvent.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    CloudDialog cloudDialog = new CloudDialog(mMainActivity);
                    cloudDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    cloudDialog.showDialog((int) motionEvent.getRawX(),
                            (int) motionEvent.getRawY());
                    break;
                case MotionEvent.BUTTON_TERTIARY:
                    break;
                case MotionEvent.ACTION_SCROLL:
                    break;
                case MotionEvent.ACTION_HOVER_ENTER:
                    break;
            }
            return false;
        }
    }

    @Override
    protected void enter(String tag, String path) {
    }
}
