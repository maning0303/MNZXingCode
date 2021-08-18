package com.google.zxing.client.android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.VpnService;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.R;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.manager.BeepManager;
import com.google.zxing.client.android.manager.InactivityTimer;
import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.other.OnScanCallback;

import java.util.Collection;

/**
 * @author : maning
 * @date : 2020-09-09
 * @desc :
 */
public class ScanSurfaceView extends FrameLayout implements SurfaceHolder.Callback {

    private static final String TAG = "ScanSurfaceView";
    public static MNScanConfig scanConfig;
    private ResizeAbleSurfaceView surfaceView;
    private ViewfinderView viewfinderView;
    private CameraManager cameraManager;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private OnScanCallback onScanCallback;
    private ScanSurfaceViewHandler scanSurfaceViewHandler;
    private ZoomControllerView zoomControllerView;
    private ScanResultPointView resultPointView;

    private Collection<BarcodeFormat> decodeFormats;
    private String characterSet;
    private boolean flagStop = false;
    private boolean hasSurface = false;

    public ScanSurfaceView(Context context) {
        this(context, null);
    }

    public ScanSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.mn_scan_surface_view, this);
        surfaceView = view.findViewById(R.id.preview_view);
        viewfinderView = view.findViewById(R.id.viewfinder_view);
        zoomControllerView = view.findViewById(R.id.zoom_controller_view);
        resultPointView = view.findViewById(R.id.result_point_view);

        //点击强行更新相机聚焦
