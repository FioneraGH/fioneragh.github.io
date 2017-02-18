---
title: Android Path
date: 2017-02-17 17:01:00
tags: [Android,Path]
---

### 0x81 Path 是什么
Path 顾名思义是路径的意思，Path API的功能很单纯，就是使用画笔Paint按照路径Path在画布Canvas上绘制相应的图形。
通过使用Path，可以方便的绘制所需的内容，从而免去画笔繁琐的绘制方法。

### 0x82 相关 API
```Java
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
```
android.graphics包中包含了View绘制所需的类。

### 0x83 Path 的简单使用
1. 初始化Paint
```Java
paint = new Paint();
paint.setAntiAlias(true);
paint.setStrokeWidth(3);
paint.setColor(Color.BLACK);
paint.setStyle(Paint.Style.STROKE);
```
新建Paint对象并设置抗锯齿，描边宽，颜色及描边方式。

2. 初始化Path
```Java
searchPath = new Path();
float smallRadius = bigCircleWidth / 8;
RectF searchRect = new RectF(-smallRadius, -smallRadius, smallRadius, smallRadius);
searchPath.addArc(searchRect, 45, 358);
```
新建一个Path对象，设定绘制的矩形大小（Android所有可视元素都是矩形模型），之后通过Path的`addArc()`方法添加一段圆弧。
Path的路径绘制API有很多，比如最简单的`lineTo()`类似于`Canvas#drawLine()`。

3. PathMeasure 的使用
```Java
pathMeasure.setPath(searchPath, false);
pathMeasure.getSegment(pathMeasure.getLength() * animPercent, pathMeasure.getLength(), dst,
        true);
canvas.drawPath(dst, paint);
```
PathMeasure的作用就是计算Path的相关参数，如长度，tan值等，还可以用于取出某一段到一个新的Path对象上，如上便是在searchPath取一段置入dst中，再用`Canvas#drawPath()`方法绘制出来。
PathMeasure要使用必须要传入Path作为参数，并且如果Path发生改变，必须重新`setPath()`以保证计算准确有效。

Path的简单用法就是这样，可以搭配ValueAnimator绘制一些简单的动画。当然，animated-svg更强大。