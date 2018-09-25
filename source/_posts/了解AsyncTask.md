---
title: 了解AsyncTask
date: 2016-01-18 21:51:49
tags: [Android,AsyncTask]
---

### 0x81 核心：ThreadPoolExecutor

```Java
ThreadPoolExecutor(int corePoolSize, // 核心线程数
    int maximumPoolSize, // 最大线程数
    long keepAliveTime, // 线程存活时间
    TimeUnit unit, // 时间单位
    BlockingQueue<Runnable> workQueue, // 任务工作对列
    ThreadFactory threadFactory, // 线程工厂
    RejectedExecutionHandler handler); // 阻塞处理器
```

### 0x82 Api中CoreSize的异同

* Api <= 19 coreSize = 5; // 意味着维护着5个活跃线程，即使任务结束
* Api >= 20 coreSize = `CPU_COUNT` + 1 // 按CPU核心数量

<!--more-->

### 0x83 为什么一定要在UI线程创建AsyncTask？

在Api > 21 时，AsyncTask里的Handler使用的是主线程的Looper：

```Java
private static final InternalHandler sHandler = new InternalHandler();

public InternalHandler() {
    super(Looper.getMainLooper());
}
```

而在Api < 22时，Handler使用的时创建线程的Looper，因此若是AsyncTask在子线程创建，当执行更新UI的方法时会触发异常，这便是这一“误解”的根源所在。

### 0x84 AsyncTask的并行性

在Google Android Framework中，AT经历过几次演进：

* Android 1.5，AsyncTask的execute在执行时是串行的，即FIFO。
* Android 1.6 -> 2.3.2，AsyncTask的execute在执行时是并行的。
* Android 3.0 -> ?，AsyncTask的execute在执行时默认是串行的，因为线程执行器

```Java
private static volatile Executor sDefaultExecutor = SERIAL_EXECUTOR;
```

也就是说，在如今的Api > 15的情况下，默认是串行执行的。

### 0x85 手动指定并行执行（但应处理好并发资源）

可以很简单粗暴的调用executeOnExecutor方法：

```Java
.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // Api > 11
```

### 0x86 AsyncTask直接使用暴露的问题

1. 若线程数大于maximumPoolSize，直接崩溃
1. 主线程创建，避免低Api 平台崩溃
1. 各平台兼容性
