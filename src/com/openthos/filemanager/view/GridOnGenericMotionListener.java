package com.openthos.filemanager.view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.T;
import com.openthos.filemanager.system.Constants;

public class GridOnGenericMotionListener implements View.OnGenericMotionListener {
    private GridView file_path_grid;
    private FileViewInteractionHub mFileViewInteractionHub;
    private int mLastClickId;
    private long mLastClickTime = 0;
    private Context context;
    private boolean mIsCtrlPress;

    public GridOnGenericMotionListener(Context mActivity, GridView file_path_grid,
                                       FileViewInteractionHub mFileViewInteractionHub) {
        this.mFileViewInteractionHub = mFileViewInteractionHub;
        this.file_path_grid = file_path_grid;
        this.context = mActivity;
    }

    public void setmIsCtrlPress(boolean mIsCtrlPress) {
        this.mIsCtrlPress = mIsCtrlPress;
    }

    @Override
    public boolean onGenericMotion(View view, final MotionEvent event) {
        switch (event.getButtonState()) {
            case MotionEvent.BUTTON_PRIMARY:
                break;
            case MotionEvent.BUTTON_SECONDARY:
                file_path_grid.setOnItemClickListener(new GridRigntItemClickListener());
                mFileViewInteractionHub.shownContextDialog(mFileViewInteractionHub, event);
                break;
            case MotionEvent.BUTTON_TERTIARY:
                file_path_grid.setOnItemClickListener(new GridLeftItemClickListener(event));
                break;
            case MotionEvent.ACTION_SCROLL:
                mFileViewInteractionHub.MouseScrollAction(event);
                break;
            case MotionEvent.ACTION_HOVER_ENTER:
                L.d("ACTION_HOVER_ENTER");
                break;
        }
        return false;
    }

    public class GridLeftItemClickListener implements AdapterView.OnItemClickListener {
        private MotionEvent event;

        public GridLeftItemClickListener(MotionEvent event) {
            this.event = event;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mLastClickId == position
                && (Math.abs(System.currentTimeMillis() - mLastClickTime) < 1200)) {
                mFileViewInteractionHub.onListItemClick(position,
                                                  Constants.DOUBLE_TAG, event, null);
                mFileViewInteractionHub.clearSelection();
            } else {
                parent.getChildAt(position).setSelected(true);
//                mFileViewInteractionHub.addDialogSelectedItem(position);
                mLastClickTime = System.currentTimeMillis();
                mLastClickId = position;
            }
        }
    }

    private class GridRigntItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            parent.getChildAt(position).findViewById(R.id.ll_grid_item_bg).setSelected(true);
//            mFileViewInteractionHub.addDialogSelectedItem(position);
        }
    }
}
