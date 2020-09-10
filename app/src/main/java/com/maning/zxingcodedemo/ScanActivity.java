package com.maning.zxingcodedemo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.other.OnScanSurfaceViewCallback;
import com.google.zxing.client.android.view.ScanSurfaceView;

public class ScanActivity extends AppCompatActivity {

    private ScanSurfaceView mScanSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initView();
    }

    private void initView() {
        mScanSurfaceView = (ScanSurfaceView) findViewById(R.id.scan_surface_view);
        mScanSurfaceView.init(this);
        MNScanConfig scanConfig = new MNScanConfig.Builder()
                .setSupportZoom(false)
                .builder();
        mScanSurfaceView.setScanConfig(scanConfig);

        mScanSurfaceView.setOnScanSurfaceViewCallback(new OnScanSurfaceViewCallback() {
            @Override
            public void onHandleDecode(String resultTxt, Bitmap barcode) {
                Toast.makeText(ScanActivity.this, "成功：" + resultTxt, Toast.LENGTH_SHORT).show();
                //关闭页面
                finish();
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(ScanActivity.this, "失败：" + msg, Toast.LENGTH_SHORT).show();
                //关闭页面
                finish();
            }

            @Override
            public void onCameraInitSuccess() {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScanSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScanSurfaceView.onDestroy();
    }
}
