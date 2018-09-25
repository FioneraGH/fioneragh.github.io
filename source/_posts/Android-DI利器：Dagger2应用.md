---
title: Android DI利器：Dagger2应用
date: 2017-03-08 18:53:36
tags: [Android,Dagger]
---

### 0x81 开始使用Dagger2

昨天我们说过了Dagger2的一些基本概念，比如Module、Component、Inject各自的职责，今天我们就根据之前的介绍动手试一试，看用Dagger2如何管理类对象实例。

### 0x82 全局唯一——AppModule

在我们平时进行的应用开发中，有很多工具类是以全局唯一的单例对象存在的，比如网络请求OkHttpClient和Retrofit、数据库管理的Realm等，对于这种global的对象，我们便可以将其与Application绑定，我们只在Application里将他们实例化一次，之后的每次调用都是从该处获取的。

Dagger2要实例化并缓存一个对象很容易，Dagger2有Scope的概念，在同一Scope内的对象可以认为是单例的，一经创建Dagger2便会将其缓存起来，以后有需要将缓存返回给Component。

```Java
@Module
public class AppModule {

    private final Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application provideApplication(){
        return application;
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(){
        File cacheFile = new File(BaseApplication.getInstance().getCacheDir(), "HttpCache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 10);

        OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(10,
                TimeUnit.SECONDS).addInterceptor(new LogInterceptor()).cache(cache);

        return builder.build();
    }

    @Provides
    @Singleton
    Gson provideGson(){
        GsonBuilder builder = new GsonBuilder();
        return builder.create();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient okHttpClient, Gson gson) {
        Retrofit.Builder builder = new Retrofit.Builder().client(okHttpClient).baseUrl(
                HttpConstants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        return builder.build();
    }

    @Provides
    @Singleton
    ApiService provideApiService(Retrofit retrofit){
        return retrofit.create(ApiService.class);
    }
}
```

其中`@Module`注解指明了该类是一个工厂，就是它返回实例对象给Component然在再被注入到`@Inject`成员上。`@Provides`注解修饰的方法通常用provide开头（不强制），Dagger会主动寻找对应的方法获取对象。`@Singleton`注解是Dagger提供的默认的一个Scope注解，并不是说加了这个注解它就单例了，实际上它规约了一种Scope，如前面所说的同一Scope下它是单例的，因此如果我们只在一个地方进行初始化，那它就是单例的，而这个注解名也起到一定的标记作用。从OkHttpClient->Retrofit->ApiService，当我们注入ApiService时，Dagger会自动根据依赖图为我们创建好对象并注入。

Module的最简单用法就是这样，当然它本身就是这么简单，当你理解了Scope，你就能了解到Dagger2的奇妙之处了。

<!--more-->

### 0x83 全局唯一——AppComponent

Component很简单，它是一个接口，Dagger为我们生成的辅助类DaggerXXX就是实现了这个接口。

```Java
@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    ApiService getApiService();
    Application getApplication();
}
```

它也用`@Singleton`注解标记了，与AppModule里的`@Provides`方法的Scope一样，表示它们是同一生命周期。其中`@Component`注解有一个很重要的参数，就是modules参数，它指明了工厂类，当注入需要对象时Component去哪里获取对象就是有它指明的，Component就像一个桥梁，连接了Module和Inject。

Component还有个Dependencies参数，用于组件依赖，甚至还支持SuboComponent，这个之后在说。

### 0x84 全局唯一——AppComponentHolder

这里是最关键的一步，前面说过，`@Singleton`注解只是字面意思上的单例，它提供的只是同样的Scope而以，所以如果我们真的要确保单例，那就是DaggerAppComponent只能build一次，而这次build在Application当中是最合适的，之后使用直接从Application中获取，或者作为依赖Component以单例的方式传入子Component。

```Java
@Override
public void onCreate() {
    super.onCreate();
    AppComponent appComponent = DaggerAppComponent
        .builder()
        .appModule(newAppModule(this))
        .build();
    AppComponentHolder.setAppComponen(appComponent);
}

public class AppComponentHolder {
    private static AppComponent appComponent;

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    public static void setAppComponent(AppComponent appComponent) {
        AppComponentHolder.appComponent = appComponent;
    }
}
```

很简单，我们使用一个Holder类持有唯一的AppComponent实例，之后通过AppComponentHolder来获取就可以了。

Dagger2的简单使用就是这样，如果你只想管理几个单例类，这样已经足够了，当然，它的强大不止于此，我学习它的道路还有很长。
