package org.openthos.filemanager;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.openthos.filemanager.bean.Mode;
import org.openthos.filemanager.system.FileSortHelper;

public abstract class BaseActivity extends FragmentActivity {

    private FileSortHelper mFileSortHelper;
    public ViewGroup mInflate;
    public Mode mMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mInflate = (ViewGroup) View.inflate(this, getLayoutId(), null);
        setContentView(mInflate);
        mFileSortHelper = new FileSortHelper();
        setMode();
        initView();
        initData();
        initListener();
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
