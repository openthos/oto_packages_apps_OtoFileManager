package com.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.SearchInfo;
import com.openthos.filemanager.system.FileIconHelper;
import com.openthos.filemanager.system.IntentBuilder;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.system.Constants;
import java.io.File;
import java.util.ArrayList;

public class SearchFragment extends BaseFragment{
    private static final String TAG = Constants.LEFT_FAVORITES;
    private Fragment mCurFragment;
    private ListView lv_mian_search;
    private SearchAdapter mSearchAdapter;
    private MainActivity mActivity;
    private LinearLayout mLlEmptyView;

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
        mLlEmptyView = (LinearLayout) rootView.findViewById(R.id.empty_view);
        if (mSearchList == null) {
            lv_mian_search.setVisibility(View.GONE);
            mLlEmptyView.setVisibility(View.VISIBLE);
        } else {
            lv_mian_search.setVisibility(View.VISIBLE);
            mLlEmptyView.setVisibility(View.GONE);
        }
    }

    protected void initData() {
        mSearchAdapter = new SearchAdapter();
        lv_mian_search.setAdapter(mSearchAdapter);
    }

    @Override
    protected void initListener() {
        lv_mian_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String fileRealPath = mSearchList.get(i).fileAbsolutePath;
                if (!new File(fileRealPath).isDirectory()) {
                    Context context = getActivity();
                    try {
                        IntentBuilder.viewFile(context,fileRealPath,null);
                    } catch (Exception e) {
                        Toast.makeText(context,getString(
                                       R.string.found_no_corresponding_application_to_open),
                                       Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (mCurFragment != null) {
                        mManager.beginTransaction().remove(mCurFragment).commit();
                    }
                    mActivity = (MainActivity) getActivity();
                    mManager.beginTransaction().hide(mActivity.getVisibleFragment()).commit();
                    mCurFragment = new SystemSpaceFragment(TAG, fileRealPath, null,null, false);
                    mManager.beginTransaction().add(R.id.fl_mian, mCurFragment,
                            Constants.SEARCHSYSTEMSPACE_TAG).commit();
                    mActivity.mCurFragment = mCurFragment;
                    notifyModify();
                }
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
            return mSearchList == null ? -1 : mSearchList.size();
        }

        @Override
        public Object getItem(int i) {
            return mSearchList == null ? -1 : mSearchList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (mSearchList != null) {
                view = View.inflate(getActivity(), R.layout.search_file_item,null);
                TextView search_file_name = (TextView) view.findViewById(R.id.search_file_name);
                ImageView image = (ImageView) view.findViewById(R.id.search_file_bg);
                String fileName = mSearchList.get(i).fileName;
                search_file_name.setText(fileName);
                String fileAbsolutePath = mSearchList.get(i).fileAbsolutePath;
                String filePath = mSearchList.get(i).filePath;
                boolean isDirectory = new File(filePath).isDirectory();
                int fileIcon = FileIconHelper.getFileIcon(Util.getExtFromFilename(filePath));
                image.setBackgroundResource(!isDirectory ? fileIcon : R.mipmap.folder);
                return view;
            } else {
                return null;
            }
        }
    }

    public void notifyModify() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSearchAdapter != null) {
                    mSearchAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalCache.setSearchText(null);
    }

    @Override
    protected void enter(String tag, String path) {
    }
}
