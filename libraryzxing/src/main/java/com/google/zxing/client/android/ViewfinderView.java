/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.utils.CommonUtils;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

    private CameraManager cameraManager;
    private final Paint paint;
    private Paint paintText;
    private Paint paintLine;
    private int maskColor;
    private int laserColor;

    private int linePosition = 0;

    private static String hintMsg;

    private Context context;

    private Rect frame;
    private int margin;
    private int lineW;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.mn_scan_viewfinder_mask);
        laserColor = resources.getColor(R.color.mn_scan_viewfinder_laser);
        hintMsg = resources.getString(R.string.mn_scan_hint_text);
        //文字
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(CommonUtils.sp2px(context, 14));
        paintText.setTextAlign(Paint.Align.CENTER);
        //扫描线 + 四角
        paintLine.setColor(laserColor);

        margin = CommonUtils.dip2px(context, 4);
        lineW = CommonUtils.dip2px(context, 2);
    }

    //设置颜色
    public void setScanLineColor(int laserColor) {
        this.laserColor = laserColor;
        paintLine.setColor(this.laserColor);
    }

    //设置文案
    public void setHintText(String msg) {
        hintMsg = msg;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // 半透明背景
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        //文字
        canvas.drawText(hintMsg, width / 2, frame.top - CommonUtils.dip2px(context, 24), paintText);

        paintLine.setShader(null);
        //四角线块
        int rectH = CommonUtils.dip2px(context, 2);
        int rectW = CommonUtils.dip2px(context, 14);
        //左上角
        canvas.drawRect(frame.left, frame.top, frame.left + rectW, frame.top + rectH, paintLine);
        canvas.drawRect(frame.left, frame.top, frame.left + rectH, frame.top + rectW, paintLine);
        //右上角
        canvas.drawRect(frame.right - rectW, frame.top, frame.right + 1, frame.top + rectH, paintLine);
        canvas.drawRect(frame.right - rectH, frame.top, frame.right + 1, frame.top + rectW, paintLine);
        //左下角
        canvas.drawRect(frame.left, frame.bottom - rectH, frame.left + rectW, frame.bottom + 1, paintLine);
        canvas.drawRect(frame.left, frame.bottom - rectW, frame.left + rectH, frame.bottom + 1, paintLine);
        //右下角
        canvas.drawRect(frame.right - rectW, frame.bottom - rectH, frame.right + 1, frame.bottom + 1, paintLine);
        canvas.drawRect(frame.right - rectH, frame.bottom - rectW, frame.right + 1, frame.bottom + 1, paintLine);

        //中间的线：动画
        if (linePosition == 0) {
            linePosition = frame.top + margin;
        }
        canvas.drawRect(frame.left + margin, linePosition, frame.right - margin, linePosition + lineW, paintLine);

        // Request another update at the animation interval, but only repaint the laser line,
        // not the entire viewfinder mask.
        postInvalidateDelayed(36,
                frame.left,
                frame.top,
                frame.right,
                frame.bottom);

        startAnimation();
    }


    ValueAnimator anim;

    public void startAnimation() {
        if (anim != null && anim.isRunning()) {
            return;
        }
        anim = ValueAnimator.ofInt(frame.top + margin, frame.bottom - margin);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.setDuration(2500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                linePosition = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        anim.start();
    }

    public void drawViewfinder() {
        postInvalidate();
    }

}
