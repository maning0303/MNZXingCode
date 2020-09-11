package com.google.zxing.client.android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.R;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.manager.BeepManager;
import com.google.zxing.client.android.manager.InactivityTimer;
import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.other.OnScanSurfaceViewCallback;

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
    private OnScanSurfaceViewCallback onScanSurfaceViewCallback;
    private ScanSurfaceViewHandler scanSurfaceViewHandler;
    private Collection<BarcodeFormat> decodeFormats;
    private String characterSet;
    private boolean hasScanComplete = false;
    private boolean hasSurface = false;
    private ZoomControllerView zoomControllerView;

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

        zoomControllerView.setOnZoomControllerListener(new ZoomControllerView.OnZoomControllerListener() {
            @Override
            public void onZoom(int progress) {
                if (getCameraManager() != null) {
                    getCameraManager().setZoom(progress);
                }
            }
        });
    }

    public void init(Activity activity) {
        inactivityTimer = new InactivityTimer(activity);
        beepManager = new BeepManager(activity);
        cameraManager = new CameraManager(getContext().getApplicationContext());
        scanSurfaceViewHandler = new ScanSurfaceViewHandler(this, decodeFormats, null, characterSet, cameraManager);
    }

    public void setScanConfig(MNScanConfig config) {
        if (config == null) {
            config = new MNScanConfig.Builder().builder();
        }
        ScanSurfaceView.scanConfig = config;
        viewfinderView.setScanConfig(ScanSurfaceView.scanConfig);
        zoomControllerView.setScanConfig(ScanSurfaceView.scanConfig);
    }

    public void setOnScanSurfaceViewCallback(OnScanSurfaceViewCallback callback) {
        onScanSurfaceViewCallback = callback;
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

    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        if (hasScanComplete) {
            return;
        }
        hasScanComplete = true;
        beepManager.playBeepSoundAndVibrate();
        viewfinderView.setResultPoint(rawResult, scaleFactor);
        //结果返回
        if (onScanSurfaceViewCallback != null) {
            onScanSurfaceViewCallback.onHandleDecode(rawResult.getText(), barcode);
        }
    }

    public void onPause() {
        if (scanSurfaceViewHandler != null) {
            scanSurfaceViewHandler.quitSynchronously();
            scanSurfaceViewHandler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    public void onResume() {
        if (scanSurfaceViewHandler != null && cameraManager != null && cameraManager.isOpen()) {
            return;
        }
        scanSurfaceViewHandler = null;

        viewfinderView.setCameraManager(cameraManager);
        viewfinderView.setVisibility(View.VISIBLE);

        beepManager.updatePrefs(scanConfig.isShowBeep(), scanConfig.isShowVibrate());

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
        //初始化完成
        if (onScanSurfaceViewCallback != null) {
            onScanSurfaceViewCallback.onCameraInitSuccess();
        }
        //刷新控制器
        zoomControllerView.updateZoomController(getCameraManager().getFramingRect());
    }

    private void displayFrameworkBugMessageAndExit(String msg) {
        if (onScanSurfaceViewCallback != null) {
            onScanSurfaceViewCallback.onFail(msg);
        }
    }

    public void onDestroy() {
        if (inactivityTimer != null) {
            inactivityTimer.shutdown();
        }
        scanConfig = null;
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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
