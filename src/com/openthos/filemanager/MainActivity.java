package com.openthos.filemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.openthos.filemanager.view.SystemSpaceFragment;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private TextView tv_desk;
    private TextView tv_music;
    private TextView tv_video;
    private TextView tv_computer;
    private TextView tv_picture;
    private TextView tv_storage;
    private TextView tv_net_service;
    private ImageView iv_list_view;
    private ImageView iv_grid_view;
    private ImageView iv_back;
    private ImageView iv_setting;
    private EditText et_nivagation;
    private EditText et_search_view;
    private ImageView iv_search_view;

    private static final String USB_SPACE_FRAGMENT = "usb_space_fragment";
    private static final String USB_DEVICE_ATTACHED = "usb_device_attached";
    private static final String USB_DEVICE_DETACHED = "usb_device_detached";
    private FragmentManager manager = getSupportFragmentManager();
    private PopWinShare popWinShare;
    private Fragment curFragment = null;
    private SdStorageFragment sdStorageFragment = null;
    private DeskFragment deskFragment;
    private MusicFragment musicFragment;
    private VideoFragment videoFragment;
    private PictrueFragment pictrueFragment;
    private OnlineNeighborFragment onlineNeighborFragment;
    private UsbConnectReceiver receiver;
    private String[] usbs;
    private boolean mIsMutiSelect;

    private Handler handler = new Handler() {
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
                }
            }
            super.handleMessage(msg);
        }
    };
    private boolean isFirst = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalCache.getInstance(MainActivity.this).setViewTag("grid");
        initView();
        initFragemnt();
        initData();
        initUsb(-1);
        curFragment = sdStorageFragment;
    }

    private void initView() {
        tv_desk = (TextView) findViewById(R.id.tv_desk);
        tv_music = (TextView) findViewById(R.id.tv_music);
        tv_video = (TextView) findViewById(R.id.tv_video);
        tv_computer = (TextView) findViewById(R.id.tv_computer);
        tv_picture = (TextView) findViewById(R.id.tv_picture);
        tv_storage = (TextView) findViewById(R.id.tv_storage);
        tv_net_service = (TextView) findViewById(R.id.tv_net_service);
        iv_list_view = (ImageView) findViewById(R.id.iv_list_view);
        iv_grid_view = (ImageView) findViewById(R.id.iv_grid_view);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_setting = (ImageView) findViewById(R.id.iv_setting);
        et_nivagation = (EditText) findViewById(R.id.et_nivagation);
        iv_search_view = (ImageView) findViewById(R.id.iv_search);
        iv_grid_view.setSelected(true);
    }

    private void initUsb(int flags) {
        String[] cmd = new String[]{"df"};
        usbs = Util.execUsb(cmd);
        if (usbs != null && usbs.length > 0 && flags != 0 && flags != 1) {
            sendMsg(2);
        }
        if (flags == UsbConnectReceiver.USB_STATE_ON || flags == 2) {
            T.showShort(MainActivity.this, getResources().getString(R.string.USB_device_connected));
            tv_storage.setVisibility(View.VISIBLE);
            tv_storage.setOnClickListener(MainActivity.this);
            sdStorageFragment = new SdStorageFragment(manager, USB_DEVICE_ATTACHED,
                                                      MainActivity.this);
            setSelectedBackground(R.id.tv_computer);
            manager.beginTransaction().replace(R.id.fl_mian, sdStorageFragment).commit();
        } else if (flags == UsbConnectReceiver.USB_STATE_OFF) {
            tv_storage.setVisibility(View.GONE);
            tv_storage.setVisibility(View.GONE);
            sdStorageFragment = new SdStorageFragment(manager, USB_DEVICE_DETACHED,
                                                      MainActivity.this);
            setSelectedBackground(R.id.tv_computer);
            manager.beginTransaction().replace(R.id.fl_mian, sdStorageFragment).commit();
        }
    }

    private void initFragemnt() {
        receiver = new UsbConnectReceiver(this);
        if (sdStorageFragment == null) {
            sdStorageFragment = new SdStorageFragment(manager, null, MainActivity.this);
        }
        if (deskFragment == null) {
            deskFragment = new DeskFragment();
        }
        if (musicFragment == null) {
            musicFragment = new MusicFragment();
        }
        if (videoFragment == null) {
            videoFragment = new VideoFragment();
        }
        if (pictrueFragment == null) {
            pictrueFragment = new PictrueFragment(manager);
        }
        if (onlineNeighborFragment == null) {
            onlineNeighborFragment = new OnlineNeighborFragment();
        }
    }

    private void initData() {
        et_search_view = (EditText) findViewById(R.id.search_view);
        tv_desk.setOnClickListener(this);
        tv_music.setOnClickListener(this);
        tv_video.setOnClickListener(this);
        tv_computer.setOnClickListener(this);
        tv_picture.setOnClickListener(this);
        tv_net_service.setOnClickListener(this);
        iv_list_view.setOnClickListener(this);
        iv_grid_view.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        iv_setting.setOnClickListener(this);

        tv_computer.performClick();
//        search_view.addTextChangedListener(new EditTextChangeListener(manager,MainActivity.this));
        et_search_view.setOnEditorActionListener(new SearchOnEditorActionListener(manager,
                                                 et_search_view.getText(), MainActivity.this));
        iv_search_view.setOnClickListener(new SearchOnClickListener(manager,
                                          et_search_view.getText(), MainActivity.this));
    }

    @Override
    protected void onStart() {
        receiver.registerReceiver();
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
        handler.sendMessage(msg);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mIsMutiSelect = false;
        if (!mIsMutiSelect && !isFirst){
            sendBroadcastMessage("is_ctrl_press", null, mIsMutiSelect);
            isFirst = true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL && !et_search_view.hasFocus()) {
            onBackPressed();
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            L.e("KEYCODE_ENTER", "KEYCODE_ENTER");
        }
        if (event.isCtrlPressed()) {
            mIsMutiSelect = true;
        }
        if (mIsMutiSelect && isFirst) {
            sendBroadcastMessage("is_ctrl_press", null, mIsMutiSelect);
            isFirst = false;
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
                startAndSettingFragment(R.id.tv_desk, manager, deskFragment);
                break;
            case R.id.tv_music:
                startAndSettingFragment(R.id.tv_music, manager, musicFragment);
                break;
            case R.id.tv_video:
                startAndSettingFragment(R.id.tv_video, manager, videoFragment);
                break;
            case R.id.tv_picture:
                startAndSettingFragment(R.id.tv_picture, manager, pictrueFragment);
                break;
            case R.id.tv_computer:
                startAndSettingFragment(R.id.tv_computer, manager, sdStorageFragment);
                break;
            case R.id.tv_storage:
                setSelectedBackground(R.id.tv_storage);
                SystemSpaceFragment usbStorageFragment = new SystemSpaceFragment
                                                         (USB_SPACE_FRAGMENT, usbs[0], null, null);
                manager.beginTransaction().replace(R.id.fl_mian, usbStorageFragment).commit();
                break;
            case R.id.tv_net_service:
                startAndSettingFragment(R.id.tv_net_service, manager, onlineNeighborFragment);
                break;
//            case R.id.iv_menu:
//                if (manager.getBackStackEntryCount() < 1) {
//                    T.showShort(MainActivity.this,
//                                getResources().getString(R.string.operation_not_support));
//                } else {
//                    shownPopWidndow("iv_menu");
//                }
//                break;
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_setting:
                shownPopWidndow("iv_setting");
                break;
            case R.id.iv_grid_view:
                iv_grid_view.setSelected(true);
                iv_list_view.setSelected(false);
                if (!"grid".equals(LocalCache.getViewTag())
                    || "grid".equals(LocalCache.getViewTag())) {
                    LocalCache.setViewTag("grid");
                    T.showShort(MainActivity.this, getResources().getString(R.string.grid_view));
                }
                sendBroadcastMessage("iv_switch_view", "grid", false);
                break;
            case R.id.iv_list_view:
                iv_grid_view.setSelected(false);
                iv_list_view.setSelected(true);
                if (!"list".equals(LocalCache.getViewTag())
                    || "list".equals(LocalCache.getViewTag())) {
                    LocalCache.setViewTag("list");
                    T.showShort(MainActivity.this, getResources().getString(R.string.list_view));
                }
                sendBroadcastMessage("iv_switch_view", "list", false);
                break;
        }
    }

    private void startAndSettingFragment(int id, FragmentManager manager, Fragment fragment) {
        setSelectedBackground(id);
        manager.beginTransaction().replace(R.id.fl_mian, fragment).commit();
    }

    private void setSelectedBackground(int id) {
        switch (id) {
            case R.id.tv_computer:
                tv_music.setSelected(false);
                tv_desk.setSelected(false);
                tv_video.setSelected(false);
                tv_computer.setSelected(true);
                tv_picture.setSelected(false);
                tv_storage.setSelected(false);
                tv_net_service.setSelected(false);
                break;
            case R.id.tv_desk:
                tv_desk.setSelected(true);
                tv_music.setSelected(false);
                tv_video.setSelected(false);
                tv_computer.setSelected(false);
                tv_picture.setSelected(false);
                tv_storage.setSelected(false);
                tv_net_service.setSelected(false);
                break;
            case R.id.tv_music:
                tv_music.setSelected(true);
                tv_desk.setSelected(false);
                tv_video.setSelected(false);
                tv_computer.setSelected(false);
                tv_picture.setSelected(false);
                tv_storage.setSelected(false);
                tv_net_service.setSelected(false);
                break;
            case R.id.tv_video:
                tv_music.setSelected(false);
                tv_desk.setSelected(false);
                tv_video.setSelected(true);
                tv_computer.setSelected(false);
                tv_picture.setSelected(false);
                tv_storage.setSelected(false);
                tv_net_service.setSelected(false);
                break;
            case R.id.tv_picture:
                tv_music.setSelected(false);
                tv_desk.setSelected(false);
                tv_video.setSelected(false);
                tv_computer.setSelected(false);
                tv_picture.setSelected(true);
                tv_storage.setSelected(false);
                tv_net_service.setSelected(false);
                break;
            case R.id.tv_storage:
                tv_music.setSelected(false);
                tv_desk.setSelected(false);
                tv_video.setSelected(false);
                tv_computer.setSelected(false);
                tv_picture.setSelected(false);
                tv_storage.setSelected(true);
                tv_net_service.setSelected(false);
                break;
            case R.id.tv_net_service:
                tv_music.setSelected(false);
                tv_desk.setSelected(false);
                tv_video.setSelected(false);
                tv_computer.setSelected(false);
                tv_picture.setSelected(false);
                tv_storage.setSelected(false);
                tv_net_service.setSelected(true);
                break;
            default:
                break;
        }
    }

    private void sendBroadcastMessage(String name, String tag, boolean isCtrl) {
        Intent intent = new Intent();
        switch (name) {
            case "iv_switch_view":
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
        popWinShare = null;
        PopOnClickLintener paramOnClickListener = new PopOnClickLintener(menu_tag,
                                                      MainActivity.this, manager);
//        if (menu_tag.equals("iv_menu")) {
//            popWinShare = new PopWinShare(MainActivity.this, paramOnClickListener,
//                    DisplayUtil.dip2px(MainActivity.this, 125),
//                                       DisplayUtil.dip2px(MainActivity.this, 260), menu_tag);
//            popWinShare.setFocusable(true);
//            popWinShare.showAsDropDown(this.iv_menu, -60, 10);
//        } else
        if (menu_tag.equals("iv_setting")) {
            popWinShare = new PopWinShare(MainActivity.this, paramOnClickListener,
                    DisplayUtil.dip2px(MainActivity.this, 120),
                                       DisplayUtil.dip2px(MainActivity.this, 160), menu_tag);
            popWinShare.setFocusable(true);
            popWinShare.showAsDropDown(this.iv_setting, -15, 10);
        }
        popWinShare.update();
        popWinShare.getContentView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    popWinShare.dismiss();
                }
            }
        });
    }

    public void DismissPopwindow() {
        popWinShare.dismiss();
    }

    @Override
    public void onBackPressed() {
        manager.findFragmentById(R.id.fl_mian);
        if ((curFragment != null) && (curFragment == sdStorageFragment)) {
            if (sdStorageFragment.canGoBack()) {
                sdStorageFragment.goBack();
            } else {
                if (manager.getBackStackEntryCount() >= 1) {
                    manager.popBackStack();
                } else {
                    finish();
                }
            }
            et_nivagation.setText("");
        } else {
            if (manager.getBackStackEntryCount() >= 1) {
                manager.popBackStack();
            } else {
                finish();
            }
        }
    }

    public interface IBackPressedListener {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiver.unregisterReceiver();
    }

    @Override
    public void setNavigationBar(String displayPath) {
        if (displayPath != null) {
            et_nivagation.setText(displayPath);
        }
    }
}