//        zoomControllerView.setOnSingleClickListener(new ZoomControllerView.OnSingleClickListener() {
//            @Override
//            public void onSingleClick(View view) {
//                Log.i(">>>>>>>>", "zoomControllerView.setOnSingleClickListener");
//                if (cameraManager != null && hasSurface) {
//                    Camera camera = cameraManager.getCamera();
//                    if (camera != null) {
//                        camera.autoFocus(new Camera.AutoFocusCallback() {
//                            @Override
//                            public void onAutoFocus(boolean success, Camera camera) {
//                                Log.i(">>>>>>>>", "onAutoFocus:" + success);
//                                if (!success) {
//                                    camera.autoFocus(this);//如果失败，自动聚焦
//                                }
//                            }
//                        });
//
//                    }
//                }
//            }
//        });
        zoomControllerView.setOnZoomControllerListener(new ZoomControllerView.OnZoomControllerListener() {
            @Override
            public void onZoom(int progress) {
                if (getCameraManager() != null) {
                    getCameraManager().setZoom(progress);
                }
            }
        });
        resultPointView.setOnResultPointClickListener(new ScanResultPointView.OnResultPointClickListener() {
            @Override
            public void onPointClick(String result) {
                if (onScanCallback != null) {
                    onScanCallback.onScanSuccess(result, null);
                }
            }

            @Override
            public void onCancle() {
                hideResultPointView();
                restartScan();
            }
        });
    }

    /**
     * 结果点是否显示
     * @return
     */
    public boolean isResultPointViewShow(){
        return resultPointView.getVisibility() == View.VISIBLE;
    }

    public void hideResultPointView(){
        resultPointView.removeAllPoints();
        resultPointView.setVisibility(View.GONE);
        zoomControllerView.setVisibility(View.VISIBLE);
        viewfinderView.setVisibility(View.VISIBLE);
        if (onScanCallback != null) {
            onScanCallback.onRestartScan();
        }
    }

    public void init() {
        cameraManager = new CameraManager(getContext().getApplicationContext());
        scanSurfaceViewHandler = new ScanSurfaceViewHandler(this, decodeFormats, null, characterSet, cameraManager);
        scanConfig = new MNScanConfig.Builder().builder();
    }

    public void init(Activity activity) {
        inactivityTimer = new InactivityTimer(activity);
        beepManager = new BeepManager(activity);
        init();
    }

    public void setScanConfig(MNScanConfig config) {
        if (config == null) {
            config = new MNScanConfig.Builder().builder();
        }
        ScanSurfaceView.scanConfig = config;
        viewfinderView.setScanConfig(ScanSurfaceView.scanConfig);
        zoomControllerView.setScanConfig(ScanSurfaceView.scanConfig);
        resultPointView.setScanConfig(ScanSurfaceView.scanConfig);
    }

    public void setOnScanCallback(OnScanCallback callback) {
        onScanCallback = callback;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public ScanSurfaceViewHandler getCaptureHandler() {
        return scanSurfaceViewHandler;
    }

    public MNScanConfig getScanConfig() {
        return scanConfig;
    }

    public void handleDecode(Result[] rawResult, Bitmap barcode, float scaleFactor) {
        Log.i(">>>>>>", "scaleFactor---：" + scaleFactor);
        if (rawResult.length <= 0) {
            return;
        }
        if (flagStop) {
            return;
        }
        flagStop = true;
        if (beepManager != null) {
            beepManager.playBeepSoundAndVibrate();
        }
        zoomControllerView.setVisibility(View.GONE);
        viewfinderView.cleanCanvas();

        //展示结果点
        resultPointView.setResizeAbleSurfaceView(surfaceView);
        resultPointView.setScanSurfaceView(this);
        resultPointView.setViewfinderView(viewfinderView);
        resultPointView.setCameraManager(getCameraManager());
        resultPointView.setDatas(rawResult, barcode, scaleFactor);
        resultPointView.setVisibility(View.VISIBLE);
        stopScan();
        if (onScanCallback != null) {
            onScanCallback.onStopScan();
        }
        //一个直接返回
        if (rawResult.length == 1) {
            if (onScanCallback != null) {
                onScanCallback.onScanSuccess(rawResult[0].getText(), barcode);
            }
        }

    }

    public void stopScan() {
        onPause();
    }

    public void restartScan() {
        onResume();
    }

    public void onPause() {
        flagStop = true;
        if (scanSurfaceViewHandler != null) {
            scanSurfaceViewHandler.quitSynchronously();
            scanSurfaceViewHandler = null;
        }
        if (inactivityTimer != null) {
            inactivityTimer.onPause();
        }
        if (beepManager != null) {
            beepManager.close();
        }
        cameraManager.closeDriver();
        zoomControllerView.setVisibility(View.GONE);
        viewfinderView.cleanCanvas();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    public void onResume() {
        flagStop = false;
        viewfinderView.cleanCanvas();
        if (scanSurfaceViewHandler != null && cameraManager != null && cameraManager.isOpen()) {
            return;
        }
        scanSurfaceViewHandler = null;

        viewfinderView.setCameraManager(cameraManager);
        viewfinderView.setVisibility(View.VISIBLE);
        zoomControllerView.setVisibility(View.VISIBLE);
        resultPointView.removeAllPoints();
        resultPointView.setVisibility(View.GONE);

        if (beepManager != null) {
            beepManager.updatePrefs(scanConfig.isShowBeep(), scanConfig.isShowVibrate());
        }
        if (inactivityTimer != null) {
            inactivityTimer.onResume();
        }

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

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            displayFrameworkBugMessageAndExit("初始化相机失败");
            return;
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder, surfaceView);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (scanSurfaceViewHandler == null) {
                scanSurfaceViewHandler = new ScanSurfaceViewHandler(this, decodeFormats, null, characterSet, cameraManager);
            }
        } catch (Exception e) {
            Log.e(TAG, "open camera fail：" + e.toString());
            displayFrameworkBugMessageAndExit("初始化相机失败");
        }
        //刷新控制器
        zoomControllerView.updateZoomController(getCameraManager().getFramingRect());
    }

    private void displayFrameworkBugMessageAndExit(String msg) {
        if (onScanCallback != null) {
            onScanCallback.onFail(msg);
        }
    }

    public void onDestroy() {
        if (inactivityTimer != null) {
            inactivityTimer.shutdown();
        }
        if (scanSurfaceViewHandler != null) {
            scanSurfaceViewHandler.destroyView();
        }
        if (cameraManager != null) {
            cameraManager.stopPreview();
        }
        if (viewfinderView != null) {
            viewfinderView.destroyView();
        }
        scanConfig = null;
        inactivityTimer = null;
        scanSurfaceViewHandler = null;
        onScanCallback = null;
        beepManager = null;
        cameraManager = null;
        viewfinderView = null;
        surfaceView = null;
        decodeFormats = null;

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, ">>>>>>surfaceChanged---width：" + width + "，height:" + height);
        ViewGroup.LayoutParams layoutParams = resultPointView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        resultPointView.setLayoutParams(layoutParams);
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

}
