---
title: Observable的转换
date: 2017-02-27 18:25:41
tags: [RxJava,Transformer]
---

### 0x81 Observable的转换

Observable 作为RxJava中极为重要的角色，在我们使用的过程中，经常需要对其转换以适应我们的需求。
无论是变换操作，切换Scheduler还是其他对Observable的操作，都有可能出现模板代码，即操作类型都是一样的。
这个时候我们需要对这些操作进行封装，来减少模板代码带来的弊端。

### 0x82 封装统一的转换方法

最简单直接，也最容易的方法就是将这一系列操作封装成一个函数，在这个函数中进行需要的转换操作。
举个最简单的例子，Retrofit是我们常用的网络请求框架，经由RxJavaAdapter转换后，他的ApiService可以返回一个Observable，
而对于这个操作，我们通常需将线程切换到非主线程进行网络请求，请求完成后再返回主线程进行相应的UI操作，我们便可以封装起来以减少模板代码。

```Java
public static Observable<List<TestEntity>> test(Map<String, String> options) {
    Observable<BaseEntity<List<TestEntity>>> observable = TestApi.getInstance().getApiService()
            .test(options);
    return wrapObservable(observable);
}

private static <T> Observable<T> wrapObservable(Observable<BaseEntity<T>> observable) {
    return observable.map(new CommonFilter<T>()).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread());
}
```

该方法将Retrofit返回的Observable经过`wrapObservable()`方法转换成一个新的Observable，在订阅后Subscriber将接受经过转换的原始数据。

### 0x83 使用Transformer

Transformer是一个很好用的东西，上面的操作方式虽然封装了操作，但不符合RxJava链式写法处理数据流的规范，因此便有了Transformer这一大杀器。
出色的RxLifecycle库便是以这种Transformer的方式侵入原Observable，再在相应的生命周期中发送对应的事件来管理订阅的取消时机。

```Java
Observable.Transformer schedulersTransformer() {
    return new Observable.Transformer() {
        @Override
        public Object call(Object observable) {
            return ((Observable)  observable).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        }
    };
}
```

核心实现写法和封装方法差不多，但是这种方式便可以在原Observable的基础上使用`compose()`方法使其发生改变。

### 0x84 网络请求的过滤

在`0x82`中有一个`.map(new CommonFilter<T>())`的调用，使用Retrofit等网络请求框架返回的数据，往往会有公共的部分，我们可以提前做统一的判断，这样也能减少模板代码。
使用的map操作符是RxJava中很重要的一个一对一数据变换操作符，他将原来的数据映射为新的数据类型，我们可以在他的实现方法中实现一个统一的过滤器，对于符合条件的取出数据，不符合条件的抛出异常进入onError回调。

```Java
private static class CommonFilter<T>
        implements Function<BaseEntity<T>, T> {
    @Override
    public T apply(BaseEntity<T> t) throws Exception {
        if (t.getError_code() != 0) {
            throw new HttpTimeException(t.getReason());
        }
        return t.getResult();
    }
}
```

通过这种方式，我们将error_code=0的数据取出数据体返回，不满足条件则抛出异常，进行相应的处理即可。
暂时就说这么多～
