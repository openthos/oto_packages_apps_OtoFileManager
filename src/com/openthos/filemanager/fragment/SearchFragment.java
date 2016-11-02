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

import java.util.ArrayList;

public class SearchFragment extends BaseFragment{
    private String LOG_TAG = "SearchFragment";
//    private ArrayList<SearchInfo> mSearchList = new ArrayList<>();
//    FragmentManager manager = getFragmentManager();

    private ListView lv_mian_search;
    @SuppressLint({"NewApi", "ValidFragment"})
    public SearchFragment(FragmentManager manager, ArrayList<SearchInfo> mFileList) {
        super();
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
                manager.popBackStack();
                manager.beginTransaction().replace(R.id.fl_mian,
                        new SystemSpaceFragment("search_fragment",fileRealPath,null,null)).commit();
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
