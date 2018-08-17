package org.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.openthos.filemanager.BaseMenuDialog;
import org.openthos.filemanager.BaseFragment;
import org.openthos.filemanager.R;
import org.openthos.filemanager.adapter.BaseDialogAdapter;
import org.openthos.filemanager.adapter.SambaAdapter;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.system.IntentBuilder;
import org.openthos.filemanager.utils.SambaUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

@SuppressLint("ValidFragment")
public class SambaFragment extends BaseFragment {
    private GridView mGv;
    private SambaAdapter mAdapter;
    private TextView mTextSacan, mTextNoHost;
    private ArrayList<String> mList = new ArrayList<>();
    private GridViewOnGenericMotionListener mMotionListener;
    public Fragment mCurFragment;
    private long mCurrentTime = 0L;
    private int mPos = -1;
    private String mCurrentPath = "";
    private String mSuffix = "";
    private ArrayList<String> mPoints = new ArrayList<>();
    private ArrayList<String> mFiles = new ArrayList<>();
    private Stack<String> mPaths = new Stack<>();
    private HashMap<String, Key> mKeys = new HashMap();

    private class Key {
        String username;
        String password;

        Key(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.samba_fragment_layout;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void initView() {
        mGv = (GridView) rootView.findViewById(R.id.gv_samba);
        mGv.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mMotionListener = new GridViewOnGenericMotionListener();
        mAdapter = new SambaAdapter(mMainActivity, mList, mMotionListener);
        mGv.setAdapter(mAdapter);
        mTextSacan = (TextView) rootView.findViewById(R.id.text_scan);
        mTextNoHost = (TextView) rootView.findViewById(R.id.text_no_host);
    }

    protected void initData() {
    }

    @Override
    protected void initListener() {
        mGv.setOnTouchListener(mMotionListener);
    }

    public ArrayList<String> getList() {
        return mList;
    }

    public SambaAdapter getAdapter() {
        return mAdapter;
    }

    public void setData(ArrayList<String> librarys) {
        mList = librarys;
    }

    @Override
    public boolean canGoBack() {
        if (mPaths.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void goBack() {
        mSuffix = "";
        if (mPaths.size() > 1) {
            mAdapter.setIsPointPage(false);
            mPaths.pop();
            enter();
        } else if (mPaths.size() == 1) {
            mPaths.pop();
            if (!mPoints.isEmpty()) {
                mAdapter.setIsPointPage(true);
                mList.clear();
                mList.addAll(mPoints);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public class GridViewOnGenericMotionListener implements View.OnTouchListener {
        private boolean mIsShowDialog = false;
        private boolean mIsItem = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMainActivity.clearNivagateFocus();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (view.getTag() instanceof SambaAdapter.ViewHolder) {
                        mAdapter.clearSelected();
                        mAdapter.setSelected(view);
                        if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                            mIsShowDialog = true;
                            mIsItem = true;
                        }
                        int pos = (int) ((SambaAdapter.ViewHolder) view.getTag()).name.getTag();
                        if (System.currentTimeMillis() - mCurrentTime
                                < Constants.DOUBLE_CLICK_INTERVAL_TIME
                                && motionEvent.getButtonState() != MotionEvent.BUTTON_SECONDARY) {
                            if (mPos != pos) {
                                mPos = pos;
                                mCurrentTime = System.currentTimeMillis();
                                return true;
                            }
                            enter();
                        } else {
                            mPos = pos;
                            mSuffix = mList.get(pos);
                            mCurrentTime = System.currentTimeMillis();
                        }
                        return true;
                    } else {
                        mAdapter.clearSelected();
                        if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                            mPos = -1;
                            mIsShowDialog = true;
                            mIsItem = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mIsShowDialog == true) {
                        if (mIsItem) {
                            showItemDialog(motionEvent, mPos);
                        } else {
                            showDialog(motionEvent);
                        }
                        mIsShowDialog = false;
                    }
            }
            return false;
        }
    }

    private void showItemDialog(MotionEvent motionEvent, int pos) {
//        new MenuDialog().showDialog(motionEvent);
    }

    private void showDialog(MotionEvent motionEvent) {
        new MenuDialog(getActivity()).showDialog(
                (int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    @Override
    public void enter() {
        super.enter();
        mCurrentPath = "";
        mCurrentTime = 0;
        for (String path : mPaths)
            mCurrentPath += path;
        String point;
        if (mPaths.size() == 0) {
            point = mSuffix;
        } else {
            point = mPaths.get(0);
        }
        Key key = mKeys.get(point);
        if (key == null) {
            key = new Key("", "");
        }

        final Key finalKey = key;
        if (!TextUtils.isEmpty(mSuffix) && !mSuffix.endsWith("/")) {
            final File f = new File(SambaUtils.BASE_DIRECTORY, mCurrentPath + mSuffix);
            if (f.exists()) {
                IntentBuilder.viewFile(mMainActivity, f.getAbsolutePath(), null);
            } else {
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setCancelable(false);
                dialog.setTitle(mMainActivity.getString(R.string.samba_downloading));
                dialog.show();

                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        final boolean isOk = SambaUtils.download(
                                finalKey.username, finalKey.password, mCurrentPath + mSuffix);
                        mMainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                if (isOk) {
                                    IntentBuilder.viewFile(mMainActivity, f.getAbsolutePath(), null);
                                } else {
                                    Toast.makeText(mMainActivity, mMainActivity.getString(
                                            R.string.download_falut), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }.start();
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    if (mPaths.size() == 1) {
                        Process pro;
                        BufferedReader in = null;
                        boolean isMounted = false;
                        String localPath = mPaths.get(0) + mSuffix;
                        File localFile = new File("/storage/samba", localPath);
                        try {
                            pro = Runtime.getRuntime().exec(
                                    new String[]{"su", "-c", "mount"});
                            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                            String line;
                            String tempPath = localPath.substring(
                                    0, localPath.length() - 1).replace(" ", "\\040") + " ";
                            while ((line = in.readLine()) != null) {
                                if (line.contains(tempPath)) {
                                    isMounted = true;
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (!isMounted || localFile.length() == 0) {
                            try {
                                String line;
                                if (localFile.exists()) {
                                    if (isMounted){
                                        pro = Runtime.getRuntime().exec(
                                                new String[]{"su", "-c", "umount "
                                                        + localFile.getAbsolutePath()
                                                        .replace(" ", "\\ ")});
                                        in = new BufferedReader(
                                                new InputStreamReader(pro.getErrorStream()));
                                        while ((line = in.readLine()) != null) {
                                        }
                                    }
                                    pro = Runtime.getRuntime().exec(
                                            new String[]{"rm", "-r", localFile.getAbsolutePath()});
                                    in = new BufferedReader(
                                            new InputStreamReader(pro.getErrorStream()));
                                    while ((line = in.readLine()) != null) {
                                    }
                                }
                                localFile.mkdirs();
                                pro = Runtime.getRuntime().exec(
                                        new String[]{"su", "-c", "busybox mount -t cifs //"
                                                + localPath.substring(0, localPath.length() - 1)
                                                .replace(" ", "\\ ")
                                                + " " + localFile.getAbsolutePath()
                                                .replace(" ", "\\ ")
                                                + " -o user=" + finalKey.username + ",password="
                                                + finalKey.password + ",iocharset=utf8,"});
                                in = new BufferedReader(new InputStreamReader(pro.getErrorStream()));
                                while ((line = in.readLine()) != null) {
                                    if (line.contains("Device or resource busy")) {
                                        isMounted = true;
                                    } else if (line.contains("Permission denied")) {
                                        mMainActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                InputAccountDialog dialog
                                                        = new InputAccountDialog(mMainActivity);
                                                dialog.show();
                                            }
                                        });
                                        return;
                                    }
                                }
                                isMounted = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (in != null) {
                                    try {
                                        in.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (isMounted) {
                            showFragment(localFile.getAbsolutePath());
                        }
                        return;
                    }
                    mCurrentPath += mSuffix;
                    mFiles.clear();
                    int result = SambaUtils.connect(
                            mFiles, finalKey.username, finalKey.password, mCurrentPath);
                    switch (result) {
                        case SambaUtils.SAMBA_OK:
                            if (!TextUtils.isEmpty(mSuffix)) {
                                mPaths.add(mSuffix);
                            }
                            mList.clear();
                            mList.addAll(mFiles);
                            mAdapter.setIsPointPage(false);
                            mGv.post(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        case SambaUtils.SAMBA_WRONG_ACCOUNT:
                        case SambaUtils.SAMBA_ACCESS_DENIED:
                            mMainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    InputAccountDialog dialog
                                            = new InputAccountDialog(mMainActivity);
                                    dialog.show();
                                }
                            });
                            break;
                        case SambaUtils.SAMBA_WRONG_NETWORK:
                            break;
                        case SambaUtils.SAMBA_NOT_FOUND:
                            break;

                    }
                }
            }.start();
        }
    }

    private void showFragment(final String path) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMainActivity.showFileSpaceFragment(path);
            }
        });
    }

    @Override
    public void processDirectionKey(int keyCode) {
    }

    @Override
    public void showMenu() {
    }

    @Override
    public void clearSelectList() {
    }

    private Thread scan;

    private void scanNet() {
        if (scan != null && scan.isAlive()) {
            return;
        }
        mTextSacan.setVisibility(View.VISIBLE);
        mTextNoHost.setVisibility(View.GONE);
        mGv.setVisibility(View.GONE);
        final boolean isVisible = isVisible();
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.text_scanning));
        if (isVisible) {
            dialog.show();
        }
        mAdapter.setIsPointPage(true);
        mList.clear();
        mPaths.clear();
        mAdapter.notifyDataSetChanged();
        scan = new Thread() {
            @Override
            public void run() {
                super.run();
                mPoints = SambaUtils.scanNet();
                mGv.post(new Runnable() {
                    @Override
                    public void run() {
                        mTextSacan.setVisibility(View.GONE);
                        if (mPoints != null && mPoints.size() > 0) {
                            mGv.setVisibility(View.VISIBLE);
                            mList.clear();
                            mList.addAll(mPoints);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(mMainActivity, mMainActivity.getString(
                                    R.string.no_samba_server), Toast.LENGTH_SHORT).show();
                            mTextNoHost.setVisibility(View.VISIBLE);
                        }
                        if (isVisible) {
                            dialog.cancel();
                        }
                    }
                });
            }
        };
        scan.start();
    }

    private class InputAccountDialog extends Dialog {

        EditText mEtAccount, mEtPassword;
        Button mBtConfirm, mBtCancel;

        public InputAccountDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_samba_account);
            mEtAccount = (EditText) findViewById(R.id.account);
            mEtPassword = (EditText) findViewById(R.id.password);
            mBtConfirm = (Button) findViewById(R.id.yes);
            mBtCancel = (Button) findViewById(R.id.no);
            mBtConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String point;
                    if (mPaths.size() == 0) {
                        point = mSuffix;
                    } else {
                        point = mPaths.get(0);
                    }
                    mKeys.put(point, new Key(mEtAccount.getText().toString(),
                            mEtPassword.getText().toString()));
                    enter();
                    dismiss();
                }
            });
            mBtCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSuffix = "";
                    dismiss();
                }
            });
        }

        @Override
        public void show() {
            super.show();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden && (mPoints == null || mPoints.size() == 0)) {
            scanNet();
        }
    }

    private class MenuDialog extends BaseMenuDialog implements ListView.OnItemClickListener {
        private Context mContext;


        public MenuDialog(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        protected void initData() {
            mDatas = new ArrayList();
            mDatas.add(mContext.getString(R.string.dialog_scan));
            mListView.setAdapter(new BaseDialogAdapter(getContext(), mDatas, null, false));
        }

        @Override
        protected void initListener() {
            mListView.setOnItemClickListener(this);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            scanNet();
            dismiss();
        }

        @Override
        public void showDialog(int x, int y) {
            super.showDialog(x, y);
        }
    }
}
