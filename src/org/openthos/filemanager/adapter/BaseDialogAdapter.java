package org.openthos.filemanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.openthos.filemanager.R;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.system.FileViewInteractionHub;

import java.util.ArrayList;

import static android.R.color.holo_purple;
import static android.R.color.transparent;

/**
 * Created by root on 6/16/17.
 */


public class BaseDialogAdapter extends BaseAdapter implements View.OnHoverListener {
    private Context mContext;
    private FileViewInteractionHub mFileViewInteractionHub;
    private ArrayList mData;
    private boolean mCanCopy;

    public BaseDialogAdapter(Context context, ArrayList data,
                             FileViewInteractionHub fileViewInteractionHub, boolean canCopy) {
        mContext = context;
        mData = data;
        mFileViewInteractionHub = fileViewInteractionHub;
        mCanCopy = canCopy;
    }

    public BaseDialogAdapter(Context context, ArrayList data,
                             FileViewInteractionHub fileViewInteractionHub) {
        mContext = context;
        mData = data;
        mFileViewInteractionHub = fileViewInteractionHub;
    }

    public BaseDialogAdapter(Context context, ArrayList data) {
        mContext = context;
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = View.inflate(mContext, R.layout.dialog_base_item, null);
        TextView mTvDialogItem = (TextView) view.findViewById(R.id.dialog_base_item);
        String content = mData.get(i).toString();
        mTvDialogItem.setText(content);
        boolean isSetHoverListener = true;
        if (content.equals(mContext.getString(R.string.operation_paste).toString())) {
            if (mCanCopy) {
                mTvDialogItem.setTextColor(Color.BLACK);
            } else {
                mTvDialogItem.setTextColor(Color.LTGRAY);
                isSetHoverListener = false;
            }
        } else if (content.equals(mContext.getString(R.string.operation_compress))) {
            switch (mFileViewInteractionHub.getCompressFileState()) {
                case Constants.COMPRESSIBLE:
                case Constants.COMPRESSIBLE_DECOMPRESSIBLE:
                    mTvDialogItem.setTextColor(Color.BLACK);
                    break;
                case Constants.DECOMPRESSIBLE:
                    if (mFileViewInteractionHub.getSelectedFileList().size() > 1) {
                        mTvDialogItem.setTextColor(Color.BLACK);
                    } else {
                        mTvDialogItem.setTextColor(Color.LTGRAY);
                        isSetHoverListener = false;
                    }
                    break;
                default:
                    break;
            }
        } else if (content.equals(mContext.getString(R.string.operation_decompress))) {
            switch (mFileViewInteractionHub.getCompressFileState()) {
                case Constants.COMPRESSIBLE:
                    mTvDialogItem.setTextColor(Color.LTGRAY);
                    isSetHoverListener = false;
                    break;
                case Constants.DECOMPRESSIBLE:
                case Constants.COMPRESSIBLE_DECOMPRESSIBLE:
                    mTvDialogItem.setTextColor(Color.BLACK);
                    break;
                default:
                    break;
            }
        }
        if (isSetHoverListener) {
            view.setOnHoverListener(this);
            view.setTag(content);
        } else {
            view.setTag("");
        }
        return view;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_HOVER_ENTER:
                v.setBackgroundColor(mContext.getResources().getColor(holo_purple));
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                v.setBackgroundColor(mContext.getResources().getColor(transparent));
                break;
        }
        return false;
    }
}
