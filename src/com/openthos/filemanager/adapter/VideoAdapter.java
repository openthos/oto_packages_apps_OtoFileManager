package com.openthos.filemanager.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.openthos.filemanager.R;

import java.util.ArrayList;

public class VideoAdapter extends BaseAdapter {
    private ArrayList<VideoItem> videoItems;
    private Context context;
    public VideoAdapter(Context context, ArrayList<VideoItem> videoItems) {
        this.videoItems = videoItems;
        this.context = context;
    }

    @Override
    public int getCount() {
        return videoItems.size();
    }

    @Override
    public Object getItem(int i) {
        return videoItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;

    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHodler hodler;
        if (convertView==null){
            hodler = new ViewHodler();
            convertView = View.inflate(context, R.layout.video_item,null);
            hodler.iv_video = (ImageView) convertView.findViewById(R.id.iv_video);
            hodler.tv_video_name = (TextView) convertView.findViewById(R.id.tv_video_name);
            hodler.tv_video_size = (TextView) convertView.findViewById(R.id.tv_video_size);
            convertView.setTag(hodler);
        }else {
            hodler = (ViewHodler) convertView.getTag();
        }
        VideoItem videoItem = videoItems.get(i);
        hodler.tv_video_name.setText(videoItem.getName());
        hodler.tv_video_size.setText(Formatter.formatFileSize(context,videoItem.getSize()));
        return convertView;
    }

    static class ViewHodler{
        private ImageView iv_video;
        private TextView tv_video_name,tv_video_size;
    }
}
