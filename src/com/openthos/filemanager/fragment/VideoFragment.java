package com.openthos.filemanager.fragment;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.VideoAdapter;
import com.openthos.filemanager.adapter.VideoItem;
import com.openthos.filemanager.system.Constants;

import java.io.File;
import java.util.ArrayList;

public class VideoFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private ArrayList<VideoItem> videoItems;
    private ProgressDialog mProgressDialog;
    private TextView tv_no_video;
    private GridView gv_video_pager;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mProgressDialog.dismiss();
            if (videoItems != null && videoItems.size() > 0) {
                gv_video_pager.setAdapter(new VideoAdapter(getActivity(), videoItems));
            }else {
                tv_no_video.setVisibility(View.VISIBLE);
            }
            handler.removeCallbacksAndMessages(null);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_fragment_layout, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        tv_no_video = (TextView) view.findViewById(R.id.tv_no_video);
        gv_video_pager = (GridView) view.findViewById(R.id.gv_video_pager);
    }

    private void initData() {
        getVideoList();
        gv_video_pager.setOnItemClickListener(this);
    }

    private void getVideoList() {
        mProgressDialog = ProgressDialog.show(getActivity(), null, "loading...");
        new Thread() {
            public void run() {
                videoItems = new ArrayList<> ();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = getActivity().getContentResolver();
                String[] projection = {
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DATA
                };
                Cursor cursor = contentResolver.query(uri,projection,null,null,null);
                if (cursor!=null) {
                    while (cursor.moveToNext()) {
                        VideoItem item = new VideoItem();
                        String name = cursor.getString
                                      (cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                        item.setName(name);
                        Long size = cursor.getLong
                                    (cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                        item.setSize(size);
                        String data = cursor.getString
                                      (cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                        item.setData(data);
                        videoItems.add(item);
                    }
                    cursor.close();
                    handler.sendEmptyMessage(0);
                }
            }
        }.start();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        VideoItem videoItem = videoItems.get(i);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        File f = new File(videoItem.getData());
        String type = Constants.getMIMEType(f);
        intent.setDataAndType(Uri.fromFile(f), type);
        startActivity(intent);
    }

    @Override
    public boolean canGoBack() {
        return false;
    }

    @Override
    public void goBack() {
    }
}
