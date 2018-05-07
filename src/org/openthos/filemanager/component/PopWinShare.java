package org.openthos.filemanager.component;

import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.R;
import org.openthos.filemanager.utils.SambaUtils;

public class PopWinShare extends PopupWindow {

    private MainActivity mMainActivity;
    private String mMenuTag;
    private View mMainView;
    private LinearLayout mShownLayout;
    private View.OnClickListener mOnClickListener;
    private final int SETTING_POPWINDOW_X = -15;
    private final int SETTING_POPWINDOW_Y = 10;
    private final int LEFT_VIEW_POPWINDOW_POP_X = 60;
    private final int LEFT_VIEW_POPWINDOW_POP_Y = 5;

    public PopWinShare(final MainActivity mainActivity, View.OnClickListener paramOnClickListener,
                       int paramInt1, int paramInt2, String menu_tag) {
        super(mainActivity);
        mMainActivity = mainActivity;
        mMenuTag = menu_tag;
        mOnClickListener = paramOnClickListener;
        mMainView = LayoutInflater.from(mainActivity).inflate(R.layout.popwin_share, null);
        mMainView.setFocusableInTouchMode(true);
        setContentView(mMainView);
        setWidth(paramInt1);
        setHeight(paramInt2);
        setAnimationStyle(R.style.AnimTools);
        setBackgroundDrawable(new ColorDrawable());
        initView();
        initListener();
    }

    private void initView() {
        switch (mMenuTag) {
            case MainActivity.SETTING_POPWINDOW_TAG:
                mShownLayout = (LinearLayout) mMainView.findViewById(R.id.ll_setting);
                TextView mPop_share_toggle = (TextView) mMainView.findViewById(R.id.pop_share_toggle);
                // judge smb is open ?
                mPop_share_toggle.setText(SambaUtils.SAMBA_RUNNING_FILE.exists()
                        ? mMainActivity.getString(R.string.operation_stop_share)
                        : mMainActivity.getString(R.string.operation_open_share));
                break;
            case MainActivity.COLLECTION_ITEM_TAG:
                mShownLayout = (LinearLayout) mMainView.findViewById(R.id.ll_collection);
                break;
            case MainActivity.USB_POPWINDOW_TAG:
                mShownLayout = (LinearLayout) mMainView.findViewById(R.id.ll_usb);
                break;
            case MainActivity.MOUNT_POPWINDOW_TAG:
                mShownLayout = (LinearLayout) mMainView.findViewById(R.id.ll_mount);
                break;
        }
        mShownLayout.setVisibility(View.VISIBLE);
    }

    private void initListener() {
        for (int i = 0; i < mShownLayout.getChildCount(); i++) {
            mShownLayout.getChildAt(i).setOnClickListener(mOnClickListener);
        }

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
                        currentChild.setBackground(mMainActivity.getResources()
                                .getDrawable(android.R.color.transparent));
                    }

                    if (mShownLayout != null) {
                        childCount = mShownLayout.getChildCount();
                        switch (event.getKeyCode()) {
                            case KeyEvent.KEYCODE_DPAD_LEFT:
                            case KeyEvent.KEYCODE_DPAD_UP:
                                if (currentIndex != -1) {
                                    mShownLayout.getChildAt(currentIndex > 0 ?
                                            --currentIndex : currentIndex)
                                            .setBackground(mMainActivity.getResources()
                                                    .getDrawable(android.R.color.holo_purple));
                                }
                                break;
                            case KeyEvent.KEYCODE_DPAD_RIGHT:
                            case KeyEvent.KEYCODE_DPAD_DOWN:
                                mShownLayout.getChildAt(currentIndex < childCount - 1 ?
                                        ++currentIndex : currentIndex).setBackground(mMainActivity
                                        .getResources().getDrawable(android.R.color.holo_purple));
                                break;
                            case KeyEvent.KEYCODE_ENTER:
                            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                                if (currentChild != null) {
                                    currentChild.performClick();
                                }
                                break;
                        }
                        currentChild = mShownLayout.getChildAt(currentIndex);
                    }
                }
                return false;
            }
        });

        mMainView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    dismiss();
                }
            }
        });
    }

    @Override
    public void showAsDropDown(View anchor) {
        setFocusable(true);
        if (mMenuTag.equals(MainActivity.SETTING_POPWINDOW_TAG)) {
            showAsDropDown(anchor, SETTING_POPWINDOW_X, SETTING_POPWINDOW_Y);
        } else {
            showAsDropDown(anchor, LEFT_VIEW_POPWINDOW_POP_X, LEFT_VIEW_POPWINDOW_POP_Y);
        }
        update();
    }
}
