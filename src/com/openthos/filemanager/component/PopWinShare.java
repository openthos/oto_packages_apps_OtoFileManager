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
    private View mainView;
    private TextView pop_menu_refresh, pop_menu_cancel_all, pop_menu_copy,
                     pop_menu_delete, pop_menu_send, pop_menu_create, pop_menu_exit,
                     pop_setting_view, pop_setting_relative, pop_setting_help, pop_setting_exit;
    private LinearLayout ll_menu, ll_setting;

    public PopWinShare(MainActivity mainActivity, View.OnClickListener paramOnClickListener,
                       int paramInt1, int paramInt2, String menu_tag) {
        super(mainActivity);
        mainView = LayoutInflater.from(mainActivity).inflate(R.layout.popwin_share, null);
        ll_menu = (LinearLayout) mainView.findViewById(R.id.ll_menu);
        ll_setting = (LinearLayout) mainView.findViewById(R.id.ll_setting);
        if (menu_tag.equals("iv_menu")) {
            ll_setting.setVisibility(View.GONE);
            ll_menu.setVisibility(View.VISIBLE);
            pop_menu_refresh = (TextView) mainView.findViewById(R.id.pop_menu_refresh);
            pop_menu_cancel_all = (TextView) mainView.findViewById(R.id.pop_menu_cancel_all);
            pop_menu_copy = (TextView) mainView.findViewById(R.id.pop_menu_copy);
            pop_menu_delete = (TextView) mainView.findViewById(R.id.pop_menu_delete);
            pop_menu_send = (TextView) mainView.findViewById(R.id.pop_menu_send);
            pop_menu_create = (TextView) mainView.findViewById(R.id.pop_menu_create);
            pop_menu_exit = (TextView) mainView.findViewById(R.id.pop_menu_exit);
            if (paramOnClickListener != null) {
                pop_menu_refresh.setOnClickListener(paramOnClickListener);
                pop_menu_cancel_all.setOnClickListener(paramOnClickListener);
                pop_menu_copy.setOnClickListener(paramOnClickListener);
                pop_menu_delete.setOnClickListener(paramOnClickListener);
                pop_menu_send.setOnClickListener(paramOnClickListener);
                pop_menu_create.setOnClickListener(paramOnClickListener);
                pop_menu_exit.setOnClickListener(paramOnClickListener);
            }
        }else if (menu_tag.equals("iv_setting")) {
            ll_menu.setVisibility(View.GONE);
            ll_setting.setVisibility(View.VISIBLE);
            pop_setting_view = (TextView) mainView.findViewById(R.id.pop_setting_view);
            pop_setting_relative = (TextView) mainView.findViewById(R.id.pop_setting_relative);
            pop_setting_help = (TextView) mainView.findViewById(R.id.pop_setting_help);
            pop_setting_exit = (TextView) mainView.findViewById(R.id.pop_setting_exit);
            if (paramOnClickListener != null) {
                pop_setting_view.setOnClickListener(paramOnClickListener);
                pop_setting_relative.setOnClickListener(paramOnClickListener);
                pop_setting_help.setOnClickListener(paramOnClickListener);
                pop_setting_exit.setOnClickListener(paramOnClickListener);
            }
        }
        setContentView(mainView);
        setWidth(paramInt1);
        setHeight(paramInt2);
        setAnimationStyle(R.style.AnimTools);
        setBackgroundDrawable(new ColorDrawable());
    }
}
