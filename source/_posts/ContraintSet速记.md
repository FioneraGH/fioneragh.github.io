---
title: ContraintSet速记
date: 2017-03-20 18:12:52
tags: [Android,ConstraintSet]
---

### 0x81 ConstraintLayout
Google在之前的GoogleIO大会上宣布了全新的Android布局组件——ConstraintLayout，中文名约束布局，其相关概念有点类似iOS的布局约束，我们终于也可以更好的在布局编辑器里用鼠标拖拽进行布局了。

### 0x82 ConstraintSet
约束布局存在于support包中，向后兼容大部分版本，对于布局的描述和更改。Google为我们提供了一套API，今天我们就来看看ConstraintSet这个类。

### 0x83 ConstraintSet使用
1. 获取布局属性
```Java
ConstraintSet applyConstraintSet = new ConstraintSet();
ConstraintLayout clContact = (ConstraintLayout) rootView.findViewById(R.id.cl_contact);
applyConstraintSet.clone(clContact);
```
这样我们便从已经处理好的布局中取出约束属性集。

2. 设置外间距
```Java
applyConstraintSet.setMargin(R.id.btn_contact1, ConstraintSet.START, 0);
applyConstraintSet.setMargin(R.id.btn_contact1, ConstraintSet.TOP, 0);
applyConstraintSet.setMargin(R.id.btn_contact1, ConstraintSet.END, 0);
applyConstraintSet.setMargin(R.id.btn_contact1, ConstraintSet.BOTTOM, 0);
```
该方法接受3个参数，一个操纵子View的id，一个边界描述，一个数值。

3. 设置宽高
```Java
applyConstraintSet.constrainWidth(R.id.btn_contact2, 500);
applyConstraintSet.constrainHeight(R.id.btn_contact1, ConstraintSet.WRAP_CONTENT);
```
该方法接受2个参数，操纵子View的id和要设置的数值。

4. 设置居中
```Java
applyConstraintSet.centerHorizontally(R.id.btn_contact1, R.id.cl_contact);
applyConstraintSet.centerVertically(R.id.btn_contact1, R.id.cl_contact);
```
上述用法设置`R.id.btn_contact1`在`R.id.cl_contact`中横竖居中。

5. 关联关系
```Java
applyConstraintSet.connect(R.id.btn_contact1, ConstraintSet.START, R.id.cl_contact, ConstraintSet.START, 0);
applyConstraintSet.connect(R.id.btn_contact1, ConstraintSet.END, R.id.cl_contact, ConstraintSet.END, 0);
```
方法接收5个参数，第一个和第三个是要关联的id，此处可以是子View和父View，第二个和第四个是关联边界，第5个是设定数值。
上述表示将`R.id.btn_contact1`的宽度占满父布局。

6. chaining
```Java
chainConstraintSet.connect(R.id.btn_contact1, ConstraintSet.START, R.id.cl_contact,
        ConstraintSet.START, 0);
chainConstraintSet.connect(R.id.btn_contact4, ConstraintSet.ER.id.cl_contact,
        ConstraintSet.END, 0);
chainConstraintSet.connect(R.id.btn_contact2, ConstraintSet.START,
        R.id.btn_contact1, ConstraintSet.END, 0);
chainConstraintSet.connect(R.id.btn_contact1, ConstraintSet.ER.id.btn_contact2,
        ConstraintSet.START, 0);
chainConstraintSet.connect(R.id.btn_contact3, ConstraintSet.START,
        R.id.btn_contact2, ConstraintSet.END, 0);
chainConstraintSet.connect(R.id.btn_contact2, ConstraintSet.ER.id.btn_contact3,
        ConstraintSet.START, 0);
chainConstraintSet.connect(R.id.btn_contact4, ConstraintSet.START,
        R.id.btn_contact3, ConstraintSet.END, 0);
chainConstraintSet.connect(R.id.btn_contact3, ConstraintSet.ER.id.btn_contact4,
        ConstraintSet.START, 

chainConstraintSet.createHorizontalChain(R.id.btn_contact_1, ConstraintSet.LEFT,
        R.id.btn_contact4, ConstraintSet.RIGHT,
        new int[]{R.id.btn_contact1, R.id.btn_contact4}, null,
        ConstraintWidget.CHAIN_SPREAD);
```
同样是关联边界，最终使用create*Chain方法创建Chain，参数分别指定链首子ViewId、链首子View位置、链尾子ViewId、链尾子View位置、Chain元素数组、配置值数组、ChainMode。

7. 清除与应用
```Java
applyConstraintSet.clear(R.id.btn_contact1);
applyConstraintSet.applyTo(clContact);
```
第一行是清除`R.id.btn_contact1`的所有约束属性，注意包括宽高也是。
第二行是将新的约束属性应用到约束布局上。

关于ContraintSet的简单使用就这么多～