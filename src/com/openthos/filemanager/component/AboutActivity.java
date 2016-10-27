package com.openthos.filemanager.component;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.openthos.filemanager.R;

public class AboutActivity extends Activity implements View.OnClickListener {
    private ImageView iv_about_back;
    private TextView tv_public;
    private TextView tv_discuss;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
        initData();
    }

    private void initView() {
        iv_about_back = (ImageView) findViewById(R.id.iv_about_back);
        tv_public = (TextView) findViewById(R.id.tv_public);
        tv_discuss = (TextView) findViewById(R.id.tv_discuss);
    }

    private void initData() {
        iv_about_back.setOnClickListener(this);
        tv_public.setOnClickListener(this);
        tv_discuss.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        switch (view.getId()) {
            case R.id.iv_about_back:
                finish();
                break;
            case R.id.tv_public:
                String url = "http://www.emindsoft.com.cn/";
                intent.setData(Uri.parse(url));
                startActivity(intent);
                break;
            case R.id.tv_discuss:
                url = "http://www.emindsoft.com.cn/";
                intent.setData(Uri.parse(url));
                startActivity(intent);
                break;
        }
    }
}
