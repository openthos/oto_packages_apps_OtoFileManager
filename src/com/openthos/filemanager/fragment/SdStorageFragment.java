package com.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.Volume;
import com.openthos.filemanager.component.DiskDialog;
import com.openthos.filemanager.drag.HomeGridView;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.T;
import com.openthos.filemanager.system.Constants;

import java.util.ArrayList;
import java.util.List;

public class SdStorageFragment extends BaseFragment {
    private ArrayList<FileInfo> mFileInfoArrayList = null;
    private FileViewInteractionHub.CopyOrMove copyOrMove = null;
    private LinearLayout mAndroidSystem;
    private LinearLayout mSdSpace;
    private LinearLayout mAndroidService;
    public LinearLayout mPersonalSpace;
    private TextView mSystemTotal;
    private TextView mSystemAvail;
    private ProgressBar mPbSystem;
    public BaseFragment mCurFragment;
    private long mCurrentTime = 0;
    private long mPreTime = 0;
    private LinearLayout mFragmentSds;
    private LinearLayout mLlMobileDevice;
    private HomeGridView mUsbDevices, mMountDevices, mLocalStorage, mFastAccess;
    private MotionEvent mLongPressEvent;
    private LinearLayout mLlMountDevice;
    private String mCurrentPath;
    private LinearLayout[] mLinearlayouts;
    private ArrayList<Volume> mVolumes;
    public ArrayList<String> mUsbLists;
    private int homeIndex = 0;
    private List<Integer> mHomeIdList = new ArrayList<>();
    private List<View> mHomeViewList = new ArrayList<>();
    public View mCurView;
    private HomeItemAdapter mUsbAdapter;
    private HomeOnTouchListener homeOnTouchListener;
    private List<View> mFastAccessViews, mLocatStorageViews, mMountViews, mUsbViews;

