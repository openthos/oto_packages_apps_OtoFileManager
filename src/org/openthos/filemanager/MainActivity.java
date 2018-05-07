package org.openthos.filemanager;

import android.app.ActivityThread;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;
import android.app.ProgressDialog;
import android.os.Build;
import android.content.ServiceConnection;
import android.content.ComponentName;

import java.util.Collections;
import org.openthos.filemanager.bean.FolderBean;
import org.openthos.filemanager.bean.Mode;
import org.openthos.filemanager.bean.SeafileLibrary;
import org.openthos.filemanager.bean.Volume;
import org.openthos.filemanager.component.CopyInfoDialog;
import org.openthos.filemanager.component.CloudInfoDialog;
import org.openthos.filemanager.component.FolderBeanComparator;
import org.openthos.filemanager.component.FolderCollectionDialog;
import org.openthos.filemanager.component.PopOnClickLintener;
import org.openthos.filemanager.component.PopWinShare;
import org.openthos.filemanager.component.SearchOnKeyListener;
import org.openthos.filemanager.fragment.SambaFragment;
import org.openthos.filemanager.fragment.SdStorageFragment;
import org.openthos.filemanager.fragment.PersonalSpaceFragment;
import org.openthos.filemanager.fragment.SearchFragment;
import org.openthos.filemanager.system.BootCompleteReceiver;
import org.openthos.filemanager.system.Util;
import org.openthos.filemanager.system.FileListAdapter;
import org.openthos.filemanager.utils.LocalCache;
import org.openthos.filemanager.utils.T;
import org.openthos.filemanager.fragment.SystemSpaceFragment;
import org.openthos.filemanager.system.IFileInteractionListener;
import org.openthos.filemanager.system.Constants;
import org.openthos.filemanager.system.FileInfo;
import org.openthos.filemanager.system.FileOperationHelper;
import org.openthos.filemanager.fragment.SeafileFragment;
import org.openthos.filemanager.utils.SeafileUtils;
import org.openthos.filemanager.component.UsbPropertyDialog;
import org.openthos.filemanager.adapter.PathAdapter;
import org.openthos.filemanager.component.HorizontalListView;
import org.openthos.seafile.ISeafileService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.io.File;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.os.storage.ExternalStorageMountter;

