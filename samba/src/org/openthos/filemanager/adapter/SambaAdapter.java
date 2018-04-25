package org.openthos.filemanager.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.openthos.filemanager.R;
import org.openthos.filemanager.bean.SeafileLibrary;
import org.openthos.filemanager.fragment.SambaFragment;
import org.openthos.filemanager.fragment.SeafileFragment;
import org.openthos.filemanager.utils.SeafileUtils;

import java.util.ArrayList;

public class SambaAdapter extends BaseAdapter {
    private ArrayList<String> mList;
    private Context mContext;
    private SambaFragment.GridViewOnGenericMotionListener mMotionListener;
    private View mView;
    private boolean mIsPonitPage = true;
    Drawable mPonitDraw;
    Drawable mFolderDraw;
    Drawable mFileDraw;

    public SambaAdapter(Context context, ArrayList<String> list,
                        SambaFragment.GridViewOnGenericMotionListener motionListener) {
        mContext = context;
        mList = list;
        mMotionListener = motionListener;
        mPonitDraw = mContext.getDrawable(R.mipmap.icon_computer);
        mFolderDraw = mContext.getDrawable(R.mipmap.folder);
        mFileDraw = mContext.getDrawable(R.mipmap.file_icon_default);
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
            convertView = View.inflate(mContext, R.layout.samba_icon_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
            convertView.setOnTouchListener(mMotionListener);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.name.setText(mList.get(position).replace("/",""));
        viewHolder.name.setTag(position);
        if (mIsPonitPage){
            viewHolder.icon.setImageDrawable(mPonitDraw);
        } else if (mList.get(position).endsWith("/")){
            viewHolder.icon.setImageDrawable(mFolderDraw);
        }else {
            viewHolder.icon.setImageDrawable(mFileDraw);
        }
        return convertView;
    }

    public void setIsPointPage(boolean b) {
        mIsPonitPage = b;
    }


    public static class ViewHolder {
        public TextView name;
        public ImageView icon;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.tv_icon);
            icon = (ImageView) view.findViewById(R.id.iv_icon);
        }
    }
}
