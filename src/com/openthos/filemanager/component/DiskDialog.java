package com.openthos.filemanager.component;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.openthos.filemanager.MainActivity;
import com.openthos.filemanager.R;
import com.openthos.filemanager.system.Constants;
import com.openthos.filemanager.fragment.SdStorageFragment;
import java.util.ArrayList;
import static android.R.color.holo_purple;
import static android.R.color.transparent;

public class DiskDialog extends Dialog {
    private Context mContext;
    private boolean mIsUSB;
    private View mView;
    private MainActivity mMainActivity;
    private MotionEvent mMotionEvent;
    private ListView mListView;
    private ArrayList mData;
    private int mDialogHeight;
    private int mDialogWidth = 176;
    private int singleHeight = 40;
    private int fix = 20;

    public DiskDialog(Context context, boolean isUSB, View view) {
        super(context);
        mContext = context;
        mMainActivity = (MainActivity) context;
        mIsUSB = isUSB;
        mView = view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_base);
        initView();
        initData();
        initListener();
    }

    public void initView() {
        mListView = (ListView) findViewById(R.id.dialog_base_lv);
    }

    public void initData() {
        String[] diskMeun = new String[] {
                mContext.getString(R.string.operation_open),
        };

        String[] uDiskMeun = new String[] {
                mContext.getString(R.string.umount),
                mContext.getString(R.string.operation_open)
        };

        mData = new ArrayList();
        prepareData(mIsUSB ? uDiskMeun : diskMeun);

        BaseDialogAdapter mAdapter = new BaseDialogAdapter();
        mListView.setAdapter(mAdapter);
        mDialogHeight = mData.size() * singleHeight - fix;
    }

    protected void initListener() {
        mListView.setOnItemClickListener(new MenuItemClickListener(mContext));
    }

    public  void prepareData(String[] sArr) {
        for (int i = 0; i < sArr.length; i++) {
            mData.add(sArr[i]);
        }
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
            view = View.inflate(mContext, R.layout.dialog_base_item ,null);
            view.setOnHoverListener(new MenuItemHoverListener());
            TextView mTvDialogItem = (TextView) view.findViewById(R.id.dialog_base_item);
            TextView mTvDialogItemUsb = (TextView) view.findViewById(R.id.dialog_base_itemUsb);
            String content = mData.get(i).toString();
            mTvDialogItem.setText(content);
            mTvDialogItemUsb.setText(content);
            view.setTag(content);
            return view;
        }
    }

    class MenuItemClickListener implements ListView.OnItemClickListener {
        private Context mContext;
        public MenuItemClickListener(Context context) {
            super();
            mContext = context;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String content = (String) view.getTag();
            if (mContext.getString(R.string.umount).equals(content)) {
                mMainActivity.uninstallUSB(1);
            } else if (mContext.getString(R.string.operation_open).equals(content)) {
                ((SdStorageFragment)(((MainActivity)mContext).getVisibleFragment())).enter();
            }
            dismiss();
        }
    }

    class MenuItemHoverListener implements View.OnHoverListener {

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
    }

    public void showDialog(int x, int y) {
        show();
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
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
