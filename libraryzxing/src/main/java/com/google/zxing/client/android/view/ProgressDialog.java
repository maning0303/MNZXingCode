package com.google.zxing.client.android.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;

import com.google.zxing.client.android.R;

/**
 * @author : maning
 * @date : 2020-09-04
 * @desc :
 */
public class ProgressDialog extends Dialog {

    private static ProgressDialog progressDialog;

    public ProgressDialog(Context context) {
        super(context);
    }

    public ProgressDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ProgressDialog show(Context context) {
        progressDialog = new ProgressDialog(context, R.style.MNScanProgressDialog);
        progressDialog.setContentView(R.layout.mn_scan_progress_dialog);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        Window window = progressDialog.getWindow();
        window.setDimAmount(0.0f);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.getAttributes().gravity = Gravity.CENTER;
        progressDialog.show();
        return progressDialog;
    }

    public static void dismissDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
