package com.google.zxing.client.android.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.google.zxing.client.android.R;
import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.utils.CommonUtils;

/**
 * @author : maning
 * @date : 2020-09-04
 * @desc :
 */
public class ZoomControllerView extends FrameLayout implements View.OnTouchListener {

    private MNScanConfig scanConfig;
    private ImageView mIvScanZoomIn;
    private ImageView mIvScanZoomOut;
    private SeekBar mSeekBarZoom;
    private LinearLayout mLlRoomController;
    private VerticalSeekBar mSeekBarZoomVertical;
    private ImageView mIvScanZoomOutVertical;
    private LinearLayout mLlRoomControllerVertical;
    private ImageView mIvScanZoomInVertical;

    private OnZoomControllerListener onZoomControllerListener;

    public interface OnZoomControllerListener {
        void onZoom(int progress);
    }

    public void setOnZoomControllerListener(OnZoomControllerListener onZoomControllerListener) {
        this.onZoomControllerListener = onZoomControllerListener;
    }


    public ZoomControllerView(Context context) {
        this(context, null);
    }

    public ZoomControllerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.mn_scan_zoom_controller, this);

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("======", "onClick----");
            }
        });

        mIvScanZoomIn = (ImageView) view.findViewById(R.id.iv_scan_zoom_in);
        mIvScanZoomOut = (ImageView) view.findViewById(R.id.iv_scan_zoom_out);
        mSeekBarZoom = (SeekBar) view.findViewById(R.id.seek_bar_zoom);
        mLlRoomController = (LinearLayout) view.findViewById(R.id.ll_room_controller);

        mSeekBarZoomVertical = (VerticalSeekBar) view.findViewById(R.id.seek_bar_zoom_vertical);
        mIvScanZoomOutVertical = (ImageView) view.findViewById(R.id.iv_scan_zoom_out_vertical);
        mIvScanZoomInVertical = (ImageView) view.findViewById(R.id.iv_scan_zoom_in_vertical);
        mLlRoomControllerVertical = (LinearLayout) view.findViewById(R.id.ll_room_controller_vertical);

        mSeekBarZoomVertical.setMaxProgress(100);
        mSeekBarZoomVertical.setProgress(0);
        mSeekBarZoomVertical.setThumbSize(8, 8);
        mSeekBarZoomVertical.setUnSelectColor(Color.parseColor("#b4b4b4"));
        mSeekBarZoomVertical.setSelectColor(Color.parseColor("#FFFFFF"));

        mIvScanZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomIn(10);
            }
        });
        mIvScanZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomOut(10);
            }
        });
        mIvScanZoomInVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomIn(10);
            }
        });
        mIvScanZoomOutVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomOut(10);
            }
        });

        mSeekBarZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSeekBarZoomVertical.setProgress(progress);
                if (onZoomControllerListener != null) {
                    onZoomControllerListener.onZoom(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarZoomVertical.setOnSlideChangeListener(new VerticalSeekBar.SlideChangeListener() {
            @Override
            public void onStart(VerticalSeekBar slideView, int progress) {

            }

            @Override
            public void onProgress(VerticalSeekBar slideView, int progress) {
                mSeekBarZoom.setProgress(progress);
                if (onZoomControllerListener != null) {
                    onZoomControllerListener.onZoom(progress);
                }
            }

            @Override
            public void onStop(VerticalSeekBar slideView, int progress) {

            }
        });

        setOnTouchListener(this);
    }

    public void zoomOut(int value) {
        int progress = mSeekBarZoom.getProgress() - value;
        if (progress <= 0) {
            progress = 0;
        }
        mSeekBarZoom.setProgress(progress);
        mSeekBarZoomVertical.setProgress(progress);
        if (onZoomControllerListener != null) {
            onZoomControllerListener.onZoom(progress);
        }
    }

    public void zoomIn(int value) {
        int progress = mSeekBarZoom.getProgress() + value;
        if (progress >= 100) {
            progress = 100;
        }
        mSeekBarZoom.setProgress(progress);
        mSeekBarZoomVertical.setProgress(progress);
        if (onZoomControllerListener != null) {
            onZoomControllerListener.onZoom(progress);
        }
    }

    public void updateZoomController(MNScanConfig scanConfig, Rect framingRect) {
        this.scanConfig = scanConfig;
        if (framingRect == null) {
            return;
        }
        //显示
        if (scanConfig.isSupportZoom()) {
            int size10 = CommonUtils.dip2px(getContext(), 10);
            int size24 = CommonUtils.dip2px(getContext(), 24);
            MNScanConfig.ZoomControllerLocation zoomControllerLocation = scanConfig.getZoomControllerLocation();
            if (zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Left) {
                //垂直方向
                RelativeLayout.LayoutParams layoutParamsVertical = (RelativeLayout.LayoutParams) mLlRoomControllerVertical.getLayoutParams();
                layoutParamsVertical.height = framingRect.bottom - framingRect.top - size10 * 2;
                int left = framingRect.left - size10 - size24;
                if (left < size10) {
                    left = size10;
                }
                layoutParamsVertical.setMargins(left, framingRect.top + size10, 0, 0);
                mLlRoomControllerVertical.setLayoutParams(layoutParamsVertical);

                if (scanConfig.isShowZoomController()) {
                    mLlRoomControllerVertical.setVisibility(View.VISIBLE);
                }
            } else if (zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Right) {
                //垂直方向
                RelativeLayout.LayoutParams layoutParamsVertical = (RelativeLayout.LayoutParams) mLlRoomControllerVertical.getLayoutParams();
                layoutParamsVertical.height = framingRect.bottom - framingRect.top - size10 * 2;
                int left = framingRect.right + size10;
                if (left + size10 + size24 > CommonUtils.getScreenWidth(getContext())) {
                    left = CommonUtils.getScreenWidth(getContext()) - size10 - size24;
                }
                layoutParamsVertical.setMargins(left, framingRect.top + size10, 0, 0);
                mLlRoomControllerVertical.setLayoutParams(layoutParamsVertical);

                if (scanConfig.isShowZoomController()) {
                    mLlRoomControllerVertical.setVisibility(View.VISIBLE);
                }
            } else if (zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Bottom) {
                //横向
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLlRoomController.getLayoutParams();
                layoutParams.width = framingRect.right - framingRect.left - size10 * 2;
                layoutParams.setMargins(0, framingRect.bottom + size10, 0, 0);
                mLlRoomController.setLayoutParams(layoutParams);

                if (scanConfig.isShowZoomController()) {
                    mLlRoomController.setVisibility(View.VISIBLE);
                }
            }
        }


    }


    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float startX = 0;
    float startY = 0;
    float moveX = 0;
    float moveY = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!scanConfig.isSupportZoom()) {
            return super.onTouchEvent(event);
        }
        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            startX = event.getX();
            startY = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //当手指离开的时候
            moveX = event.getX();
            moveY = event.getY();
            MNScanConfig.ZoomControllerLocation zoomControllerLocation = scanConfig.getZoomControllerLocation();
            if (startY - moveY > 50) {
                if (zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Left
                        || zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Right) {
                    //垂直方向
                    //向上滑
                    zoomIn(1);
                }
            } else if (moveY - startY > 50) {
                if (zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Left
                        || zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Right) {
                    //垂直方向
                    //向下滑
                    zoomOut(1);
                }
            } else if (startX - moveX > 50) {
                if (zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Bottom) {
                    //垂直方向
                    //向左滑
                    zoomOut(1);
                }
            } else if (moveX - startX > 50) {
                if (zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Bottom) {
                    //垂直方向
                    //向右滑
                    zoomIn(1);
                }
            }
        }
        return true;
    }
}
