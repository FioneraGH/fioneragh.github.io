---
title: Android 硬件加速：概述
date: 2017-03-28 18:15:15
tags: [Android,HardwareAccelerate]
---

### 0x80 前言

Framework源码来自Android Platform API 25

### 0x81 说说invalidate与onDraw的关系

invalidate有多个重载方法，分别接收一个参数Rect、四个int类型的ltrb和无参数方法。

```Java
public void invalidate(Rect dirty) {
    final int scrollX = mScrollX;
    final int scrollY = mScrollY;
    invalidateInternal(dirty.left - scrollX, dirty.top - scrollY,
            dirty.right - scrollX, dirty.bottom - scrollY, true, false);
}

public void invalidate(int l, int t, int r, int b) {
    final int scrollX = mScrollX;
    final int scrollY = mScrollY;
    invalidateInternal(l - scrollX, t - scrollY, r - scrollX, b - scrollY, true, false);
}
```

其中前两个方法的作用是一致的，View类将Rect参数转换成了ltrb，并最终调用了invalidateInternal方法，并且invalidateCache参数为true，fullInvalidate为false，意为刷新缓存且非全局刷新，看Rect为参数的方法也能看出来，这种重绘绘制的是被标记为"dirty"的区域。

再看invalidate方法，它调用了invalidate(boolean invalidateCache)，并入参为true：

```Java
public void invalidate() {
    invalidate(true);
}

void invalidate(boolean invalidateCache) {
    invalidateInternal(0, 0, mRight - mLeft, mBottom - mTop, invalidateCache, true);
}
```

可以发现，该方法最终也是调用了invalidateInterval，只不过重绘范围是整个View可见区域并设置了fullInvalidate为true。

而根据文档所说的，invalidate方法必须在UI线程调用，如果在非UI线程需使用postInvalidate方法，并且该方法最终会触发onDraw回调以让我们重绘自己的View。

### 0x82 invalidate可能导致的性能问题

假设我们需要实现View的拖拽，我们通常会在手指发生移动时更改绘制参数，然后通过invalidate方法（实际上是invalidateInterval内设定了相应的flag）通知Framework我的View中有"dirty"区域，需要重绘，这样当16ms检查到来时，就会调用我们的onDraw回调，我们便把View绘制出来。由于Android目前也是60fps渲染机制，因此每16ms都会开始一次新的绘制，这便要求我们的绘制工作需要在16ms内完成，负责就会出现跳帧丢帧的状况。

我们都知道CPU的资源通常非常宝贵，如果我们像以前开发那样使用CPU去绘制图形，一旦元素非常多就会占用过多的CPU资源，这时候不仅是绘制掉帧，还有可能拖慢整个机器的运行速度。

但是，GPU的定位就不太一样了，GPU内没有复杂的逻辑控制器，它的内部构造可以看作就是一个一个的图形处理单元堆叠，由于不需要逻辑判断，GPU便可以单纯的接受输入只做数据处理并输出，因此GPU还是并行计算的重要角色，他们不需要关心逻辑只需要正确的处理输入的数据。

于是在Android3.0之后，Google把使用GPU进行硬件加速的绘制方式正式提上了台面，使用硬件加速的方法其实也很简单。

<!--more-->

### 0x83 开启硬件加速

最简单的配置硬件加速的方法就是在清单文件AndroidManifest.xml中进行配置：

```XML
<application
    android:hardwareAccelerated="true">

    <activity
        android:hardwareAccelerated="false">
    </activity>
</application>
```

硬件加速属性的设置既可以在application结点上也可以在activity节点上，根据文档说明，在TargetApi 14（Android 4.0）之后的app默认是开启了硬件加速的。如果在activity节点上指定开关，它将影响所有该Activity创建的Window以及自身的Window，当然其实不也可以在WindowParams上添加硬件加速的flag，但是并不能关闭它。

在我们自定义View的时候，有时候会出现硬件加速的兼容性问题，这个时候LayerType就派上用场了，设置方式很简单：

```Java
setLayerType(LAYER_TYPE_SOFTWARE, null);
```

其中第一个参数是LAYER_TYPE，第二个参数是画笔paint，这样我们的View在onDraw中绘制时便是在设定的Layer上进行的，和Window这一Level一样有一个限制，你不能在硬件加速未开启的状态下设置HARDWARE_LAYER。

设置LayerType很简单，就不多说了～
