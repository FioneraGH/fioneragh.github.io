---
title: Android DI利器：Dagger2 Scope
date: 2017-05-13 10:18:56
tags: [Android,Dagger]
---

### 0x81 Singleton注解

在两个月前的一篇文章[Android DI利器：Dagger2应用](https://fioneragh.github.io/2017/03/08/Android-DI%E5%88%A9%E5%99%A8%EF%BC%9ADagger2%E5%BA%94%E7%94%A8/)中，我书写了关于Module、Component的简单用法，其中有一个注解@Singleton用在了@Provide的模块和Component当中，表示它们是单例的。我们看下Singleton注解：

```
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Singleton {
}
```

什么也没有，就是一个Scope注解，加了Singleton注解，并不是说它就单例了，实际上它规约了一种Scope，其表示的是同一Scope下它是单例的，因此如果我们只在一个地方进行初始化，那它就是单例的，而这个注解名也起到一定的标记作用。

### 0x82 Scope

Dagger使用Scope来规范对象的实例化，要做到单例的管理，有两个前提条件：

1. Scope注解必须用在Module的provide方法上，否则并不能达到局部单例的效果。

1. 如果Module的provide方法使用了Scope注解，那么Component就必须使用同一个注解，否则编译会失败。（从逻辑上也是不合理的）

Scope的原理明白了，使用起来就很容易了，它本身就是一个标记告诉Dagger如何维护实例，但是具体的情况还要看@Inject注入的状况。

<!--more-->

### 0x83 自定义Scope

我们先为GankFragment创建FragmentScope注解：

```Java
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface FragmentScope {
}
```

和Singleton一样，我们将注解加在GankModule的provide方法和Component上：

```Java
@Module(includes = {})
public class GankModule {
    private GankContract.View view;

    public GankModule(GankContract.View view) {
        this.view = view;
    }

    @Provides
    @FragmentScope
    GankContract.View provideView(){
        return view;
    }
}

@FragmentScope
@Component(dependencies = {AppComponent.class}, modules = {GankModule.class})
public interface GankComponent {
    void inject(GankFragment gankFragment);
}
```

GankModule提供了GankContract.View并且是在GankFragment生命周期中唯一的，GankComponent依赖了AppComponent因此能使用全局定义的实例如Retrofit等，然后对GankPresenterImpl进行注入：

```Java
@Inject
GankPresenterImpl(GankContract.View view) {
    this.view = view;
    this.model = new GankModelImpl();
}

@Inject
public GankPresenterImpl presenter;
```

这样我们就自定义了Scope注解并使用，其中注入的使用并不是好的实践，这里只是记录它的用法。Scope的用法就先说这么多，也达到了它作用域的作用。
