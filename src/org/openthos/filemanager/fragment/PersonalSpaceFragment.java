package org.openthos.filemanager.fragment;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.openthos.filemanager.BaseFragment;
import org.openthos.filemanager.R;
import org.openthos.filemanager.adapter.PersonalAdapter;
import org.openthos.filemanager.bean.PersonalBean;
import org.openthos.filemanager.component.PersonalMenuDialog;
import org.openthos.filemanager.component.DragGridView;
import org.openthos.filemanager.utils.Constants;

import java.util.List;

public class PersonalSpaceFragment extends BaseFragment {
    private DragGridView mPersonalGrid;
    private PersonalAdapter mPersonalAdapter;
    private List<PersonalBean> mPersonalBeanList;
    private double mLastBackTime;
    private GridViewOnGenericMotionListener mMotionListener;
    private int mPos;
    private PersonalMenuDialog mPersonalMenuDialog;


    @Override
    protected void initListener() {
        mPersonalGrid.setOnGenericMotionListener(mMotionListener);
    }

    @Override
    protected void initData() {
        mPersonalBeanList = mMainActivity.getPersonalBeanList();
        mMotionListener = new GridViewOnGenericMotionListener();
        mPersonalAdapter = new PersonalAdapter(mMainActivity, mPersonalBeanList, mMotionListener);
        mPersonalGrid.setAdapter(mPersonalAdapter);
    }


    @Override
    protected void initView() {
        mPersonalGrid = (DragGridView) rootView.findViewById(R.id.personal_fragment_grid);
    }

    @Override
    public int getLayoutId() {
        return R.layout.personal_fragments_layout;

    }

    @Override
    public boolean canGoBack() {
        return false;
    }

    @Override
    public void goBack() {
    }

    public class GridViewOnGenericMotionListener implements View.OnGenericMotionListener {
        List<Integer> integerList;

        @Override
        public boolean onGenericMotion(View v, MotionEvent event) {
            mMainActivity.clearNivagateFocus();
            integerList = mPersonalAdapter.getSelectFileInfoList();
            switch (event.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    if (v.getTag() instanceof PersonalAdapter.ViewHolder) {
                        if (mPos != (Integer)
                                ((PersonalAdapter.ViewHolder) v.getTag()).tvTitle.getTag()) {
                            mLastBackTime = 0;
                            mPos = (Integer) ((PersonalAdapter.ViewHolder) v.getTag()).tvTitle.getTag();
                        }
                        if (System.currentTimeMillis() - mLastBackTime
                                < Constants.DOUBLE_CLICK_INTERVAL_TIME) {
                            enter();
                            mLastBackTime = 0;
                        } else {
                            mLastBackTime = System.currentTimeMillis();
                        }
                        if (!integerList.contains(mPos)) {
                            integerList.clear();
                            integerList.add(mPos);
                        }
                    } else {
                        integerList.clear();
                    }
                    mPersonalAdapter.notifyDataSetChanged();
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    if (v.getTag() instanceof PersonalAdapter.ViewHolder) {
                        mPos = (Integer) ((PersonalAdapter.ViewHolder) v.getTag()).tvTitle.getTag();
                        if (!integerList.contains(mPos)) {
                            integerList.clear();
                            integerList.add(mPos);
                        }
                        mPersonalMenuDialog = new PersonalMenuDialog
                                (mMainActivity, mPersonalBeanList.get(mPos));
                        mPersonalMenuDialog.showDialog((int) event.getRawX(), (int) event.getRawY());
                    } else {
//                        mPersonalMenuDialog = new PersonalMenuDialog(mMainActivity);
                        integerList.clear();
                    }
//                    mPersonalMenuDialog.showDialog((int) event.getRawX(), (int) event.getRawY());
                    mPersonalAdapter.notifyDataSetChanged();
                    break;
                case MotionEvent.BUTTON_TERTIARY:
                    mMainActivity.onUp();
                    break;
                case MotionEvent.ACTION_SCROLL:
                    break;
                case MotionEvent.ACTION_HOVER_ENTER:
                    break;
                default:

            }
            return true;
        }
    }

    @Override
    public void enter() {
        enter(mPos);
    }

    private void enter(int position) {
        mMainActivity.showFileSpaceFragment(mPersonalBeanList.get(position).getPath());
        mMainActivity.setSelectedBackground(R.id.tv_collected);
    }

    public void copyPath() {
        ((ClipboardManager) mMainActivity.getSystemService(Context.CLIPBOARD_SERVICE))
                .setText(mPersonalBeanList.get(mPos).getPath());
    }

    @Override
    public void processDirectionKey(int keyCode) {
        int numColumns = mPersonalGrid.getNumColumns();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mPos = mPos > 0 ? mPos - 1 : mPos;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                mPos = mPos > numColumns - 1 ? mPos - numColumns : mPos;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mPos = mPos < mPersonalBeanList.size() - 1 ? mPos + 1 : mPos;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mPos = mPos < mPersonalBeanList.size() - numColumns ?
                        mPos + numColumns : mPos;
                break;
        }
        List<Integer> integerList = mPersonalAdapter.getSelectFileInfoList();
        integerList.clear();
        integerList.add(mPos);
        mPersonalAdapter.notifyDataSetChanged();
    }

    @Override
    public void showMenu() {
    }

    @Override
    public void clearSelectList() {
    }

}
