package com.openthos.filemanager.component;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;

public class PopWinShare extends PopupWindow {
    private static final String IV_SETTING_TAG = "iv_setting";
    private static final String IV_USB_TAG = "iv_usb";
    private static final String IV_MENU_TAG = "iv_menu";
    private static final String MOUNT_POPWINDOW_TAG = "MOUNT_POPWINDOW_TAG";

    private View mMainView;
    private TextView mPop_menu_refresh, mPop_menu_cancel_all, mPop_menu_copy,
                     mPop_menu_delete, mPop_menu_send, mPop_menu_create, mPop_menu_exit,
                     mPop_setting_view, mPop_usb_view, mPop_usb_info;
    private TextView mPopFormatUsb;
    private LinearLayout mLl_menu, mLl_setting, mLl_usb;
    private LinearLayout mLlMount;
    private TextView mPopMount, mPopUmount;


    public PopWinShare(MainActivity mainActivity, View.OnClickListener paramOnClickListener,
                       int paramInt1, int paramInt2, String menu_tag) {
        super(mainActivity);
        mMainView = LayoutInflater.from(mainActivity).inflate(R.layout.popwin_share, null);
        mLl_menu = (LinearLayout) mMainView.findViewById(R.id.ll_menu);
        mLl_setting = (LinearLayout) mMainView.findViewById(R.id.ll_setting);
        mLl_usb = (LinearLayout) mMainView.findViewById(R.id.ll_usb);
        mLlMount = (LinearLayout) mMainView.findViewById(R.id.ll_mount);
        if (IV_MENU_TAG.equals(menu_tag)) {
            mLl_setting.setVisibility(View.GONE);
            mLl_usb.setVisibility(View.GONE);
            mLl_menu.setVisibility(View.VISIBLE);
            mPop_menu_refresh = (TextView) mMainView.findViewById(R.id.pop_menu_refresh);
            mPop_menu_cancel_all = (TextView) mMainView.findViewById(R.id.pop_menu_cancel_all);
            mPop_menu_copy = (TextView) mMainView.findViewById(R.id.pop_menu_copy);
            mPop_menu_delete = (TextView) mMainView.findViewById(R.id.pop_menu_delete);
            mPop_menu_send = (TextView) mMainView.findViewById(R.id.pop_menu_send);
            mPop_menu_create = (TextView) mMainView.findViewById(R.id.pop_menu_create);
            mPop_menu_exit = (TextView) mMainView.findViewById(R.id.pop_menu_exit);
            if (paramOnClickListener != null) {
                mPop_menu_refresh.setOnClickListener(paramOnClickListener);
                mPop_menu_cancel_all.setOnClickListener(paramOnClickListener);
                mPop_menu_copy.setOnClickListener(paramOnClickListener);
                mPop_menu_delete.setOnClickListener(paramOnClickListener);
                mPop_menu_send.setOnClickListener(paramOnClickListener);
                mPop_menu_create.setOnClickListener(paramOnClickListener);
                mPop_menu_exit.setOnClickListener(paramOnClickListener);
            }
        } else if (IV_SETTING_TAG.equals(menu_tag)) {
            mLl_menu.setVisibility(View.GONE);
            mLl_usb.setVisibility(View.GONE);
            mLl_setting.setVisibility(View.VISIBLE);
            mPop_setting_view = (TextView) mMainView.findViewById(R.id.pop_setting_view);
            if (paramOnClickListener != null) {
                mPop_setting_view.setOnClickListener(paramOnClickListener);
            }
        } else if (IV_USB_TAG.equals(menu_tag)) {
            mLl_menu.setVisibility(View.GONE);
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
            mLl_menu.setVisibility(View.GONE);
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
    }
}
