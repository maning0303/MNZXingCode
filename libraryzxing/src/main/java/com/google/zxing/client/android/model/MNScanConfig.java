package com.google.zxing.client.android.model;

import android.view.View;

import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.R;
import com.google.zxing.client.android.other.MNCustomViewBindCallback;

import java.io.Serializable;

/**
 * Created by maning on 2017/12/7.
 * 启动Activity的一些配置参数
 */

public class MNScanConfig implements Serializable {

    private static final long serialVersionUID = -5260676142223049891L;

    //枚举类型：缩放控制器位置
    public enum ZoomControllerLocation {
        Bottom,
        Left,
        Right,
        None,
    }

    //枚举类型：扫描线样式
    public enum LaserStyle {
        Line,
        Grid,
    }

    //是否显示相册
    private boolean showPhotoAlbum;
    //扫描声音
    private boolean showBeep;
    //扫描震动
    private boolean showVibrate;
    //扫描框和扫描线的颜色
    private String scanColor;
    //扫描线的样式
    private LaserStyle laserStyle;
    //扫描提示文案
    private String scanHintText;
    //扫描提示文案颜色
    private String scanHintTextColor;
    //扫描提示文案字体大小
    private int scanHintTextSize;
    //开启Activity动画
    private int activityOpenAnime;
    //关闭Activity动画
    private int activityExitAnime;
    //是否显示缩放控制器
    private boolean showZoomController = true;
    //是否支持手势缩放，默认支持
    private boolean isSupportZoom = true;
    //控制器位置
    private ZoomControllerLocation zoomControllerLocation = ZoomControllerLocation.Right;
    //自定义View
    private int customShadeViewLayoutID;
    //扫描背景色
    private String bgColor;
    //网格扫描线的列数
    private int gridScanLineColumn;
    //网格扫描线的高度
    private int gridScanLineHeight;
    //显示闪光灯
    private boolean showLightController = true;
    //扫描框高度偏移值：+向上偏移，-向下偏移 （单位px）
    private int scanFrameHeightOffsets;
    //是否需要全屏扫描，默认值扫描扫描框中的二维码
    private boolean isFullScreenScan = false;
    //是否显示扫描中心点
    private boolean isShowResultPoint = false;
    //扫描二维码中心点显示半径
    private int resultPointRadiusCircle = 0;
    //扫描二维码中心点显示圆角
    private int resultPointCorners = 0;
    //扫描二维码中心点显示描边
    private int resultPointStrokeWidth = 0;
    //扫描二维码中心点显示描边颜色
    private String resultPointStrokeColor;
    //扫描二维码中心点显示颜色
    private String resultPointColor;
    //状态栏颜色
    private String statusBarColor = "#00000000";
    //状态栏是否显示黑色字体
    private boolean statusBarDarkMode = false;

    private MNScanConfig() {

    }


    private MNScanConfig(Builder builder) {
        showPhotoAlbum = builder.showPhotoAlbum;
        showBeep = builder.showBeep;
        showVibrate = builder.showVibrate;
        scanColor = builder.scanColor;
        laserStyle = builder.laserStyle;
        scanHintText = builder.scanHintText;
        activityOpenAnime = builder.activityOpenAnime;
        activityExitAnime = builder.activityExitAnime;
        showZoomController = builder.showZoomController;
        zoomControllerLocation = builder.zoomControllerLocation;
        customShadeViewLayoutID = builder.customShadeViewLayoutID;
        bgColor = builder.bgColor;
        gridScanLineColumn = builder.gridScanLineColumn;
        gridScanLineHeight = builder.gridScanLineHeight;
        showLightController = builder.showLightController;
        scanHintTextColor = builder.scanHintTextColor;
        scanHintTextSize = builder.scanHintTextSize;
        scanFrameHeightOffsets = builder.scanFrameHeightOffsets;
        isFullScreenScan = builder.isFullScreenScan;
        isSupportZoom = builder.isSupportZoom;
        isShowResultPoint = builder.isShowResultPoint;
        resultPointRadiusCircle = builder.resultPointRadiusCircle;
        resultPointCorners = builder.resultPointCorners;
        resultPointStrokeWidth = builder.resultPointStrokeWidth;
        resultPointStrokeColor = builder.resultPointStrokeColor;
        resultPointColor = builder.resultPointColor;
        statusBarColor = builder.statusBarColor;
        statusBarDarkMode = builder.statusBarDarkMode;

    }

