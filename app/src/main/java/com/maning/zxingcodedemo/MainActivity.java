package com.maning.zxingcodedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.baozi.Zxing.CaptureActivity;
import com.baozi.Zxing.ZXingConstants;

public class MainActivity extends AppCompatActivity{

    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.tv_show);

    }


    public void scanCode(View view) {
        Intent intent = new Intent(MainActivity.this,
                CaptureActivity.class);
        intent.putExtra(ZXingConstants.ScanIsShowHistory,true);
        startActivityForResult(intent, ZXingConstants.ScanRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case ZXingConstants.ScanRequestCode:
                if(resultCode == ZXingConstants.ScanRequestCode){
                    /**
                     * 拿到解析完成的字符串
                     */
                    String result = data.getStringExtra(ZXingConstants.ScanResult);
                    textView.setText(result);
                }else if(resultCode == ZXingConstants.ScanHistoryResultCode){
                    /**
                     * 历史记录
                     */
                    String resultHistory = data.getStringExtra(ZXingConstants.ScanHistoryResult);
                    if(!TextUtils.isEmpty(resultHistory)){
                        //自己实现历史页面
                        startActivity(new Intent(MainActivity.this,HistoryActivity.class));
                    }
                }
                break;
        }
    }
}
