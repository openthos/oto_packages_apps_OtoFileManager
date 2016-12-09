package com.openthos.filemanager.view;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.system.Constants;

public class ListOnGenericMotionListener implements View.OnGenericMotionListener {
    private ListView file_path_list;
    private FileViewInteractionHub mFileViewInteractionHub;
    private int mLastClickId;
    private long mLastClickTime = 0;
    private boolean mIsCtrlPress;

    public ListOnGenericMotionListener(ListView file_path_list,
                                       FileViewInteractionHub mFileViewInteractionHub,
                                       boolean isCtrlPress) {
        this.mFileViewInteractionHub = mFileViewInteractionHub;
        this.file_path_list = file_path_list;
        this.mIsCtrlPress = isCtrlPress;
    }

    @Override
    public boolean onGenericMotion(View view, MotionEvent event) {
        switch (event.getButtonState()) {
            case MotionEvent.BUTTON_PRIMARY:
                if (event.getButtonState() == MotionEvent.BUTTON_PRIMARY) {
                    if (!mIsCtrlPress){
                        file_path_list.setOnItemClickListener(new ListItemClick(event));
                    }
                }
                break;
            case MotionEvent.BUTTON_SECONDARY:
                file_path_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view, int position, long id) {
                        if (mLastClickId != position){
                            mFileViewInteractionHub.clearSelection();
                        }
                        view.setSelected(true);
                        parent.getChildAt(position).findViewById(R.id.ll_list_item_bg)
                              .setSelected(true);
//                        mFileViewInteractionHub.addDialogSelectedItem(position);
                    }
                });
//                mFileViewInteractionHub.shownContextDialog(mFileViewInteractionHub);
                break;
            case MotionEvent.BUTTON_TERTIARY:
                if (event.getButtonState() == MotionEvent.BUTTON_TERTIARY){
                    file_path_list.setOnItemClickListener(new ListItemClick(event));
                }
                break;
            case MotionEvent.ACTION_SCROLL:
                mFileViewInteractionHub.MouseScrollAction(event);
                break;
            default:
                break;
        }
        return false;
    }

    private class ListItemClick implements AdapterView.OnItemClickListener {
        private MotionEvent event;
        public ListItemClick(MotionEvent event) {
            this.event = event;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mLastClickId == position
                && (Math.abs(System.currentTimeMillis() - mLastClickTime) < 1500)) {
                view.setSelected(false);

                mFileViewInteractionHub.onListItemClick(position,
                                                  Constants.DOUBLE_TAG, event, null);
                mFileViewInteractionHub.clearSelection();
            } else {
                view.setSelected(true);
                parent.getChildAt(position).findViewById(R.id.ll_list_item_bg).setSelected(true);
//                mFileViewInteractionHub.addDialogSelectedItem(position);
                mLastClickTime = System.currentTimeMillis();
                mLastClickId = position;
            }
        }
    }
}
