package com.openthos.filemanager.component;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.bean.SearchInfo;
import com.openthos.filemanager.fragment.PersonalSpaceFragment;
import com.openthos.filemanager.fragment.SdStorageFragment;
import com.openthos.filemanager.fragment.SearchFragment;
import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.system.FileCategoryHelper;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.Settings;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.T;
import android.support.v4.app.Fragment;
import com.openthos.filemanager.system.Constants;
import java.io.File;
import java.util.ArrayList;
import android.widget.Toast;

public class SearchOnKeyListener implements TextView.OnKeyListener {
    private String mInputData;
    private String LOG_TAG = "SearchOnQueryTextListener";
    private static ProgressDialog progressDialog;
    private ArrayList<FileInfo> mFileList = new ArrayList<>();
    private FragmentManager mManager;
    private String mInputText;
    private MainActivity mMainActivity;
    private String mCurPath;
    private SearchFragment mCurSearchFragment;

    public SearchOnKeyListener(FragmentManager manager,
                               Editable text, MainActivity context) {
        mManager = manager;
        mMainActivity = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("search...");
    }

    public void setInputData(String inputData) {
        mInputData = inputData;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                v.clearFocus();
                if (mMainActivity.mCurFragment instanceof SdStorageFragment) {
                    mCurPath = Constants.ROOT_PATH;
                } else if (mMainActivity.mCurFragment instanceof PersonalSpaceFragment) {
                    mCurPath = Constants.SDCARD_PATH;
                } else {
                    mCurPath = mMainActivity.getCurPath();
                }
                if (Constants.ROOT_PATH.equals(mCurPath)) {
                    mMainActivity.mHandler.sendEmptyMessage(Constants.MENU_SHOWHIDE);
                } else {
                    excuSearch((TextView) v);
                }
                return true;
            case KeyEvent.KEYCODE_ESCAPE:
                v.clearFocus();
                return true;
        }
        return false;
    }

    private void excuSearch(TextView input) {
        progressDialog.show();
        mInputText = input.getText().toString().trim();
        if (mFileList != null && mFileList.size() > 0) {
            mFileList.clear();
        }
        new Thread() {
            @Override
            public void run() {
                startSearch(mInputText);
            }
        }.start();
    }

    public ArrayList<FileInfo> refreshList() {
        mFileList.clear();
        startSearch(mInputText);
        return mFileList;
    }

    public void startSearch(String text_search) {
        File curFile = new File(mCurPath);
        if (curFile.exists() && curFile.isDirectory()) {
            final File[] currentFiles = curFile.listFiles();
            if (currentFiles != null && currentFiles.length != 0) {
                getFiles(text_search, currentFiles);
            }
            mMainActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mFileList != null && mFileList.size() > 0) {
                        startSearchFragment();
                    } else {
                        showEmptyView();
                    }
                }
            });
        }
    }

    private void getFiles(String text_search, File[] files) {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String fileName = file.getName();
            if (fileName.contains(text_search)) {
                FileInfo fileInfo = Util.GetFileInfo(file,
                        new FileCategoryHelper(mMainActivity).getFilter(),
                        Settings.instance().getShowDotAndHiddenFiles());

                if (mFileList != null && mFileList.contains(fileName)
                                      && mFileList.contains(file.getPath())) {
                    continue;
                } else {
                    mFileList.add(fileInfo);
                }
            }
            if (file.isDirectory()) {
                if (file.listFiles() != null) {
                    getFiles(text_search, file.listFiles());
                }
            }
        }
    }

    private void startSearchFragment() {
        mMainActivity.mStartSearchFragment = (BaseFragment) mMainActivity.getVisibleFragment();
        mManager.beginTransaction().
            hide(mMainActivity.getVisibleFragment()).commitAllowingStateLoss();
        if (mManager.findFragmentByTag(Constants.SEARCHFRAGMENT_TAG) != null) {
            mCurSearchFragment  = (SearchFragment) mManager
                                  .findFragmentByTag(Constants.SEARCHFRAGMENT_TAG);
            if (mCurSearchFragment != null) {
                mManager.beginTransaction().remove(mCurSearchFragment).commitAllowingStateLoss();
            }
        }
        mCurSearchFragment = new SearchFragment(this, mManager, mFileList);
        mManager.beginTransaction().add(R.id.fl_mian, mCurSearchFragment,
                                       Constants.SEARCHFRAGMENT_TAG).commitAllowingStateLoss();
        mMainActivity.mCurFragment = mCurSearchFragment;
        progressDialog.dismiss();
    }

    private void showEmptyView() {
        Toast.makeText(mMainActivity, mMainActivity.getString(R.string.found_no_file),
                       Toast.LENGTH_SHORT).show();
        if (!(mMainActivity.getVisibleFragment() instanceof SearchFragment)) {
            mMainActivity.mStartSearchFragment = (BaseFragment) mMainActivity.getVisibleFragment();
        }
        mManager.beginTransaction().
            hide(mMainActivity.getVisibleFragment()).commitAllowingStateLoss();
        if (mManager.findFragmentByTag(Constants.SEARCHFRAGMENT_TAG) != null) {
            mCurSearchFragment  = (SearchFragment) mManager
                                  .findFragmentByTag(Constants.SEARCHFRAGMENT_TAG);
            if (mCurSearchFragment != null) {
                mManager.beginTransaction().remove(mCurSearchFragment).commitAllowingStateLoss();
            }
        }
        mCurSearchFragment = new SearchFragment(this, mManager,null);
        mManager.beginTransaction().add(R.id.fl_mian, mCurSearchFragment,
                                       Constants.SEARCHFRAGMENT_TAG).commitAllowingStateLoss();
        mMainActivity.mCurFragment = mCurSearchFragment;
        progressDialog.dismiss();
    }
}
