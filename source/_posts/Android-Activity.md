---
title: Android Activity
date: 2015-04-25 19:56:53
modify: 2017-03-21 19:56:53
tags: [Android,Activity]
---

## [迁移，2017-03-21修改]

### 0x81 什么是Activity
Activity是Android四大组件之一，是View的承载者和控制者。Android设计之初将Activity视作Controller这一层，这样使得View与Controller糅合，到后期代码变得十分难维护，所以现在都比较使用MVP、MVVM甚至两者结合的MVPVM的混合模式开发，当然具体的设计模式还是业务需求和具体情况。

### 0x82 Activity与setContentView关系
我们通常会在Actvity的onCreate回调当中调用setContentView方法设置内容，最终构成如下层次：

（图是别人的哈哈哈，如果不能用告诉我～）

![Activity层次图](/images/2017_03_21_02.jpg)

这其中PhoneWindow是系统对Window的唯一实现类，DecorView就是我们getDecorView方法获取的部分，contentLayout的FrameLayout就是`android.R.id.content`对应的空间，它们都不是我们设置的View，setContentView实际上是经历了Window->WindowManager最终把View添加到了contentLayout里，可以看一下除了使用`getWindow().setContentView(layoutResID);`添加View之外（在Framework刚构造出Activity时创建了Window这些东西），还调用了`initWindowDecorActionBar();`，这里以后有机会也记录一下～

### 0x83 Activity的调用
下面两个是Context和Activity有不同实现的方法[（这里有速记）](https://fioneragh.github.io/2017/03/21/Android之startActivity "Android之startActivity")：

`startActivity(intent)` 通过intent启动Activity

`startActivity(intent, options)` 通过intent启动Activity，options是transition配置的内容

下面两个是Activity才有的方法：

`startActivityForResult(intent, requestCode)` requestCode为请求参数，用于onActivityResult函数执行的判断值

`startActivityForResult(intent, requestCode, options)` requestCode为请求参数，用于onActivityResult函数执行的判断值，options是transition配置的内容

### 0x84 OnActivityResult Tips
* 若用户在目标页面使用回退返回，或未使用`setResult(resultCode, dataIntent)`方法指明返回结果，则原Acitivity中该方法接受的resultCode为RESULT_CANCELED（其值为0，但是最好不要写数字判断，避免编译API发生变化造成不可预料的结果）。
* 为正确处理结果，目标Activity都应该显式调用setResult方法，并在返回的Activity中进行resultCode判断，若结果单一应遵循规范，使用RESULT_OK（其值-1）作为resultCode。
* 返回结果中的data类型为Intent，要注意intent传值限制和bundle序列化处理。
* Fragment中的onActivityResult也要注意。

### 0x85 生命周期啥的就不说了