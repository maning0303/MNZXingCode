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
    public static final String INTENT_KEY_CONFIG_MODEL = "INTENT_KEY_CONFIG_MODEL";


    public static void startScan(Activity activity, MNScanCallback scanCallback) {
        startScan(activity, null, scanCallback);
    }

    public static void startScan(Activity activity, MNScanConfig mnScanConfig, MNScanCallback scanCallback) {
        if (mnScanConfig == null) {
            mnScanConfig = new MNScanConfig.Builder().builder();
        }
        Intent intent = new Intent(activity.getApplicationContext(), CaptureActivity.class);
        //传递数据
        intent.putExtra(MNScanManager.INTENT_KEY_CONFIG_MODEL, mnScanConfig);
        ActResultRequest actResultRequest = new ActResultRequest(activity);
        actResultRequest.startForResult(intent, scanCallback);
        activity.overridePendingTransition(mnScanConfig.getActivityOpenAnime(), android.R.anim.fade_out);
    }

    /**
     * 关闭当前页面
     */
    public static void closeScanPage() {
        CaptureActivity.closeScanPage();
    }

    /**
     * 打开相册扫描图片
     */
    public static void openAlbumPage() {
        CaptureActivity.openAlbumPage();
    }

    /**
     * 打开手电筒
     */
    public static void openScanLight() {
        CaptureActivity.openScanLight();
    }

    /**
     * 关闭手电筒
     */
    public static void closeScanLight() {
        CaptureActivity.closeScanLight();
    }

    /**
     * 手电筒是否开启
     */
    public static boolean isLightOn() {
        return CaptureActivity.isLightOn();
    }

}
