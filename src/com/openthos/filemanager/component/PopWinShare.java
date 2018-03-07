package com.openthos.filemanager.component;

import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;

import java.util.ArrayList;
import java.util.List;
public class PopWinShare extends PopupWindow {
    private static final String IV_SETTING_TAG = "iv_setting";
    private static final String IV_USB_TAG = "iv_usb";
    private static final String MOUNT_POPWINDOW_TAG = "MOUNT_POPWINDOW_TAG";

    private View mMainView;
    private TextView mPop_setting_view, mPop_cloud_view, mPop_usb_view, mPop_usb_info;
    private TextView mPopFormatUsb;
    private LinearLayout  mLl_setting, mLl_usb;
    private LinearLayout mLlMount;
    private TextView mPopMount, mPopUmount;
    private List<LinearLayout> mContainers;

    public PopWinShare(final MainActivity mainActivity, View.OnClickListener paramOnClickListener,
                       int paramInt1, int paramInt2, String menu_tag) {
        super(mainActivity);
        mMainView = LayoutInflater.from(mainActivity).inflate(R.layout.popwin_share, null);
        mLl_setting = (LinearLayout) mMainView.findViewById(R.id.ll_setting);
        mLl_usb = (LinearLayout) mMainView.findViewById(R.id.ll_usb);
        mLlMount = (LinearLayout) mMainView.findViewById(R.id.ll_mount);
        mContainers = new ArrayList<>();
        mContainers.add(mLl_setting);
        mContainers.add(mLl_usb);
        mContainers.add(mLlMount);
        if (IV_SETTING_TAG.equals(menu_tag)) {
            mLl_usb.setVisibility(View.GONE);
            mLl_setting.setVisibility(View.VISIBLE);
            mPop_setting_view = (TextView) mMainView.findViewById(R.id.pop_setting_view);
            mPop_cloud_view = (TextView) mMainView.findViewById(R.id.pop_cloud_view);
            if (paramOnClickListener != null) {
                mPop_setting_view.setOnClickListener(paramOnClickListener);
                mPop_cloud_view.setOnClickListener(paramOnClickListener);
            }
        } else if (IV_USB_TAG.equals(menu_tag)) {
            mLl_setting.setVisibility(View.GONE);
            mLl_usb.setVisibility(View.VISIBLE);
            mPop_usb_view = (TextView) mMainView.findViewById(R.id.pop_usb_view);
            mPop_usb_info = (TextView) mMainView.findViewById(R.id.pop_usb_info);
            mPopFormatUsb = (TextView) mMainView.findViewById(R.id.pop_usb_format);
            if (paramOnClickListener != null) {
                mPop_usb_view.setOnClickListener(paramOnClickListener);
                mPop_usb_info.setOnClickListener(paramOnClickListener);
                mPopFormatUsb.setOnClickListener(paramOnClickListener);
            }
        }  else if (MOUNT_POPWINDOW_TAG.equals(menu_tag)) {
            mLlMount.setVisibility(View.VISIBLE);
            mLl_setting.setVisibility(View.GONE);
            mLl_usb.setVisibility(View.GONE);
            mPopMount = (TextView) mMainView.findViewById(R.id.pop_mount);
            mPopUmount = (TextView) mMainView.findViewById(R.id.pop_umount);
            if (paramOnClickListener != null) {
                mPopMount.setOnClickListener(paramOnClickListener);
                mPopUmount.setOnClickListener(paramOnClickListener);
            }
        }
        setContentView(mMainView);
        setWidth(paramInt1);
        setHeight(paramInt2);
        setAnimationStyle(R.style.AnimTools);
        setBackgroundDrawable(new ColorDrawable());
        mMainView.setOnKeyListener(new View.OnKeyListener() {
            int currentIndex = -1;
            int childCount;
            View currentChild;

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_TAB) {
                    return false;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (currentChild != null) {
                        currentChild.setBackground(mainActivity.getResources()
                                .getDrawable(android.R.color.transparent));
                    }
                    LinearLayout view = null;
                    for (LinearLayout container : mContainers) {
                        if (container.getVisibility() == View.VISIBLE) {
                            view = container;
                            break;
                        }
                    }
                    if (view != null) {
                        childCount = view.getChildCount();
                        switch (event.getKeyCode()) {
                            case KeyEvent.KEYCODE_DPAD_LEFT:
                            case KeyEvent.KEYCODE_DPAD_UP:
                                if (currentIndex != -1) {
                                    view.getChildAt(currentIndex > 0 ?
                                            --currentIndex : currentIndex)
                                            .setBackground(mainActivity.getResources()
                                                    .getDrawable(android.R.color.holo_purple));
                                }
                                break;
                            case KeyEvent.KEYCODE_DPAD_RIGHT:
                            case KeyEvent.KEYCODE_DPAD_DOWN:
                                view.getChildAt(currentIndex < childCount - 1 ?
                                    ++currentIndex : currentIndex).setBackground(mainActivity
                                        .getResources().getDrawable(android.R.color.holo_purple));
                                break;
                            case KeyEvent.KEYCODE_ENTER:
                            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                                if (currentChild != null) {
                                    currentChild.performClick();
                                }
                                break;
                        }
                        currentChild = view.getChildAt(currentIndex);
                    }
                }
                return false;
            }
        });
        mMainView.setFocusableInTouchMode(true);
    }
}
