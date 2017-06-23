package com.openthos.filemanager.component;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.BaseDialog;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OpenWithDialog extends BaseDialog implements AdapterView.OnItemClickListener {

    private TextView mTextView;
    private List<ResolveInfo> mResolveList;
    private ResolveAdapter mResolveAdapter;
    private PackageManager mPackageManager;
    private String mFilePath;
    private String mFileType;

    public OpenWithDialog(Context context, String filePath) {
        super(context);
        mActivity = (MainActivity) context;
        mFilePath = filePath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_with_dialog);
        initView();
        initListener();
    }

    @Override
    protected void initData() {

    }

    protected void initListener() {
        mListView.setOnItemClickListener(this);
    }

    private void initView() {
        mPackageManager = mActivity.getPackageManager();
        mListView = (ListView) findViewById(R.id.lv_open_with);
        mTextView = (TextView) findViewById(R.id.tv_no_open_with);
        initList();
        if (mResolveList.size() > 0) {
            mResolveAdapter = new ResolveAdapter();
            mListView.setAdapter(mResolveAdapter);
            mListView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
            if (mResolveList.size() > Constants.LIMIT_FILES_NUM) {
                ViewGroup.LayoutParams params = mListView.getLayoutParams();
                params.height = Constants.LIMIT_FILES_HEIGHT;
                mListView.setLayoutParams(params);
            }
            mResolveAdapter.notifyDataSetChanged();
        } else {
            mListView.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    private void initList() {
        mResolveList = new ArrayList<>();
        mFileType = Constants.getMIMEType(new File(mFilePath));
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(mFilePath)), mFileType);
        mResolveList = mPackageManager.queryIntentActivities(intent,
                                           PackageManager.MATCH_DEFAULT_ONLY);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String packageName = mResolveList.get(i).activityInfo.packageName;
        String className = mResolveList.get(i).activityInfo.name;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName cn = new ComponentName(packageName, className);
        intent.setDataAndType(Uri.fromFile(new File(mFilePath)), mFileType);
        intent.setComponent(cn);
        mActivity.startActivity(intent);
        dismiss();
    }

    private class ResolveAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mResolveList.size();
        }

        @Override
        public Object getItem(int i) {
            return mResolveList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            View convertView = view;
            if (convertView == null) {
                convertView = LayoutInflater.from(mActivity).
                                             inflate(R.layout.list_item, viewGroup, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();
            holder.text.setText(mResolveList.get(i).loadLabel(mPackageManager));
            holder.image.setImageDrawable(mResolveList.get(i).loadIcon(mPackageManager));
            return convertView;
        }
    }

    private class ViewHolder {
        public TextView text;
        public ImageView image;

        public ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.tv_list_item);
            image = (ImageView) view.findViewById(R.id.iv_list_item);
        }
    }
}
