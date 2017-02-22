---
title: 发布Library到JitPack
date: 2017-02-22 17:05:20
tags: [JitPack]
---

### 0x81 关于JitPack
JitPack是一个Maven仓库，但是他的配置比写大量的gradle配置代码要省事的多。

### 0x82 发布基本配置
* 在Project的build.gradle中添加如下类路径`classpath 'com.github.dcendents:android-maven-gradle-plugin:1.5'`
* repo中添加`maven { url "https://jitpack.io" }`
* module中插件配置`apply plugin: 'com.github.dcendents.android-maven'`，并填写group名`group='com.github.fioneragh'`

### 0x83 Github发布信息
在Github上发布一个Release，填写完发布信息后即可在[JitPack]("https://jitpack.io")上找到刚刚发布的库。