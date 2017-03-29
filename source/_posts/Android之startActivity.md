---
title: Android之startActivity
date: 2017-03-21 18:19:45
tags: [Android,Context]
---

### 0x80 前言
Framework源码来自Android Platform API 25

### 0x81 startActivity的定义
是时候倒回来重新看一下startActivity这个方法了，我们从Activity进入会发现是在ContentWrapper中调用了mBase中的方法，而mBase继承自Context，贴一段在Context中的官方的源码：
```Java
/**
 * Same as {@link #startActivity(Intent, Bundle)} with no options
 * specified.
 *
 * @param intent The description of the activity to start.
 *
 * @throws ActivityNotFoundException &nbsp;
 *`
 * @see #startActivity(Intent, Bundle)
 * @see PackageManager#resolveActivity
 */
public abstract void startActivity(@RequiresPermission Intent intent);
```
很明显，这个方法是定义在`android.content.Context`类中的一个抽象方法。我们接着来看Context的直属实现类ContextWrapper的对它的实现：
```Java
public ContextWrapper(Context base) {
    mBase = base;
}
    
/**
 * Set the base context for this ContextWrapper. All calls will then be
 * delegated to the base context.  Throws
 * IllegalStateException if a base context hasalready been set.
 * 
 * @param base The new base context for thiswrapper.
 */
protected void attachBaseContext(Context base) {
    if (mBase != null) {
        throw new IllegalStateException("Basecontext already set");
    }
    mBase = base;
}

@Override
public void startActivity(Intent intent) {
    mBase.startActivity(intent);
}
```
ContextWrapper中的startActivity也只是简单的调用了mBase的实现方法，而mBase是在Constructor或attachBaseContext方法中初始化的，并且attachBase不允许重复设值mBase，这是一种代理机制。那这个Context的真正实现者是谁？没错！是ContextImpl，是不是七大姑八大姨很多，但是这种代理的设计模式才有了Android比较出色的兼容性，ContextImpl的内容不在这里展开。当然你也会发现在Activity类中有一块重写：
```Java
/**
 * Same as {@link #startActivit(Intent, Bundle)} with no options
 * specified.
 *
 * @param intent The intent tostart.
 *
 * @throwsandroid.content.ActivityNotFoundEception
 *
 * @see #startActivity(Intent, Bundle)
 * @see #startActivityForResult
 */
@Override
public void startActivity(Intent intent) {
    this.startActivity(intent, null);
}
```
这是因为除了Activity类之外的Context在调用startActivity的是非standard模式的，换句话说就是不会按常规的入栈方式，直接调用会抛异常。

### 0x82 startActivity的实现
继续追溯mBase，我们都知道，Activity这种持有文上下Context的类是不允许直接实例化的，而应该让Framework层创建并处理生命周期的问题，所以不深入Framework层我们就暂且看一下谁调用了attachBaseContext方法。

我们查一下ContextWrapper的实现，发现所有的Service实现类都实现了这个方法，关系链是`Service : ContextWrapper : Context`，还发现所有继承自Activity的类也实现了该方法，其实关系链是`Activity : ContextThemeWrapper : ContextWrapper : Context`，实际上ContextThemeWrapper也就是维护了一些主题资源和运行配置等相关的内容，所以我们先从Service下手：
```Java
// ------------------ Internal API ------------------
    
/**
 * @hide
 */
public final void attach(
        Context context,
        ActivityThread thread, String className, IBinder token,
        Application application, Object activityManager) {
    attachBaseContext(context);
    mThread = thread;           // NOTE:  unused - remove?
    mClassName = className;
    mToken = token;
    mApplication = application;
    mActivityManager = (IActivityManager)activityManager;
    mStartCompatibility = getApplicationInfo().targetSdkVersion
            < Build.VERSION_CODES.ECLAIR;
}
```
是在Service被Framework层attach的时候传递的，而这个SDK中是internal API被标记为@hide的，这就只能去看AOSP的源码，并且各大厂商之间可能会有变动。再看看Activity:
```Java
// ------------------ Internal API ------------------

final void attach(Context context, ActivityThread aThread,
        Instrumentation instr, IBinder token, int ident,
        Application application, Intent intent, ActivityInfo info,
        CharSequence title, Activity parent, String id,
        NonConfigurationInstances lastNonConfigurationInstances,
        Configuration config, String referrer, IVoiceInteractor voiceInteractor,
        Window window) {
    attachBaseContext(context);
}
```
是一样的，都是在attach的时候设值，所以也指明了那个Context为什么叫mBase，方法为什么叫做attachBaseContext，至于这个Context是谁？没错！是ContextImpl，至于它到底是构造的时候传进去的，还是attach进去的，管它呢，看起来都可以（当然AOSP里其实有具体实现，感兴趣的可以看看，毕竟我只是想记录一下两种调用方式的不同）。因此我们可以认为这种方式是一种fallback的方式，Google希望你的新页面是正常进入任务栈且接受AMS管理的，它希望你从Activity中调用startActivity。

当然对于某些特殊需求，你可能完全在当前Affinity栈为空的情况下启动一个新的Actvity，这个时候你需要为你的Intent设置一个`Context.FLAG_ACTIVITY_NEW_TASK`来表示在必要的时候开启一个新的任务栈。

### 0x83 startActivity在Activity中的实现
我们用开发工具追溯一下：

![startActivity的实现类](/images/2017_03_21_01.png)

文档解释就不贴了：
```Java
@Override
public void startActivity(Intent intent) {
    this.startActivity(intent, null);
}

@Override
public void startActivity(Intent intent, @Nullable Bundle options) {
    if (options != null) {
        startActivityForResult(intent, -1, options);
    } else {
        // Note we want to go through this call for compatibility with
        // applications that may have overridden the method.
        startActivityForResult(intent, -1);
    }
}
```
我们发现Activity实际上调用的是startActivityForResult方法，到这就比较明确了，Activity的跳转逻辑是单独的，它实际上就是传递了requestCode等于-1，这也是reqesutCode必须不小于0的原因。

后面将写写startActivityForResult这个方法要注意的问题，如果有时间可以分析一下AMS的实现，当然这里主要是记录这些细小的差异以避免在日常开发中遇到的坑。
