package com.maning.zxingcodedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
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

    }


    public void scanCode(View view) {
        Intent intent = new Intent(this, CaptureActivity.class);
        //是否显示相册按钮
        intent.putExtra(CaptureActivity.INTENT_KEY_PHOTO_FLAG, true);
        //识别声音
        intent.putExtra(CaptureActivity.INTENT_KEY_BEEP_FLAG, true);
        //识别震动
        intent.putExtra(CaptureActivity.INTENT_KEY_VIBRATE_FLAG, true);
        //扫码框的颜色
        intent.putExtra(CaptureActivity.INTENT_KEY_SCSNCOLOR, "#FFFF00");
        //扫码框上面的提示文案
        intent.putExtra(CaptureActivity.INTENT_KEY_HINTTEXT, "请将二维码放入框中....");
        startActivityForResult(intent, 1000);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (data == null) {
                return;
            }
            switch (resultCode) {
                case CaptureActivity.RESULT_SUCCESS:
                    String resultSuccess = data.getStringExtra(CaptureActivity.INTENT_KEY_RESULT_SUCCESS);
                    showToast(resultSuccess);
                    textView.setText(resultSuccess);
                    break;
                case CaptureActivity.RESULT_FAIL:
                    String resultError = data.getStringExtra(CaptureActivity.INTENT_KEY_RESULT_ERROR);
                    showToast(resultError);
                    break;
                case CaptureActivity.RESULT_CANCLE:
                    showToast("取消扫码");
                    break;
            }
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
