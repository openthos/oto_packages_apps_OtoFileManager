package com.openthos.filemanager.system;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.openthos.filemanager.R;

public class CreateFileDialog extends AlertDialog implements View.OnClickListener,
                                                AdapterView.OnItemClickListener {
    private String mInputText;
    private String mTitle;
    private String mMsg;
    private OnFinishListener mListener;
    private Context mContext;
    private EditText mFolderName;
    private PopupWindow mPopupWindow;
    private TextView mTvType;
    private ListView mListView;
    private String[] mTypes;
    private View mView;

    public interface OnFinishListener {
        // return true to accept and dismiss, false reject
        boolean onFinish(String text);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mTvType.setText(mTypes[i]);
        mPopupWindow.dismiss();
    }

    @Override
    public void onClick(View view) {
        initPopupWindow();
    }

    private void initPopupWindow() {
        View popupWindow_view = getLayoutInflater().inflate(R.layout.file_type_popwindow, null);

        mListView = (ListView) popupWindow_view.findViewById(R.id.list_file_type);
        mTypes = mContext.getResources().getStringArray(R.array.file_type_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                                           android.R.layout.simple_list_item_1, mTypes);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
        mPopupWindow = new PopupWindow(popupWindow_view, mTvType.getWidth(),
                                           LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        int xOff = (int) mContext.getResources().getDimension(R.dimen.left_margrin_text);
        int yOff = (int) mContext.getResources().getDimension(R.dimen.navigation);
        mPopupWindow.showAtLocation(mView, Gravity.RIGHT | Gravity.BOTTOM, xOff, -yOff);
    }

    public CreateFileDialog(Context context, String title, String msg,
                           String text, OnFinishListener listener) {
        super(context);
        mTitle = title;
        mMsg = msg;
        mListener = listener;
        mInputText = text;
        mContext = context;
    }

    public String getInputText() {
        return mInputText;
    }

    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.text_input_dialog, null);

        setTitle(mTitle);
        setMessage(mMsg);

        mFolderName = (EditText) mView.findViewById(R.id.text);
        mFolderName.setText(mInputText);
        mTvType = (TextView) mView.findViewById(R.id.tv_file_type);
        mTvType.setOnClickListener(this);

        setView(mView);
        setButton(BUTTON_POSITIVE, mContext.getString(R.string.confirm),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_POSITIVE) {
                            mInputText = mFolderName.getText().toString() +
                                         mTvType.getText().toString();
                            if (mListener.onFinish(mInputText)) {
                                dismiss();
                            }
                        }
                    }
                });
        setButton(BUTTON_NEGATIVE, mContext.getString(R.string.cancel),
                (OnClickListener) null);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        super.onCreate(savedInstanceState);
    }
}
