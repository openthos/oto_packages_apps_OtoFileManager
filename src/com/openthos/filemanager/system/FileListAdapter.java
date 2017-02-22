package com.openthos.filemanager.system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.drag.DragGridView;
import com.openthos.filemanager.drag.DragListView;
import com.openthos.filemanager.fragment.SearchFragment;
import com.openthos.filemanager.fragment.SystemSpaceFragment;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.IconHolder;

import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private FileViewInteractionHub mFileViewInteractionHub;
    private FileIconHelper mFileIcon;
    private Context mContext;
    private int layoutId;
    private List<FileInfo> fileInfoList;
    private List<Integer> selectFileInfoListIndex = new ArrayList<>();
    private View.OnTouchListener mMotionListener;
    private int mWidth, mHeight;
    private IconHolder mIconHolder;

    public FileListAdapter(Context context, int resource,
                           List<FileInfo> objects, FileViewInteractionHub f,
                           FileIconHelper fileIcon,
                           View.OnTouchListener motionListener) {
        fileInfoList = objects;
        layoutId = resource;
        mInflater = LayoutInflater.from(context);
        mFileViewInteractionHub = f;
        mFileIcon = fileIcon;
        mContext = context;
        mMotionListener = motionListener;
        initIconHolder();
    }

    private void initIconHolder() {
        mIconHolder = IconHolder.getIconHolder(mContext);
    }

    public List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }

    public List<Integer> getSelectFileInfoList() {
        return selectFileInfoListIndex;
    }

    @Override
    public int getCount() {
        return fileInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null)  {
            if ("list".equals(LocalCache.getViewTag())) {
                convertView = mInflater.inflate(R.layout.file_browser_item_list, parent, false);
            } else if ("grid".equals(LocalCache.getViewTag())) {
                convertView = mInflater.inflate(R.layout.file_browser_item_grid, parent, false);
            }
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
            convertView.setOnTouchListener(mMotionListener);
            ViewGroup.LayoutParams params = convertView.getLayoutParams();
            setParams(params.width, params.height);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.name.setTag(position);
        viewHolder.name.setOnTouchListener(mMotionListener);

        FileInfo lFileInfo = fileInfoList.get(position);
        FileListItem.setupFileListItemInfo(mContext, convertView, lFileInfo,
                                           mIconHolder, mFileViewInteractionHub);
        LinearLayout background = (LinearLayout)convertView;
        background.setBackgroundResource(selectFileInfoListIndex.contains(position) ?
                                         R.drawable.list_item_bg_shape : R.color.white);
//        convertView.findViewById(R.id.file_checkbox).setOnClickListener(
//                new FileListItem.FileItemOnClickListener(
//                        mFileViewInteractionHub));
        return convertView;
    }

    public static class ViewHolder {
        public TextView name;
        public ImageView icon;
        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.file_name);
            icon = (ImageView) view.findViewById(R.id.file_image);
        }
    }

    private void setParams(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int[] getParams() {
        return new int[] {mWidth, mHeight};
    }

    public void dispose() {
        if (mIconHolder != null) {
            mIconHolder.cleanup();
            mIconHolder = null;
        }
    }
}
