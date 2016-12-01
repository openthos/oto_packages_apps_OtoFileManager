package com.openthos.filemanager.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.R;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.utils.T;

import java.util.ArrayList;

public class PersonalSpaceFragment extends BaseFragment {
    public static final String DOCUMENTS_PATH = Constants.ROOT_PATH + "documents";
    public static final String DOWNLOAD_PATH = Constants.ROOT_PATH + "Download";
    private LinearLayout mLlVideos;
    private LinearLayout mLlPictures;
    private LinearLayout mLlDocument;
    private LinearLayout mLlDownload;
    private LinearLayout mLlMusic;
    private long mCurrentBackTime;
    private double mLastBackTime;
    public Fragment mCurFragment;
    ArrayList<FileInfo> mFileInfoArrayList = null;
    FileViewInteractionHub.CopyOrMove mCopyOrMove = null;

    @Override
    protected void initData() {}

    @Override
    protected void initListener() {
        mLlVideos.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlPictures.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlDocument.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlDownload.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlMusic.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
    }

    @Override
    protected void initView() {
        mLlVideos = (LinearLayout) rootView.findViewById(R.id.ll_personal_videos);
        mLlPictures = (LinearLayout) rootView.findViewById(R.id.ll_personal_pictures);
        mLlDocument = (LinearLayout) rootView.findViewById(R.id.ll_personal_document);
        mLlDownload = (LinearLayout) rootView.findViewById(R.id.ll_personal_downloads);
        mLlMusic = (LinearLayout) rootView.findViewById(R.id.ll_personal_music);
    }

    @Override
    public int getLayoutId() {
        return R.layout.personal_grid_fragment;
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

    private class MouseLinearOnGenericMotionListener implements View.OnGenericMotionListener {
        @Override
        public boolean onGenericMotion(View v, MotionEvent event) {
            switch (event.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    primaryClick(v);
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    break;
                case MotionEvent.BUTTON_TERTIARY:
                    break;
                case MotionEvent.ACTION_SCROLL:
                    break;
                case MotionEvent.ACTION_HOVER_ENTER:
                    break;
            }
            return true;
        }
    }

    public void primaryClick(View view) {
        mCurrentBackTime = System.currentTimeMillis();
        switch (view.getId()) {
            case R.id.ll_personal_videos:
                setDiskClickInfo(R.id.ll_personal_videos, Constants.LEFT_FAVORITES,
                                 Constants.VIDEOS_PATH);
                break;
            case R.id.ll_personal_pictures:
                setDiskClickInfo(R.id.ll_personal_pictures, Constants.LEFT_FAVORITES,
                                 Constants.PICTURES_PATH);
                break;
            case R.id.ll_personal_document:
                setDiskClickInfo(R.id.ll_personal_document, Constants.LEFT_FAVORITES,
                                 DOCUMENTS_PATH);
                break;
            case R.id.ll_personal_downloads:
                setDiskClickInfo(R.id.ll_personal_downloads, Constants.LEFT_FAVORITES,
                                 DOWNLOAD_PATH);
                break;
            case R.id.ll_personal_music:
                setDiskClickInfo(R.id.ll_personal_music, Constants.LEFT_FAVORITES,
                                 Constants.MUSIC_PATH);
            default:
                break;
        }
    }

    private void setDiskClickInfo(int id, String tag, String path) {
        if (mCurrentBackTime - mLastBackTime > Constants.DOUBLE_CLICK_INTERVAL_TIME) {
            mLastBackTime = mCurrentBackTime;
        } else {
            if (mCurFragment != null) {
                mFileInfoArrayList = ((SystemSpaceFragment) mCurFragment).getFileInfoList();
                mCopyOrMove = ((SystemSpaceFragment) mCurFragment).getCurCopyOrMoveMode();
            }
            if (mFileInfoArrayList != null && mCopyOrMove != null) {
                T.showShort(context,
                        context.getString(R.string.operation_failed_permission_refuse));
            }
            mCurFragment = new SystemSpaceFragment(tag, path, mFileInfoArrayList, mCopyOrMove);

            FragmentTransaction transaction = mManager.beginTransaction();
            transaction.hide(mMainActivity.mSdStorageFragment.mCurFragment);
            transaction.add(R.id.fl_mian, mCurFragment, Constants.SYSTEM_SPACE_FRAGMENT_TAG)
                                                             .addToBackStack(null).commit();
            mMainActivity.mIsSdStorageFragmentHided = true;
            mMainActivity.mIsSdStorageFragment = false;
        }
    }
}
