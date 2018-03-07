package com.openthos.filemanager;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.content.Context;

import com.openthos.filemanager.bean.Mode;
import com.openthos.filemanager.component.AppManager;
import com.openthos.filemanager.system.FileSortHelper;

public abstract class BaseActivity extends FragmentActivity {

    private FileSortHelper mFileSortHelper;
    public ViewGroup mInflate;
    public Mode mMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mInflate = (ViewGroup) View.inflate(this, getLayoutId(), null);
       // setContentView(getLayoutId());
        setContentView(mInflate);
        AppManager.getAppManager().addActivity(this);
        mFileSortHelper = new FileSortHelper();
        setMode();
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void setNavigationBar(String displayPath) {
    }

    public FileSortHelper getFileSortHelper() {
        return mFileSortHelper;
    }

    protected abstract void setMode();
    protected abstract void initListener();
    protected abstract void initData();
    protected abstract void initView();
    protected abstract int getLayoutId();
}
