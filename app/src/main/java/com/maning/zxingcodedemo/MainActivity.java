package com.maning.zxingcodedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.client.android.MNScanManager;
import com.google.zxing.client.android.other.MNScanCallback;
import com.google.zxing.client.android.utils.ZXingUtils;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    private ImageView imageView;
    private EditText editText;
    private CheckBox checkbox;
    private Spinner mSpColorBlack;
    private Spinner mSpColorWhite;
    private Spinner mSpMargin;

    private int margin = 0;
    private int color_black = Color.BLACK;
    private int color_white = Color.WHITE;


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
        mSpColorBlack = (Spinner) findViewById(R.id.sp_color_black);
        mSpColorWhite = (Spinner) findViewById(R.id.sp_color_white);
        mSpMargin = (Spinner) findViewById(R.id.sp_margin);
        mSpMargin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                margin = Integer.parseInt(getResources().getStringArray(R.array.spinarr_margin)[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpColorBlack.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str_color_black = getResources().getStringArray(R.array.spinarr_color_black)[position];
                if (str_color_black.equals("黑色")) {
                    color_black = Color.BLACK;
                } else if (str_color_black.equals("白色")) {
                    color_black = Color.WHITE;
                } else if (str_color_black.equals("蓝色")) {
                    color_black = Color.BLUE;
                } else if (str_color_black.equals("绿色")) {
                    color_black = Color.GREEN;
                } else if (str_color_black.equals("黄色")) {
                    color_black = Color.YELLOW;
                } else if (str_color_black.equals("红色")) {
                    color_black = Color.RED;
                } else {
                    color_black = Color.BLACK;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpColorWhite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str_color_white = getResources().getStringArray(R.array.spinarr_color_white)[position];
                if (str_color_white.equals("黑色")) {
                    color_white = Color.BLACK;
                } else if (str_color_white.equals("白色")) {
                    color_white = Color.WHITE;
                } else if (str_color_white.equals("蓝色")) {
                    color_white = Color.BLUE;
                } else if (str_color_white.equals("绿色")) {
                    color_white = Color.GREEN;
                } else if (str_color_white.equals("黄色")) {
                    color_white = Color.YELLOW;
                } else if (str_color_white.equals("红色")) {
                    color_white = Color.RED;
                } else {
                    color_white = Color.WHITE;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        Bitmap logo = null;
        if (checkbox.isChecked()) {
            logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }
        qrImage = ZXingUtils.createQRCodeImage(str, 500, margin, color_black, color_white, logo);
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
