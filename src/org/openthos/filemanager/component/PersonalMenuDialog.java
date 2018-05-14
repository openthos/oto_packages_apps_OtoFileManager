package org.openthos.filemanager.component;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.BaseMenuDialog;
import org.openthos.filemanager.R;
import org.openthos.filemanager.adapter.BaseDialogAdapter;
import org.openthos.filemanager.bean.PersonalBean;
import org.openthos.filemanager.fragment.PersonalSpaceFragment;

import java.util.ArrayList;

public class PersonalMenuDialog extends BaseMenuDialog implements ListView.OnItemClickListener {

    private boolean mIsBlank = true;
    private PersonalBean mBean;

    public PersonalMenuDialog(Context context) {
        super(context);
        mActivity = (MainActivity) context;
    }

    public PersonalMenuDialog(Context context, PersonalBean bean) {
        super(context);
        mActivity = (MainActivity) context;
        mBean = bean;
        mIsBlank = false;
    }

    protected void initData() {
        mDatas = new ArrayList();
        if (mIsBlank) {
            mDatas.add(mActivity.getString(R.string.operation_refresh));
        } else {
            String[] menu = mActivity.getResources().getStringArray(R.array.personal_folder_menu);
            String strCollect = getContext().getResources().getString(R.string.collect);
            String strCancelCollected =
                    getContext().getResources().getString(R.string.cancel_collected);
            for (int i = 0; i < menu.length; i++) {
                if (menu[i].equals(strCollect) && mBean.isCollected()) {
                    continue;
                }
                if (menu[i].equals(strCancelCollected) && !mBean.isCollected()) {
                    continue;
                }
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
                (PersonalSpaceFragment) (mActivity).getVisibleFragment();
        if (mActivity.getString(R.string.operation_open).equals(content)) {
            personalSpaceFragment.enter();
        } else if (mActivity.getString(R.string.operation_copy_path).equals(content)) {
            personalSpaceFragment.copyPath();
        } else if (mActivity.getString(R.string.operation_refresh).equals(content)) {
        } else if (mActivity.getString(R.string.collect).equals(content)
                || mActivity.getString(R.string.cancel_collected).equals(content)) {
            mActivity.handleCollectedChange(mBean);
        }
        if (!TextUtils.isEmpty(content)) {
            dismiss();
        }
    }
}
