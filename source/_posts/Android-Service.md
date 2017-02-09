---
title: Android Service
date: 2015-04-25 20:29:36
tags: [Android,Service]
---

## [迁移]

### 0x80 什么是Service
Service是四大组件之一，通常用于实现一定的功能而不需要前台页面。

### 0x81 Service的使用方式
1. Start & Stop
2. Bind & UnBind

### 0x82 Service的生命周期

`onCreate()`和`onDestroy`方法为服务创建与销毁必然执行的方法，若采用第一种方式，则每次调用启动服务都会执行`onStartCommand()`方法，若使用绑定的方式，则`onBind()`方法会调用并返回一个IBinder对象，该对象可用于回调Service的方法。

### 0x83 第一种方式
StartService方式是相对独立的服务启动方式，不与调用该服务的Activity相关联，即使Activity生命周期结束，除非显示的调用StopService()或服务自身结束，该服务一直处于运行状态。

### 0x84 第二种方式
BindService方式是与Activity动态绑定的，其生命周期受限于Activity自身。其中onBind方法返回IBinder对象，可在Service中创建一个内部类继承Binder，并实现一个getService()方法用于获取该Binder关联的Service，然后在ServiceConnection中的OnConnected方法中获取到Binder，进一步得到Service并调用Service的方法。
