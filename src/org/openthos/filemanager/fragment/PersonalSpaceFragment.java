package org.openthos.filemanager.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.content.ClipboardManager;

import org.openthos.filemanager.BaseFragment;
import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.R;
import org.openthos.filemanager.adapter.PersonalAdapter;
import org.openthos.filemanager.bean.FolderBean;
import org.openthos.filemanager.drag.DragGridView;
import org.openthos.filemanager.system.Constants;
import org.openthos.filemanager.system.FileInfo;
import org.openthos.filemanager.system.FileViewInteractionHub;
import org.openthos.filemanager.utils.T;
import org.openthos.filemanager.component.PersonalMenuDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalSpaceFragment extends BaseFragment {
    private DragGridView mPersonalGrid;
    private PersonalAdapter mPersonalAdapter;
    private List<FolderBean> mFolderBeanList;
    private Map<Integer, SystemSpaceFragment> mPosAndFragmentMap = new HashMap<>();
    private double mLastBackTime;
    public Fragment mCurFragment;
    private GridViewOnGenericMotionListener mMotionListener;
    ArrayList<FileInfo> mFileInfoArrayList = null;
    private int mPos;
    private PersonalMenuDialog mPersonalDialog;

    @Override
    protected void initData() {
        mFolderBeanList = mMainActivity.getFolderBeanList();
        mMotionListener = new GridViewOnGenericMotionListener();
        mPersonalAdapter = new PersonalAdapter(mMainActivity, mFolderBeanList, mMotionListener);
        mPersonalGrid.setAdapter(mPersonalAdapter);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refresh();
        }
    }

    public void refresh() {

    }

    @Override
    protected void initListener() {
        mPersonalGrid.setOnGenericMotionListener(mMotionListener);
    }

    @Override
    protected void initView() {
        mPersonalGrid = (DragGridView) rootView.findViewById(R.id.personal_fragment_grid);
    }

    @Override
    public int getLayoutId() {
        return R.layout.personal_fragments_layout;

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

    public class GridViewOnGenericMotionListener implements View.OnGenericMotionListener {
        List<Integer> integerList;

        @Override
        public boolean onGenericMotion(View v, MotionEvent event) {
            mMainActivity.clearNivagateFocus();
            integerList = mPersonalAdapter.getSelectFileInfoList();
            switch (event.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    if (v.getTag() instanceof PersonalAdapter.ViewHolder) {
                        if (mPos != (Integer)
                                ((PersonalAdapter.ViewHolder) v.getTag()).tvTitle.getTag()) {
                            mLastBackTime = 0;
                            mPos = (Integer) ((PersonalAdapter.ViewHolder) v.getTag()).tvTitle.getTag();
                        }
                        if (System.currentTimeMillis() - mLastBackTime
                                < Constants.DOUBLE_CLICK_INTERVAL_TIME) {
                            enter();
                            mLastBackTime = 0;
                        } else {
                            mLastBackTime = System.currentTimeMillis();
                        }
                        if (!integerList.contains(mPos)) {
                            integerList.clear();
                            integerList.add(mPos);
                        }
                    } else {
                        integerList.clear();
                    }
                    mPersonalAdapter.notifyDataSetChanged();
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    if (v.getTag() instanceof PersonalAdapter.ViewHolder) {
                        mPos = (Integer) ((PersonalAdapter.ViewHolder) v.getTag()).tvTitle.getTag();
                        if (!integerList.contains(mPos)) {
                            integerList.clear();
                            integerList.add(mPos);
                        }
                        mPersonalDialog = new PersonalMenuDialog
                                (mMainActivity, mPos, mFolderBeanList.get(mPos).isCollected());
                        mPersonalDialog.showDialog((int) event.getRawX(), (int) event.getRawY());
                    } else {
//                        mPersonalDialog = new PersonalMenuDialog(mMainActivity);
                        integerList.clear();
                    }
//                    mPersonalDialog.showDialog((int) event.getRawX(), (int) event.getRawY());
                    mPersonalAdapter.notifyDataSetChanged();
                    break;
                case MotionEvent.BUTTON_TERTIARY:
                    mMainActivity.onUp();
                    break;
                case MotionEvent.ACTION_SCROLL:
                    break;
                case MotionEvent.ACTION_HOVER_ENTER:
                    break;
            }
            return true;
        }
    }

    @Override
    public void enter(String tag, String title) {
    }

    @Override
    public void enter() {
        enter(mPos);
    }

    private void enter(int position) {
        if (mCurFragment != null) {
            mFileInfoArrayList = ((SystemSpaceFragment) mCurFragment).getFileInfoList();
        }
        if (mFileInfoArrayList != null) {
            T.showShort(mMainActivity,
                    mMainActivity.getString(R.string.operation_failed_permission_refuse));
        }
        FragmentTransaction transaction = mManager.beginTransaction();
        transaction.hide(mMainActivity.mCurFragment);
        mCurFragment = mPosAndFragmentMap.get(position);
        if (mCurFragment == null) {
            mCurFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                    mFolderBeanList.get(position).getPath(), null, false);
            transaction.add(R.id.fl_mian, mCurFragment, Constants.PERSONALSYSTEMSPACE_TAG);
            mPosAndFragmentMap.put(position, (SystemSpaceFragment) mCurFragment);
        }
        transaction.show(mCurFragment).commitAllowingStateLoss();
        mMainActivity.mCurFragment = mCurFragment;
    }

    public void copyPath() {
        ((ClipboardManager) mMainActivity.getSystemService(Context.CLIPBOARD_SERVICE))
                .setText(mFolderBeanList.get(mPos).getPath());
    }

    @Override
    public void processDirectionKey(int keyCode) {
        int numColumns = mPersonalGrid.getNumColumns();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mPos = mPos > 0 ? mPos - 1 : mPos;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                mPos = mPos > numColumns - 1 ? mPos - numColumns : mPos;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mPos = mPos < mFolderBeanList.size() - 1 ? mPos + 1 : mPos;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mPos = mPos < mFolderBeanList.size() - numColumns ?
                        mPos + numColumns : mPos;
                break;
        }
        List<Integer> integerList = mPersonalAdapter.getSelectFileInfoList();
        integerList.clear();
        integerList.add(mPos);
        mPersonalAdapter.notifyDataSetChanged();
    }

    @Override
    public void showMenu() {
    }

    @Override
    public void clearSelectList() {
    }

}