import android.os.storage.StorageVolume;
import android.os.RemoteException;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final int ACTIVITY_MIN_COUNT_FOR_BACK = 3;
    private static final String USB_SPACE_FRAGMENT = "usb_space_fragment";
    private static final String MAIN_SP_TAG = "main_sp_tag";
    private static final String VIEW_TAG = "viewtag";
    private static final String VIEW_TAG_GRID = "grid";
    private static final String VIEW_TAG_LIST = "list";
    private static final String IV_SWITCH_VIEW = "iv_switch_view";
    public static final String SETTING_POPWINDOW_TAG = "iv_setting";
    public static final String COLLECTION_ITEM_TAG = "collection_item";
    public static final String USB_POPWINDOW_TAG = "iv_usb";
    public static final String MOUNT_POPWINDOW_TAG = "MOUNT_POPWINDOW_TAG";
    public static final String NAME_SPACE_RES_AUTO = "http://schemas.android.com/apk/res-auto";
    private TextView mTvComputer;
    private TextView mTvCloudService;
    private TextView mTvNetService;
    private TextView mTvAdd;
    private View mCurEventView;
    private ImageView mIvListView, mIvGridView, mIvForward, mIvSetting;
    public ImageView mIvUp, mIvBack;
    private EditText mEtNavigation, mEtSearchView;
    private ImageView mIvSearchView;
    private LinearLayout mLlUsb;

    private FragmentManager mManager = getSupportFragmentManager();
    private PopWinShare mPopWinShare;
    public Fragment mCurFragment;
    public SdStorageFragment mSdStorageFragment;
    public SeafileFragment mSeafileFragment;
    private SambaFragment mSambaFragment;
    private UsbConnectReceiver mReceiver;
    private boolean mIsMutiSelect;
    private SharedPreferences mSharedPreferences;
    private Editor mEditor;
    public boolean mIsSdStorageFragment;

    public Handler mHandler;
    private LeftTouchListener mLeftTouchListener;
    private LeftHoverListener mLeftHoverListener;
    private boolean mIsFirst = true;
    private HashMap<String, Integer> mLeftViewTagAndIdMap = new HashMap<>();
    private SearchOnKeyListener mSearchOnKeyListener;
    private CopyInfoDialog mCopyInfoDialog;
    private ProgressDialog mProgressDialog;
    private ProgressDialog mPopUpProgressDialog;
    public PersonalSpaceFragment mPersonalSpaceFragment;
    private SystemSpaceFragment mUsbStorageFragment;
    public BaseFragment mStartSearchFragment;
    private SearchFragment mSearchFragment;
    public String mCurPath;
    public ArrayList<SeafileLibrary> mLibrarys = new ArrayList<>();
    private CustomFileObserver mCustomFileObserver;
    private String mUsbPath;
    private static ContentResolver mContentResolver;
    private static Uri mUri;
    private static boolean mIsCtrlPress = false;
    private static boolean mIsShiftPress = false;
    private HorizontalListView mAddressListView;

    private String[] mPath;
    private String mClickPath;
    private List<String> mPathList = new ArrayList<>();
    private PathAdapter mPathAdapter;
    private AddressOnTouchListener mAddressTouchListener;
    public List<Fragment> mUserOperationFragments = new ArrayList<>();
    public int mFragmentIndex = -1;
    private ArrayList<Volume> mVolumes = new ArrayList<>();
    private HashMap<String, SystemSpaceFragment> mMountMap = new HashMap<>();
    private List<FolderBean> mFolderBeanList = new ArrayList<>();
    private Map<String, SystemSpaceFragment> mPathAndFragmentMap = new HashMap<>();
    private Map<View, String> mCollectedFolderViewAndPathMap = new HashMap<>();
    private List<SystemSpaceFragment> mDynamicFragments = new ArrayList<>();
    private ArrayList<String> mUsbLists = new ArrayList<>();
    private HashMap<String, SystemSpaceFragment> mUsbFragments = new HashMap<>();
    private LinearLayout mLlCollection;
    private LinearLayout mLlMount;
    private int mLeftIndex = 0;
    private List<View> mLeftViewList = new ArrayList<>();
    private List<View> mUsbViews = new ArrayList<>();
    public int mCurTabIndex = 0;
    private View mCurLeftItem, mPreTabView, mPreSelectedView;
    private EditTextTouchListener mEditTextTouchListener;
    private CloudInfoDialog mCloudInfoDialog;
    private FolderCollectionDialog mFolderCollectionDialog;
    private SeafileThread mSeafileThread;
    private SeafileServiceConnection mSeafileServiceConnection;
    public ISeafileService mISeafileService;

    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    private class SeafileThread extends Thread {

        @Override
        public void run() {
            super.run();
            synchronized (SeafileUtils.TAG) {
                try {
                    SeafileUtils.TAG.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    SeafileUtils.mUserId = mISeafileService.getUserName();
                    if (TextUtils.isEmpty(SeafileUtils.mUserId)) {
                        return;
                    }
                    String librarys = mISeafileService.getLibrary();
                    try {
                        JSONArray jsonArray = new JSONArray(librarys);
                        JSONObject jsonObject = null;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            SeafileLibrary seafileLibrary = new SeafileLibrary();
                            jsonObject = jsonArray.getJSONObject(i);
                            seafileLibrary.libraryName = jsonObject.getString("name");
                            seafileLibrary.libraryId = jsonObject.getString("id");
                            seafileLibrary.isSync = mISeafileService.isSync(
                                    seafileLibrary.libraryId, seafileLibrary.libraryName);
                            if (seafileLibrary.libraryName.
                                    equals(SeafileUtils.FILEMANAGER_SEAFILE_NAME)) {
                                mLibrarys.add(seafileLibrary);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (mCloudInfoDialog != null && mCloudInfoDialog.isShowing()) {
                    mCloudInfoDialog.refreshView();
                }
            }
        }
    }

    protected void initView() {
        ((FileManagerApplication) getApplication()).addActivity(this);
        mLlCollection = (LinearLayout) findViewById(R.id.ll_collection);
        mLlMount = (LinearLayout) findViewById(R.id.ll_mount);
        mSharedPreferences = getSharedPreferences(MAIN_SP_TAG, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        String viewTag = mSharedPreferences.getString(VIEW_TAG, VIEW_TAG_GRID);
        LocalCache.getInstance(MainActivity.this).setViewTag(viewTag);
        mTvAdd = (TextView) findViewById(R.id.tv_add);
        mTvComputer = (TextView) findViewById(R.id.tv_computer);
        mTvCloudService = (TextView) findViewById(R.id.tv_cloud_service);
        mTvNetService = (TextView) findViewById(R.id.tv_net_service);
        mIvListView = (ImageView) findViewById(R.id.iv_list_view);
        mIvGridView = (ImageView) findViewById(R.id.iv_grid_view);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mIvForward = (ImageView) findViewById(R.id.iv_forward);
        mIvUp = (ImageView) findViewById(R.id.iv_up);
        mIvSetting = (ImageView) findViewById(R.id.iv_setting);
        mEtNavigation = (EditText) findViewById(R.id.et_nivagation);
        mIvSearchView = (ImageView) findViewById(R.id.iv_search);
        mEtSearchView = (EditText) findViewById(R.id.search_view);
        mLlUsb = (LinearLayout) findViewById(R.id.ll_usb);
        mAddressListView = (HorizontalListView) findViewById(R.id.lv_address);
        if (LocalCache.getViewTag() != null && "list".equals(LocalCache.getViewTag())) {
            mIvGridView.setSelected(false);
            mIvListView.setSelected(true);
        } else {
            mIvGridView.setSelected(true);
            mIvListView.setSelected(false);
        }
        File file = new File(Constants.DOCUMENT_PATH);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdir();
        }

        mSeafileServiceConnection = new SeafileServiceConnection();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("org.openthos.seafile",
                "org.openthos.seafile.SeafileService"));
        bindService(intent, mSeafileServiceConnection, Context.BIND_AUTO_CREATE);
        mSeafileThread = new SeafileThread();
        mSeafileThread.start();
        mLeftViewTagAndIdMap.put(Constants.SDSTORAGEFRAGMENT_TAG, R.id.tv_computer);
        mLeftViewTagAndIdMap.put(Constants.SDSSYSTEMSPACE_TAG, R.id.tv_computer);
        mLeftViewTagAndIdMap.put(Constants.PERSONALSYSTEMSPACE_TAG, R.id.tv_computer);
        mLeftViewTagAndIdMap.put(Constants.SEAFILESYSTEMSPACE_TAG, R.id.tv_cloud_service);
        mLeftViewTagAndIdMap.put(Constants.USBFRAGMENT_TAG, R.id.tv_computer);
        mLeftViewTagAndIdMap.put(Constants.PERSONAL_TAG, R.id.tv_computer);
        mLeftViewTagAndIdMap.put(Constants.SAMBA_FRAGMENT_TAG, R.id.tv_net_service);
        mCopyInfoDialog = CopyInfoDialog.getInstance(MainActivity.this);
        mHandler = new Handler() {
            long mPreTime = 0L;

            @Override
            public void handleMessage(Message msg) {
                if (!Thread.currentThread().isInterrupted() && !isDestroyed()) {
                    switch (msg.what) {
                        case Constants.SET_CLIPBOARD_TEXT:
                            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                                    .setText((String) msg.obj);
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
                            mLlUsb.removeAllViews();
                            mUsbViews.clear();
                            for (int i = 0; i < mUsbLists.size(); i++) {
                                View v = getUsbView(mUsbLists.get(i));
                                mLlUsb.addView(v);
                                mUsbViews.add(v);
                            }
                            if (mProgressDialog != null) {
                                mProgressDialog.dismiss();
                            }
                            if (TextUtils.isEmpty(getCurPath())) {
                                mTvComputer.performClick();
                            }
                            break;
                        case Constants.USB_HIDE:
                            String usbPath = (String) msg.obj;
                            for (View tempUsbView : mUsbViews) {
                                if (tempUsbView.getTag().equals(usbPath)) {
                                    mLlUsb.removeView(tempUsbView);
                                    mUsbViews.remove(tempUsbView);
                                    break;
                                }
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
                            mSeafileFragment.setData(mLibrarys);
                            mSeafileFragment.getAdapter().notifyDataSetChanged();
                            break;
                        case Constants.REFRESH_BY_OBSERVER:
                            if (System.currentTimeMillis() - mPreTime >= 1000) {
                                mHandler.removeMessages(Constants.ONLY_REFRESH);
                                mHandler.sendMessage(Message.obtain(
                                        mHandler,
                                        Constants.ONLY_REFRESH,
                                        ((BaseFragment) getVisibleFragment())
                                                .mFileViewInteractionHub.getCurrentPath()));
                                mPreTime = System.currentTimeMillis();
                            } else {
                                mHandler.removeMessages(Constants.ONLY_REFRESH);
                                mHandler.sendMessageDelayed(Message.obtain(
                                        mHandler,
                                        Constants.ONLY_REFRESH,
                                        ((BaseFragment) getVisibleFragment())
                                                .mFileViewInteractionHub.getCurrentPath()), 1000);
                            }
                            break;
                        case Constants.REFRESH_HOME_UI:
                            if (mSdStorageFragment != null) {
                                mSdStorageFragment.refreshHomeUI();
                            }
                            break;
                    }
                }
                super.handleMessage(msg);
            }
        };
    }

    protected void initData() {
        checkFolder();
        getFoldersInfoFromXml();
        getMountData();
        initFragment();
        mLeftTouchListener = new LeftTouchListener();
        mLeftHoverListener = new LeftHoverListener();
        initLeftCollectionView();
        mContentResolver = getContentResolver();
        mUri = Uri.parse("content://org.openthos.filemanager/recycle");
        mAddressTouchListener = new AddressOnTouchListener();
        mPathAdapter = new PathAdapter(this, mPathList, mAddressTouchListener);
        mAddressListView.setAdapter(mPathAdapter);
        clickComputer();
        initUsb(Constants.USB_INIT);
        initLeftViewList();
        initFirstPage();
    }

    @Override
    protected void initListener() {
        mEditTextTouchListener = new EditTextTouchListener();
        for (int i = 0; i < mLeftViewList.size(); i++) {
            mLeftViewList.get(i).setOnTouchListener(mLeftTouchListener);
            mLeftViewList.get(i).setOnHoverListener(mLeftHoverListener);
        }
        mTvAdd.setOnClickListener(this);
        mIvListView.setOnClickListener(this);
        mIvGridView.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
        mIvUp.setOnClickListener(this);
        mIvForward.setOnClickListener(this);
        mIvSetting.setOnClickListener(this);
        mSearchOnKeyListener = new SearchOnKeyListener(mManager, MainActivity.this);
        mEtSearchView.setOnKeyListener(mSearchOnKeyListener);
        mIvSearchView.setOnClickListener(this);
        mEtSearchView.setOnTouchListener(mEditTextTouchListener);
        NavigationOnKeyListener navigationOnKeyListener = new NavigationOnKeyListener();
        mEtNavigation.setOnTouchListener(mEditTextTouchListener);
        mEtNavigation.setOnKeyListener(navigationOnKeyListener);
        TextChangeListener textChangeListener = new TextChangeListener();
        mEtNavigation.addTextChangedListener(textChangeListener);
        mEtNavigation.setOnFocusChangeListener(new AddressOnFocusChangeListener());
        mAddressListView.setOnTouchListener(mAddressTouchListener);
        mReceiver = new UsbConnectReceiver(this);
    }

    private void getFoldersInfoFromXml() {
        XmlResourceParser parser = getResources().getXml(R.xml.personal_space_folders);
        try {
            FolderBean bean;
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.getAttributeCount() > 0) {
                    int pathRes = parser.getAttributeResourceValue(
                            NAME_SPACE_RES_AUTO, "pathRes", -1);
                    String path = Constants.SDCARD_PATH + getResources().getString(pathRes);
                    if (new File(path).exists()) {
                        bean = new FolderBean();
                        bean.setPath(path);
                        bean.setTitle(getResources().getString(parser.getAttributeResourceValue(
                                NAME_SPACE_RES_AUTO, "titleRes", -1)));
                        bean.setIsSystemFolder(parser.getAttributeBooleanValue(
                                NAME_SPACE_RES_AUTO, "isSystemFolder", false));
                        bean.setIconRes(parser.getAttributeResourceValue(
                                NAME_SPACE_RES_AUTO, "iconRes", -1));
                        bean.setSmallIconRes(parser.getAttributeResourceValue(
                                NAME_SPACE_RES_AUTO, "smallIconRes", -1));
                        bean.setIsCollected(
                                mSharedPreferences.getBoolean(bean.getPath(), false));
                        mFolderBeanList.add(bean);
                    }
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(mFolderBeanList, new FolderBeanComparator());
    }

    private void initFragment() {
        FragmentTransaction transaction = mManager.beginTransaction();
        mSdStorageFragment = new SdStorageFragment(mManager, MainActivity.this);
        transaction.add(R.id.fl_mian, mSdStorageFragment, Constants.SDSTORAGEFRAGMENT_TAG)
                .hide(mSdStorageFragment);
        mPersonalSpaceFragment = new PersonalSpaceFragment();
        transaction.add(R.id.fl_mian, mPersonalSpaceFragment, Constants.PERSONAL_TAG)
                .hide(mPersonalSpaceFragment);
        mSeafileFragment = new SeafileFragment();
        transaction.add(R.id.fl_mian, mSeafileFragment, Constants.SEAFILESYSTEMSPACE_TAG)
                .hide(mSeafileFragment);
        mSambaFragment = new SambaFragment();
        transaction.add(R.id.fl_mian, mSambaFragment, Constants.SAMBA_FRAGMENT_TAG)
                .hide(mSambaFragment);
        for (int i = 0; i < mVolumes.size(); i++) {
            Volume v = mVolumes.get(i);
            View inflate = View.inflate(this, R.layout.mount_list, null);
            TextView name = (TextView) inflate.findViewById(R.id.usb_list_usb_name);
            name.setText(v.getBlock());
            inflate.setOnHoverListener(mLeftHoverListener);
            inflate.setOnTouchListener(mLeftTouchListener);
            inflate.setTag(v);
            SystemSpaceFragment mountFragment = new SystemSpaceFragment(
                    v.getBlock(), "/storage/disk" + i, null, true);
            mMountMap.put(v.getBlock(), mountFragment);
            transaction.add(R.id.fl_mian, mMountMap.get(v.getBlock()),
                    v.getBlock()).hide(mMountMap.get(v.getBlock())).commitAllowingStateLoss();
            v.setPath("/storage/disk" + i);
            mLlMount.addView(inflate);
            mCurTabIndex = -1;
            mDynamicFragments.add(mountFragment);
        }
        transaction.commitAllowingStateLoss();
    }

    private void initLeftCollectionView() {
        FolderBean bean;
        for (int i = 0; i < mFolderBeanList.size(); i++) {
            bean = mFolderBeanList.get(i);
            if (bean.isCollected()) {
                View v = getLeftViewByFolderBean(bean);
                mLlCollection.addView(
                        v, mLlCollection.getChildCount() - 1, mTvAdd.getLayoutParams());
                mCollectedFolderViewAndPathMap.put(v, bean.getPath());
                mLeftViewTagAndIdMap.put(bean.getPath(), v.getId());
            }
        }
    }

    private void initLeftViewList() {
        for (int i = 0; i < mLlCollection.getChildCount(); i++) {
            mLeftViewList.add(mLlCollection.getChildAt(i));
        }
        mLeftViewList.add(mTvComputer);
        for (int i = 0; i < mLlMount.getChildCount(); i++) {
            mLeftViewList.add(mLlMount.getChildAt(i));
        }
        mLeftViewList.add(mTvCloudService);
        mLeftViewList.add(mTvNetService);
    }

    private void showSdSFragmentAfterInstallUSB() {
        mManager.beginTransaction().remove(mCurFragment).show(mSdStorageFragment)
                .commitAllowingStateLoss();
        mCurFragment = mSdStorageFragment;
    }

    private void initUsb(int flags) {
        switch (flags) {
            case Constants.USB_INIT:
                if (TextUtils.isEmpty(getCurPath())) {
                    mManager.beginTransaction().remove(mSdStorageFragment).commitAllowingStateLoss();
                    mManager.beginTransaction().hide(mCurFragment).commitAllowingStateLoss();
                    mSdStorageFragment = new SdStorageFragment(mManager, MainActivity.this);
                    setSelectedBackground(R.id.tv_computer);
                    mManager.beginTransaction().add(R.id.fl_mian, mSdStorageFragment,
                            Constants.SDSTORAGEFRAGMENT_TAG).show(mSdStorageFragment)
                            .commitAllowingStateLoss();
                    mCurFragment = mSdStorageFragment;
                } else {
                    BaseFragment visibleFragment = (BaseFragment) getVisibleFragment();
                    mManager.beginTransaction().remove(mSdStorageFragment).commitAllowingStateLoss();
                    mSdStorageFragment = new SdStorageFragment(mManager, MainActivity.this);
                    mManager.beginTransaction().add(R.id.fl_mian, mSdStorageFragment,
                            Constants.SDSTORAGEFRAGMENT_TAG).hide(mSdStorageFragment)
                            .commitAllowingStateLoss();
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
                    int position;
                    for (int i = 0; i < mUsbLists.size(); i++) {
                        if (mUsbPath.equals(mUsbLists.get(i))) {
                            position = getUsbPosition(mUsbPath);
                            View v = mLlUsb.getChildAt(getUsbPosition(mUsbPath));
                            mUsbViews.remove(v);
                            mLlUsb.removeView(v);
                            mSdStorageFragment.removeUsbView(position);
                            break;
                        }
                    }

                    if (mUsbPath.indexOf("/storage/") != -1) {
                        for (int i = mUsbLists.size() - 1; i >= 0; i--) {
                            if ((mUsbLists.get(i)).indexOf(mUsbPath + "_") != -1) {
                                position = getUsbPosition(mUsbLists.get(i));
                                mLlUsb.removeViewAt(getUsbPosition(mUsbLists.get(i)));
                                mSdStorageFragment.removeUsbView(position);
                            }
                        }
                    }

                }

                if (getCurPath() != null && getCurPath().startsWith(mUsbPath)) {
                    showSdSFragmentAfterInstallUSB();
                    setCurPath(null);
                    setNavigationPath(getCurPath());
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

    private void getMountData() {
        String data = getSharedPreferences("automount", Context.MODE_PRIVATE)
                .getString("automount", "ERROR");
        if (data.equals("ERROR")) {
            data = BootCompleteReceiver.refreshAutoMountData(this);
            getSharedPreferences("automount", Context.MODE_PRIVATE)
                    .edit().putString("automount", data).commit();
        }
        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                Volume v = new Volume();
                v.setBlock(object.getString("block"));
                v.setIsMount(object.getBoolean("ismount"));
                v.setType(object.getString("type"));
                mVolumes.add(v);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<File> mDafaultFileList = new ArrayList<>();

    private void checkFolder() {
        if (mDafaultFileList.size() == 0) {
            mDafaultFileList.add(new File(Constants.DESKTOP_PATH));
            mDafaultFileList.add(new File(Constants.MUSIC_PATH));
            mDafaultFileList.add(new File(Constants.VIDEOS_PATH));
            mDafaultFileList.add(new File(Constants.PICTURES_PATH));
            mDafaultFileList.add(new File(Constants.DOCUMENT_PATH));
            mDafaultFileList.add(new File(Constants.DOWNLOAD_PATH));
            mDafaultFileList.add(new File(Constants.RECYCLE_PATH));
        }
        for (File f : mDafaultFileList) {
            if (!f.exists() && !f.isDirectory()) {
                f.mkdirs();
            }
        }
    }

    protected void initFirstPage() {
        String path = getIntent().getStringExtra(Constants.PATH_TAG);
        if (path != null) {
            showSpaceFragment(path);
        }
        setCurPath(path);
    }

    class NavigationOnKeyListener implements View.OnKeyListener {
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

    protected void showSpaceFragment(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        if (path.startsWith(getString(R.string.path_sd_eng))) {
            path = path.replaceAll(getString(R.string.path_sd_eng), Util.getSdDirectory());
        } else if (!path.startsWith(Constants.ROOT_PATH)) {
            path = Constants.ROOT_PATH + path;
        }
        File file = new File(path);
        try {
            if (file.getCanonicalPath().startsWith(new File("sdcard").getCanonicalPath())) {
                path = file.getAbsolutePath();
            } else {
                path = file.getCanonicalPath();
            }
            if (!Build.TYPE.equals("eng")
                    && !(path.startsWith(Constants.USER_PERMISSION_PATH))) {
                Toast.makeText(this, "" + getResources().getString(R.string.have_no_permission),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file.exists()) {
            FragmentTransaction transaction = mManager.beginTransaction();
            transaction.hide(mCurFragment);
            SystemSpaceFragment fragment = mPathAndFragmentMap.get(path);
            if (fragment == null) {
                fragment = new SystemSpaceFragment(Constants.LEFT_FAVORITES, path,
                        null, mLeftViewTagAndIdMap.containsKey(path));
                transaction.add(R.id.fl_mian, fragment, path).commitAllowingStateLoss();
                mPathAndFragmentMap.put(path, fragment);
            } else {
                transaction.show(fragment).commitAllowingStateLoss();
            }
            setFileInfo(getLeftViewIdByTag(path), path, fragment);
            //transaction.show(fragment).addToBackStack(null).commitAllowingStateLoss();
            setNavigationPath(Util.getDisplayPath(this, path));
            mCurTabIndex = 9;
        } else {
            Toast.makeText(this, "" + getResources().getString(R.string.address_search_false),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public int getLeftViewIdByTag(String tag) {
        Integer id = mLeftViewTagAndIdMap.get(tag);
        if(id == null){
            id = R.id.tv_computer;
        }
        return id;
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

    public void processTab(View v) {
        disSelectPreView();
        v.setBackgroundColor(0x68ffffff);
        v.setFocusable(true);
        v.requestFocus();
        mPreTabView = v;
    }

    public void disSelectPreView() {
        if (mPreTabView != null) {
            mPreTabView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void switchTab() {
        switch (mCurTabIndex) {
            case 0:
                processTab(mIvBack);
                break;
            case 1:
                processTab(mIvForward);
                break;
            case 2:
                processTab(mIvUp);
                break;
            case 3:
                processTab(mIvSetting);
                break;
            case 4:
                processTab(mIvGridView);
                break;
            case 5:
                processTab(mIvListView);
                break;
            case 6:
                mAddressListView.setVisibility(View.GONE);
                mEtNavigation.setVisibility(View.VISIBLE);
                processTab(mEtNavigation);
                mEtNavigation.setSelection(mEtNavigation.getText().length());
                mPreTabView = null;
                break;
            case 7:
                processTab(mEtSearchView);
                mPreTabView = null;
                break;
            case 8:
                mCurLeftItem = mLlCollection.getChildAt(0);
                processTab(mCurLeftItem);
                break;
            case 9:
                if (mCurFragment instanceof SdStorageFragment) {
                    if (mSdStorageFragment.mCurView != null) {
                        mSdStorageFragment.mCurView.setSelected(false);
                    }
                    mLlCollection.getChildAt(0).setBackgroundColor(Color.TRANSPARENT);
                    mSdStorageFragment.mPersonalSpace.requestFocus();
                    mSdStorageFragment.mPersonalSpace.setSelected(true);
                    mSdStorageFragment.mCurView = mSdStorageFragment.mPersonalSpace;
                } else if (mCurFragment instanceof SystemSpaceFragment) {
                    SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) this.mCurFragment;
                    FileListAdapter adapter = systemSpaceFragment.getAdapter();
                    List integerList = adapter.getSelectFileInfoList();
                    integerList.clear();
                    systemSpaceFragment.mFileViewInteractionHub.clearSelection();
                    if (adapter.getFileInfoList().size() != 0) {
                        FileInfo fileInfo = adapter.getFileInfoList().get(0);
                        fileInfo.Selected = true;
                        integerList.add(0);
                        systemSpaceFragment.mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
                        systemSpaceFragment.mPos = 0;
                        systemSpaceFragment.onDataChanged();
                    }

                }
                break;
        }
    }

    public void processLeftDirectionKey(int keyCode) {
        if (mCurLeftItem != null) {
            mCurLeftItem.setSelected(false);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP:
                mLeftIndex = mLeftIndex > 0 ? --mLeftIndex : mLeftIndex;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mLeftIndex = mLeftIndex < mLeftViewList.size() - 1 ? ++mLeftIndex : mLeftIndex;
                break;
        }
        mCurLeftItem = mLeftViewList.get(mLeftIndex);
        processTab(mCurLeftItem);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                && !(mEtNavigation.isFocused() || mEtSearchView.isFocused())) {
            if (mCurTabIndex == 8) {
                if (mLeftIndex > mLlCollection.getChildCount() - 1) {
                    if (mUsbViews.size() != 0) {
                        mLeftViewList.addAll(
                                mLeftViewList.size() - mLlMount.getChildCount() - 2, mUsbViews);
                    }
                }
                processLeftDirectionKey(keyCode);
                mLeftViewList.removeAll(mUsbViews);
            } else if (mCurTabIndex == -1 || mCurTabIndex > 8) {
                ((BaseFragment) mCurFragment).processDirectionKey(keyCode);
            }
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_DEL)
                && !mEtSearchView.hasFocus() && !mEtNavigation.isFocused()) {
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
            if (isCopyByHot()) {
                return false;
            }
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
                        ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub
                                .getCurrentPath()));
            }
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            if (mEtNavigation.isFocused() || mEtSearchView.isFocused()) {
                return false;
            }
            if (mCurTabIndex == 8) {
                leftEnter(mCurLeftItem);
            } else {
                if (isRecycle()) {
                    Toast.makeText(this, getString(R.string.fail_open_recycle),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (getVisibleFragment() instanceof BaseFragment) {
                    ((BaseFragment) getVisibleFragment()).enter();
                }
            }
        }
        return false;
    }

    public void leftEnter(View view) {
        disSelectPreView();
        switch (view.getId()) {
            case R.id.tv_collected:
                showSpaceFragment(view.getTag(R.id.left_view_path_tag).toString());
                break;
            case R.id.tv_computer:
                clickComputer();
                break;
            case R.id.tv_cloud_service:
                if (!SeafileUtils.isNetworkOn(this)) {
                    T.showShort(this, getResources().getString(R.string.network_down));
                }
                setFileInfo(R.id.tv_cloud_service, "", mSeafileFragment);
                break;
            case R.id.tv_net_service:
                setFileInfo(R.id.tv_net_service, "", mSambaFragment);
                break;
            case R.id.usb:
                mUsbPath = (String) view.getTag();
                enter(mUsbPath);
                break;
            case R.id.mount:
                Volume v = (Volume) view.getTag();
                if (!v.isMount()) {
                    mountVolume(v);
                }
                enter(v);
                break;
        }
    }

    private boolean isCopyByHot() {
        return getVisibleFragment() instanceof PersonalSpaceFragment
                || getVisibleFragment() instanceof SdStorageFragment
                || getVisibleFragment() instanceof SeafileFragment
                || getVisibleFragment() instanceof SambaFragment
                || mEtNavigation.isFocused() || mEtSearchView.isFocused();
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
        new Thread() {
            @Override
            public void run() {
                super.run();
                ArrayList<FileInfo> list = ((BaseFragment) getVisibleFragment())
                        .mFileViewInteractionHub.getSelectedFileList();
                StringBuffer stringBuffer = new StringBuffer();
                if (!list.isEmpty()) {
                    for (int i = 0; i < list.size(); i++) {
                        stringBuffer.append(Constants.EXTRA_CROP_FILE_HEADER + list.get(i).filePath);
                    }
                    mHandler.sendMessage(Message.obtain(mHandler,
                            Constants.SET_CLIPBOARD_TEXT, stringBuffer.toString()));
                }
            }
        }.start();
    }

    public void paste() {
        String sourcePath = "";
        String destPath =
                ((BaseFragment) getVisibleFragment()).mFileViewInteractionHub.getCurrentPath();
        try {
            sourcePath = (String) ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                    .getText();
        } catch (ClassCastException e) {
            sourcePath = "";
        }
        if (sourcePath == null) {
            sourcePath = "";
        }
        if (!TextUtils.isEmpty(sourcePath) && sourcePath.startsWith(Constants.EXTRA_FILE_HEADER)) {
            new CopyThread(sourcePath, destPath).start();
        } else if (!TextUtils.isEmpty(sourcePath)
                && sourcePath.startsWith(Constants.EXTRA_CROP_FILE_HEADER)) {
            new CropThread(sourcePath, destPath).start();
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
                && sourcePath.startsWith(Constants.EXTRA_CROP_FILE_HEADER)) {
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setText("");
        }
    }

    class CopyThread extends Thread {
        String[] mSrcCopyPaths;
        String mDestPath;

        public CopyThread(String srcPaths, String destPath) {
            super();
            mSrcCopyPaths = srcPaths.split(Constants.EXTRA_FILE_HEADER);
            mDestPath = destPath;
        }

        @Override
        public void run() {
            super.run();
            for (int i = 1; i < mSrcCopyPaths.length; i++) {
                FileOperationHelper.CopyFile(MainActivity.this,
                        mSrcCopyPaths[i].replace(Constants.EXTRA_FILE_HEADER, ""), mDestPath);
            }
        }
    }

    class CropThread extends Thread {
        String[] mSrcCropPaths;
        String mDestPath;

        public CropThread(String srcPaths, String destPath) {
            super();
            mSrcCropPaths = srcPaths.split(Constants.EXTRA_CROP_FILE_HEADER);
            mDestPath = destPath;
        }

        @Override
        public void run() {
            super.run();
            for (int i = 1; i < mSrcCropPaths.length; i++) {
                FileOperationHelper.MoveFile(MainActivity.this,
                        mSrcCropPaths[i].replace(Constants.EXTRA_CROP_FILE_HEADER, ""), mDestPath, true);
            }
        }
    }

    public void copy() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                ArrayList<FileInfo> list = ((BaseFragment) getVisibleFragment())
                        .mFileViewInteractionHub.getSelectedFileList();
                StringBuffer stringBuffer = new StringBuffer();
                if (!list.isEmpty()) {
                    for (int i = 0; i < list.size(); i++) {
                        stringBuffer.append(Constants.EXTRA_FILE_HEADER + list.get(i).filePath);
                    }
                    mHandler.sendMessage(Message.obtain(mHandler, Constants.SET_CLIPBOARD_TEXT,
                            stringBuffer.toString()));
                }
            }
        }.start();
    }

    private void processUserOperation() {
        mManager.beginTransaction().hide(mCurFragment)
                .show(mUserOperationFragments.get(mFragmentIndex)).commitAllowingStateLoss();
        mCurFragment = mUserOperationFragments.get(mFragmentIndex);
        if (mCurFragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment mCurFragment = (SystemSpaceFragment) this.mCurFragment;
            String currentPath = mCurFragment.getCurrentPath();
            setCurPath(currentPath);
            setNavigationPath(currentPath);
            String tag = mCurFragment.getTag();
            if (tag != null) {
                setSelectedBackground(getLeftViewIdByTag(tag));
            }
        }
        if (mCurFragment == mSdStorageFragment) {
            mSdStorageFragment.setUnselectAll();
            setCurPath(null);
            setNavigationPath(null);
        }
    }

    public void onBackward() {
        if (mFragmentIndex < mUserOperationFragments.size()) {
            if (mCurFragment == mSdStorageFragment) {
                mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_disable));
                if (mFragmentIndex > 0) {
                    mIvForward.setImageDrawable(getDrawable(R.mipmap.forward_enable));
                }
            } else {
                mIvForward.setImageDrawable(getDrawable(R.mipmap.forward_enable));
                mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_enable));
            }

            if (mFragmentIndex > 1) {
                mFragmentIndex--;
                processUserOperation();
            } else {
                returnToRootDir();
                mFragmentIndex = 0;
                mIvBack.setImageDrawable(getDrawable(R.mipmap.backward_disable));
            }
        }
    }

    public void onForward() {
        if (mFragmentIndex >= 0 && mFragmentIndex < mUserOperationFragments.size() - 1) {
            mFragmentIndex++;
            processUserOperation();
        } else {
            mIvForward.setImageDrawable(getDrawable(R.mipmap.forward_disable));
        }

        if (mCurFragment == mSdStorageFragment) {
            mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_disable));
        } else {
            mIvBack.setImageDrawable(getDrawable(R.mipmap.backward_enable));
            mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_enable));
        }

        if (mFragmentIndex == mUserOperationFragments.size() - 1) {
            mIvForward.setImageDrawable(getDrawable(R.mipmap.forward_disable));
        }
    }

    public void onUp() {
        onBackPressed();
    }

    @Override
    public void onClick(View view) {
        clearNivagateFocus();
        switch (view.getId()) {
            case R.id.iv_back:
                mCurTabIndex = 0;
                onBackward();
                break;
            case R.id.iv_up:
                mCurTabIndex = 2;
                onUp();
                break;
            case R.id.iv_forward:
                mCurTabIndex = 1;
                onForward();
                break;
            case R.id.iv_setting:
                mCurTabIndex = 3;
                showPopWindow(SETTING_POPWINDOW_TAG);
                break;
            case R.id.iv_grid_view:
                mCurTabIndex = 4;
                mIvGridView.setSelected(true);
                mIvListView.setSelected(false);
                LocalCache.setViewTag(VIEW_TAG_GRID);
                sendBroadcastMessage(IV_SWITCH_VIEW, VIEW_TAG_GRID, false);
                mEditor.putString(VIEW_TAG, VIEW_TAG_GRID);
                mEditor.commit();
                break;
            case R.id.iv_list_view:
                mCurTabIndex = 5;
                mIvGridView.setSelected(false);
                mIvListView.setSelected(true);
                LocalCache.setViewTag(VIEW_TAG_LIST);
                sendBroadcastMessage(IV_SWITCH_VIEW, VIEW_TAG_LIST, false);
                mEditor.putString(VIEW_TAG, VIEW_TAG_LIST);
                mEditor.commit();
                break;
            case R.id.iv_search:
                mEtSearchView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_ENTER));
                break;
            case R.id.tv_add:
                showFolderCollectionDialog();
                break;
        }
        disSelectPreView();
    }

    public void uninstallUSB(String usbPath) {
        //if (usbPath.indexOf("/storage/usb") != -1 && usbPath.indexOf("_") != -1) {
        //    usbPath = usbPath.substring(0, 13);
        //}

        if (mPopUpProgressDialog == null) {
            mPopUpProgressDialog = new ProgressDialog(this);
        }
        mPopUpProgressDialog.setMessage(getString(R.string.usb_sync));
        mPopUpProgressDialog.setIndeterminate(true);
        mPopUpProgressDialog.setCancelable(true);
        mPopUpProgressDialog.setCanceledOnTouchOutside(true);
        mPopUpProgressDialog.show();
        new UninstallUsbThread(usbPath).start();
    }

    private class UninstallUsbThread extends Thread {
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
                mPopUpProgressDialog.dismiss();
                if (usbPath.indexOf("/storage/usb") != -1 && usbPath.indexOf("_") != -1) {
                    usbPath = usbPath.substring(0, 13);
                }
                try {
                    Intent umountIntent = new Intent(ExternalStorageMountter.UMOUNT_ONLY);
                    umountIntent.setComponent(ExternalStorageMountter.COMPONENT_NAME);
                    StorageVolume[] vols = getMountService().getVolumeList(
                            MainActivity.this.getUserId(), ActivityThread.currentPackageName(), 0);
                    StorageVolume vol = null;
                    for (StorageVolume i : vols) {
                        if (i.getPath().equals(usbPath)) {
                            vol = i;
                            break;
                        }
                    }
                    if (vol != null) {
                        umountIntent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, vol);
                        startService(umountIntent);
                    }
                } catch (RemoteException e) {
                }
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
            }
        }
        return mountService;
    }

    private void setFileInfo(int id, String path, Fragment fragment) {
        if (fragment != mSdStorageFragment) {
            mIvBack.setImageDrawable(getDrawable(R.mipmap.backward_enable));
            mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_enable));
        }
        if (fragment instanceof SystemSpaceFragment) {
            SystemSpaceFragment systemSpaceFragment = (SystemSpaceFragment) fragment;
            systemSpaceFragment.setPath(path);
            FileListAdapter adapter = systemSpaceFragment.getAdapter();
            if (adapter != null) {
                adapter.getSelectFileInfoList().clear();
                systemSpaceFragment.getFileViewInteractionHub().clearSelection();
            }
        }
        setCurPath(path);
        FragmentTransaction transaction = mManager.beginTransaction();
        if (mCurFragment != null) {
            transaction.hide(mCurFragment);
        }
        if (fragment != null) {
            transaction.show(fragment);
        }
        mCurFragment = fragment;
        transaction.commitAllowingStateLoss();
        setSelectedBackground(id);
        mUserOperationFragments.add(mCurFragment);
        mFragmentIndex++;
    }

    protected void setSelectedBackground(int id) {
        switch (id) {
            case R.id.tv_collected:
                for (int i = 0; i < mLlCollection.getChildCount(); i++) {
                    if (mCurFragment.getTag().equals(
                            mLlCollection.getChildAt(i).getTag(R.id.left_view_path_tag))) {
                        setSelectView(mLlCollection.getChildAt(i));
                    }
                }
                break;
            case R.id.tv_computer:
                setSelectView(mTvComputer);
                break;
            case R.id.tv_cloud_service:
                setSelectView(mTvCloudService);
                break;
            case R.id.tv_net_service:
                setSelectView(mTvNetService);
                break;
            case R.id.mount:
                for (int i = 0; i < mDynamicFragments.size(); i++) {
                    SystemSpaceFragment systemSpaceFragment = mDynamicFragments.get(i);
                    if (mCurFragment == systemSpaceFragment) {
                        setSelectView(mLlMount.getChildAt(i));
                    }
                }
                break;
            case R.id.usb:
                for (int i = 0; i < mLlUsb.getChildCount(); i++) {
                    if (mCurFragment.getTag().equals(mLlUsb.getChildAt(i).getTag())) {
                        setSelectView(mLlUsb.getChildAt(i));
                    }
                }
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
        View.OnClickListener onPopItemClickListener = null;
        View anchorView = null;
        switch (menu_tag) {
            case SETTING_POPWINDOW_TAG:
                onPopItemClickListener = new PopOnClickLintener(
                        menu_tag, MainActivity.this, mManager);
                anchorView = mIvSetting;
                break;
            case COLLECTION_ITEM_TAG:
                onPopItemClickListener = new PopOnClickLintener(
                        menu_tag, MainActivity.this, mManager);
                anchorView = mCurEventView;
                break;
            case USB_POPWINDOW_TAG:
                onPopItemClickListener = new usbListener();
                anchorView = mLlUsb.getChildAt(getUsbPosition(mUsbPath));
                break;
        }
        mPopWinShare = new PopWinShare(MainActivity.this, onPopItemClickListener,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                menu_tag);
        mPopWinShare.showAsDropDown(anchorView);
    }

    private void showPopWindow(Volume volume, View view) {
        mPopWinShare = new PopWinShare(MainActivity.this, new MountListener(volume),
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                MOUNT_POPWINDOW_TAG);
        //mPopWinShare.setWindowLayoutType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        mPopWinShare.showAsDropDown(view);
    }

    public void dismissPopwindow() {
        mPopWinShare.dismiss();
    }

    @Override
    public void onBackPressed() {
        mManager.findFragmentById(R.id.fl_mian);
        if (mCurFragment != mSdStorageFragment) {
            if (mCurFragment instanceof SystemSpaceFragment) {
                SystemSpaceFragment sdCurFragment = (SystemSpaceFragment) mCurFragment;
                String currentPath = sdCurFragment.getCurrentPath();
                setCurPath(currentPath);
                setNavigationPath(currentPath);
                String tag = mCurFragment.getTag();
                if (tag == null) {
                    returnToRootDir();
                }
                if (tag.equals(Constants.PERSONALSYSTEMSPACE_TAG)) {
                    if (mPersonalSpaceFragment.canGoBack()) {
                        mPersonalSpaceFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() >= ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToPersonalDir();
                    }
                } else if (tag.equals(Constants.SEAFILESYSTEMSPACE_TAG)) {
                    if (mSeafileFragment.canGoBack()) {
                        mSeafileFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() >= ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToSeafileDir();
                    }
                } else if (tag.equals(Constants.SDSSYSTEMSPACE_TAG)) {
                    if (mSdStorageFragment.canGoBack()) {
                        mSdStorageFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() >= ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToRootDir();
                    }
                } else if (tag.equals(Constants.USBFRAGMENT_TAG)) {
                    if (mUsbStorageFragment.canGoBack()) {
                        mUsbStorageFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() >= ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToRootDir();
                    }
                } else if (tag.equals(Constants.SEARCHSYSTEMSPACE_TAG)) {
                    SystemSpaceFragment searchSysFragment = (SystemSpaceFragment) mCurFragment;
                    if (searchSysFragment.canGoBack()) {
                        searchSysFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToSearchFragment();
                    }
                } else if (tag.equals(Constants.ADDRESSFRAGMENT_TAG)) {
                    SystemSpaceFragment addressFragment = (SystemSpaceFragment) mCurFragment;
                    if (addressFragment.canGoBack()) {
                        addressFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToRootDir();
                    }
                } else if (tag.equals(Constants.SAMBA_FRAGMENT_TAG)) {
                    SystemSpaceFragment sambaFragment = (SystemSpaceFragment) mCurFragment;
                    if (sambaFragment.canGoBack()) {
                        sambaFragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToSambaDir();
                    }
                } else if (mMountMap.containsKey(tag)) {
                    SystemSpaceFragment dynamicfragment = (SystemSpaceFragment) mCurFragment;
                    if (dynamicfragment.canGoBack()) {
                        dynamicfragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else {
                        returnToRootDir();
                    }
                } else if (mPathAndFragmentMap.get(tag) != null) {
                    SystemSpaceFragment fragment = (SystemSpaceFragment) mCurFragment;
                    if (fragment.canGoBack()) {
                        fragment.goBack();
                    } else if (mManager.getBackStackEntryCount() > ACTIVITY_MIN_COUNT_FOR_BACK) {
                        mManager.popBackStack();
                    } else if (mManager.getBackStackEntryCount() == ACTIVITY_MIN_COUNT_FOR_BACK) {
                        showSpaceFragment(tag);
                    } else {
                        returnToRootDir();
                    }
                }
            } else if (mStartSearchFragment != null && mCurFragment instanceof SearchFragment) {
                mManager.beginTransaction().hide(mCurFragment).show(mStartSearchFragment)
                        .commitAllowingStateLoss();
                mCurFragment = mStartSearchFragment;
                mStartSearchFragment = null;
            } else if (mCurFragment instanceof SambaFragment
                    && !((SambaFragment) mCurFragment).canGoBack()) {
                ((SambaFragment) mCurFragment).goBack();
            } else {
                returnToRootDir();
            }
        }

        if (mCurFragment == mSdStorageFragment) {
            mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_disable));
        }
    }

    public Fragment getVisibleFragment() {
        List<Fragment> fragments = mManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible()) {
                return fragment;
            }
        }
        android.util.Log.i("newbee", android.util.Log.getStackTraceString(new Throwable()));
        String path = mEtNavigation.getText().toString();
        if (TextUtils.isEmpty(path.trim())) {
            showSpaceFragment("~");
        } else {
            for (int i = 0; i < path.length(); i++) {
                if (path.charAt(i) != ' ') {
                    showSpaceFragment(path.substring(i, path.length()));
                    break;
                }
            }
        }
        return getVisibleFragment();
    }

    private void returnToSearchFragment() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        mSearchFragment = (SearchFragment) mManager
                .findFragmentByTag(Constants.SEARCHFRAGMENT_TAG);
        fragmentTransaction.show(mSearchFragment);
        fragmentTransaction.commitAllowingStateLoss();
        mCurFragment = mSearchFragment;
    }

    private void returnToCloudDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mSeafileFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setNavigationPath(getResources().getString(R.string.cloud));
        setSelectedBackground(R.id.tv_computer);
        mCurFragment = mSeafileFragment;
    }

    private void returnToPersonalDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mPersonalSpaceFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setNavigationPath(null);
        setSelectedBackground(R.id.tv_computer);
        mCurFragment = mPersonalSpaceFragment;
    }

    public void returnToRootDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(mCurFragment);
        fragmentTransaction.show(mSdStorageFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setCurPath(null);
        setNavigationPath(null);
        setSelectedBackground(R.id.tv_computer);
        mSdStorageFragment.setSelectedCardBg(Constants.RETURN_TO_WHITE);
        mCurFragment = mSdStorageFragment;
        mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_disable));
    }

    private void returnToSeafileDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mSeafileFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setNavigationPath(null);
        setSelectedBackground(R.id.tv_cloud_service);
        mCurFragment = mSeafileFragment;
    }

    private void returnToSambaDir() {
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        fragmentTransaction.hide(getVisibleFragment());
        fragmentTransaction.show(mSambaFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setNavigationPath(null);
        setSelectedBackground(R.id.tv_net_service);
        mCurFragment = mSambaFragment;
    }

    public interface IBackPressedListener {
    }

    @Override
    protected void onDestroy() {
        android.util.Log.i("wwwwww", getComponentName().getClassName() + " Destory");
        ((FileManagerApplication) getApplication()).removeActivity(this);
        super.onDestroy();
    }

    boolean isRestart = false;

    @Override
    protected void onPause() {
        android.util.Log.i("wwwwww", getComponentName().getClassName() + " Pause");
        isRestart = true;
        super.onPause();
    }

    @Override
    protected void onStop() {
        mReceiver.unregisterReceiver();
        android.util.Log.i("wwwwww", getComponentName().getClassName() + " Stop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        android.util.Log.i("wwwwww", getComponentName().getClassName() + " Resume");
        super.onResume();
    }

    @Override
    public void onStart() {
        mReceiver.registerReceiver();
        android.util.Log.i("wwwwww", getComponentName().getClassName() + " Start");
        super.onStart();
    }

    @Override
    public void onRestart() {
        android.util.Log.i("wwwwww", getComponentName().getClassName() + " ReStart");
        super.onRestart();
    }

    public void setNavigationBar(String displayPath) {
        setNavigationPath(displayPath);
    }

    @Override
    protected void setMode() {
        mMode = Mode.VIEW;
    }

    public void setNavigationPath(String displayPath) {
        mEtNavigation.setText(displayPath);
        mPath = null;
        mPathList.clear();
        if (displayPath == null || displayPath.equals("")) {
            mAddressListView.setVisibility(View.GONE);
            mEtNavigation.setVisibility(View.VISIBLE);
        } else {
            updateAddressButton(displayPath);
            mAddressListView.setVisibility(View.VISIBLE);
            mEtNavigation.setVisibility(View.GONE);
        }
        mPathAdapter.notifyDataSetChanged();
    }

    private void updateAddressButton(String displayPath) {
        if (displayPath.equals(Constants.ROOT_PATH)) {
            mPath = new String[]{Constants.ROOT_PATH};
            mPathList.add(Constants.ROOT_PATH);
        } else {
            mPath = displayPath.split(Constants.ROOT_PATH);
            for (String s : mPath) {
                mPathList.add(s);
            }
        }
        if (!mPathList.get(0).equals(getString(R.string.path_sd_eng))) {
            mPath[0] = Constants.ROOT_PATH;
            mPathList.set(0, Constants.ROOT_PATH);
        }
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
            dismissPopwindow();
            switch (view.getId()) {
                case R.id.pop_usb_view:
                    uninstallUSB(mUsbPath);
                    break;
                case R.id.pop_usb_info:
                    int usbPosition = getUsbPosition(mUsbPath);
                    String usbs = mUsbLists.get(usbPosition);
                    if (usbs != null && new File(usbs).exists()) {
                        UsbPropertyDialog usbPropertyDialog =
                                new UsbPropertyDialog(MainActivity.this, usbs);
                        usbPropertyDialog.showDialog();
                    }
                    break;
                case R.id.pop_usb_format:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.message_format_usb);
                    builder.setNegativeButton(R.string.no, null);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            formatVolume();
                        }
                    });
                    builder.create().show();
            }
        }
    }

    public void formatVolume() {
        String usbPath = mUsbPath;
        if (usbPath.indexOf("/storage/usb") != -1 && usbPath.indexOf("_") != -1) {
            usbPath = usbPath.substring(0, 13);
        }
        try {
            Intent formatIntent = new Intent(ExternalStorageFormatter.FORMAT_ONLY);
            formatIntent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
            StorageVolume[] vols = getMountService().getVolumeList(
                    MainActivity.this.getUserId(), ActivityThread.currentPackageName(), 0);
            StorageVolume vol = null;
            for (StorageVolume i : vols) {
                if (i.getPath().equals(usbPath)) {
                    vol = i;
                    break;
                }
            }
            if (vol != null) {
                formatIntent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, vol);
                startService(formatIntent);
            }
        } catch (RemoteException e) {
        }
    }

    private void setSelectView(View view) {
        if (view == mPreSelectedView) {
            return;
        }
        if (mPreSelectedView != null) {
            mPreSelectedView.setSelected(false);
            mPreSelectedView.setBackground(getResources().getDrawable(R.drawable.left_bg_shape));
        }
        view.setSelected(true);
        view.setBackground(getResources().getDrawable(android.R.color.holo_purple));
        mPreSelectedView = view;
    }

    private View getUsbView(String usbPath) {
        View inflate = View.inflate(this, R.layout.usb_list, null);
        TextView name = (TextView) inflate.findViewById(R.id.usb_list_usb_name);
        ImageView uninstall = (ImageView) inflate.findViewById(R.id.usb_list_uninstall);
        name.setText(Util.getUsbName(this, usbPath));
        uninstall.setTag(usbPath);
        inflate.setTag(usbPath);
        uninstall.setOnClickListener(new UsbUninstallListener());
        inflate.setOnTouchListener(mLeftTouchListener);
        inflate.setOnHoverListener(mLeftHoverListener);
        return inflate;
    }

    private int getUsbPosition(String path) {
        for (int i = 0; i < mUsbLists.size(); i++) {
            String viewPath = (String) mLlUsb.getChildAt(i).getTag();
            if (path.equals(viewPath)) {
                return i;
            }
        }
        return -1;
    }

    private class CustomFileObserver extends FileObserver {

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
                    ((FileManagerApplication) getApplication()).handler
                            .sendMessage(Message.obtain(
                                    ((FileManagerApplication) getApplication()).handler,
                                    Constants.ONLY_REFRESH,
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
            String path = editable.toString();
            if (TextUtils.isEmpty(path)) {
                return;
            }
            path = Util.getRealPath(MainActivity.this, path);
            if (mCustomFileObserver != null) {
                mCustomFileObserver.stopWatching();
                mCustomFileObserver = null;
            }
            mCustomFileObserver = new CustomFileObserver(path);
            mCustomFileObserver.startWatching();
        }
    }

    public void clearNivagateFocus() {
        mEtSearchView.clearFocus();
        mEtNavigation.clearFocus();
    }

    private class UsbUninstallListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            mUsbPath = (String) view.getTag();
            uninstallUSB((String) view.getTag());
        }
    }

    private class LeftHoverListener implements View.OnHoverListener {
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
        setNavigationPath(null);
        setSelectView(mTvComputer);
        setFileInfo(R.id.tv_computer, "", mSdStorageFragment);
    }

    private class LeftTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            clearNivagateFocus();
            switch (motionEvent.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    mCurLeftItem = view;
                    leftEnter(view);
                    mCurTabIndex = 8;
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    switch (view.getId()) {
                        case R.id.tv_collected:
                            mCurEventView = view;
                            showPopWindow(COLLECTION_ITEM_TAG);
                            break;
                        case R.id.usb:
                            if (view.getTag() != null) {
                                mUsbPath = (String) view.getTag();
                                showPopWindow(USB_POPWINDOW_TAG);
                            }
                            break;
                        case R.id.mount:
                            Volume v = (Volume) view.getTag();
                            showPopWindow(v, view);
                    }
                    break;
            }
            return false;
        }
    }

    public class AddressOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (getVisibleFragment() != null
                        && getVisibleFragment() instanceof SystemSpaceFragment
                        && view.getTag() instanceof PathAdapter.ViewHolder) {
                    mCurTabIndex = 9;
                    int pos = (int) ((PathAdapter.ViewHolder) view.getTag()).path.getTag();
                    if (pos == 0 && !Build.TYPE.equals("eng")
                            && mPath[pos].equals(Constants.ROOT_PATH)) {
                        return true;
                    }
                    if (pos == mPath.length - 1) {
                        ((IFileInteractionListener) getVisibleFragment()).
                                onRefreshFileList(mCurPath, getFileSortHelper());
                    } else {
                        mClickPath = "";
                        for (int j = 0; j <= pos; j++) {
                            if ((j == 0 && mPath[0].equals(Constants.ROOT_PATH)) || j == pos) {
                                mClickPath += mPath[j];
                            } else {
                                mClickPath += mPath[j] + Constants.ROOT_PATH;
                            }
                        }
                        mClickPath = mClickPath.replaceAll(
                                getResources().getString(R.string.path_sd_eng),
                                Util.getSdDirectory());
                        ((SystemSpaceFragment) getVisibleFragment()).
                                mFileViewInteractionHub.openSelectFolder(mClickPath);
                    }
                } else {
                    mCurTabIndex = 6;
                    disSelectPreView();
                    mAddressListView.setVisibility(View.GONE);
                    mEtNavigation.setVisibility(View.VISIBLE);
                    mEtNavigation.requestFocus();
                    mEtNavigation.setSelection(mEtNavigation.getText().length());
                }
            }
            return true;
        }
    }

    public class AddressOnFocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (!view.hasFocus()) {
                mEtNavigation.setVisibility(View.GONE);
                mAddressListView.setVisibility(View.VISIBLE);
            }
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

    public ArrayList<Volume> getVolumes() {
        return mVolumes;
    }

    public ArrayList<String> getUsbLists() {
        return mUsbLists;
    }

    private void mountVolume(Volume v) {
        String arg = "";
        String command = "";
        if (v.getType().equals("vfat")) {
            command = "mount ";
            arg = "-t vfat ";
        } else if (v.getType().equals("ext2")) {
            command = "mount ";
            arg = "-t ext2 ";
        } else if (v.getType().equals("ext3")) {
            command = "mount ";
            arg = "-t ext3 ";
        } else if (v.getType().equals("ext4")) {
            command = "mount ";
            arg = "-t ext4 ";
        } else if (v.getType().equals("ntfs")) {
            command = "ntfs-3g ";
            arg = "";
        }
        Util.exec(new String[]{"su", "-c", command + arg + "-o rw "
                + "/dev/block/" + v.getBlock() + " " + v.getPath()});
        v.setIsMount(true);
        mSdStorageFragment.initMountData();
    }

    public void umountVolume(Volume v) {
        Util.exec(new String[]{"su", "-c", "umount " + v.getPath()});
        v.setIsMount(false);
        mSdStorageFragment.initMountData();
        if (getCurPath() != null && getCurPath().startsWith(v.getPath())) {
            mManager.beginTransaction().hide(mMountMap.get(v.getBlock()))
                    .show(mSdStorageFragment).commit();
            mCurFragment = mSdStorageFragment;
            setCurPath(null);
            setNavigationPath(null);
        }
        mUserOperationFragments.remove(mMountMap.get(v.getBlock()));
    }

    public void enter(Volume v) {
        SystemSpaceFragment fragment = mMountMap.get(v.getBlock());
        fragment.setPath(v.getPath());
        fragment.getFileViewInteractionHub().setRootPath(v.getPath());
        FileListAdapter adapter = fragment.getAdapter();
        if (adapter != null) {
            adapter.getSelectFileInfoList().clear();
            fragment.getFileViewInteractionHub().clearSelection();
            fragment.onRefreshFileList(v.getPath(), getFileSortHelper());
        }
        setNavigationPath(v.getPath());
        setCurPath(v.getPath());
        if (mCurFragment != null) {
            mManager.beginTransaction().hide(mCurFragment).commitAllowingStateLoss();
        }
        mManager.beginTransaction().show(fragment).commitAllowingStateLoss();
        mCurFragment = fragment;
        mUserOperationFragments.add(fragment);
        mFragmentIndex++;
        mLeftViewTagAndIdMap.put(v.getBlock(), R.id.mount);
        mIvBack.setImageDrawable(getDrawable(R.mipmap.backward_enable));
        mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_enable));
        setSelectedBackground(R.id.mount);
    }

    public void enter(String usbPath) {
        mUsbStorageFragment = mUsbFragments.get(usbPath);
        if (mUsbStorageFragment == null || !mUsbStorageFragment.isAdded()) {
            mUsbStorageFragment = new SystemSpaceFragment(
                    usbPath, usbPath, null, false);
            mUsbFragments.put(usbPath, mUsbStorageFragment);
        }
        if (mUsbStorageFragment.getFileViewInteractionHub() != null) {
            mUsbStorageFragment.getFileViewInteractionHub().setRootPath(usbPath);
            FileListAdapter adapter = mUsbStorageFragment.getAdapter();
            if (adapter != null) {
                adapter.getSelectFileInfoList().clear();
                mUsbStorageFragment.getFileViewInteractionHub().clearSelection();
                mUsbStorageFragment.onRefreshFileList(usbPath, getFileSortHelper());
            }
        }
        mUsbStorageFragment.setPath(usbPath);
        setNavigationPath(usbPath);
        setCurPath(usbPath);
        if (mCurFragment != null) {
            mManager.beginTransaction().hide(mCurFragment).commitAllowingStateLoss();
        }
        if (!mUsbStorageFragment.isAdded()) {
            mManager.beginTransaction().add(R.id.fl_mian,
                    mUsbStorageFragment, usbPath).commitAllowingStateLoss();
        } else {
            mManager.beginTransaction().show(mUsbStorageFragment).commitAllowingStateLoss();
        }
        mCurFragment = mUsbStorageFragment;
        mUserOperationFragments.add(mUsbStorageFragment);
        mFragmentIndex++;
        mLeftViewTagAndIdMap.put(usbPath, R.id.usb);
        mIvBack.setImageDrawable(getDrawable(R.mipmap.backward_enable));
        mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_enable));
        setSelectedBackground(R.id.usb);
    }

    private class MountListener implements View.OnClickListener {
        private Volume mVolume;

        public MountListener(Volume volume) {
            mVolume = volume;
        }

        @Override
        public void onClick(View view) {
            clearNivagateFocus();
            dismissPopwindow();
            switch (view.getId()) {
                case R.id.pop_mount:
                    mountVolume(mVolume);
                    break;
                case R.id.pop_umount:
                    umountVolume(mVolume);
                    break;
            }
        }
    }

    public LongPressRunnable mLongPressRunnable = new LongPressRunnable();

    private class LongPressRunnable implements Runnable {

        @Override
        public void run() {
            ((BaseFragment) getVisibleFragment()).showMenu();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL) {
            if (event.isCtrlPressed() && event.isAltPressed()) {
                mIsCtrlPress = false;
                return true;
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_TAB
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mCurTabIndex < 9) {
                mCurTabIndex++;
            } else {
                mCurTabIndex = 0;
                if (mCurFragment instanceof SdStorageFragment) {
                    mSdStorageFragment.mPersonalSpace.setSelected(false);
                }
            }
            switchTab();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private class EditTextTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.et_nivagation:
                    mCurTabIndex = 6;
                    break;
                case R.id.search_view:
                    mCurTabIndex = 7;
                    break;
            }
            return false;
        }
    }

    public void setUsbPath(String path) {
        mUsbPath = path;
    }

    public class SeafileServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mISeafileService = ISeafileService.Stub.asInterface(service);
            synchronized (SeafileUtils.TAG) {
                SeafileUtils.TAG.notify();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    public void showCloudInfoDialog() {
        mCloudInfoDialog = new CloudInfoDialog(this);
        mCloudInfoDialog.showDialog();
        dismissPopwindow();
    }

    private void showFolderCollectionDialog() {
        if (mFolderCollectionDialog == null) {
            mFolderCollectionDialog = new FolderCollectionDialog(this, mFolderBeanList);
        }
        mFolderCollectionDialog.show();
    }

    private View getLeftViewByFolderBean(FolderBean bean) {
        TextView tv = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.left_textview_model, null);
        tv.setTag(mFolderBeanList.indexOf(bean));
        tv.setTag(R.id.left_view_path_tag, bean.getPath());
        tv.setText(bean.getTitle());
        tv.setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(bean.getSmallIconRes()), null, null, null);
        tv.setOnHoverListener(mLeftHoverListener);
        tv.setOnTouchListener(mLeftTouchListener);
        return tv;
    }

    public void handleCollectedChange(int index) {
        ArrayList<Integer> changedList = new ArrayList<>();
        changedList.add(index);
        handleCollectedChange(changedList);
    }

    public void handleCollectedChange(List<Integer> changedList) {
        FolderBean bean;
        for (int index : changedList) {
            bean = mFolderBeanList.get(index);
            bean.setIsCollected(!bean.isCollected());
            mEditor.putBoolean(bean.getPath(), bean.isCollected()).apply();
        }

        for (int index = 0, insertPos = 0; index < mFolderBeanList.size(); index++) {
            bean = mFolderBeanList.get(index);
            if (changedList.contains(index)) {
                if (bean.isCollected()) {
                    View view = getLeftViewByFolderBean(bean);
                    mLlCollection.addView(view, insertPos, mTvAdd.getLayoutParams());
                    mCollectedFolderViewAndPathMap.put(view, bean.getPath());
                    mLeftViewTagAndIdMap.put(bean.getPath(), view.getId());
                    mLeftViewList.add(insertPos, view);
                } else {
                    View view = mLlCollection.findViewWithTag(index);
                    mLlCollection.removeView(view);
                    mCollectedFolderViewAndPathMap.remove(view);
                    mLeftViewTagAndIdMap.remove(bean.getPath());
                    mLeftViewList.remove(view);
                }
            }
            if (bean.isCollected()) {
                insertPos++;
            }
        }

        if (isCollectedFolderPath(mCurPath)) {
            setSelectedBackground(mLeftViewTagAndIdMap.get(mCurPath));
        }
    }

    public boolean isCollectedFolderPath(String path) {
        return mCollectedFolderViewAndPathMap.containsValue(path);
    }

    public View getCurEventView() {
        return mCurEventView;
    }

    public Map<String, Integer> getLeftTagAndViewIdMap() {
        return mLeftViewTagAndIdMap;
    }

    public List<FolderBean> getFolderBeanList() {
        return mFolderBeanList;
    }

}
