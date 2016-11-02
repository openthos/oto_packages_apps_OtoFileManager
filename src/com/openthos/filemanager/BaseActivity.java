package com.openthos.filemanager;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.openthos.filemanager.component.AppManager;

public abstract class BaseActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayoutId());
        AppManager.getAppManager().addActivity(this);
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().finishActivity(this);
    }

    public void setNavigationBar(String displayPath) {
    }

    protected abstract void initListener();
    protected abstract void initData();
    protected abstract void initView();
    protected abstract int getLayoutId();
}
