package com.openthos.filemanager.component;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.utils.T;

public class PopOnClickLintener implements View.OnClickListener {
    private static final String POP_REFRESH = "pop_refresh";
    private static final String POP_CANCEL_ALL = "pop_cancel_all";
    private static final String POP_COPY = "pop_copy";
    private static final String POP_DELETE = "pop_delete";
    private static final String POP_SEND = "pop_send";
    private static final String POP_CREATE = "pop_create";
    private static final String VIEW_OR_DISMISS = "view_or_dismiss";
    private String menu_tag;
    private MainActivity mainActivity;
    private FragmentManager manager;

    public PopOnClickLintener(String menu_tag, MainActivity mainActivity, FragmentManager manager) {
        this.menu_tag = menu_tag;
        this.mainActivity = mainActivity;
        this.manager = manager;
    }

    @Override
    public void onClick(View view) {
//        if (menu_tag.equals("iv_menu")) {
//            switch (view.getId()) {
//                case R.id.pop_menu_refresh:
//                    sendBroadcastMessage("iv_menu", POP_REFRESH);
//                    mainActivity.DismissPopwindow();
//                    break;
//                case R.id.pop_menu_cancel_all:
//                    sendBroadcastMessage("iv_menu", POP_CANCEL_ALL);
//                    mainActivity.DismissPopwindow();
//                    break;
//                case R.id.pop_menu_copy:
//                    sendBroadcastMessage("iv_menu", POP_COPY);
//                    mainActivity.DismissPopwindow();
//                    break;
//                case R.id.pop_menu_delete:
//                    sendBroadcastMessage("iv_menu", POP_DELETE);
//                    mainActivity.DismissPopwindow();
//                    break;
//                case R.id.pop_menu_send:
//                    sendBroadcastMessage("iv_menu", POP_SEND);
//                    mainActivity.DismissPopwindow();
//                    break;
//                case R.id.pop_menu_create:
//                    sendBroadcastMessage("iv_menu", POP_CREATE);
//                    mainActivity.DismissPopwindow();
//                    break;
//                case R.id.pop_menu_exit:
//                    mainActivity.finish();
//                    break;
//                default:
//                    break;
//            }
//        } else
        if (menu_tag.equals("iv_setting")) {
            switch (view.getId()) {
                case R.id.pop_setting_view:
                    if (manager.getBackStackEntryCount() < 1) {
                        T.showShort(mainActivity,
                                    mainActivity.getString(R.string.operation_not_support));
                    }
                    sendBroadcastMessage("iv_menu", VIEW_OR_DISMISS);
                    mainActivity.DismissPopwindow();
                    break;
                case R.id.pop_setting_relative:
                    Intent intent = new Intent(mainActivity, AboutActivity.class);
                    mainActivity.startActivity(intent);
                    mainActivity.DismissPopwindow();
                    break;
                case R.id.pop_setting_help:
                    intent = new Intent(mainActivity,HelpActivity.class);
                    mainActivity.startActivity(intent);
                    mainActivity.DismissPopwindow();
                    break;
                case R.id.pop_setting_exit:
                    mainActivity.finish();
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
        mainActivity.sendBroadcast(intent);
    }
}
