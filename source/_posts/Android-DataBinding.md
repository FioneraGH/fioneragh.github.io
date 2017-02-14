---
title: Android DataBinding[更新]
date: 2017-02-05 19:38:55
tags: [Android,DataBinding]
---

### 0x81 前言
DataBinding 解决了 Android UI 编程的一个痛点，官方原生支持MVVM模型可以让我们在不改变既有代码框架的前提下，非常容易地使用这些新特性。
DataBinding 如今已经官方实现双向绑定，当然双向绑定如果使用不当很容易出现死循环，在使用时还是要多加注意。

### 0x82 配置方法
配置方法已经很简单，在`build.gradle`android scope中配置如下：
```Groovy
dataBinding {
	enabled true
}
```
但是DataBinding所依赖的支持库可能和你的配置存在差异。

### 0x83 使用用法
1. 布局文件xml的变化

```xml
<layout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <data>
        <variable
            name="presenter"
            type="com.github.markzhai.sample.MainActivity.Presenter"/>
    </data>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center_horizontal"
        android:orientation="vertical"
		tools:context=".MainActivity"/>
</layout>
```
使用`<layout></layout>`标签包裹原本的布局文件，即可用于生成相应的DataBinding类。
`<data></data>`声明了当前布局绑定的model，可使用`@{exp}`或`@={exp}`的方式实现数据绑定或双向绑定。

2. 获取绑定类

一种方法，`DateBindingUtil.setContentView(this, R.layout.xxx)`这样便将布局文件与当前页面绑定起来并返回一个Binding类，这个Binding类包含了所需的各种View映射。
另一种方法，`DateBindingUtil.inflate(inflater,layout,parent,attach)`和LayoutInflater的inflater方法类似，处理布局生成View的Binding类，还可以通过`getRoot()`方法获取根视图。

### 0x84 适配器Adapter中的用法
以RecyclerView为例，用法就是上述第二种方法，将Binding传入ViewHolder，并将ViewRoot绑定到ViewHolder，之后使用Binding类进行相关操作即可。
```Java
@Override
    public void onBindViewHolder(BindingViewHolder holder, int position) {
        final Employee employee = mEmployeeList.get(position);
        holder.getBinding().setVariable(com.github.markzhai.sample.BR.item, employee);
        holder.getBinding().executePendingBindings();
}
```
使用setVariable方法可以免去类型转换，但需在布局中的data部分都有配置。