    public String getStatusBarColor() {
        return statusBarColor;
    }

    public boolean isStatusBarDarkMode() {
        return statusBarDarkMode;
    }

    public boolean isShowResultPoint() {
        return isShowResultPoint;
    }

    public int getResultPointRadiusCircle() {
        return resultPointRadiusCircle;
    }

    public int getResultPointCorners() {
        return resultPointCorners;
    }

    public int getResultPointStrokeWidth() {
        return resultPointStrokeWidth;
    }

    public String getResultPointStrokeColor() {
        return resultPointStrokeColor;
    }

    public String getResultPointColor() {
        return resultPointColor;
    }

    public boolean isShowPhotoAlbum() {
        return showPhotoAlbum;
    }

    public boolean isShowBeep() {
        return showBeep;
    }

    public boolean isShowVibrate() {
        return showVibrate;
    }

    public String getScanColor() {
        return scanColor;
    }

    public LaserStyle getLaserStyle() {
        return laserStyle;
    }

    public String getScanHintText() {
        return scanHintText;
    }

    public String getScanHintTextColor() {
        return scanHintTextColor;
    }

    public int getScanHintTextSize() {
        return scanHintTextSize;
    }

    public int getActivityOpenAnime() {
        return activityOpenAnime;
    }

    public int getActivityExitAnime() {
        return activityExitAnime;
    }

    public boolean isShowZoomController() {
        return showZoomController;
    }

    public ZoomControllerLocation getZoomControllerLocation() {
        return zoomControllerLocation;
    }

    public int getCustomShadeViewLayoutID() {
        return customShadeViewLayoutID;
    }

    public String getBgColor() {
        return bgColor;
    }

    public int getGridScanLineColumn() {
        return gridScanLineColumn;
    }

    public int getGridScanLineHeight() {
        return gridScanLineHeight;
    }

    public boolean isShowLightController() {
        return showLightController;
    }

    public int getScanFrameHeightOffsets() {
        return scanFrameHeightOffsets;
    }

    public boolean isFullScreenScan() {
        return isFullScreenScan;
    }

    public boolean isSupportZoom() {
        return isSupportZoom;
    }

    public static class Builder {
        private boolean showPhotoAlbum = true;
        private boolean showBeep = true;
        private boolean showVibrate = true;
        private String scanColor;
        private LaserStyle laserStyle = LaserStyle.Line;
        private int activityOpenAnime = R.anim.mn_scan_activity_bottom_in;
        private int activityExitAnime = R.anim.mn_scan_activity_bottom_out;
        private boolean showZoomController = true;
        private ZoomControllerLocation zoomControllerLocation = ZoomControllerLocation.Right;
        private int customShadeViewLayoutID;
        private String bgColor;
        //网格扫描线的列数
        private int gridScanLineColumn;
        //网格扫描线的高度
        private int gridScanLineHeight;
        //闪光灯
        private boolean showLightController = true;
        //扫描提示文案
        private String scanHintText = "将二维码放入框内，即可自动扫描";
        //扫描提示文案颜色
        private String scanHintTextColor;
        //扫描提示文案字体大小
        private int scanHintTextSize;
        //扫描框高度偏移值：+向上偏移，-向下偏移
        private int scanFrameHeightOffsets = 100;
        //是否需要全屏扫描，默认值扫描扫描框中的二维码
        private boolean isFullScreenScan = false;
        //是否支持手势缩放，默认支持
        private boolean isSupportZoom = true;
        //是否显示扫描中心点
        private boolean isShowResultPoint = false;
        //扫描二维码中心点显示半径
        private int resultPointRadiusCircle = 0;
        //扫描二维码中心点显示圆角
        private int resultPointCorners = 0;
        //扫描二维码中心点显示描边
        private int resultPointStrokeWidth = 0;
        //扫描二维码中心点显示描边颜色
        private String resultPointStrokeColor;
        //扫描二维码中心点显示颜色
        private String resultPointColor;
        //状态栏颜色
        private String statusBarColor = "#00000000";
        //状态栏是否显示黑色字体
        private boolean statusBarDarkMode = false;

