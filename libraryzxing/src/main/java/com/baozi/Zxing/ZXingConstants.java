package com.baozi.Zxing;

/**
 * Created by maning on 16/1/21.
 * 常量类
 */
public class ZXingConstants {

    //开启二维码扫描的请求码
    public static final int ScanRequestCode = 0x003;
    //二维码扫描界面到相册获取图片的请求码
    public static final int ScanPhotosRequestCode = 0x001;
    //二维码扫描返回的Intent结果
    public static final String ScanResult = "ScanResult";
    //是否显示历史信息
    public static final String ScanIsShowHistory = "ScanIsShowHistory";
    //二维码扫描点击历史记录回调回去
    public static final String ScanHistoryResult = "ScanHistoryResult";
    public static final int ScanHistoryResultCode = 0x002;


}
