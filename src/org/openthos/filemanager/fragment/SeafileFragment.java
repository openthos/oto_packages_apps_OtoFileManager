package org.openthos.filemanager.fragment;

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

import org.openthos.filemanager.BaseFragment;
import org.openthos.filemanager.R;
import org.openthos.filemanager.adapter.SeafileAdapter;
import org.openthos.filemanager.bean.SeafileLibrary;
import org.openthos.filemanager.component.SeafileDialog;
import org.openthos.filemanager.system.Constants;
import org.openthos.filemanager.system.FileInfo;
import org.openthos.filemanager.system.FileListAdapter;
import org.openthos.filemanager.system.FileViewInteractionHub;
import org.openthos.filemanager.utils.SeafileUtils;

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
    private GridViewOnGenericMotionListener mMotionListener;
    private long mCurrentTime = 0L;
    private int mPos = -1;
    private SystemSpaceFragment fragment;


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
        mList = librarys;
        mAdapter.setData(librarys);
    }

    @Override
    public boolean canGoBack() {
        boolean canGoBack = false;
        if (fragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) fragment;
            canGoBack = systemSpaceFragment.canGoBack();
        }
        return canGoBack;
    }

    @Override
    public void goBack() {
        if (fragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) fragment;
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
        seafileDialog.showDialog((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    void showDialog(MotionEvent motionEvent) {
        SeafileDialog seafileDialog = new SeafileDialog(mMainActivity, false, null, -1);
        seafileDialog.showDialog((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    @Override
    public void enter() {
        super.enter();
        enter(null, SeafileUtils.SEAFILE_DATA_PATH + "/" + SeafileUtils.mUserId
                + "/" + mList.get(mPos).libraryName);
    }

    @Override
    public void enter(String tag, String path) {
        FragmentTransaction transaction = mManager.beginTransaction();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        transaction.hide(mMainActivity.mCurFragment);
        if (fragment == null) {
            fragment = new SystemSpaceFragment(
                    Constants.LEFT_FAVORITES, path, null, false);
            transaction.add(R.id.fl_mian, fragment, Constants.SEAFILESYSTEMSPACE_TAG);
        } else {
            fragment.setPath(path);
            fragment.getFileViewInteractionHub().setRootPath(path);
            FileListAdapter adapter = fragment.getAdapter();
            if (adapter != null) {
                adapter.getSelectFileInfoList().clear();
                fragment.getFileViewInteractionHub().clearSelection();
                fragment.onRefreshFileList(path, mMainActivity.getFileSortHelper());
            }
        }
        transaction.show(fragment).commitAllowingStateLoss();
        mMainActivity.mCurFragment = fragment;
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

    @Override
    public void processDirectionKey(int keyCode) {
    }

    @Override
    public void showMenu() {
    }

    @Override
    public void clearSelectList() {
    }
}
