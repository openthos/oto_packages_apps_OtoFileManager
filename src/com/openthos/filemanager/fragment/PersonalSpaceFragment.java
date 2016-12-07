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
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PersonalSpaceFragment extends BaseFragment {
    private static final String QQ_IMAGE_PATH = Constants.ROOT_PATH + "Tencent/QQ_Images";
    private static final String QQ_FILE_PATH = Constants.ROOT_PATH + "Tencent/QQfile_recv";
    private static final String WEIXIN_IMG_PATH = Constants.ROOT_PATH + "Tencent/MicroMsg/WeiXin";
    private static final String BAIDU_PAN_PATH = Constants.ROOT_PATH + "BaiduNetdisk";
    private LinearLayout mLlVideos;
    private LinearLayout mLlPictures;
    private LinearLayout mLlDocument;
    private LinearLayout mLlDownload;
    private LinearLayout mLlMusic;
    private LinearLayout mLlQqImage;
    private LinearLayout mLlQqFile;
    private LinearLayout mLlWeixin;
    private LinearLayout mLlBaiduPan;
    private long mCurrentBackTime;
    private double mLastBackTime;
    public Fragment mCurFragment;
    ArrayList<FileInfo> mFileInfoArrayList = null;
    FileViewInteractionHub.CopyOrMove mCopyOrMove = null;

    @Override
    protected void initData() {
        HashMap<String, LinearLayout> layoutMap = new HashMap<>();
        layoutMap.put(QQ_IMAGE_PATH, mLlQqImage);
        layoutMap.put(QQ_FILE_PATH, mLlQqFile);
        layoutMap.put(WEIXIN_IMG_PATH, mLlWeixin);
        layoutMap.put(BAIDU_PAN_PATH, mLlBaiduPan);
        Iterator iterator = layoutMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String path = (String) entry.getKey();
            File file = new File(path);
            if (file.exists()) {
                ((LinearLayout)entry.getValue()).setVisibility(View.VISIBLE);
            } else {
                ((LinearLayout)entry.getValue()).setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void initListener() {
        mLlVideos.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlPictures.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlDocument.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlDownload.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlMusic.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlQqImage.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlQqFile.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlWeixin.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlBaiduPan.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
    }

    @Override
    protected void initView() {
        mLlVideos = (LinearLayout) rootView.findViewById(R.id.ll_personal_videos);
        mLlPictures = (LinearLayout) rootView.findViewById(R.id.ll_personal_pictures);
        mLlDocument = (LinearLayout) rootView.findViewById(R.id.ll_personal_document);
        mLlDownload = (LinearLayout) rootView.findViewById(R.id.ll_personal_downloads);
        mLlMusic = (LinearLayout) rootView.findViewById(R.id.ll_personal_music);
        mLlQqImage = (LinearLayout) rootView.findViewById(R.id.ll_personal_qq_image);
        mLlQqFile = (LinearLayout) rootView.findViewById(R.id.ll_personal_qq_file);
        mLlWeixin = (LinearLayout) rootView.findViewById(R.id.ll_personal_weixin);
        mLlBaiduPan = (LinearLayout) rootView.findViewById(R.id.ll_personal_baidudisk);
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
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.VIDEOS_PATH);
                break;
            case R.id.ll_personal_pictures:
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.PICTURES_PATH);
                break;
            case R.id.ll_personal_document:
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.DOCUMENT_PATH);
                break;
            case R.id.ll_personal_downloads:
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.DOWNLOAD_PATH);
                break;
            case R.id.ll_personal_music:
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.MUSIC_PATH);
                break;
            case R.id.ll_personal_qq_image:
                setDiskClickInfo(Constants.LEFT_FAVORITES, QQ_IMAGE_PATH);
                break;
            case R.id.ll_personal_qq_file:
                setDiskClickInfo(Constants.LEFT_FAVORITES, QQ_FILE_PATH);
                break;
            case R.id.ll_personal_weixin:
                setDiskClickInfo(Constants.LEFT_FAVORITES, WEIXIN_IMG_PATH);
                break;
            case R.id.ll_personal_baidudisk:
                setDiskClickInfo(Constants.LEFT_FAVORITES, BAIDU_PAN_PATH);
                break;
            default:
                break;
        }
    }

    private void setDiskClickInfo(String tag, String path) {
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
            transaction.hide(mMainActivity.mCurFragment);
            transaction.add(R.id.fl_mian, mCurFragment, Constants.PERSONALSYSTEMSPACE_TAG).commit();
            mMainActivity.mCurFragment = mCurFragment;
        }
    }
}
