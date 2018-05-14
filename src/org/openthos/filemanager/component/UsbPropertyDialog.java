package org.openthos.filemanager.component;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.openthos.filemanager.R;
import org.openthos.filemanager.BaseMenuDialog;
import org.openthos.filemanager.utils.Util;

public class UsbPropertyDialog extends BaseMenuDialog {
    private Context mContext;
    private String mUsbs;

    public UsbPropertyDialog(Context context, String usbs) {
        super(context);
        mContext = context;
        mUsbs = usbs;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_usb_property);
        getWindow().setBackgroundDrawable(mContext.getResources().
                                          getDrawable(R.color.transparent));
        initBody();
        initFoot();
    }

    private void initBody() {
        TextView size = (TextView) findViewById(R.id.size);
        TextView sizeOnDisk = (TextView) findViewById(R.id.size_on_disk);
        Util.UsbMemoryInfo usbInfo = Util.getUsbMemoryInfo(mUsbs);
        size.setText(Util.convertStorage(usbInfo.usbTotal));
        sizeOnDisk.setText(Util.convertStorage(usbInfo.usbFree));
    }

    private void initFoot() {
        TextView confirm = (TextView) findViewById(R.id.confirm);
        View.OnClickListener click= new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        };
        confirm.setOnClickListener(click);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {

    }
}
