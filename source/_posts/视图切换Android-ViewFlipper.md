---
title: 视图切换Android ViewFlipper
date: 2016-01-19 19:00:34
tags:
---

## [迁移]

### 0x81 什么是ViewFlipper
ViewFlipper继承自ViewAnimater，是View控件动态显示的容器类，通常用于动态的显示和切换View组件。

### 0x82 ViewFlipper添加子视图
* ViewFlipper添加子视图可以在布局中静态的添加View组件，也可以使用`<include>` 进行子布局的包含，其添加后的View都有一个从0开始的索引号Index，其索引号递加。
* ViewFlipper动态的添加View组件，其方法和一般的ViewGroup一样，通过`addView(layout，[index])` 方法。index是索引号，可用索引号默认是当前容器最大索引号+1。

### 0x83 让视图动起来
ViewFlipper默认是非自动切换，若想设置自动切换需要使用ViewFlipper的`setAutoStart(boolean)` 方法，同时可使用`setInterval(int)` 方法设置切换间隔。
`startFlipping()` 方法可开启飞翔～手动切换调用`showNext()` 和`showPrevious()` 方法进行切换，并且对于设置为INVISIABLE属性的View也能显示出来。
