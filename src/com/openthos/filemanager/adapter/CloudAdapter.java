package com.openthos.filemanager.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.ImageBean;
import com.openthos.filemanager.component.CloudDialog;
import com.openthos.filemanager.component.MyImageView;
import com.openthos.filemanager.component.NativeImageLoader;

import java.util.List;

public class CloudAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mList;
    //private LayoutInflater mInflater;

    public CloudAdapter(Context mContext, List<String> mList) {
        this.mContext = mContext;
        this.mList = mList;
    //    mInflater = LayoutInflater.from(mContext);
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.icon_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.name.setText(mList.get(position));
        return convertView;
    }

    public static class ViewHolder {
        public TextView name;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.tv_icon);
        }
    }
}
