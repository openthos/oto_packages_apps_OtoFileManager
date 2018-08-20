package org.openthos.filemanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openthos.filemanager.adapter.PathAdapter;
import org.openthos.filemanager.bean.Mode;
import org.openthos.filemanager.bean.PathBean;
import org.openthos.filemanager.bean.PersonalBean;
import org.openthos.filemanager.bean.SeafileLibrary;
import org.openthos.filemanager.bean.Volume;
import org.openthos.filemanager.component.CloudInfoDialog;
import org.openthos.filemanager.component.FolderCollectionDialog;
import org.openthos.filemanager.component.HorizontalListView;
import org.openthos.filemanager.component.InfoDialog;
import org.openthos.filemanager.component.PopOnClickLintener;
import org.openthos.filemanager.component.PopWinShare;
import org.openthos.filemanager.component.SearchOnKeyListener;
import org.openthos.filemanager.component.UsbPropertyDialog;
import org.openthos.filemanager.fragment.PersonalSpaceFragment;
import org.openthos.filemanager.fragment.SambaFragment;
import org.openthos.filemanager.fragment.SdStorageFragment;
import org.openthos.filemanager.fragment.SeafileFragment;
import org.openthos.filemanager.fragment.SearchFragment;
import org.openthos.filemanager.fragment.SystemSpaceFragment;
import org.openthos.filemanager.system.BootCompleteReceiver;
import org.openthos.filemanager.utils.Constants;
import org.openthos.filemanager.bean.FileInfo;
import org.openthos.filemanager.adapter.FileListAdapter;
import org.openthos.filemanager.system.FileOperationHelper;
import org.openthos.filemanager.system.IFileInteractionListener;
import org.openthos.filemanager.system.UsbConnectionReceiver;
import org.openthos.filemanager.utils.Util;
import org.openthos.filemanager.utils.LocalCache;
import org.openthos.filemanager.utils.PersonalBeanComparator;
import org.openthos.filemanager.utils.SeafileUtils;
import org.openthos.filemanager.utils.ToastUtils;
import org.openthos.seafile.ISeafileService;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements View.OnClickListener {
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
    private LinearLayout mLeftUsb;

    private FragmentManager mManager = getSupportFragmentManager();
    private PopWinShare mPopWinShare;
    public BaseFragment mCurFragment;
    public SdStorageFragment mSdStorageFragment;
    public SeafileFragment mSeafileFragment;
    private SambaFragment mSambaFragment;
    private UsbConnectionReceiver mReceiver;
    private boolean mIsMutiSelect;
    private SharedPreferences mSharedPreferences;
    private Editor mEditor;

    public Handler mHandler;
    private LeftTouchListener mLeftTouchListener;
    private LeftHoverListener mLeftHoverListener;
    private boolean mIsFirst = true;

    private InfoDialog mInfoDialog;
    private ProgressDialog mProgressDialog;
    private ProgressDialog mPopUpProgressDialog;
    public PersonalSpaceFragment mPersonalSpaceFragment;
    public SearchFragment mStartSearchFragment;
    public String mCurPath;
    private CustomFileObserver mCustomFileObserver;
    private String mUsbPath;
    private static ContentResolver mContentResolver;
    private static Uri mUri;
    private static boolean mIsCtrlPress = false;
    private static boolean mIsShiftPress = false;
    private HorizontalListView mAddressListView;

    private String[] mPath;
    private List<String> mPathList = new ArrayList<>();
    private PathAdapter mPathAdapter;
    private AddressOnTouchListener mAddressTouchListener;
    private ArrayList<Volume> mVolumes = new ArrayList<>();
    private List<PersonalBean> mPersonalBeanList = new ArrayList<>();
    private Map<View, String> mCollectedFolderViewAndPathMap = new HashMap<>();
    private ArrayList<String> mUsbLists = new ArrayList<>();
    private LinearLayout mLeftCollection;
    private LinearLayout mLeftAutoMount;
    private int mLeftIndex = 0;
    private List<View> mLeftViewList = new ArrayList<>();
    private List<View> mUsbViews = new ArrayList<>();
    public int mCurTabIndex = 0;
    private View mCurLeftItem, mPreTabView, mPreSelectedView;
    private CloudInfoDialog mCloudInfoDialog;
    private FolderCollectionDialog mFolderCollectionDialog;

    public ISeafileService mISeafileService;
    private SystemSpaceFragment mSystemSpaceFragment;
    private List<Object> mHistory = new ArrayList<>();
    private int mHistoryIndex = 0;
    private int mCurLeftSelectedIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initEnvironment();
        initView();
        initData();
        initListener();
    }

    private void initEnvironment() {
        ((FileManagerApplication) getApplication()).addActivity(this);
        mSharedPreferences = getSharedPreferences(Constants.MAIN_SP, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        SeafileServiceConnection serviceConnection = new SeafileServiceConnection();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("org.openthos.seafile",
                "org.openthos.seafile.SeafileService"));
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void initView() {
        mLeftCollection = (LinearLayout) findViewById(R.id.ll_collection);
        mLeftAutoMount = (LinearLayout) findViewById(R.id.ll_mount);
        mLeftUsb = (LinearLayout) findViewById(R.id.ll_usb);
        mTvAdd = (TextView) findViewById(R.id.tv_add);
        mTvComputer = (TextView) findViewById(R.id.tv_computer);
        mTvCloudService = (TextView) findViewById(R.id.tv_seafile);
        mTvNetService = (TextView) findViewById(R.id.tv_samba);
        mIvListView = (ImageView) findViewById(R.id.iv_list_view);
        mIvGridView = (ImageView) findViewById(R.id.iv_grid_view);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mIvForward = (ImageView) findViewById(R.id.iv_forward);
        mIvUp = (ImageView) findViewById(R.id.iv_up);
        mIvSetting = (ImageView) findViewById(R.id.iv_setting);
        mEtNavigation = (EditText) findViewById(R.id.et_nivagation);
        mIvSearchView = (ImageView) findViewById(R.id.iv_search);
        mEtSearchView = (EditText) findViewById(R.id.search_view);
        mAddressListView = (HorizontalListView) findViewById(R.id.lv_address);

        LocalCache.setViewTag(mSharedPreferences.getString(Constants.VIEW_TAG, Constants.VIEW_TAG_GRID));
        if (LocalCache.getViewTag().equals(Constants.VIEW_TAG_LIST)) {
            mIvGridView.setSelected(false);
            mIvListView.setSelected(true);
        } else if (LocalCache.getViewTag().equals(Constants.VIEW_TAG_GRID)) {
            mIvGridView.setSelected(true);
            mIvListView.setSelected(false);
        }

        mInfoDialog = InfoDialog.getInstance(MainActivity.this);

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
                            mUsbPath = msg.obj.toString();
                            initUsb(msg.what);
                            break;
                        case Constants.USB_UNMOUNT:
                            mUsbPath = (String) msg.obj;
                            initUsb(Constants.USB_UNMOUNT);
                            break;
                        case Constants.USB_READY:
                            mLeftUsb.removeAllViews();
                            mUsbViews.clear();
                            for (int i = 0; i < mUsbLists.size(); i++) {
                                View v = getUsbView(mUsbLists.get(i));
                                mLeftUsb.addView(v);
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
                                    mLeftUsb.removeView(tempUsbView);
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
                            mInfoDialog.showDialog(R.raw.paste);
                            mInfoDialog.changeTitle(MainActivity.this.getResources()
                                    .getString(R.string.copy_info));
                            break;
                        case Constants.DELETE_INFO_SHOW:
                            mInfoDialog.showDialog(R.raw.delete);
                            mInfoDialog.changeTitle(MainActivity.this.getResources()
                                    .getString(R.string.copy_info));
                            break;
                        case Constants.COMPRESS_INFO_SHOW:
                            mInfoDialog.showDialog(R.raw.compress);
                            mInfoDialog.changeTitle(MainActivity.this.getResources()
                                    .getString(R.string.copy_info));
                            break;
                        case Constants.DECOMPRESS_INFO_SHOW:
                            mInfoDialog.showDialog(R.raw.decompress);
                            mInfoDialog.changeTitle(MainActivity.this.getResources()
                                    .getString(R.string.copy_info));
                            break;
                        case Constants.COPY_INFO:
                            mInfoDialog.changeMsg((String) msg.obj);
                            break;
                        case Constants.COPY_INFO_HIDE:
                            mInfoDialog.cancel();
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
                            try {
                                mSeafileFragment.setData(mISeafileService.isSync());
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            break;
                        case Constants.REFRESH_BY_OBSERVER:
                            if (System.currentTimeMillis() - mPreTime >= 1000) {
                                mHandler.removeMessages(Constants.ONLY_REFRESH);
                                mHandler.sendMessage(Message.obtain(
                                        mHandler,
                                        Constants.ONLY_REFRESH,
                                        ((SystemSpaceFragment) getVisibleFragment())
                                                .mFileViewInteractionHub.getCurrentPath()));
                                mPreTime = System.currentTimeMillis();
                            } else {
                                mHandler.removeMessages(Constants.ONLY_REFRESH);
                                mHandler.sendMessageDelayed(Message.obtain(
                                        mHandler,
                                        Constants.ONLY_REFRESH,
                                        ((SystemSpaceFragment) getVisibleFragment())
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
        getPersonalFolderInfoFromXml();
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
        initLeftViewList();
    }

    protected void initListener() {

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
        SearchOnKeyListener searchOnKeyListener = new SearchOnKeyListener(mManager, this);
        mEtSearchView.setOnKeyListener(searchOnKeyListener);
        mIvSearchView.setOnClickListener(this);
        EditTextTouchListener editTextTouchListener = new EditTextTouchListener();
        mEtSearchView.setOnTouchListener(editTextTouchListener);
        NavigationOnKeyListener navigationOnKeyListener = new NavigationOnKeyListener();
        mEtNavigation.setOnTouchListener(editTextTouchListener);
        mEtNavigation.setOnKeyListener(navigationOnKeyListener);
        TextChangeListener textChangeListener = new TextChangeListener();
        mEtNavigation.addTextChangedListener(textChangeListener);
        mEtNavigation.setOnFocusChangeListener(new AddressOnFocusChangeListener());
        mAddressListView.setOnTouchListener(mAddressTouchListener);
        mReceiver = new UsbConnectionReceiver(this);
    }

    private void getPersonalFolderInfoFromXml() {
        XmlResourceParser parser = getResources().getXml(R.xml.personal_folders);
        try {
            PersonalBean bean;
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.getAttributeCount() > 0) {
                    int pathRes = parser.getAttributeResourceValue(
                            NAME_SPACE_RES_AUTO, "pathRes", -1);
                    String path = Constants.SDCARD_PATH + getResources().getString(pathRes);
                    if (new File(path).exists()) {
                        bean = new PersonalBean();
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
                                mSharedPreferences.getBoolean(bean.getPath(), bean.isSystemFolder()));
                        mPersonalBeanList.add(bean);
                    }
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(mPersonalBeanList, new PersonalBeanComparator());
    }

    private void initFragment() {
        FragmentTransaction transaction = mManager.beginTransaction();
        mSdStorageFragment = new SdStorageFragment();
        transaction.add(R.id.fl_mian, mSdStorageFragment).hide(mSdStorageFragment);
        mPersonalSpaceFragment = new PersonalSpaceFragment();
        transaction.add(R.id.fl_mian, mPersonalSpaceFragment).hide(mPersonalSpaceFragment);
        mSeafileFragment = new SeafileFragment();
        transaction.add(R.id.fl_mian, mSeafileFragment).hide(mSeafileFragment);
        mSambaFragment = new SambaFragment();
        transaction.add(R.id.fl_mian, mSambaFragment).hide(mSambaFragment);
        mSystemSpaceFragment = new SystemSpaceFragment();
        transaction.add(R.id.fl_mian, mSystemSpaceFragment).hide(mSystemSpaceFragment);
        for (int i = 0; i < mVolumes.size(); i++) {
            Volume v = mVolumes.get(i);
            View inflate = View.inflate(this, R.layout.mount_list, null);
            TextView name = (TextView) inflate.findViewById(R.id.usb_list_usb_name);
            name.setText(v.getName());
            inflate.setOnHoverListener(mLeftHoverListener);
            inflate.setOnTouchListener(mLeftTouchListener);
            inflate.setTag(v);
            v.setPath("/storage/disk" + i);
            mLeftAutoMount.addView(inflate);

        }
        mCurTabIndex = -1;
        transaction.commitAllowingStateLoss();
    }

    private void initLeftCollectionView() {
        PersonalBean bean;
        for (int i = 0; i < mPersonalBeanList.size(); i++) {
            bean = mPersonalBeanList.get(i);
            if (bean.isCollected()) {
                View v = getLeftViewByFolderBean(bean);
                mLeftCollection.addView(
                        v, mLeftCollection.getChildCount() - 1, mTvAdd.getLayoutParams());
                mCollectedFolderViewAndPathMap.put(v, bean.getPath());
            }
        }
    }

    private void initLeftViewList() {
        for (int i = 0; i < mLeftCollection.getChildCount(); i++) {
            mLeftViewList.add(mLeftCollection.getChildAt(i));
        }
        mLeftViewList.add(mTvComputer);
        for (int i = 0; i < mLeftAutoMount.getChildCount(); i++) {
            mLeftViewList.add(mLeftAutoMount.getChildAt(i));
        }
        if (mLeftAutoMount.getChildCount() == 0) {
            findViewById(R.id.text_mount).setVisibility(View.GONE);
        }
        mLeftViewList.add(mTvCloudService);
        mLeftViewList.add(mTvNetService);
    }

    private void initUsb(int flags) {
        switch (flags) {
            case Constants.USB_INIT:
                mSdStorageFragment.initUsbData();
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
                ToastUtils.showShort(this, getResources().getString(R.string.USB_device_connected));
                initUsb(Constants.USB_INIT);
                break;
            case Constants.USB_EJECT:
                if (mUsbPath != null) {
                    int position;
                    for (int i = 0; i < mUsbLists.size(); i++) {
                        if (mUsbPath.equals(mUsbLists.get(i))) {
                            position = getUsbPosition(mUsbPath);
                            View v = mLeftUsb.getChildAt(getUsbPosition(mUsbPath));
                            mUsbViews.remove(v);
                            mLeftUsb.removeView(v);
                            mSdStorageFragment.removeUsbView(position);
                            break;
                        }
                    }

                    if (mUsbPath.indexOf("/storage/") != -1) {
                        for (int i = mUsbLists.size() - 1; i >= 0; i--) {
                            if ((mUsbLists.get(i)).indexOf(mUsbPath + "_") != -1) {
                                position = getUsbPosition(mUsbLists.get(i));
                                mLeftUsb.removeViewAt(getUsbPosition(mUsbLists.get(i)));
                                mSdStorageFragment.removeUsbView(position);
                            }
                        }
                    }

                }
                ToastUtils.showShort(this, getResources().getString(R.string.USB_device_disconnected));
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
        String data = mSharedPreferences.getString(Constants.SP_AUTOMOUNT, "ERROR");
        if (data.equals("ERROR")) {
            data = BootCompleteReceiver.refreshAutoMountData(this);
            mEditor.putString(Constants.SP_AUTOMOUNT, data).commit();
        }
        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                Volume v = new Volume();
                v.setBlock(object.getString("block"));
                v.setIsMount(object.getBoolean("ismount"));
                v.setType(object.getString("type"));
                v.setName(object.getString("name"));
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
        setNavigationPath(null);
        setSelectView(mTvComputer);
        mCurFragment = mSdStorageFragment;
        mHistory.add(mCurFragment);
        mManager.beginTransaction().show(mCurFragment).commitAllowingStateLoss();
        String path = getIntent().getStringExtra(Constants.PATH_TAG);
        if (!TextUtils.isEmpty(path)) {
            showFileSpaceFragment(path);
        }
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
                            showFileSpaceFragment(path.substring(i, path.length()));
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
                mCurLeftItem = mLeftCollection.getChildAt(0);
                processTab(mCurLeftItem);
                break;
            case 9:
                if (mCurFragment instanceof SdStorageFragment) {
                    if (mSdStorageFragment.mCurView != null) {
                        mSdStorageFragment.mCurView.setSelected(false);
                    }
                    mLeftCollection.getChildAt(0).setBackgroundColor(Color.TRANSPARENT);
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
                if (mLeftIndex > mLeftCollection.getChildCount() - 1) {
                    if (mUsbViews.size() != 0) {
                        mLeftViewList.addAll(
                                mLeftViewList.size() - mLeftAutoMount.getChildCount() - 2, mUsbViews);
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
            ((SystemSpaceFragment) getVisibleFragment()).mFileViewInteractionHub.onOperationDelete();
        } else if (keyCode == KeyEvent.KEYCODE_FORWARD_DEL && event.isShiftPressed()) {
            sendBroadcastMessage("iv_menu", "pop_delete", false);
            if (isCopyByHot()) {
                return false;
            }
            ((SystemSpaceFragment) getVisibleFragment()).mFileViewInteractionHub.onOperationDeleteDirect();
        }
        if (keyCode == KeyEvent.KEYCODE_F2) {
            if (isCopyByHot() || isRecycle()) {
                return false;
            }
            ((SystemSpaceFragment) getVisibleFragment()).mFileViewInteractionHub.onOperationRename();
        }
        if (keyCode == KeyEvent.KEYCODE_F5) {
            if (getVisibleFragment() instanceof PersonalSpaceFragment) {
                mHandler.sendEmptyMessage(Constants.REFRESH_PERSONAL);
            } else if (isCopyByHot()) {
                return false;
            } else {
                mHandler.sendMessage(Message.obtain(mHandler, Constants.ONLY_REFRESH,
                        ((SystemSpaceFragment) getVisibleFragment()).mFileViewInteractionHub
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
                showFileSpaceFragment(((PersonalBean) view.getTag()).getPath());
                setSelectedBackground(R.id.tv_collected);
                break;
            case R.id.tv_computer:
                showFragment(mSdStorageFragment);
                setSelectedBackground(R.id.tv_computer);
                break;
            case R.id.tv_seafile:
                if (!SeafileUtils.isNetworkOn(this)) {
                    ToastUtils.showShort(this, getResources().getString(R.string.network_down));
                }
                mSeafileFragment.goToHome();
                showFragment(mSeafileFragment);
                break;
            case R.id.tv_samba:
                showFragment(mSambaFragment);
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
                setSelectedBackground(R.id.mount);
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
                ArrayList<FileInfo> list = ((SystemSpaceFragment) getVisibleFragment())
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
                ((SystemSpaceFragment) getVisibleFragment()).mFileViewInteractionHub.getCurrentPath();
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
                ArrayList<FileInfo> list = ((SystemSpaceFragment) getVisibleFragment())
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

    public void onUp() {
        onBackPressed();
    }

    @Override
    public void onClick(View view) {
        clearNivagateFocus();
        switch (view.getId()) {
            case R.id.iv_back:
                mCurTabIndex = 0;
                mHistoryIndex--;
                showHistory();
                break;
            case R.id.iv_forward:
                mCurTabIndex = 1;
                mHistoryIndex++;
                showHistory();
                break;
            case R.id.iv_up:
                mCurTabIndex = 2;
                onUp();
                break;
            case R.id.iv_setting:
                mCurTabIndex = 3;
                showPopWindow(SETTING_POPWINDOW_TAG);
                break;
            case R.id.iv_grid_view:
                mCurTabIndex = 4;
                mIvGridView.setSelected(true);
                mIvListView.setSelected(false);
                LocalCache.setViewTag(Constants.VIEW_TAG_GRID);
                sendBroadcastMessage(IV_SWITCH_VIEW, Constants.VIEW_TAG_GRID, false);
                mEditor.putString(Constants.VIEW_TAG, Constants.VIEW_TAG_GRID);
                mEditor.commit();
                break;
            case R.id.iv_list_view:
                mCurTabIndex = 5;
                mIvGridView.setSelected(false);
                mIvListView.setSelected(true);
                LocalCache.setViewTag(Constants.VIEW_TAG_LIST);
                sendBroadcastMessage(IV_SWITCH_VIEW, Constants.VIEW_TAG_LIST, false);
                mEditor.putString(Constants.VIEW_TAG, Constants.VIEW_TAG_LIST);
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
        if (usbPath.indexOf("/storage/usb") != -1 && usbPath.indexOf("_") != -1) {
            usbPath = usbPath.substring(0, 13);
        }

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
		UsbUtils.umount(MainActivity.this, usbPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setSelectedBackground(int id) {
        switch (id) {
            case R.id.tv_collected:
                for (int i = 0; i < mLeftCollection.getChildCount() - 1; i++) {
                    if (mSystemSpaceFragment.getCurrentPath().equals(
                            ((PersonalBean) (mLeftCollection.getChildAt(i).getTag())).getPath())) {
                        setSelectView(mLeftCollection.getChildAt(i));
                        break;
                    }
                }
                break;
            case R.id.tv_computer:
                if (mCurLeftSelectedIndex == id) {
                    return;
                }
                setSelectView(mTvComputer);
                break;
            case R.id.tv_seafile:
                if (mCurLeftSelectedIndex == id) {
                    return;
                }
                setSelectView(mTvCloudService);
                break;
            case R.id.tv_samba:
                if (mCurLeftSelectedIndex == id) {
                    return;
                }
                setSelectView(mTvNetService);
                break;
            case R.id.mount:
                for (int i = 0; i < mLeftAutoMount.getChildCount(); i++) {
                    if (mSystemSpaceFragment.getCurrentPath()
                            .equals(((Volume) (mLeftAutoMount.getChildAt(i).getTag())).getPath())) {
                        setSelectView(mLeftAutoMount.getChildAt(i));
                        break;
                    }
                }
                break;
            case R.id.usb:
                for (int i = 0; i < mLeftUsb.getChildCount(); i++) {
                    if (mSystemSpaceFragment.getCurrentPath().equals(mLeftUsb.getChildAt(i).getTag())) {
                        setSelectView(mLeftUsb.getChildAt(i));
                        break;
                    }
                }
                break;
        }
        mCurLeftSelectedIndex = id;
    }

    private void sendBroadcastMessage(String name, String tag, boolean isCtrl) {
        Intent intent = new Intent();
        switch (name) {
            case IV_SWITCH_VIEW:
                intent.setAction("com.switchview");
                intent.putExtra("switch_view", tag);
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
                anchorView = mLeftUsb.getChildAt(getUsbPosition(mUsbPath));
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
        mPopWinShare.showAsDropDown(view);
    }

    public void dismissPopwindow() {
        mPopWinShare.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (mCurFragment.canGoBack()) {
            mCurFragment.goBack();
        } else {
            for (int i = mHistoryIndex - 1; i >= 0; i--) {
                Object o = mHistory.get(i);
                if (o instanceof PersonalSpaceFragment
                        || o instanceof SambaFragment
                        || o instanceof SeafileFragment
                        || o instanceof SdStorageFragment) {
                    if (o != mCurFragment) {
                        showFragment((BaseFragment) o, true);
                        return;
                    }
                }
            }
            showFragment(mSdStorageFragment, true);
        }
    }

    public Fragment getVisibleFragment() {
        List<Fragment> fragments = mManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible()) {
                return fragment;
            }
        }
        Log.i("FileManager", android.util.Log.getStackTraceString(new Throwable()));
        String path = mEtNavigation.getText().toString();
        if (TextUtils.isEmpty(path.trim())) {
            showFileSpaceFragment("~");
        } else {
            for (int i = 0; i < path.length(); i++) {
                if (path.charAt(i) != ' ') {
                    showFileSpaceFragment(path.substring(i, path.length()));
                    break;
                }
            }
        }
        return getVisibleFragment();
    }

    public void returnToRootDir() {
        showFragment(mSdStorageFragment);
    }

    public interface IBackPressedListener {
    }

    @Override
    protected void onDestroy() {
        Log.i("FileManager", getComponentName().getClassName() + " Destory");
        ((FileManagerApplication) getApplication()).removeActivity(this);
        super.onDestroy();
    }

    boolean isRestart = false;

    @Override
    protected void onPause() {
        Log.i("FileManager", getComponentName().getClassName() + " Pause");
        isRestart = true;
        super.onPause();
    }

    @Override
    protected void onStop() {
        mReceiver.unregisterReceiver();
        Log.i("FileManager", getComponentName().getClassName() + " Stop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.i("FileManager", getComponentName().getClassName() + " Resume");
        super.onResume();
        if (mCurFragment == null) {
            initFirstPage();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mReceiver.registerReceiver();
        Log.i("FileManager", getComponentName().getClassName() + " Start");
    }

    @Override
    public void onRestart() {
        Log.i("FileManager", getComponentName().getClassName() + " ReStart");
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
	UsbUtils.format(MainActivity.this, usbPath);
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
            String viewPath = (String) mLeftUsb.getChildAt(i).getTag();
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
                                    ((SystemSpaceFragment) getVisibleFragment())
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

    private class LeftTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
//                    && motionEvent.getButtonState() != MotionEvent.BUTTON_FORWARD
//                    && motionEvent.getButtonState() != MotionEvent.BUTTON_BACK
//                    && motionEvent.getButtonState() != MotionEvent.BUTTON_TERTIARY
                    ) {
                clearNivagateFocus();
                if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
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
                            break;
                    }
                } else {
                    mCurLeftItem = view;
                    leftEnter(view);
                    mCurTabIndex = 8;

                }
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
                        String mClickPath = "";
                        for (int j = 0; j <= pos; j++) {
                            if ((j == 0 && mPath[0].equals(Constants.ROOT_PATH)) || j == pos) {
                                mClickPath += mPath[j];
                            } else {
                                mClickPath += mPath[j] + Constants.ROOT_PATH;
                            }
                        }
                        mClickPath = mClickPath.replaceAll(
                                getString(R.string.path_sd_eng), Constants.SDCARD_PATH);
                        showFileSpaceFragment(mClickPath);
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
    }

    public void enter(Volume v) {
        showFileSpaceFragment(v.getPath());
        setSelectedBackground(R.id.mount);
    }

    public void enter(String usbPath) {
        showFileSpaceFragment(usbPath);
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
            try {
                SeafileUtils.mUserId = mISeafileService.getUserName();
                if (TextUtils.isEmpty(SeafileUtils.mUserId)) {
                    return;
                }
                mHandler.sendEmptyMessage(Constants.SEAFILE_DATA_OK);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (mCloudInfoDialog != null && mCloudInfoDialog.isShowing()) {
                mCloudInfoDialog.refreshView();
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
            mFolderCollectionDialog = new FolderCollectionDialog(this, mPersonalBeanList);
        }
        mFolderCollectionDialog.show();
    }

    private View getLeftViewByFolderBean(PersonalBean bean) {
        TextView tv = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.left_textview_model, null);
        tv.setTag(bean);
        tv.setText(bean.getTitle());
        tv.setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(bean.getSmallIconRes()), null, null, null);
        tv.setOnHoverListener(mLeftHoverListener);
        tv.setOnTouchListener(mLeftTouchListener);
        return tv;
    }

    public void handleCollectedChange(PersonalBean bean) {
        ArrayList<PersonalBean> changedList = new ArrayList<>();
        changedList.add(bean);
        handleCollectedChange(changedList);
    }

    public void handleCollectedChange(List<PersonalBean> changedList) {
        for (PersonalBean bean : changedList) {
            bean.setIsCollected(!bean.isCollected());
            mEditor.putBoolean(bean.getPath(), bean.isCollected()).apply();
        }

        for (int index = 0, insertPos = 0; index < mPersonalBeanList.size(); index++) {
            PersonalBean bean = mPersonalBeanList.get(index);
            if (changedList.contains(bean)) {
                if (bean.isCollected()) {
                    View view = getLeftViewByFolderBean(bean);
                    mLeftCollection.addView(view, insertPos, mTvAdd.getLayoutParams());
                    mCollectedFolderViewAndPathMap.put(view, bean.getPath());
                    mLeftViewList.add(insertPos, view);
                } else {
                    View view = mLeftCollection.findViewWithTag(bean);
                    mLeftCollection.removeView(view);
                    mCollectedFolderViewAndPathMap.remove(view);
                    mLeftViewList.remove(view);
                }
            }
            if (bean.isCollected()) {
                insertPos++;
            }
        }
    }

    public boolean isCollectedFolderPath(String path) {
        return mCollectedFolderViewAndPathMap.containsValue(path);
    }

    public View getCurEventView() {
        return mCurEventView;
    }

    public List<PersonalBean> getPersonalBeanList() {
        return mPersonalBeanList;
    }

    public void showFileSpaceFragment(String path) {
        showFileSpaceFragment(path, path, true);
    }

    public void showFileSpaceFragment(String parent, String path) {
        showFileSpaceFragment(path, path, true);
    }

    public void showFileSpaceFragment(String path, boolean isSetHistory) {
        showFileSpaceFragment(path, path, isSetHistory);
    }

    public void showFileSpaceFragment(String parent, String path, boolean isSetHistory) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(parent)) {
            return;
        }
        if (path.startsWith(getString(R.string.path_sd_eng))) {
            path = path.replaceAll(getString(R.string.path_sd_eng), Constants.SDCARD_PATH);
        } else if (!path.startsWith(Constants.ROOT_PATH)) {
            path = Constants.ROOT_PATH + path;
        }
        if (parent.startsWith(getString(R.string.path_sd_eng))) {
            parent = parent.replaceAll(getString(R.string.path_sd_eng), Constants.SDCARD_PATH);
        } else if (!parent.startsWith(Constants.ROOT_PATH)) {
            parent = Constants.ROOT_PATH + parent;
        }
        File file = new File(path);
        File fileParent = new File(parent);
        try {
            path = file.getCanonicalPath();
            parent = fileParent.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!Build.TYPE.equals("eng") && !(path.startsWith(Constants.DEVICE_PATH))) {
            ToastUtils.showShort(this, getResources().getString(R.string.have_no_permission));
            return;
        }
        if (file.exists()) {
            if (!(mCurFragment instanceof SystemSpaceFragment)) {
                FragmentTransaction transaction = mManager.beginTransaction();
                transaction.hide(mCurFragment);
                mCurFragment = mSystemSpaceFragment;
                transaction.show(mCurFragment).commitAllowingStateLoss();
            }
            mSystemSpaceFragment.setPath(parent, path);
            mCurPath = path;
            mCurTabIndex = 9;
            if (isSetHistory) {
                setHistory(new PathBean(path));
            } else {
                setSelectedBackground(R.id.tv_collected);
            }
            mIvBack.setImageDrawable(getDrawable(R.mipmap.backward_enable));
            mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_enable));
        } else {
            ToastUtils.showShort(this, getResources().getString(R.string.address_search_false));
        }
    }

    public void showFragment(BaseFragment fragment) {
        showFragment(fragment, true);
    }

    public void showFragment(BaseFragment fragment, boolean isSetHistory) {
        if (mCurFragment != fragment) {
            FragmentTransaction transaction = mManager.beginTransaction();
            transaction.hide(mCurFragment);
            mCurFragment = fragment;
            transaction.show(mCurFragment).commitAllowingStateLoss();
            setCurPath(null);
            setNavigationPath(null);
            if (isSetHistory) {
                setHistory(fragment);
            }
            if (fragment instanceof SdStorageFragment) {
                mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_disable));
            } else {
                mIvUp.setImageDrawable(getResources().getDrawable(R.mipmap.up_enable));
            }
            if (mCurFragment instanceof SdStorageFragment
                    || mCurFragment instanceof PersonalSpaceFragment) {
                setSelectedBackground(R.id.tv_computer);
            } else if (mCurFragment instanceof SeafileFragment) {
                setSelectedBackground(R.id.tv_seafile);
            } else if (mCurFragment instanceof SambaFragment) {
                setSelectedBackground(R.id.tv_samba);
            }
        }
    }

    public void setHistory(Object obj) {
        if (!(mHistoryIndex == mHistory.size() - 1)) {
            mHistory = mHistory.subList(0, mHistoryIndex + 1);
        }
        if (!mHistory.get(mHistoryIndex).equals(obj)) {
            mHistory.add(obj);
            mHistoryIndex++;
        }
    }

    private void showHistory() {
        if (mHistoryIndex <= 0) {
            mIvBack.setImageDrawable(getDrawable(R.mipmap.backward_disable));
            mIvForward.setImageDrawable(getDrawable(R.mipmap.forward_enable));
            mHistoryIndex = 0;
        } else if (mHistoryIndex >= mHistory.size() - 1) {
            mIvBack.setImageDrawable(getDrawable(R.mipmap.backward_enable));
            mIvForward.setImageDrawable(getDrawable(R.mipmap.forward_disable));
            mHistoryIndex = mHistory.size() - 1;
        } else {
            mIvBack.setImageDrawable(getDrawable(R.mipmap.backward_enable));
            mIvForward.setImageDrawable(getDrawable(R.mipmap.forward_enable));
        }
        Object o = mHistory.get(mHistoryIndex);
        if (o instanceof PersonalSpaceFragment
                || o instanceof SambaFragment
                || o instanceof SeafileFragment
                || o instanceof SdStorageFragment) {
            showFragment((BaseFragment) o, false);
        } else if (o instanceof PathBean) {
            showFileSpaceFragment(((PathBean) o).root, ((PathBean) o).path, false);
        }
    }
}
