package com.openthos.filemanager.fragment;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.DeskAdapter;
import com.openthos.filemanager.bean.AppInfo;
import com.openthos.filemanager.utils.L;

import java.util.ArrayList;
import java.util.List;

public class DeskFragment extends BaseFragment {
    private ArrayList<AppInfo> appInfos = new ArrayList<>();
    private String packageName;
    private DeskAdapter deskAdapter;
    private GridView gv_desk_icon;
    private PackageManager pm;

    @Override
    protected void initView() {
        gv_desk_icon = (GridView) rootView.findViewById(R.id.gv_desk_icon);
    }

    protected void initData() {
        if (appInfos != null){
            appInfos.clear();
        }
        getInstallPackageInfo();
        deskAdapter = new DeskAdapter(appInfos, getActivity());
        gv_desk_icon.setAdapter(deskAdapter);
    }

    @Override
    protected void initListener() {
        gv_desk_icon.setOnGenericMotionListener(new DeskOnGenericMotionListener());
    }

    @Override
    public int getLayoutId() {
        return R.layout.desk_fragment_layout;
    }

    private void getInstallPackageInfo() {
        pm = getActivity().getPackageManager();
        List<PackageInfo> packages
                          = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for(int i = 0; i< packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            AppInfo appInfo =new AppInfo();
            String appName = packageInfo.applicationInfo
                                        .loadLabel(getActivity().getPackageManager()).toString();
            appInfo.setAppName(appName);
            Drawable appIcon = packageInfo.applicationInfo
                                          .loadIcon(getActivity().getPackageManager());
            appInfo.setIcon(appIcon);
            String packageName = packageInfo.packageName;
            appInfo.setPackageName(packageName);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appInfos.add(appInfo);
            }
        }
    }

    private class DeskOnGenericMotionListener implements View.OnGenericMotionListener {
        @Override
        public boolean onGenericMotion(View view, MotionEvent event) {
            switch (event.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    IconItemClickListener();
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    IconUninstallItemClickListener();
                    break;
                case MotionEvent.BUTTON_TERTIARY:
                    IconItemClickListener();
                    break;
                case MotionEvent.ACTION_SCROLL:
                    MouseScrollAction(event);
                    break;
            }
            return false;
        }
    }

    private void IconItemClickListener() {
        gv_desk_icon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                packageName = appInfos.get(i).getPackageName();
                Intent intent = pm.getLaunchIntentForPackage(packageName);
                if (null != intent){
                    startActivity(intent);
                }
            }
        });
    }

    private void IconUninstallItemClickListener() {
        gv_desk_icon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                packageName = appInfos.get(i).getPackageName();
                uninstallAPK(packageName);
            }
        });
    }

    private void MouseScrollAction(MotionEvent event) {
        if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
            L.i("fortest::onGenericMotionEvent", "down");
//            T.showShort(getContext(), getContext.getString(R.string.scroll_down));
        }
        else {
            L.i("fortest::onGenericMotionEvent", "up");
//            T.showShort(getContext(), getContext.getString(R.string.scroll_up));
        }
    }

    private void uninstallAPK(String packageName){
        Uri uri=Uri.parse("package:"+packageName);
        Intent intent=new Intent(Intent.ACTION_DELETE,uri);
        startActivityForResult(intent,0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0){
            deskAdapter.notifyDataSetChanged();
            initData();
        }
    }

    @Override
    public boolean canGoBack() {
        return false;
    }

    @Override
    public void goBack() {
    }

    @Override
    protected void enter(String tag, String path) {
    }

    @Override
    public void processDirectionKey(int keyCode) {
    }

    @Override
    public void showMenu() {
    }
}