    @SuppressLint({"NewApi", "ValidFragment"})
    public SdStorageFragment(FragmentManager manager, MainActivity context) {
        mManager = manager;
        super.context = context;
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public SdStorageFragment() {
        super();
    }

    @Override
    public int getLayoutId() {
        return R.layout.android_fragment_layout;
    }

    @Override
    public void processDirectionKey(int keyCode) {
        for (View v : mHomeViewList) {
            v.setSelected(false);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP:
                homeIndex = homeIndex > 0 ? --homeIndex : homeIndex;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                homeIndex = homeIndex < mHomeViewList.size() - 1 ?
                        ++homeIndex : homeIndex;
                break;
        }
        mCurView = mHomeViewList.get(homeIndex);
        mCurView.requestFocus();
        mCurView.setSelected(true);
    }

    @Override
    protected void initView() {
        mFragmentSds = (LinearLayout) rootView.findViewById(R.id.fragment_sds_ll);
        mLlMobileDevice = (LinearLayout) rootView.findViewById(R.id.ll_mobile_device);
        mUsbDevices = ((HomeGridView) rootView.findViewById(R.id.grid_usb_device));
        mMountDevices = ((HomeGridView) rootView.findViewById(R.id.grid_auto_mount_device));
        mLlMountDevice = (LinearLayout) rootView.findViewById(R.id.ll_auto_mount);
        mLocalStorage = (HomeGridView) rootView.findViewById(R.id.grid_local_storage);
        mFastAccess = (HomeGridView) rootView.findViewById(R.id.grid_fast_access);

        View inflate = View.inflate(mMainActivity, R.layout.grid_home_item, null);
        mAndroidSystem = (LinearLayout) inflate.findViewById(R.id.rl_android_system);
        mSdSpace = (LinearLayout) inflate.findViewById(R.id.rl_sd_space);
        mPersonalSpace = (LinearLayout) inflate.findViewById(R.id.rl_personal_space);
        mAndroidService = (LinearLayout) inflate.findViewById(R.id.rl_android_service);
        mSystemTotal = (TextView) inflate.findViewById(R.id.tv_system_total);
        mSystemAvail = (TextView) inflate.findViewById(R.id.tv_system_avail);
        mPbSystem = (ProgressBar) inflate.findViewById(R.id.pb_system);
    }

    private void setVolumSize() {
        Util.SystemInfo systemInfo = Util.getRomMemory();
        Util.SDCardInfo sdCardInfo = Util.getSDCardInfo();
        if (null != systemInfo) {
            mSystemTotal.setText(Util.convertStorage(sdCardInfo.total));
            mSystemAvail.setText(Util.convertStorage(sdCardInfo.free));
            int maxOne = 1000;
            int progressOne = (int) ((sdCardInfo.total - sdCardInfo.free)
                    * 1000 / (sdCardInfo.total));
            mPbSystem.setMax(maxOne);
            mPbSystem.setProgress(progressOne);
        }
    }


    private void initFastAccessData() {
        mFastAccessViews = new ArrayList<>();
        mFastAccessViews.add(mPersonalSpace);
        mHomeIdList.add(R.id.rl_personal_space);
        mHomeViewList.add(mPersonalSpace);
        HomeItemAdapter adapter = new HomeItemAdapter(mFastAccessViews);
        mFastAccess.setAdapter(adapter);
    }

    private void initLocalStorageData() {
        mLocatStorageViews = new ArrayList<>();
        mLocatStorageViews.add(mAndroidSystem);
        mLocatStorageViews.add(mSdSpace);
        mHomeIdList.add(R.id.rl_android_system);
        mHomeViewList.add(mAndroidSystem);
        mHomeIdList.add(R.id.rl_sd_space);
        mHomeViewList.add(mSdSpace);
        HomeItemAdapter adapter = new HomeItemAdapter(mLocatStorageViews);
        mLocalStorage.setAdapter(adapter);
    }

    protected void initData() {
        initFastAccessData();
        initLocalStorageData();
        setVolumSize();
        initUsbData();
        initMountData();
        mLinearlayouts = new LinearLayout[]{
                mAndroidService, mSdSpace, mAndroidSystem, mPersonalSpace};
    }

    private void initUsbData() {
        mUsbViews = new ArrayList<>();
        mUsbLists = mMainActivity.getUsbLists();
        mUsbLists.clear();
        mUsbLists.addAll(Util.execUsb());
        if (mUsbLists.size() > 0) {
            mLlMobileDevice.setVisibility(View.VISIBLE);
            mMainActivity.mHandler.sendEmptyMessage(Constants.USB_READY);
            for (int i = 0; i < mUsbLists.size(); i++) {
                mUsbViews.add(getUsbView(mUsbLists.get(i)));
            }
            mUsbAdapter = new HomeItemAdapter(mUsbViews);
            mUsbDevices.setAdapter(mUsbAdapter);
        }
    }

    public void initMountData() {
        mVolumes = mMainActivity.getVolumes();
        boolean isShow = false;
        mMountViews = new ArrayList<>();
        for (Volume v : mVolumes) {
            if (v.isMount()) {
                isShow = true;
                mMountViews.add(getMountView(v));
            }
        }
        HomeItemAdapter autoMountAdapter = new HomeItemAdapter(mMountViews);
        mMountDevices.setAdapter(autoMountAdapter);
        if (isShow) {
            mMountDevices.setVisibility(View.VISIBLE);
            mLlMountDevice.setVisibility(View.VISIBLE);
        } else {
            mMountDevices.setVisibility(View.GONE);
            mLlMountDevice.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initListener() {
        homeOnTouchListener = new HomeOnTouchListener();
        mFragmentSds.setOnTouchListener(homeOnTouchListener);
        mUsbDevices.setOnTouchListener(homeOnTouchListener);
        mMountDevices.setOnTouchListener(homeOnTouchListener);
        mLocalStorage.setOnTouchListener(homeOnTouchListener);
        mFastAccess.setOnTouchListener(homeOnTouchListener);
    }

    private View getMountView(Volume v) {
        StatFs stat = new StatFs(v.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        long totalBlocks = stat.getBlockCount();
        View inflate = View.inflate(getActivity(), R.layout.mount_grid, null);
        LinearLayout mountLayout = (LinearLayout) inflate.findViewById(R.id.mount_grid_ll);
        ProgressBar diskResidue = (ProgressBar) inflate.findViewById(R.id.mount_grid_pb);
        TextView usbName = (TextView) inflate.findViewById(R.id.mount_grid_name);
        TextView totalSize = (TextView) inflate.findViewById(R.id.mount_grid_total_size);
        TextView availSize = (TextView) inflate.findViewById(R.id.mount_grid_available_size);
        totalSize.setText(Util.convertStorage(totalBlocks * blockSize));
        availSize.setText(Util.convertStorage(blockSize * availableBlocks));
        usbName.setText(v.getBlock());
        int maxOne = 1000;
        int progressOne = (int) ((totalBlocks * blockSize - blockSize * availableBlocks) * 1000
                / (totalBlocks * blockSize));
        diskResidue.setMax(maxOne);
        diskResidue.setProgress(progressOne);
        mountLayout.setTag(v);
        return mountLayout;
    }

    class HomeItemAdapter extends BaseAdapter {
        private List<View> datas;

        public HomeItemAdapter(List<View> views) {
            datas = views;
        }

        @Override
        public int getCount() {
            return datas == mUsbViews ? mUsbLists.size() : datas.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (datas == mUsbViews) {
                return getUsbView(mUsbLists.get(position));
            } else {
                datas.get(position).setOnTouchListener(homeOnTouchListener);
                return datas.get(position);
            }
        }
    }

    class HomeOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMainActivity.clearNivagateFocus();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mMainActivity.mCurTabIndext = 9;
                    if (mCurView != view) {
                        mCurView = view;
                        mPreTime = 0;
                    }
                    mLongPressEvent = motionEvent;
                    mMainActivity.mHandler.postDelayed(
                            mMainActivity.mLongPressRunnable,
                            Constants.LONG_PRESS_TIME);
                    mCurrentTime = System.currentTimeMillis();
                    checkView(view);
                    if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                        showMenu();
                        mCurrentTime = 0;
                    } else if (mCurrentTime - mPreTime < Constants.DOUBLE_CLICK_INTERVAL_TIME) {
                        enter();
                    }
                    mPreTime = mCurrentTime;
                    mCurrentTime = 0;
                    return true;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    mMainActivity.mHandler.removeCallbacks(mMainActivity.mLongPressRunnable);
                    break;
            }
            return false;
        }
    }

