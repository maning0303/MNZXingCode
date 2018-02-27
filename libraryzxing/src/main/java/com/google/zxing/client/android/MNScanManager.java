package com.google.zxing.client.android;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.client.android.model.MNScanConfig;

/**
 * Created by maning on 2017/12/7.
 * 启动扫描的主类
 */

public class MNScanManager {

    /**
     * 默认打开扫描页面
     *
     * @param context     上下文
     * @param requestCode If >= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     */
    public static void startScan(Activity context, int requestCode) {
        Intent intent = new Intent(context.getApplicationContext(), CaptureActivity.class);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.mn_scan_activity_bottom_in, 0);
    }

    public static void startScan(Activity context, int requestCode, MNScanConfig mnScanConfig) {
        Intent intent = new Intent(context.getApplicationContext(), CaptureActivity.class);
        //是否显示相册按钮
        intent.putExtra(CaptureActivity.INTENT_KEY_PHOTO_FLAG, mnScanConfig.isShowPhotoAlbum());
        //识别声音
        intent.putExtra(CaptureActivity.INTENT_KEY_BEEP_FLAG, mnScanConfig.isShowBeep());
        //识别震动
        intent.putExtra(CaptureActivity.INTENT_KEY_VIBRATE_FLAG, mnScanConfig.isShowVibrate());
        //扫码框的颜色
        intent.putExtra(CaptureActivity.INTENT_KEY_SCSNCOLOR, mnScanConfig.getScanColor());
        //扫码框上面的提示文案
        intent.putExtra(CaptureActivity.INTENT_KEY_HINTTEXT, mnScanConfig.getScanHintText());
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(mnScanConfig.getActivityOpenAnime(), 0);
    }

}
