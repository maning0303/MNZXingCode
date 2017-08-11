# ZXingCodeDemo [![](https://jitpack.io/v/maning0303/MNZXingCode.svg)](https://jitpack.io/#maning0303/MNZXingCode)

## 功能：
    1：生成二维码（带Logo）
    2：二维码扫描
    3：相册中选取图片
    4：开启闪光灯
    5：历史记录（需要自己实现：ActivityForResult）
    
## 截图:
![image](https://github.com/maning0303/ZXingCodeDemo/blob/master/screenshots/mn_zxing_screenshot_001.png)
![image](https://github.com/maning0303/ZXingCodeDemo/blob/master/screenshots/mn_zxing_screenshot_002.png)

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
               if (resultCode == ZXingConstants.ScanRequltCode) {
                   /**
                    * 拿到解析完成的字符串
                    */
                   String result = data.getStringExtra(ZXingConstants.ScanResult);
                   textView.setText(result);
               } else if (resultCode == ZXingConstants.ScanHistoryResultCode) {
                   /**
                    * 历史记录
                    */
                   //自己实现历史页面
                   startActivity(new Intent(MainActivity.this, HistoryActivity.class));
               }
           }
            
        3：生成二维码：
        	Bitmap qrImage = ZXingUtils.createQRImage("xxxxxx");
        	Bitmap qrImage = ZXingUtils.createQRCodeWithLogo("xxxxxx", logoBitmap);
```


## 关于代码：
    部分代码采用：  [BGAQRCode-Android](https://github.com/bingoogolapple/BGAQRCode-Android)
    感谢所有开源的人；
    
## 推荐:
Name | Describe |
--- | --- |
[MNUpdateAPK](https://github.com/maning0303/MNUpdateAPK) | Android APK 版本更新的下载和安装,适配7.0,简单方便。 |
[MNImageBrowser](https://github.com/maning0303/MNImageBrowser) | 交互特效的图片浏览框架,微信向下滑动动态关闭 |
[MNCalendar](https://github.com/maning0303/MNCalendar) | 简单的日历控件练习，水平方向日历支持手势滑动切换，跳转月份；垂直方向日历选取区间范围。 |
[MClearEditText](https://github.com/maning0303/MClearEditText) | 带有删除功能的EditText |
[MNCrashMonitor](https://github.com/maning0303/MNCrashMonitor) | Debug监听程序崩溃日志,展示崩溃日志列表，方便自己平时调试。 |
[MNProgressHUD](https://github.com/maning0303/MNProgressHUD) | MNProgressHUD是对常用的自定义弹框封装,加载ProgressDialog,状态显示的StatusDialog和自定义Toast,支持背景颜色,圆角,边框和文字的自定义。 |
[MNXUtilsDB](https://github.com/maning0303/MNXUtilsDB) | xUtils3 数据库模块单独抽取出来，方便使用。 |
[MNVideoPlayer](https://github.com/maning0303/MNVideoPlayer) | SurfaceView + MediaPlayer 实现的视频播放器，支持横竖屏切换，手势快进快退、调节音量，亮度等。------代码简单，新手可以看一看。 |
[MNZXingCode](https://github.com/maning0303/MNZXingCode) | 快速集成二维码扫描和生成二维码 |
[MNChangeSkin](https://github.com/maning0303/MNChangeSkin) | Android夜间模式，通过Theme实现 |
[SwitcherView](https://github.com/maning0303/SwitcherView) | 垂直滚动的广告栏文字展示。 |

