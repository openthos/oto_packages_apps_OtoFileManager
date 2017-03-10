package com.openthos.filemanager.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;

import java.util.List;

public class PathAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mPathList;
    private MainActivity.AddressOnTouchListener mOnTouchListener;

    public PathAdapter(Context context, List<String> pathList,
                       MainActivity.AddressOnTouchListener listener) {
        mContext = context;
        mPathList = pathList;
        mOnTouchListener = listener;
    }

    @Override
    public int getCount() {
        return mPathList.size();
    }

    @Override
    public Object getItem(int i) {
        return mPathList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.path_grid_item, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
            view.setOnTouchListener(mOnTouchListener);
        }
        holder = (ViewHolder) view.getTag();
        holder.path.setTag(i);
        holder.path.setText(mPathList.get(i));
        int i1 = (int) holder.path.getPaint().measureText(holder.path.getText().toString());
        holder.path.setWidth(i1 + 10);
        return view;
    }

    public static class ViewHolder {
        public TextView path;
        public ViewHolder (View view) {
            path = (TextView) view.findViewById(R.id.tv_path);
        }
    }
}
