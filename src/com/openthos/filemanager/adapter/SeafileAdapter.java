package com.openthos.filemanager.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.SeafileAccount;
import com.openthos.filemanager.bean.SeafileLibrary;
import com.openthos.filemanager.utils.SeafileUtils;
import com.openthos.filemanager.fragment.SeafileFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class SeafileAdapter extends BaseAdapter {
    private ArrayList<SeafileLibrary> mList;
    private Context mContext;
    private SeafileFragment.GridViewOnGenericMotionListener mMotionListener;
    private View mView;

    public SeafileAdapter(Context context, ArrayList<SeafileLibrary> mList,
                          SeafileFragment.GridViewOnGenericMotionListener motionListener) {
        this.mList = mList;
        mContext = context;
        mMotionListener = motionListener;
    }

    public void setData(ArrayList<SeafileLibrary> librarys) {
        mList = librarys;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clearSelected() {
        if (mView != null) {
            mView.setSelected(false);
        }
        mView = null;
    }

    public void setSelected(View v) {
        mView = v;
        mView.setSelected(true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.icon_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
            convertView.setOnTouchListener(mMotionListener);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.name.setText(mList.get(position).libraryName);
        viewHolder.name.setTag(position);
        int isSync = mList.get(position).isSync;
        if (isSync == SeafileUtils.SYNC) {
            viewHolder.state.setImageResource(R.drawable.sync);
        } else {
            viewHolder.state.setImageResource(R.drawable.desync);
        }
        return convertView;
    }


    public static class ViewHolder {
        public TextView name;
        public ImageView state;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.tv_icon);
            state = (ImageView) view.findViewById(R.id.iv_icon);
        }
    }
}
