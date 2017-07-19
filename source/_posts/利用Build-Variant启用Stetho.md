---
title: 利用Build Variant启用Stetho
date: 2017-07-19 18:52:04
tags: [Android,Stetho]
---

### 0x81 Stetho
之前项目中使用了OkHttp作为网络请求工具，为了方便跟踪调试，我在项目中使用了Stetho这个调试工具来抓包。Stetho很强大，由Facebook出品，它构建了一个Debug Bridge让Chrome Developer Tools附到了Android应用上，这样便可以使用CDT对App的网络请求进行抓包并跟踪。著名的移动数据库Realm也提供了Stetho的插件方便开发者观察Realm数据库数据。

Stetho的集成方式很简单：
```Groovy
dependencies { 
    implemetation 'com.facebook.stetho:stetho:1.5.0' 
    implemetation 'com.facebook.stetho:stetho-okhttp3:1.5.0' 
} 
```
之后在Application中初始化：
```Java
Stetho.initializeWithDefaults(this);
// OkHttp3 Helper
new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).build();
```

### 0x82 只在Debug模式下启用Stetho
我们都知道，开发时的辅助工具我们在发布版本时都是要去除的，冗余代码不说，所有切入式的调试工具都会影响App的性能。因此我们要保证只在Debug模式下编译代码到App并启用，而发布模式需要排除代码并去除初始化的Api调用。

最直接的办法，就是在发包时手动去除代码，移除依赖。当然Build Variant的支持下，我们可以使用Gradle强大的依赖管理来完成这件事情：
```Groovy
dependencies { 
    debugImplemetation 'com.facebook.stetho:stetho:1.5.0' 
    debugImplemetation 'com.facebook.stetho:stetho-okhttp3:1.5.0' 
} 
```
通过这种方式我们就能保证只有在debug下才会依赖Stetho，然后利用Build Variant分离代码的特点，在Debug的文件下添加初始化Api的调用，而Release不写就可以达到目的，我们便不需要再手动去管理打包的依赖问题。

![分离式的工程结构](/images/2017_07_19_01.png)
