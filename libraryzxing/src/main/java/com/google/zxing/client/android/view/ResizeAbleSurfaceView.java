package com.google.zxing.client.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * @author : maning
 * @date : 2020-06-19
 * @desc :
 */
public class ResizeAbleSurfaceView extends SurfaceView {

    private int mWidth = -1;
    private int mHeight = -1;

    public ResizeAbleSurfaceView(Context context) {
        super(context);
    }

    public ResizeAbleSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizeAbleSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (-1 == mWidth || -1 == mHeight) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(mWidth, mHeight);
        }
    }

    public void resize(int width, int height) {
        mWidth = width;
        mHeight = height;
        getHolder().setFixedSize(width, height);
        requestLayout();
        invalidate();
    }
}