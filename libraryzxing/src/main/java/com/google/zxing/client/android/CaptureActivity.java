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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.other.MNCustomViewBindCallback;
import com.google.zxing.client.android.other.OnScanCallback;
import com.google.zxing.client.android.utils.ImageUtils;
import com.google.zxing.client.android.utils.StatusBarUtil;
import com.google.zxing.client.android.utils.ZXingUtils;
import com.google.zxing.client.android.view.ProgressDialog;
import com.google.zxing.client.android.view.ScanActionMenuView;
import com.google.zxing.client.android.view.ScanSurfaceView;

import java.lang.ref.WeakReference;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public class CaptureActivity extends Activity {

    //用来保存当前Activity
    private static WeakReference<CaptureActivity> sActivityRef;
    private static final String TAG = CaptureActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_IMAGE = 10010;
    private static final int REQUEST_CODE_PERMISSION_CAMERA = 10011;
    private static final int REQUEST_CODE_PERMISSION_STORAGE = 10012;

    private Context context;
    private View fakeStatusBar;
    private ScanSurfaceView scanSurfaceView;
    private ScanActionMenuView mActionMenuView;

    //闪光灯是否打开
    private boolean is_light_on = false;

    //自定义遮罩View
    private static MNCustomViewBindCallback customViewBindCallback;
    private static MNScanConfig mnScanConfig;
    private Handler UIHandler = new Handler(Looper.getMainLooper());

    public static void setMnCustomViewBindCallback(MNCustomViewBindCallback mnCustomViewBindCallback) {
        customViewBindCallback = mnCustomViewBindCallback;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.mn_scan_capture);
        sActivityRef = new WeakReference<>(this);
        context = this;
        initView();
        initIntent();
        initStatusBar();
        initPermission();
    }

    private void initPermission() {
        //检查相机权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //没有相机权限
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION_CAMERA);
            }
        }
    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarUtil.setTransparentForWindow(this);
            int statusBarHeight = StatusBarUtil.getStatusBarHeight(context);
            Log.e("======", "statusBarHeight--" + statusBarHeight);
            ViewGroup.LayoutParams fakeStatusBarLayoutParams = fakeStatusBar.getLayoutParams();
            fakeStatusBarLayoutParams.height = statusBarHeight;
            fakeStatusBar.setLayoutParams(fakeStatusBarLayoutParams);
            //状态栏文字颜色
            if (mnScanConfig.isStatusBarDarkMode()) {
                StatusBarUtil.setDarkMode(this);
            }
            //状态栏颜色
            String statusBarColor = mnScanConfig.getStatusBarColor();
            fakeStatusBar.setBackgroundColor(Color.parseColor(statusBarColor));
        } else {
            ViewGroup.LayoutParams fakeStatusBarLayoutParams = fakeStatusBar.getLayoutParams();
            fakeStatusBarLayoutParams.height = 0;
            fakeStatusBar.setLayoutParams(fakeStatusBarLayoutParams);
        }
    }

    private void initView() {
        fakeStatusBar = (View) findViewById(R.id.fakeStatusBar);

        scanSurfaceView = (ScanSurfaceView) findViewById(R.id.scan_surface_view);
        scanSurfaceView.init(this);
        scanSurfaceView.setOnScanCallback(new OnScanCallback() {
            @Override
            public void onScanSuccess(final String resultTxt, Bitmap barcode) {
                UIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishSuccess(resultTxt);
                    }
                }, 200);
            }

            @Override
            public void onStopScan() {
                mActionMenuView.setVisibility(View.GONE);
            }

            @Override
            public void onRestartScan() {
                mActionMenuView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFail(String msg) {
                finishFailed(msg);
            }

        });

        mActionMenuView = (ScanActionMenuView) findViewById(R.id.action_menu_view);
        mActionMenuView.setOnScanActionMenuListener(new ScanActionMenuView.OnScanActionMenuListener() {
            @Override
            public void onClose() {
                finishCancle();
            }

            @Override
            public void onLight() {
                if (is_light_on) {
                    closeLight();
                } else {
                    openLight();
                }
            }

            @Override
            public void onPhoto() {
                getImageFromAlbum();
            }
        });
    }

    private void openLight() {
        if (!is_light_on) {
            is_light_on = true;
            scanSurfaceView.getCameraManager().openLight();
            mActionMenuView.openLight();
        }
    }

    private void closeLight() {
        if (is_light_on) {
            is_light_on = false;
            scanSurfaceView.getCameraManager().offLight();
            mActionMenuView.closeLight();
        }
    }

    private void initIntent() {
        mnScanConfig = (MNScanConfig) getIntent().getSerializableExtra(MNScanManager.INTENT_KEY_CONFIG_MODEL);
        if (mnScanConfig == null) {
            mnScanConfig = new MNScanConfig.Builder().builder();
        }
        scanSurfaceView.setScanConfig(mnScanConfig);
        mActionMenuView.setScanConfig(mnScanConfig, customViewBindCallback);
    }

    /**
     * 获取相册中的图片
     */
    public void getImageFromAlbum() {
        if (checkStoragePermission()) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        }
    }

    private boolean checkStoragePermission() {
        //判断权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_STORAGE);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //去相册选择图片
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            ProgressDialog.show(context);
            //需要压缩图片
            final String picturePath = ImageUtils.getImageAbsolutePath(context, data.getData());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String decodeQRCodeFromBitmap = ZXingUtils.syncDecodeQRCode(picturePath);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressDialog.dismissDialog();
                            if (TextUtils.isEmpty(decodeQRCodeFromBitmap)) {
                                Toast.makeText(CaptureActivity.this, "未发现二维码", Toast.LENGTH_SHORT).show();
                            } else {
                                finishSuccess(decodeQRCodeFromBitmap);
                            }
                        }
                    });
                }
            }).start();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (scanSurfaceView != null) {
            scanSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanSurfaceView != null) {
            scanSurfaceView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (scanSurfaceView != null) {
            scanSurfaceView.onDestroy();
        }
        UIHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (scanSurfaceView != null && scanSurfaceView.isResultPointViewShow()) {
            scanSurfaceView.hideResultPointView();
            scanSurfaceView.restartScan();
            return;
        }
        //取消扫码
        finishCancle();
    }

    private void finishFailed(String errorMsg) {
        Intent intent = new Intent();
        intent.putExtra(MNScanManager.INTENT_KEY_RESULT_ERROR, errorMsg);
        setResult(MNScanManager.RESULT_FAIL, intent);
        finishFinal();
    }

    private void finishCancle() {
        setResult(MNScanManager.RESULT_CANCLE, null);
        finishFinal();
    }

    private void finishSuccess(String result) {
        Intent intent = new Intent();
        intent.putExtra(MNScanManager.INTENT_KEY_RESULT_SUCCESS, result);
        setResult(MNScanManager.RESULT_SUCCESS, intent);
        finishFinal();
    }

    private void finishFinal() {
        //清空监听
        setMnCustomViewBindCallback(null);
        //销毁相关数据
        sActivityRef = null;
        //关闭
        closeLight();
        //关闭
        finish();
        //关闭窗体动画显示
        overridePendingTransition(0, mnScanConfig.getActivityExitAnime() == 0 ? R.anim.mn_scan_activity_bottom_out : mnScanConfig.getActivityExitAnime());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted 授予权限
                    onResume();
                } else {
                    // Permission Denied 权限被拒绝
                    Toast.makeText(context, "初始化相机失败,相机权限被拒绝", Toast.LENGTH_SHORT).show();
                    finishFailed("初始化相机失败,相机权限被拒绝");
                }
                break;
            case REQUEST_CODE_PERMISSION_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意使用write
                    getImageFromAlbum();
                } else {
                    //缺少权限
                    Toast.makeText(context, "打开相册失败,读写权限被拒绝", Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    //---------对外提供方法----------

    /**
     * 关闭当前Activity
     */
    public static void closeScanPage() {
        if (sActivityRef != null && sActivityRef.get() != null) {
            sActivityRef.get().finishCancle();
        }
    }

    /**
     * 打开相册扫描图片
     */
    public static void openAlbumPage() {
        if (sActivityRef != null && sActivityRef.get() != null) {
            sActivityRef.get().getImageFromAlbum();
        }
    }

    /**
     * 打开手电筒
     */
    public static void openScanLight() {
        if (sActivityRef != null && sActivityRef.get() != null) {
            sActivityRef.get().openLight();
        }
    }

    /**
     * 关闭手电筒
     */
    public static void closeScanLight() {
        if (sActivityRef != null && sActivityRef.get() != null) {
            sActivityRef.get().closeLight();
        }
    }

    /**
     * 是否开启手电筒
     */
    public static boolean isLightOn() {
        if (sActivityRef != null && sActivityRef.get() != null) {
            return sActivityRef.get().is_light_on;
        }
        return false;
    }

}
