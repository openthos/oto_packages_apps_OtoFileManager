package com.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DialerFilter;
import android.widget.EditText;
import android.widget.GridView;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.R;
import com.openthos.filemanager.adapter.SambaAdapter;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.utils.SambaUtils;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class SambaFragment extends BaseFragment {
    private GridView mGv;
    private SambaAdapter mAdapter;
    private ArrayList<String> mList = new ArrayList<>();
    private GridViewOnGenericMotionListener mMotionListener;
    public Fragment mCurFragment;
    private long mCurrentTime = 0L;
    private int mPos = -1;
    private String mAccount = "";
    private String mPassword = "";
    private String mPath = "";
    private String mSuffix = "";
    private ArrayList<String> mPonits = new ArrayList<>();
    private ArrayList<String> mFiles = new ArrayList<>();

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
        scanNet();
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
//        mAdapter.setData(librarys);
    }

    @Override
    public boolean canGoBack() {
        if (mPath.contains("/")) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void goBack() {
        mSuffix = "";
        if (mPath.substring(0, mPath.lastIndexOf("/")).contains("/")) {
            mPath = mPath.substring(0, mPath.substring(0, mPath.length() - 1).lastIndexOf("/")) + "/";
            enter();
        } else {
            mPath = "";
            mAdapter.setIsPointPage(true);
            scanNet();
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
        scanNet();
    }

    private void showDialog(MotionEvent motionEvent) {
        scanNet();
    }

    @Override
    public void enter() {
        super.enter();
        mPath = mPath + mSuffix;
        mFiles.clear();
        new Thread() {
            @Override
            public void run() {
                super.run();
                int result = SambaUtils.connect(mFiles, mAccount, mPassword, mPath);
                switch (result) {
                    case SambaUtils.SAMBA_OK:
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
                        mMainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                InputAccountDialog dialog = new InputAccountDialog(mMainActivity);
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

    @Override
    public void enter(String tag, String path) {
        enter();
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

    private void scanNet() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                mPonits = SambaUtils.scanNet();
                mList.clear();
                mList.addAll(mPonits);
                mGv.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
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
                    mAccount = mEtAccount.getText().toString();
                    mPassword = mEtPassword.getText().toString();
                    if (mPath.substring(0, mPath.lastIndexOf("/")).contains("/")) {
                        mPath = mPath.substring(0, mPath.lastIndexOf("/")).substring(0, mPath.lastIndexOf("/"));
                    } else {
                        mPath = "";
                    }
//                    mPath = mPath.replace(mSuffix ,"");
                    Log.i("wwww", mPath);
                    enter();
                    dismiss();
                }
            });
            mBtCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        @Override
        public void show() {
            super.show();
        }
    }
}
