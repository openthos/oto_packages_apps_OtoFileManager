package com.openthos.filemanager.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.AppInfo;

import java.util.ArrayList;

public class DeskAdapter extends BaseAdapter {
    private ArrayList<AppInfo> appInfos = new ArrayList<>();
    private Context context;

    public DeskAdapter(ArrayList<AppInfo> appInfos, Context context) {
        this.appInfos = appInfos;
        this.context = context;
    }

    @Override
    public int getCount() {
        return appInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return appInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        convertView = View.inflate(context, R.layout.desk_icon_item, null);
        ImageView iv_desk_icon = (ImageView) convertView.findViewById(R.id.iv_desk_icon);
        TextView tv_app_name = (TextView) convertView.findViewById(R.id.tv_app_name);
        AppInfo appInfo = appInfos.get(i);
        iv_desk_icon.setImageDrawable(appInfo.getIcon());
        tv_app_name.setText(appInfo.getAppName());
        return convertView;
    }
}
