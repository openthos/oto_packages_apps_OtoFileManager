package com.openthos.filemanager.system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.openthos.filemanager.R;
import com.openthos.filemanager.utils.LocalCache;

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
    public FileListAdapter(Context context, int resource,
                           List<FileInfo> objects, FileViewInteractionHub f,
                           FileIconHelper fileIcon) {
        fileInfoList = objects;
        layoutId = resource;
        mInflater = LayoutInflater.from(context);
        mFileViewInteractionHub = f;
        mFileIcon = fileIcon;
        mContext = context;
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
        if (convertView == null)  {
            if ("list".equals(LocalCache.getViewTag())) {
                convertView = mInflater.inflate(R.layout.file_browser_item_list, parent, false);
            } else if ("grid".equals(LocalCache.getViewTag())) {
                convertView = mInflater.inflate(R.layout.file_browser_item_grid, parent, false);
            }
        }

        final FileInfo lFileInfo = fileInfoList.get(position);
        FileListItem.setupFileListItemInfo(mContext, convertView, position, lFileInfo,
                mFileIcon, mFileViewInteractionHub);
        LinearLayout background = (LinearLayout)convertView;
        background.setBackgroundResource(selectFileInfoListIndex.contains(position) ?
                                         R.drawable.list_item_bg_shape : R.color.white);
//        convertView.findViewById(R.id.file_checkbox).setOnClickListener(
//                new FileListItem.FileItemOnClickListener(
//                        mFileViewInteractionHub));
        return convertView;
    }
}
