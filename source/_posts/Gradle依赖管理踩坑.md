---
title: Gradle依赖管理踩坑
date: 2019-03-30 18:42:29
tags: [Android, Gradle]
---

### 0x81 诱因

春节快过去两个月了，果然如我之前所说，还是鸽了这么久，主要是年后挺忙的，公司搬家酒仙桥，自己租房折腾了一顿，工作上事情也比较多。再就是花一段时间攒了台电脑，有时间谢谢心得。

有关Gradle的笔记已经好久没写了，上一篇应该是两年前了，随着这两年Gradle 的快速迭代，其实多了很多奇技淫巧来处理一些问题，这里主要聊聊一个老问题，这个问题其实早在两年前Dependency API 变更的时候就会遇到，不是Gradle的问题，是我使用的问题。这个问题就是`api/implementation`带来的依赖可见性导致的编译不通过。

### 0x82 依赖管理的变化

从AGP3开始，Android的构建脚本开始支持最新的Gradle依赖管理API，大致分为三个部分（因为画表格麻烦我就不画了）：compile|provided|apk(runtime) 分别被api/implementation|compileOnly|runtimeOnly取代，后面的没啥好说的主要是compile，分别用api 实现原本的compile的能力，而implemetation 则控制被依赖包的CompileScope。

举个简单的例子，A < B < C。如果`B api C`，则A可以调用C的API。如果`B impl C`，则只有B能用。

那问题是什么呢？假设B有静态方法B#d，B#d中有调用C的API，A调用B#d，如果你是`B impl C`，那么很遗憾，Lint就会告诉你这个方法是有问题的，其实很好理解，因为通常非静态方法就不会有这个问题。

还有一种情况，假设`B extends C`，`B super C`没有问题，如果`A extends B` 且`B impl C`，那么很遗憾，只能`A super B` 不能 `A super C`，也很好理解，C对A是不可见的，但是这违背了Java的继承和多态，只有`B api C`是才能`A super C`，这就是控制Consumer的可见性带来的问题。

知道了原因，解决方法也很容易，一个简单粗暴api完事，但是作为规范遵从最小化原则，我们可以做Wrapper来灵活控制，毕竟面向接口编程是一个可读性非常差但十分灵活的方式:P
