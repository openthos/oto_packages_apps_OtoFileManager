package com.openthos.filemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.fragment.PersonalSpaceFragment;
import com.openthos.filemanager.system.FileListAdapter;
import com.openthos.filemanager.utils.LocalCache;

import java.util.ArrayList;
import java.util.List;

public class PersonalAdapter extends BaseAdapter {
    private List<String> mPersonalList;
    private LayoutInflater mInflater;
    private PersonalSpaceFragment.GridViewOnGenericMotionListener mMotionListener;
    private List<Integer> selectFileInfoListIndex = new ArrayList<>();
    public PersonalAdapter(Context context, List<String> list,
                           PersonalSpaceFragment.GridViewOnGenericMotionListener motionListener) {
        mPersonalList = list;
        mMotionListener = motionListener;
        mInflater = LayoutInflater.from(context);
    }

    public List<Integer> getSelectFileInfoList() {
        return selectFileInfoListIndex;
    }

    @Override
    public int getCount() {
        return mPersonalList.size();
    }

    @Override
    public Object getItem(int i) {
        return mPersonalList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null)  {
            view = mInflater.inflate(R.layout.file_browser_item_grid, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
            view.setOnGenericMotionListener(mMotionListener);
        }
        viewHolder = (ViewHolder) view.getTag();
        viewHolder.name.setTag(i);
        viewHolder.name.setText(mPersonalList.get(i));
        LinearLayout background = (LinearLayout)view;
        background.setBackgroundResource(selectFileInfoListIndex.contains(i) ?
                R.drawable.list_item_bg_shape : R.color.white);
        return view;
    }

    public static class ViewHolder {
        public TextView name;
        public LinearLayout ll;
        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.file_name);
            ll = (LinearLayout) view.findViewById(R.id.ll_grid_item_bg);
        }
    }
}
