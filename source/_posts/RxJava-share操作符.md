---
title: RxJava-share操作符
date: 2017-03-18 15:43:23
tags: [RxJava,share]
---

### 0x80 前言
本部分源码参考RxJava 2.0.7

### 0x81 share 操作符是什么
share操作符是Observable的一个方法，它实际上就是调用了publish操作符和refCount操作符。
根据官方文档的解释，它返回了一个新的ObservableSource，这个新的事件源多目广播源事件，并且，只要存在一个Observer，这个ObservableSource就会被订阅并开始发送数据，并且当所有的订阅者都解除订阅时，它也会从源ObservableSource解除订阅。

### 0x82 ConnectableObservable
我们先来了解一个概念，ConnectableObservable（在RxJava 1.x中名字为ConnectedObservable）又叫作可连接的Observable，根据官方文档解释，这种Observable即使有再多的Observer订阅，它也不会发送事件，只有在调用了connect方法之后，这个Observable才开始发送事件。基于以上这种状况，我们通常认为这种Observable相比于普通的Observable是不活跃的，称为“冷”Observable。

### 0x83 publish 操作符
和它的名字一样，把一个Observable发布，这样源Observable将被转换为ConnectableObservable并等待订阅和连接，这样你如果有多个订阅者，你可以等他们都订阅完成再调用connect方法：
```Java
@CheckReturnValue
@SchedulerSupport(SchedulerSupport.NONE)
public final ConnectableObservable<T> publish() {
    return ObservablePublish.create(this);
}
```
方法很简单，就是用自己创建了一个ObservablePublish，ObservablePublish的源码这里就不追述了。

### 0x84 refCount 操作符
该操作符也是字面意思，它维护了一个引用计数器，引用计数器的作用我们都知道，通常是用来控制一个对象在引用为0的时候销毁的，这里这个操作符也是类似的意思。我们都知道订阅关系本身是一种引用，当不再需要接受Observable的事件时我们要解除订阅以免发生内存泄露，但是如果对于这种多订阅者的状况，正确的处理这里面的关系会变得非常麻烦，这个操作符在所有订阅解除时会自动断开与ConnectableObservable的联系，并且这个操作符和publish一起可以保证我们实际上关联的是同一个ObservableSource，接受的是同样的事件，从而起到类似总线的作用。

所以RxJava为了方便这种组合式的调用，为Observable准备了一个更加贴切含义的操作符——share操作符。