package com.openthos.filemanager.component;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.BaseDialog;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.BaseDialogAdapter;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.fragment.SdStorageFragment;
import java.util.ArrayList;
import static android.R.color.holo_purple;
import static android.R.color.transparent;

public class DiskDialog extends BaseDialog implements ListView.OnItemClickListener{
    private boolean mIsUSB;
    private View mView;
    private MotionEvent mMotionEvent;

    public DiskDialog(Context context, boolean isUSB) {
        super(context);
        mActivity = (MainActivity) context;
        mIsUSB = isUSB;
    }

    public void initData() {
        String[] diskMeun = new String[] {
                mActivity.getString(R.string.operation_open),
        };

        String[] uDiskMeun = new String[] {
                mActivity.getString(R.string.operation_open),
                mActivity.getString(R.string.umount),
                mActivity.getString(R.string.format_usb_device)
        };

        mDatas = new ArrayList();
        prepareData(mIsUSB ? uDiskMeun : diskMeun);

        BaseDialogAdapter mAdapter = new BaseDialogAdapter(mActivity, mDatas);
        mListView.setAdapter(mAdapter);
    }

    protected void initListener() {
        mListView.setOnItemClickListener(this);
    }

    public  void prepareData(String[] sArr) {
        for (int i = 0; i < sArr.length; i++) {
            mDatas.add(sArr[i]);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String content = (String) view.getTag();
        if (mActivity.getString(R.string.umount).equals(content)) {
            ((SdStorageFragment) (((MainActivity)
                                           mActivity).getVisibleFragment())).uninstallUSB();
        } else if (mActivity.getString(R.string.operation_open).equals(content)) {
            ((SdStorageFragment) (((MainActivity) mActivity).getVisibleFragment())).enter();
        } else if (mActivity.getString(R.string.format_usb_device).equals(content)) {
            ((MainActivity) mActivity).formatVolume();
        }
        dismiss();
    }
}
