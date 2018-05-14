package org.openthos.filemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import org.openthos.filemanager.R;
import org.openthos.filemanager.bean.PersonalBean;
import org.openthos.filemanager.fragment.PersonalSpaceFragment;

import java.util.ArrayList;
import java.util.List;

public class PersonalAdapter extends BaseAdapter {
    private List<PersonalBean> mPersonalList;
    private LayoutInflater mInflater;
    private PersonalSpaceFragment.GridViewOnGenericMotionListener mMotionListener;
    private List<Integer> selectFileInfoListIndex = new ArrayList<>();

    public PersonalAdapter(Context context, List<PersonalBean> list,
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
        if (view == null) {
            view = mInflater.inflate(R.layout.personal_item, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
            view.setOnGenericMotionListener(mMotionListener);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.tvTitle.setText(mPersonalList.get(i).getTitle());
        viewHolder.tvTitle.setTag(i);
        viewHolder.ivIcon.setImageResource(mPersonalList.get(i).getIconRes());
        view.setBackgroundResource(selectFileInfoListIndex.contains(i) ?
                R.drawable.list_item_bg_shape : R.color.white);
        return view;
    }

    public static class ViewHolder {
        public TextView tvTitle;
        public ImageView ivIcon;
        public LinearLayout ll;

        public ViewHolder(View view) {
            tvTitle = (TextView) view.findViewById(R.id.file_name);
            ivIcon = (ImageView) view.findViewById(R.id.file_image);
            ll = (LinearLayout) view.findViewById(R.id.ll_grid_item_bg);
        }
    }

}
