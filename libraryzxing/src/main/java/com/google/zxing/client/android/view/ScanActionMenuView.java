package com.google.zxing.client.android.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.client.android.R;
import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.other.MNCustomViewBindCallback;
import com.google.zxing.client.android.utils.StatusBarUtil;

/**
 * @author : maning
 * @date : 2020-09-04
 * @desc :
 */
public class ScanActionMenuView extends FrameLayout {

    private LinearLayout btn_scan_light;
    private ImageView iv_scan_light;
    private TextView tv_scan_light;
    private LinearLayout btn_close;
    private LinearLayout btn_photo;

    private RelativeLayout rl_default_menu;
    private LinearLayout ll_custom_view;
    private View fakeStatusBar;

    private OnScanActionMenuListener onScanActionMenuListener;

    public interface OnScanActionMenuListener {
        void onClose();

        void onLight();

        void onPhoto();
    }

    public void setOnScanActionMenuListener(OnScanActionMenuListener onScanActionMenuListener) {
        this.onScanActionMenuListener = onScanActionMenuListener;
    }


    public ScanActionMenuView(Context context) {
        this(context, null);
    }

    public ScanActionMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanActionMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        //绑定
        View view = LayoutInflater.from(getContext()).inflate(R.layout.mn_scan_action_menu, this);

        btn_scan_light = (LinearLayout) findViewById(R.id.btn_scan_light);
        iv_scan_light = (ImageView) findViewById(R.id.iv_scan_light);
        tv_scan_light = (TextView) findViewById(R.id.tv_scan_light);
        btn_close = (LinearLayout) findViewById(R.id.btn_close);
        btn_photo = (LinearLayout) findViewById(R.id.btn_photo);
        rl_default_menu = (RelativeLayout) findViewById(R.id.rl_default_menu);
        ll_custom_view = (LinearLayout) findViewById(R.id.ll_custom_view);
        fakeStatusBar = (View) findViewById(R.id.fakeStatusBar);

        rl_default_menu.setVisibility(View.GONE);
        ll_custom_view.setVisibility(View.GONE);

        //点击事件
        btn_scan_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onScanActionMenuListener != null) {
                    onScanActionMenuListener.onLight();
                }
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onScanActionMenuListener != null) {
                    onScanActionMenuListener.onClose();
                }
            }
        });
        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onScanActionMenuListener != null) {
                    onScanActionMenuListener.onPhoto();
                }
            }
        });

    }

    private MNScanConfig scanConfig;

    public void setScanConfig(MNScanConfig config, MNCustomViewBindCallback customViewBindCallback) {
        this.scanConfig = config;

        //自定义View
        int customShadeViewLayoutID = scanConfig.getCustomShadeViewLayoutID();
        if (customShadeViewLayoutID > 0 && customViewBindCallback != null) {
            //显示出来
            ll_custom_view.setVisibility(View.VISIBLE);
            View customView = LayoutInflater.from(getContext()).inflate(customShadeViewLayoutID, null);
            ll_custom_view.addView(customView);
            //事件绑定
            customViewBindCallback.onBindView(customView);
        } else {
            rl_default_menu.setVisibility(View.VISIBLE);
        }

        //闪光灯配置
        boolean showLightController = scanConfig.isShowLightController();
        if (showLightController) {
            btn_scan_light.setVisibility(View.VISIBLE);
        } else {
            btn_scan_light.setVisibility(View.GONE);
        }

        //其他配置
        if (!scanConfig.isShowPhotoAlbum()) {
            btn_photo.setVisibility(View.GONE);
        }
    }


    public void openLight() {
        iv_scan_light.setImageResource(R.drawable.mn_icon_scan_flash_light_on);
        tv_scan_light.setText("关闭手电筒");
    }

    public void closeLight() {
        iv_scan_light.setImageResource(R.drawable.mn_icon_scan_flash_light_off);
        tv_scan_light.setText("打开手电筒");
    }


}
