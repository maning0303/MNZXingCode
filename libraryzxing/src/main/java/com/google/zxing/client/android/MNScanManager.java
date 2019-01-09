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
    public static final String INTENT_KEY_RESULT_SUCCESS = "INTENT_KEY_RESULT_SUCCESS";
    public static final String INTENT_KEY_RESULT_ERROR = "INTENT_KEY_RESULT_ERROR";


    //跳转传入的数据
    //提示文案
    public static final String INTENT_KEY_HINTTEXT = "INTENT_KEY_HINTTEXT";
    //扫描线的颜色
    public static final String INTENT_KEY_SCSNCOLOR = "INTENT_KEY_SCSNCOLOR";
    //相机是否显示
    public static final String INTENT_KEY_PHOTO_FLAG = "INTENT_KEY_PHOTO_FLAG";
    //识别声音
    public static final String INTENT_KEY_BEEP_FLAG = "INTENT_KEY_BEEP_FLAG";
    //识别震动
    public static final String INTENT_KEY_VIBRATE_FLAG = "INTENT_KEY_VIBRATE_FLAG";
    //扫描退出动画
    public static final String INTENT_KEY_ACTIVITY_EXIT_ANIME = "INTENT_KEY_ACTIVITY_EXIT_ANIME";
    //是否显示缩放控制器
    public static final String INTENT_KEY_ZOOM_CONTROLLER = "INTENT_KEY_ZOOM_CONTROLLER";


    public static void startScan(Activity activity, MNScanCallback scanCallback) {
        startScan(activity, null, scanCallback);
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
        //退出动画
        intent.putExtra(MNScanManager.INTENT_KEY_ACTIVITY_EXIT_ANIME, mnScanConfig.getActivityExitAnime());
        //是否显示缩放控制器
        intent.putExtra(MNScanManager.INTENT_KEY_ZOOM_CONTROLLER, mnScanConfig.isShowZoomController());

        ActResultRequest actResultRequest = new ActResultRequest(activity);
        actResultRequest.startForResult(intent, scanCallback);
        activity.overridePendingTransition(mnScanConfig.getActivityOpenAnime(), android.R.anim.fade_out);
    }

}
