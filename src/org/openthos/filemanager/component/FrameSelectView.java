package org.openthos.filemanager.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class FrameSelectView extends View {
    private Paint mPaint;
    private float mLeft, mTop, mRight, mBootom;

    public FrameSelectView(Context context) {
        super(context);
        mPaint= new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setAlpha(0x7f);
        mPaint.setStrokeWidth(5.0f);
    }

    public void setPositionCoordinate(float left, float top, float right, float bootom) {
        mLeft = left;
        mTop = top;
        mRight = right;
        mBootom = bootom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(mLeft, mTop, mRight, mBootom,mPaint);
    }
}