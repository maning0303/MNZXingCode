package com.google.zxing.client.android.model;

import com.google.zxing.client.android.R;

/**
 * Created by maning on 2017/12/7.
 * 启动Activity的一些配置参数
 */

public class MNScanConfig {

    //是否显示相册
    private boolean showPhotoAlbum;
    //扫描声音
    private boolean showBeep;
    //扫描震动
    private boolean showVibrate;
    //扫描框和扫描线的颜色
    private String scanColor;
    //扫描提示文案
    private String scanHintText;
    //开启Activity动画
    private int activityOpenAnime;

    private MNScanConfig() {

    }

    private MNScanConfig(Builder builder) {
        showPhotoAlbum = builder.showPhotoAlbum;
        showBeep = builder.showBeep;
        showVibrate = builder.showVibrate;
        scanColor = builder.scanColor;
        scanHintText = builder.scanHintText;
        activityOpenAnime = builder.activityOpenAnime;
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

    public String getScanHintText() {
        return scanHintText;
    }

    public int getActivityOpenAnime() {
        return activityOpenAnime;
    }

    public static class Builder {
        private boolean showPhotoAlbum = true;
        private boolean showBeep = true;
        private boolean showVibrate = true;
        private String scanColor;
        private String scanHintText;
        private int activityOpenAnime = R.anim.mn_scan_activity_bottom_in;

        public MNScanConfig builder() {
            return new MNScanConfig(this);
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

    }

}
