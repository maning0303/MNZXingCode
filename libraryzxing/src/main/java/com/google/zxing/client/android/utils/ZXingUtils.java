package com.google.zxing.client.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by maning on 2017/11/10.
 */

public class ZXingUtils {

    public static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);

    static {
        List<BarcodeFormat> allFormats = new ArrayList<>();
        allFormats.add(BarcodeFormat.AZTEC);
        allFormats.add(BarcodeFormat.CODABAR);
        allFormats.add(BarcodeFormat.CODE_39);
        allFormats.add(BarcodeFormat.CODE_93);
        allFormats.add(BarcodeFormat.CODE_128);
        allFormats.add(BarcodeFormat.DATA_MATRIX);
        allFormats.add(BarcodeFormat.EAN_8);
        allFormats.add(BarcodeFormat.EAN_13);
        allFormats.add(BarcodeFormat.ITF);
        allFormats.add(BarcodeFormat.MAXICODE);
        allFormats.add(BarcodeFormat.PDF_417);
        allFormats.add(BarcodeFormat.QR_CODE);
        allFormats.add(BarcodeFormat.RSS_14);
        allFormats.add(BarcodeFormat.RSS_EXPANDED);
        allFormats.add(BarcodeFormat.UPC_A);
        allFormats.add(BarcodeFormat.UPC_E);
        allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION);

        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, allFormats);
        HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    public static Bitmap createQRCodeImage(String content) {
        return createQRCodeImage(content, 500, 0, Color.BLACK, Color.WHITE, null, null, null);
    }

    public static Bitmap createQRCodeImage(String content, int size) {
        return createQRCodeImage(content, size, 0, Color.BLACK, Color.WHITE, null, null, null);
    }

    public static Bitmap createQRCodeImage(String content, int size, int margin) {
        return createQRCodeImage(content, size, margin, Color.BLACK, Color.WHITE, null, null, null);
    }

    public static Bitmap createQRCodeImage(String text, Bitmap logo_bitmap) {
        return createQRCodeImage(text, 500, 0, Color.BLACK, Color.WHITE, null, logo_bitmap, null);
    }

    public static Bitmap createQRCodeImage(String text, int size, Bitmap logo_bitmap) {
        return createQRCodeImage(text, size, 0, Color.BLACK, Color.WHITE, null, logo_bitmap, null);
    }


    public static Bitmap createQRCodeImage(String text, int size, int margin, Bitmap logo_bitmap) {
        return createQRCodeImage(text, size, margin, Color.BLACK, Color.WHITE, null, logo_bitmap, null);
    }

    /**
     * 生成带logo的二维码，logo默认为二维码的1/5
     *
     * @param text                   需要生成二维码的内容
     * @param size                   需要生成二维码的大小
     * @param margin                 二维码边距
     * @param foreground_color       二维码前景色
     * @param background_color       二维码背景颜色
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param logo_bitmap            logo文件
     * @return bitmap
     */
    public static Bitmap createQRCodeImage(String text, int size, int margin, int foreground_color, int background_color, String error_correction_level, Bitmap logo_bitmap) {
        return createQRCodeImage(text, size, margin, foreground_color, background_color, error_correction_level, logo_bitmap, null);
    }

    /**
     * 生成带logo的二维码，logo默认为二维码的1/5
     *
     * @param text                   需要生成二维码的内容
     * @param size                   需要生成二维码的大小
     * @param margin                 二维码边距
     * @param foreground_color       二维码前景色
     * @param background_color       二维码背景颜色
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param logo_bitmap            logo文件
     * @param logo_bitmap            二维码前景图片，不建议使用（可能太花识别率会降低）
     * @return bitmap
     */
    public static Bitmap createQRCodeImage(String text, int size, int margin, int foreground_color, int background_color, String error_correction_level, Bitmap logo_bitmap, Bitmap foreground_bitmap) {
        try {
            int IMAGE_HALFWIDTH = size / 10;
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, Math.max(margin, 0));
            /*
             * 设置容错级别，默认为ErrorCorrectionLevel.L
             * 因为中间加入logo所以建议你把容错级别调至H,否则可能会出现识别不了
             */
            if (TextUtils.isEmpty(error_correction_level)) {
                if (logo_bitmap != null) {
                    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                } else {
                    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                }
            } else {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }


            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, size, size, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int halfW = width / 2;
            int halfH = height / 2;

            if (logo_bitmap != null) {
                Matrix m = new Matrix();
                float sx = (float) 2 * IMAGE_HALFWIDTH / logo_bitmap.getWidth();
                float sy = (float) 2 * IMAGE_HALFWIDTH / logo_bitmap.getHeight();
                m.setScale(sx, sy);
                //设置缩放信息
                //将logo图片按martix设置的信息缩放
                logo_bitmap = Bitmap.createBitmap(logo_bitmap, 0, 0,
                        logo_bitmap.getWidth(), logo_bitmap.getHeight(), m, false);
            }
            if (foreground_bitmap != null) {
                //从当前位图按一定的比例创建一个新的位图
                foreground_bitmap = Bitmap.createScaledBitmap(foreground_bitmap, width, height, false);
            }

            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (logo_bitmap != null) {
                        if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
                                && y > halfH - IMAGE_HALFWIDTH
                                && y < halfH + IMAGE_HALFWIDTH) {
                            //该位置用于存放图片信息
                            //记录图片每个像素信息
                            pixels[y * width + x] = logo_bitmap.getPixel(x - halfW
                                    + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                        } else {
                            if (bitMatrix.get(x, y)) {
                                if (foreground_bitmap != null) {
                                    pixels[y * width + x] = foreground_bitmap.getPixel(x, y);
                                } else {
                                    pixels[y * width + x] = foreground_color;
                                }
                            } else {
                                pixels[y * size + x] = background_color;
                            }
                        }
                    } else {
                        if (bitMatrix.get(x, y)) {
                            if (foreground_bitmap != null) {
                                pixels[y * width + x] = foreground_bitmap.getPixel(x, y);
                            } else {
                                pixels[y * width + x] = foreground_color;
                            }
                        } else {
                            pixels[y * size + x] = background_color;
                        }
                    }

                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }


    //------解析图片-----

    /**
     * 同步解析本地图片二维码。该方法是耗时操作，请在子线程中调用。
     *
     * @param picturePath 要解析的二维码图片本地路径
     * @return 返回二维码图片里的内容 或 null
     */
    public static String syncDecodeQRCode(String picturePath) {
        return syncDecodeQRCode(getDecodeAbleBitmap(picturePath));
    }

    /**
     * 同步解析bitmap二维码。该方法是耗时操作，请在子线程中调用。
     *
     * @param bitmap 要解析的二维码图片
     * @return 返回二维码图片里的内容 或 null
     */
    public static String syncDecodeQRCode(Bitmap bitmap) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            com.google.zxing.RGBLuminanceSource source = new com.google.zxing.RGBLuminanceSource(width, height, pixels);
            Result result = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(source)), HINTS);
            if (result != null) {
                return recode(result.getText());
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将本地图片文件转换成可解码二维码的 Bitmap
     *
     * @param picturePath 本地图片文件路径
     * @return
     */
    private static Bitmap getDecodeAbleBitmap(String picturePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, options);
            int sampleSize = options.outHeight / 400;
            if (sampleSize <= 0) {
                sampleSize = 1;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(picturePath, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 中文乱码
     *
     * @return
     */
    public static String recode(String str) {
        String formart = "";
        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder()
                    .canEncode(str);
            if (ISO) {
                formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
            } else {
                formart = str;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return formart;
    }

}
