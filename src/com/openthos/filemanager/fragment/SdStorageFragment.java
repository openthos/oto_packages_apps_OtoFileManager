package com.openthos.filemanager.fragment;

import android.os.Build;
import android.annotation.SuppressLint;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.Volume;
import com.openthos.filemanager.component.DiskDialog;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.T;
import com.openthos.filemanager.system.Constants;

import java.util.ArrayList;
import java.util.List;

public class SdStorageFragment extends BaseFragment {
    private static final String TAG = SdStorageFragment.class.getSimpleName();

    ArrayList<FileInfo> mFileInfoArrayList = null;
    FileViewInteractionHub.CopyOrMove copyOrMove = null;

    private LinearLayout mAndroidSystem;
    private LinearLayout mSdSpace;
    private LinearLayout mAndroidService;
    private LinearLayout mPersonalSpace;
    private TextView mSystemTotal;
    private TextView mSystemAvail;
    private LinearLayout mSdInfo;
    private ProgressBar mPbSystem;
    private TextView mSdTotal;
    private TextView mSdAvail;
    private ProgressBar mPbSd;
    private View mMountView;

    public BaseFragment mCurFragment;
    private long lastBackTime = 0;
    private long currentBackTime;
    private String mountDiskPath = null;
    private LinearLayout mFragmentSds;
    private LinearLayout mLlMobileDevice;
    private LinearLayout mUsbDevices;
    private LinearLayout mMountDevices;
    private LinearLayout mLlMountDevice;
    private String mCurrentPath;
    private String mLastPath;
    private LinearLayout[] mLinearlayouts;
    private ArrayList<Volume> mVolumes;
    public ArrayList<String[]> mUsbLists;
    private MouseRelativeOnGenericMotionListener mTouchListener
                                     = new MouseRelativeOnGenericMotionListener();

