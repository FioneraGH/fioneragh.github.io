---
title: Android之startActivityForResult
date: 2017-03-23 18:41:03
tags: [Android,Activity]
---

### 0x80 前言
Framework源码来自Android Platform API 25

### 0x81 startActivity碎碎念
前面我记得我写了Context是如何调用startActivity的，其本质是一种代理机制，在这其中提到了非常重要的一个类——ContextImpl，实际上就是它进行了一系列判断（主要是该Intent不来自Activity的话FLAG_ACTIVITY_NEW_TASK的判断），并最终使用ActivityManagerNative通知到AMS打开了Activity。在这篇笔记里，我们就会发现，差别其实仅在过程，结果都是一样的，Activity都是由AMS管理的。

### 0x82 startActivity在Activity中的实现
```Java
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
很明显，startActivity调用了Activity的startActvityForResult这个方法，并向其中传递了值为-1的requestCode，这个我们最常用的方法其实就是一个简单的封装，视为语法糖也无可厚非，我一我们重点来看startActvityForResult这个方法。

### 0x83 startActivityForResult
方法的代码不多，直接贴了：
```Java
public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
        @Nullable Bundle options) {
    if (mParent == null) {
        options = transferSpringboardActivityOptions(options);
        Instrumentation.ActivityResult ar =
            mInstrumentation.execStartActivity(
                this, mMainThread.getApplicationThread(), mToken, this,
                intent, requestCode, options);
        if (ar != null) {
            mMainThread.sendActivityResult(
                mToken, mEmbeddedID, requestCode, ar.getResultCode(),
                ar.getResultData());
        }
        if (requestCode >= 0) {
            mStartedActivity = true;
        }

    cancelInputsAndStartExitTransition(options);
    // TODO Consider clearing/flushing other event sources and events for child windows.
    } else {
        if (options != null) {
            mParent.startActivityFromChild(this, intent, requestCode, options);
        } else {
            // Note we want to go through this method for compatibility with
            // existing applications that may have overridden it.
            mParent.startActivityFromChild(this, intent, requestCode);
        }
    }
}
```
这个方法的注释，说明了几种状况：1. 这个方法用于启动一个Activity，但是requestCode必须有意义，什么叫有意义？就是不小于0，否则这一次启动与直接调用startActivity无异（requestCode = -1），onActivityResult不会被调用，当符合条件，mStartedAcitivity会被标记为true；2. 如果Activity是singleTask的，它将不会运行在你的任务栈里并立即收到一个取消的request；3. 当你在onCreate到onResume期间调用这个方法，那你原来Activity的window将不会被显示知道收到result，这么做是为了避免界面闪烁。这几种情况都是可能会出现问题的情况，需要多加注意。

我们看代码，首先是mParent的判断，mParent指的是父Activty，什么是父Actvity？在早期Android中还没有出现Fragment概念的时候，有一个Activity容器，名字叫做ActivityGroup，现在它已经被废弃了，Google推荐使用FragmentManager代替它（也是个坑），所以这里就是判定这个Activity是不是包含在其他Activity内以免当前Activity收不到result，所以这里有两种情况：

1. `mParent == null`

    ```Java
    options = transferSpringboardActivityOptions(options);
    Instrumentation.ActivityResult ar =
        mInstrumentation.execStartActivity(
            this, mMainThread.getApplicationThread(), mToken, this,
            intent, requestCode, options);
    if (ar != null) {
        mMainThread.sendActivityResult(
            mToken, mEmbeddedID, requestCode, ar.getResultCode(),
            ar.getResultData());
    }
    cancelInputsAndStartExitTransition(options);
    ```
    我们可以看到，它先处理了options用于入场，这个不重要。重要的是mInstrumentation.execStartActivity这次调用，其实呢，ContextImpl启动Actvity后面也是通过它启动了Activity，他返回一个结果，这个结果不为null也就是目标Activity设置了setResult的时候，这个结果就会设置给我们源Activity，onActivityResult被调用，最后处理清理inputs并退场，其实顺着看进去这些过程都是由ApplicationThread为我们的App负责schedule的，太过深入的以后有机会记录一下。

    接下来看Instrumentation这个类，这个是极其重要的一个类，它是个工具类，负责把我们的需求送出去或者执行，源码不多2000多行，但是大部分API都是被标记为@hide的，也就是说日常开发不能使用。我们直接看ActivityResult后面部分的execStartActivity系列方法：
    ```Java
    try {
        intent.migrateExtraStreamToClipData();
        intent.prepareToLeaveProcess(who);
        int result = ActivityManagerNative.getDefault()
            .startActivity(whoThread, who.getBasePackageName(), intent,
                intent.resolveTypeIfNeeded(who.getContentResolver()),
                token, target != null ? target.mEmbeddedID : null,
                requestCode, 0, null, options);
        checkStartActivityResult(result, intent);
    } catch (RemoteException e) {
        throw new RuntimeException("Failure from system", e);
    }
    ```
    代码也不多，你会发现，它其实就是把通过ActivityManagerNative参数又传了出去，交给了AMS处理，而`checkStartActivityResult(result, intent)`方法就是负责抛出异常的。

2. `mParent != null`

    这种情况调用的是startActivityFromChild方法：
    ```Java
    if (options != null) {
        mParent.startActivityFromChild(this, intent, requestCode, options);
    } else {
        mParent.startActivityFromChild(this, intent, requestCode);
    }
    ```
    点进去就会发现和上面的流程似乎差不多，由于已经被抛弃了这里就不说了。

### 0x84 结语
Activity可以说是Androidxits比较核心的地方，其复杂的调用过程自然不是几篇文章就能说清楚的，当然我也不想在这篇文章深入的去挖掘，毕竟这是一篇Activity#startActivityForResult方法的笔记，后续的内容慢慢去挖掘记录，我不想自己的日常笔记冗杂而让我自己失去看的欲望，其实是为了多写几篇强迫自己尽可能的坚持去干一件事情，自己目前却是学过也用过了很多东西，但我还是希望自己能慢慢把他们记录下来，而不是蛇吞象。