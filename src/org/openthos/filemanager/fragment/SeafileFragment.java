package org.openthos.filemanager.fragment;

import android.app.LocalActivityManager;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;

import org.openthos.filemanager.BaseFragment;
import org.openthos.filemanager.R;
import org.openthos.filemanager.adapter.SeafileAdapter;
import org.openthos.filemanager.bean.SeafileLibrary;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.utils.SeafileUtils;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class SeafileFragment extends BaseFragment {

    private GridView mGvCloud;
    private FrameLayout mFlOther, mNoAccount;
    private Button mBindAccount;
    private SeafileAdapter mAdapter;
    private ArrayList<SeafileLibrary> mList = new ArrayList();
    private GridViewOnGenericMotionListener mMotionListener;
    private long mCurrentTime = 0L;
    private int mPos = -1;
    private View wd;
    private View backView;

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
        mMotionListener = new GridViewOnGenericMotionListener();
        mAdapter = new SeafileAdapter(mMainActivity, mList, mMotionListener);
        mGvCloud.setAdapter(mAdapter);
        mFlOther = (FrameLayout) rootView.findViewById(R.id.fl_other);
        mNoAccount = (FrameLayout) rootView.findViewById(R.id.no_account);
        mBindAccount = (Button) rootView.findViewById(R.id.bind_account);
    }

    protected void initData() {
    }

    @Override
    protected void initListener() {
        mBindAccount.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("org.openthos.org.openthos.seafile",
                                "org.openthos.org.openthos.seafile.OpenthosIDActivity"));
                        mMainActivity.startActivity(intent);
                    }
                });
        mGvCloud.setOnTouchListener(mMotionListener);
    }

    public ArrayList<SeafileLibrary> getList() {
        return mList;
    }

    public SeafileAdapter getAdapter() {
        return mAdapter;
    }

    public void setData(boolean isSync) {
        mNoAccount.setVisibility(View.GONE);
        mGvCloud.setVisibility(View.VISIBLE);
        mList.clear();
        mList.add(new SeafileLibrary("DATA", isSync));
        if (android.os.Build.TYPE.equals("eng")) {
            mList.add(new SeafileLibrary("Other", isSync));
        }
        mAdapter.notifyDataSetChanged();
    }

    public void clearData() {
        mList.clear();
        mAdapter.notifyDataSetChanged();
        mNoAccount.setVisibility(View.VISIBLE);
        mGvCloud.setVisibility(View.GONE);
    }

    @Override
    public boolean canGoBack() {
        if (wd == null) {
            return false;
        }
        backView = wd.findViewWithTag("back");
        if (backView.getVisibility() == View.VISIBLE || mGvCloud.getVisibility() != View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void goBack() {
        if (backView.getVisibility() == View.VISIBLE) {
            backView.performClick();
        } else {
            mFlOther.removeAllViews();
            mGvCloud.setVisibility(View.VISIBLE);
        }
    }

    public class GridViewOnGenericMotionListener implements View.OnTouchListener {
        //private boolean mIsShowDialog = false;
        //private boolean mIsItem = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMainActivity.clearNivagateFocus();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (view.getTag() instanceof SeafileAdapter.ViewHolder) {
                        mAdapter.clearSelected();
                        mAdapter.setSelected(view);
                        //if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                        //    mIsShowDialog = true;
                        //    mIsItem = true;
                        //}
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
                            //mIsShowDialog = true;
                            //mIsItem = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //if (mIsShowDialog == true) {
                    //    if (mIsItem) {
                    //        showItemDialog(motionEvent, mList.get(mPos), mPos);
                    //    } else {
                    //        showDialog(motionEvent);
                    //    }
                    //    mIsShowDialog = false;
                    //}
            }
            return false;
        }
    }

    //private void showItemDialog(MotionEvent motionEvent, SeafileLibrary library, int pos) {
    //    SeafileMenuDialog seafileDialog = new SeafileMenuDialog(mMainActivity, true, library, pos);
    //    seafileDialog.showDialog((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    //}

    //void showDialog(MotionEvent motionEvent) {
    //    SeafileMenuDialog seafileDialog = new SeafileMenuDialog(mMainActivity, false, null, -1);
    //    seafileDialog.showDialog((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    //}

    @Override
    public void enter() {
        super.enter();
        if (mPos == 0) {
            mMainActivity.showFileSpaceFragment(SeafileUtils.SEAFILE_DATA_PATH + "/"
                    + SeafileUtils.mUserId + "/" + mList.get(mPos).libraryName);
        } else {
            mGvCloud.setVisibility(View.GONE);
            if (wd == null) {
                Intent intent = new Intent();
                intent.setClassName("org.openthos.org.openthos.seafile", "org.openthos.org.openthos.seafile.seaapp.SeafileActivity");
                LocalActivityManager mLocalActivityManager = new LocalActivityManager(getActivity(), true);
                mLocalActivityManager.dispatchCreate(null);
                mLocalActivityManager.dispatchResume();
                final Window w = mLocalActivityManager.startActivity("TagName", intent);
                wd = w != null ? w.getDecorView() : null;
                if (wd != null) {
                    wd.setVisibility(View.VISIBLE);
                    wd.setFocusableInTouchMode(true);
                    ((ViewGroup) wd).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                }
            }
            mFlOther.addView(wd);
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

    public void goToHome() {
        if (wd != null) {
            mFlOther.removeAllViews();
            mGvCloud.setVisibility(View.VISIBLE);
            wd = null;
            backView = null;
        }
    }
}
