package org.openthos.filemanager.component;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.R;
import org.openthos.filemanager.bean.FolderBean;

import java.util.ArrayList;
import java.util.List;

public class FolderCollectionDialog extends Dialog {
    private Context mContext;
    private ListView mListView;
    private List<FolderBean> mFolderBeanList;
    private List<Integer> mChangedIndexList = new ArrayList<>();
    private final int MAX_VISIBLE_ITEM_COUNT = 8;

    public FolderCollectionDialog(@NonNull Context context, List<FolderBean> folderBeanList) {
        super(context, R.style.menu_dialog);
        mContext = context;
        mFolderBeanList = folderBeanList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        View rootView = LayoutInflater.from(mContext)
                .inflate(R.layout.collect_folders_dialog, null);
        setContentView(rootView);
        mListView = (ListView) rootView.findViewById(R.id.list_view);
        mListView.setAdapter(new FolderListAdapter());
        rootView.findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mChangedIndexList.size() != 0) {
                    ((MainActivity) mContext).handleCollectedChange(mChangedIndexList);
                    mChangedIndexList.clear();
                }
            }
        });
        rootView.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setListViewWidthAndHeight();
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }

    private class FolderListAdapter extends BaseAdapter {

        View.OnClickListener layoutClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder) v.getTag();
                holder.cb.setChecked(!holder.cb.isChecked());
                int index = Integer.parseInt(holder.tv.getTag().toString());
                if (mChangedIndexList.contains(index)) {
                    mChangedIndexList.remove((Integer) index);
                } else {
                    mChangedIndexList.add(index);
                }
            }
        };

        @Override
        public int getCount() {
            return mFolderBeanList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFolderBeanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext)
                        .inflate(R.layout.collect_folders_dialog_item, parent, false);
                convertView.setOnClickListener(layoutClickListener);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            FolderBean bean = mFolderBeanList.get(position);
            holder.iv.setImageResource(bean.getSmallIconRes());
            holder.tv.setText(bean.getTitle());
            holder.tv.setTag(position);
            holder.cb.setChecked(bean.isCollected());
            return convertView;
        }
    }

    private class ViewHolder {
        ImageView iv;
        TextView tv;
        CheckBox cb;

        private ViewHolder(View view) {
            iv = (ImageView) view.findViewById(R.id.image_view);
            tv = (TextView) view.findViewById(R.id.text_view);
            cb = (CheckBox) view.findViewById(R.id.check_box);
        }
    }

    private void setListViewWidthAndHeight() {
        ViewGroup.LayoutParams lp = mListView.getLayoutParams();
        View view = View.inflate(mContext, R.layout.collect_folders_dialog_item, null);
        TextView tv = (TextView) view.findViewById(R.id.text_view);
        for (FolderBean bean : mFolderBeanList) {
            tv.setText(bean.getTitle());
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            if (lp.width < view.getMeasuredWidth()) {
                lp.width = view.getMeasuredWidth();
            }
        }
        if (mFolderBeanList.size() > MAX_VISIBLE_ITEM_COUNT) {
            lp.height = view.getMeasuredHeight() * MAX_VISIBLE_ITEM_COUNT;
        } else {
            lp.height = view.getMeasuredHeight() * mFolderBeanList.size();
        }
    }
}
