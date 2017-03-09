package com.openthos.filemanager.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.openthos.filemanager.R;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileIconHelper;
import com.openthos.filemanager.system.Util;

import java.io.File;

public class InformationDialog extends AlertDialog {
    protected static final int ID_USER = 100;
    private FileInfo mFileInfo;
    private FileIconHelper mFileIconHelper;
    private Context mContext;
    private View mView;

    public InformationDialog(Context context, FileInfo f, FileIconHelper iconHelper) {
        super(context);
        mFileInfo = f;
        mFileIconHelper = iconHelper;
        mContext = context;
    }

    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.information_dialog, null);

        if (mFileInfo.IsDir) {
            setIcon(R.mipmap.folder);
            asyncGetSize();
        } else {
            setIcon(R.mipmap.file_icon_default);
        }
        setTitle(mFileInfo.fileName);

        ((TextView) mView.findViewById(R.id.information_size))
                .setText(formatFileSizeString(mFileInfo.fileSize));
        ((TextView) mView.findViewById(R.id.information_location))
                .setText(mFileInfo.filePath);
        ((TextView) mView.findViewById(R.id.information_modified)).setText(Util
                .formatDateString(mContext, mFileInfo.ModifiedDate));
        ((TextView) mView.findViewById(R.id.information_canread))
                .setText(mFileInfo.canRead ? R.string.yes : R.string.no);
        ((TextView) mView.findViewById(R.id.information_canwrite))
                .setText(mFileInfo.canWrite ? R.string.yes : R.string.no);
        ((TextView) mView.findViewById(R.id.information_ishidden))
                .setText(mFileInfo.isHidden ? R.string.yes : R.string.no);

        setView(mView);
        setButton(BUTTON_NEGATIVE, mContext.getString(R.string.confirm_know),
                  (OnClickListener) null);

        super.onCreate(savedInstanceState);
    }

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ID_USER:
                    Bundle data = msg.getData();
                    long size = data.getLong("SIZE");
                    ((TextView) mView.findViewById(R.id.information_size))
                                .setText(formatFileSizeString(size));
            }
        }
    };

    private AsyncTask task;
    @SuppressWarnings("unchecked")
    private void asyncGetSize() {
        task = new AsyncTask() {
            private long size;

            @Override
            protected Object doInBackground(Object... params) {
                String path = (String) params[0];
                size = 0;
                getSize(path);
                task = null;
                return null;
            }

            private void getSize(String path) {
                if (isCancelled())
                    return;
                File file = new File(path);
                if (file.isDirectory()) {
                    File[] listFiles = file.listFiles();
                    if (listFiles == null)
                        return;

                    for (File f : listFiles) {
                        if (isCancelled())
                            return;

                        getSize(f.getPath());
                    }
                } else {
                    size += file.length();
                    onSize(size);
                }
            }

        }.execute(mFileInfo.filePath);
    }

    private void onSize(final long size) {
        Message msg = new Message();
        msg.what = ID_USER;
        Bundle bd = new Bundle();
        bd.putLong("SIZE", size);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private String formatFileSizeString(long size) {
        String ret;
        if (size >= 1024) {
            ret = Util.convertStorage(size);
            ret += (" (" + mContext.getResources().getString(R.string.file_size, size) + ")");
        } else {
            ret = mContext.getResources().getString(R.string.file_size, size);
        }

        return ret;
    }
}
