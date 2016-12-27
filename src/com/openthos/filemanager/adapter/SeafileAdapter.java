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

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.ImageBean;
import com.openthos.filemanager.bean.SeafileAccount;
import com.openthos.filemanager.component.CloudDialog;
import com.openthos.filemanager.component.MyImageView;
import com.openthos.filemanager.component.NativeImageLoader;
import com.openthos.filemanager.utils.SeafileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SeafileAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> mList;
    private LayoutInflater mInflater;
    private Context mContext;

    public SeafileAdapter(Context context, ArrayList<HashMap<String, String>> mList) {
        this.mList = mList;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<HashMap<String, String>> librarys) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.icon_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.name.setText(mList.get(position).get(SeafileAccount.LIBRARY_NAME));
        File f = new File(((MainActivity) mContext).mAccount.mFile,
                mList.get(position).get(SeafileAccount.LIBRARY_NAME));
        if (!f.exists()){
            f.mkdirs();
        }
        SeafileUtils.download(mList.get(position).get(SeafileAccount.LIBRARY_ID),
                              f.getAbsolutePath());
        return convertView;
    }

    public static class ViewHolder {
        public TextView name;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.tv_icon);
        }
    }
}
