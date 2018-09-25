---
title: 集成Weex
date: 2017-02-15 19:28:25
tags: [Weex]
---

### 0x81 Weex 是什么

[Weex官方主页](https://weex-project.io/,"Weex")
Weex 是什么，用它官方的slogan——“A framework for building Mobile cross-platform UI”，它是一个跨平台移动UI框架。
Weex 足够轻量，易扩展且高性能。

### 0x82 Weex 特点

Weex 采用Vue的语法，最新的Weex更是支持直接采用Vue文件作为源文件，这也更方便Vue.js开发者开发移动应用。
Weex 有强大的扩展系统，可以方便自定义组件与原生交互。
Weex 同时拥有比较高的性能，因为模板引擎将文件转换为js文件后在移动设备上经由JS引擎进一步转换渲染为原生组件，有媲美RN甚至优于RN的性能表现。

<!--more-->

### 0x83 Weex Android SDK 的集成

集成方式很简单，百川已经将SDK发布到jcenter，可以直接在maven依赖系统中引用，如以下gradle配置：

```Groovy
compile 'com.taobao.android:weex_sdk:0.9.5'
```

Weex SDK 目前最新版本是0.9.5，目前仍然只提供armeabi和x86两种ABI。

### 0x84 Weex 初始化

```Java
try {
    WXEnvironment.addCustomOptions("appName", getString(R.string.app_name));
    WXSDKEngine.registerComponent("custom-view-component", CustomViewComponent.class);
    WXSDKEngine.registerModule("URLHelper", URLHelperModule.class);
    WXSDKEngine.initialize(this, new InitConfig.Builder().setHttpAdapter(
            new WeexHttpAdapter()).setImgAdapter(new WeexImageLoaderAdapter()).build());
} catch (WXException e) {
    e.printStackTrace();
}
```

在这里，可以设置Weex环境变量比如应用名字，还可以注册自定义的组件和模块，通过`initialize()`方法初始化。
其中InitConfig可以自定义网络、图片等适配器，可按项目需要自行配置。

### 0x84 Weex 简单使用

Weex API 几经迭代，目前通过render方法即可以完成界面的渲染。

```Java
mInstance = new WXSDKInstance(this);
renderPage(mInstance, getPackageName(), WXFileUtils.loadAsset("app.weex.js", this), WEEX_INDEX_URL, "{\"os\":\"android\"}");

private void renderPage(WXSDKInstance mInstance, String packageName, String template,
    String source, String jsonInitData) {
    Map<String, Object> options = new HashMap<>();
    mInstance.render(packageName, template, options, jsonInitData, WXRenderStrategy.APPEND_ASYNC);
}
```

### 0x85 Weex 后续

静等1.0的发布。
