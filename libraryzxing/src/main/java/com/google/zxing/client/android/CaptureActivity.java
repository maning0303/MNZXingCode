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

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.manager.BeepManager;
import com.google.zxing.client.android.manager.InactivityTimer;
import com.google.zxing.client.android.utils.ZXingUtils;

import java.io.FileNotFoundException;
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
    //常量
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAIL = 1;
    public static final int RESULT_CANCLE = 2;
    public static final String INTENT_KEY_RESULT_SUCCESS = "intent_key_result_success";
    public static final String INTENT_KEY_RESULT_ERROR = "intent_key_result_error";
    public static final String INTENT_KEY_HINTTEXT = "intent_key_hinttext";
    public static final String INTENT_KEY_SCSNCOLOR = "intent_key_scsncolor";
    public static final String INTENT_KEY_PHOTO_FLAG = "intent_key_photo_flag";
    public static final String INTENT_KEY_BEEP_FLAG = "intent_key_beep_flag";
    public static final String INTENT_KEY_VIBRATE_FLAG = "intent_key_vibrate_flag";

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
    //闪光灯是否打开
    private boolean is_light_on = false;
    private boolean beepFlag = true;
    private boolean vibrateFlag = true;
    private SurfaceView surfaceView;

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

        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        btn_scan_light = (LinearLayout) findViewById(R.id.btn_scan_light);
        iv_scan_light = (ImageView) findViewById(R.id.iv_scan_light);
        tv_scan_light = (TextView) findViewById(R.id.tv_scan_light);
        btn_close = (LinearLayout) findViewById(R.id.btn_close);
        btn_photo = (LinearLayout) findViewById(R.id.btn_photo);
        btn_dialog_bg = (RelativeLayout) findViewById(R.id.btn_dialog_bg);
        btn_dialog_bg.setVisibility(View.GONE);

        //初始化相关参数
        initIntent();

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
    }

    private void initIntent() {
        Intent intent = getIntent();
        String hintText = intent.getStringExtra(INTENT_KEY_HINTTEXT);
        String scanColor = intent.getStringExtra(INTENT_KEY_SCSNCOLOR);
        boolean photoFlag = intent.getBooleanExtra(INTENT_KEY_PHOTO_FLAG, true);
        beepFlag = intent.getBooleanExtra(INTENT_KEY_BEEP_FLAG, true);
        vibrateFlag = intent.getBooleanExtra(INTENT_KEY_VIBRATE_FLAG, true);
        if (!TextUtils.isEmpty(hintText)) {
            viewfinderView.setHintText(hintText);
        }
        if (!TextUtils.isEmpty(scanColor)) {
            viewfinderView.setScanLineColor(Color.parseColor(scanColor));
        }
        if (!photoFlag) {
            btn_photo.setVisibility(View.GONE);
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
            if (data == null){
                //隐藏Dialog
                btn_dialog_bg.setVisibility(View.GONE);
                return;
            }
            final Uri uri = data.getData();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmapChoose = ZXingUtils.decodeUriAsBitmap(CaptureActivity.this, uri);
                    if (bitmapChoose != null) {
                        final String decodeQRCodeFromBitmap = ZXingUtils.syncDecodeQRCode(bitmapChoose);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_dialog_bg.setVisibility(View.GONE);
                                Log.i(TAG, "图片的decodeQRCode:" + decodeQRCodeFromBitmap);
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
        Log.i(TAG, "onResume");
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
    }

    private void finishFailed(String errorMsg) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_KEY_RESULT_ERROR, errorMsg);
        this.setResult(RESULT_FAIL, intent);
        this.finish();
    }

    private void finishCancle() {
        this.setResult(RESULT_CANCLE, null);
        this.finish();
    }

    private void finishSuccess(String result) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_KEY_RESULT_SUCCESS, result);
        this.setResult(RESULT_SUCCESS, intent);
        this.finish();
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
        if (surfaceHolder == null) {
            displayFrameworkBugMessageAndExit("SurfaceHolder 不存在");
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
            displayFrameworkBugMessageAndExit("开启摄像头异常：" + e.toString());
        }
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
}
