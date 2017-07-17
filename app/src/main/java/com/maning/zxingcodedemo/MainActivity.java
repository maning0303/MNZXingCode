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

import com.maning.library.zxing.CaptureActivity;
import com.maning.library.zxing.ZXingConstants;
import com.maning.library.zxing.utils.ZXingUtils;

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

        Intent intent = new Intent(MainActivity.this,
                CaptureActivity.class);
        intent.putExtra(ZXingConstants.ScanIsShowHistory, true);
        startActivityForResult(intent, 0x001);
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
        if (data == null) {
            return;
        }
        if (resultCode == ZXingConstants.ScanRequltCode) {
            /**
             * 拿到解析完成的字符串
             */
            String result = data.getStringExtra(ZXingConstants.ScanResult);
            textView.setText(result);
        } else if (resultCode == ZXingConstants.ScanHistoryResultCode) {
            /**
             * 历史记录
             */
            //自己实现历史页面
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        }
    }
}
