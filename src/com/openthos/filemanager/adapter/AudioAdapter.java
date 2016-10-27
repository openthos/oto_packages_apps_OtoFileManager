package com.openthos.filemanager.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.AudioItem;

import java.util.ArrayList;

public class AudioAdapter extends BaseAdapter {
    private ArrayList<AudioItem> audioItems;
    private Context context;

    public AudioAdapter(Context context, ArrayList<AudioItem> audioItems) {
        this.audioItems = audioItems;
        this.context = context;
    }

    @Override
    public int getCount() {
        return audioItems.size();
    }

    @Override
    public Object getItem(int i) {
        return audioItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHodler hodler;
        if (convertView == null){
            hodler = new ViewHodler();
            convertView = View.inflate(context, R.layout.audio_item,null);
            hodler.iv_audio = (ImageView) convertView.findViewById(R.id.iv_audio);
            hodler.tv_audio_name = (TextView) convertView.findViewById(R.id.tv_audio_name);
            hodler.tv_audio_size = (TextView) convertView.findViewById(R.id.tv_audio_size);
            convertView.setTag(hodler);
        }else {
            hodler = (ViewHodler) convertView.getTag();
        }
        AudioItem audioItem = audioItems.get(i);
        hodler.tv_audio_name.setText(audioItem.getName());
        hodler.tv_audio_size.setText(Formatter.formatFileSize(context,audioItem.getSize()));
        return convertView;
    }

    static class ViewHodler{
        private ImageView iv_audio;
        private TextView tv_audio_name,tv_audio_size;
    }
}
