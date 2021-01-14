package com.google.zxing.client.android.other;

import android.graphics.Bitmap;

/**
 * @author : maning
 * @date : 2020-09-09
 * @desc :
 */
public interface OnScanCallback {

    /**
     * 扫码成功
     * @param resultTxt
     * @param barcode
     */
    void onScanSuccess(String resultTxt, Bitmap barcode);

    /**
     * 暂停扫描
     */
    void onStopScan();

    /**
     * 重新扫描
     */
    void onRestartScan();

    /**
     * 失败
     * @param msg
     */
    void onFail(String msg);

}
