---
title: windowIsTranlucent 踩坑之旅
date: 2017-08-09 14:53:24
tags: [Android,Theme]
---

### 0x81 windowIsTranslucent

`android:windowIsTranslucent`是Android提供的一个非常有用的属性，它可以将应用了该Style的Activity所在的Window设置成透明的。当我们有一些特殊的需求，比如我们需要一个透明的Activity，它的Theme可能是`Theme.Transparent`，这个Style将`android:windowIsTranslucent`和`android:windowIsFloating`都设置成了true，后面这个属性也有很多坑。通常状况下，我们不会使用windowIsTranslucent这个属性，因为它将Activity设置为透明，那之前打开这个Activity的Activity如果不finish就会处于可见状态，从而导致Acitivity的onStop hook不会被调用，有可能会导致逻辑上的失误。

### 0x82 windowIsTranslucent为生命周期带来的影响

其实这个属性带来的不良影响还有不少，最典型的就是Activity生命周期影响
根据windowIsTranslucent有true/false两种状态那两个Activity就有四种情况：

* A:true,B:false    A:onPause->onStop
* A:true,B:true     A:onPause
* A:false,B:false   A:onPause->onStop
* A:false,B:true    A:onPause

以上是AOSP的默认行为，意为只要目标Acitivitytranslucent的那源Acitivity就不会调用onStop，是个别的ROM自己实现了多窗口，可能第二种状况也会用onStop，所以要注意onStop中调用逻辑的可靠性。

<!--more-->

### 0x83 Activity转场动画失效

这个问题是个很棘手的问题，我分别在preL(4.4)和aboveL(8.0)上做了很多的测试，发现如果你的Application中指定的Theme是translucent的并且没有为Activity指定特定的Theme，也就是说你的所有Activity都是透明的，这样就带来一个问题，那就是通过以下方法指定的转场动画全部失效：

```XML
<item name="android:windowAnimationStyle">@style/Temp</item>

<style name="Temp" parent="@android:style/Animation.Activity">
    <item name="android:activityOpenEnterAnimation">@anim/slide_in_left</item>
    <item name="android:activityOpenExitAnimation">@anim/slide_out_left</item>
    <item name="android:activityCloseEnterAnimation">@anim/slide_in_right</item>
    <item name="android:activityCloseExitAnimation">@anim/slide_out_right</item>
</style>
```

其转场行为会变成入场动画仍旧是系统默认，而出场则无效。但是存在这样一种状况，我们为栈底Activity设置一个特定的Theme，它的windowIsTranslucent属性为false，也就是说栈底Activity是不透明的，换句话说，对于Launcher来说，新Task的栈底Activity不是translucent的，不会影响Luancher的行为。

经过上述配置，可以发现，栈底之上的Activity的进场退场动画是我们配置的，但是源Activity的对应退场进场动画是无效的，源Activity处于它原来的状态不发生变化，根据之前生命周期变化的影响，我猜测onStop和onStart/onRestart对转场是有影响的，所以第2和3的配置是无效的。

那对于这种最普通不过的转场动画又没有一个方便的解决方式呢？有，就是overridePendingTransition这个用来覆盖转场的方法，不管是打开还是关闭它都可以作出一定的影响。
> PS:网上说overridePendingTransition在有的设备少也是无效，在这里我测试了几个设备没有这种状况，从之前生命周期的表现来看，这种情况不是没有可能。

### 0x84 对同时关闭两个Activity的影响

这一系列问题的来源都出自一个情景，在4.4设备上，使用[RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult)启动Activity，然后关闭这个Activity时转场动画会立即结束，出现屏幕闪烁的状况，而在L之后的Android平台上则不存在这个问题。

RxActivityResult是一个基于RxJava实现的一个方便开发者不必重写onActivityResult便可以获取结果的库，在Observer里可以直接接受结果这种现象肯定是需要一个中间Activity代为接受目标Activity返回的数据，而这个Activity就是HolderActivity，它是translucent的。我在项目中由于使用了侧滑返回，所以支持这个特性的Acitivity都是translucent的，这样点返回就会出现两个translucent的Activity同时finish的状况，在4.x上就会出现闪屏，在L以上就不会出现这个问题。

经过大量测试，我发现5.0以上没有问题的原因是系统可以正确的处理好每一个退场动画，而4.4则会被打断，这可能和Lolipop引入的RenderThread有一定关系。目前我发现唯一一个处理这种状况的方式就是推迟要finish的Activity，尽量避免同时关闭，或者通过回调的方式在第一个关闭后再finish第二个。

### 0x85 滑动返回的黑科技

其实如前面所说，透明Activity所带来的问题很多，4.x闪动这种是不痛不痒的，如果页面存在类似SurfaceView的控件时（比如视频播放），有一段你会发现你可以很轻易地看到底层的Activity，更具体的状况参考这篇《[Android版与微信Activity侧滑后退效果完全相同的SwipeBackLayout](http://www.jianshu.com/p/b6d682e301c2#)》，这篇文章对于微信是如何实现侧滑返回的说的很详细，遇到类似问题的朋友可以读一读，主要的解决方式就是通过反射调用Android中隐藏的两个方法：convertToTranslucent和convertFromTranslucent，以此来达到动态修改Activity透明度的方式来避免不必要的问题。
