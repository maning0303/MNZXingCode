package com.maning.zxingcodedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.MNScanManager;
import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.other.MNCustomViewBindCallback;
import com.google.zxing.client.android.other.MNScanCallback;
import com.google.zxing.client.android.utils.ZXingUtils;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    private ImageView imageView;
    private EditText editText;
    private CheckBox checkbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.tv_show);
        imageView = (ImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
        checkbox = (CheckBox) findViewById(R.id.checkbox);

        requestCameraPerm();
    }

    public void requestCameraPerm() {
        //判断权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 10010);
            }
        }
    }

    public void scanCodeDefault(View view) {
        //需要判断有没有权限
        MNScanManager.startScan(this, new MNScanCallback() {
            @Override
            public void onActivityResult(int resultCode, Intent data) {
                handlerResult(resultCode, data);
            }
        });
    }

    public void scanCode(View view) {
        //需要判断有没有权限
        MNScanConfig scanConfig = new MNScanConfig.Builder()
                //设置完成震动
                .isShowVibrate(false)
                //扫描完成声音
                .isShowBeep(true)
                //显示相册功能
                .isShowPhotoAlbum(true)
                //显示闪光灯
                .isShowLightController(true)
                //打开扫描页面的动画
                .setActivityOpenAnime(R.anim.activity_anmie_in)
                //退出扫描页面动画
                .setActivityExitAnime(R.anim.activity_anmie_out)
                //自定义文案
                .setScanHintText("请将二维码放入框中")
                .setScanHintTextColor("#FFFF00")
                .setScanHintTextSize(16)
                //扫描线的颜色
                .setScanColor("#FFFF00")
                //是否显示缩放控制器
                .isShowZoomController(true)
                //显示缩放控制器位置
                .setZoomControllerLocation(MNScanConfig.ZoomControllerLocation.Bottom)
                //扫描线样式
                .setLaserStyle(MNScanConfig.LaserStyle.Grid)
                //背景颜色
                .setBgColor("#33FF0000")
                //网格扫描线的列数
                .setGridScanLineColumn(30)
                //网格高度
                .setGridScanLineHeight(150)
                //自定义遮罩
                .setCustomShadeViewLayoutID(R.layout.layout_custom_view, new MNCustomViewBindCallback() {
                    @Override
                    public void onBindView(View customView) {
                        ImageView iv_back = customView.findViewById(R.id.iv_back);
                        ImageView iv_photo = customView.findViewById(R.id.iv_photo);
                        LinearLayout btn_scan_light = customView.findViewById(R.id.btn_scan_light);
                        final ImageView iv_scan_light = customView.findViewById(R.id.iv_scan_light);
                        final TextView tv_scan_light = customView.findViewById(R.id.tv_scan_light);
                        LinearLayout btn_my_card = customView.findViewById(R.id.btn_my_card);
                        LinearLayout btn_scan_record = customView.findViewById(R.id.btn_scan_record);
                        iv_back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //关闭扫描页面
                                MNScanManager.closeScanPage();
                            }
                        });
                        btn_scan_light.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //手电筒
                                if (MNScanManager.isLightOn()) {
                                    MNScanManager.closeScanLight();
                                    iv_scan_light.setImageResource(R.drawable.icon_custom_light_close);
                                    tv_scan_light.setText("开启手电筒");
                                } else {
                                    MNScanManager.openScanLight();
                                    iv_scan_light.setImageResource(R.drawable.icon_custom_light_open);
                                    tv_scan_light.setText("关闭手电筒");
                                }
                            }
                        });
                        iv_photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //打开相册扫描
                                MNScanManager.openAlbumPage();
                            }
                        });
                        btn_my_card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //我的名片
                                showToast("我的名片");
                            }
                        });
                        btn_scan_record.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //扫码记录
                                showToast("扫码记录");
                            }
                        });
                    }
                })
                .builder();
        MNScanManager.startScan(this, scanConfig, new MNScanCallback() {
            @Override
            public void onActivityResult(int resultCode, Intent data) {
                handlerResult(resultCode, data);
            }
        });
    }


    public void createQRImage(View view) {
        String str = editText.getText().toString();

        if (TextUtils.isEmpty(str)) {
            Toast.makeText(this, "字符串不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap qrImage;
        if (checkbox.isChecked()) {
            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            qrImage = ZXingUtils.createQRCodeWithLogo(str, logo);
        } else {
            qrImage = ZXingUtils.createQRImage(str);
        }

        if (qrImage != null) {
            imageView.setImageBitmap(qrImage);
        } else {
            Toast.makeText(this, "生成失败", Toast.LENGTH_SHORT).show();
        }

    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    private void handlerResult(int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        switch (resultCode) {
            case MNScanManager.RESULT_SUCCESS:
                String resultSuccess = data.getStringExtra(MNScanManager.INTENT_KEY_RESULT_SUCCESS);
                showToast(resultSuccess);
                textView.setText("扫描结果显示：" + resultSuccess);
                break;
            case MNScanManager.RESULT_FAIL:
                String resultError = data.getStringExtra(MNScanManager.INTENT_KEY_RESULT_ERROR);
                showToast(resultError);
                break;
            case MNScanManager.RESULT_CANCLE:
                showToast("取消扫码");
                break;
        }
    }
}
