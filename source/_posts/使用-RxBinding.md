---
title: 使用 RxBinding
date: 2017-02-13 18:20:59
tags: [RxJava,RxBinding]
---

### 0x81 什么是RxBinding
RxBinding 是一个异步调用库，用RxJava实现，用于处理Android 开发中控件异步事件的处理。

### 0x82 RxBinding 原理
其实RxBinding 的代码很简单，目的也很单纯。它的内部托管了原本用于设定事件监听的方法，转为触发RxJava中的发送事件并返回一个Observable，这样便可以通过订阅的方法进行事件的消费。

以RxView的clicks方法为例：
```Java
@CheckResult @NonNull
public static Observable<Void> clicks(@NonNull View view) {
  checkNotNull(view, "view == null");
  return Observable.create(new ViewClickOnSubscribe(view));
}
```
clicks方法返回了一个Observable，而这个Observable是通过ViewClickOnSubscribe实例创建的。
```Java
final class ViewClickOnSubscribe implements Observable.OnSubscribe<Void> {
  final View view;

  ViewClickOnSubscribe(View view) {
    this.view = view;
  }

  @Override public void call(final Subscriber<? super Void> subscriber) {
    verifyMainThread();

    View.OnClickListener listener = new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (!subscriber.isUnsubscribed()) {
          subscriber.onNext(null);
        }
      }
    };

    subscriber.add(new MainThreadSubscription() {
      @Override protected void onUnsubscribe() {
        view.setOnClickListener(null);
      }
    });

    view.setOnClickListener(listener);
  }
}
```
最核心的call方法，首先`verifyMainThread();`检查接下来的操作是否是在Android主线程。
之后创建了一个View的点击监听器，监听器内的实现就是调用一下onNext()，当然先检查了是否这个subscriber已解除订阅。
然后添加一个Subscription来保证解除订阅时移除监听器避免内存泄露。
最后就是给view添加这个监听器，这样点击操作便是发送一个事件给将来的订阅者。

### 0x83 订阅Observable
既然返回值是Observable，那么我们就可以像普通的订阅操作一样处理订阅关系。
```Java
RxView.clicks(textView).throttleFirst(2000, TimeUnit.MILLISECONDS).subscribe(
    new Action1<Void>() {
    @Override
    public void call(Void aVoid) {
        EventBus.getDefault().post(new DeviceRelateEvent.RobotTracePageFullEvent());
    }
});
```
以上就是给textView这个控件添加点击事件并返回一个Observable，因此我们可以用throttleFirst操作符，限制2s内不得重复发送该事件，这样就可以防止重复点击带来的各种问题。
在call方法中处理本来监听中需要进行的逻辑操作即可。