    public void checkView(View view) {
        mMainActivity.clearNivagateFocus();
        switch (view.getId()) {
            case R.id.rl_android_system:
            case R.id.rl_sd_space:
            case R.id.rl_android_service:
            case R.id.rl_personal_space:
                setSelectedCardBg(view.getId());
                break;
            case R.id.mount_grid_ll:
                if (!view.isSelected()) {
                    setUnselectAll();
                    view.setSelected(true);
                }
                break;
            case R.id.usb_grid_ll:
                mCurrentPath = (String) view.getTag();
                mMainActivity.setUsbPath(mCurrentPath);
                if (!view.isSelected()) {
                    setUnselectAll();
                    view.setSelected(true);
                }
                break;
            default:
                setUnselectAll();
                mCurView = null;
                break;
        }
    }

    public void uninstallUmount() {
        for (int i = 0; i < mMountDevices.getChildCount(); i++) {
            if (mMountDevices.getChildAt(i).isSelected()) {
                Volume v = (Volume) mMountDevices.getChildAt(i).getTag();
                mMainActivity.umountVolume(v);
                return;
            }
        }
    }

    private void showDiskDialog(MotionEvent event, String tag) {
        DiskDialog diskDialog = new DiskDialog(context, tag);
        diskDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        diskDialog.showDialog((int) event.getRawX(), (int) event.getRawY());
    }


    @Override
    public void enter() {
        super.enter();
        mMainActivity.mHandler.removeCallbacks(mMainActivity.mLongPressRunnable);
        if (mCurView != null) {
            switch (mCurView.getId()) {
                case R.id.rl_android_system:
                    enter(Constants.SYSTEM_SPACE_FRAGMENT, Constants.SDCARD_PATH);
                    break;
                case R.id.rl_sd_space:
                    enter(Constants.SD_SPACE_FRAGMENT, Constants.ROOT_PATH + "storage");
                    break;
                case R.id.rl_android_service:
                    enter(Constants.YUN_SPACE_FRAGMENT, null);
                    break;
                case R.id.rl_personal_space:
                    enter(Constants.PERSONAL_TAG, null);
                    break;
                case R.id.mount_grid_ll:
                    for (int i = 0; i < mMountDevices.getChildCount(); i++) {
                        if (mMountDevices.getChildAt(i).isSelected()) {
                            Volume v = (Volume) mMountDevices.getChildAt(i).getTag();
                            mMainActivity.enter(v);
                            break;
                        }
                    }
                    break;
                case R.id.usb_grid_ll:
                    mMainActivity.enter(mCurrentPath);
                    mCurrentPath = null;
                    break;
            }
        }
    }

    public void uninstallUSB() {
        mMainActivity.uninstallUSB(mCurrentPath);
    }

