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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.manager.BeepManager;
import com.google.zxing.client.android.manager.InactivityTimer;
import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.utils.CommonUtils;
import com.google.zxing.client.android.utils.ZXingUtils;
import com.google.zxing.client.android.view.VerticalSeekBar;

import java.util.Collection;
import java.util.Map;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CAMERA = 10010;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private Result lastResult;
    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    private LinearLayout btn_scan_light;
    private ImageView iv_scan_light;
    private TextView tv_scan_light;
    private LinearLayout btn_close;
    private LinearLayout btn_photo;
    private RelativeLayout btn_dialog_bg;
    private ImageView ivScreenshot;

    private SurfaceView surfaceView;
    private ImageView mIvScanZoomIn;
    private ImageView mIvScanZoomOut;
    private SeekBar mSeekBarZoom;
    private LinearLayout mLlRoomController;
    private Context context;
    private VerticalSeekBar mSeekBarZoomVertical;
    private ImageView mIvScanZoomOutVertical;
    private LinearLayout mLlRoomControllerVertical;
    private ImageView mIvScanZoomInVertical;

    //传递数据
    //闪光灯是否打开
    private boolean is_light_on = false;
    private boolean beepFlag = true;
    private boolean vibrateFlag = true;
    private boolean zoomControllerFlag = true;
    private int exitAnime = 0;
    private MNScanConfig.ZoomControllerLocation zoomControllerLocation;

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.mn_scan_capture);
        context = this;
        initView();
        initIntent();
    }


    private void initIntent() {
        Intent intent = getIntent();

        MNScanConfig mnScanConfig = (MNScanConfig) intent.getSerializableExtra(MNScanManager.INTENT_KEY_CONFIG_MODEL);


        String hintText = mnScanConfig.getScanHintText();
        String scanColor = mnScanConfig.getScanColor();
        boolean photoFlag = mnScanConfig.isShowPhotoAlbum();
        beepFlag = mnScanConfig.isShowBeep();
        vibrateFlag = mnScanConfig.isShowVibrate();
        exitAnime = mnScanConfig.getActivityExitAnime();
        zoomControllerFlag = mnScanConfig.isShowZoomController();
        zoomControllerLocation = mnScanConfig.getZoomControllerLocation();

        if (!TextUtils.isEmpty(hintText)) {
            viewfinderView.setHintText(hintText);
        }
        if (!TextUtils.isEmpty(scanColor)) {
            viewfinderView.setScanLineColor(Color.parseColor(scanColor));
        }
        if (!photoFlag) {
            btn_photo.setVisibility(View.GONE);
        }
        if (exitAnime == 0) {
            exitAnime = R.anim.mn_scan_activity_bottom_out;
        }
    }

    /**
     * 获取相册中的图片
     */
    public void getImageFromAlbum() {
        Intent intent = new Intent();
        /* 开启Pictures画面Type设定为image */
        intent.setType("image/*");
        /* 使用Intent.ACTION_GET_CONTENT这个Action */
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setAction(Intent.ACTION_PICK);
        /* 取得相片后返回本画面 */
        startActivityForResult(intent, 1000);
        //开始转Dialog
        btn_dialog_bg.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //去相册选择图片
        if (requestCode == 1000) {
            if (data == null) {
                //隐藏Dialog
                btn_dialog_bg.setVisibility(View.GONE);
                return;
            }
            final Uri uri = data.getData();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //需要压缩图片
                    Bitmap bitmapChoose = ZXingUtils.decodeUriAsBitmap(CaptureActivity.this, uri);
                    if (bitmapChoose != null) {
                        final String decodeQRCodeFromBitmap = ZXingUtils.syncDecodeQRCode(bitmapChoose);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_dialog_bg.setVisibility(View.GONE);
                                if (TextUtils.isEmpty(decodeQRCodeFromBitmap)) {
                                    Toast.makeText(CaptureActivity.this, "未发现二维码", Toast.LENGTH_SHORT).show();
                                } else {
                                    finishSuccess(decodeQRCodeFromBitmap);
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_dialog_bg.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }).start();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        if (handler != null && cameraManager != null && cameraManager.isOpen()) {
            return;
        }
        cameraManager = new CameraManager(getApplication());
        viewfinderView.setCameraManager(cameraManager);

        handler = null;
        lastResult = null;

        resetStatusView();

        beepManager.updatePrefs(beepFlag, vibrateFlag);

        inactivityTimer.onResume();
        decodeFormats = null;
        characterSet = null;

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // 防止sdk8的设备初始化预览异常
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //取消扫码
        finishCancle();
    }

    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        lastResult = rawResult;
        //播放声音和震动
        beepManager.playBeepSoundAndVibrate();
        //关闭页面
        finishSuccess(lastResult.getText());
        //图片显示：测试才显示
        ivScreenshot.setImageBitmap(barcode);

    }

    private void finishFailed(String errorMsg) {
        Intent intent = new Intent();
        intent.putExtra(MNScanManager.INTENT_KEY_RESULT_ERROR, errorMsg);
        this.setResult(MNScanManager.RESULT_FAIL, intent);
        this.finish();
        finishFinal();
    }

    private void finishCancle() {
        this.setResult(MNScanManager.RESULT_CANCLE, null);
        finishFinal();
    }

    private void finishSuccess(String result) {
        Intent intent = new Intent();
        intent.putExtra(MNScanManager.INTENT_KEY_RESULT_SUCCESS, result);
        this.setResult(MNScanManager.RESULT_SUCCESS, intent);
        finishFinal();
    }

    private void finishFinal() {
        this.finish();
        //关闭窗体动画显示
        this.overridePendingTransition(0, exitAnime);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // do nothing
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        //检查相机权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //没有相机权限
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
                return;
            }
        }
        if (surfaceHolder == null) {
            displayFrameworkBugMessageAndExit("初始化相机失败");
            return;
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
        } catch (Exception e) {
            Log.e(TAG, "open camera fail：" + e.toString());
            displayFrameworkBugMessageAndExit("初始化相机失败");
        }
        //刷新控制器
        updateZoomController();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted 授予权限
                    onResume();
                } else {
                    // Permission Denied 权限被拒绝
                    displayFrameworkBugMessageAndExit("初始化相机失败,权限被拒绝");
                }
                break;
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void displayFrameworkBugMessageAndExit(String errorMessage) {
        finishFailed(errorMessage);
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
        viewfinderView.setVisibility(View.VISIBLE);
        lastResult = null;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        btn_scan_light = (LinearLayout) findViewById(R.id.btn_scan_light);
        iv_scan_light = (ImageView) findViewById(R.id.iv_scan_light);
        tv_scan_light = (TextView) findViewById(R.id.tv_scan_light);
        btn_close = (LinearLayout) findViewById(R.id.btn_close);
        btn_photo = (LinearLayout) findViewById(R.id.btn_photo);
        btn_dialog_bg = (RelativeLayout) findViewById(R.id.btn_dialog_bg);
        ivScreenshot = (ImageView) findViewById(R.id.ivScreenshot);
        btn_dialog_bg.setVisibility(View.GONE);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);

        //点击事件
        btn_scan_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_light_on) {
                    is_light_on = false;
                    cameraManager.offLight();
                    iv_scan_light.setImageResource(R.drawable.mn_icon_scan_flash_light_off);
                    tv_scan_light.setText("打开手电筒");
                } else {
                    is_light_on = true;
                    cameraManager.openLight();
                    iv_scan_light.setImageResource(R.drawable.mn_icon_scan_flash_light_on);
                    tv_scan_light.setText("关闭手电筒");
                }
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCancle();
            }
        });

        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromAlbum();
            }
        });

        btn_dialog_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mIvScanZoomIn = (ImageView) findViewById(R.id.iv_scan_zoom_in);
        mIvScanZoomOut = (ImageView) findViewById(R.id.iv_scan_zoom_out);
        mSeekBarZoom = (SeekBar) findViewById(R.id.seek_bar_zoom);
        mLlRoomController = (LinearLayout) findViewById(R.id.ll_room_controller);

        mSeekBarZoomVertical = (VerticalSeekBar) findViewById(R.id.seek_bar_zoom_vertical);
        mIvScanZoomOutVertical = (ImageView) findViewById(R.id.iv_scan_zoom_out_vertical);
        mIvScanZoomInVertical = (ImageView) findViewById(R.id.iv_scan_zoom_in_vertical);
        mLlRoomControllerVertical = (LinearLayout) findViewById(R.id.ll_room_controller_vertical);

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
                cameraManager.setZoom(progress);
                mSeekBarZoomVertical.setProgress(progress);
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
                cameraManager.setZoom(progress);
                mSeekBarZoom.setProgress(progress);
            }

            @Override
            public void onStop(VerticalSeekBar slideView, int progress) {

            }
        });
    }

    private void zoomOut(int value) {
        int progress = mSeekBarZoom.getProgress() - value;
        if (progress <= 0) {
            progress = 0;
        }
        mSeekBarZoom.setProgress(progress);
        mSeekBarZoomVertical.setProgress(progress);
        cameraManager.setZoom(progress);
    }

    private void zoomIn(int value) {
        int progress = mSeekBarZoom.getProgress() + value;
        if (progress >= 100) {
            progress = 100;
        }
        mSeekBarZoom.setProgress(progress);
        mSeekBarZoomVertical.setProgress(progress);
        cameraManager.setZoom(progress);
    }

    private void updateZoomController() {
        if (cameraManager == null) {
            return;
        }
        Rect framingRect = cameraManager.getFramingRect();
        if (framingRect == null) {
            return;
        }
        //显示
        if (zoomControllerFlag) {
            int size10 = CommonUtils.dip2px(context, 10);

            if (zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Left) {
                //垂直方向
                RelativeLayout.LayoutParams layoutParamsVertical = (RelativeLayout.LayoutParams) mLlRoomControllerVertical.getLayoutParams();
                layoutParamsVertical.height = framingRect.bottom - framingRect.top - size10 * 2;
                layoutParamsVertical.setMargins(framingRect.left - size10 - layoutParamsVertical.width, framingRect.top + size10, 0, 0);
                mLlRoomControllerVertical.setLayoutParams(layoutParamsVertical);

                mLlRoomControllerVertical.setVisibility(View.VISIBLE);
            } else if (zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Right) {
                //垂直方向
                RelativeLayout.LayoutParams layoutParamsVertical = (RelativeLayout.LayoutParams) mLlRoomControllerVertical.getLayoutParams();
                layoutParamsVertical.height = framingRect.bottom - framingRect.top - size10 * 2;
                layoutParamsVertical.setMargins(framingRect.right + size10, framingRect.top + size10, 0, 0);
                mLlRoomControllerVertical.setLayoutParams(layoutParamsVertical);

                mLlRoomControllerVertical.setVisibility(View.VISIBLE);
            } else if (zoomControllerLocation == MNScanConfig.ZoomControllerLocation.Bottom) {
                //横向
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLlRoomController.getLayoutParams();
                layoutParams.width = framingRect.right - framingRect.left - size10 * 2;
                layoutParams.setMargins(0, framingRect.bottom + size10, 0, 0);
                mLlRoomController.setLayoutParams(layoutParams);

                mLlRoomController.setVisibility(View.VISIBLE);
            }
        }


    }


    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float startX = 0;
    float startY = 0;
    float moveX = 0;
    float moveY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
            if (!zoomControllerFlag) {
                return super.onTouchEvent(event);
            }
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
        return super.onTouchEvent(event);
    }


}
