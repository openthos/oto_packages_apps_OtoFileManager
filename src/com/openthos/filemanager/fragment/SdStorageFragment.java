package com.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.component.DiskDialog;
import com.openthos.filemanager.component.MenuFirstDialog;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.T;
import com.openthos.filemanager.fragment.SystemSpaceFragment;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.component.DiskDialog;

import java.io.File;
import java.util.ArrayList;

public class SdStorageFragment extends BaseFragment {
    private static final String TAG = SdStorageFragment.class.getSimpleName();
//    private String usbDeviceIsAttached;

    ArrayList<FileInfo> mFileInfoArrayList = null;
    FileViewInteractionHub.CopyOrMove copyOrMove = null;

    private RelativeLayout mRl_android_system;
    private RelativeLayout mRl_sd_space;
    private RelativeLayout mRl_android_service;
    private RelativeLayout mRl_mount_space_one;
    private RelativeLayout mRl_mount_space_two;
    private RelativeLayout mRl_mount_space_three;
    private RelativeLayout mRl_personal_space;
    private TextView tv_system_total;
    private TextView tv_system_avail;
    private ProgressBar pb_system;
    private TextView tv_sd_total;
    private TextView tv_sd_avail;
    private ProgressBar pb_sd;
    private TextView tv_usb_total_one;
    private TextView tv_usb_avail_one;
    private ProgressBar pb_usb_one;
    private TextView tv_usb_total_two;
    private TextView tv_usb_avail_two;
    private ProgressBar pb_usb_two;
    private TextView tv_usb_total_three;
    private TextView tv_usb_avail_three;
    private ProgressBar pb_usb_three;

    public BaseFragment mCurFragment;
//    FragmentmManager mManager = getFragmentmManager();
    private long lastBackTime = 0;
    private ArrayList<File> mountUsb = null;
    private String mountPathOne;
    private String mountPathTwo;
    private String mountPathThree;
    private long currentBackTime;
    private String mountDiskPath = null;
//    private Context context;
    private LinearLayout mFragment_sds_ll;
    private LinearLayout mLlMobileDevice;

    @SuppressLint({"NewApi", "ValidFragment"})
    public SdStorageFragment(FragmentManager mManager,
                             String usbDeviceIsAttached, MainActivity context) {
        super(mManager,usbDeviceIsAttached,context);
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
        mFragment_sds_ll = (LinearLayout) rootView.findViewById(R.id.fragment_sds_ll);
        mRl_android_system = (RelativeLayout) rootView.findViewById(R.id.rl_android_system);
        mRl_sd_space = (RelativeLayout) rootView.findViewById(R.id.rl_sd_space);
        mRl_android_service = (RelativeLayout) rootView.findViewById(R.id.rl_android_service);
        mRl_mount_space_one = (RelativeLayout) rootView.findViewById(R.id.rl_mount_space_one);
        mRl_mount_space_two = (RelativeLayout) rootView.findViewById(R.id.rl_mount_space_two);
        mRl_mount_space_three = (RelativeLayout) rootView.findViewById(R.id.rl_mount_space_three);
        mRl_personal_space = (RelativeLayout) rootView.findViewById(R.id.rl_personal_space);
        mLlMobileDevice = (LinearLayout) rootView.findViewById(R.id.ll_mobile_device);

        tv_system_total = (TextView) rootView.findViewById(R.id.tv_system_total);
        tv_system_avail = (TextView) rootView.findViewById(R.id.tv_system_avail);
        tv_sd_total = (TextView) rootView.findViewById(R.id.tv_sd_total);
        tv_sd_avail = (TextView) rootView.findViewById(R.id.tv_sd_avail);
        tv_usb_total_one = (TextView) rootView.findViewById(R.id.tv_usb_total_one);
        tv_usb_avail_one = (TextView) rootView.findViewById(R.id.tv_usb_avail_one);
        tv_usb_total_two = (TextView) rootView.findViewById(R.id.tv_usb_total_two);
        tv_usb_avail_two = (TextView) rootView.findViewById(R.id.tv_usb_avail_two);
        tv_usb_total_three = (TextView) rootView.findViewById(R.id.tv_usb_total_three);
        tv_usb_avail_three = (TextView) rootView.findViewById(R.id.tv_usb_avail_three);

        pb_system = (ProgressBar) rootView.findViewById(R.id.pb_system);
        pb_sd = (ProgressBar) rootView.findViewById(R.id.pb_sd);
        pb_usb_one = (ProgressBar) rootView.findViewById(R.id.pb_usb_one);
        pb_usb_two = (ProgressBar) rootView.findViewById(R.id.pb_usb_two);
        pb_usb_three = (ProgressBar) rootView.findViewById(R.id.pb_usb_three);
    }

