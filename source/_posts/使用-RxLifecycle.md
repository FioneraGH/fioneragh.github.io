---
title: 使用 RxLifecycle
date: 2017-07-21 14:17:43
tags: [RxJava,RxLifecycle]
---

## [2017-07-21修改]

### 0x80 “解除订阅”
经过进一步的使用和Github上README的解释，Rxlifecycle实际上并没有真正的解除订阅关系，而只是终止了事件流，如若需要显式地解除订阅（比如doOnUnsubscribe/doOnDispose的操作）可能仍然需要手动解除。另一方面，如果事件流已经停止并且没有强引用的对象存在，当发生GC时变为DISPOSED状态的Disposable最终会被回收。

### 0x81 响应式编程
RxJava 为编程带来的便利性毋庸置疑，它把我这种编码猴子从复杂的多线程编程和回调地狱中解救出来。
流式的业务处理让原本复杂的多线程逻辑变得符合人类思维，事件发起者（被关注者）Observable 可以立即或随时的发送事件，订阅者接受事件进行处理。
一旦订阅者与被订阅的者建立联系Subscribtion，如果此时不想接受事件的订阅，还可以进行解除订阅。
在Android 编程当中，很多元素都有生命周期的概念，一个个钩子都与元素挂钩，而一个元素要实现某些功能通常都是在固定的生命周期当中，这便引出了生命周期与订阅的关系。
当然，我们可以手动处理这种关系，但是这种模板式的关系约束，完全可以抽象出来，自动管理Observable 的订阅与“解除订阅”，这便是RxLifecycle。

### 0x82 RxLifecycle
RxLifecycle 是一个基于RxJava 开发的生命周期管理库，它能按配置自动管理Observable的订阅和解除。
要使用它只需要在`build.gradle`中添加依赖：
```Groovy
// RxLifecycle
compile 'com.trello:rxlifecycle:1.0' [1]
compile 'com.trello:rxlifecycle-android:1.0' [2]
compile 'com.trello:rxlifecycle-components:1.0' [3]
```
其中`[1]`是基础库，`[2]`是对Android支持，`[3]`是RxActivity、RxFragment等组件。

### 0x83 RxLifecycle 使用
RxLifecycle 是一个比较简单明了的库，因为它的定位足够单纯，像张白纸一样——在特定的生命周期钩子“解除订阅”。
它的用法主要有两个——自动和手动。

1. 自动管理

    Observable 在`subcribe()`之前使用`compose(bindToLifecycle())`这样订阅的解除便会自动关联，并且解除时机与订阅时机对应。
    
    Activity:onCreate -> onDestroy, onStart -> onStop, onResume -> onPause, onPause -> onStop, onStop -> onDestroy
    
    Fragment:类似

2. 手动管理

    compose 对象是`bindUntilEvent(ActivityEvent.PAUSE)`，示例是在Activity#onPause 时“解除订阅”。

### 0x84 RxLifecycle 详解
此处我们以`RxAppCompatActivity`为讲解对象。

* 首先`RxAppCompatActivity`内创建了一个Activity内全局对象lifecycleSubject：`private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();`。
然后在每个生命周期都发送对应的事件：
```Java
@Override
@CallSuper
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    lifecycleSubject.onNext(ActivityEvent.CREATE);
}

@Override
@CallSuper
protected void onStart() {
    super.onStart();
    lifecycleSubject.onNext(ActivityEvent.START);
}

@Override
@CallSuper
protected void onResume() {
    super.onResume();
    lifecycleSubject.onNext(ActivityEvent.RESUME);
}

@Override
@CallSuper
protected void onPause() {
    lifecycleSubject.onNext(ActivityEvent.PAUSE);
    super.onPause();
}

@Override
@CallSuper
protected void onStop() {
    lifecycleSubject.onNext(ActivityEvent.STOP);
    super.onStop();
}

@Override
@CallSuper
protected void onDestroy() {
    lifecycleSubject.onNext(ActivityEvent.DESTROY);
    super.onDestroy();
}
```
* 以上便是各个生命周期发出的事件。
* 手动管理核心方法，此处返回一个Transformer用于`compose()`并生成一个新的Observable。
```Java
@Override
@NonNull
@CheckResult
public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ActivityEvent event) {
    return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
}
```
* bindUntilEvent是RxLifecycle类里的方法，这个方法返回了一个`new UntilEventObservableTransformer<>(lifecycle, event);`。
该类的call中有操作符执行时的具体实现：
```Java
public ntilEventObservableTransformer(@Nonnull Observable<R> lifecycle, @Nonnull R event) {
    this.lifecycle = lifecycle;
    this.event = event;
}

@Override
public Observable<T> call(Observable<T> source) {
    return source.takeUntil(takeUntilEvent(lifecycle, event));
}
```
* 它调用了takeUntil 操作符，这便是流结束的标志。TakeUntilGenerator 中的`takeUntilEvent()`方法计算了具体的case：
```Java
@Nonnull
static <T> Observable<T> takeUntilEvent(@Nonnull final Observable<T> lifecycle, @Nonnull final T event) {
    return lifecycle.takeFirst(new Func1<T, Boolean>() {
        @Override
        public Boolean call(T lifecycleEvent) {
            return lifecycleEvent.equals(event);
        }
    });
}
```
* 实际上取了第一个符合你设定的生命周期的事件。
* 自动管理核心方法，此处也返回一个Transformer用于`compose()`并生成一个新的Observable。
```Java
@Override
@NonNull
@CheckResult
public final <T> LifecycleTransformer<T> bindToLifecycle() {
    return RxLifecycleAndroid.bindActivity(lifecycleSubject);
}
```
* RxLifecycleAndroid 是管理自动映射关系的类，通过bindActivity 方法确定对应关系`ACTIVITY_LIFECYCLE`并调用RxLifecycle 的bind 方法进行绑定。
与手动管理类似，返回了一个`new UntilCorrespondingEventObservableTransformer<>(lifecycle.share(), correspondingEvents);`，该对象的call：
```Java
public UntilCorrespondingEventObservableTransformer(@Nonnull Observable<R> sharedLifecycle,
                                                    @Nonnull Func1<R, R> correspondingEvents) {
    this.sharedLifecycle = sharedLifecycle;
    this.correspondingEvents = correspondingEvents;
}

@Override
public Observable<T> call(Observable<T> source) {
    return source.takeUntil(takeUntilCorrespondingEvent(sharedLifecycle, correspondingEvents));
}
```
* `TakeUntilGenerator`的`takeUntilCorrespondingEvent`方法：
```Java
@Nonnull
static <T> Observable<Boolean> takeUntilCorrespondingEvent(@Nonnull final Observable<T> lifecycle,
                                                           @Nonnull final Func1<T, T> correspondingEvents) {
    return Observable.combineLatest(
        lifecycle.take(1).map(correspondingEvents), // [1]
        lifecycle.skip(1), // [2]
        new Func2<T, T, Boolean>() {
            @Override
            public Boolean call(T bindUntilEvent, T lifecycleEvent) {
                return lifecycleEvent.equals(bindUntilEvent);
            }
        }) // [3]
        .onErrorReturn(Functions.RESUME_FUNCTION)
        .takeFirst(Functions.SHOULD_COMPLETE);
}
```
* Observable.combineLatest 将两个Observable合并，三个参数分别是:
[1]根据订阅时的当前已被发送事件映射取消订阅事件的Observable，[2]跳过当前事件的下一次事件，[3]combine合并规则，根据事件是否一致判断，一致则触发takeFirst的true返回导致takeUtil触发并“解除订阅”。
事件流自动结束，ALL DONE～
