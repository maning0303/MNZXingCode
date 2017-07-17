package com.maning.library.zxing;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.maning.library.zxing.camera.CameraManager;
import com.maning.library.zxing.decoding.CaptureActivityHandler;
import com.maning.library.zxing.decoding.InactivityTimer;
import com.maning.library.zxing.utils.ZXingUtils;
import com.maning.library.zxing.view.ViewfinderView;
import com.maning.libraryzxing.R;

import java.io.IOException;
import java.util.Vector;


/**
 * 拍照的Activity
 */
public class CaptureActivity extends Activity implements Callback, OnClickListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private String photo_path;
    private ImageView scanLine;
    private TranslateAnimation translateAnimation;
    private ImageView mo_scanner_back;
    private ImageView mo_scanner_photo;
    private ImageView mo_scanner_histroy;
    private ImageView mo_scanner_light;
    private boolean isShowHistory;

    private static final int REQUEST_TAKE_PHOTO_PERMISSION = 111;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mo_scanner_main);

        initIntent();


        initView();
        // 初始化 CameraManager
        CameraManager.init(getApplication());

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);

        //权限判断
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_TAKE_PHOTO_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_TAKE_PHOTO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //申请成功
            } else {
                Toast.makeText(this, "相机权限被拒绝,关闭页面", Toast.LENGTH_SHORT).show();
                CaptureActivity.this.finish();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initAnimation() {
        //扫描动画
        translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation
                .RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.9f);
        translateAnimation.setDuration(4500);
        translateAnimation.setRepeatCount(-1);
        translateAnimation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(translateAnimation);
    }

    private void initIntent() {

        Intent intent = getIntent();
        isShowHistory = intent.getBooleanExtra(ZXingConstants.ScanIsShowHistory, false);

    }

    private void initView() {
        viewfinderView = (ViewfinderView) findViewById(R.id.mo_scanner_viewfinder_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
        mo_scanner_back = (ImageView) findViewById(R.id.mo_scanner_back);
        mo_scanner_photo = (ImageView) findViewById(R.id.mo_scanner_photo);
        mo_scanner_light = (ImageView) findViewById(R.id.mo_scanner_light);
        mo_scanner_histroy = (ImageView) findViewById(R.id.mo_scanner_histroy);


        mo_scanner_histroy.setOnClickListener(this);
        mo_scanner_back.setOnClickListener(this);
        mo_scanner_photo.setOnClickListener(this);
        mo_scanner_light.setOnClickListener(this);

        //默认隐藏历史记录
        mo_scanner_histroy.setVisibility(View.GONE);
        if (isShowHistory) {
            mo_scanner_histroy.setVisibility(View.VISIBLE);
        }

        //初始化动画
        initAnimation();

    }

    boolean flag = true;

    protected void light() {
        if (flag) {
            flag = false;
            // 开闪光灯
            CameraManager.get().openLight();
            mo_scanner_light.setBackgroundResource(R.drawable.zxing_circle_trans_red);
        } else {
            flag = true;
            // 关闪光灯
            CameraManager.get().offLight();
            mo_scanner_light.setBackgroundResource(R.drawable.zxing_circle_trans_black);
        }
        mo_scanner_light.setPadding(
                ZXingUtils.dip2px(this, 8),
                ZXingUtils.dip2px(this, 8),
                ZXingUtils.dip2px(this, 8),
                ZXingUtils.dip2px(this, 8)
        );

    }

    private void photo() {
        Intent innerIntent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            innerIntent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
        startActivityForResult(wrapperIntent, ZXingConstants.ScanPhotosRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ZXingConstants.ScanPhotosRequestCode:
                    //获取图片路径
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(data.getData(), proj, null, null, null);
                    if (cursor != null) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        if (cursor.moveToFirst()) {
                            photo_path = cursor.getString(columnIndex);
                        }
                        cursor.close();
                    }
                    //解析图片
                    analysisImage(photo_path);
                    break;
            }
        }
    }

    private void analysisImage(final String photoPath) {
        if (TextUtils.isEmpty(photoPath)) {
            Toast.makeText(this, R.string.libraryzxing_get_pic_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String result = ZXingUtils.syncDecodeQRCode(photoPath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(result)) {
                            Toast.makeText(CaptureActivity.this, R.string.libraryzxing_get_pic_fail, Toast.LENGTH_SHORT).show();
                        } else {
                            // 数据返回
                            Intent data = new Intent();
                            data.putExtra(ZXingConstants.ScanResult, result);
                            setResult(ZXingConstants.ScanRequltCode, data);
                            finish();
                        }
                    }
                });
            }
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.mo_scanner_preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        if (inactivityTimer != null) {
            inactivityTimer.shutdown();
            inactivityTimer = null;
        }
        if (translateAnimation != null) {
            translateAnimation.cancel();
            translateAnimation = null;
        }
        super.onDestroy();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (Exception ioe) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public void handleDecode(final Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String recode = ZXingUtils.recode(result.toString());
        // 数据返回
        Intent data = new Intent();
        data.putExtra(ZXingConstants.ScanResult, recode);
        setResult(ZXingConstants.ScanRequltCode, data);
        finish();
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.mo_scanner_beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.mo_scanner_back) {
            this.finish();
        } else if (id == R.id.mo_scanner_photo) {
            photo();
        } else if (id == R.id.mo_scanner_light) {
            light();
        } else if (id == R.id.mo_scanner_histroy) {
            // 数据返回
            Intent data = new Intent();
            setResult(ZXingConstants.ScanHistoryResultCode, data);
            this.finish();
        }
    }

}