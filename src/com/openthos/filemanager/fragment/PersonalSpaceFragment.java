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
    private LinearLayout mLlRecycle;
    private LinearLayout mLlQqImage;
    private LinearLayout mLlQqFile;
    private LinearLayout mLlWeixin;
    private LinearLayout mLlBaiduPan;
    private long mCurrentBackTime;
    private LinearLayout mLlPersonalFragment;
    private double mLastBackTime;
    public Fragment mCurFragment;
    ArrayList<FileInfo> mFileInfoArrayList = null;
    FileViewInteractionHub.CopyOrMove mCopyOrMove = null;
    private int mCurId;

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
        mLlPersonalFragment.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlVideos.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlPictures.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlDocument.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlDownload.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlMusic.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlRecycle.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlQqImage.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlQqFile.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlWeixin.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
        mLlBaiduPan.setOnGenericMotionListener(new MouseLinearOnGenericMotionListener());
    }


    @Override
    protected void initView() {
        mLlPersonalFragment = (LinearLayout) rootView.findViewById(R.id.ll_personal_fragment);
        mLlVideos = (LinearLayout) rootView.findViewById(R.id.ll_personal_videos);
        mLlPictures = (LinearLayout) rootView.findViewById(R.id.ll_personal_pictures);
        mLlDocument = (LinearLayout) rootView.findViewById(R.id.ll_personal_document);
        mLlDownload = (LinearLayout) rootView.findViewById(R.id.ll_personal_downloads);
        mLlMusic = (LinearLayout) rootView.findViewById(R.id.ll_personal_music);
        mLlRecycle = (LinearLayout) rootView.findViewById(R.id.ll_personal_recycle);
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
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.VIDEOS_PATH,
                                                           R.id.ll_personal_videos);
                break;
            case R.id.ll_personal_pictures:
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.PICTURES_PATH,
                                                           R.id.ll_personal_pictures);
                break;
            case R.id.ll_personal_document:
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.DOCUMENT_PATH,
                                                           R.id.ll_personal_document);
                break;
            case R.id.ll_personal_downloads:
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.DOWNLOAD_PATH,
                                                           R.id.ll_personal_downloads);
                break;
            case R.id.ll_personal_music:
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.MUSIC_PATH,
                                                           R.id.ll_personal_music);
                break;
            case R.id.ll_personal_recycle:
                setDiskClickInfo(Constants.LEFT_FAVORITES, Constants.RECYCLE_PATH,
                                                           R.id.ll_personal_recycle);
                break;
            case R.id.ll_personal_qq_image:
                setDiskClickInfo(Constants.LEFT_FAVORITES, QQ_IMAGE_PATH,
                                                           R.id.ll_personal_qq_image);
                break;
            case R.id.ll_personal_qq_file:
                setDiskClickInfo(Constants.LEFT_FAVORITES, QQ_FILE_PATH,
                                                           R.id.ll_personal_qq_file);
                break;
            case R.id.ll_personal_weixin:
                setDiskClickInfo(Constants.LEFT_FAVORITES, WEIXIN_IMG_PATH,
                                                           R.id.ll_personal_weixin);
                break;
            case R.id.ll_personal_baidudisk:
                setDiskClickInfo(Constants.LEFT_FAVORITES, BAIDU_PAN_PATH,
                                                           R.id.ll_personal_baidudisk);
                break;
            default:
                setItemBackGround(Constants.RETURN_TO_WHITE);
                break;
        }
    }

    private void setDiskClickInfo(String tag, String path, int id) {
        if (mCurrentBackTime - mLastBackTime > Constants.DOUBLE_CLICK_INTERVAL_TIME
                || id != mCurId) {
            setItemBackGround(id);
            mCurId = id;
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
            mCurId = Constants.RETURN_TO_WHITE;
        }
    }

    private void setItemBackGround(int id) {
        switch (id) {
            case  R.id.ll_personal_videos:
                mLlVideos.setSelected(true);
                mLlPictures.setSelected(false);
                mLlDocument.setSelected(false);
                mLlDownload.setSelected(false);
                mLlMusic.setSelected(false);
                mLlQqImage.setSelected(false);
                mLlQqFile.setSelected(false);
                mLlWeixin.setSelected(false);
                mLlBaiduPan.setSelected(false);
            break;
            case  R.id.ll_personal_pictures:
                mLlVideos.setSelected(false);
                mLlPictures.setSelected(true);
                mLlDocument.setSelected(false);
                mLlDownload.setSelected(false);
                mLlMusic.setSelected(false);
                mLlQqImage.setSelected(false);
                mLlQqFile.setSelected(false);
                mLlWeixin.setSelected(false);
                mLlBaiduPan.setSelected(false);
            break;
            case  R.id.ll_personal_document:
                mLlVideos.setSelected(false);
                mLlPictures.setSelected(false);
                mLlDocument.setSelected(true);
                mLlDownload.setSelected(false);
                mLlMusic.setSelected(false);
                mLlQqImage.setSelected(false);
                mLlQqFile.setSelected(false);
                mLlWeixin.setSelected(false);
                mLlBaiduPan.setSelected(false);
            break;
            case  R.id.ll_personal_downloads:
                mLlVideos.setSelected(false);
                mLlPictures.setSelected(false);
                mLlDocument.setSelected(false);
                mLlDownload.setSelected(true);
                mLlMusic.setSelected(false);
                mLlQqImage.setSelected(false);
                mLlQqFile.setSelected(false);
                mLlWeixin.setSelected(false);
                mLlBaiduPan.setSelected(false);
            break;
            case R.id.ll_personal_music :
                mLlVideos.setSelected(false);
                mLlPictures.setSelected(false);
                mLlDocument.setSelected(false);
                mLlDownload.setSelected(false);
                mLlMusic.setSelected(true);
                mLlQqImage.setSelected(false);
                mLlQqFile.setSelected(false);
                mLlWeixin.setSelected(false);
                mLlBaiduPan.setSelected(false);
            break;
            case  R.id.ll_personal_qq_image:
                mLlVideos.setSelected(false);
                mLlPictures.setSelected(false);
                mLlDocument.setSelected(false);
                mLlDownload.setSelected(false);
                mLlMusic.setSelected(false);
                mLlQqImage.setSelected(true);
                mLlQqFile.setSelected(false);
                mLlWeixin.setSelected(false);
                mLlBaiduPan.setSelected(false);
            break;
            case  R.id.ll_personal_qq_file:
                mLlVideos.setSelected(false);
                mLlPictures.setSelected(false);
                mLlDocument.setSelected(false);
                mLlDownload.setSelected(false);
                mLlMusic.setSelected(false);
                mLlQqImage.setSelected(false);
                mLlQqFile.setSelected(true);
                mLlWeixin.setSelected(false);
                mLlBaiduPan.setSelected(false);
            break;
            case  R.id.ll_personal_weixin:
                mLlVideos.setSelected(false);
                mLlPictures.setSelected(false);
                mLlDocument.setSelected(false);
                mLlDownload.setSelected(false);
                mLlMusic.setSelected(false);
                mLlQqImage.setSelected(false);
                mLlQqFile.setSelected(false);
                mLlWeixin.setSelected(true);
                mLlBaiduPan.setSelected(false);
                break;
            case  R.id.ll_personal_baidudisk:
                mLlVideos.setSelected(false);
                mLlPictures.setSelected(false);
                mLlDocument.setSelected(false);
                mLlDownload.setSelected(false);
                mLlMusic.setSelected(false);
                mLlQqImage.setSelected(false);
                mLlQqFile.setSelected(false);
                mLlWeixin.setSelected(false);
                mLlBaiduPan.setSelected(true);
                break;
            case  Constants.RETURN_TO_WHITE:
                mLlVideos.setSelected(false);
                mLlPictures.setSelected(false);
                mLlDocument.setSelected(false);
                mLlDownload.setSelected(false);
                mLlMusic.setSelected(false);
                mLlQqImage.setSelected(false);
                mLlQqFile.setSelected(false);
                mLlWeixin.setSelected(false);
                mLlBaiduPan.setSelected(false);
                break;
        }
    }
}
