---
title: SVG在Android上的实现——VectorDrawable
date: 2017-02-20 18:59:36
tags: [Android,SVG]
---

### 0x81 SVG

SVG全称Scalable Vector Graphics，意为可伸缩矢量图形，由于它的是矢量图，所以无论如何放大都不会失真。
SVG由Path描述，通过PathData控制究竟如何渲染显示图形，SVG的具体语法不是本文的重点，故不多加阐述。

### 0x82 Android 实现

有大牛在早期曾造了一个轮子——`android-pathview`，这个库内容比较简单，主要就是两个类，一个SVG路径解析类`SvgUtils`，一个根据解析结果绘制类`PathView`。

在Lolipop（Android 5.0）之后，Google官方引入了对矢量图的支持，虽然支持有限（兼容性问题），但是官方支持总是好的。
并且谷歌提供了支持库（Support Library）用于提供向后兼容。

<!--more-->

### 0x83 VectorDrawable

VectorDrawable继承自Drawable，它可以像类似ShapeDrawable一样用xml的格式表示一张图片。

```XML
<vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="24dp"
        android:height="24dp"
        android:viewportWidth="24.0"
        android:viewportHeight="24.0">
    <path
        android:fillColor="#FF000000"
        android:pathData="z"/>
</vector>
```

`<vector></vector>`标签定义了这张图片是一张矢量图，用`<path></path>`描述内容。
其中`android:width="24dp"`属性界定了SVG的固有宽度，若使用时不指定，则使用该值。
`android:viewportWidth="24.0"`制定了绘制时画布大小，path的描述是基于该值的。
`android:pathData="z"`指定绘制路径，具体语法查看w3c上SVG的语法。

### 0x84 VectorDrawable 的兼容性

VectorDrawable可以像普通的Drawable一样使用，将其设置给src或background即可，但是Android开发就不得不考虑其向后兼容性。

VectorDrawable兼容主要有两种方式——不处理和使用支持库。

1. 不做显式处理
    这种方式是因为Android Studio在执行Gradle Task时aapt会根据VectorDrawable生成对应的位图资源，用于低于API 21的平台使用，当然若是在Lolipop以上就无所谓了。
    ```Groovy
    defaultConfig{
        VectorDrawables.generatedDensities['hdpi','xxhdpi'] // 对屏幕密度为1.5和3的设备生成对应的位图资源
    }
    ```

1. 使用支持库（23.2+）
    使用支持库支持SVG的显示，类似DrawableWrapper的方式包装Drawable以用于ImageView的显示，开启方法很简单：
    ```Groovy
    android {
        defaultConfig {
            vectorDrawables.useSupportLibrary = true
        }
    }
    ```
    和DataBinding一样，将支持库的配置打开即可，之后便可以在AppCompatImageView的app:srcCompat中指定矢量图。
    当然，如果想在代码中设置，只需像下面一样即可：
    ```Java
    Resources resources = context.getResources(Resources, int, Theme);
    Theme theme = context.getTheme();
    Drawable drawable = VectorDrawableCompat.create(resources, R.drawable.vector_drawable, theme);
    view.setBackground(drawable);
    ```
    23.2之后还对AnimatedVectorDrawable提供支持。

### 0x85 AnimatedVectorDrawable

AnimatedVectorDrawable会在专篇记录用法。