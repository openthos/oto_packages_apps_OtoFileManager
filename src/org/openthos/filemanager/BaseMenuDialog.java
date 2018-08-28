package org.openthos.filemanager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.openthos.filemanager.system.FileViewInteractionHub;

import java.util.ArrayList;

/**
 * Created by wang on 17-3-9.
 */

public abstract class BaseMenuDialog extends BaseDialog {
    protected MainActivity mActivity;
    protected ListView mListView;
    protected ArrayList mDatas;
    protected FileViewInteractionHub mFileViewInteractionHub;

    public BaseMenuDialog(@NonNull Context context) {
        super(context);
    }

    public BaseMenuDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
    }

    public BaseMenuDialog(@NonNull Context context, boolean cancelable,
                          @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_base);
        mListView = (ListView) findViewById(R.id.dialog_base_lv);
        initData();
        initListener();
        setListViewBasedOnChildren(mListView);
    }

    public void setListViewBasedOnChildren(ListView listView) {
        if (listView == null || listView.getAdapter() == null) {
            return;
        }
        ListAdapter listAdapter = listView.getAdapter();
        int maxWidth = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = listItem.getMeasuredWidth();
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.width = maxWidth;
        listView.setLayoutParams(params);
    }

    protected abstract void initData();

    protected abstract void initListener();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        MainActivity.setState(event.isCtrlPressed(), event.isShiftPressed());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        MainActivity.setState(event.isCtrlPressed(), event.isShiftPressed());
        return super.onKeyUp(keyCode, event);
    }
}
