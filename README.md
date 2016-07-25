# ZXingCodeDemo

###（Android）
一个关于二维码扫描的Demo（Android Studio版本）
#####*功能：
    1：二维码扫描
    2：相册中选取图片
    3：开启闪光灯
    4：历史记录（只放了个按钮，功能未实现）
    
#####*截图:
![image](https://github.com/maning0303/ZXingCodeDemo/blob/master/screenshots/001.png)
![image](https://github.com/maning0303/ZXingCodeDemo/blob/master/screenshots/002.png)

#####使用方法:  
    直接拷贝整个modlue：libraryzxing，然后关联到你的项目就可以使用！
    
``` java
        1.跳转：
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            //判断是不是显示历史记录按钮
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
``` 

#####关于代码：
    例子中的代码用的是Baozi的，稍微改了点，还有扫描框用的徐医生大神的扫描框（自己太懒，感觉那个挺好看的）
    感谢开源的大神们；
