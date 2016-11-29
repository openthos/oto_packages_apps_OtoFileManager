package com.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.SearchInfo;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.fragment.SystemSpaceFragment;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.Constants;

import java.util.ArrayList;

public class SearchFragment extends BaseFragment{
    private static final String TAG = SearchFragment.class.getSimpleName();
    private BaseFragment mCurFragment;
    private ArrayList<FileInfo> mFileInfoArrayList;
    private FileViewInteractionHub.CopyOrMove mCopyOrMove;
    private String LOG_TAG = "SearchFragment";
//    private ArrayList<SearchInfo> mSearchList = new ArrayList<>();
//    FragmentmManager mManager = getFragmentmManager();

    private ListView lv_mian_search;
    @SuppressLint({"NewApi", "ValidFragment"})
    public SearchFragment(FragmentManager manager, ArrayList<SearchInfo> mFileList) {
        super(manager,mFileList);
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public SearchFragment() {
        super();
    }

    @Override
    public int getLayoutId() {
        return R.layout.search_fragment_layout;
    }

    @Override
    protected void initView() {
        lv_mian_search = (ListView) rootView.findViewById(R.id.lv_mian_search);
    }

    protected void initData() {
        L.e("initData"+LOG_TAG,mSearchList.size()+"");
        SearchAdapter searchAdapter = new SearchAdapter();
        lv_mian_search.setAdapter(searchAdapter);
    }

    @Override
    protected void initListener() {
        lv_mian_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String filePath = mSearchList.get(i).getFilePath();
                String fileRealPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
                //mManager.popBackStack();
                mManager.beginTransaction().hide(mMainActivity.mCurFragment).commit();
                if (mCurFragment != null) {
                    mFileInfoArrayList = ((SystemSpaceFragment) mCurFragment).getFileInfoList();
                    mCopyOrMove = ((SystemSpaceFragment) mCurFragment).getCurCopyOrMoveMode();
                    mCurFragment = new SystemSpaceFragment(TAG, fileRealPath, mFileInfoArrayList,
                                                           mCopyOrMove);
                } else {
                    mCurFragment = new SystemSpaceFragment(TAG, fileRealPath, null, null);
                }
                mManager.beginTransaction().add(R.id.fl_mian, mCurFragment,
                              Constants.SYSTEMSPACEFRAGMENT_TAG)
                        .show(mCurFragment).addToBackStack(null).commit();
                mMainActivity.mCurFragment = mCurFragment;
            }
        });
    }

    @Override
    public boolean canGoBack() {
        return false;
    }

    @Override
    public void goBack() {

    }
    private class SearchAdapter extends BaseAdapter {

        @Override
        public int getCount() {
//            L.e("mSearchList"+LOG_TAG,mSearchList.size()+"");
            return mSearchList.size();
        }

        @Override
        public Object getItem(int i) {
            return mSearchList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = View.inflate(getActivity(), R.layout.search_file_item,null);
            TextView search_file_name = (TextView) view.findViewById(R.id.search_file_name);
            search_file_name.setText(mSearchList.get(i).fileName);
            return view;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalCache.setSearchText(null);
    }
}
