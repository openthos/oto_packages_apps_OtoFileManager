package com.openthos.filemanager.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.view.Window;
import android.content.ClipboardManager;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.PersonalAdapter;
import com.openthos.filemanager.drag.DragGridView;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.utils.T;
import com.openthos.filemanager.component.PersonalMenuDialog;

import java.util.ArrayList;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PersonalSpaceFragment extends BaseFragment {
    private DragGridView mPersonalGrid;
    private List<String> mPersonalList;
    private PersonalAdapter mPersonalAdaper;
    private LinkedHashMap<String, String> mPathMap;
    private Context mContext;
    private long mCurrentBackTime;
    private double mLastBackTime;
    public Fragment mCurFragment;
    private GridViewOnGenericMotionListener mMotionListener;
    ArrayList<FileInfo> mFileInfoArrayList = null;
    FileViewInteractionHub.CopyOrMove mCopyOrMove = null;
    private PersonalMenuDialog mPersonalDialog;
    private int mPos;

    @Override
    protected void initData() {
        mContext = getActivity();
        mPersonalList = new ArrayList<>();
        mMotionListener = new GridViewOnGenericMotionListener();
        mPathMap = new LinkedHashMap<>();
        mPersonalAdaper = new PersonalAdapter(mContext, mPersonalList, mMotionListener);
        mPersonalGrid.setAdapter(mPersonalAdaper);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            checkFolder();
        }
    }

    public void checkFolder() {
        mPersonalList.clear();
        mPathMap.put(getString(R.string.desk), Constants.DESKTOP_PATH);
        mPathMap.put(getString(R.string.music), Constants.MUSIC_PATH);
        mPathMap.put(getString(R.string.video), Constants.VIDEOS_PATH);
        mPathMap.put(getString(R.string.picture), Constants.PICTURES_PATH);
        mPathMap.put(getString(R.string.docement), Constants.DOCUMENT_PATH);
        mPathMap.put(getString(R.string.downloads), Constants.DOWNLOAD_PATH);
        mPathMap.put(getString(R.string.recycle), Constants.RECYCLE_PATH);
        mPathMap.put(getString(R.string.qq_image), Constants.QQ_IMAGE_PATH);
        mPathMap.put(getString(R.string.qq_file), Constants.QQ_FILE_PATH);
        mPathMap.put(getString(R.string.winxin), Constants.WEIXIN_IMG_PATH);
        mPathMap.put(getString(R.string.baidu_disk), Constants.BAIDU_PAN_PATH);
        Iterator iterator = mPathMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String path = (String) entry.getValue();
            File file = new File(path);
            if (file.exists()) {
                mPersonalList.add((String) entry.getKey());
            }
        }
        mPersonalAdaper = new PersonalAdapter(mContext, mPersonalList, mMotionListener);
        mPersonalGrid.setAdapter(mPersonalAdaper);
        mPersonalAdaper.notifyDataSetChanged();
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
            integerList = mPersonalAdaper.getSelectFileInfoList();
            switch (event.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    if (v.getTag() instanceof PersonalAdapter.ViewHolder) {
                        mPos = (int) ((PersonalAdapter.ViewHolder) v.getTag()).name.getTag();
                        if (!integerList.contains(mPos)) {
                            integerList.clear();
                            integerList.add(mPos);
                        }
                        mCurrentBackTime = System.currentTimeMillis();
                        setDiskClickInfo(Constants.LEFT_FAVORITES, mPos);
                        mPersonalAdaper.notifyDataSetChanged();
                    } else {
                        integerList.clear();
                    }
                    mPersonalAdaper.notifyDataSetChanged();
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    if (v.getTag() instanceof PersonalAdapter.ViewHolder) {
                        int pos = (int) ((PersonalAdapter.ViewHolder) v.getTag()).name.getTag();
                        if (!integerList.contains(pos)) {
                            integerList.clear();
                            integerList.add(pos);
                        }
                        mCurId = pos;
                        mPersonalDialog = new PersonalMenuDialog(mContext, false);
                    } else {
                        mPersonalDialog = new PersonalMenuDialog(mContext, true);
                        integerList.clear();
                    }
                    mPersonalDialog.showDialog((int) event.getRawX(), (int) event.getRawY());
                    mPersonalAdaper.notifyDataSetChanged();
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

    private void setDiskClickInfo(String tag, int id) {
        if (mCurrentBackTime - mLastBackTime > Constants.DOUBLE_CLICK_INTERVAL_TIME
                || id != mCurId) {
            mCurId = id;
            mLastBackTime = mCurrentBackTime;
        } else {
            enter(tag, mPathMap.get(mPersonalList.get(id)));
        }
    }

    @Override
    protected void enter(String tag, String path) {
        if (mCurFragment != null) {
            mFileInfoArrayList = ((SystemSpaceFragment) mCurFragment).getFileInfoList();
            mCopyOrMove = ((SystemSpaceFragment) mCurFragment).getCurCopyOrMoveMode();
        }
        if (mFileInfoArrayList != null && mCopyOrMove != null) {
            T.showShort(context,
                    context.getString(R.string.operation_failed_permission_refuse));
        }
        mCurFragment = new SystemSpaceFragment(tag, path, mFileInfoArrayList, mCopyOrMove, false);
        FragmentTransaction transaction = mManager.beginTransaction();
        transaction.hide(mMainActivity.mCurFragment);
        transaction.add(R.id.fl_mian, mCurFragment, Constants.PERSONALSYSTEMSPACE_TAG)
                .commitAllowingStateLoss();
        mMainActivity.mCurFragment = mCurFragment;
    }

    @Override
    public void enter() {
        enter(Constants.LEFT_FAVORITES, mPathMap.get(mPersonalList.get(mCurId)));
    }

    public void copyPath() {
        ((ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE))
                                             .setText(mPathMap.get(mPersonalList.get(mCurId)));
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
                mPos = mPos < mPersonalList.size() - 1 ? mPos + 1 : mPos;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mPos = mPos < mPersonalList.size() - numColumns ? mPos + numColumns : mPos;
                break;
        }
        List<Integer> integerList = mPersonalAdaper.getSelectFileInfoList();
        integerList.clear();
        integerList.add(mPos);
        mPersonalAdaper.notifyDataSetChanged();
    }

    @Override
    public void showMenu() {
    }
}
