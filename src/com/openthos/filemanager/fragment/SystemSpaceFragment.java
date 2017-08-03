package com.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaScannerConnection;

import com.openthos.filemanager.BaseFragment;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.component.FrameSelectView;
import com.openthos.filemanager.drag.DragGridView;
import com.openthos.filemanager.drag.DragListView;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileCategoryHelper;
import com.openthos.filemanager.system.FileIconHelper;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileListAdapter;
import com.openthos.filemanager.system.FileSortHelper;
import com.openthos.filemanager.system.FileViewInteractionHub;
import com.openthos.filemanager.system.FileOperationHelper;
import com.openthos.filemanager.system.IFileInteractionListener;
import com.openthos.filemanager.system.Settings;
import com.openthos.filemanager.system.Util;
import com.openthos.filemanager.utils.L;
import com.openthos.filemanager.utils.LocalCache;
import com.openthos.filemanager.utils.T;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

public class SystemSpaceFragment extends BaseFragment implements
        IFileInteractionListener, MainActivity.IBackPressedListener {
    private static final String TAG = SystemSpaceFragment.class.getSimpleName();
    public static final String ROOT_DIRECTORY = "root_directory";
    private FileListAdapter mAdapter;
    private FileCategoryHelper mFileCagetoryHelper;
    private FileIconHelper mFileIconHelper;
    private ArrayList<FileInfo> mFileNameList = new ArrayList<>();
    private List<FileInfo> mFileListInfo;
    private Activity mActivity;
    private MainActivity mMainActivity;
    //    private View view;
    private DragListView file_path_list;
    private DragGridView file_path_grid;
    private FrameLayout mFragmentSysFl;
    private static final String sdDir = Util.getSdDirectory();
    //    private String sdOrSystem;
//    private String directorPath;
    private String curRootDir = "";
    //    private ArrayList<FileInfo> fileInfoList = null;
//    FileViewInteractionHub.CopyOrMove copyOrMove = null;
    private boolean isCtrlPress;
    private String mouseRightTag = "mouse";
    private boolean isDialogShow = false;
    private boolean isShow = false;
    private boolean mIsLeftItem;

    private boolean mIsShowDialog = false;
    private boolean mIsItem = false;
    private boolean mIsMove;
    private MotionEvent mEvent;
    private boolean mIsLongPress;
    // Selected files list
    private List<Integer> mIntegerList = new ArrayList<>();

    // memorize the scroll positions of previous paths
    private ArrayList<PathScrollPositionItem> mScrollPositionList = new ArrayList<>();
    private String mPreviousPath;
    private boolean mSdCardReady;
    private View mEmptyView;
    private View mNoSdView;
    private HashMap<Enum, Boolean> mSortMap;
    private long mCurrentTime;
    public int mPos = -1;
    private boolean mNamePositive = true;
    private boolean mSizePositive = true;
    private boolean mDatePositive = true;
    private boolean mTypePositive = true;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case "com.switchview":
                    if (null != intent.getExtras().getString("switch_view")) {
                        String switch_view = intent.getExtras().getString("switch_view");
                        selectorMenuId(switch_view);
                    }
                    break;
                case "com.switchmenu":
                    if (null != intent.getExtras().getString("pop_menu")) {
                        String pop_menu = intent.getExtras().getString("pop_menu");
                        //selectorMenuId(pop_menu);
                    }
                    break;
                case "com.isCtrlPress":
                    isCtrlPress = intent.getExtras().getBoolean("is_ctrl_press");
                    break;
                case Intent.ACTION_MEDIA_MOUNTED:
                case Intent.ACTION_MEDIA_UNMOUNTED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI();
                        }
                    });
                    break;
            }
        }
    };
    private ViewOnGenericMotionListener mViewMotionListener;
    private FrameSelectView mFrameSelectView;

    private int GRID_LEFT_POS = 0;
    private int GRID_TOP_POS = 1;
    private int GRID_WIDTH_POS = 2;
    private int GRID_VERSPACE_POS = 3;
    private int GRID_NUMCOLUMNS_POS = 4;
    private int GRID_HORSPACE_POS = 5;
    private int ADAPTER_WIDTH_POS = 0;
    private int ADAPTER_HEIGHT_POS = 1;
    private int mLastClickId;
    private long mLastClickTime = 0;
    private int mShiftPos = -1;

    private TextView mInfo;
    private String mInfoText = "";

    private void selectorMenuId(String tag) {
        if (mFileViewInteractionHub.getSelectedFileList() != null) {

        }
        switch (tag) {
            case "pop_refresh":
                mFileViewInteractionHub.onOperationReferesh();
                break;
            case "pop_cancel_all":
                mFileViewInteractionHub.onOperationSelectAllOrCancel();
                break;
            case "pop_copy":
                if (mFileViewInteractionHub.getSelectedFileList() != null) {
                    mFileViewInteractionHub.doOnOperationCopy();
                }
                T.showShort(mActivity, getString(R.string.select_file_to_copy));
                break;
            case "pop_send":
                if (mFileViewInteractionHub.getSelectedFileList() != null) {
                    mFileViewInteractionHub.onOperationSend();
                }
                T.showShort(mActivity, getString(R.string.select_file_to_send));
                break;
            case "pop_create":
                mFileViewInteractionHub.onOperationCreateFolder();
                break;
            case "view_or_dismiss":
                mFileViewInteractionHub.onOperationShowSysFiles();
                break;
            case "pop_cut":
                mFileViewInteractionHub.onOperationMove();
                break;
            case "grid":
            case "list":
                switchMode();
                mFileViewInteractionHub.clearSelection();
                break;
        }
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public SystemSpaceFragment(String sdSpaceFragment, String directPath,
                               ArrayList<FileInfo> fileInfoList,
                               FileViewInteractionHub.CopyOrMove mCopyOrMove, boolean isLeftItem) {
        super(sdSpaceFragment, directPath, fileInfoList, mCopyOrMove);
        mIsLeftItem = isLeftItem;
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public SystemSpaceFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public int getLayoutId() {
        return R.layout.system_fragment_layout;
    }

    protected void initView() {
        mActivity = getActivity();
        mMainActivity = (MainActivity) getActivity();
        mFragmentSysFl = (FrameLayout) rootView.findViewById(R.id.fragment_sys_fl);
        mFrameSelectView = new FrameSelectView(mMainActivity);
        mFragmentSysFl.addView(mFrameSelectView);
        mEmptyView = rootView.findViewById(R.id.empty_view);
        mSdCardReady = Util.isSDCardReady();
        mNoSdView = rootView.findViewById(R.id.sd_not_available_page);
        file_path_list = (DragListView) rootView.findViewById(R.id.file_path_list);
        file_path_grid = (DragGridView) rootView.findViewById(R.id.file_path_grid);
        mInfo = (TextView) rootView.findViewById(R.id.info);
        initSortMap();
    }

    private void initSortMap() {
        mSortMap = new HashMap<>();
        mSortMap.put(FileSortHelper.SortMethod.name, mNamePositive);
        mSortMap.put(FileSortHelper.SortMethod.size, mSizePositive);
        mSortMap.put(FileSortHelper.SortMethod.date, mDatePositive);
        mSortMap.put(FileSortHelper.SortMethod.type, mTypePositive);
    }

    protected void initData() {
        mFileCagetoryHelper = new FileCategoryHelper(mActivity);
        mFileViewInteractionHub = new FileViewInteractionHub(this);
        Intent intent = getActivity().getIntent();
        mFileIconHelper = new FileIconHelper(mActivity);
        mViewMotionListener = new ViewOnGenericMotionListener();
        if ("list".equals(LocalCache.getViewTag())) {
            addHeadView(mActivity);
            mAdapter = new FileListAdapter(mActivity, R.layout.file_browser_item_list,
                    mFileNameList, mFileViewInteractionHub, mFileIconHelper, mViewMotionListener);
        } else if ("grid".equals(LocalCache.getViewTag())) {
            mAdapter = new FileListAdapter(mActivity, R.layout.file_browser_item_grid,
                    mFileNameList, mFileViewInteractionHub, mFileIconHelper, mViewMotionListener);
        }
        mFileViewInteractionHub.setRootPath(directorPath);
        if (!mIsLeftItem) {
            mMainActivity.setCurPath(directorPath);
        }
        curRootDir = directorPath;

        if (mFileInfoList != null && mFileInfoList.size() > 0) {
            mFileViewInteractionHub.setCheckedFileList(mFileInfoList, mCopyOrMove);
        }
        initReciever();
//        updateUI();
        mFileViewInteractionHub.initFileList();
        operatorData();
        setHasOptionsMenu(true);
        mFileListInfo = mAdapter.getFileInfoList();
    }

    private void switchMode() {
        if ("list".equals(LocalCache.getViewTag())) {
            addHeadView(mActivity);
            mAdapter = new FileListAdapter(mActivity, R.layout.file_browser_item_list,
                    mFileNameList, mFileViewInteractionHub, mFileIconHelper, mViewMotionListener);
        } else if ("grid".equals(LocalCache.getViewTag())) {
            mAdapter = new FileListAdapter(mActivity, R.layout.file_browser_item_grid,
                    mFileNameList, mFileViewInteractionHub, mFileIconHelper, mViewMotionListener);
        }
        operatorData();
    }

    private void addHeadView(Context context) {
        if (file_path_list.getHeaderViewsCount() == 0) {
            View headView =
                    LayoutInflater.from(context).inflate(R.layout.file_browser_item_list, null);
            ImageView lFileImage = (ImageView) headView.findViewById(R.id.file_image);
            Util.setText(headView, R.id.file_name,
                    context.getResources().getString(R.string.file_title_name),
                    context.getResources().getColor(R.color.file_title_color));
            Util.setText(headView, R.id.file_count,
                    context.getResources().getString(R.string.file_title_type),
                    context.getResources().getColor(R.color.file_title_color));
            Util.setText(headView, R.id.modified_time,
                    context.getResources().getString(R.string.file_title_modified),
                    context.getResources().getColor(R.color.file_title_color));
            Util.setText(headView, R.id.file_size,
                    context.getResources().getString(R.string.file_title_size),
                    context.getResources().getColor(R.color.file_title_color));
            lFileImage.setVisibility(View.GONE);
            file_path_list.addHeaderView(headView);
        }
    }

    @Override
    protected void initListener() {
        file_path_grid.setOnTouchListener(mViewMotionListener);
        file_path_list.setOnTouchListener(mViewMotionListener);
    }

    public View getFirstVisibleView() {
        if ("grid".equals(LocalCache.getViewTag())) {
            return file_path_grid.getChildAt(0);
        } else {
            return file_path_list.getChildAt(0);
        }
    }

    @Override
    public void processDirectionKey(int keyCode) {
        int size = mFileNameList.size();
        if (mPos < size && size != 0) {
            List integerList = mAdapter.getSelectFileInfoList();
            integerList.clear();
            mFileViewInteractionHub.clearSelection();
            if (mPos == -1) {
                mPos = 0;
            } else {
                boolean isGrid = "grid".equals(LocalCache.getViewTag());
                int numColumns = file_path_grid.getNumColumns();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (isGrid) {
                            mPos = isGrid && mPos > 0 ? mPos - 1 : mPos;
                        } else {
                            mPos = mPos > 0 ? mPos - 1 : mPos;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (isGrid) {
                            mPos = mPos > numColumns - 1 ? mPos - numColumns : mPos;
                        } else {
                            mPos = mPos > 0 ? mPos - 1 : mPos;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (isGrid) {
                            mPos = isGrid && mPos < size - 1 ? mPos + 1 : mPos;
                        } else {
                            mPos = mPos < size - 1 ? mPos + 1 : mPos;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (isGrid) {
                            mPos = mPos < size - numColumns ? mPos + numColumns :
                                    mPos < size - size % numColumns ? size - 1 : mPos;
                        } else {
                            mPos = mPos < size - 1 ? mPos + 1 : mPos;
                        }
                        break;
                }
                if (isGrid) {
                    int firstVisiblePos = file_path_grid.getFirstVisiblePosition();
                    int lastVisiblePos = file_path_grid.getLastVisiblePosition();
                    if (mPos < numColumns
                            || (mPos <= size - 1 && mPos >= size - (size % numColumns))) {
                        if (size > lastVisiblePos - firstVisiblePos + 1) {
                            file_path_grid.setSelection(mPos);
                        } else {
                            file_path_grid.smoothScrollToPosition(mPos);
                        }
                    } else if (mPos < firstVisiblePos
                            || mPos > lastVisiblePos - numColumns) {
                        file_path_grid.setSelection(mPos);
                    }
                } else {
                    if (mPos == 0 || mPos == size - 1) {
                        file_path_list.smoothScrollToPosition(mPos);
                    } else if (mPos < file_path_list.getFirstVisiblePosition()
                            || mPos > file_path_list.getLastVisiblePosition() - 3) {
                        file_path_list.setSelection(mPos);
                    }
                }
            }
            FileInfo fileInfo = mAdapter.getFileInfoList().get(mPos);
            fileInfo.Selected = true;
            integerList.add(mPos);
            mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
            onDataChanged();
        }
    }

    @Override
    public void showMenu() {
        FileInfo fileInfo = null;
        int compressFileState = Constants.COMPRESSIBLE;
        if (mPos != -1) {
            mShiftPos = mPos;
            fileInfo = mAdapter.getFileInfoList().get(mPos);
            if (!mFileViewInteractionHub.getSelectedFileList().contains(fileInfo)) {
                fileInfo.Selected = true;
                mIntegerList.clear();
                mFileViewInteractionHub.clearSelection();
                mIntegerList.add(mPos);
                mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
            }
        } else {
            mIntegerList.clear();
            mFileViewInteractionHub.clearSelection();
        }
        if (mIsItem) {
            boolean isDirectory = true;
            if (fileInfo != null) {
                File file = new File(fileInfo.filePath);
                isDirectory = file.isDirectory() ? true : false;
                compressFileState = Util.getCompressFileState(fileInfo.filePath);
            }
            mFileViewInteractionHub.setIsBlank(false);
            mFileViewInteractionHub.setIsDirectory(isDirectory);
        } else {
            mFileViewInteractionHub.setIsBlank(true);
            mFileViewInteractionHub.setIsDirectory(true);
        }
        ArrayList<FileInfo> selectedFile = mFileViewInteractionHub.getSelectedFileList();
        if (selectedFile.size() != 0) {
            for (FileInfo info : selectedFile) {
                mFileViewInteractionHub.setIsProtected(false);
                if (!info.canWrite) {
                    mFileViewInteractionHub.setIsProtected(true);
                    break;
                }
            }
        } else {
            mFileViewInteractionHub.setIsProtected(
                    !new File(mFileViewInteractionHub.getCurrentPath()).canWrite());
        }
        mFileViewInteractionHub.setCompressFileState(compressFileState);
        mFileViewInteractionHub.showContextDialog(mFileViewInteractionHub, mEvent);
        mIsMove = false;
        mIsShowDialog = false;
        mIsLongPress = true;
        onDataChanged();
    }

    public class ViewOnGenericMotionListener implements View.OnTouchListener {
        private float mDownX = -1;
        private float mDownY, mMoveX, mMoveY;
        private List<Integer> list = new ArrayList<>();
        private FileInfo info;
        private long lastTime = -1;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMainActivity.mCurTabIndext = 9;
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mMainActivity.clearNivagateFocus();
                    mIntegerList = mAdapter.getSelectFileInfoList();
                    mEvent = motionEvent;
                    if (motionEvent.getButtonState() == MotionEvent.BUTTON_PRIMARY) {
                        mMainActivity.mHandler.postDelayed(mMainActivity.mLongPressRunnable,
                                Constants.LONG_PRESS_TIME);
                        mIsLongPress = false;
                    }
                    if (motionEvent.getButtonState() == MotionEvent.BUTTON_TERTIARY) {
                        mMainActivity.onUp();
                        lastTime = -1;
                        return false;
                    } else {
                        lastTime = System.currentTimeMillis();
                    }
                    if ("grid".equals(LocalCache.getViewTag())) {
                        calculateFileGridLocation(file_path_grid.getVerticalScrollDistance());
                    } else {
                        calculateFileListLocation(file_path_list.getVerticalScrollDistance());
                    }
                    if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                        mIsShowDialog = true;
                    }
                    if (view.getTag() instanceof FileListAdapter.ViewHolder
                            || view.getId() == R.id.file_name) {
                        mDownX = -1;
                        mIsItem = true;
                        if (view.getId() == R.id.file_name) {
                            mPos = (int) view.getTag();
                        } else {
                            mPos = (int) ((FileListAdapter.ViewHolder) view.getTag()).name.getTag();
                        }
                        FileInfo fileInfo = mAdapter.getFileInfoList().get(mPos);
                        if (!MainActivity.getCtrlState() && !MainActivity.getShiftState()
                                && mIsShowDialog != true && mLastClickId == mPos
                                && (Math.abs(System.currentTimeMillis() - mLastClickTime)
                                < Constants.DOUBLE_CLICK_INTERVAL_TIME)) {
                            if (isRecycle()) {
                                Toast.makeText(mMainActivity, getString(R.string.fail_open_recycle),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                enter(motionEvent);
                                mPos = -1;
                                mLastClickId = -1;
                                mIntegerList.clear();
                                mFileViewInteractionHub.clearSelection();
                            }
                        } else {
                            mLastClickTime = System.currentTimeMillis();
                            mLastClickId = mPos;
                        }
                        return true;
                    } else {
                        if (!MainActivity.getCtrlState() && !MainActivity.getShiftState()) {
                            mDownX = motionEvent.getX();
                            mDownY = motionEvent.getY();
                        }
                        mIsItem = false;
                        mPos = -1;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mIsLongPress) {
                        mMainActivity.mHandler.removeCallbacks(mMainActivity.mLongPressRunnable);
                        lastTime = -1;
                    }
                    if ((mIsShowDialog || mIsLongPress || MainActivity.getCtrlState()
                            || MainActivity.getShiftState()) && !mIsMove) {
                        return true;
                    }
                    if (mDownX != -1 && !mIsItem) {
                        mIsMove = true;
                        mMoveX = motionEvent.getX();
                        mMoveY = motionEvent.getY();
                        mFrameSelectView.setPositionCoordinate(
                                mDownX < mMoveX ? mDownX : mMoveX,
                                mDownY < mMoveY ? mDownY : mMoveY,
                                mDownX > mMoveX ? mDownX : mMoveX,
                                mDownY > mMoveY ? mDownY : mMoveY);
                        mFrameSelectView.invalidate();
                        int i;
                        mIntegerList.clear();
                        for (i = 0; i < mFileListInfo.size(); i++) {
                            info = mFileListInfo.get(i);
                            if (frameSelectionJudge(info, mDownX, mDownY, mMoveX, mMoveY)) {
                                info.Selected = true;
                                mIntegerList.add(i);
                            }
                        }
                        if (!(list.containsAll(mIntegerList)
                                && list.size() == mIntegerList.size())) {
                            mAdapter.notifyDataSetChanged();
                        }
                        list.clear();
                        list.addAll(mIntegerList);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    mFrameSelectView.setPositionCoordinate(-1, -1, -1, -1);
                    mFrameSelectView.invalidate();
                    FileInfo fileInfo = null;
                    if (!mIsLongPress) {
                        mMainActivity.mHandler.removeCallbacks(mMainActivity.mLongPressRunnable);
                        lastTime = -1;
                    }
                    if (mIsShowDialog) {
                        showMenu();
                    } else {
                        if (mPos != -1) {
                            fileInfo = mAdapter.getFileInfoList().get(mPos);
                            fileInfo.Selected = true;
                            if (MainActivity.getShiftState()) {
                                if (mShiftPos == -1) {
                                    mShiftPos = mPos;
                                }
                                mIntegerList.clear();
                                mIntegerList.add(mPos);
                                mFileViewInteractionHub.clearSelection();
                                mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
                                if (mShiftPos != mPos) {
                                    for (int i = Math.min(mShiftPos, mPos); i <= Math.max(mShiftPos, mPos); i++) {
                                        fileInfo = mAdapter.getFileInfoList().get(i);
                                        fileInfo.Selected = true;
                                        if (i != mPos) {
                                            mIntegerList.add(i);
                                            mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
                                        }
                                    }
                                }
                            } else if (MainActivity.getCtrlState()) {
                                mShiftPos = mPos;
                                if (!mIntegerList.contains(mPos)) {
                                    mIntegerList.add(mPos);
                                    mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
                                } else {
                                    mIntegerList.remove(new Integer(mPos));
                                    mFileViewInteractionHub.removeDialogSelectedItem(fileInfo);
                                }
                            } else if (!mIntegerList.contains(mPos) || mIntegerList.size() > 1) {
                                mShiftPos = mPos;
                                mIntegerList.clear();
                                mIntegerList.add(mPos);
                                mFileViewInteractionHub.clearSelection();
                                mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
                            }
                        } else {
                            if (!MainActivity.getCtrlState() && !MainActivity.getShiftState()) {
                                mIntegerList.clear();
                                mFileViewInteractionHub.clearSelection();
                                mShiftPos = -1;
                            }
                        }
                    }
                    if (mIsMove) {
                        mIsMove = false;
                        float upX = motionEvent.getX();
                        float upY = motionEvent.getY();
                        FileInfo info;
                        for (int i = 0; i < mFileListInfo.size(); i++) {
                            info = mFileListInfo.get(i);
                            if (frameSelectionJudge(info, mDownX, mDownY, upX, upY)) {
                                info.Selected = true;
                                if (!mIntegerList.contains(i)) {
                                    mIntegerList.add(i);
                                    mFileViewInteractionHub.addDialogSelectedItem(info);
                                } else {
                                    mIntegerList.remove(new Integer(i));
                                    mFileViewInteractionHub.removeDialogSelectedItem(info);
                                }
                            }
                        }
                    }
                    onDataChanged();
                    mDownX = -1;
                    mIsItem = false;
            }
            return false;
        }
    }

    private boolean frameSelectionJudge(FileInfo info, float downX, float downY,
                                        float toX, float toY) {
        return (((info.left >= Math.min(downX, toX) && info.left <= Math.max(downX, toX))
                || (info.right >= Math.min(downX, toX) && info.right <= Math.max(downX, toX)))
                && ((info.top >= Math.min(downY, toY) && info.top <= Math.max(downY, toY))
                || (info.bottom >= Math.min(downY, toY) && info.bottom <= Math.max(downY, toY))))
                || (((info.left <= Math.min(downX, toX) && info.right >= Math.max(downX, toX))
                && ((info.top >= Math.min(downY, toY) && info.top <= Math.max(downY, toY))
                || (info.bottom >= Math.min(downY, toY) && info.bottom <= Math.max(downY, toY))))
                || ((info.top <= Math.min(downY, toY) && info.bottom >= Math.max(downY, toY))
                && ((info.left >= Math.min(downX, toX) && info.left <= Math.max(downX, toX))
                || (info.right >= Math.min(downX, toX) && info.right <= Math.max(downX, toX)))));
    }

    private void operatorData() {
        if ("list".equals(LocalCache.getViewTag())) {
            file_path_grid.setVisibility(View.GONE);
            file_path_list.setVisibility(View.VISIBLE);
            file_path_list.setAdapter(mAdapter);
        } else if ("grid".equals(LocalCache.getViewTag())) {
            file_path_list.setVisibility(View.GONE);
            file_path_grid.setVisibility(View.VISIBLE);
            file_path_grid.setAdapter(mAdapter);
        }
    }

    private void initReciever() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.switchview");
        intentFilter.addAction("com.switchmenu");
        intentFilter.addAction("com.isTouchEvent");
        intentFilter.addAction("com.isCtrlPress");
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        mActivity.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.unregisterReceiver(mReceiver);
        mAdapter.dispose();
    }

    public boolean onBack() {
        if (!Util.isSDCardReady() || mFileViewInteractionHub == null) {
            return false;
        }
        return mFileViewInteractionHub.onBackPressed();
    }

    private class PathScrollPositionItem {
        String path;
        int pos;

        PathScrollPositionItem(String s, int p) {
            path = s;
            pos = p;
        }
    }

    // execute before change, return the memorized scroll position
    private int computeScrollPosition(String path) {
        int pos = 0;
        if (mPreviousPath != null) {
            if (path.startsWith(mPreviousPath)) {
                int firstVisiblePosition = 0;
                if ("list".equals(LocalCache.getViewTag())) {
                    firstVisiblePosition = file_path_list.getFirstVisiblePosition();
                } else if ("grid".equals(LocalCache.getViewTag())) {
                    firstVisiblePosition = file_path_grid.getFirstVisiblePosition();
                }
                if (mScrollPositionList.size() != 0
                        && mPreviousPath.equals(mScrollPositionList
                        .get(mScrollPositionList.size() - 1).path)) {
                    mScrollPositionList.get(mScrollPositionList.size() - 1).pos
                            = firstVisiblePosition;
                    Log.i(TAG, "computeScrollPosition: update item: " + mPreviousPath + " "
                            + firstVisiblePosition + " stack count:" + mScrollPositionList.size());
                    pos = firstVisiblePosition;
                } else {
                    mScrollPositionList.add(new PathScrollPositionItem(mPreviousPath,
                            firstVisiblePosition));
                    Log.i(TAG, "computeScrollPosition: add item: " + mPreviousPath + " "
                            + firstVisiblePosition + " stack count:" + mScrollPositionList.size());
                }
            } else {
                int i;
                for (i = 0; i < mScrollPositionList.size(); i++) {
                    if (!path.startsWith(mScrollPositionList.get(i).path)) {
                        break;
                    }
                }
                // navigate to a totally new branch, not in current stack
                if (i > 0) {
                    pos = mScrollPositionList.get(i - 1).pos;
                }

                for (int j = mScrollPositionList.size() - 1; j >= i - 1 && j >= 0; j--) {
                    mScrollPositionList.remove(j);
                }
            }
        }

        Log.i(TAG, "computeScrollPosition: result pos: " + path + " "
                + pos + " stack count:" + mScrollPositionList.size());
        mPreviousPath = path;
        return pos;
    }

    public boolean onRefreshFileList(String path, FileSortHelper sort) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        final int pos = computeScrollPosition(path);
        ArrayList<FileInfo> fileList = mFileNameList;
        fileList.clear();

        File[] listFiles = file.listFiles(mFileCagetoryHelper.getFilter());
        if (listFiles == null)
            return true;

        int directoryCount = 0;
        int fileCount = 0;
        long fileSize = 0L;
        for (File child : listFiles) {
            // do not show selected file if in move state
            if (mFileViewInteractionHub.isMoveState()
                    && mFileViewInteractionHub.isFileSelected(child.getPath()))
                continue;

            String absolutePath = child.getAbsolutePath();
            if (Util.isNormalFile(absolutePath) && Util.shouldShowFile(absolutePath)) {
                FileInfo lFileInfo = Util.GetFileInfo(child, mFileCagetoryHelper.getFilter(),
                        Settings.instance().getShowDotAndHiddenFiles());
                if (lFileInfo != null) {
                    fileList.add(lFileInfo);
                    if (child.isDirectory()) {
                        directoryCount++;
                    } else {
                        fileCount++;
                        fileSize += child.length();
                    }
                }
            }
        }
        mInfoText = "";
        if (directoryCount != 0) {
            mInfoText += getString(R.string.folder_count, directoryCount);
        }
        if (fileCount != 0) {
            mInfoText += getString(R.string.file_count, fileCount);
            mInfoText += getString(R.string.file_count_zise, Util.convertStorage(fileSize));
        }
        if (TextUtils.isEmpty(mInfoText)) {
            mInfoText = getString(R.string.empty_folder);
        }

        sortCurrentList(sort);
        showEmptyView(fileList.size() == 0);
        if ("list".equals(LocalCache.getViewTag())) {
            file_path_list.post(new Runnable() {
                @Override
                public void run() {
                    file_path_list.setSelection(pos);
                }
            });
        } else if ("grid".equals(LocalCache.getViewTag())) {
            file_path_grid.post(new Runnable() {
                @Override
                public void run() {
                    file_path_grid.setSelection(pos);
                }
            });
        }
        mAdapter.getSelectFileInfoList().clear();
        mFileViewInteractionHub.getSelectedFileList().clear();
        onDataChanged();
        clearSelect();
        return true;
    }

    private void updateUI() {
        mNoSdView.setVisibility(mSdCardReady ? View.GONE : View.VISIBLE);
        if ("list".equals(LocalCache.getViewTag())) {
            file_path_list.setVisibility(mSdCardReady ? View.VISIBLE : View.GONE);
        } else if ("grid".equals(LocalCache.getViewTag())) {
            file_path_grid.setVisibility(mSdCardReady ? View.VISIBLE : View.GONE);
        }
        if (mSdCardReady) {
            mFileViewInteractionHub.refreshFileList();
        }
    }

    private void showEmptyView(boolean show) {
        View mEmptyView = rootView.findViewById(R.id.empty_view);
        if (mEmptyView != null)
            mEmptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public View getViewById(int id) {
        return rootView.findViewById(id);
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mFileViewInteractionHub.getSelectedFileList().size() == 0) {
                    mInfo.setText(mInfoText);
                } else if (mFileViewInteractionHub.getSelectedFileList().size() == 1) {
                    mInfo.setText(mFileViewInteractionHub.getSelectedFileList().get(0).fileName);
                    if (mFileViewInteractionHub.getSelectedFileList().get(0).IsDir) {
                        mInfo.append("\t" + mFileViewInteractionHub.getSelectedFileList()
                                .get(0).Count + " " + getString(R.string.items));
                    } else {
                        mInfo.append("\t" + Util.convertStorage(mFileViewInteractionHub
                                .getSelectedFileList().get(0).fileSize));
                    }
                } else {
                    mInfo.setText(getString(R.string.item_selected,
                            mFileViewInteractionHub.getSelectedFileList().size()));
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onPick(FileInfo f) {
        try {
            Intent intent = Intent.parseUri(Uri.fromFile(new File(f.filePath)).toString(), 0);
            mActivity.setResult(Activity.RESULT_OK, intent);
            mActivity.finish();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setWallpaper(FileInfo f) {
        final WallpaperManager wpm = WallpaperManager.getInstance(mMainActivity);
        Uri uri = Uri.fromFile(new File(f.filePath));
        String path = uri.getEncodedPath();
        if (path != null) {
            path = Uri.decode(path);
            ContentResolver cr = mMainActivity.getContentResolver();
            StringBuffer buff = new StringBuffer();
            buff.append("(")
                    .append(MediaStore.Images.ImageColumns.DATA)
                    .append("=")
                    .append("'" + path + "'")
                    .append(")");
            Cursor cur = cr.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.ImageColumns._ID},
                    buff.toString(), null, null);
            int index = 0;
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                index = cur.getInt(index);
            }
            cur.close();
            if (index != 0) {
                Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                if (uri_temp != null) {
                    uri = uri_temp;
                }
                Intent intent = wpm.getCropAndSetWallpaperIntent(uri);
                startActivity(intent);
                mMainActivity.finish();
            } else {
                MediaScannerConnection.scanFile(mMainActivity,
                        new String[]{f.filePath}, new String[]{"image/*"},
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                if (uri.getEncodedPath().startsWith("/external/images/media/")) {
                                    Intent intent = wpm.getCropAndSetWallpaperIntent(uri);
                                    startActivity(intent);
                                    mMainActivity.finish();
                                } else {
                                    mMainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(mMainActivity,
                                                    getString(R.string.check_right_image),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
            }
        }
    }

    @Override
    public boolean shouldShowOperationPane() {
        return true;
    }

    @Override
    public boolean onOperation(int id) {
        return false;
    }

    @Override
    public boolean onNavigation(String path) {
        return false;
    }

    @Override
    public boolean shouldHideMenu(int menu) {
        return false;
    }
    //TODO  copyFile

    public interface SelectFilesCallback {
        // files equals null indicates canceled
        void selected(ArrayList<FileInfo> files);
    }

    @Override
    public FileIconHelper getFileIconHelper() {
        return mFileIconHelper;
    }

    public boolean setPath(String location) {
        if (mFileViewInteractionHub != null) {
            if (!location.startsWith(mFileViewInteractionHub.getRootPath())) {
                return false;
            }
            mFileViewInteractionHub.setCurrentPath(location);
            mFileViewInteractionHub.refreshFileList();
        }
        return true;
    }

    @Override
    public FileInfo getItem(int pos) {
        if (pos < 0 || pos > mFileNameList.size() - 1)
            return null;
        return mFileNameList.get(pos);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void sortCurrentList(FileSortHelper sort) {
        if (mSortMap.get(sort.getSortMethod())) {
            Collections.sort(mFileNameList, sort.getComparator());
        } else {
            Collections.sort(mFileNameList, Collections.reverseOrder(sort.getComparator()));
        }
        onDataChanged();
    }

    @Override
    public ArrayList<FileInfo> getAllFiles() {
        return mFileNameList;
    }

    @Override
    public void addSingleFile(FileInfo file) {
        mFileNameList.add(file);
        onDataChanged();
    }

    @Override
    public int getItemCount() {
        return mFileNameList.size();
    }

    @Override
    public void runOnUiThread(Runnable r) {
        mMainActivity.runOnUiThread(r);
    }

    public boolean canGoBack() {
        String currentPath = mFileViewInteractionHub.getCurrentPath();
        return !currentPath.trim().equals(curRootDir.trim());
    }

    public void goBack() {
        mAdapter.getSelectFileInfoList().clear();
        mFileViewInteractionHub.clearSelection();
        mFileViewInteractionHub.onBackPressed();
    }

    public ArrayList<FileInfo> getFileInfoList() {
        return mFileViewInteractionHub.getCheckedFileList();
    }

    public FileViewInteractionHub.CopyOrMove getCurCopyOrMoveMode() {
        return mFileViewInteractionHub.getCurCopyOrMoveMode();
    }

    public String getCurrentPath() {
        return mFileViewInteractionHub.getCurrentPath();
    }

    public void refreshUI() {
        mFileViewInteractionHub.refreshFileList();
    }

    @Override
    public void enter() {
        enter(null);
    }

    public void enter(MotionEvent event) {
        mMainActivity.mHandler.removeCallbacks(mMainActivity.mLongPressRunnable);
        mFileViewInteractionHub.onOperationOpen(event);
    }

    @Override
    protected void enter(String tag, String path) {
    }

    public FileListAdapter getAdapter() {
        return mAdapter;
    }

    public FileViewInteractionHub getFileViewInteractionHub() {
        return mFileViewInteractionHub;
    }

    public void setSortTag(Enum sort, boolean positive) {
        mSortMap.put(sort, positive);
    }

    public boolean getSortTag(Enum sort) {
        return mSortMap.get(sort);
    }

    public void calculateFileGridLocation(int fixY) {
        int[] gridViewParams = file_path_grid.getParams();
        int[] itemParams = file_path_grid.getChildWidthAndHeight();
        for (int i = 0; i < mFileListInfo.size(); i++) {
            mFileListInfo.get(i).left = gridViewParams[GRID_LEFT_POS]
                    + (i % gridViewParams[GRID_NUMCOLUMNS_POS]) * (
                    gridViewParams[GRID_WIDTH_POS] + gridViewParams[GRID_HORSPACE_POS]);
            mFileListInfo.get(i).top = gridViewParams[GRID_TOP_POS]
                    + (i / gridViewParams[GRID_NUMCOLUMNS_POS])
                    * (itemParams[ADAPTER_HEIGHT_POS] + gridViewParams[GRID_VERSPACE_POS]) - fixY;
            mFileListInfo.get(i).right = gridViewParams[GRID_LEFT_POS]
                    + itemParams[ADAPTER_WIDTH_POS]
                    + (i % gridViewParams[GRID_NUMCOLUMNS_POS]) * (gridViewParams[GRID_WIDTH_POS]
                    + gridViewParams[GRID_HORSPACE_POS]);
            mFileListInfo.get(i).bottom = gridViewParams[GRID_TOP_POS]
                    + itemParams[ADAPTER_HEIGHT_POS] + (i / gridViewParams[GRID_NUMCOLUMNS_POS])
                    * (itemParams[ADAPTER_HEIGHT_POS] + gridViewParams[GRID_VERSPACE_POS]) - fixY;
        }
    }

    public void calculateFileListLocation(int fixY) {
        int[] listViewParams = file_path_list.getParams();
        int[] itemParams = file_path_list.getChildWidthAndHeight();
        for (int i = 0; i < mFileListInfo.size(); i++) {
            mFileListInfo.get(i).left = listViewParams[GRID_LEFT_POS];
            mFileListInfo.get(i).top = listViewParams[GRID_TOP_POS]
                    + (i + 1) * itemParams[ADAPTER_HEIGHT_POS] - fixY;
            mFileListInfo.get(i).right = listViewParams[GRID_WIDTH_POS]
                    - listViewParams[GRID_LEFT_POS];
            mFileListInfo.get(i).bottom = listViewParams[GRID_TOP_POS]
                    + itemParams[ADAPTER_HEIGHT_POS]
                    + (i + 1) * itemParams[ADAPTER_HEIGHT_POS] - fixY;
        }
    }

    public void clearSelect() {
        mShiftPos = -1;
        mPos = -1;
    }

    public boolean isRecycle() {
        return getCurrentPath().startsWith(FileOperationHelper.RECYCLE_PATH1)
                || getCurrentPath().startsWith(FileOperationHelper.RECYCLE_PATH2)
                || getCurrentPath().startsWith(FileOperationHelper.RECYCLE_PATH3);
    }
}
