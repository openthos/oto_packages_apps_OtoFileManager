package com.openthos.filemanager.component;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.BaseDialog;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.BaseDialogAdapter;
import com.openthos.filemanager.fragment.PersonalSpaceFragment;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;

import java.util.ArrayList;

import static android.R.color.holo_purple;
import static android.R.color.transparent;

public class PersonalMenuDialog extends BaseDialog implements ListView.OnItemClickListener {

    private boolean mIsBlank;

    public PersonalMenuDialog(Context context, boolean isBlank) {
        super(context);
        mActivity = (MainActivity) context;
        mIsBlank = isBlank;
    }

    protected void initData() {
        mDatas = new ArrayList();
        if (mIsBlank) {
            mDatas.add(mActivity.getString(R.string.operation_refresh));
        } else {
            String[] menu = mActivity.getResources().getStringArray(R.array.personal_folder_menu);
            for (int i = 0; i < menu.length; i++) {
                mDatas.add(menu[i]);
            }
        }
        BaseDialogAdapter mAdapter = new BaseDialogAdapter(mActivity, mDatas);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void initListener() {
        mListView.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String content = (String) view.getTag();
        PersonalSpaceFragment personalSpaceFragment =
            (PersonalSpaceFragment) ((MainActivity) mActivity).getVisibleFragment();
        if (mActivity.getString(R.string.operation_open).equals(content)) {
            personalSpaceFragment.enter();
        } else if (mActivity.getString(R.string.operation_copy_path).equals(content)) {
            personalSpaceFragment.copyPath();
        } else if (mActivity.getString(R.string.operation_refresh).equals(content)) {
            personalSpaceFragment.checkFolder();
        }
        if (!TextUtils.isEmpty(content)) {
            dismiss();
        }
    }
}
