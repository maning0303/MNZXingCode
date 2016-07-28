# ZXingCodeDemo

#（Android）一个关于二维码扫描的Demo

##功能：
    1：生成二维码（带Logo）
    2：二维码扫描
    3：相册中选取图片
    4：开启闪光灯
    5：历史记录（需要自己实现：ActivityForResult）
    
##截图:
![image](https://github.com/maning0303/ZXingCodeDemo/blob/master/screenshots/001.png)
![image](https://github.com/maning0303/ZXingCodeDemo/blob/master/screenshots/002.png)

## 如何添加
###Gradle添加：
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
	     compile 'com.github.maning0303:ZXingCodeDemo:V1.0.1'
	}
```

### 源码添加：
#### 直接拷贝整个modlue：libraryzxing，然后关联到你的项目就可以使用

##使用方法:  
    
``` java
	<!--权限添加-->
    	<uses-permission android:name="android.permission.CAMERA" />
    	<uses-permission android:name="android.permission.FLASHLIGHT" />

        1.跳转：
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            //是不是显示历史记录按钮
            intent.putExtra(ZXingConstants.ScanIsShowHistory,true);
            startActivityForResult(intent, ZXingConstants.ScanRequestCode);
        
        2.获取结果：
            @Override
            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (data == null) {
                    return;
                }
                switch (requestCode) {
                    case ZXingConstants.ScanRequestCode:
                        if(resultCode == ZXingConstants.ScanRequestCode){
                            /**
                             * 拿到解析完成的字符串
                             */
                            String result = data.getStringExtra(ZXingConstants.ScanResult);
                            textView.setText(result);
                        }else if(resultCode == ZXingConstants.ScanHistoryResultCode){
                            /**
                             * 历史记录
                             */
                            String resultHistory = data.getStringExtra(ZXingConstants.ScanHistoryResult);
                            if(!TextUtils.isEmpty(resultHistory)){
                                //自己实现历史页面
                                startActivity(new Intent(MainActivity.this,HistoryActivity.class));
                            }
                        }
                        break;
                }
            }
            
        3：生成二维码：
        	Bitmap qrImage = ZXingUtils.createQRImage("xxxxxx");
        	Bitmap qrImage = ZXingUtils.createQRCodeWithLogo("xxxxxx", logoBitmap);
``` 
##注意：
		如果你targetSdkVersion >= 23 ，进入前先申请相机权限，不然不能扫描；


##关于代码：
    例子中的代码用的是Baozi的，稍微改了点
    感谢开源的大神们；
