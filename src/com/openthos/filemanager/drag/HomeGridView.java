package com.openthos.filemanager.drag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

@SuppressLint("NewApi")
public class HomeGridView extends GridView {
    public HomeGridView(Context context) {
        this(context, null);
    }

    public HomeGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int spec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, spec);
    }
}
