package com.google.zxing.client.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.MNScanManager;
import com.google.zxing.client.android.R;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.utils.CommonUtils;
import com.google.zxing.client.android.utils.StatusBarUtil;
import com.google.zxing.qrcode.detector.FinderPattern;

import static android.graphics.drawable.GradientDrawable.RECTANGLE;

/**
 * @author : maning
 * @date : 2021/1/7
 * @desc : 扫描结果点View展示
 */
public class ScanResultPointView extends FrameLayout {

    private MNScanConfig scanConfig;
    private Result[] resultPoint;
    private Rect cameraFrame;
    private OnResultPointClickListener onResultPointClickListener;
    private ScanSurfaceView scanSurfaceView;
    private ViewfinderView viewfinderView;
    private CameraManager cameraManager;

    private int resultPointColor;
    private int resultPointStrokeColor;
    private int resultPointWithdHeight;
    private int resultPointRadiusCorners;
    private int resultPointStrokeWidth;
    private TextView tv_cancle;
    private RelativeLayout rl_result_root;
    private FrameLayout fl_result_point_root;
    private View fakeStatusBar;
    private int statusBarHeight;
    private ImageView iv_show_result;

    public void setOnResultPointClickListener(OnResultPointClickListener onResultPointClickListener) {
        this.onResultPointClickListener = onResultPointClickListener;
    }

    public interface OnResultPointClickListener {
        void onPointClick(String result);

        void onCancle();
    }

    public ScanResultPointView(Context context) {
        this(context, null);
    }

