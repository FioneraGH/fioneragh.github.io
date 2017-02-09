---
title: Android DataBinding
date: 2016-02-05 19:38:30
tags: [Android,DataBinding]
---

### 0x81 前言
Data Binding 解决了 Android UI 编程的一个痛点，官方原生支持 MVVM 模型可以让我们在不改变既有代码框架的前提下，非常容易地使用这些新特性。

### 0x82 配置方法
如今配置方法已经很简单了，设置Project的`build.gradle`的`gradle-plugin:1.5.0`，然后在Module的`build.gradle`中添加块：
```Groovy
dataBinding {
	enabled true
}
```
说实在的这里有个坑，由于DB推出不久，网络相关的教程不多，官方的文档没有办法总是关注更新。网络上普遍的添加classpath并声明plugin的方式已经过时，在较新的工具版本下会出现Sync错误。

### 0x83 简单用法
1. layout 标签，VM容纳标签，将原本的布局与data包装在一起。
2. data 标签，VM桥，View与Model的桥梁，用于绑定数据。
3. XxxYyyBinding 类，布局xxx_yyy.xml自动生成的绑定类。

### 0x84
MVVM是一种更适合移动开发的模式，官方对于MVVM的支持是一件大好事，至于具体的用法，之后再谈～
