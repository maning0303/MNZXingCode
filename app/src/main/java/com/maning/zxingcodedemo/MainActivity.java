package com.maning.zxingcodedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.MNScanManager;
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
        initView();

        requestCameraPerm();
    }

    private void initView() {
        textView = (TextView) findViewById(R.id.tv_show);
        imageView = (ImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
        checkbox = (CheckBox) findViewById(R.id.checkbox);
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

    public void customScan(View view) {
        startActivity(new Intent(this, CustomActivity.class));
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
            default:
                break;
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

    public void customActivity(View view) {
        startActivity(new Intent(this, ScanActivity.class));
    }
}
