package com.openthos.filemanager.fragment;

import android.os.Build;
import android.annotation.SuppressLint;
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
    public static List<String[]> usbLists = new ArrayList<>();

    ArrayList<FileInfo> mFileInfoArrayList = null;
    FileViewInteractionHub.CopyOrMove copyOrMove = null;

    private LinearLayout mAndroidSystem;
    private LinearLayout mSdSpace;
    private LinearLayout mAndroidService;
    private LinearLayout mPersonalSpace;
    private TextView mSystemTotal;
    private TextView mSystemAvail;
    private ProgressBar mPbSystem;
    private TextView mSdTotal;
    private TextView mSdAvail;
    private ProgressBar mPbSd;

    public BaseFragment mCurFragment;
    private long lastBackTime = 0;
    private long currentBackTime;
    private String mountDiskPath = null;
    private LinearLayout mFragmentSds;
    private LinearLayout mLlMobileDevice;
    private LinearLayout mUsbDevices;
    private String mCurrentPath;
    private String mLastPath;
    private LinearLayout[] mLinearlayouts;

    @SuppressLint({"NewApi", "ValidFragment"})
    public SdStorageFragment(FragmentManager mManager,
                             String usbDeviceIsAttached, MainActivity context) {
        super(mManager, usbDeviceIsAttached, context);
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

        mPbSystem = (ProgressBar) rootView.findViewById(R.id.pb_system);
        mPbSd = (ProgressBar) rootView.findViewById(R.id.pb_sd);
        mUsbDevices = ((LinearLayout) rootView.findViewById(R.id.ll_usb_device));
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
        if (null != systemInfo) {
            mSystemTotal.setText(Util.convertStorage(systemInfo.romMemory));
            mSystemAvail.setText(Util.convertStorage(systemInfo.avilMemory));
//            L.e("tv_system_total", Util.convertStorage(systemInfo.romMemory).substring(0, 3));
//            L.e("tv_system_avail", Util.convertStorage(systemInfo.avilMemory).substring(0, 3));
            mPbSystem.setMax((int) Double.parseDouble(Util.convertStorage(systemInfo.romMemory)
                    .substring(0, 3)) * 10);
            mPbSystem.setSecondaryProgress
                    ((int) (Double.parseDouble
                            (Util.convertStorage(systemInfo.romMemory - systemInfo.avilMemory)
                                    .substring(0, 3)) * 10));
        }

        String[] cmd = {"df"};
        String[] usbs = Util.execDisk(cmd);
        if (usbs != null && usbs.length > 0) {
            showDiskInfo(usbs);
        } else {
            showSdcardInfo();
        }
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
        }
    }

    protected void initData() {
        setVolumSize();
        if (usbDeviceIsAttached != null && usbDeviceIsAttached.equals("usb_device_attached")) {
            String[] cmd = {"df"};
            usbLists = Util.execUsb(cmd);
            int size = usbLists.size();
            if (size > 0) {
                mLlMobileDevice.setVisibility(View.VISIBLE);
                mMainActivity.mHandler.sendEmptyMessage(Constants.USB_READY);
                mUsbDevices.removeAllViews();
                for (int i = 0; i < usbLists.size(); i++) {
                    mUsbDevices.addView(getUsbView(usbLists.get(i)));
//                    mUsbDevices.addView(getPaddingView());
                }
            }
        } else if (usbDeviceIsAttached != null
                && usbDeviceIsAttached.equals("usb_device_detached")) {
            usbLists.clear();
            mUsbDevices.removeAllViews();
        }
    }

    @Override
    protected void initListener() {
        mFragmentSds.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
        mAndroidSystem.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
        mSdSpace.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
        mAndroidService.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
        mPersonalSpace.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
        mUsbDevices.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
    }

    public class MouseRelativeOnGenericMotionListener implements View.OnGenericMotionListener {
        @Override
        public boolean onGenericMotion(View v, MotionEvent event) {
            if (v.getId() != R.id.ll_usb_device){
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
        switch (view.getId()) {
            case R.id.rl_android_system:
                setDiskClickInfo(R.id.rl_android_system, Constants.SYSTEM_SPACE_FRAGMENT, null);
                break;
            case R.id.rl_sd_space:
                setDiskClickInfo(R.id.rl_sd_space, Constants.SD_SPACE_FRAGMENT,
                                 Constants.SD_PATH);
                break;
            case R.id.rl_android_service:
                setDiskClickInfo(R.id.rl_android_service, Constants.YUN_SPACE_FRAGMENT, null);
                break;
            case R.id.rl_personal_space:
                setDiskClickInfo(R.id.rl_personal_space, Constants.PERSONAL_TAG, null);
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
        if (mCurrentPath != null && mCurId == -1) {
            enter(Constants.USB_SPACE_FRAGMENT, mCurrentPath);
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
    }

    public void removeUsbView(int position) {
        usbLists.remove(position);
        mUsbDevices.removeViewAt(position);
        if (usbLists.size() == 0) {
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
            setUnselectAll();
            switch (motionEvent.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    view.setSelected(true);
                    currentBackTime = System.currentTimeMillis();
                    if (currentBackTime - lastBackTime > Constants.DOUBLE_CLICK_INTERVAL_TIME
                            || mCurrentPath != mLastPath) {
                        lastBackTime = currentBackTime;
                    } else {
                        enter(Constants.USB_SPACE_FRAGMENT, mCurrentPath);
                    }
                    mLastPath = mCurrentPath;
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    view.setSelected(true);
                    mLastPath = null;
                    showDiskDialog(view, motionEvent, true);
                    break;
            }
            return false;
        }
    }
}
