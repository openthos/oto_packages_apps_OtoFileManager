package com.openthos.filemanager.component;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.BaseDialog;
import com.openthos.filemanager.R;
import com.openthos.filemanager.fragment.PersonalSpaceFragment;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.system.FileInfo;
import com.openthos.filemanager.system.FileViewInteractionHub;

import java.util.ArrayList;

import static android.R.color.holo_purple;
import static android.R.color.transparent;

public class PersonalMenuDialog extends BaseDialog
                             implements View.OnHoverListener, ListView.OnItemClickListener{
    private int singleHeight = 40;
    private int fix = 20;
    private int mDialogWidth = 176;
    private int mDialogHeight;
    private Context mContext;
    private ListView mListView;
    private ArrayList mData;
    boolean mIsBlank;

    public PersonalMenuDialog(Context context, boolean isBlank) {
        super(context);
        mContext = context;
        mIsBlank = isBlank;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_base);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mListView.setOnItemClickListener(this);
    }

    private void initData() {
        mData = new ArrayList();
        if (mIsBlank) {
            mData.add(mContext.getString(R.string.operation_refresh));
        } else {
            String[] menu = mContext.getResources().getStringArray(R.array.personal_folder_menu);
            for (int i = 0; i < menu.length; i++) {
                mData.add(menu[i]);
            }
        }
        BaseDialogAdapter mAdapter = new BaseDialogAdapter();
        mListView.setAdapter(mAdapter);
        mDialogHeight = mData.size() * singleHeight - fix;
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.dialog_base_lv);
    }

    class BaseDialogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = View.inflate(mContext, R.layout.dialog_base_item, null);
            TextView mTvDialogItem = (TextView) view.findViewById(R.id.dialog_base_item);
            String content = mData.get(i).toString();
            mTvDialogItem.setText(content);
            view.setOnHoverListener(PersonalMenuDialog.this);
            view.setTag(content);
            return view;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String content = (String) view.getTag();
        PersonalSpaceFragment personalSpaceFragment =
            (PersonalSpaceFragment) ((MainActivity) mContext).getVisibleFragment();
        if (mContext.getString(R.string.operation_open).equals(content)) {
            personalSpaceFragment.enter();
        } else if (mContext.getString(R.string.operation_copy_path).equals(content)) {
            personalSpaceFragment.copyPath();
        } else if (mContext.getString(R.string.operation_refresh).equals(content)) {
            personalSpaceFragment.checkFolder();
        }
        if (!TextUtils.isEmpty(content)) {
            dismiss();
        }
    }

    @Override
    public boolean onHover(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_HOVER_ENTER:
                view.setBackgroundColor(mContext.getResources().getColor(holo_purple));
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                view.setBackgroundColor(mContext.getResources().getColor(transparent));
                break;
        }
        return false;
    }

    public void showDialog(int x, int y) {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        show();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager m = ((Activity) mContext).getWindowManager();
        Display d = m.getDefaultDisplay();
        int dialogPadding = (int) mContext.getResources().getDimension(R.dimen.left_margrin_text);
        if (x > (d.getWidth() - mDialogWidth)) {
            lp.x = x - mDialogWidth + dialogPadding;
        } else {
            lp.x = x + dialogPadding;
        }
        if (y > (d.getHeight() - mDialogHeight - Constants.BAR_Y)) {
            lp.y = d.getHeight() - mDialogHeight - Constants.BAR_Y + dialogPadding;

        } else {
            lp.y = y + dialogPadding;
        }
        dialogWindow.setAttributes(lp);
    }
}