    @SuppressLint({"NewApi", "ValidFragment"})
    public SdStorageFragment(FragmentManager manager,
                             ArrayList<String[]> usbLists, MainActivity context) {
        mManager = manager;
        mUsbLists = usbLists;
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
    protected void initView() {
        mFragmentSds = (LinearLayout) rootView.findViewById(R.id.fragment_sds_ll);
        mAndroidSystem = (LinearLayout) rootView.findViewById(R.id.rl_android_system);
        mSdSpace = (LinearLayout) rootView.findViewById(R.id.rl_sd_space);
        mAndroidService = (LinearLayout) rootView.findViewById(R.id.rl_android_service);
        mPersonalSpace = (LinearLayout) rootView.findViewById(R.id.rl_personal_space);
        mLlMobileDevice = (LinearLayout) rootView.findViewById(R.id.ll_mobile_device);

        mSystemTotal = (TextView) rootView.findViewById(R.id.tv_system_total);
        mSystemAvail = (TextView) rootView.findViewById(R.id.tv_system_avail);
        mSdTotal = (TextView) rootView.findViewById(R.id.tv_sd_total);
        mSdAvail = (TextView) rootView.findViewById(R.id.tv_sd_avail);
        mSdInfo = (LinearLayout) rootView.findViewById(R.id.tv_sd_info);
        mSdInfo.setVisibility(View.GONE);
        mPbSystem = (ProgressBar) rootView.findViewById(R.id.pb_system);
        mPbSd = (ProgressBar) rootView.findViewById(R.id.pb_sd);
        mUsbDevices = ((LinearLayout) rootView.findViewById(R.id.ll_usb_device));
        mMountDevices = ((LinearLayout) rootView.findViewById(R.id.ll_auto_mount_device));
        mLlMountDevice = (LinearLayout) rootView.findViewById(R.id.ll_auto_mount);
        mLinearlayouts = new LinearLayout[]{
                mAndroidService, mSdSpace, mAndroidSystem, mPersonalSpace};
        if (Build.TYPE.equals("eng")) {
            mSdSpace.setVisibility(View.VISIBLE);
        } else {
            mSdSpace.setVisibility(View.INVISIBLE);
        }
    }

    private void setVolumSize() {
        Util.SystemInfo systemInfo = Util.getRomMemory();
        Util.SDCardInfo sdCardInfo = Util.getSDCardInfo();
        if (null != systemInfo) {
            mSystemTotal.setText(Util.convertStorage(sdCardInfo.total));
            mSystemAvail.setText(Util.convertStorage(sdCardInfo.free));
            mPbSystem.setMax((int) Double.parseDouble
                    (Util.convertStorage(sdCardInfo.total).substring(0, 3)) * 10);
            mPbSystem.setProgress((int) (Double.parseDouble
                    (Util.convertStorage(sdCardInfo.total - sdCardInfo.free)
                            .substring(0, 3)) * 10));
        }
        showSdcardInfo();
    }

    private void showDiskInfo(String[] usbs) {
        mountDiskPath = usbs[0];
        mSdTotal.setText(usbs[1]);
        mSdAvail.setText(usbs[3]);
        int max = (int) Double.parseDouble(usbs[1].substring(0, 3)) * 10;
        int avail = (int) Double.parseDouble(usbs[3].substring(0, 2)) * 10;
        mPbSd.setMax(max);
        mPbSd.setProgress(max - avail);
    }

    private void showSdcardInfo() {
        Util.SDCardInfo sdCardInfo = Util.getSDCardInfo();
        if (null != sdCardInfo) {
            mSdTotal.setText(Util.convertStorage(sdCardInfo.total));
            mSdAvail.setText(Util.convertStorage(sdCardInfo.free));

            L.e("tv_sd_total", Util.convertStorage(sdCardInfo.total).substring(0, 3));
            L.e("tv_sd_avail", Util.convertStorage(sdCardInfo.free).substring(0, 3));

            mPbSd.setMax((int) Double.parseDouble
                    (Util.convertStorage(sdCardInfo.total).substring(0, 3)) * 10);
            mPbSd.setProgress((int) (Double.parseDouble
                    (Util.convertStorage(sdCardInfo.total - sdCardInfo.free)
                            .substring(0, 3)) * 10));
            mSdTotal.setVisibility(View.GONE);
            mSdAvail.setVisibility(View.GONE);
            mPbSd.setVisibility(View.GONE);
        }
    }

    protected void initData() {
        setVolumSize();
        initUsbData();
        mVolumes = mMainActivity.getVolumes();
        initMountData();
    }

    private void initUsbData() {
        String[] cmd = {"df"};
        mUsbLists.clear();
        mUsbLists.addAll(Util.execUsb(cmd));
        if (mUsbLists.size() > 0) {
            mLlMobileDevice.setVisibility(View.VISIBLE);
            mMainActivity.mHandler.sendEmptyMessage(Constants.USB_READY);
            mUsbDevices.removeAllViews();
            for (int i = 0; i < mUsbLists.size(); i++) {
                mUsbDevices.addView(getUsbView(mUsbLists.get(i)));
            }
        }
    }

    public void initMountData() {
        boolean isShow = false;
        mMountDevices.removeAllViews();
        for (Volume v : mVolumes) {
            if (v.isMount()) {
                isShow = true;
                StatFs stat = new StatFs(v.getPath());
                long blockSize = stat.getBlockSize();
                long availableBlocks = stat.getAvailableBlocks();
                long totalBlocks = stat.getBlockCount();
                String[] s = new String[]{v.getBlock(),
                                          Util.convertStorage(blockSize * availableBlocks),
                                          Util.convertStorage(totalBlocks * blockSize)};
                mMountDevices.addView(getMountView(s, v));
            }
        }
        if (isShow) {
            mMountDevices.setVisibility(View.VISIBLE);
            mLlMountDevice.setVisibility(View.VISIBLE);

        } else {
            mMountDevices.setVisibility(View.GONE);
            mLlMountDevice.setVisibility(View.GONE);
        }
    }

    private View getMountView(String[] mountInfo, Volume v) {
        View inflate = View.inflate(getActivity(), R.layout.mount_grid, null);
        LinearLayout mountLayout = (LinearLayout) inflate.findViewById(R.id.mount_grid_ll);
        ProgressBar diskResidue = (ProgressBar) inflate.findViewById(R.id.mount_grid_pb);
        TextView usbName = (TextView) inflate.findViewById(R.id.mount_grid_name);
        TextView totalSize = (TextView) inflate.findViewById(R.id.mount_grid_total_size);
        TextView availSize = (TextView) inflate.findViewById(R.id.mount_grid_available_size);
        totalSize.setText(mountInfo[2]);
        availSize.setText(mountInfo[1]);
        usbName.setText(mountInfo[0]);
        int maxOne = (int) (Double.parseDouble(mountInfo[1].substring(0, 3)) * 100);
        int availOne = (int) (Double.parseDouble(mountInfo[2].substring(0, 3)) * 100);
        int progressOne = maxOne - availOne >= 0 ?
                maxOne - availOne : maxOne - (availOne / 1024);
        diskResidue.setMax(maxOne);
        diskResidue.setProgress(progressOne);
        inflate.setTag(v);
        inflate.setOnGenericMotionListener(mTouchListener);
        return inflate;
    }

    @Override
    protected void initListener() {
        mFragmentSds.setOnGenericMotionListener(mTouchListener);
        mAndroidSystem.setOnGenericMotionListener(mTouchListener);
        mSdSpace.setOnGenericMotionListener(mTouchListener);
        mAndroidService.setOnGenericMotionListener(mTouchListener);
        mPersonalSpace.setOnGenericMotionListener(mTouchListener);
        mUsbDevices.setOnGenericMotionListener(mTouchListener);

        SdOnTouchListener sdOnTouchListener = new SdOnTouchListener();
        mAndroidSystem.setOnTouchListener(sdOnTouchListener);
        mSdSpace.setOnTouchListener(sdOnTouchListener);
        mAndroidService.setOnTouchListener(sdOnTouchListener);
        mPersonalSpace.setOnTouchListener(sdOnTouchListener);
    }



    private class SdOnTouchListener implements View.OnTouchListener {
        private long lastTime;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (motionEvent.getButtonState() == MotionEvent.BUTTON_PRIMARY) {
                        lastTime = System.currentTimeMillis();
                        mIsUsb = false;
                        mLongPressView = view;
                        mLongPressEvent = motionEvent;
                        mMainActivity.mHandler.postDelayed(mMainActivity.mLongPressRunnable,
                                                           Constants.LONG_PRESS_TIME);
                    } else {
                        lastTime = -1;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (lastTime != -1 &&
                            System.currentTimeMillis() - lastTime < Constants.LONG_PRESS_TIME) {
                        mMainActivity.mHandler.removeCallbacks(mMainActivity.mLongPressRunnable);
                    }
                    break;
            }
            return true;
        }
    }

