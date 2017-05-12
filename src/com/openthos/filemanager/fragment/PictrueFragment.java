package com.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.GroupAdapter;
import com.openthos.filemanager.bean.ImageBean;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.fragment.DetailFragment;
import com.openthos.filemanager.system.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;

public class PictrueFragment extends BaseFragment {
    private static final int SCAN_OK = 1;
    private GridView gv_pictrue;
    private TextView tv_no_pictrue;
    private GroupAdapter adapter;
    private ProgressDialog mProgressDialog;
    private ArrayList<ImageBean> list = new ArrayList<>();
    private HashMap<String, List<String>> mGruopMap = new HashMap<>();
    private ContentResolver mContentResolver;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCAN_OK:
                    mProgressDialog.dismiss();
                    list = subGroupOfImage(mGruopMap);
                    if (null != list) {
                        adapter = new GroupAdapter(getActivity(), list, gv_pictrue);
                    } else {
                        tv_no_pictrue.setVisibility(View.VISIBLE);
                    }
                    if (adapter != null) {
                        gv_pictrue.setAdapter(adapter);
                    }
                    mHandler.removeCallbacksAndMessages(null);
                    break;
            }
        }
    };

    @SuppressLint({"NewApi", "ValidFragment"})
    public PictrueFragment(FragmentManager mManager) {
        super(mManager);
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public PictrueFragment() {
        super();
    }

    @Override
    public int getLayoutId() {
        return R.layout.pictrue_fragment_layout;
    }

    @Override
    protected void initView() {
        gv_pictrue = (GridView) rootView.findViewById(R.id.gv_pictrue);
        tv_no_pictrue = (TextView) rootView.findViewById(R.id.tv_no_pictrue);
    }

    protected void initData() {
        if (null != list) {
            list.clear();
        }
        getImages();
    }

    @Override
    protected void initListener() {
        gv_pictrue.setOnItemClickListener(new FolderOnItemClickListener());
    }

    private class FolderOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            DetailFragment fragment = new DetailFragment(mGruopMap, list, i);
            mManager.beginTransaction().hide(mMainActivity.mCurFragment).commit();
            mManager.beginTransaction().add(R.id.fl_mian, fragment, Constants.DETAILFRAGMENT_TAG)
                                           .addToBackStack(null).commit();
        }
    }

    private ArrayList<ImageBean> subGroupOfImage(HashMap<String, List<String>> mGruopMap) {
        if (mGruopMap.size() == 0) {
            return null;
        }
        for (Map.Entry<String, List<String>> entry : mGruopMap.entrySet()) {
            ImageBean mImageBean = new ImageBean();
            String key = entry.getKey();
            List<String> value = entry.getValue();

            mImageBean.setFolderName(key);
            mImageBean.setImageCounts(value.size());
            mImageBean.setTopImagePath(value.get(0));
            list.add(mImageBean);
        }
        return list;
    }

    private void getImages() {
        if (!Util.isSDCardReady()) {
            Toast.makeText(getActivity(),
                           getString(R.string.external_storage_not_exist), LENGTH_SHORT).show();
            return;
        }

        mContentResolver = getActivity().getContentResolver();
        if (mContentResolver != null) {
            mProgressDialog = ProgressDialog.show(getActivity(), null, "loading...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    Cursor mCursor = mContentResolver.query(mImageUri, null,
                            MediaStore.Images.Media.MIME_TYPE + "=? or "
                                    + MediaStore.Images.Media.MIME_TYPE + "=?",
                            new String[]{"image/jpeg", "image/png"},
                            MediaStore.Images.Media.DATE_MODIFIED);

                    assert mCursor != null;
                    while (mCursor.moveToNext()) {
                        String path = mCursor.getString(mCursor
                                .getColumnIndex(MediaStore.Images.Media.DATA));
                        String parentName = new File(path).getParentFile().getName();
                        if (!mGruopMap.containsKey(parentName)) {
                            List<String> chileList = new ArrayList<>();
                            chileList.add(path);
                            mGruopMap.put(parentName, chileList);
                        } else {
                            mGruopMap.get(parentName).add(path);
                        }
                    }
                    mCursor.close();
                    mHandler.sendEmptyMessage(SCAN_OK);
                }
            }).start();
        }
    }

    public boolean canGoBack() {
        return false;
    }

    public void goBack() {
    }

    @Override
    protected void enter(String tag, String path) {
    }

    @Override
    public void processDirectionKey(int keyCode) {
    }

    @Override
    public void showMenu() {
    }
}
