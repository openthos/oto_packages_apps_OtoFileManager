package com.openthos.filemanager.component;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.utils.T;

public class PopOnClickLintener implements View.OnClickListener {
    private static final String VIEW_OR_DISMISS = "view_or_dismiss";
    private static final String SETTING_POPWINDOW_TAG = "iv_setting";
    private String mMenu_tag;
    private MainActivity mMainActivity;
    private FragmentManager mManager;

    public PopOnClickLintener(String menu_tag, MainActivity mainActivity, FragmentManager manager) {
        this.mMenu_tag = menu_tag;
        this.mMainActivity = mainActivity;
        this.mManager = manager;
    }

    @Override
    public void onClick(View view) {
        if (SETTING_POPWINDOW_TAG.equals(mMenu_tag)) {
            switch (view.getId()) {
                case R.id.pop_setting_view:
                    if (mManager.getBackStackEntryCount() < 1) {
                        T.showShort(mMainActivity,
                                    mMainActivity.getString(R.string.operation_not_support));
                    }
                    sendBroadcastMessage("iv_menu", VIEW_OR_DISMISS);
                    mMainActivity.DismissPopwindow();
                    break;
                default:
                    break;
            }
        }
    }

    private void sendBroadcastMessage(String name, String tag) {
        Intent intent = new Intent();
        if (name.equals("iv_menu")) {
            intent.setAction("com.switchmenu");
            intent.putExtra("pop_menu", tag);
        }
        mMainActivity.sendBroadcast(intent);
    }
}
