package org.openthos.filemanager.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.openthos.filemanager.BaseFragment;
import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.R;
import org.openthos.filemanager.component.SearchOnKeyListener;
import org.openthos.filemanager.system.FileIconHelper;
import org.openthos.filemanager.system.FileInfo;
import org.openthos.filemanager.system.FileListItem;
import org.openthos.filemanager.system.FileSortHelper;
import org.openthos.filemanager.system.FileViewInteractionHub;
import org.openthos.filemanager.system.IFileInteractionListener;
import org.openthos.filemanager.system.IntentBuilder;
import org.openthos.filemanager.system.Util;
import org.openthos.filemanager.utils.IconHolder;
import org.openthos.filemanager.utils.LocalCache;
import org.openthos.filemanager.system.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SearchFragment extends BaseFragment implements IFileInteractionListener {
    private static final String TAG = Constants.LEFT_FAVORITES;
    private Fragment mCurFragment;
    private ListView mListView;
    private SearchAdapter mSearchAdapter;
    private MainActivity mActivity;
    private LinearLayout mLlEmptyView;
    private SearchOnTouchListener mOnTouchListener;
    private int mPosition;
    private List<Integer> mSelectedList;
    private SearchOnKeyListener mSearchOnKeyListener;
    private MotionEvent mMotionEvent;
    private int mLastClickPos = -1;
    private long mLastClickTime;
    private int mShiftPos;
    private boolean mIsRightButton;
    private boolean mIsItem;
    private ArrayList<FileInfo> mSearchList = new ArrayList<>();

    @SuppressLint({"NewApi", "ValidFragment"})
    public SearchFragment(SearchOnKeyListener listener, FragmentManager manager,
                          ArrayList<FileInfo> mFileList) {
        super(manager, mFileList);
        mActivity = (MainActivity) getActivity();
        mSearchOnKeyListener = listener;
    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public SearchFragment() {
        super();
    }

    @Override
    public int getLayoutId() {
        return R.layout.search_fragment_layout;
    }

    @Override
    protected void initView() {
        mListView = (ListView) rootView.findViewById(R.id.lv_mian_search);
        mLlEmptyView = (LinearLayout) rootView.findViewById(R.id.empty_view);
        if (mSearchList == null) {
            mListView.setVisibility(View.GONE);
            mLlEmptyView.setVisibility(View.VISIBLE);
        } else {
            mListView.setVisibility(View.VISIBLE);
            mLlEmptyView.setVisibility(View.GONE);
        }
    }

    protected void initData() {
        mFileViewInteractionHub = new FileViewInteractionHub(this);
        mSelectedList = new ArrayList<>();
        mSearchAdapter = new SearchAdapter();
        mListView.setAdapter(mSearchAdapter);
    }

    @Override
    protected void initListener() {
        mOnTouchListener = new SearchOnTouchListener();
        mListView.setOnTouchListener(mOnTouchListener);
    }


    private class SearchAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSearchList == null ? -1 : mSearchList.size();
        }

        @Override
        public Object getItem(int i) {
            return mSearchList == null ? -1 : mSearchList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (mSearchList != null) {
                ViewHolder viewHolder;
                if (view == null) {
                    view = View.inflate(getActivity(), R.layout.search_file_item, null);
                    viewHolder = new ViewHolder(view);
                    view.setTag(viewHolder);
                    view.setOnTouchListener(mOnTouchListener);
                }
                viewHolder = (ViewHolder) view.getTag();
                viewHolder.name.setTag(i);
                viewHolder.name.setText(mSearchList.get(i).fileName);
                String fileAbsolutePath = mSearchList.get(i).filePath;
                viewHolder.path.setText(fileAbsolutePath.substring(0,
                        fileAbsolutePath.lastIndexOf(Constants.ROOT_PATH)));
                mActivity = (MainActivity) getActivity();
                FileListItem.setupFileListItemInfo(mActivity, view, mSearchList.get(i),
                        IconHolder.getIconHolder(mActivity), mFileViewInteractionHub);
                RelativeLayout background = (RelativeLayout) view;
                background.setBackgroundResource(mSelectedList.contains(i) ?
                        R.drawable.list_item_bg_shape : R.color.white);
                return view;
            } else {
                return null;
            }
        }

        class ViewHolder {
            TextView name;
            TextView path;
            ImageView icon;

            public ViewHolder(View view) {
                name = (TextView) view.findViewById(R.id.search_file_name);
                path = (TextView) view.findViewById(R.id.search_file_path);
                icon = (ImageView) view.findViewById(R.id.file_image);
            }
        }
    }

    public void notifyModify() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSearchAdapter != null) {
                    mSearchAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private class SearchOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mMotionEvent = motionEvent;
                    if (view.getTag() instanceof SearchAdapter.ViewHolder) {
                        if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                            mIsRightButton = true;
                        }
                        mIsItem = true;
                        mPosition = (int) ((SearchAdapter.ViewHolder) view.getTag()).name.getTag();
                        if (mPosition == mLastClickPos
                                && !mIsRightButton
                                && (Math.abs(System.currentTimeMillis() - mLastClickTime)
                                < Constants.DOUBLE_CLICK_INTERVAL_TIME)) {
                            enter();
                            mLastClickPos = -1;
                            mSelectedList.clear();
                            mFileViewInteractionHub.clearSelection();
                        } else {
                            mLastClickTime = System.currentTimeMillis();
                        }
                    } else {
                        mIsItem = false;
                        mPosition = -1;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    FileInfo fileInfo = null;
                    if (mIsRightButton) {
                        showMenu();
                    } else {
                        if (mPosition != -1) {
                            fileInfo = mSearchList.get(mPosition);
                            fileInfo.Selected = true;
                            if (MainActivity.getCtrlState()) {
                                mShiftPos = mPosition;
                                if (!mSelectedList.contains(mPosition)) {
                                    mSelectedList.add(mPosition);
                                    mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
                                } else {
                                    mSelectedList.remove(new Integer(mPosition));
                                    mFileViewInteractionHub.removeDialogSelectedItem(fileInfo);
                                }
                            } else if (MainActivity.getShiftState()) {
                                if (mShiftPos == -1) {
                                    mShiftPos = mPosition;
                                }
                                mSelectedList.clear();
                                mSelectedList.add(mPosition);
                                mFileViewInteractionHub.clearSelection();
                                mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
                                if (mShiftPos != mPosition) {
                                    for (int i = Math.min(mShiftPos, mPosition);
                                            i <= Math.max(mShiftPos, mPosition); i++) {
                                        fileInfo = mSearchList.get(i);
                                        fileInfo.Selected = true;
                                        if (i != mPosition) {
                                            mSelectedList.add(i);
                                            mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
                                        }
                                    }
                                }
                            } else if (!mSelectedList.contains(mPosition)
                                    || mSelectedList.size() > 1) {
                                mShiftPos = mPosition;
                                mSelectedList.clear();
                                mSelectedList.add(mPosition);
                                mFileViewInteractionHub.clearSelection();
                                mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
                            }
                        } else {
                            if (!MainActivity.getCtrlState() && !MainActivity.getShiftState()) {
                                mSelectedList.clear();
                                mFileViewInteractionHub.clearSelection();
                                mShiftPos = -1;
                            }
                        }
                    }

                    mSearchAdapter.notifyDataSetChanged();
                    mLastClickPos = mPosition;
                    mIsItem = false;
                    break;
            }
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalCache.setSearchText(null);
    }

    @Override
    public boolean canGoBack() {
        return false;
    }

    @Override
    public void goBack() {
    }

    @Override
    public void enter() {
        if (mSelectedList.size() != 0) {
            String fileRealPath = mSearchList.get(mPosition).filePath;
            enter(null, fileRealPath);
        }
    }

    @Override
    public void enter(String tag, String path) {
        if (!new File(path).isDirectory()) {
            Context context = getActivity();
            try {
                IntentBuilder.viewFile(context, path, null);
            } catch (Exception e) {
                Toast.makeText(context, getString(
                        R.string.found_no_corresponding_application_to_open),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            if (mCurFragment != null) {
                mManager.beginTransaction().remove(mCurFragment).commitAllowingStateLoss();
            }
            mActivity = (MainActivity) getActivity();
            mManager.beginTransaction().hide(mActivity.getVisibleFragment()).commitAllowingStateLoss();
            mCurFragment = new SystemSpaceFragment(TAG, path, null, null, false);
            mManager.beginTransaction().add(R.id.fl_mian, mCurFragment,
                    Constants.SEARCHSYSTEMSPACE_TAG).commitAllowingStateLoss();
            mActivity.mCurFragment = mCurFragment;
            notifyModify();
        }
    }

    @Override
    public void processDirectionKey(int keyCode) {
    }

    @Override
    public void showMenu() {
        FileInfo fileInfo = null;
        int compressFileState = Constants.COMPRESSIBLE;
        if (mPosition != -1) {
            mShiftPos = mPosition;
            fileInfo = mSearchList.get(mPosition);
            if (!mFileViewInteractionHub.getSelectedFileList().contains(fileInfo)) {
                fileInfo.Selected = true;
                mSelectedList.clear();
                mFileViewInteractionHub.clearSelection();
                mSelectedList.add(mPosition);
                mFileViewInteractionHub.addDialogSelectedItem(fileInfo);
            }
        } else {
            mSelectedList.clear();
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
        }
        mFileViewInteractionHub.setCompressFileState(compressFileState);
        mFileViewInteractionHub.showContextDialog(mFileViewInteractionHub, mMotionEvent);
        mIsRightButton = false;
        onDataChanged();
    }

    @Override
    public View getViewById(int id) {
        return rootView.findViewById(id);
    }

    @Override
    public Context getContext() {
        return mMainActivity;
    }

    @Override
    public void onDataChanged() {
        mSearchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPick(FileInfo f) {
    }

    @Override
    public void setWallpaper(FileInfo f) {
    }

    @Override
    public boolean shouldShowOperationPane() {
        return false;
    }

    @Override
    public boolean onOperation(int id) {
        return false;
    }

    @Override
    public void runOnUiThread(Runnable r) {
    }

    @Override
    public boolean onNavigation(String path) {
        return false;
    }

    @Override
    public boolean shouldHideMenu(int menu) {
        return false;
    }

    @Override
    public FileIconHelper getFileIconHelper() {
        return null;
    }

    @Override
    public FileInfo getItem(int pos) {
        if (pos < 0 || pos > mSearchList.size() - 1) {
            return null;
        }
        return mSearchList.get(pos);
    }

    @Override
    public void sortCurrentList(FileSortHelper sort) {
    }

    @Override
    public ArrayList<FileInfo> getAllFiles() {
        return mSearchList;
    }

    @Override
    public void addSingleFile(FileInfo file) {
    }

    @Override
    public boolean onBack() {
        return false;
    }

    @Override
    public boolean onRefreshFileList(String path, FileSortHelper sort) {
        mSearchList.clear();
        mSearchList = mSearchOnKeyListener.refreshList();
        mSearchAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public int getItemCount() {
        return mSearchList.size();
    }

    @Override
    public void clearSelectList() {
        mSearchList.clear();
    }
}
