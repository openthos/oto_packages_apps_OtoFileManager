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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.AudioAdapter;
import com.openthos.filemanager.bean.AudioItem;
import com.openthos.filemanager.system.Constants;

import java.io.File;
import java.util.ArrayList;

public class MusicFragment  extends BaseFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = MusicFragment.class.getSimpleName();
    private static final int MUSIC_OK = 0;
    private ArrayList<AudioItem> audioItems;
    private ContentResolver contentResolver;
    private ProgressDialog mProgressDialog;
    private GridView gv_audio_pager;
    private TextView tv_no_audio;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case MUSIC_OK:
                    mProgressDialog.dismiss();
                    if (audioItems != null && audioItems.size() > 0) {
                        gv_audio_pager.setAdapter(new AudioAdapter(getActivity(), audioItems));
                    } else {
                        tv_no_audio.setVisibility(View.VISIBLE);
                    }
                    handler.removeCallbacksAndMessages(null);
                    break;
            }
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.music_fragment_layout;
    }

    @Override
    protected void initView() {
        gv_audio_pager = (GridView) rootView.findViewById(R.id.gv_audio_pager);
        tv_no_audio = (TextView) rootView.findViewById(R.id.tv_no_audio);
    }

    protected void initData() {
        getAudioList();
    }

    @Override
    protected void initListener() {
        gv_audio_pager.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        File f = new File(audioItems.get(i).getData());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = Constants.getMIMEType(f);
        intent.setDataAndType(Uri.fromFile(f), type);
        startActivity(intent);
    }

    private void getAudioList() {
        mProgressDialog = ProgressDialog.show(getActivity(), null, "loading...");
        new Thread() {
            public void run() {
                audioItems = new ArrayList<> ();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                contentResolver = getActivity().getContentResolver();
                String[] projection = {
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA
                };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        AudioItem item = new AudioItem();
                        String name = cursor.getString
                                      (cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        item.setName(name);
                        Long size = cursor.getLong
                                    (cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                        item.setSize(size);
                        String data = cursor.getString
                                      (cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        Log.e(TAG, data);
                        item.setData(data);
                        audioItems.add(item);
                    }
                    cursor.close();
                    handler.sendEmptyMessage(MUSIC_OK);
                }
            }
        }.start();
    }

    @Override
    public boolean canGoBack() {
        return false;
    }

    @Override
    public void goBack() {
    }

    @Override
    protected void enter(String tag, String path) {
    }

    @Override
    public void processDirectionKey(int keyCode) {
    }
}