    public class MouseRelativeOnGenericMotionListener implements View.OnGenericMotionListener {
        @Override
        public boolean onGenericMotion(View v, MotionEvent event) {
            if (v.getId() != R.id.ll_usb_device) {
                mLastPath = null;
            }
            switch (event.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    mMainActivity.clearNivagateFocus();
                    primaryClick(v);
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    mMainActivity.clearNivagateFocus();
                    secondaryClick(v, event);
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
        currentBackTime = System.currentTimeMillis();
        if (view.getId() != R.id.mount_grid) {
            mMountView = null;
        }
        switch (view.getId()) {
            case R.id.rl_android_system:
                setDiskClickInfo(R.id.rl_android_system, Constants.SYSTEM_SPACE_FRAGMENT, null);
                break;
            case R.id.rl_sd_space:
                setDiskClickInfo(R.id.rl_sd_space, Constants.SD_SPACE_FRAGMENT,
                                 Constants.SD_PATH+"storage/");
                break;
            case R.id.rl_android_service:
                setDiskClickInfo(R.id.rl_android_service, Constants.YUN_SPACE_FRAGMENT, null);
                break;
            case R.id.rl_personal_space:
                setDiskClickInfo(R.id.rl_personal_space, Constants.PERSONAL_TAG, null);
                break;
            case R.id.mount_grid:
                setUnselectAll();
                view.setSelected(true);
                mCurId = view.getId();
                currentBackTime = System.currentTimeMillis();
                if (currentBackTime - lastBackTime > Constants.DOUBLE_CLICK_INTERVAL_TIME
                        || view != mMountView) {
                    lastBackTime = currentBackTime;
                } else {
                    mMainActivity.enter((Volume) view.getTag());
                }
                mMountView = view;
                break;
            default:
                setSelectedCardBg(Constants.RETURN_TO_WHITE);
                mCurId = -1;
                break;
        }
    }

    public void secondaryClick(View view, MotionEvent event) {
        mCurId = -1;
        primaryClick(view);
        switch (view.getId()) {
            case R.id.ll_usb_device:
            case R.id.fragment_sds_ll:
            case R.id.mount_grid:
                break;
            default:
                showDiskDialog(view, event, false);
                break;
        }
    }

    private void showDiskDialog(View view, MotionEvent event, boolean isUSB) {
        DiskDialog diskDialog = new DiskDialog(context, isUSB, view);
        diskDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        diskDialog.showDialog((int) event.getRawX(), (int) event.getRawY());
    }

    private void setDiskClickInfo(int id, String tag, String path) {
        if (currentBackTime - lastBackTime > Constants.DOUBLE_CLICK_INTERVAL_TIME
                 || id != mCurId) {
            setSelectedCardBg(id);
            mCurId = id;
            lastBackTime = currentBackTime;
        } else {
            enter(tag, path);
        }
    }

    @Override
    public void enter() {
        super.enter();
        switch (mCurId) {
            case R.id.mount_grid:
                for (int i = 0; i < mMountDevices.getChildCount(); i++) {
                    if (mMountDevices.getChildAt(i).isSelected()) {
                        Volume v = (Volume) mMountDevices.getChildAt(i).getTag();
                        mMainActivity.enter(v);
                        return;
                    }
                }
                break;
        }
        if (mCurrentPath != null && mCurId == -1) {
            mMainActivity.enter(mCurrentPath);
            mCurrentPath = null;
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
            transaction.show(mMainActivity.mPersonalSpaceFragment).commit();
            mCurFragment = mMainActivity.mPersonalSpaceFragment;
        } else if (Constants.YUN_SPACE_FRAGMENT.equals(tag)) {
            mMainActivity.setNavigationPath(null);
            transaction.show(mMainActivity.mSeafileFragment).commit();
            mCurFragment = mMainActivity.mSeafileFragment;
        } else {
            mCurFragment = new SystemSpaceFragment(tag, path,
                                                   mFileInfoArrayList, copyOrMove, false);
            transaction.add(R.id.fl_mian, mCurFragment, Constants.SDSSYSTEMSPACE_TAG).commit();
        }
        mMainActivity.mCurFragment = mCurFragment;
        mCurId = Constants.RETURN_TO_WHITE;
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
            case Constants.RETURN_TO_WHITE:
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
       //else {
       //    PersonalSpaceFragment personalSpaceFragment = (PersonalSpaceFragment) baseFragment;
       //    canGoBack = personalSpaceFragment.canGoBack();
       //}
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
        mUsbDevices.removeViewAt(position);
        if (mUsbLists.size() == 0) {
            mLlMobileDevice.setVisibility(View.GONE);
        }
    }

    private View getUsbView(String[] usbData) {
        View inflate = View.inflate(getActivity(), R.layout.usb_grid, null);
        LinearLayout usbLayout = (LinearLayout) inflate.findViewById(R.id.usb_grid_ll);
        ProgressBar diskResidue = (ProgressBar) inflate.findViewById(R.id.usb_grid_pb);
        TextView usbName = (TextView) inflate.findViewById(R.id.usb_grid_name);
        TextView totalSize = (TextView) inflate.findViewById(R.id.usb_grid_total_size);
        TextView availSize = (TextView) inflate.findViewById(R.id.usb_grid_available_size);
        totalSize.setText(usbData[1]);
        availSize.setText(usbData[3]);
        usbName.setText(Util.getUsbName(getActivity(), usbData[0]));
        int maxOne = (int) (Double.parseDouble(usbData[1].substring(0, 3)) * 100);
        int availOne = (int) (Double.parseDouble(usbData[3].substring(0, 3)) * 100);
        int progressOne = maxOne - availOne >= 0 ?
                maxOne - availOne : maxOne - (availOne / 1024);
        diskResidue.setMax(maxOne);
        diskResidue.setProgress(progressOne);
        inflate.setTag(usbLayout);
        usbLayout.setTag(usbData[0]);
        usbLayout.setOnTouchListener(new UsbTouchListener());
        return inflate;
    }

    private class UsbTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCurId = -1;
            mCurrentPath = (String) view.getTag();
            mMainActivity.clearNivagateFocus();
            if (!view.isSelected()) {
                setUnselectAll();
                view.setSelected(true);
            }
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    switch (motionEvent.getButtonState()) {
                        case MotionEvent.BUTTON_PRIMARY:
                            currentBackTime = System.currentTimeMillis();
                            if (currentBackTime - lastBackTime > Constants.DOUBLE_CLICK_INTERVAL_TIME
                                    || mCurrentPath != mLastPath) {
                                lastBackTime = currentBackTime;
                                mIsUsb = true;
                                mLongPressView = view;
                                mLongPressEvent = motionEvent;
                                mMainActivity.mHandler.postDelayed(mMainActivity.mLongPressRunnable,
                                                                   Constants.LONG_PRESS_TIME);
                            } else {
                                mMainActivity.enter(mCurrentPath);
                            }
                            mLastPath = mCurrentPath;
                            break;
                        case MotionEvent.BUTTON_SECONDARY:
                            lastBackTime = -1;
                            mLastPath = null;
                            showDiskDialog(view, motionEvent, true);
                            break;
                        default:
                            lastBackTime = -1;
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (lastBackTime != -1 &&
                            System.currentTimeMillis() - lastBackTime < Constants.LONG_PRESS_TIME) {
                        mMainActivity.mHandler.removeCallbacks(mMainActivity.mLongPressRunnable);
                    } else {
                        mLastPath = null;
                        lastBackTime = -1;
                    }
                    break;
            }
            return true;
        }
    }

    @Override
    public void processDirectionKey(int keyCode) {
    }

    private View mLongPressView;
    private MotionEvent mLongPressEvent;
    private boolean mIsUsb;

    @Override
    public void showMenu() {
        if (mIsUsb) {
            showDiskDialog(mLongPressView, mLongPressEvent, true);
        } else {
            secondaryClick(mLongPressView, mLongPressEvent);
        }
    }
}
