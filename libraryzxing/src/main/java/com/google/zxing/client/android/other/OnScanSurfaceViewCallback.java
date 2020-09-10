package com.google.zxing.client.android.other;

import android.graphics.Bitmap;

/**
 * @author : maning
 * @date : 2020-09-09
 * @desc :
 */
public interface OnScanSurfaceViewCallback {
    void onHandleDecode(String resultTxt, Bitmap barcode);

    void onFail(String msg);

    void onCameraInitSuccess();
}
