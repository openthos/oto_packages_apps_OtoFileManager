package com.openthos.filemanager.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.SeafileAccount;
import com.openthos.filemanager.fragment.SeafileFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class SeafileAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> mList;
    private Context mContext;
    private SeafileFragment.GridViewOnGenericMotionListener mMotionListener;
    private View mView;

    public SeafileAdapter(Context context, ArrayList<HashMap<String, String>> mList,
                          SeafileFragment.GridViewOnGenericMotionListener motionListener) {
        this.mList = mList;
        mContext = context;
        mMotionListener = motionListener;
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
        viewHolder.name.setText(mList.get(position).get(SeafileAccount.LIBRARY_NAME));
        viewHolder.name.setTag(position);
        File f = new File(((MainActivity) mContext).mAccount.mFile,
                mList.get(position).get(SeafileAccount.LIBRARY_NAME));
        if (!f.exists()) {
            f.mkdirs();
        }
        return convertView;
    }


    public static class ViewHolder {
        public TextView name;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.tv_icon);
        }
    }
}
