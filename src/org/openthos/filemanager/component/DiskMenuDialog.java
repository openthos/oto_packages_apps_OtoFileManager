package org.openthos.filemanager.component;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.BaseMenuDialog;
import org.openthos.filemanager.R;
import org.openthos.filemanager.adapter.BaseDialogAdapter;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.fragment.SdStorageFragment;
import java.util.ArrayList;

public class DiskMenuDialog extends BaseMenuDialog implements ListView.OnItemClickListener{
    private String mDiskTag;

    public DiskMenuDialog(Context context, String tag) {
        super(context);
        mActivity = (MainActivity) context;
        mDiskTag = tag;
    }

    public void initData() {
        String[] diskMenu = null;
        switch (mDiskTag) {
            case Constants.TAG_SYSTEM:
                diskMenu = new String[] {
                        mActivity.getString(R.string.operation_open)
                };
                break;
            case Constants.TAG_USB:
                diskMenu = new String[] {
                        mActivity.getString(R.string.operation_open),
                        mActivity.getString(R.string.umount),
                        mActivity.getString(R.string.format_usb_device)
                };
                break;
            case Constants.TAG_AUTO_MOUNT:
                diskMenu = new String[] {
                        mActivity.getString(R.string.operation_open),
                        mActivity.getString(R.string.umount)
                };
                break;
        }
        mDatas = new ArrayList();
        prepareData(diskMenu);

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
            if (mDiskTag.equals(Constants.TAG_USB)) {
                ((SdStorageFragment) (((MainActivity)
                        mActivity).getVisibleFragment())).uninstallUSB();
            } else if (mDiskTag.equals(Constants.TAG_AUTO_MOUNT)) {
                ((SdStorageFragment) (((MainActivity)
                        mActivity).getVisibleFragment())).uninstallUmount();
            }
        } else if (mActivity.getString(R.string.operation_open).equals(content)) {
            ((SdStorageFragment) (((MainActivity) mActivity).getVisibleFragment())).enter();
        } else if (mActivity.getString(R.string.format_usb_device).equals(content)) {
            ((MainActivity) mActivity).formatVolume();
        }
        dismiss();
    }
}