        public MNScanConfig builder() {
            return new MNScanConfig(this);
        }

        public Builder setStatusBarConfigs(String statusBarColor,
                                           boolean statusBarDarkMode) {
            this.statusBarColor = statusBarColor;
            this.statusBarDarkMode = statusBarDarkMode;
            return this;
        }

        public Builder isShowResultPoint(boolean isShowResultPoint) {
            this.isShowResultPoint = isShowResultPoint;
            return this;
        }

        public Builder setResultPointConfigs(int resultPointRadiusCircle,
                                             int resultPointCorners,
                                             int resultPointStrokeWidth,
                                             String resultPointStrokeColor,
                                             String resultPointColor) {
            this.resultPointRadiusCircle = resultPointRadiusCircle;
            this.resultPointCorners = resultPointCorners;
            this.resultPointStrokeWidth = resultPointStrokeWidth;
            this.resultPointStrokeColor = resultPointStrokeColor;
            this.resultPointColor = resultPointColor;
            return this;
        }

        public Builder setLaserStyle(LaserStyle laserStyle) {
            this.laserStyle = laserStyle;
            return this;
        }

        public Builder isShowPhotoAlbum(boolean showPhotoAlbum) {
            this.showPhotoAlbum = showPhotoAlbum;
            return this;
        }

        public Builder isShowBeep(boolean showBeep) {
            this.showBeep = showBeep;
            return this;
        }

        public Builder isShowVibrate(boolean showVibrate) {
            this.showVibrate = showVibrate;
            return this;
        }

        public Builder setScanColor(String scanColor) {
            this.scanColor = scanColor;
            return this;
        }

        public Builder setScanHintText(String scanHintText) {
            this.scanHintText = scanHintText;
            return this;
        }

        public Builder setActivityOpenAnime(int activityOpenAnime) {
            this.activityOpenAnime = activityOpenAnime;
            return this;
        }

        public Builder setActivityExitAnime(int activityExitAnime) {
            this.activityExitAnime = activityExitAnime;
            return this;
        }

        public Builder isShowZoomController(boolean showZoomController) {
            this.showZoomController = showZoomController;
            return this;
        }

        public Builder setZoomControllerLocation(ZoomControllerLocation zoomControllerLocation) {
            this.zoomControllerLocation = zoomControllerLocation;
            return this;
        }

        public Builder setCustomShadeViewLayoutID(int customShadeViewLayoutID, MNCustomViewBindCallback mnCustomViewBindCallback) {
            this.customShadeViewLayoutID = customShadeViewLayoutID;
            CaptureActivity.setMnCustomViewBindCallback(mnCustomViewBindCallback);
            return this;
        }

        public Builder setBgColor(String bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public Builder setGridScanLineColumn(int gridScanLineColumn) {
            this.gridScanLineColumn = gridScanLineColumn;
            return this;
        }

        public Builder setGridScanLineHeight(int gridScanLineHeight) {
            this.gridScanLineHeight = gridScanLineHeight;
            return this;
        }

        public Builder isShowLightController(boolean showLightController) {
            this.showLightController = showLightController;
            return this;
        }

        public Builder setScanHintTextColor(String scanHintTextColor) {
            this.scanHintTextColor = scanHintTextColor;
            return this;
        }

        public Builder setScanHintTextSize(int scanHintTextSize) {
            this.scanHintTextSize = scanHintTextSize;
            return this;
        }

        public Builder setScanFrameHeightOffsets(int scanFrameHeightOffsets) {
            this.scanFrameHeightOffsets = scanFrameHeightOffsets;
            return this;
        }

        public Builder setFullScreenScan(boolean fullScreenScan) {
            isFullScreenScan = fullScreenScan;
            return this;
        }

        public Builder setSupportZoom(boolean supportZoom) {
            isSupportZoom = supportZoom;
            return this;
        }
    }

}
