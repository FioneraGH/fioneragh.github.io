---
title: Android Activity
date: 2015-04-25 19:56:53
tags:
---

## [迁移]

### 0x80 什么是Activity
Activity是Android四大组件之一，是View的承载者和控制者。

### 0x81 Activity的使用

`startActivity(intent)` 通过intent启动Activity

`startActivityForResult(intent, requestCode)` requestCode为请求参数，用于onActivityResult函数执行的参数

### 0x82 OnActivityResult Tips
* 接收到返回参数的Activity会执行`onActivityResult(requestCode, responseCode, intent)`方法，根据相应的参数作出相应处理。
* 若setResult()方法未调用或无效，则该方法接受的返回值默认值为0。