    public void hideMountSpaceOne() {
        if (mRl_mount_space_one != null) {
            mRl_mount_space_one.setVisibility(View.GONE);
        }
    }

    public void hideMountSpaceTwo() {
        if (mRl_mount_space_two != null) {
            mRl_mount_space_two.setVisibility(View.GONE);
        }
    }

    public void hideMountSpaceThree() {
        if (mRl_mount_space_three != null) {
            mRl_mount_space_three.setVisibility(View.GONE);
        }
    }

    private void setVolumSize() {
        Util.SystemInfo systemInfo = Util.getRomMemory();
        if (null != systemInfo) {
            tv_system_total.setText(Util.convertStorage(systemInfo.romMemory));
            tv_system_avail.setText(Util.convertStorage(systemInfo.avilMemory));
//            L.e("tv_system_total", Util.convertStorage(systemInfo.romMemory).substring(0, 3));
//            L.e("tv_system_avail", Util.convertStorage(systemInfo.avilMemory).substring(0, 3));
            pb_system.setMax((int) Double.parseDouble(Util.convertStorage(systemInfo.romMemory)
                                                          .substring(0, 3)) * 10);
            pb_system.setSecondaryProgress
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
        tv_sd_total.setText(usbs[1]);
        tv_sd_avail.setText(usbs[3]);
        int max = (int) Double.parseDouble(usbs[1].substring(0, 3)) * 10;
        int avail = (int) Double.parseDouble(usbs[3].substring(0, 2)) * 10;
        pb_sd.setMax(max);
        pb_sd.setProgress(max - avail);
    }

    private void showSdcardInfo() {
        Util.SDCardInfo sdCardInfo = Util.getSDCardInfo();
        if (null != sdCardInfo) {
            tv_sd_total.setText(Util.convertStorage(sdCardInfo.total));
            tv_sd_avail.setText(Util.convertStorage(sdCardInfo.free));

            L.e("tv_sd_total", Util.convertStorage(sdCardInfo.total).substring(0, 3));
            L.e("tv_sd_avail", Util.convertStorage(sdCardInfo.free).substring(0, 3));

            pb_sd.setMax((int) Double.parseDouble
                               (Util.convertStorage(sdCardInfo.total).substring(0, 3)) * 10);
            pb_sd.setProgress((int) (Double.parseDouble
                                    (Util.convertStorage(sdCardInfo.total - sdCardInfo.free)
                                         .substring(0, 3)) * 10));
        }
    }

    protected void initData() {
        setVolumSize();
        if (usbDeviceIsAttached != null && usbDeviceIsAttached.equals("usb_device_attached")) {
            String[] cmd = {"df"};
            ArrayList<String[]> list = Util.execUsb(cmd);
            if (list.size() >= 1) {
                String[] usb1 = list.get(0);
                mountPathOne = usb1[0];
                if (usb1 != null && usb1.length > 0) {
                    showMountDevices(usb1, 1);
                    mLlMobileDevice.setVisibility(View.VISIBLE);
                    mRl_mount_space_one.setVisibility(View.VISIBLE);
                    mRl_mount_space_one.setOnGenericMotionListener
                            (new MouseRelativeOnGenericMotionListener());
                    mMainActivity.mHandler.sendEmptyMessage(Constants.USB1_READY);
                    if (list.size() >= 2) {
                        String[] usb2 = list.get(1);
                        mountPathTwo = usb2[0];
                        if (usb2 != null && usb2.length > 0) {
                            showMountDevices(usb2, 2);
                            mRl_mount_space_two.setVisibility(View.VISIBLE);
                            mRl_mount_space_two.setOnGenericMotionListener
                                    (new MouseRelativeOnGenericMotionListener());
                            mMainActivity.mHandler.sendEmptyMessage(Constants.USB2_READY);
                           // if (list.size() >= 3) {
                           //     String[] usb3 = list.get(2);
                           //     mountPathThree = usb3[0];
                           //     if (usb3 != null && usb3.length > 0) {
                           //         showMountDevices(usb3, 3);
                           //         mRl_mount_space_three.setVisibility(View.VISIBLE);
                           //         mRl_mount_space_three.setOnGenericMotionListener
                           //                 (new MouseRelativeOnGenericMotionListener());
                           //         mMainActivity.mHandler.sendEmptyMessage(Constants.USB3_READY);
                           //     }
                           // }
                        }
                    }
                }
            }
        } else if (usbDeviceIsAttached != null
                   && usbDeviceIsAttached.equals("usb_device_detached")) {
            mLlMobileDevice.setVisibility(View.GONE);
            mRl_mount_space_one.setVisibility(View.GONE);
            mRl_mount_space_two.setVisibility(View.GONE);
            mRl_mount_space_three.setVisibility(View.GONE);
            T.showShort(mMainActivity, getResources().getString(R.string.USB_device_disconnected));
        }
    }

    @Override
    protected void initListener() {
        mFragment_sds_ll.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
        mRl_android_system.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
        mRl_sd_space.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
        mRl_android_service.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
        mRl_personal_space.setOnGenericMotionListener(new MouseRelativeOnGenericMotionListener());
    }

    private class MouseRelativeOnGenericMotionListener implements View.OnGenericMotionListener {
        @Override
        public boolean onGenericMotion(View v, MotionEvent event) {
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
//            return false;
            return true;
        }
    }

    private void showMountDevices(String[] usbs, int flag) {
        switch (flag) {
            case 1:
                tv_usb_total_one.setText(usbs[1]);
                tv_usb_avail_one.setText(usbs[3]);
                int maxOne = (int) (Double.parseDouble(usbs[1].substring(0, 3)) * 100);
                int availOne = (int) (Double.parseDouble(usbs[3].substring(0, 3)) * 100);
                int progressOne = maxOne - availOne >= 0?
                                  maxOne - availOne : maxOne -  (availOne / 1024);
                pb_usb_one.setMax(maxOne);
                pb_usb_one.setProgress(progressOne);
                break;
            case 2:
                tv_usb_total_two.setText(usbs[1]);
                tv_usb_avail_two.setText(usbs[3]);
                int maxTwo = (int) (Double.parseDouble(usbs[1].substring(0, 3)) * 100);
                int availTwo = (int) (Double.parseDouble(usbs[3].substring(0, 3)) * 100);
                int progressTwo = maxTwo - availTwo >= 0?
                                  maxTwo - availTwo : maxTwo -  (availTwo / 1024);
                pb_usb_two.setMax(maxTwo);
                pb_usb_two.setProgress(progressTwo);
                break;
            case 3:
                tv_usb_total_three.setText(usbs[1]);
                tv_usb_avail_three.setText(usbs[3]);
                int maxThree = (int) (Double.parseDouble(usbs[1].substring(0, 3)) * 100);
                int availThree = (int) (Double.parseDouble(usbs[3].substring(0, 3)) * 100);
                int progressThree = maxThree - availThree >= 0?
                                    maxThree - availThree : maxThree - (availThree / 1024);
                pb_usb_three.setMax(maxThree);
                pb_usb_three.setProgress(progressThree);
                break;
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
            case R.id.rl_mount_space_one:
                setDiskClickInfo(R.id.rl_mount_space_one, Constants.USB_SPACE_FRAGMENT,
                                 mountPathOne);
                break;
            case R.id.rl_mount_space_two:
                setDiskClickInfo(R.id.rl_mount_space_two, Constants.USB_SPACE_FRAGMENT,
                                 mountPathTwo);
                break;
            case R.id.rl_mount_space_three:
                setDiskClickInfo(R.id.rl_mount_space_three, Constants.USB_SPACE_FRAGMENT,
                                 mountPathThree);
                break;
            case R.id.rl_android_service:
                setDiskClickInfo(R.id.rl_android_service, Constants.YUN_SPACE_FRAGMENT, null);
                break;
            case R.id.rl_personal_space:
                setDiskClickInfo(R.id.rl_personal_space, Constants.PERSONAL_TAG, null);
                break;
            default:
                setSelectedCardBg(Constants.RETURN_TO_WHITE);
                break;
        }
    }

    public void secondaryClick(View view, MotionEvent event) {
        primaryClick(view);
        switch (view.getId()) {
            case R.id.rl_mount_space_one:
                showDiskDialog(view, event, true);
                break;
            case R.id.rl_mount_space_two:
                showDiskDialog(view, event, true);
                break;
            case R.id.rl_mount_space_three:
                showDiskDialog(view, event, true);
                break;
            case R.id.rl_personal_space:
            case R.id.rl_android_system:
            case R.id.rl_sd_space:
            case R.id.rl_android_service:
                showDiskDialog(view, event,false);
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
        if (mCurId == R.id.rl_mount_space_one) {
            enter(Constants.USB_SPACE_FRAGMENT, mountPathOne);
        } else if (mCurId == R.id.rl_mount_space_two) {
            enter(Constants.USB_SPACE_FRAGMENT, mountPathTwo);
        } else if (mCurId == R.id.rl_mount_space_three) {
            enter(Constants.USB_SPACE_FRAGMENT, mountPathThree);
        }
    }

   public void uninstallUSB() {
        switch (mCurId) {
            case R.id.rl_mount_space_one:
                mMainActivity.uninstallUSB(Constants.USB_ONE);
                break;
            case R.id.rl_mount_space_two:
                mMainActivity.uninstallUSB(Constants.USB_TWO);
                break;
            case R.id.rl_mount_space_three:
                mMainActivity.uninstallUSB(Constants.USB_THREE);
                break;
            default:
                break;
        }
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
            mMainActivity.setNavigationPath("SDCard");
            transaction.show(mMainActivity.mPersonalSpaceFragment).commit();
            mCurFragment = mMainActivity.mPersonalSpaceFragment;
        } else {
            mCurFragment = new SystemSpaceFragment(tag, path,
                                                   mFileInfoArrayList, copyOrMove, false);
            transaction.add(R.id.fl_mian, mCurFragment, Constants.SDSSYSTEMSPACE_TAG).commit();
        }
        mMainActivity.mCurFragment = mCurFragment;
        mCurId = Constants.RETURN_TO_WHITE;
    }

    public void setSelectedCardBg(int id) {
        switch (id) {
            case R.id.rl_android_system:
                mRl_android_system.setSelected(true);
                mRl_sd_space.setSelected(false);
                mRl_mount_space_one.setSelected(false);
                mRl_mount_space_two.setSelected(false);
                mRl_mount_space_three.setSelected(false);
                mRl_android_service.setSelected(false);
                mRl_personal_space.setSelected(false);
                break;
            case R.id.rl_sd_space:
                mRl_android_system.setSelected(false);
                mRl_sd_space.setSelected(true);
                mRl_mount_space_one.setSelected(false);
                mRl_mount_space_two.setSelected(false);
                mRl_mount_space_three.setSelected(false);
                mRl_android_service.setSelected(false);
                mRl_personal_space.setSelected(false);
                break;
            case R.id.rl_mount_space_one:
                mRl_android_system.setSelected(false);
                mRl_sd_space.setSelected(false);
                mRl_mount_space_one.setSelected(true);
                mRl_mount_space_two.setSelected(false);
                mRl_mount_space_three.setSelected(false);
                mRl_android_service.setSelected(false);
                mRl_personal_space.setSelected(false);
                break;
            case R.id.rl_mount_space_two:
                mRl_android_system.setSelected(false);
                mRl_sd_space.setSelected(false);
                mRl_mount_space_one.setSelected(false);
                mRl_mount_space_two.setSelected(true);
                mRl_mount_space_three.setSelected(false);
                mRl_android_service.setSelected(false);
                mRl_personal_space.setSelected(false);
                break;
            case R.id.rl_mount_space_three:
                mRl_android_system.setSelected(false);
                mRl_sd_space.setSelected(false);
                mRl_mount_space_one.setSelected(false);
                mRl_mount_space_two.setSelected(false);
                mRl_mount_space_three.setSelected(true);
                mRl_android_service.setSelected(false);
                mRl_personal_space.setSelected(false);
                break;
            case R.id.rl_android_service:
                mRl_android_system.setSelected(false);
                mRl_sd_space.setSelected(false);
                mRl_mount_space_one.setSelected(false);
                mRl_mount_space_two.setSelected(false);
                mRl_mount_space_three.setSelected(false);
                mRl_android_service.setSelected(true);
                mRl_personal_space.setSelected(false);
                break;
            case R.id.rl_personal_space:
                mRl_android_system.setSelected(false);
                mRl_sd_space.setSelected(false);
                mRl_mount_space_one.setSelected(false);
                mRl_mount_space_two.setSelected(false);
                mRl_mount_space_three.setSelected(false);
                mRl_android_service.setSelected(false);
                mRl_personal_space.setSelected(true);
                break;
            case Constants.RETURN_TO_WHITE:
                if (mRl_android_system != null) {
                    mRl_android_system.setSelected(false);
                    mRl_sd_space.setSelected(false);
                    mRl_mount_space_one.setSelected(false);
                    mRl_mount_space_two.setSelected(false);
                    mRl_mount_space_three.setSelected(false);
                    mRl_android_service.setSelected(false);
                    mRl_personal_space.setSelected(false);
                }
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
       // else {
       //     PersonalSpaceFragment personalSpaceFragment = (PersonalSpaceFragment) baseFragment;
       //     personalSpaceFragment.goBack();
       // }
    }
}
