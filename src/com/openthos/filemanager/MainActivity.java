package com.openthos.filemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.FragmentTransaction;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;

import com.openthos.filemanager.component.PopOnClickLintener;
import com.openthos.filemanager.component.PopWinShare;
import com.openthos.filemanager.component.SearchOnClickListener;
import com.openthos.filemanager.component.SearchOnEditorActionListener;
import com.openthos.filemanager.fragment.DeskFragment;
import com.openthos.filemanager.fragment.MusicFragment;
import com.openthos.filemanager.fragment.OnlineNeighborFragment;
import com.openthos.filemanager.fragment.PictrueFragment;
import com.openthos.filemanager.fragment.SdStorageFragment;
import com.openthos.filemanager.fragment.VideoFragment;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.utils.DisplayUtil;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.T;
import com.openthos.filemanager.fragment.SystemSpaceFragment;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.fragment.PersonalSpaceFragment;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements View.OnClickListener,
                                               TextView.OnEditorActionListener {
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
                                mPictrueFragment, mAddressFragment;
    private OnlineNeighborFragment mOnlineNeighborFragment;
    private UsbConnectReceiver mReceiver;
    private String[] mUsbs;
    private boolean mIsMutiSelect;
    private SharedPreferences mSharedPreferences;
    private Editor mEditor;
    public boolean mIsSdStorageFragment;

    public Handler mHandler = new Handler() {
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
                        mTv_computer.performClick();
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };
    private boolean mIsFirst = true;
    private HashMap<String, Integer> mHashMap;
    private SearchOnEditorActionListener mSearchOnEditorActionListener;

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

        mHashMap = new HashMap<>();
        mHashMap.put(Constants.DESKFRAGMENT_TAG,R.id.tv_desk);
        mHashMap.put(Constants.MUSICFRAGMENT_TAG,R.id.tv_music);
        mHashMap.put(Constants.VIDEOFRAGMENT_TAG,R.id.tv_video);
        mHashMap.put(Constants.PICTRUEFRAGMENT_TAG,R.id.tv_picture);
        mHashMap.put(Constants.SDSTORAGEFRAGMENT_TAG,R.id.tv_computer);
        mHashMap.put(Constants.ONLINENEIGHBORFRAGMENT_TAG,R.id.tv_net_service);
        mHashMap.put(Constants.DETAILFRAGMENT_TAG,R.id.tv_picture);
        mHashMap.put(Constants.SYSTEMSPACEFRAGMENT_TAG,R.id.tv_storage);
        mHashMap.put(Constants.ADDRESSFRAGMENT_TAG,R.id.tv_storage);
        mHashMap.put(Constants.SYSTEM_SPACE_FRAGMENT_TAG, R.id.tv_computer);
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
            mManager.beginTransaction().replace(R.id.fl_mian, mSdStorageFragment,
                        Constants.SDSTORAGEFRAGMENT_TAG).commit();
          //  T.showShort(MainActivity.this, getResources().getString(R.string.USB_device_connected));
          //  mTv_computer.performClick();
            mCurFragment = mSdStorageFragment;
        } else if (flags == UsbConnectReceiver.USB_STATE_OFF) {
            mRl_usb.setVisibility(View.GONE);
            mManager.beginTransaction().hide(mCurFragment).commit();
            mSdStorageFragment = new SdStorageFragment(mManager, USB_DEVICE_DETACHED,
                                                      MainActivity.this);
            setSelectedBackground(R.id.tv_computer);
           // mManager.beginTransaction().remove(mSdStorageFragment).commit();
            mManager.beginTransaction().replace(R.id.fl_mian, mSdStorageFragment,
                     Constants.SDSTORAGEFRAGMENT_TAG).commit();
        }
    }

    private void initFragment() {
        mReceiver = new UsbConnectReceiver(this);
        FragmentTransaction transaction = mManager.beginTransaction();
        if (mSdStorageFragment == null) {
            mSdStorageFragment = new SdStorageFragment(mManager, null, MainActivity.this);
            transaction.add(R.id.fl_mian, mSdStorageFragment,Constants.SDSTORAGEFRAGMENT_TAG)
                           .hide(mSdStorageFragment)
                           .addToBackStack(null);
        }
        if (mDeskFragment == null) {
            mDeskFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                    Constants.DESKTOP_PATH, null, null);
            transaction.add(R.id.fl_mian, mDeskFragment,Constants.DESKFRAGMENT_TAG)
                       .hide(mDeskFragment);
        }
        if (mMusicFragment == null) {
            mMusicFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                     Constants.MUSIC_PATH, null, null);
            transaction.add(R.id.fl_mian, mMusicFragment,Constants.MUSICFRAGMENT_TAG)
                       .hide(mMusicFragment);
        }
        if (mVideoFragment == null) {
            mVideoFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                     Constants.VIDEOS_PATH, null, null);
            transaction.add(R.id.fl_mian, mVideoFragment,Constants.VIDEOFRAGMENT_TAG)
                       .hide(mVideoFragment);
        }
        if (mPictrueFragment == null) {
            mPictrueFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES,
                                                       Constants.PICTURES_PATH, null, null);
            transaction.add(R.id.fl_mian, mPictrueFragment,Constants.PICTRUEFRAGMENT_TAG)
                       .hide(mPictrueFragment);
        }
        if (mOnlineNeighborFragment == null) {
            mOnlineNeighborFragment = new OnlineNeighborFragment();
            transaction.add(R.id.fl_mian, mOnlineNeighborFragment,
                            Constants.ONLINENEIGHBORFRAGMENT_TAG).hide(mOnlineNeighborFragment);
        }
        transaction.commit();
    }

    protected void initData() {
        initFragment();
    }

    @Override
    protected void initListener() {
        mTv_desk.setOnClickListener(this);
        mTv_music.setOnClickListener(this);
        mTv_video.setOnClickListener(this);
        mTv_computer.setOnClickListener(this);
        mTv_picture.setOnClickListener(this);
        mTv_net_service.setOnClickListener(this);
        mIv_list_view.setOnClickListener(this);
        mIv_grid_view.setOnClickListener(this);
        mIv_back.setOnClickListener(this);
        mIv_setting.setOnClickListener(this);
        mTv_computer.performClick();
//        search_view.addTextChangedListener(new EditTextChangeListener(mManager,
//                                                                        MainActivity.this));
        mSearchOnEditorActionListener = new SearchOnEditorActionListener(mManager,
                                        mEt_search_view.getText(), MainActivity.this);
        mEt_search_view.setOnEditorActionListener(mSearchOnEditorActionListener);
        mIv_search_view.setOnClickListener(new SearchOnClickListener(mManager,
                                          mEt_search_view.getText(), MainActivity.this));
        mEt_nivagation.setOnEditorActionListener(this);
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

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        FragmentTransaction transaction = mManager.beginTransaction();
        String path = textView.getText().toString().trim();
        File file = new File(path);
        if (file.exists()) {
            transaction.hide(mCurFragment);
            mAddressFragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES, path, null, null);
            transaction.add(R.id.fl_mian, mAddressFragment ,Constants.ADDRESSFRAGMENT_TAG);
            transaction.show(mAddressFragment).addToBackStack(null).commit();
            mCurFragment = mAddressFragment;
            setFileInfo(R.id.et_nivagation, path, mAddressFragment);
        } else {
            Toast.makeText(this, "" + getResources().getString(R.string.address_search_false),
                                                                   Toast.LENGTH_SHORT).show();
        }
        return false;
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
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_C) {
            sendBroadcastMessage("iv_menu", "pop_copy", false);
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_V) {
            sendBroadcastMessage("iv_menu", "pop_paste", false);
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_Z) {
            sendBroadcastMessage("iv_menu", "pop_cacel", false);
        }
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_D) {
            sendBroadcastMessage("iv_menu", "pop_delete", false);
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_desk:
                setFileInfo(R.id.tv_desk, Constants.DESKTOP_PATH, mDeskFragment);
                break;
            case R.id.tv_music:
                setFileInfo(R.id.tv_music, Constants.MUSIC_PATH, mMusicFragment);
                break;
            case R.id.tv_video:
                setFileInfo(R.id.tv_video, Constants.VIDEOS_PATH, mVideoFragment);
                break;
            case R.id.tv_picture:
                setFileInfo(R.id.tv_picture, Constants.PICTURES_PATH, mPictrueFragment);
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
                SystemSpaceFragment usbStorageFragment = new SystemSpaceFragment
                                          (Constants.USB_SPACE_FRAGMENT, mUsbs[0], null, null);
                mManager.beginTransaction().add(R.id.fl_mian, usbStorageFragment)
                                          .hide(usbStorageFragment).commit();
                break;
            case R.id.tv_pop_up:
                mRl_usb.setVisibility(View.GONE);
                mManager.beginTransaction().remove(getVisibleFragment()).commit();
                mSdStorageFragment = new SdStorageFragment(mManager, USB_DEVICE_DETACHED,
                        MainActivity.this);
                setSelectedBackground(R.id.tv_computer);
                mManager.beginTransaction().replace(R.id.fl_mian,
                         mSdStorageFragment,Constants.SDSTORAGEFRAGMENT_TAG)
                        .commit();
                mCurFragment = null;
                mIsSdStorageFragmentHided = false;
                mTv_computer.performClick();
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
            if (mIsSdStorageFragmentHided) {
                transaction.hide(mSdStorageFragment.mCurFragment);
                if (mSdStorageFragment.mCurFragment instanceof PersonalSpaceFragment) {
                    PersonalSpaceFragment personalSpaceFragment =
                               (PersonalSpaceFragment) mSdStorageFragment.mCurFragment;
                    transaction.hide(personalSpaceFragment);
                    if (personalSpaceFragment.mCurFragment != null) {
                        transaction.hide(personalSpaceFragment.mCurFragment);
                    }
                }
                mIsSdStorageFragmentHided = false;
            }
        }
        if (fragment != null) {
            transaction.show(fragment).addToBackStack(null);
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
                break;
            case R.id.tv_desk:
                mTv_desk.setSelected(true);
                mTv_music.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
                break;
            case R.id.tv_music:
                mTv_music.setSelected(true);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
                break;
            case R.id.tv_video:
                mTv_music.setSelected(false);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(true);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(false);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
                break;
            case R.id.tv_picture:
                mTv_music.setSelected(false);
                mTv_desk.setSelected(false);
                mTv_video.setSelected(false);
                mTv_computer.setSelected(false);
                mTv_picture.setSelected(true);
                mTv_storage.setSelected(false);
                mTv_net_service.setSelected(false);
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
        mSearchOnEditorActionListener.setInputData(null);
        mManager.findFragmentById(R.id.fl_mian);
        if ((mCurFragment != null) && (mCurFragment == mSdStorageFragment)) {
            if (mSdStorageFragment.canGoBack()) {
                mSdStorageFragment.goBack();
            } else {
                if (mManager.getBackStackEntryCount() >= ACTIVITY_MIN_COUNT_FOR_BACK) {
                    mManager.popBackStackImmediate();
                    mCurFragment = getVisibleFragment();
                    if (mCurFragment != null) {
                        if (mCurFragment.getTag() != null) {
                            setSelectedBackground(mHashMap.get(mCurFragment.getTag()));
                        }
                    } else {
                        mManager.beginTransaction().show(mSdStorageFragment).commit();
                        mCurFragment = mSdStorageFragment;
                    }
                } else {
//                    finish();
                    returnToRootDir();
                }
            }
        } else {
            if (mManager.getBackStackEntryCount() >= ACTIVITY_MIN_COUNT_FOR_BACK) {
                mManager.popBackStackImmediate();
                mCurFragment = getVisibleFragment();
                if (mCurFragment != null) {
                    setSelectedBackground(mHashMap.get(mCurFragment.getTag()));
                } else {
                    mManager.beginTransaction().show(mSdStorageFragment).commit();
                    mCurFragment = mSdStorageFragment;
                }
            } else {
//                finish();
                returnToRootDir();
            }
            if (mCurFragment == mSdStorageFragment && mSdStorageFragment.canGoBack()
                    && !mIsSdStorageFragment) {
                if (mSdStorageFragment.mCurFragment instanceof SystemSpaceFragment) {
                    mManager.beginTransaction().hide(mSdStorageFragment).commit();
                    mIsSdStorageFragmentHided = true;
                }
            }
        }
        if (mCurFragment == mSdStorageFragment && !mIsSdStorageFragmentHided) {
            mEt_nivagation.setText(null);
        }
        if (mCurFragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment sdCurFrament = (SystemSpaceFragment) mCurFragment;
            String currentPath = sdCurFrament.getCurrentPath();
            mEt_nivagation.setText(currentPath);
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

    public void returnToRootDir() {
        if (mCurFragment != null) {
            mManager.beginTransaction().hide(mCurFragment).commit();
        }
        mManager.beginTransaction().show(mSdStorageFragment).commit();
        mCurFragment = mSdStorageFragment;
        if (mCurFragment.getTag() != null) {
            setSelectedBackground(mHashMap.get(mSdStorageFragment.getTag()));
            mSdStorageFragment.setSelectedCardBg(-1);
        }
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
                if (mCurFragment instanceof SystemSpaceFragment){
                    mEt_nivagation.setText(displayPath);
                }else {
                    mEt_nivagation.setText(null);
                }
            }
        }
    }
}
