package org.openthos.filemanager.component;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;

import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.R;
import org.openthos.filemanager.BaseMenuDialog;
import org.openthos.filemanager.adapter.BaseDialogAdapter;
import org.openthos.filemanager.system.FileSortHelper;
import org.openthos.filemanager.system.FileViewInteractionHub;
import org.openthos.filemanager.fragment.SystemSpaceFragment;

import java.util.ArrayList;

public class SortMenuDialog extends BaseMenuDialog implements AdapterView.OnItemClickListener {

    public SortMenuDialog(Context context, FileViewInteractionHub fileViewInteractionHub) {
        super(context);
        mActivity = (MainActivity) context;
        mFileViewInteractionHub = fileViewInteractionHub;
    }

    @Override
    protected void initData() {
        mDatas = new ArrayList();
        String[] sArr = mActivity.getResources().getStringArray(R.array.sort_menu);
        for (int i = 0; i < sArr.length; i++) {
            mDatas.add(sArr[i]);
        }
        mListView.setAdapter(new BaseDialogAdapter(getContext(),
                mDatas, mFileViewInteractionHub));
    }

    @Override
    protected void initListener() {
        mListView.setOnItemClickListener(this);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
                mFileViewInteractionHub.clearSelection();
                mFileViewInteractionHub.refreshFileList();
            }
        });
    }

    private void setSortPositive(Enum sort) {
        SystemSpaceFragment fragment = (SystemSpaceFragment) mActivity.mCurFragment;
        fragment.setSortTag(sort, !fragment.getSortTag(sort));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String content = (String) view.getTag();
        if (mActivity.getString(R.string.menu_item_sort_name).equals(content)) {
            setSortPositive(FileSortHelper.SortMethod.name);
            mFileViewInteractionHub.onSortChanged(FileSortHelper.SortMethod.name);
        } else if (mActivity.getString(R.string.menu_item_sort_date).equals(content)) {
            setSortPositive(FileSortHelper.SortMethod.date);
            mFileViewInteractionHub.onSortChanged(FileSortHelper.SortMethod.date);
        } else if (mActivity.getString(R.string.menu_item_sort_size).equals(content)) {
            setSortPositive(FileSortHelper.SortMethod.size);
            mFileViewInteractionHub.onSortChanged(FileSortHelper.SortMethod.size);
        } else if (mActivity.getString(R.string.menu_item_sort_type).equals(content)) {
            setSortPositive(FileSortHelper.SortMethod.type);
            mFileViewInteractionHub.onSortChanged(FileSortHelper.SortMethod.type);
        }
        dismiss();
    }
}
