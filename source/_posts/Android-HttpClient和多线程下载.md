---
title: Android HttpClient和多线程下载
date: 2015-07-03 21:44:22
tags: [HttpClient]
---

## [迁移]

### 0x80 HttpClient

虽然HttpClient类已被抛弃，Google官方推荐使用`HttpUrlConnection`代替，但是它的用法依然对于网络操作的使用有所帮助。
HttpClient是Apache封装的一个网页客户端模拟类，其中封装了一些方法用于简化Http操作。

### 0x81 GET用法

```Java
HttpGet httpGet = new HttpGet("http://host:port/index.php?username=" + URLEncoder.encode(username,"UTF-8") + "&password=" + URLEncoder.encode(password,"UTF-8"));
```

### 0x82 POST用法

```Java
HttpPost httpPost = new HttpPost(url);
    List parameters = new ArryList();
    parameters.add(new BasicNameValuePair("username",username));
    parameters.add(new BasicNameValuePair("password",password));
    httpPost.setEntity(new URLEncoderFormEntity(parameters,"UTF-8"));
```

<!--more-->

### 0x83 HttpResponse

```Java
HttpResponse httpResponse = client.execute(httpGet/Post);
    int code = httpResponse.getStatusLine().getStatusCode();
    if(200 == code){
        InputStream is = response.getEntity().getContent();
    }
```

### 0x84 多线程下载思路

1. 本地创建一个相同大小的临时文件
1. 分配线程下载并计算各线程下载位置（`(tid - 1) * block -- tid * block - 1`）
1. 各线程分别下载各自内容

### 0x85 简单实例

```Java
    Url url = new Url("http://host:port/test");
    HttpUrlConnection conn = (HttpUrlConnection)url.openConnection(); //打开链接
    conn.setConnectionTimeout(5000); // 设置超时
    conn.setRequestMethod("GET"); // 设置访问方式为GET

    int length = conn.getContentLength(); // 获取文件长度
    RandomAccessFile raf = new RandomAccessFile("/sdcard/test","rwd");
    raf.setLength(length); // 创建同大小临时空文件
    raf.close();

    int blockSize = length / 3; // 计算块大小
    for (int threadId = 0; threadId < 3; threadId ++){
        int startIndex = (i - 1) * blockSize;
        int endIndex = (i) * blockSize - 1;
        if(2 == i){
            endIndex = length; // 若是最后一个线程，负责下载完所有剩余内容
        }
        new DownloadThread(path,threadId,startIndex,endIndex).run(); // 启动线程下载
    }

    // 下载线程类
    public class DownloadThread extends Thread{
        private int threadId;
        private int startIndex;
        private int endIndex;
        private String path;

        public void run(){
            conn.setRequestProperty("Range","bytes=" + startIndex + "-" + "endIndex");
            if (206 == conn.getResponseCode()){ // 此处部分下载返回状态码206
                InputStream is = conn.getInputStream(); // 获取下载文件流
                RandomAccessFile raf = new RandomAccessFile("/sdcard/test","rwd"); // "rwd"是数据内容变化立即写入而不等待缓存
                raf.seek(startIndex); // 将文件指针挪到指定位置

                int len = 0;
                byte[] buffer =new byte[1024];
                while((len = is.read(buffer)) != -1){
                    raf.write(buffer,0,len);
                }
            }
        }
    }
```

上方即是简单的多线程下载，可以用SharedPreference或文件保存当前下载进度以便下载异常恢复下载。
