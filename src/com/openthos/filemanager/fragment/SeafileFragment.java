package com.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.view.Window;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.SeafileAdapter;
import com.openthos.filemanager.bean.SeafileAccount;
import com.openthos.filemanager.bean.SeafileLibrary;
import com.openthos.filemanager.component.SeafileDialog;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.utils.SeafileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("ValidFragment")
public class SeafileFragment extends BaseFragment {

    private GridView mGvCloud;
    private SeafileAdapter mAdapter;
    private ArrayList<SeafileLibrary> mList;
    private Timer mTimer;
    private GridViewOnGenericMotionListener mMotionListener;
    public Fragment mCurFragment;
    private long mCurrentTime = 0L;
    private int mPos = -1;
    private static final int DELAY_TIME = 1000;
    private static final int SLEEP_TIME = 10000;


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
        mGvCloud.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mList = new ArrayList<>();
        mMotionListener = new GridViewOnGenericMotionListener();
        mAdapter = new SeafileAdapter(mMainActivity, mList, mMotionListener);
        mGvCloud.setAdapter(mAdapter);
        mTimer = new Timer();
    }

    protected void initData() {
    }

    @Override
    protected void initListener() {
        mGvCloud.setOnTouchListener(mMotionListener);
    }

    public ArrayList<SeafileLibrary> getList() {
        return mList;
    }

    public SeafileAdapter getAdapter() {
        return mAdapter;
    }

    public void setData(ArrayList<SeafileLibrary> librarys) {
        if (librarys.size() > 0) {
            mTimer.schedule(new LiftLimitTask(SeafileUtils.SEAFILE_DATA_PATH_REAlLY),
                    DELAY_TIME, SLEEP_TIME);
        }
        mList = librarys;
        mAdapter.setData(librarys);
    }

    @Override
    public boolean canGoBack() {
        boolean canGoBack = false;
        Fragment baseFragment = mCurFragment;
        if (baseFragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) baseFragment;
            canGoBack = systemSpaceFragment.canGoBack();
        }
        return canGoBack;
    }

    @Override
    public void goBack() {
        Fragment baseFragment = mCurFragment;
        if (baseFragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) baseFragment;
            systemSpaceFragment.goBack();
        }
    }

    public class GridViewOnGenericMotionListener implements View.OnTouchListener {
        private boolean mIsShowDialog = false;
        private boolean mIsItem = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMainActivity.clearNivagateFocus();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (view.getTag() instanceof SeafileAdapter.ViewHolder) {
                        mAdapter.clearSelected();
                        mAdapter.setSelected(view);
                        if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                            mIsShowDialog = true;
                            mIsItem = true;
                        }
                        int pos = (int) ((SeafileAdapter.ViewHolder) view.getTag()).name.getTag();
                        if (System.currentTimeMillis() - mCurrentTime
                                < Constants.DOUBLE_CLICK_INTERVAL_TIME
                                && motionEvent.getButtonState() != MotionEvent.BUTTON_SECONDARY) {
                            if (mPos != pos) {
                                mPos = pos;
                                mCurrentTime = System.currentTimeMillis();
                                return true;
                            }
                            enter();
                        } else {
                            mPos = pos;
                            mCurrentTime = System.currentTimeMillis();
                        }
                        return true;
                    } else {
                        mAdapter.clearSelected();
                        if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                            mPos = -1;
                            mIsShowDialog = true;
                            mIsItem = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mIsShowDialog == true) {
                        if (mIsItem) {
                            showItemDialog(motionEvent, mList.get(mPos), mPos);
                        } else {
                            showDialog(motionEvent);
                        }
                        mIsShowDialog = false;
                    }
            }
            return false;
        }
    }

    private void showItemDialog(MotionEvent motionEvent, SeafileLibrary library, int pos) {
        SeafileDialog seafileDialog = new SeafileDialog(mMainActivity, true, library, pos);
        seafileDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        seafileDialog.showDialog((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    void showDialog(MotionEvent motionEvent) {
        SeafileDialog seafileDialog = new SeafileDialog(mMainActivity, false, null, -1);
        seafileDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        seafileDialog.showDialog((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    @Override
    public void enter() {
        super.enter();
        enter("hello", SeafileUtils.SEAFILE_DATA_PATH_REAlLY
                + mMainActivity.mAccount.mFile.getAbsolutePath()
                + "/" + mList.get(mPos).libraryName);
    }

    @Override
    protected void enter(String tag, String path) {
        FragmentTransaction transaction = mManager.beginTransaction();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        transaction.hide(mMainActivity.mCurFragment);
        mCurFragment = new SystemSpaceFragment(
                Constants.LEFT_FAVORITES, path, null, null, false);
        transaction.add(R.id.fl_mian, mCurFragment, Constants.SEAFILESYSTEMSPACE_TAG);
        transaction.show(mCurFragment).commit();
        mMainActivity.mCurFragment = mCurFragment;
        //mMainActivity.setFileInfo(R.id.et_nivagation, path, mAddressFragment);
    }

    private class LiftLimitTask extends TimerTask {
        Process pro;
        BufferedReader in = null;
        String filePath;

        public LiftLimitTask(String path) {
            filePath = path.replace(" ", "\\");
        }

        @Override
        public void run() {
            try {
                pro = Runtime.getRuntime().exec(
                        new String[]{"su", "-c", "chmod -R 777 " + filePath});
                in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                while (in.readLine() != null) {
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
