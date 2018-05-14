package org.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

import org.openthos.filemanager.BaseFragment;
import org.openthos.filemanager.R;
import org.openthos.filemanager.adapter.SeafileAdapter;
import org.openthos.filemanager.bean.SeafileLibrary;
import org.openthos.filemanager.component.SeafileMenuDialog;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.utils.SeafileUtils;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class SeafileFragment extends BaseFragment {

    private GridView mGvCloud;
    private SeafileAdapter mAdapter;
    private ArrayList<SeafileLibrary> mList;
    private GridViewOnGenericMotionListener mMotionListener;
    private long mCurrentTime = 0L;
    private int mPos = -1;


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
        return false;
    }

    @Override
    public void goBack() {
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
        SeafileMenuDialog seafileDialog = new SeafileMenuDialog(mMainActivity, true, library, pos);
        seafileDialog.showDialog((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    void showDialog(MotionEvent motionEvent) {
        SeafileMenuDialog seafileDialog = new SeafileMenuDialog(mMainActivity, false, null, -1);
        seafileDialog.showDialog((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    @Override
    public void enter() {
        super.enter();
        enter(SeafileUtils.SEAFILE_DATA_PATH + "/" + SeafileUtils.mUserId
                + "/" + mList.get(mPos).libraryName);
    }

    public void enter(String path) {
        mMainActivity.showFileSpaceFragment(path);
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
