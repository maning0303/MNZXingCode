package com.google.zxing.client.android;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.other.ActResultRequest;
import com.google.zxing.client.android.other.MNScanCallback;

/**
 * Created by maning on 2017/12/7.
 * 启动扫描的主类
 */

public class MNScanManager {

    //常量
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAIL = 1;
    public static final int RESULT_CANCLE = 2;
    public static final String INTENT_KEY_RESULT_SUCCESS = "intent_key_result_success";
    public static final String INTENT_KEY_RESULT_ERROR = "intent_key_result_error";


    //跳转传入的数据
    public static final String INTENT_KEY_HINTTEXT = "intent_key_hinttext";
    public static final String INTENT_KEY_SCSNCOLOR = "intent_key_scsncolor";
    public static final String INTENT_KEY_PHOTO_FLAG = "intent_key_photo_flag";
    public static final String INTENT_KEY_BEEP_FLAG = "intent_key_beep_flag";
    public static final String INTENT_KEY_VIBRATE_FLAG = "intent_key_vibrate_flag";


    public static void startScan(Activity activity, MNScanCallback scanCallback) {
        Intent intent = new Intent(activity.getApplicationContext(), CaptureActivity.class);
        ActResultRequest actResultRequest = new ActResultRequest(activity);
        actResultRequest.startForResult(intent, scanCallback);
        activity.overridePendingTransition(R.anim.mn_scan_activity_bottom_in, 0);
    }

    public static void startScan(Activity activity, MNScanConfig mnScanConfig, MNScanCallback scanCallback) {
        if (mnScanConfig == null) {
            mnScanConfig = new MNScanConfig.Builder().builder();
        }
        Intent intent = new Intent(activity.getApplicationContext(), CaptureActivity.class);
        //是否显示相册按钮
        intent.putExtra(MNScanManager.INTENT_KEY_PHOTO_FLAG, mnScanConfig.isShowPhotoAlbum());
        //识别声音
        intent.putExtra(MNScanManager.INTENT_KEY_BEEP_FLAG, mnScanConfig.isShowBeep());
        //识别震动
        intent.putExtra(MNScanManager.INTENT_KEY_VIBRATE_FLAG, mnScanConfig.isShowVibrate());
        //扫码框的颜色
        intent.putExtra(MNScanManager.INTENT_KEY_SCSNCOLOR, mnScanConfig.getScanColor());
        //扫码框上面的提示文案
        intent.putExtra(MNScanManager.INTENT_KEY_HINTTEXT, mnScanConfig.getScanHintText());

        ActResultRequest actResultRequest = new ActResultRequest(activity);
        actResultRequest.startForResult(intent, scanCallback);
        activity.overridePendingTransition(mnScanConfig.getActivityOpenAnime(), 0);
    }

}