    public ScanResultPointView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanResultPointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.mn_scan_result_point_view, this);
        fakeStatusBar = view.findViewById(R.id.fakeStatusBar2);
        iv_show_result = view.findViewById(R.id.iv_show_result);
        tv_cancle = view.findViewById(R.id.tv_cancle);
        rl_result_root = view.findViewById(R.id.rl_result_root);
        fl_result_point_root = view.findViewById(R.id.fl_result_point_root);

        statusBarHeight = StatusBarUtil.getStatusBarHeight(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ViewGroup.LayoutParams fakeStatusBarLayoutParams = fakeStatusBar.getLayoutParams();
            fakeStatusBarLayoutParams.height = statusBarHeight;
            fakeStatusBar.setLayoutParams(fakeStatusBarLayoutParams);
        }

        tv_cancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //隐藏View
                if (onResultPointClickListener != null) {
                    onResultPointClickListener.onCancle();
                }
                removeAllPoints();
            }
        });
        rl_result_root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //拦截点击事件
            }
        });
    }

    public void setScanConfig(MNScanConfig config) {
        scanConfig = config;
        initResultPointConfigs();
    }

    private ResizeAbleSurfaceView resizeAbleSurfaceView;

    public void setResizeAbleSurfaceView(ResizeAbleSurfaceView resizeAbleSurfaceView) {
        this.resizeAbleSurfaceView = resizeAbleSurfaceView;
    }

    public void setScanSurfaceView(ScanSurfaceView scanSurfaceView) {
        this.scanSurfaceView = scanSurfaceView;
    }

    public void setViewfinderView(ViewfinderView viewfinderView) {
        this.viewfinderView = viewfinderView;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
        cameraFrame = cameraManager.getFramingRect();
    }


    private void initResultPointConfigs() {
        if (scanConfig == null) {
            return;
        }
        resultPointRadiusCorners = CommonUtils.dip2px(getContext(), scanConfig.getResultPointCorners());
        resultPointWithdHeight = CommonUtils.dip2px(getContext(), scanConfig.getResultPointWithdHeight());
        resultPointStrokeWidth = CommonUtils.dip2px(getContext(), scanConfig.getResultPointStrokeWidth());
        String resultPointColorStr = scanConfig.getResultPointColor();
        String resultPointStrokeColorStr = scanConfig.getResultPointStrokeColor();
        if (resultPointWithdHeight == 0) {
            resultPointWithdHeight = CommonUtils.dip2px(getContext(), 36);
        }
        if (resultPointRadiusCorners == 0) {
            resultPointRadiusCorners = CommonUtils.dip2px(getContext(), 36);
        }
        if (resultPointStrokeWidth == 0) {
            resultPointStrokeWidth = CommonUtils.dip2px(getContext(), 3);
        }
        if (!TextUtils.isEmpty(resultPointColorStr)) {
            resultPointColor = Color.parseColor(resultPointColorStr);
        } else {
            resultPointColor = getContext().getResources().getColor(R.color.mn_scan_viewfinder_laser_result_point);
        }
        if (!TextUtils.isEmpty(resultPointStrokeColorStr)) {
            resultPointStrokeColor = Color.parseColor(resultPointStrokeColorStr);
        } else {
            resultPointStrokeColor = getContext().getResources().getColor(R.color.mn_scan_viewfinder_laser_result_point_border);
        }
    }


    private Bitmap barcodeBitmap;
    private float scaleFactor;

    public void setDatas(Result[] results, Bitmap barcode, float scaleFactor) {
        this.resultPoint = results;
        this.barcodeBitmap = barcode;
        this.scaleFactor = scaleFactor;
        drawableResultPoint();
    }

    public void removeAllPoints(){
        fl_result_point_root.removeAllViews();
    }

    public void drawableResultPoint() {
        Log.e(">>>>>>", "drawableResultPoint---start");
        removeAllPoints();
        if (resultPoint == null || resultPoint.length == 0) {
            if (onResultPointClickListener != null) {
                onResultPointClickListener.onCancle();
            }
            return;
        }
        if (barcodeBitmap != null) {
            Log.e(">>>>>>", "barcodeBitmap--w:" + barcodeBitmap.getWidth() + ",h:" + barcodeBitmap.getHeight());
        }
        Log.e(">>>>>>", "statusBarHeight--->" + statusBarHeight);
        Log.e(">>>>>>", "viewfinderView.getRectFrame()--->" + viewfinderView.getRectFrame().toString());
        Log.e(">>>>>>", "previewSizeOnScreen:" + cameraManager.getConfigManager().getPreviewSizeOnScreen().toString());
        if (resizeAbleSurfaceView != null) {
            Log.e(">>>>>>", "resizeAbleSurfaceView.getWidth():" + resizeAbleSurfaceView.getWidth() + ",resizeAbleSurfaceView.getHeight():" + resizeAbleSurfaceView.getHeight());
        }
        if (scanSurfaceView != null) {
            Log.e(">>>>>>", "scanSurfaceView.getWidth():" + scanSurfaceView.getWidth() + ",scanSurfaceView.getHeight():" + scanSurfaceView.getHeight());
            int[] location = new int[2];
            scanSurfaceView.getLocationOnScreen(location);
            //更新虚假状态栏高度
            if (location[1] <= 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ViewGroup.LayoutParams fakeStatusBarLayoutParams = fakeStatusBar.getLayoutParams();
                    fakeStatusBarLayoutParams.height = statusBarHeight;
                    fakeStatusBar.setLayoutParams(fakeStatusBarLayoutParams);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ViewGroup.LayoutParams fakeStatusBarLayoutParams = fakeStatusBar.getLayoutParams();
                    fakeStatusBarLayoutParams.height = 0;
                    fakeStatusBar.setLayoutParams(fakeStatusBarLayoutParams);
                }
            }
        }
        if (scanConfig == null) {
            scanConfig = new MNScanConfig.Builder().builder();
        }
        if (resultPoint.length == 1) {
            tv_cancle.setVisibility(View.INVISIBLE);
        } else {
            tv_cancle.setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < resultPoint.length; i++) {
            final Result result = resultPoint[i];
            ResultPoint[] points = result.getResultPoints();
            if (points == null || points.length == 0) {
                return;
            }
            //测试需要：绘制point
            if (MNScanManager.isDebugMode) {
                for (int j = 0; j < points.length; j++) {
                    ResultPoint point = points[j];

                    //判断是不是全屏模式
                    int centerX = (int) point.getX();
                    int centerY = (int) point.getY();
                    Point centerPoint = getCenterPoint(centerX, centerY);

                    GradientDrawable gradientDrawable = new GradientDrawable();
                    gradientDrawable.setShape(GradientDrawable.OVAL);
                    gradientDrawable.setColor(resultPointColor);
                    ImageView view = new ImageView(getContext());
                    view.setImageDrawable(gradientDrawable);
                    RelativeLayout.LayoutParams lpRoot = new RelativeLayout.LayoutParams(CommonUtils.dip2px(getContext(), 6), CommonUtils.dip2px(getContext(), 6));
                    view.setLayoutParams(lpRoot);

                    view.setX(centerPoint.x);
                    view.setY(centerPoint.y);

                    fl_result_point_root.addView(view);
                }
            }


            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.mn_scan_result_point_item_view, null);
            RelativeLayout rl_root = inflate.findViewById(R.id.rl_root);
            ImageView iv_point_bg = inflate.findViewById(R.id.iv_point_bg);
            ImageView iv_point_arrow = inflate.findViewById(R.id.iv_point_arrow);

            //绘制中心
            if (points.length >= 2) {
                //计算右上角点
                ResultPoint pointRight = points[0];
                ResultPoint pointBottom = points[0];
                float maxX = points[0].getX();
                float maxY = points[0].getY();
                for (int j = 0; j < points.length; j++) {
                    ResultPoint point = points[j];
                    Log.e(">>>>>>", "drawableResultPoint---points :" + point.toString());
                    if (maxX < point.getX()) {
                        maxX = point.getX();
                        pointRight = point;
                    }
                    if (maxY < point.getY()) {
                        maxY = point.getY();
                        pointBottom = point;
                    }
                }
                Point realRight = getCenterPoint((int) pointRight.getX(), (int) pointRight.getY());
                Point realBottom = getCenterPoint((int) pointBottom.getX(), (int) pointBottom.getY());
                int centerX = (int) (realRight.x - (realRight.x - realBottom.x) / 2);
                int centerY = (int) (realBottom.y - (realBottom.y - realRight.y) / 2);
                //位置
                RelativeLayout.LayoutParams lpRoot = new RelativeLayout.LayoutParams(resultPointWithdHeight, resultPointWithdHeight);
                rl_root.setLayoutParams(lpRoot);

                rl_root.setX(centerX - resultPointWithdHeight / 2.0f);
                rl_root.setY(centerY - resultPointWithdHeight / 2.0f);

                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setCornerRadius(resultPointRadiusCorners);
                gradientDrawable.setShape(RECTANGLE);
                gradientDrawable.setStroke(resultPointStrokeWidth, resultPointStrokeColor);
                gradientDrawable.setColor(resultPointColor);

                iv_point_bg.setImageDrawable(gradientDrawable);

                //点的大小
                ViewGroup.LayoutParams lpPoint = iv_point_bg.getLayoutParams();
                lpPoint.width = resultPointWithdHeight;
                lpPoint.height = resultPointWithdHeight;
                iv_point_bg.setLayoutParams(lpPoint);

                //箭头大小
                if (resultPoint.length > 1) {
                    ViewGroup.LayoutParams lpArrow = iv_point_arrow.getLayoutParams();
                    lpArrow.width = resultPointWithdHeight / 2;
                    lpArrow.height = resultPointWithdHeight / 2;
                    iv_point_arrow.setLayoutParams(lpArrow);
                    iv_point_arrow.setVisibility(View.VISIBLE);
                } else {
                    //一个不需要箭头
                    iv_point_arrow.setVisibility(View.GONE);
                }

                iv_point_bg.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onResultPointClickListener != null) {
                            onResultPointClickListener.onPointClick(result.getText());
                        }
                    }
                });
                fl_result_point_root.addView(inflate);
            }
        }
        int childCount = fl_result_point_root.getChildCount();
        Log.e(">>>>>>", "fl_result_point_root---childCount：" + childCount);
        if (childCount <= 0) {
            //关闭页面
            if (onResultPointClickListener != null) {
                onResultPointClickListener.onCancle();
            }
        }
        if (MNScanManager.isDebugMode) {
            iv_show_result.setImageBitmap(barcodeBitmap);
        }
        Log.e(">>>>>>", "drawableResultPoint---end");
    }

    private Point getCenterPoint(int x, int y) {
        int centerX = x;
        int centerY = y;
        Rect rectFrame = viewfinderView.getRectFrame();
        Point previewSizeOnScreen = cameraManager.getConfigManager().getPreviewSizeOnScreen();
        //计算对应点在当前View上的宽高
        float newHeight = (float) y / (float) previewSizeOnScreen.y * scanSurfaceView.getHeight();
        centerY = (int) newHeight;
        float newWidth = (float) x / (float) previewSizeOnScreen.x * scanSurfaceView.getWidth();
        centerX = (int) newWidth;
        //判断是不是全屏模式
        if (!scanConfig.isFullScreenScan()) {
            if (viewfinderView != null) {
                centerX += rectFrame.left;
                centerY += rectFrame.top;
            }
        }
        return new Point(centerX, centerY);
    }

}