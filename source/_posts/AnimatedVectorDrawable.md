---
title: AnimatedVectorDrawable
date: 2017-02-21 12:40:32
tags: [Android,AnimatedVectorDrawable]
---

### 0x81 AnimatedVectorDrawable

承接上篇[SVG在Android上的实现——VectorDrawable](https://fioneragh.github.io/2017/02/20/SVG%E5%9C%A8Android%E4%B8%8A%E7%9A%84%E5%AE%9E%E7%8E%B0%E2%80%94%E2%80%94VectorDrawable/,"VectorDrawable")，本片主要介绍SVG的动画。
AnimatedVectorDrawable通过`ObjectAnimator`和`AnimatorSet`两组API对VectorDrawable进行动画处理，本着分治原则，一个动画通常包含动画描述文件、动画文件和VectorDrawable文件。

### 0x82 动画描述文件

动画描述文件就是将矢量图与动画关联起来：

```XML
<animated-vector xmlns:android="http://schemas.android.com/apk/res/android"
   android:drawable="@drawable/vector" >
     <target
         android:name="anim_group"
         android:animation="@anim/rotation" />
     <target
         android:name="anim_name"
         android:animation="@anim/animated_path" />
</animated-vector>
```

`<target></target>`子标签指定矢量图中哪一部分需要执行哪一个动画。

### 0x83 动画文件

动画文件就是Animator，除了一般的objectAnimator设置scale、rotation外，AnimatedVectorDrawable还支持对path作动画。

1. 传统动画
    ```XML
    <objectAnimator
         android:duration="6000"
         android:propertyName="rotation"
         android:valueFrom="0"
         android:valueTo="360" />
    ```
    这是一个普通的旋转动画，时常6000ms，其效果和对View使用属性动画类似。

1. Path动画
    要使用Path动画有一个要求，path的描述指令必须一致，如果不一致需要做一致性处理，其实也很好理解。
    之后便可以编写动画文件
    ```XML
    <set xmlns:android="http://schemas.android.com/apk/res/android">
         <objectAnimator
             android:duration="3000"
             android:repeatCount="infinite"
             android:propertyName="pathData"
             android:valueFrom="M 100 100 L 300 100 L 300 300 L 100 300z"
             android:valueTo="M 100 100 L 300 100 L 200 300 L 200 300z"
             android:valueType="pathType"/>
    </set>
    ```
    与传统的属性动画类似，只是属性名`propertyName`的值为`pathData`，值类型`valueType`为`pathType`。

### 0x84 Drawable文件

```XML
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="400dp"
    android:height="400dp"
    android:viewportHeight="400"
    android:viewportWidth="400">

    <group
        android:name="anim_group">

        <path
            android:name="anim_name"
            android:fillColor="#FF0000"
            android:pathData="M 100 100 L 300 100 L 200 300 z"
            android:strokeColor="#000000"
            android:strokeWidth="5" />
    </group>
</vector>
```

即VectorDrawable的描述文件，除了之前提到的属性，还有一个name属性用于在设定动画target时使用，上图的pathData绘制了一个三角形。

### 0x85 如何使用动画

获取这个Drawable，比如`Drawable drawable = imageView.getDrawable();`，判断这个Drawable是继承了Animatable接口的`drawable instanceof Animatable`，然后调用start开启动画即可
