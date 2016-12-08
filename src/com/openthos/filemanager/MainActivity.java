package com.openthos.filemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.FragmentTransaction;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.ProgressDialog;

import com.openthos.filemanager.component.CopyInfoDialog;
import com.openthos.filemanager.component.PopOnClickLintener;
import com.openthos.filemanager.component.PopWinShare;
import com.openthos.filemanager.component.SearchOnClickListener;
import com.openthos.filemanager.component.SearchOnKeyListener;
import com.openthos.filemanager.fragment.DeskFragment;
import com.openthos.filemanager.fragment.MusicFragment;
import com.openthos.filemanager.fragment.OnlineNeighborFragment;
import com.openthos.filemanager.fragment.PictrueFragment;
import com.openthos.filemanager.fragment.SdStorageFragment;
import com.openthos.filemanager.fragment.VideoFragment;
import com.openthos.filemanager.fragment.PersonalSpaceFragment;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.utils.DisplayUtil;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.T;
import com.openthos.filemanager.fragment.SystemSpaceFragment;
import com.openthos.filemanager.system.IFileInteractionListener;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileOperationHelper;
import com.openthos.filemanager.system.FileSortHelper;
import com.openthos.filemanager.fragment.PersonalSpaceFragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import android.widget.Toast;
import java.io.IOException;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final int POPWINDOW_WINTH = 120;
    private static final int POPWINDOW_HEIGHT = 40;
    private static final int POPWINDOW_X = -15;
    private static final int POPWINDOW_Y = 10;
    private static final int ACTIVITY_MIN_COUNT_FOR_BACK = 3;
    private static final String USB_SPACE_FRAGMENT = "usb_space_fragment";
    private static final String USB_DEVICE_ATTACHED = "usb_device_attached";
    private static final String USB_DEVICE_DETACHED = "usb_device_detached";
    private static final String VIEW_TAG = "viewtag";
    private static final String VIEW_TAG_GRID = "grid";
    private static final String VIEW_TAG_LIST = "list";
    private static final String IV_SWITCH_VIEW = "iv_switch_view";
    private static final String SETTING_POPWINDOW_TAG = "iv_setting";
    private TextView mTv_desk;
    private TextView mTv_music;
    private TextView mTv_video;
    private TextView mTv_computer;
    private TextView mTv_picture;
    private TextView mTv_storage;
    private TextView mTv_document;
    private TextView mTv_download;
    private TextView mTv_net_service;
    private ImageView mIv_list_view;
    private ImageView mIv_grid_view;
    private ImageView mIv_back;
    private ImageView mIv_setting;
    private EditText mEt_nivagation;
    private EditText mEt_search_view;
    private ImageView mIv_search_view;
    private TextView mTv_pop_up;
    private RelativeLayout mRl_usb;

    private FragmentManager mManager = getSupportFragmentManager();
    private PopWinShare mPopWinShare;
    public Fragment mCurFragment;
    public SdStorageFragment mSdStorageFragment;
    public boolean mIsSdStorageFragmentHided;
    private SystemSpaceFragment mDeskFragment, mMusicFragment, mVideoFragment,
                                mPictrueFragment, mAddressFragment,
                                mDocumentFragment, mDownloadFragment;
    private OnlineNeighborFragment mOnlineNeighborFragment;
    private UsbConnectReceiver mReceiver;
    private String[] mUsbs;
    private boolean mIsMutiSelect;
    private SharedPreferences mSharedPreferences;
    private Editor mEditor;
    public boolean mIsSdStorageFragment;

    public static Handler mHandler;
    private boolean mIsFirst = true;
    private HashMap<String, Integer> mHashMap;
    private SearchOnKeyListener mSearchOnKeyListener;
    private ProgressDialog mProgressDialog;
    private CopyInfoDialog mCopyInfoDialog;
    public PersonalSpaceFragment mPersonalSpaceFragment;
    private SystemSpaceFragment mUsbStorageFragment;

    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    protected void initView() {
        mSharedPreferences = getSharedPreferences("view", Context.MODE_PRIVATE);
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
        mTv_storage = (TextView) findViewById(R.id.tv_storage);
        mTv_net_service = (TextView) findViewById(R.id.tv_net_service);
        mIv_list_view = (ImageView) findViewById(R.id.iv_list_view);
        mIv_grid_view = (ImageView) findViewById(R.id.iv_grid_view);
        mIv_back = (ImageView) findViewById(R.id.iv_back);
        mIv_setting = (ImageView) findViewById(R.id.iv_setting);
        mEt_nivagation = (EditText) findViewById(R.id.et_nivagation);
        mIv_search_view = (ImageView) findViewById(R.id.iv_search);
        mEt_search_view = (EditText) findViewById(R.id.search_view);
        mTv_pop_up = (TextView) findViewById(R.id.tv_pop_up);
        mRl_usb = (RelativeLayout) findViewById(R.id.rl_usb);
        mIv_grid_view.setSelected(true);

        File file = new File(Constants.DOCUMENT_PATH);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdir();
        }

        mHashMap = new HashMap<>();
        mHashMap.put(Constants.DESKFRAGMENT_TAG, R.id.tv_desk);
        mHashMap.put(Constants.MUSICFRAGMENT_TAG, R.id.tv_music);
        mHashMap.put(Constants.VIDEOFRAGMENT_TAG, R.id.tv_video);
        mHashMap.put(Constants.PICTRUEFRAGMENT_TAG, R.id.tv_picture);
        mHashMap.put(Constants.DOCUMENTFRAGMENT_TAG, R.id.tv_document);
        mHashMap.put(Constants.DOWNLOADFRRAGMENT_TAG, R.id.tv_download);
        mHashMap.put(Constants.SDSTORAGEFRAGMENT_TAG, R.id.tv_computer);
        mHashMap.put(Constants.ONLINENEIGHBORFRAGMENT_TAG, R.id.tv_net_service);
        mHashMap.put(Constants.DETAILFRAGMENT_TAG, R.id.tv_picture);
        mHashMap.put(Constants.SYSTEMSPACEFRAGMENT_TAG, R.id.tv_storage);
        mHashMap.put(Constants.ADDRESSFRAGMENT_TAG, R.id.tv_storage);
        mHashMap.put(Constants.SYSTEM_SPACE_FRAGMENT_TAG, R.id.tv_computer);
        mHashMap.put(Constants.USBFRAGMENT_TAG,R.id.tv_storage);
        mCopyInfoDialog = CopyInfoDialog.getInstance(MainActivity.this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (!Thread.currentThread().isInterrupted()) {
                    switch (msg.what) {
                        case UsbConnectReceiver.USB_STATE_ON:
                            initUsb(UsbConnectReceiver.USB_STATE_ON);
                            break;
                        case UsbConnectReceiver.USB_STATE_OFF:
                            initUsb(UsbConnectReceiver.USB_STATE_OFF);
                            break;
                        case 2:
                            initUsb(0);
                            break;
                        case Constants.USB_READY:
                            mRl_usb.setVisibility(View.VISIBLE);
                            if (mProgressDialog != null) {
                                mProgressDialog.dismiss();
                            }
                            mTv_computer.performClick();
                            break;
                        case Constants.REFRESH:
                            ((IFileInteractionListener) getVisibleFragment())
                                         .onRefreshFileList((String) msg.obj, new FileSortHelper());
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
                            mCopyInfoDialog.showDialog();
                            mCopyInfoDialog.changeTitle(MainActivity.this.getResources()
                                                                    .getString(R.string.copy_info));
                            break;
                        case Constants.COPY_INFO:
                            mCopyInfoDialog.changeMsg((String) msg.obj);
                            break;
                        case Constants.COPY_INFO_HIDE:
                            mCopyInfoDialog.cancel();
                            break;
                    }
                }
                super.handleMessage(msg);
            }
        };
    }

    private void initUsb(int flags) {
        String[] cmd = new String[]{"df"};
        mUsbs = Util.execUsb(cmd);
        if (mUsbs != null && mUsbs.length > 0 && flags != 0 && flags != 1) {
            sendMsg(2);
        }
        if (flags == UsbConnectReceiver.USB_STATE_ON || flags == 2) {
         // T.showShort(MainActivity.this, getResources().getString(R.string.USB_device_connected));
         // mRl_usb.setVisibility(View.VISIBLE);
            mTv_storage.setOnClickListener(MainActivity.this);
            mTv_pop_up.setOnClickListener(this);
            mManager.beginTransaction().remove(mSdStorageFragment).commit();
            mManager.beginTransaction().hide(mCurFragment).commit();
            mSdStorageFragment = new SdStorageFragment(mManager, USB_DEVICE_ATTACHED,
                                                      MainActivity.this);
            setSelectedBackground(R.id.tv_computer);
            mManager.beginTransaction().add(R.id.fl_mian, mSdStorageFragment).commit();
          //  T.showShort(MainActivity.this, getResources().getString(R.string.USB_device_connected));
          //  mTv_computer.performClick();
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
            }
            mProgressDialog.setMessage(getString(R.string.USB_recognising));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(true);
            mProgressDialog.show();
            mCurFragment = mSdStorageFragment;
        } else if (flags == UsbConnectReceiver.USB_STATE_OFF) {
            mRl_usb.setVisibility(View.GONE);
            mManager.beginTransaction().remove(mSdStorageFragment).commit();
            mManager.beginTransaction().hide(mCurFragment).commit();
            mSdStorageFragment = new SdStorageFragment(mManager, USB_DEVICE_DETACHED,
                                                      MainActivity.this);
            setSelectedBackground(R.id.tv_computer);
           // mManager.beginTransaction().remove(mSdStorageFragment).commit();
            mManager.beginTransaction().add(R.id.fl_mian, mSdStorageFragment).commit();
            mCurFragment = mSdStorageFragment;
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
                                                    Constants.DESKTOP_PATH, null, null);
            transaction.add(R.id.fl_mian, mDeskFragment).hide(mDeskFragment);
        }
        if (mMusicFragment == null) {
            mMusicFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                     Constants.MUSIC_PATH, null, null);
            transaction.add(R.id.fl_mian, mMusicFragment).hide(mMusicFragment);
        }
        if (mVideoFragment == null) {
            mVideoFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                     Constants.VIDEOS_PATH, null, null);
            transaction.add(R.id.fl_mian, mVideoFragment).hide(mVideoFragment);
        }
        if (mPictrueFragment == null) {
            mPictrueFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                       Constants.PICTURES_PATH, null, null);
            transaction.add(R.id.fl_mian, mPictrueFragment).hide(mPictrueFragment);
        }
        if (mDocumentFragment == null) {
            mDocumentFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                        Constants.DOCUMENT_PATH, null, null);
            transaction.add(R.id.fl_mian, mDocumentFragment).hide(mDocumentFragment);
        }
        if (mDownloadFragment == null) {
            mDownloadFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                        Constants.DOWNLOAD_PATH, null, null);
            transaction.add(R.id.fl_mian, mDownloadFragment).hide(mDownloadFragment);
        }
        if (mOnlineNeighborFragment == null) {
            mOnlineNeighborFragment = new OnlineNeighborFragment();
            transaction.add(R.id.fl_mian, mOnlineNeighborFragment).hide(mOnlineNeighborFragment);
        }
        if (mPersonalSpaceFragment == null) {
            mPersonalSpaceFragment = new PersonalSpaceFragment();
            transaction.add(R.id.fl_mian, mPersonalSpaceFragment).hide(mPersonalSpaceFragment);
        }
        transaction.commit();
    }

    protected void initData() {
        initFragment();
        checkFolder(null);
    }

    private void checkFolder(Fragment fragment) {
        List<String> fileList = new ArrayList<>();
        fileList.add(Constants.DESKTOP_PATH);
        fileList.add(Constants.MUSIC_PATH);
        fileList.add(Constants.VIDEOS_PATH);
        fileList.add(Constants.PICTURES_PATH);
        fileList.add(Constants.DOCUMENT_PATH);
        fileList.add(Constants.DOWNLOAD_PATH);
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
        mTv_desk.setOnClickListener(this);
        mTv_music.setOnClickListener(this);
        mTv_video.setOnClickListener(this);
        mTv_computer.setOnClickListener(this);
        mTv_picture.setOnClickListener(this);
        mTv_document.setOnClickListener(this);
        mTv_download.setOnClickListener(this);
        mTv_net_service.setOnClickListener(this);
        mIv_list_view.setOnClickListener(this);
        mIv_grid_view.setOnClickListener(this);
        mIv_back.setOnClickListener(this);
        mIv_setting.setOnClickListener(this);
        mTv_computer.performClick();
//        search_view.addTextChangedListener(new EditTextChangeListener(mManager,
//                                                                        MainActivity.this));
        mSearchOnKeyListener = new SearchOnKeyListener(mManager,
                                        mEt_search_view.getText(), MainActivity.this);
        mEt_search_view.setOnKeyListener(mSearchOnKeyListener);
        mIv_search_view.setOnClickListener(new SearchOnClickListener(mManager,
                                          mEt_search_view.getText(), MainActivity.this));
        NivagationOnClickLinstener nivagationOnClickLinstener = new NivagationOnClickLinstener();
        NivagationOnKeyLinstener nivagationOnKeyLinstener =new NivagationOnKeyLinstener();
        mEt_nivagation.setOnClickListener(nivagationOnClickLinstener);
        mEt_nivagation.setOnKeyListener(nivagationOnKeyLinstener);
        initUsb(-1);
        mCurFragment = mSdStorageFragment;
        Intent intent = getIntent();
        String path = intent.getStringExtra(Intent.EXTRA_DESKTOP_PATH_TAG);
        if (path != null) {
            mEt_nivagation.setText(path);
            mEt_nivagation.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                                                         KeyEvent.KEYCODE_ENTER));
            mEt_nivagation.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                                                         KeyEvent.KEYCODE_ENTER));
        }
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
                    showSpaceFragment((TextView) v);
                    return true;
                case KeyEvent.KEYCODE_ESCAPE:
                    v.clearFocus();
                    return true;
            }
            return false;
        }
    }

    private void showSpaceFragment(TextView textView) {
        FragmentTransaction transaction = mManager.beginTransaction();
        String path = textView.getText().toString().trim();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File file = new File(path);
        if (file.exists()) {
            transaction.hide(mCurFragment);
            mAddressFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES, path, null, null);
            transaction.add(R.id.fl_mian, mAddressFragment, Constants.ADDRESSFRAGMENT_TAG);
            transaction.show(mAddressFragment).addToBackStack(null).commit();
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

        public static final int USB_STATE_ON = 0;
        public static final int USB_STATE_OFF = 1;
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
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED) ||
                    intent.getAction().equals(Intent.ACTION_MEDIA_CHECKING)) {
                sendMsg(USB_STATE_ON);
            } else if (action.equals(Intent.ACTION_MEDIA_REMOVED) ||
                    action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                sendMsg(USB_STATE_OFF);
            }
        }
    }

    private void sendMsg(int flags) {
        Message msg = new Message();
        msg.what = flags;
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mIsMutiSelect = false;
        if (!mIsMutiSelect && !mIsFirst){
            sendBroadcastMessage("is_ctrl_press", null, mIsMutiSelect);
            mIsFirst = true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL && !mEt_search_view.hasFocus()) {
            onBackPressed();
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            L.e("KEYCODE_ENTER", "KEYCODE_ENTER");
        }
        if (event.isCtrlPressed()) {
            mIsMutiSelect = true;
        }
        if (mIsMutiSelect && mIsFirst) {
            sendBroadcastMessage("is_ctrl_press", null, mIsMutiSelect);
            mIsFirst = false;
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_X) {
            sendBroadcastMessage("iv_menu", "pop_cut", false);
            if (getVisibleFragment() instanceof PersonalSpaceFragment
                                     || getVisibleFragment() instanceof SdStorageFragment
                                     || getVisibleFragment() instanceof OnlineNeighborFragment
                                     || mEt_nivagation.isFocused() || mEt_search_view.isFocused()) {
                return false;
            }
            cut();
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_C) {
            sendBroadcastMessage("iv_menu", "pop_copy", false);
            if (getVisibleFragment() instanceof PersonalSpaceFragment
                                     || getVisibleFragment() instanceof SdStorageFragment
                                     || getVisibleFragment() instanceof OnlineNeighborFragment
                                     || mEt_nivagation.isFocused() || mEt_search_view.isFocused()) {
                return false;
            }
            copy();
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_V) {
            sendBroadcastMessage("iv_menu", "pop_paste", false);
            if (getVisibleFragment() instanceof PersonalSpaceFragment
                                     || getVisibleFragment() instanceof SdStorageFragment
                                     || getVisibleFragment() instanceof OnlineNeighborFragment
                                     || mEt_nivagation.isFocused() || mEt_search_view.isFocused()) {
                return false;
            }
            paste();
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_Z) {
            sendBroadcastMessage("iv_menu", "pop_cacel", false);
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_D) {
            sendBroadcastMessage("iv_menu", "pop_delete", false);
        }
        return false;
    }

    private void cut() {
        ArrayList<FileInfo> list =
               ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub.getSelectedFileList();
        if (!list.isEmpty()) {
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                      .setText(Intent.EXTRA_CROP_FILE_HEADER + list.get(list.size() - 1).filePath);
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
        if (!TextUtils.isEmpty(sourcePath) && sourcePath.startsWith(Intent.EXTRA_FILE_HEADER)) {
            new CopyThread(sourcePath.replace(Intent.EXTRA_FILE_HEADER, ""), destPath).start();
        } else if (!TextUtils.isEmpty(sourcePath)
                                        && sourcePath.startsWith(Intent.EXTRA_CROP_FILE_HEADER)) {
            new CropThread(sourcePath.replace(Intent.EXTRA_CROP_FILE_HEADER, ""), destPath).start();
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
        String mSourcePath;
        String mDestPath;

        public CopyThread(String sourcePath, String destPath) {
            super();
            mSourcePath = sourcePath;
            mDestPath = destPath;
        }

        @Override
        public void run() {
            super.run();
            FileOperationHelper.CopyFile(
                                 mSourcePath.replace(Intent.EXTRA_FILE_HEADER, ""), mDestPath);
        }
    }

    class CropThread extends Thread {
        String mSourcePath;
        String mDestPath;

        public CropThread(String sourcePath, String destPath) {
            super();
            mSourcePath = sourcePath;
            mDestPath = destPath;
        }

        @Override
        public void run() {
            super.run();
            FileOperationHelper.MoveFile(
                                 mSourcePath.replace(Intent.EXTRA_CROP_FILE_HEADER, ""), mDestPath);
        }
    }

    public void copy() {
        ArrayList<FileInfo> list =
                ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub.getSelectedFileList();
        if (!list.isEmpty()) {
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                            .setText(Intent.EXTRA_FILE_HEADER + list.get(list.size() - 1).filePath);
        }
    }

    @Override
    public void onClick(View view) {
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
            case R.id.tv_computer:
                mIsSdStorageFragment = true;
                mEt_nivagation.setText(null);
                Fragment fragment = mManager.findFragmentByTag(Constants.SYSTEMSPACEFRAGMENT_TAG);
                if (fragment != null) {
                    FragmentTransaction transaction = mManager.beginTransaction();
                    transaction.remove(fragment).commit();
                }

                setFileInfo(R.id.tv_computer, "", mSdStorageFragment);
                break;
            case R.id.tv_storage:
                setSelectedBackground(R.id.tv_storage);
                if (mCurFragment != null) {
                    mManager.beginTransaction().hide(mCurFragment).commit();
                }
                mUsbStorageFragment = new SystemSpaceFragment
                                          (Constants.USB_SPACE_FRAGMENT, mUsbs[0], null, null);
                mManager.beginTransaction().add(R.id.fl_mian, mUsbStorageFragment,
                                               Constants.USBFRAGMENT_TAG).commit();
                mCurFragment = mUsbStorageFragment;
                break;
            case R.id.tv_pop_up:
                mRl_usb.setVisibility(View.GONE);
                mManager.beginTransaction().hide(getVisibleFragment()).commit();
                mSdStorageFragment = new SdStorageFragment(mManager, USB_DEVICE_DETACHED,
                        MainActivity.this);
                setSelectedBackground(R.id.tv_computer);
                mManager.beginTransaction().add(R.id.fl_mian, mSdStorageFragment).commit();
                mCurFragment = mSdStorageFragment;
                break;
            case R.id.tv_net_service:
                setFileInfo(R.id.tv_net_service, "", mOnlineNeighborFragment);
                break;
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_setting:
                shownPopWidndow(SETTING_POPWINDOW_TAG);
                break;
            case R.id.iv_grid_view:
                mIv_grid_view.setSelected(true);
                mIv_list_view.setSelected(false);
                if (!VIEW_TAG_GRID.equals(LocalCache.getViewTag())
                    || VIEW_TAG_GRID.equals(LocalCache.getViewTag())) {
                    LocalCache.setViewTag(VIEW_TAG_GRID);
                }
                sendBroadcastMessage(IV_SWITCH_VIEW, VIEW_TAG_GRID, false);
                mEditor.putString(VIEW_TAG, VIEW_TAG_GRID);
                mEditor.commit();
                break;
            case R.id.iv_list_view:
                mIv_grid_view.setSelected(false);
                mIv_list_view.setSelected(true);
                if (!VIEW_TAG_LIST.equals(LocalCache.getViewTag())
                    || VIEW_TAG_LIST.equals(LocalCache.getViewTag())) {
                    LocalCache.setViewTag(VIEW_TAG_LIST);
                }
                sendBroadcastMessage(IV_SWITCH_VIEW, VIEW_TAG_LIST, false);
                mEditor.putString(VIEW_TAG, VIEW_TAG_LIST);
                mEditor.commit();
                break;
        }
    }

    private void setFileInfo(int id, String path, Fragment fragment) {
        setSelectedBackground(id);
        mEt_nivagation.setText(path);
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
                mTv_music.setSelected(false);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(true);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
                mTv_document.setSelected(false);
                mTv_download.setSelected(false);
                break;
            case R.id.tv_desk:
                mTv_desk.setSelected(true);
                mTv_music.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
                mTv_document.setSelected(false);
                mTv_download.setSelected(false);
                break;
            case R.id.tv_music:
                mTv_music.setSelected(true);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
                mTv_document.setSelected(false);
                mTv_download.setSelected(false);
                break;
            case R.id.tv_video:
                mTv_music.setSelected(false);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(true);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
                mTv_document.setSelected(false);
                mTv_download.setSelected(false);
                break;
            case R.id.tv_picture:
                mTv_music.setSelected(false);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(true);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
                mTv_document.setSelected(false);
                mTv_download.setSelected(false);
                break;
            case R.id.tv_document:
                mTv_music.setSelected(false);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
                mTv_document.setSelected(true);
                mTv_download.setSelected(false);
                break;
            case R.id.tv_download:
                mTv_music.setSelected(false);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
                mTv_document.setSelected(false);
                mTv_download.setSelected(true);
                break;
            case R.id.tv_storage:
                mTv_music.setSelected(false);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(true);
                mTv_net_service.setSelected(false);
                break;
            case R.id.tv_net_service:
                mTv_music.setSelected(false);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(true);
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

    private void shownPopWidndow(String menu_tag) {
        mPopWinShare = null;
        PopOnClickLintener paramOnClickListener = new PopOnClickLintener(menu_tag,
                                                      MainActivity.this, mManager);
        if (SETTING_POPWINDOW_TAG.equals(menu_tag)) {
            mPopWinShare = new PopWinShare(MainActivity.this, paramOnClickListener,
                               DisplayUtil.dip2px(MainActivity.this, POPWINDOW_WINTH),
                               DisplayUtil.dip2px(MainActivity.this, POPWINDOW_HEIGHT), menu_tag);
            mPopWinShare.setFocusable(true);
            mPopWinShare.showAsDropDown(mIv_setting, POPWINDOW_X, POPWINDOW_Y);
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
                } else {
                    returnToRootDir();
                }
            } else {
                returnToRootDir();
            }
        }
    }

    public Fragment getVisibleFragment(){
        List<Fragment> fragments = mManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
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
        mEt_nivagation.setText(null);
        setSelectedBackground(R.id.tv_computer);
        mSdStorageFragment.setSelectedCardBg(Constants.RETURN_TO_WHITE);
        mCurFragment = mSdStorageFragment;
    }

    public interface IBackPressedListener {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver.unregisterReceiver();
    }

    @Override
    public void setNavigationBar(String displayPath) {
        if (displayPath != null) {
            if (mCurFragment == mSdStorageFragment && mSdStorageFragment.mCurFragment != null) {
                mEt_nivagation.setText(displayPath);
            } else {
                if (mCurFragment instanceof SystemSpaceFragment) {
                    mEt_nivagation.setText(displayPath);
                }else {
                    mEt_nivagation.setText(null);
                }
            }
        }
    }

    public void setNavigationPath(String displayPath) {
        mEt_nivagation.setText(displayPath);
    }
}