    @Override
    protected void enter(String tag, String path) {
        if (mCurFragment != null) {
            if (mCurFragment instanceof SystemSpaceFragment) {
                mFileInfoArrayList = ((SystemSpaceFragment) mCurFragment).getFileInfoList();
                copyOrMove = ((SystemSpaceFragment) mCurFragment).getCurCopyOrMoveMode();
            }
        }
        if (mFileInfoArrayList != null && copyOrMove != null) {
            T.showShort(context,
                    context.getString(R.string.operation_failed_permission_refuse));
        }
        FragmentTransaction transaction = mManager.beginTransaction();
        transaction.hide(mMainActivity.mCurFragment);
        if (Constants.PERSONAL_TAG.equals(tag)) {
            mMainActivity.setNavigationPath(null);
            transaction.show(mMainActivity.mPersonalSpaceFragment).commitAllowingStateLoss();
            mCurFragment = mMainActivity.mPersonalSpaceFragment;
        } else if (Constants.YUN_SPACE_FRAGMENT.equals(tag)) {
            mMainActivity.setNavigationPath(null);
            transaction.show(mMainActivity.mSeafileFragment).commitAllowingStateLoss();
            mCurFragment = mMainActivity.mSeafileFragment;
        } else {
            mCurFragment = new SystemSpaceFragment(tag, path,
                    mFileInfoArrayList, copyOrMove, false);
            transaction.add(R.id.fl_mian, mCurFragment,
                    Constants.SDSSYSTEMSPACE_TAG).commitAllowingStateLoss();
            ((SystemSpaceFragment) mCurFragment).mPos = 0;
            mMainActivity.setNavigationPath(Util.getDisplayPath(mMainActivity, path));
        }
        mMainActivity.mCurFragment = mCurFragment;
        mCurView = null;
        mMainActivity.mUserOperationFragments.add(mMainActivity.mCurFragment);
        mMainActivity.mFragmentIndex++;
        mMainActivity.mIv_back.setImageDrawable(
                mMainActivity.getDrawable(R.mipmap.backward_enable));
        mMainActivity.mIv_up.setImageDrawable(mMainActivity.getDrawable(R.mipmap.up_enable));
    }

    public void setSelectedCardBg(int id) {
        setUnselectAll();
        switch (id) {
            case R.id.rl_android_system:
                mAndroidSystem.setSelected(true);
                break;
            case R.id.rl_sd_space:
                mSdSpace.setSelected(true);
                break;
            case R.id.rl_android_service:
                mAndroidService.setSelected(true);
                break;
            case R.id.rl_personal_space:
                mPersonalSpace.setSelected(true);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalCache.setSearchText(null);
    }

    public boolean canGoBack() {
        boolean canGoBack = false;
        Fragment baseFragment = mCurFragment;
        if (baseFragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) baseFragment;
            canGoBack = systemSpaceFragment.canGoBack();
        }
        return canGoBack;
    }

    public void goBack() {
        Fragment baseFragment = mCurFragment;
        if (baseFragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) baseFragment;
            systemSpaceFragment.goBack();
        }
    }

    public void setUnselectAll() {
        if (mLinearlayouts != null) {
            for (int i = 0; i < mLinearlayouts.length; i++) {
                mLinearlayouts[i].setSelected(false);
            }
        }
        if (mUsbDevices != null) {
            for (int i = 0; i < mUsbDevices.getChildCount(); i++) {
                ((View) mUsbDevices.getChildAt(i).getTag()).setSelected(false);
            }
        }
        for (int i = 0; i < mMountDevices.getChildCount(); i++) {
            mMountDevices.getChildAt(i).setSelected(false);
        }
    }

    public void removeUsbView(int position) {
        mUsbLists.remove(position);
        mUsbAdapter.notifyDataSetChanged();
        if (mUsbLists.size() == 0) {
            mLlMobileDevice.setVisibility(View.GONE);
        }
    }

    private View getUsbView(String usbData) {
        View inflate = View.inflate(getActivity(), R.layout.usb_grid, null);
        LinearLayout usbLayout = (LinearLayout) inflate.findViewById(R.id.usb_grid_ll);
        ProgressBar diskResidue = (ProgressBar) inflate.findViewById(R.id.usb_grid_pb);
        TextView usbName = (TextView) inflate.findViewById(R.id.usb_grid_name);
        TextView totalSize = (TextView) inflate.findViewById(R.id.usb_grid_total_size);
        TextView availSize = (TextView) inflate.findViewById(R.id.usb_grid_available_size);
        Util.UsbMemoryInfo usbInfo = Util.getUsbMemoryInfo(usbData);
        totalSize.setText(Util.convertStorage(usbInfo.usbTotal));
        availSize.setText(Util.convertStorage(usbInfo.usbFree));
        usbName.setText(Util.getUsbName(getActivity(), usbData));
        int maxOne = 1000;
        int progressOne = (int) ((usbInfo.usbTotal - usbInfo.usbFree)
                * 1000 / (usbInfo.usbTotal));
        diskResidue.setMax(maxOne);
        diskResidue.setProgress(progressOne);
        inflate.setTag(usbLayout);
        usbLayout.setTag(usbData);
        usbLayout.setOnTouchListener(homeOnTouchListener);
        return inflate;
    }

    @Override
    public void showMenu() {
        if (mCurView != null) {
            switch (mCurView.getId()) {
                case R.id.usb_grid_ll:
                    showDiskDialog(mLongPressEvent, Constants.TAG_USB);
                    break;
                case R.id.mount_grid_ll:
                    showDiskDialog(mLongPressEvent, Constants.TAG_AUTO_MOUNT);
                    break;
                default:
                    showDiskDialog(mLongPressEvent, Constants.TAG_SYSTEM);
                    break;
            }
        }
    }
}
