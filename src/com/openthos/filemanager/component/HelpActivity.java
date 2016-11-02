package com.openthos.filemanager.component;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openthos.filemanager.BaseActivity;
import com.openthos.filemanager.R;

public class HelpActivity extends BaseActivity implements View.OnClickListener {
    LinearLayout ll_help_back;
    TextView tv_public_official;
    TextView tv_official_help;

    protected void initView() {
        ll_help_back = (LinearLayout) findViewById(R.id.ll_help_back);
        tv_public_official = (TextView) findViewById(R.id.tv_public_official);
        tv_official_help = (TextView) findViewById(R.id.tv_official_help);
    }

    protected void initListener() {
        ll_help_back.setOnClickListener(this);
        tv_public_official.setOnClickListener(this);
        tv_official_help.setOnClickListener(this);
    }

    protected int getLayoutId() {
        return R.layout.activity_help;
    }

    protected void initData() {
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        switch (view.getId()) {
            case R.id.ll_help_back:
                finish();
                break;
            case R.id.tv_official_help:
                String url = "http://www.emindsoft.com.cn/";
                intent.setData(Uri.parse(url));
                startActivity(intent);
                break;
            case R.id.tv_public_official:
                url = "http://www.emindsoft.com.cn/";
                intent.setData(Uri.parse(url));
                startActivity(intent);
                break;
        }
    }
}
