package com.openthos.filemanager;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.FragmentTransaction;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.app.ProgressDialog;

import com.openthos.filemanager.bean.SeafileAccount;
import com.openthos.filemanager.bean.SeafileLibrary;
import com.openthos.filemanager.component.CopyInfoDialog;
import com.openthos.filemanager.component.PopOnClickLintener;
import com.openthos.filemanager.component.PopWinShare;
import com.openthos.filemanager.component.SearchOnKeyListener;
import com.openthos.filemanager.fragment.OnlineNeighborFragment;
import com.openthos.filemanager.fragment.SdStorageFragment;
import com.openthos.filemanager.fragment.PersonalSpaceFragment;
import com.openthos.filemanager.fragment.SearchFragment;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.system.FileListAdapter;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.T;
import com.openthos.filemanager.fragment.SystemSpaceFragment;
import com.openthos.filemanager.system.IFileInteractionListener;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileOperationHelper;
import com.openthos.filemanager.fragment.SeafileFragment;
import com.openthos.filemanager.utils.SeafileUtils;
import com.openthos.filemanager.component.UsbPropertyDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity
                 implements View.OnClickListener {
    private static final int POPWINDOW_X = -15;
    private static final int POPWINDOW_Y = 10;
    private static final int USB_POPWINDOW_X = 60;
    private static final int USB_POPWINDOW_Y = 10;
    private static final int ACTIVITY_MIN_COUNT_FOR_BACK = 3;
    private static final String USB_SPACE_FRAGMENT = "usb_space_fragment";
    private static final String USB_DEVICE_ATTACHED = "usb_device_attached";
    private static final String USB_DEVICE_DETACHED = "usb_device_detached";
    private static final String VIEW_TAG = "viewtag";
    private static final String VIEW_TAG_GRID = "grid";
    private static final String VIEW_TAG_LIST = "list";
    private static final String IV_SWITCH_VIEW = "iv_switch_view";
    private static final String SETTING_POPWINDOW_TAG = "iv_setting";
    private static final String USB_POPWINDOW_TAG = "iv_usb";
    private TextView mTv_desk;
    private TextView mTv_music;
    private TextView mTv_video;
    private TextView mTv_computer;
    private TextView mTv_picture;
    private TextView mTv_document;
    private TextView mTv_download;
    private TextView mTv_recycle;
    private TextView mTv_cloud_service;
    private TextView mTv_net_service;
    private ImageView mIv_list_view;
    private ImageView mIv_grid_view;
    private ImageView mIv_back;
    private ImageView mIv_setting;
    private EditText mEt_nivagation;
    private EditText mEt_search_view;
    private ImageView mIv_search_view;
    private LinearLayout ll_usb;

    private FragmentManager mManager = getSupportFragmentManager();
    private PopWinShare mPopWinShare;
    public Fragment mCurFragment;
    public SdStorageFragment mSdStorageFragment;
    public boolean mIsSdStorageFragmentHided;
    private SystemSpaceFragment mDeskFragment, mMusicFragment, mVideoFragment,
                                mPictrueFragment, mAddressFragment,
                                mDocumentFragment, mDownloadFragment,
                                mRecycleFragment;
    private OnlineNeighborFragment mOnlineNeighborFragment;
    public SeafileFragment mSeafileFragment;
    private UsbConnectReceiver mReceiver;
    private boolean mIsMutiSelect;
    private SharedPreferences mSharedPreferences;
    private Editor mEditor;
    public boolean mIsSdStorageFragment;

    public static Handler mHandler;
    private HomeLeftOnTouchListener mHomeLeftOnTouchListener;
    private HomeLeftOnHoverListener mHomeLeftOnHoverListener;
    private boolean mIsFirst = true;
    private HashMap<String, Integer> mHashMap;
    private SearchOnKeyListener mSearchOnKeyListener;
    private CopyInfoDialog mCopyInfoDialog;
    private ProgressDialog mProgressDialog;
    private ProgressDialog mPopUpProgressDialog;
    public PersonalSpaceFragment mPersonalSpaceFragment;
    private SystemSpaceFragment mUsbStorageFragment;
    public BaseFragment mStartSearchFragment;
    private SearchFragment mSearchFragment;
    public String mCurPath;
    public SeafileAccount mAccount;
    public SeafileUtils.SeafileSQLConsole mConsole;
    public CustomFileObserver mCustomFileObserver;
    private InitSeafileThread mInitSeafileThread;
    private SeafileThread mSeafileThread;
    private String mUsbPath;
    private ExecutorService mUsbSingleExecutor;
    private TextView[] mLeftTexts;
    private static ContentResolver mContentResolver;
    private static Uri mUri;
    private static boolean mIsCtrlPress = false;
    private static boolean mIsShiftPress = false;

    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    private class InitSeafileThread extends Thread {
        @Override
        public void run() {
            super.run();
            SeafileUtils.init();
            SeafileUtils.start();
            mSeafileThread = new SeafileThread();
            mSeafileThread.start();
        }
    }

    private class SeafileThread extends Thread {
        private boolean isExistsSetting = false;
        private boolean isExistsFileManager = false;
        private String id = "";
        private String settingId = "";
        @Override
        public void run() {
            super.run();
            ContentResolver mResolver = MainActivity.this.getContentResolver();
            Uri uriQuery = Uri.parse(Constants.OPENTHOS_URI);
            Cursor cursor = mResolver.query(uriQuery, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //current openthos id and password
                    SeafileUtils.mUserId = cursor.getString(cursor.getColumnIndex("openthosID"));
                    SeafileUtils.mUserPassword =
                                     cursor.getString(cursor.getColumnIndex("password"));
                    break;
                }
                cursor.close();
            }
            if (TextUtils.isEmpty(SeafileUtils.mUserId)
                                               || TextUtils.isEmpty(SeafileUtils.mUserPassword)) {
                return;
            }
            String librarys = SeafileUtils.listRemote();
            mAccount = new SeafileAccount();
            mAccount.mUserName = SeafileUtils.mUserId;
            mConsole = new SeafileUtils.SeafileSQLConsole(MainActivity.this);
            mAccount.mUserId = mConsole.queryAccountId(mAccount.mUserName);
            mAccount.mFile = new File(SeafileUtils.SEAFILE_DATA_PATH, mAccount.mUserName);
            if (!mAccount.mFile.exists()) {
                mAccount.mFile.mkdirs();
            }
            try {
                if (librarys.equals("]")) {
                    librarys = getSharedPreferences(SeafileUtils.SEAFILE_DATA,
                            Context.MODE_PRIVATE).getString(SeafileUtils.SEAFILE_DATA, "");
                }
                JSONArray jsonArray = new JSONArray(librarys);
                JSONObject jsonObject = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    SeafileLibrary seafileLibrary = new SeafileLibrary();
                    jsonObject = jsonArray.getJSONObject(i);
                    seafileLibrary.libraryName = jsonObject.getString("name");
                    seafileLibrary.libraryId = jsonObject.getString("id");
                    if (seafileLibrary.libraryName.equals(SeafileUtils.SETTING_SEAFILE_NAME)) {
                        isExistsSetting = true;
                        settingId = seafileLibrary.libraryId;
                        continue;
                    }
                    if (!seafileLibrary.libraryName.equals(SeafileUtils.FILEMANAGER_SEAFILE_NAME)) {
                        continue;
                    }
                    isExistsFileManager = true;
                    id = seafileLibrary.libraryId;
                    mAccount.mLibrarys.add(seafileLibrary);
                }
                getSharedPreferences(SeafileUtils.SEAFILE_DATA, Context.MODE_PRIVATE).edit()
                        .putString(SeafileUtils.SEAFILE_DATA, librarys).commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!isExistsFileManager) {
                SeafileLibrary seafileLibrary = new SeafileLibrary();
                seafileLibrary.libraryName = SeafileUtils.FILEMANAGER_SEAFILE_NAME;
                seafileLibrary.libraryId
                                     = SeafileUtils.create(SeafileUtils.FILEMANAGER_SEAFILE_NAME);
                mAccount.mLibrarys.add(seafileLibrary);
            }
            if (mAccount.mLibrarys.size() > 0) {
                for (SeafileLibrary seafileLibrary : mAccount.mLibrarys) {
                    String name = seafileLibrary.libraryName;
                    int isSync = mConsole.queryFile(mAccount.mUserId,
                            seafileLibrary.libraryId, seafileLibrary.libraryName);
                    seafileLibrary.isSync = isSync;
                    if (isSync == SeafileUtils.SYNC) {
                        SeafileUtils.sync(seafileLibrary.libraryId,
                                new File(mAccount.mFile, seafileLibrary.libraryName)
                                        .getAbsolutePath());
                    }
                }
                MainActivity.mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
            }
            File settingSeafile = new File(SeafileUtils.SETTING_SEAFILE_PATH);
            if (!settingSeafile.exists()) {
                settingSeafile.mkdirs();
            }
            if (!isExistsSetting) {
                settingId = SeafileUtils.create(SeafileUtils.SETTING_SEAFILE_NAME);
            }
            SeafileUtils.sync(settingId, SeafileUtils.SETTING_SEAFILE_PROOT_PATH);
        }
    }

    public boolean isInitSeafile() {
        return mInitSeafileThread.isAlive();
    }

    public boolean isSeafile() {
        return mSeafileThread.isAlive();
    }

    protected void initView() {
        mInitSeafileThread = new InitSeafileThread();
        mInitSeafileThread.start();
        mSharedPreferences = getSharedPreferences(VIEW_TAG, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        String viewTag = mSharedPreferences.getString(VIEW_TAG, VIEW_TAG_GRID);
        LocalCache.getInstance(MainActivity.this).setViewTag(viewTag);
        mTv_desk = (TextView) findViewById(R.id.tv_desk);
        mTv_music = (TextView) findViewById(R.id.tv_music);
        mTv_video = (TextView) findViewById(R.id.tv_video);
        mTv_computer = (TextView) findViewById(R.id.tv_computer);
        mTv_picture = (TextView) findViewById(R.id.tv_picture);
        mTv_document = (TextView) findViewById(R.id.tv_document);
        mTv_download = (TextView) findViewById(R.id.tv_download);
        mTv_recycle = (TextView) findViewById(R.id.tv_recycle);
        mTv_cloud_service = (TextView) findViewById(R.id.tv_cloud_service);
        mTv_net_service = (TextView) findViewById(R.id.tv_net_service);
        mIv_list_view = (ImageView) findViewById(R.id.iv_list_view);
        mIv_grid_view = (ImageView) findViewById(R.id.iv_grid_view);
        mIv_back = (ImageView) findViewById(R.id.iv_back);
        mIv_setting = (ImageView) findViewById(R.id.iv_setting);
        mEt_nivagation = (EditText) findViewById(R.id.et_nivagation);
        mIv_search_view = (ImageView) findViewById(R.id.iv_search);
        mEt_search_view = (EditText) findViewById(R.id.search_view);
        ll_usb = (LinearLayout) findViewById(R.id.ll_usb);
        mLeftTexts = new TextView[]{mTv_music, mTv_desk, mTv_video, mTv_computer, mTv_picture,
                mTv_net_service, mTv_document, mTv_download, mTv_recycle, mTv_cloud_service};
        if (LocalCache.getViewTag() != null && "list".equals(LocalCache.getViewTag())) {
            mIv_grid_view.setSelected(false);
            mIv_list_view.setSelected(true);
        } else {
            mIv_grid_view.setSelected(true);
            mIv_list_view.setSelected(false);
        }
        File file = new File(Constants.DOCUMENT_PATH);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdir();
        }

        mUsbSingleExecutor = Executors.newSingleThreadExecutor();
        mHashMap = new HashMap<>();
        mHashMap.put(Constants.DESKFRAGMENT_TAG, R.id.tv_desk);
        mHashMap.put(Constants.MUSICFRAGMENT_TAG, R.id.tv_music);
        mHashMap.put(Constants.VIDEOFRAGMENT_TAG, R.id.tv_video);
        mHashMap.put(Constants.PICTRUEFRAGMENT_TAG, R.id.tv_picture);
        mHashMap.put(Constants.DOCUMENTFRAGMENT_TAG, R.id.tv_document);
        mHashMap.put(Constants.DOWNLOADFRRAGMENT_TAG, R.id.tv_download);
        mHashMap.put(Constants.RECYCLEFRAGMENT_TAG, R.id.tv_recycle);
        mHashMap.put(Constants.SDSTORAGEFRAGMENT_TAG, R.id.tv_computer);
        mHashMap.put(Constants.ONLINENEIGHBORFRAGMENT_TAG, R.id.tv_net_service);
        mHashMap.put(Constants.CLOUDSERVICEFRAGMENT_TAG, R.id.tv_cloud_service);
        mHashMap.put(Constants.DETAILFRAGMENT_TAG, R.id.tv_picture);
        mHashMap.put(Constants.SYSTEM_SPACE_FRAGMENT_TAG, R.id.tv_computer);
        mCopyInfoDialog = CopyInfoDialog.getInstance(MainActivity.this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (!Thread.currentThread().isInterrupted()) {
                    switch (msg.what) {
                        case Constants.DESKTOP_SHOW_FILE:
                            Intent showIntent = new Intent();
                            showIntent.setAction(Intent.ACTION_DESKTOP_SHOW_FILE);
                            showIntent.putExtra(Intent.EXTRA_DESKTOP_PATH_TAG, (String) msg.obj);
                            MainActivity.this.sendBroadcast(showIntent);
                            break;
                        case Constants.DESKTOP_DELETE_FILE:
                            Intent deleteIntent = new Intent();
                            deleteIntent.setAction(Intent.ACTION_DESKTOP_DELETE_FILE);
                            deleteIntent.putExtra(Intent.EXTRA_DESKTOP_PATH_TAG, (String) msg.obj);
                            MainActivity.this.sendBroadcast(deleteIntent);
                            break;
                        case Constants.USB_CHECKING:
                        case Constants.USB_MOUNT:
                        case Constants.USB_EJECT:
                            initUsb(msg.what);
                            break;
                        case Constants.USB_UNMOUNT:
                            mUsbPath = (String) msg.obj;
                            initUsb(Constants.USB_UNMOUNT);
                            break;
                        case Constants.USB_READY:
                            ll_usb.removeAllViews();
                            for (int i = 0; i < SdStorageFragment.usbLists.size(); i++) {
                                ll_usb.addView(getUsbView(i));
                            }
                            if (mProgressDialog != null) {
                                mProgressDialog.dismiss();
                            }
                            if (TextUtils.isEmpty(getCurPath())) {
                                mTv_computer.performClick();
                            }
                            break;
                        case Constants.REFRESH:
                            ((IFileInteractionListener) getVisibleFragment())
                                         .onRefreshFileList((String) msg.obj, getFileSortHelper());
                            resetClipboard();
                            break;
                        case Constants.COPY:
                            copy();
                            break;
                        case Constants.CUT:
                            cut();
                            break;
                        case Constants.PASTE:
                            paste();
                            break;
                        case Constants.COPY_INFO_SHOW:
                            mCopyInfoDialog.showDialog(R.raw.paste);
                            mCopyInfoDialog.changeTitle(MainActivity.this.getResources()
                                                                    .getString(R.string.copy_info));
                            break;
                        case Constants.DELETE_INFO_SHOW:
                            mCopyInfoDialog.showDialog(R.raw.delete);
                            mCopyInfoDialog.changeTitle(MainActivity.this.getResources()
                                                                    .getString(R.string.copy_info));
                            break;
                        case Constants.COMPRESS_INFO_SHOW:
                            mCopyInfoDialog.showDialog(R.raw.compress);
                            mCopyInfoDialog.changeTitle(MainActivity.this.getResources()
                                                                    .getString(R.string.copy_info));
                            break;
                        case Constants.DECOMPRESS_INFO_SHOW:
                            mCopyInfoDialog.showDialog(R.raw.decompress);
                            mCopyInfoDialog.changeTitle(MainActivity.this.getResources()
                                                                    .getString(R.string.copy_info));
                            break;
                        case Constants.COPY_INFO:
                            mCopyInfoDialog.changeMsg((String) msg.obj);
                            break;
                        case Constants.COPY_INFO_HIDE:
                            mCopyInfoDialog.cancel();
                            break;
                        case Constants.ONLY_REFRESH:
                            ((IFileInteractionListener) getVisibleFragment())
                                    .onRefreshFileList((String) msg.obj, getFileSortHelper());
                            break;
                        case Constants.MENU_SHOWHIDE:
                            Toast.makeText(MainActivity.this,
                                           getResources().getString(R.string.can_not_search),
                                           Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.SEAFILE_DATA_OK:
                            mSeafileFragment.setData(mAccount.mLibrarys);
                            mSeafileFragment.getAdapter().notifyDataSetChanged();
                            break;
                    }
                }
                super.handleMessage(msg);
            }
        };
    }

    private void showSdSFragmentAfterInstallUSB() {
        mManager.beginTransaction().remove(mCurFragment).show(mSdStorageFragment).commit();
        mCurFragment = mSdStorageFragment;
    }

    private void removeMobileDevice() {
        if (TextUtils.isEmpty(getCurPath())
                || (getCurPath() != null
                     && getCurPath().startsWith(Constants.PERMISS_DIR_STORAGE_USB))) {
            mManager.beginTransaction().remove(mSdStorageFragment).commit();
            mManager.beginTransaction().hide(mCurFragment).commit();
            mSdStorageFragment = new SdStorageFragment(mManager,
                    USB_DEVICE_DETACHED, MainActivity.this);
            setSelectedBackground(R.id.tv_computer);
            mManager.beginTransaction().add(R.id.fl_mian, mSdStorageFragment)
                    .show(mSdStorageFragment).commit();
            mCurFragment = mSdStorageFragment;
        } else {
            BaseFragment visibleFragment = (BaseFragment) getVisibleFragment();
            mManager.beginTransaction().remove(mSdStorageFragment).commit();
            mSdStorageFragment = new SdStorageFragment(mManager,
                    USB_DEVICE_DETACHED, MainActivity.this);
            mManager.beginTransaction().add(R.id.fl_mian, mSdStorageFragment)
                    .hide(mSdStorageFragment).commit();
            mSdStorageFragment.mCurFragment = visibleFragment;
        }
    }

    private void initUsb(int flags) {
        switch (flags) {
            case Constants.USB_INIT:
                if (TextUtils.isEmpty(getCurPath())) {
                    mManager.beginTransaction().remove(mSdStorageFragment).commit();
                    mManager.beginTransaction().hide(mCurFragment).commit();
                    mSdStorageFragment = new SdStorageFragment(mManager, USB_DEVICE_ATTACHED,
                            MainActivity.this);
                    setSelectedBackground(R.id.tv_computer);
                    mManager.beginTransaction().add(R.id.fl_mian, mSdStorageFragment)
                            .show(mSdStorageFragment).commit();
                    mCurFragment = mSdStorageFragment;
                } else {
                    BaseFragment visibleFragment = (BaseFragment) getVisibleFragment();
                    mManager.beginTransaction().remove(mSdStorageFragment).commit();
                    mSdStorageFragment = new SdStorageFragment(mManager, USB_DEVICE_ATTACHED,
                            MainActivity.this);
                    mManager.beginTransaction().add(R.id.fl_mian, mSdStorageFragment)
                            .hide(mSdStorageFragment).commit();
                    mSdStorageFragment.mCurFragment = visibleFragment;
                }
                break;
            case Constants.USB_CHECKING:
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(this);
                }
                mProgressDialog.setMessage(getString(R.string.USB_recognising));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(true);
                mProgressDialog.setCanceledOnTouchOutside(true);
                mProgressDialog.show();
                break;
            case Constants.USB_MOUNT:
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                T.showShort(this, getResources().getString(R.string.USB_device_connected));
                initUsb(Constants.USB_INIT);
                break;
            case Constants.USB_EJECT:
                if (mUsbPath != null) {
                    for (int i = 0; i < SdStorageFragment.usbLists.size(); i++) {
                        if (mUsbPath.equals(SdStorageFragment.usbLists.get(i)[0])) {
                            int position = getUsbPosition(mUsbPath);
                            ll_usb.removeViewAt(getUsbPosition(mUsbPath));
                            mSdStorageFragment.removeUsbView(position);
                            break;
                        }
                    }
                }
                if (getCurPath() != null && getCurPath().equals(mUsbPath)) {
                    showSdSFragmentAfterInstallUSB();
                    setCurPath(null);
                    mEt_nivagation.setText(getCurPath());
                }
                T.showShort(this, getResources().getString(R.string.USB_device_disconnected));
                break;
            case Constants.USB_UNMOUNT:
                if (mPopUpProgressDialog != null) {
                    mPopUpProgressDialog.dismiss();
                }
                initUsb(Constants.USB_EJECT);
                break;
        }
    }

    private void initFragment() {
        mReceiver = new UsbConnectReceiver(this);
        FragmentTransaction transaction = mManager.beginTransaction();
        if (mSdStorageFragment == null) {
            mSdStorageFragment = new SdStorageFragment(mManager, null, MainActivity.this);
            transaction.add(R.id.fl_mian, mSdStorageFragment).hide(mSdStorageFragment);
        }
        if (mDeskFragment == null) {
            mDeskFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                    Constants.DESKTOP_PATH, null, null, true);
            transaction.add(R.id.fl_mian, mDeskFragment, Constants.DESKFRAGMENT_TAG)
                       .hide(mDeskFragment);
        }
        if (mMusicFragment == null) {
            mMusicFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                     Constants.MUSIC_PATH, null, null, true);
            transaction.add(R.id.fl_mian, mMusicFragment, Constants.MUSICFRAGMENT_TAG)
                       .hide(mMusicFragment);
        }
        if (mVideoFragment == null) {
            mVideoFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                     Constants.VIDEOS_PATH, null, null, true);
            transaction.add(R.id.fl_mian, mVideoFragment, Constants.VIDEOFRAGMENT_TAG)
                       .hide(mVideoFragment);
        }
        if (mPictrueFragment == null) {
            mPictrueFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                       Constants.PICTURES_PATH, null, null, true);
            transaction.add(R.id.fl_mian, mPictrueFragment, Constants.PICTRUEFRAGMENT_TAG)
                       .hide(mPictrueFragment);
        }
        if (mDocumentFragment == null) {
            mDocumentFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                        Constants.DOCUMENT_PATH, null, null, true);
            transaction.add(R.id.fl_mian, mDocumentFragment, Constants.DOCUMENTFRAGMENT_TAG)
                       .hide(mDocumentFragment);
        }
        if (mDownloadFragment == null) {
            mDownloadFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                        Constants.DOWNLOAD_PATH, null, null, true);
            transaction.add(R.id.fl_mian, mDownloadFragment, Constants.DOWNLOADFRRAGMENT_TAG)
                       .hide(mDownloadFragment);
        }
        if (mRecycleFragment == null) {
            mRecycleFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                        Constants.RECYCLE_PATH, null, null, true);
            transaction.add(R.id.fl_mian, mRecycleFragment, Constants.RECYCLEFRAGMENT_TAG)
                       .hide(mRecycleFragment);
        }

        if (mOnlineNeighborFragment == null) {
            mOnlineNeighborFragment = new OnlineNeighborFragment();
            transaction.add(R.id.fl_mian, mOnlineNeighborFragment).hide(mOnlineNeighborFragment);
        }
        if (mPersonalSpaceFragment == null) {
            mPersonalSpaceFragment = new PersonalSpaceFragment();
            transaction.add(R.id.fl_mian, mPersonalSpaceFragment).hide(mPersonalSpaceFragment);
        }
        if (mSeafileFragment == null) {
            mSeafileFragment = new SeafileFragment();
            transaction.add(R.id.fl_mian, mSeafileFragment).hide(mSeafileFragment);
        }
        transaction.commit();
    }

    protected void initData() {
        initFragment();
        checkFolder(null);
        mContentResolver = getContentResolver();
        mUri = Uri.parse("content://com.openthos.filemanager/recycle");
    }

    private void checkFolder(Fragment fragment) {
        List<String> fileList = new ArrayList<>();
        fileList.add(Constants.DESKTOP_PATH);
        fileList.add(Constants.MUSIC_PATH);
        fileList.add(Constants.VIDEOS_PATH);
        fileList.add(Constants.PICTURES_PATH);
        fileList.add(Constants.DOCUMENT_PATH);
        fileList.add(Constants.DOWNLOAD_PATH);
        fileList.add(Constants.RECYCLE_PATH);
        for (int i = 0; i < fileList.size(); i++) {
            File file = new File(fileList.get(i));
            if (!file.exists() && !file.isDirectory()) {
                file.mkdir();
            }
        }
        if (fragment != null) {
            ((SystemSpaceFragment) fragment).refreshUI();
        }
    }

    @Override
    protected void initListener() {
        mHomeLeftOnTouchListener = new HomeLeftOnTouchListener();
        mHomeLeftOnHoverListener = new HomeLeftOnHoverListener();
        for (int i = 0; i < mLeftTexts.length; i++) {
            mLeftTexts[i].setOnTouchListener(mHomeLeftOnTouchListener);
            mLeftTexts[i].setOnHoverListener(mHomeLeftOnHoverListener);
        }
        mIv_list_view.setOnClickListener(this);
        mIv_grid_view.setOnClickListener(this);
        mIv_back.setOnClickListener(this);
        mIv_setting.setOnClickListener(this);
        clickComputer();
        mSearchOnKeyListener = new SearchOnKeyListener(mManager,
                                        mEt_search_view.getText(), MainActivity.this);
        mEt_search_view.setOnKeyListener(mSearchOnKeyListener);
        mIv_search_view.setOnClickListener(this);
        NivagationOnClickLinstener nivagationOnClickLinstener = new NivagationOnClickLinstener();
        NivagationOnKeyLinstener nivagationOnKeyLinstener = new NivagationOnKeyLinstener();
        mEt_nivagation.setOnClickListener(nivagationOnClickLinstener);
        mEt_nivagation.setOnKeyListener(nivagationOnKeyLinstener);
        mEt_nivagation.addTextChangedListener(new TextChangeListener());
        initUsb(Constants.USB_INIT);
        mCurFragment = mSdStorageFragment;
        Intent intent = getIntent();
        String path = intent.getStringExtra(Intent.EXTRA_DESKTOP_PATH_TAG);
        if (path != null) {
            showSpaceFragment(path);
            if (path.startsWith(Constants.DESKTOP_PATH)) {
                setSelectedBackground(R.id.tv_desk);
            } else if (path.startsWith(Constants.RECYCLE_PATH)) {
                setSelectedBackground(R.id.tv_recycle);
            }
        }
        setCurPath(path);
    }

    class NivagationOnClickLinstener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            v.requestFocus();
        }
    }

    class NivagationOnKeyLinstener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
                    v.clearFocus();
                    String path = ((TextView) v).getText().toString();
                    for (int i = 0; i < path.length(); i++) {
                        if (path.charAt(i) != ' ') {
                            showSpaceFragment(path.substring(i, path.length()));
                            break;
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_ESCAPE:
                    v.clearFocus();
                    return true;
            }
            return false;
        }
    }

    private void showSpaceFragment(String path) {
        FragmentTransaction transaction = mManager.beginTransaction();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        if (path.startsWith(getString(R.string.path_sd_eng))) {
            path = path.replaceAll(getString(R.string.path_sd_eng), Util.getSdDirectory());
        } else if (!path.startsWith(Constants.SD_PATH)) {
            path = Constants.SD_PATH + path;
        }
        File file = new File(path);
        if (file.exists()) {
            transaction.hide(mCurFragment);
            mAddressFragment = new SystemSpaceFragment(
                                   Constants.LEFT_FAVORITES, path, null, null, false);
            transaction.add(R.id.fl_mian, mAddressFragment, Constants.ADDRESSFRAGMENT_TAG);
            //transaction.show(mAddressFragment).addToBackStack(null).commit();
            transaction.show(mAddressFragment).commit();
            mCurFragment = mAddressFragment;
            setFileInfo(R.id.et_nivagation, path, mAddressFragment);
        } else {
            Toast.makeText(this, "" + getResources().getString(R.string.address_search_false),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        mReceiver.registerReceiver();
        super.onStart();
    }

    public class UsbConnectReceiver extends BroadcastReceiver {
        private static final String TAG = "UsbConnectReceiver";
        MainActivity execactivity;

        public IntentFilter filter = new IntentFilter();

        public UsbConnectReceiver(Context context) {
            execactivity = (MainActivity) context;
            filter.addAction(Intent.ACTION_MEDIA_CHECKING);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);

            filter.addDataScheme("file");
        }

        public Intent registerReceiver() {
            return execactivity.registerReceiver(this, this.filter);
        }

        public void unregisterReceiver() {
            execactivity.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String dataString = intent.getDataString();
            mUsbPath = dataString.substring(7, dataString.length());
            switch (action) {
                case Intent.ACTION_MEDIA_CHECKING:
                    sendMsg(Constants.USB_CHECKING);
                    break;
                case Intent.ACTION_MEDIA_MOUNTED:
                    sendMsg(Constants.USB_MOUNT);
                    break;
                case Intent.ACTION_MEDIA_EJECT:
                    sendMsg(Constants.USB_EJECT);
                    break;
            }
        }
    }

    private void sendMsg(int flags) {
        Message msg = new Message();
        msg.what = flags;
        mHandler.sendMessage(msg);
    }

    private void sendMsg(int flags, String path) {
        Message msg = mHandler.obtainMessage();
        msg.what = flags;
        msg.obj = path;
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mIsCtrlPress = event.isCtrlPressed();
        mIsShiftPress = event.isShiftPressed();
        mIsMutiSelect = false;
        if (!mIsMutiSelect && !mIsFirst) {
            sendBroadcastMessage("is_ctrl_press", null, mIsMutiSelect);
            mIsFirst = true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
             || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                     && !(mEt_nivagation.isFocused() || mEt_search_view.isFocused())) {
             return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DEL && !mEt_search_view.hasFocus()
                                                                  && !mEt_nivagation.isFocused()) {
            onBackPressed();
        }
        mIsCtrlPress = event.isCtrlPressed();
        mIsShiftPress = event.isShiftPressed();
        if (event.isCtrlPressed()) {
            mIsMutiSelect = true;
        }
        if (mIsMutiSelect && mIsFirst) {
            sendBroadcastMessage("is_ctrl_press", null, mIsMutiSelect);
            mIsFirst = false;
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_X) {
            sendBroadcastMessage("iv_menu", "pop_cut", false);
            if (isCopyByHot()) {
                return false;
            }
            cut();
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_C) {
            sendBroadcastMessage("iv_menu", "pop_copy", false);
            if (isCopyByHot() || isRecycle()) {
                return false;
            }
            copy();
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_V) {
            sendBroadcastMessage("iv_menu", "pop_paste", false);
            if (isCopyByHot() || isRecycle()) {
                return false;
            }
            paste();
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_Z) {
            sendBroadcastMessage("iv_menu", "pop_cacel", false);
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_A) {
            sendBroadcastMessage("iv_menu", "pop_cacel", false);
            if (getVisibleFragment() instanceof SystemSpaceFragment) {
                final SystemSpaceFragment fragment = (SystemSpaceFragment) getVisibleFragment();
                fragment.mFileViewInteractionHub.onOperationSelectAll();
                FileListAdapter adapter = fragment.getAdapter();
                List<FileInfo> list = adapter.getFileInfoList();
                List<Integer> integerList = adapter.getSelectFileInfoList();
                for (int i = 0; i < list.size(); i++) {
                    integerList.add(i);
                }
                adapter.notifyDataSetChanged();
            }
        }
        if ((keyCode == KeyEvent.KEYCODE_FORWARD_DEL && !event.isShiftPressed())
                || (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_D)) {
            sendBroadcastMessage("iv_menu", "pop_delete", false);
            if (isCopyByHot()) {
                return false;
            }
            ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub.onOperationDelete();
        } else if (keyCode == KeyEvent.KEYCODE_FORWARD_DEL && event.isShiftPressed()) {
            sendBroadcastMessage("iv_menu", "pop_delete", false);
            if (isCopyByHot()) {
                return false;
            }
            ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub.onOperationDeleteDirect();
        }
        if (keyCode == KeyEvent.KEYCODE_F2) {
            if (isCopyByHot() || isRecycle()) {
                return false;
            }
            ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub.onOperationRename();
        }
        if (keyCode == KeyEvent.KEYCODE_F5) {
            if (getVisibleFragment() instanceof PersonalSpaceFragment) {
                mHandler.sendEmptyMessage(Constants.REFRESH_PERSONAL);
            } else if (isCopyByHot()) {
                return false;
            } else {
                mHandler.sendMessage(Message.obtain(mHandler, Constants.ONLY_REFRESH,
                  ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub.getCurrentPath()));
            }
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            if (mEt_nivagation.isFocused() || mEt_search_view.isFocused()) {
                return false;
            }
            if (isRecycle()) {
                Toast.makeText(this, getString(R.string.fail_open_recycle),
                                                                  Toast.LENGTH_SHORT).show();
                return false;
            }
            if (getVisibleFragment() instanceof BaseFragment) {
                ((BaseFragment) getVisibleFragment()).enter();
            }
        }
        return false;
    }

    private boolean isCopyByHot() {
        return getVisibleFragment() instanceof PersonalSpaceFragment
                || getVisibleFragment() instanceof SdStorageFragment
                || getVisibleFragment() instanceof OnlineNeighborFragment
                || getVisibleFragment() instanceof SeafileFragment
                || mEt_nivagation.isFocused() || mEt_search_view.isFocused();
    }

    public boolean isRecycle() {
        return (getVisibleFragment() instanceof SystemSpaceFragment)
                && (((SystemSpaceFragment) getVisibleFragment()).getCurrentPath()
                             .startsWith(FileOperationHelper.RECYCLE_PATH1)
                || ((SystemSpaceFragment) getVisibleFragment()).getCurrentPath()
                             .startsWith(FileOperationHelper.RECYCLE_PATH2)
                || ((SystemSpaceFragment) getVisibleFragment()).getCurrentPath()
                             .startsWith(FileOperationHelper.RECYCLE_PATH3));
    }

    public void cut() {
        ArrayList<FileInfo> list =
               ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub.getSelectedFileList();
        StringBuffer stringBuffer = new StringBuffer();
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++ ) {
                stringBuffer.append(Intent.EXTRA_CROP_FILE_HEADER + list.get(i).filePath);
            }
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                    .setText(stringBuffer);
        }
    }

    public void paste() {
        String sourcePath = "";
        String destPath =
                    ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub.getCurrentPath();
        try {
            sourcePath =
               (String) ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).getText();
        } catch (ClassCastException e) {
            sourcePath = "";
        }
        if (sourcePath == null) {
            sourcePath = "";
        }
        String[] srcCopyPaths = sourcePath.split(Intent.EXTRA_FILE_HEADER);
        String[] srcCropPaths = sourcePath.split(Intent.EXTRA_CROP_FILE_HEADER);
        if (!TextUtils.isEmpty(sourcePath) && sourcePath.startsWith(Intent.EXTRA_FILE_HEADER)) {
            new CopyThread(srcCopyPaths, destPath).start();
        } else if (!TextUtils.isEmpty(sourcePath)
                                        && sourcePath.startsWith(Intent.EXTRA_CROP_FILE_HEADER)) {
            new CropThread(srcCropPaths, destPath).start();
        }
    }

    private void resetClipboard() {
        String sourcePath = "";
        try {
            sourcePath =
                (String) ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).getText();
        } catch (ClassCastException e) {
            sourcePath = "";
        }
        if (!TextUtils.isEmpty(sourcePath)
               && sourcePath.startsWith(Intent.EXTRA_CROP_FILE_HEADER)) {
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setText("");
        }
    }

    class CopyThread extends Thread {
        String[] mSrcCopyPaths;
        String mDestPath;

        public CopyThread(String[] srcPaths, String destPath) {
            super();
            mSrcCopyPaths = srcPaths;
            mDestPath = destPath;
        }

        @Override
        public void run() {
            super.run();
            for (int i = 1; i < mSrcCopyPaths.length; i++) {
                FileOperationHelper.CopyFile(
                        mSrcCopyPaths[i].replace(Intent.EXTRA_FILE_HEADER, ""), mDestPath);
            }
        }
    }

    class CropThread extends Thread {
        String[] mSrcCropPaths;
        String mDestPath;

        public CropThread(String[] srcPaths, String destPath) {
            super();
            mSrcCropPaths = srcPaths;
            mDestPath = destPath;
        }

        @Override
        public void run() {
            super.run();
            for (int i = 1; i < mSrcCropPaths.length; i++) {
                FileOperationHelper.MoveFile(
                        mSrcCropPaths[i].replace(Intent.EXTRA_CROP_FILE_HEADER, ""), mDestPath, true);
            }
        }
    }

    public void copy() {
        ArrayList<FileInfo> list =
                ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub.getSelectedFileList();
        StringBuffer stringBuffer = new StringBuffer();
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++ ) {
                stringBuffer.append(Intent.EXTRA_FILE_HEADER + list.get(i).filePath);
            }
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                            .setText(stringBuffer);
        }
    }

    @Override
    public void onClick(View view) {
        clearNivagateFocus();
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_setting:
                showPopWindow(SETTING_POPWINDOW_TAG);
                break;
            case R.id.iv_grid_view:
                mIv_grid_view.setSelected(true);
                mIv_list_view.setSelected(false);
                LocalCache.setViewTag(VIEW_TAG_GRID);
                sendBroadcastMessage(IV_SWITCH_VIEW, VIEW_TAG_GRID, false);
                mEditor.putString(VIEW_TAG, VIEW_TAG_GRID);
                mEditor.commit();
                break;
            case R.id.iv_list_view:
                mIv_grid_view.setSelected(false);
                mIv_list_view.setSelected(true);
                LocalCache.setViewTag(VIEW_TAG_LIST);
                sendBroadcastMessage(IV_SWITCH_VIEW, VIEW_TAG_LIST, false);
                mEditor.putString(VIEW_TAG, VIEW_TAG_LIST);
                mEditor.commit();
                break;
            case R.id.iv_search:
                mEt_search_view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_ENTER));
                break;
        }
    }

    public void uninstallUSB(String usbPath) {
        if (mPopUpProgressDialog == null) {
            mPopUpProgressDialog = new ProgressDialog(this);
        }
        mPopUpProgressDialog.setMessage(getString(R.string.USB_umounting));
        mPopUpProgressDialog.setIndeterminate(true);
        mPopUpProgressDialog.setCancelable(true);
        mPopUpProgressDialog.setCanceledOnTouchOutside(true);
        mPopUpProgressDialog.show();
        mUsbSingleExecutor.execute(new UninstallUsbThread(usbPath));
    }

    private class UninstallUsbThread implements Runnable {
        private String usbPath;

        public UninstallUsbThread(String usbPath) {
            this.usbPath = usbPath;
        }

        @Override
        public void run() {
            Process pro;
            BufferedReader in = null;
            try {
                pro = Runtime.getRuntime().exec("sync");
                in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                }
                getMountService().unmountVolume(usbPath, true, false);
                sendMsg(Constants.USB_UNMOUNT, usbPath);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private IMountService getMountService() {
        IMountService mountService = null;
        if (mountService == null) {
            IBinder iBinder = ServiceManager.getService("mount");
            if (iBinder != null) {
                mountService = IMountService.Stub.asInterface(iBinder);
                if (mountService == null) {
                    L.e("ljh", "Unable to connect to mount service! - is it running yet?");
                }
            }
        }
        return mountService;
    }

    private void setFileInfo(int id, String path, Fragment fragment) {
        if (fragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) fragment;
            systemSpaceFragment.setPath(path);
            FileListAdapter adapter = systemSpaceFragment.getAdapter();
            if (adapter != null) {
                adapter.getSelectFileInfoList().clear();
                systemSpaceFragment.getFileViewInteractionHub().clearSelection();
            }
        }
        setSelectedBackground(id);
        mEt_nivagation.setText(path);
        setCurPath(path);
        FragmentTransaction transaction = mManager.beginTransaction();
        if (mCurFragment != null) {
            transaction.hide(mCurFragment);
        }
        if (fragment != null) {
            transaction.show(fragment);
        }
        transaction.commit();
        mCurFragment = fragment;
    }


    private void setSelectedBackground(int id) {
        switch (id) {
            case R.id.tv_computer:
            case R.id.tv_desk:
            case R.id.tv_music:
            case R.id.tv_video:
            case R.id.tv_picture:
            case R.id.tv_document:
            case R.id.tv_download:
            case R.id.tv_recycle:
            case R.id.tv_net_service:
            case R.id.tv_cloud_service:
                setSelectView(findViewById(id));
                break;
            default:
                break;
        }
    }

    private void sendBroadcastMessage(String name, String tag, boolean isCtrl) {
        Intent intent = new Intent();
        switch (name) {
            case IV_SWITCH_VIEW:
                intent.setAction("com.switchview");
                intent.putExtra("switch_view", tag);
                break;
            case "iv_fresh":
                intent.setAction("com.refreshview");
                break;
            case "iv_menu":
                intent.setAction("com.switchmenu");
                intent.putExtra("pop_menu", tag);
                break;
            case "is_ctrl_press":
                intent.setAction("com.isCtrlPress");
                intent.putExtra("is_ctrl_press", isCtrl);
                break;
        }
        sendBroadcast(intent);
    }

    private void showPopWindow(String menu_tag) {
        mPopWinShare = null;
        PopOnClickLintener paramOnClickListener = new PopOnClickLintener(menu_tag,
                                                      MainActivity.this, mManager);
        if (SETTING_POPWINDOW_TAG.equals(menu_tag)) {
            mPopWinShare = new PopWinShare(MainActivity.this, paramOnClickListener,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    menu_tag);
            mPopWinShare.setFocusable(true);
            mPopWinShare.showAsDropDown(mIv_setting, POPWINDOW_X, POPWINDOW_Y);
        } else if (USB_POPWINDOW_TAG.equals(menu_tag)) {
            mPopWinShare = new PopWinShare(MainActivity.this, new usbListener(),
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    menu_tag);
            mPopWinShare.setFocusable(true);
            mPopWinShare.showAsDropDown(ll_usb.getChildAt(getUsbPosition(mUsbPath)), USB_POPWINDOW_X, USB_POPWINDOW_Y);
        }
        mPopWinShare.update();
        mPopWinShare.getContentView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mPopWinShare.dismiss();
                }
            }
        });
    }

    public void DismissPopwindow() {
        mPopWinShare.dismiss();
    }

    @Override
    public void onBackPressed() {
        mSearchOnKeyListener.setInputData(null);
        mManager.findFragmentById(R.id.fl_mian);
        if (mCurFragment != mSdStorageFragment) {
            if (mCurFragment instanceof SystemSpaceFragment) {
                SystemSpaceFragment sdCurFrament = (SystemSpaceFragment) mCurFragment;
                String currentPath = sdCurFrament.getCurrentPath();
                setCurPath(currentPath);
                mEt_nivagation.setText(currentPath);
                if (mCurFragment.getTag() != null &&
                    mCurFragment.getTag().equals(Constants.PERSONALSYSTEMSPACE_TAG)) {
                    if (mPersonalSpaceFragment.canGoBack()) {
                        mPersonalSpaceFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() >= ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToPersonalDir();
                    }
                } else if (mCurFragment.getTag() != null &&
                        mCurFragment.getTag().equals(Constants.SEAFILESYSTEMSPACE_TAG)) {
                    if (mSeafileFragment.canGoBack()) {
                        mSeafileFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() >= ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToSeafileDir();
                    }
                } else if (mCurFragment.getTag() != null &&
                          mCurFragment.getTag().equals(Constants.SDSSYSTEMSPACE_TAG)) {
                    if (mSdStorageFragment.canGoBack()) {
                        mSdStorageFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() >= ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToRootDir();
                    }
                } else if (mCurFragment.getTag() != null &&
                          mCurFragment.getTag().equals(Constants.USBFRAGMENT_TAG)) {
                    if (mUsbStorageFragment.canGoBack()) {
                        mUsbStorageFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() >= ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToRootDir();
                    }
                } else if (mCurFragment.getTag() != null
                               && mCurFragment.getTag().equals(Constants.DESKFRAGMENT_TAG)) {
                    if (mDeskFragment.canGoBack()) {
                        mDeskFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else if (mManager.getBackStackEntryCount() == ACTIVITY_MIN_COUNT_FOR_BACK) {
                        returnToDeskDir();
                    } else {
                        returnToRootDir();
                    }
                } else if (mCurFragment.getTag() != null
                               && mCurFragment.getTag().equals(Constants.MUSICFRAGMENT_TAG)) {
                    if (mMusicFragment.canGoBack()) {
                        mMusicFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else if (mManager.getBackStackEntryCount() == ACTIVITY_MIN_COUNT_FOR_BACK) {
                        returnToMusicDir();
                    } else {
                        returnToRootDir();
                    }
                } else if (mCurFragment.getTag() != null
                               && mCurFragment.getTag().equals(Constants.VIDEOFRAGMENT_TAG)) {
                    if (mVideoFragment.canGoBack()) {
                        mVideoFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else if (mManager.getBackStackEntryCount() == ACTIVITY_MIN_COUNT_FOR_BACK) {
                        returnToVideoDir();
                    } else {
                        returnToRootDir();
                    }
                } else if (mCurFragment.getTag() != null
                               && mCurFragment.getTag().equals(Constants.PICTRUEFRAGMENT_TAG)) {
                    if (mPictrueFragment.canGoBack()) {
                        mPictrueFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else if (mManager.getBackStackEntryCount() == ACTIVITY_MIN_COUNT_FOR_BACK) {
                        returnToPicDir();
                    } else {
                        returnToRootDir();
                    }
                } else if (mCurFragment.getTag() != null
                               && mCurFragment.getTag().equals(Constants.DOCUMENTFRAGMENT_TAG)) {
                    if (mDocumentFragment.canGoBack()) {
                        mDocumentFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else if (mManager.getBackStackEntryCount() == ACTIVITY_MIN_COUNT_FOR_BACK) {
                        returnToDocDir();
                    } else {
                        returnToRootDir();
                    }
                } else if (mCurFragment.getTag() != null
                               && mCurFragment.getTag().equals(Constants.DOWNLOADFRRAGMENT_TAG)) {
                    if (mDownloadFragment.canGoBack()) {
                        mDownloadFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else if (mManager.getBackStackEntryCount() == ACTIVITY_MIN_COUNT_FOR_BACK) {
                        returnToDownloadDir();
                    } else {
                        returnToRootDir();
                    }
                } else if (mCurFragment.getTag() != null
                               && mCurFragment.getTag().equals(Constants.RECYCLEFRAGMENT_TAG)) {
                    if (mRecycleFragment.canGoBack()) {
                        mRecycleFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else if (mManager.getBackStackEntryCount() == ACTIVITY_MIN_COUNT_FOR_BACK) {
                        returnToRecycleDir();
                    } else {
                        returnToRootDir();
                    }
                } else if (mCurFragment.getTag() != null
                               && mCurFragment.getTag().equals(Constants.SEARCHSYSTEMSPACE_TAG)) {
                    SystemSpaceFragment searchSysFragment = (SystemSpaceFragment) mCurFragment;
                    if (searchSysFragment.canGoBack()) {
                        searchSysFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToSearchFragment();
                    }
                } else if (mCurFragment.getTag() != null
                               && mCurFragment.getTag().equals(Constants.ADDRESSFRAGMENT_TAG)) {
                    SystemSpaceFragment addressFragment = (SystemSpaceFragment) mCurFragment;
                    if (addressFragment.canGoBack()) {
                        addressFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToRootDir();
                    }
                }
            } else if (mStartSearchFragment != null && mCurFragment instanceof SearchFragment) {
                mManager.beginTransaction().hide(mCurFragment).show(mStartSearchFragment).commit();
                mCurFragment = mStartSearchFragment;
                mStartSearchFragment = null;
            } else {
                returnToRootDir();
            }
        }
    }

    public Fragment getVisibleFragment() {
        List<Fragment> fragments = mManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
    }

    private void returnToSearchFragment() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        mSearchFragment = (SearchFragment) mManager
                              .findFragmentByTag(Constants.SEARCHFRAGMENT_TAG);
        fragmentTransaction.show(mSearchFragment);
        fragmentTransaction.commit();
        mCurFragment = mSearchFragment;
    }

    private void returnToDeskDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mDeskFragment);
        fragmentTransaction.commit();
        mEt_nivagation.setText(Constants.DESKTOP_PATH);
        setSelectedBackground(R.id.tv_desk);
        mCurFragment = mDeskFragment;
    }

    private void returnToMusicDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mMusicFragment);
        fragmentTransaction.commit();
        mEt_nivagation.setText(Constants.MUSIC_PATH);
        setSelectedBackground(R.id.tv_music);
        mCurFragment = mMusicFragment;
    }

    private void returnToVideoDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mVideoFragment);
        fragmentTransaction.commit();
        mEt_nivagation.setText(Constants.VIDEOS_PATH);
        setSelectedBackground(R.id.tv_video);
        mCurFragment = mVideoFragment;
    }

    private void returnToPicDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mPictrueFragment);
        fragmentTransaction.commit();
        mEt_nivagation.setText(Constants.PICTURES_PATH);
        setSelectedBackground(R.id.tv_picture);
        mCurFragment = mPictrueFragment;
    }

    private void returnToDocDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mDocumentFragment);
        fragmentTransaction.commit();
        mEt_nivagation.setText(Constants.DOCUMENT_PATH);
        setSelectedBackground(R.id.tv_document);
        mCurFragment = mDocumentFragment;
    }

    private void returnToDownloadDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mDownloadFragment);
        fragmentTransaction.commit();
        mEt_nivagation.setText(Constants.DOWNLOAD_PATH);
        setSelectedBackground(R.id.tv_download);
        mCurFragment = mDownloadFragment;
    }

    private void returnToRecycleDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mRecycleFragment);
        fragmentTransaction.commit();
        mEt_nivagation.setText(Constants.RECYCLE_PATH);
        setSelectedBackground(R.id.tv_recycle);
        mCurFragment = mRecycleFragment;
    }

    private void returnToCloudDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mSeafileFragment);
        fragmentTransaction.commit();
        mEt_nivagation.setText(getResources().getString(R.string.cloud));
        setSelectedBackground(R.id.tv_computer);
        mCurFragment = mSeafileFragment;
    }

    private void returnToPersonalDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mPersonalSpaceFragment);
        fragmentTransaction.commit();
        mEt_nivagation.setText("SDCard");
        setSelectedBackground(R.id.tv_computer);
        mCurFragment = mPersonalSpaceFragment;
    }

    public void returnToRootDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(mCurFragment);
        fragmentTransaction.show(mSdStorageFragment);
        fragmentTransaction.commit();
        setCurPath(null);
        mEt_nivagation.setText(null);
        setSelectedBackground(R.id.tv_computer);
        mSdStorageFragment.setSelectedCardBg(Constants.RETURN_TO_WHITE);
        mCurFragment = mSdStorageFragment;
    }

    private void returnToSeafileDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mSeafileFragment);
        fragmentTransaction.commit();
        mEt_nivagation.setText("seafile");
        setSelectedBackground(R.id.tv_cloud_service);
        mCurFragment = mSeafileFragment;
    }

    public interface IBackPressedListener {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver.unregisterReceiver();
        if (mCustomFileObserver != null) {
            mCustomFileObserver.stopWatching();
            mCustomFileObserver = null;
        }
    }

    @Override
    public void setNavigationBar(String displayPath) {
        if (displayPath != null) {
            if (mCurFragment == mSdStorageFragment && mSdStorageFragment.mCurFragment != null) {
                mEt_nivagation.setText(displayPath);
            } else {
                if (mCurFragment instanceof SystemSpaceFragment) {
                    mEt_nivagation.setText(displayPath);
                    displayPath = displayPath.replaceAll(getString(R.string.sd_folder),
                                      Constants.PERMISS_DIR_STORAGE_EMULATED_0);
                }else {
                    mEt_nivagation.setText(null);
                }
            }
        }
    }

    public void setNavigationPath(String displayPath) {
        mEt_nivagation.setText(displayPath);
    }

    public void setCurPath(String path) {
        mCurPath = path;
    }

    public String getCurPath() {
        return mCurPath;
    }

    class usbListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            clearNivagateFocus();
            DismissPopwindow();
            switch (view.getId()) {
                case R.id.pop_usb_view:
                    uninstallUSB(mUsbPath);
                    Intent intent = new Intent();
                    intent.setAction("com.switchmenu");
                    intent.putExtra("pop_menu", "view_or_dismiss");
                    sendBroadcast(intent);
                    break;
                case R.id.pop_usb_info:
                    int usbPosition = getUsbPosition(mUsbPath);
                    String[] usbs = SdStorageFragment.usbLists.get(usbPosition);
                    if (usbs[0] != null && new File(usbs[0]).exists()) {
                        UsbPropertyDialog usbPropertyDialog =
                                new UsbPropertyDialog(MainActivity.this, usbs);
                        usbPropertyDialog.showDialog();
                    }
                    break;
            }
        }
    }

    private void setSelectView(View view) {
        for (int i = 0; i < mLeftTexts.length; i++) {
            if (mLeftTexts[i].isSelected()) {
                if (mLeftTexts[i] == view) {
                    return;
                } else {
                    mLeftTexts[i].setSelected(false);
                    mLeftTexts[i].setBackground(getResources().getDrawable(R.drawable.left_bg_shape));
                }
            }
        }
        if (ll_usb != null) {
            for (int i = 0; i < ll_usb.getChildCount(); i++) {
                View childAt = ll_usb.getChildAt(i);
                if (childAt.isSelected()) {
                    if (childAt == view) {
                        return;
                    } else {
                        childAt.setSelected(false);
                        childAt.setBackground(getResources().getDrawable(R.drawable.left_bg_shape));
                    }
                }
            }
        }
        view.setSelected(true);
        view.setBackground(getResources().getDrawable(android.R.color.holo_purple));
    }

    private View getUsbView(int position) {
        View inflate = View.inflate(this, R.layout.usb_list, null);
        TextView name = (TextView) inflate.findViewById(R.id.usb_list_usb_name);
        ImageView uninstall = (ImageView) inflate.findViewById(R.id.usb_list_uninstall);
        String usbPath = SdStorageFragment.usbLists.get(position)[0];
        name.setText(Util.getUsbName(this, usbPath));
        uninstall.setTag(usbPath);
        inflate.setTag(usbPath);
        uninstall.setOnClickListener(new UsbUninstallListener());
        inflate.setOnTouchListener(mHomeLeftOnTouchListener);
        inflate.setOnHoverListener(mHomeLeftOnHoverListener);
        return inflate;
    }

    private int getUsbPosition(String path) {
        for (int i = 0; i < SdStorageFragment.usbLists.size(); i++) {
            String viewPath = (String) ll_usb.getChildAt(i).getTag();
            if (path.equals(viewPath)) {
                return i;
            }
        }
        return -1;
    }

    class CustomFileObserver extends FileObserver {
        public CustomFileObserver(String path) {
            super(path);
        }

        @Override
        public void onEvent(int event, String path) {
            final int action = event & FileObserver.ALL_EVENTS;
            switch (action) {
                case FileObserver.CREATE:
                case FileObserver.DELETE:
                case FileObserver.MOVED_FROM:
                case FileObserver.MOVED_TO:
                case FileObserver.MODIFY:
                    mHandler.sendMessage(Message.obtain(mHandler, Constants.ONLY_REFRESH,
                                         ((BaseFragment) getVisibleFragment())
                                                       .mFileViewInteractionHub.getCurrentPath()));
                    break;
                default:
                    break;
            }
        }
    }

    private class TextChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String sdfolder = getResources().getString(R.string.path_sd_eng);
            String path = editable.toString();
            if (path.startsWith(sdfolder)) {
                path = Constants.ROOT_PATH + path.substring(1, path.length());
            }
            if (mCustomFileObserver != null) {
                mCustomFileObserver.stopWatching();
                mCustomFileObserver = null;
            }
            mCustomFileObserver = new CustomFileObserver(path);
            mCustomFileObserver.startWatching();
        }
    }

    public void clearNivagateFocus() {
        mEt_search_view.clearFocus();
        mEt_nivagation.clearFocus();
    }

    private class UsbUninstallListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            mUsbPath = (String) view.getTag();
            uninstallUSB((String) view.getTag());
        }
    }

    private class HomeLeftOnHoverListener implements View.OnHoverListener {
        @Override
        public boolean onHover(View view, MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    view.setBackground(getResources().getDrawable(android.R.color.holo_purple));
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (!view.isSelected()) {
                        view.setBackground(getResources().getDrawable(R.drawable.left_bg_shape));
                    }
                    break;
            }
            return false;
        }
    }

    private void clickComputer() {
        mIsSdStorageFragment = true;
        mEt_nivagation.setText(null);
        setSelectView(mTv_computer);
        Fragment fragment = mManager.findFragmentByTag(Constants.SYSTEMSPACEFRAGMENT_TAG);
        if (fragment != null) {
            FragmentTransaction transaction = mManager.beginTransaction();
            transaction.remove(fragment).commit();
        }

        setFileInfo(R.id.tv_computer, "", mSdStorageFragment);
        if (mSdStorageFragment != null) {
            mSdStorageFragment.setSelectedCardBg(Constants.RETURN_TO_WHITE);
        }
    }

    private class HomeLeftOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            clearNivagateFocus();
            switch (motionEvent.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    setSelectView(view);
                    switch (view.getId()) {
                        case R.id.tv_desk:
                            setFileInfo(R.id.tv_desk, Constants.DESKTOP_PATH, mDeskFragment);
                            checkFolder(mDeskFragment);
                            break;
                        case R.id.tv_music:
                            setFileInfo(R.id.tv_music, Constants.MUSIC_PATH, mMusicFragment);
                            checkFolder(mMusicFragment);
                            break;
                        case R.id.tv_video:
                            setFileInfo(R.id.tv_video, Constants.VIDEOS_PATH, mVideoFragment);
                            checkFolder(mVideoFragment);
                            break;
                        case R.id.tv_picture:
                            setFileInfo(R.id.tv_picture, Constants.PICTURES_PATH, mPictrueFragment);
                            checkFolder(mPictrueFragment);
                            break;
                        case R.id.tv_document:
                            setFileInfo(R.id.tv_document, Constants.DOCUMENT_PATH, mDocumentFragment);
                            checkFolder(mDocumentFragment);
                            break;
                        case R.id.tv_download:
                            setFileInfo(R.id.tv_download, Constants.DOWNLOAD_PATH, mDownloadFragment);
                            checkFolder(mDownloadFragment);
                            break;
                        case R.id.tv_recycle:
                            setFileInfo(R.id.tv_recycle, Constants.RECYCLE_PATH, mRecycleFragment);
                            checkFolder(mRecycleFragment);
                            break;
                        case R.id.tv_computer:
                            clickComputer();
                            break;
                        case R.id.tv_cloud_service:
                            setFileInfo(R.id.tv_cloud_service, "", mSeafileFragment);
                            break;
                        default:
                            mUsbPath = (String) view.getTag();
                            if (mCurFragment != null) {
                                mManager.beginTransaction().hide(mCurFragment).commit();
                            }
                            mUsbStorageFragment = new SystemSpaceFragment(
                                    Constants.USB_SPACE_FRAGMENT, mUsbPath, null, null, false);
                            mManager.beginTransaction().add(R.id.fl_mian, mUsbStorageFragment,
                                    Constants.USBFRAGMENT_TAG).commit();
                            mCurFragment = mUsbStorageFragment;
                            break;
                    }
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    if (view.getTag() != null) {
                        mUsbPath = (String) view.getTag();
                        showPopWindow(USB_POPWINDOW_TAG);
                    }
                    break;
            }
            return false;
        }
    }

    public static ContentResolver getResolver() {
        return mContentResolver;
    }

    public static Uri getUri() {
        return mUri;
    }

    public static boolean getCtrlState() {
        return mIsCtrlPress;
    }

    public static boolean getShiftState() {
        return mIsShiftPress;
    }

    public static void setState(boolean isCtrlPress, boolean isShiftPress) {
        mIsCtrlPress = isCtrlPress;
        mIsShiftPress = isShiftPress;
    }
}
