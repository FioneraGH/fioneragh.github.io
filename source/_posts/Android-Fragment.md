---
title: Android Fragment
date: 2015-04-13 20:50:02
tags: [Android,Fragment]
---

## [迁移]

### 0x80 什么是Fragment

Fragment是一种UI组件，可以视为Activity布局组成元素。

### 0x81 Fragment的使用

Fragment通常有两种使用方式：

1. 在XML布局文件中使用声明，android:name="package.class"是必须的，他确定了布局所使用的Fragment类，这种方法属于静态方法。
1. 在java文件中动态的添加、删除和替换Fragment，使用FragmentTransaction类提供的API进行操作，其操作可以是连续的。

### 0x81 FragmentManager

Fragment的管理类

`getFragmentManager()` 取得Manager

`findFragmentById()` 获取一个id值的Fragment实例

`beginTransaction()` 返回一个FragmentTransaction实例

### 0x82 FragmentTransaction

`add(id,fragment)` 添加一个Fragment到指定id容器

`remove(fragment)` 删除一个Fragment

`replace(id,fragment)` 替换（覆盖）id容器上的Fragment

`addToBackStack(null)` 将当前Fragment状态压入回退栈

`beginTransaction().replace(id,fragment).addToBackStack(null).commit()` 支持链式编程

### 0x83 FragmentPagerAdapter

Fragment可用于ViewPager的显示项，只需要实现FragmentPagerAdapter的相应方法即可。
