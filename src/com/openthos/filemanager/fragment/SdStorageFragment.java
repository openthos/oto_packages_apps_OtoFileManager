package com.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.T;
import com.openthos.filemanager.view.SystemSpaceFragment;

import java.io.File;
import java.util.ArrayList;

public class SdStorageFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = SdStorageFragment.class.getSimpleName();
    private String usbDeviceIsAttached;

    private static final String SYSTEM_SPACE_FRAGMENT = "system_space_fragment";
    private static final String SD_SPACE_FRAGMENT = "sd_space_fragment";
    private static final String USB_SPACE_FRAGMENT = "usb_space_fragment";
    private static final String YUN_SPACE_FRAGMENT = "yun_space_fragment";

    private static final String SYSTEM_SPACE_FRAGMENT_TAG = "System_Space_Fragment_tag";
    ArrayList<FileInfo> fileInfoArrayList = null;
    FileViewInteractionHub.CopyOrMove copyOrMove = null;

    private RelativeLayout rl_android_system;
    private RelativeLayout rl_sd_space;
    private RelativeLayout rl_android_service;
    private RelativeLayout rl_mount_space_one;
    private RelativeLayout rl_mount_space_two;
    private TextView tv_system_total;
    private TextView tv_system_avail;
    private ProgressBar pb_system;
    private TextView tv_sd_total;
    private TextView tv_sd_avail;
    private ProgressBar pb_sd;
    private TextView tv_usb_total;
    private TextView tv_usb_avail;
    private ProgressBar pb_usb;
    private ProgressBar pb_service;

    private BaseFragment curFragment;
    FragmentManager manager = getFragmentManager();
    private long lastBackTime = 0;
    private ArrayList<File> mountUsb = null;
    private String mountPath;
    private long currentBackTime;
    private String mountDiskPath = null;
    private Context context;

    @SuppressLint({"NewApi", "ValidFragment"})
    public SdStorageFragment(FragmentManager manager,
                             String usbDeviceIsAttached, MainActivity context) {
        this.manager = manager;
        this.usbDeviceIsAttached = usbDeviceIsAttached;
        this.context = context;
    }
    @SuppressLint({"NewApi", "ValidFragment"})
    public SdStorageFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.android_fragment_layout, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        rl_android_system = (RelativeLayout) view.findViewById(R.id.rl_android_system);
        rl_sd_space = (RelativeLayout) view.findViewById(R.id.rl_sd_space);
        rl_android_service = (RelativeLayout) view.findViewById(R.id.rl_android_service);
        rl_mount_space_one = (RelativeLayout) view.findViewById(R.id.rl_mount_space_one);
        rl_mount_space_two = (RelativeLayout) view.findViewById(R.id.rl_mount_space_two);

        tv_system_total = (TextView) view.findViewById(R.id.tv_system_total);
        tv_system_avail = (TextView) view.findViewById(R.id.tv_system_avail);
        tv_sd_total = (TextView) view.findViewById(R.id.tv_sd_total);
        tv_sd_avail = (TextView) view.findViewById(R.id.tv_sd_avail);
        tv_usb_total = (TextView) view.findViewById(R.id.tv_usb_total);
        tv_usb_avail = (TextView) view.findViewById(R.id.tv_usb_avail);

        pb_system = (ProgressBar) view.findViewById(R.id.pb_system);
        pb_sd = (ProgressBar) view.findViewById(R.id.pb_sd);
        pb_usb = (ProgressBar) view.findViewById(R.id.pb_usb);
        pb_service = (ProgressBar) view.findViewById(R.id.pb_service);
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

    private void initData() {
        setVolumSize();
        rl_android_system.setOnClickListener(this);
        rl_sd_space.setOnClickListener(this);
        rl_android_service.setOnClickListener(this);

        if (usbDeviceIsAttached != null && usbDeviceIsAttached.equals("usb_device_attached")) {
            String[] cmd = {"df"};
            String[] usbs = Util.execUsb(cmd);
            if (usbs != null && usbs.length > 0) {
                showMountDevices(usbs);
                rl_mount_space_one.setVisibility(View.VISIBLE);
                rl_mount_space_one.setOnClickListener(this);
            }
        } else if (usbDeviceIsAttached != null
                   && usbDeviceIsAttached.equals("usb_device_detached")) {
            rl_mount_space_one.setVisibility(View.GONE);
        }
    }

    private void showMountDevices(String[] usbs) {
        mountPath = usbs[0];
        tv_usb_total.setText(usbs[1]);
        tv_usb_avail.setText(usbs[3]);
        int max = (int) Double.parseDouble(usbs[1].substring(0, 3)) * 10;
        int avail = (int) Double.parseDouble(usbs[3].substring(0, 3)) * 10;
        pb_usb.setMax(max);
        pb_usb.setProgress(max - avail);
    }

    @Override
    public void onClick(View view) {
        currentBackTime = System.currentTimeMillis();
        switch (view.getId()) {
            case R.id.rl_android_system:
                setDiskClickInfo(R.id.rl_android_system, SYSTEM_SPACE_FRAGMENT, null);
                break;
            case R.id.rl_sd_space:
                setDiskClickInfo(R.id.rl_sd_space, SD_SPACE_FRAGMENT,mountDiskPath);
                break;
            case R.id.rl_mount_space_one:
                setDiskClickInfo(R.id.rl_mount_space_one, USB_SPACE_FRAGMENT, mountPath);
                break;
            case R.id.rl_android_service:
                setDiskClickInfo(R.id.rl_android_service, YUN_SPACE_FRAGMENT, null);
                break;
            default:
                break;
        }
    }

    private void setDiskClickInfo(int id, String tag, String path) {
        if (currentBackTime - lastBackTime > 800) {
            setSelectedCardBg(id);
            lastBackTime = currentBackTime;
        } else {
            if (curFragment != null) {
                fileInfoArrayList = ((SystemSpaceFragment) curFragment).getFileInfoList();
                copyOrMove = ((SystemSpaceFragment) curFragment).getCurCopyOrMoveMode();
            }
            if (fileInfoArrayList != null && copyOrMove != null) {
                T.showShort(context,
                            context.getString(R.string.operation_failed_permission_refuse));
            }
            curFragment = new SystemSpaceFragment(tag, path, fileInfoArrayList, copyOrMove);
            manager.beginTransaction().replace(R.id.fl_mian, curFragment, SYSTEM_SPACE_FRAGMENT_TAG)
                    .addToBackStack(null).commit();
        }
    }

    private void setSelectedCardBg(int id) {
        switch (id) {
            case R.id.rl_android_system:
                rl_android_system.setSelected(true);
                rl_sd_space.setSelected(false);
                rl_mount_space_one.setSelected(false);
                rl_android_service.setSelected(false);
                break;
            case R.id.rl_sd_space:
                rl_android_system.setSelected(false);
                rl_sd_space.setSelected(true);
                rl_mount_space_one.setSelected(false);
                rl_android_service.setSelected(false);
                break;
            case R.id.rl_mount_space_one:
                rl_android_system.setSelected(false);
                rl_sd_space.setSelected(false);
                rl_mount_space_one.setSelected(true);
                rl_android_service.setSelected(false);
                break;
            case R.id.rl_android_service:
                rl_android_system.setSelected(false);
                rl_sd_space.setSelected(false);
                rl_mount_space_one.setSelected(false);
                rl_android_service.setSelected(true);
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
        Fragment baseFragment = curFragment;
        if (baseFragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) baseFragment;
            canGoBack = systemSpaceFragment.canGoBack();
        }
        return canGoBack;
    }

    public void goBack() {
        Fragment baseFragment = curFragment;
        if (baseFragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) baseFragment;
            systemSpaceFragment.goBack();
        }
    }
}
