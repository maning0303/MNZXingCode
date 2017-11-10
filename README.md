# ZXingCodeDemo

#（Android）快速集成二维码扫描，使用最新版本的zxing代码提取（2017.11.10）
[![](https://jitpack.io/v/maning0303/MNZXingCode.svg)](https://jitpack.io/#maning0303/MNZXingCode)

## 功能：
    1：生成二维码（带Logo）
    2：二维码扫描
    3：相册中选取图片识别
    4：开启闪光灯
    
## 截图:
![image](https://github.com/maning0303/ZXingCodeDemo/blob/master/screenshots/mn_zxing_screenshot_001.png)
![image](https://github.com/maning0303/ZXingCodeDemo/blob/master/screenshots/mn_zxing_screenshot_002.jpg)
![image](https://github.com/maning0303/ZXingCodeDemo/blob/master/screenshots/mn_zxing_screenshot_003.jpg)
## 最新版本zxing:
![image](https://github.com/maning0303/ZXingCodeDemo/blob/master/screenshots/mn_zxing_screenshot_004.png)

## 如何添加
### Gradle添加：
#### 1.在Project的build.gradle中添加仓库地址

``` gradle
	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```

#### 2.在Module目录下的build.gradle中添加依赖
``` gradle
	dependencies {
	     compile 'com.github.maning0303:MNZXingCode:V1.0.5'
	}
```

### 源码添加：
#### 直接拷贝整个module：libraryzxing，然后关联到你的项目就可以使用

## 使用方法:  
    
``` java

        1.跳转：
            Intent intent = new Intent(this, CaptureActivity.class);
            //是否显示相册按钮
            intent.putExtra(CaptureActivity.INTENT_KEY_PHOTO_FLAG, true);
            //识别声音
            intent.putExtra(CaptureActivity.INTENT_KEY_BEEP_FLAG, true);
            //识别震动
            intent.putExtra(CaptureActivity.INTENT_KEY_VIBRATE_FLAG, true);
            //扫码框的颜色
            intent.putExtra(CaptureActivity.INTENT_KEY_SCSNCOLOR, "#FFFF00");
            //扫码框上面的提示文案
            intent.putExtra(CaptureActivity.INTENT_KEY_HINTTEXT, "请将二维码放入框中....");
            startActivityForResult(intent, 1000);
        
        2.获取结果：
            @Override
            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == 1000) {
                    if (data == null) {
                        return;
                    }
                    switch (resultCode) {
                        case CaptureActivity.RESULT_SUCCESS:
                            String resultSuccess = data.getStringExtra(CaptureActivity.INTENT_KEY_RESULT_SUCCESS);
                            showToast(resultSuccess);
                            textView.setText(resultSuccess);
                            break;
                        case CaptureActivity.RESULT_FAIL:
                            String resultError = data.getStringExtra(CaptureActivity.INTENT_KEY_RESULT_ERROR);
                            showToast(resultError);
                            break;
                        case CaptureActivity.RESULT_CANCLE:
                            showToast("取消扫码");
                            break;
                    }
                }
            }
            
        3：生成二维码：
        	Bitmap qrImage = ZXingUtils.createQRImage("xxxxxx");
        	Bitmap qrImage = ZXingUtils.createQRCodeWithLogo("xxxxxx", logoBitmap);
```


## 关于代码：
    感谢：  [zxing](https://github.com/zxing/zxing)
    感谢：  [BGAQRCode-Android](https://github.com/bingoogolapple/BGAQRCode-Android)
    感谢所有开源的人；

