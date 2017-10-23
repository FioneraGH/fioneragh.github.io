---
title: 捕捉Android应用的崩溃
date: 2017-05-08 17:49:01
tags: [Android]
---

### 0x81 ForceClose

做过Android开发或Java开发的都知道，对于这样一个面向对象的语言，用null来表示一个对象为空再常见不过了，而空指针错误也变成了最让人诟病的错误。我每次在做完一个功能开始调试的时候，Android一个"ForceClose"的弹窗让我直接想骂娘，看StackTrace是NullPointerException更是让我想砸键盘。这种情况真的比较常见，缺少空判断可能会出现这种问题，而如果我们对代码中抛出的异常加以处理，那默认的异常处理器就是通知系统弹出FC框，在用户点击确定后退出进程。

### 0x82 以前的崩溃处理方法

出现"ForceClose"是非常烦人的，这种阻断式的弹窗提醒不但没什么卵用，还会增加用户的厌恶感，因此iOS系统通常是直接闪退，一些国产Android定制厂商的产品如MIUI、Bugme等也效仿去掉Android原生的错误弹窗。

当然了，事情都有两面性，iOS的调试功能我感觉非常强大，而Android可能受限于调试器的笨重（反正我是只有需要具体分析的时候才适用附加调试，通常APP崩溃都会保留栈跟踪信息），在程序崩溃时Logcat会打印StackTrace，但是Logcat为了避免log太多，重新启动进程时即使是同一个应用也会清除之前的栈追踪信息，给我带来很大的困扰，当然添加过滤器或者用一些第三方的Logcat工具可以很好的解决这个问题。

为了能更直接的控制应用崩溃，比如在崩溃时将信息写入文件然后在下次启动应用时上传服务器等等需求，我们可以自己实现异常处理器来完成这个事情：

```Java
public class CrashHandler
        implements UncaughtExceptionHandler {
    private UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return new CrashHandler();
    }

    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        ex.printStackTrace();
        return true;
    }
}
```

因为虚拟机一旦发生异常通常会有try-catch捕捉走异常，而像空指针这种异常就不一定了，因此我们实现异常处理器并设定为线程默认未捕捉异常处理器`Thread.setDefaultUncaughtExceptionHandler(this)`，之后调用系统Api关闭进程`android.os.Process.killProcess(android.os.Process.myPid())`，这样也能实现MIUI等三方ROM的效果。`System.exit(1)`意为虚拟机以non-zero退出，表示不正常退出通常会自动尝试启动应用。

### 0x83 合适的异常捕捉

前面的处理方法其实仅仅是将系统的FC弹窗去掉，用户体验上依然不好，那我们是不是在处理回调中不要关闭程序就行了？答案当然是否定的，既然程序已经抛出了异常，如果不加处理那我们的发生异常的线程就已经退出了，而对于上面的状况则有可能整个进程没有一个正常运行的线程，最终就是ANR。

最简单的验证方法，我们直接在点击事件中抛出异常，我们预想的状况是不退出进程而是打开一个新的页面：

```Java
btnCreateCrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        throw new RuntimeException("Exception by us");
                    }
                }).start();
            }
        });
```

我们启动一个新的线程只用来抛出异常，这样是没有问题的，页面也能正常跳转；但是如果去掉外面那层Thread，这样就会导致ANR，页面无法跳转。其实这很好理解，子线程死亡并不会影响页面的跳转，但是主线程抛出了异常，那整个消息队列停滞，页面停止渲染，也就是ANR了。

### 0x84 恢复主线程的消息循环

我们做开发的都知道，主线程是不同于我们直接创建的子线程的，它负责的事情非常多，其中有一个很重要的东西——Looper。我们如果要在子线程做主线程才能做的事情，比如弹出一个Toast，则必须经历三个步骤：`Looper.prepare()`、`Toast.show()`和`Looper.loop()`。消息循环是极为重要的东西，而主线程之所以不需要是因为操作系统已经为我们建立了一个MainLooper，因此按正常Looper的使用逻辑推测，一旦主线程抛出异常，那就是loop被阻断了，因此我们要做的就是再次调用`Looper.loop()`。

最终Handler代码如下：

```Java
@SuppressWarnings("InfiniteLoopStatement")
public class DefaultCrashHandler
        implements UncaughtExceptionHandler {
    private Context mContext;
    private UncaughtExceptionHandler mDefaultHandler;

    private DefaultCrashHandler() {
    }

    public static DefaultCrashHandler getInstance() {
        return new DefaultCrashHandler();
    }

    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                /*
                Here is a main loop for main thread message queue.
                If the main thread is dead, it will loop it again to maintain message queue.
                 */
                while (true) {
                    try {
                        Looper.loop();
                    } catch (Throwable e) {
                        uncaughtException(Looper.getMainLooper().getThread(), e);
                    }
                }
            }
        });
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            if (mContext != null) {
                String crashInfo = ex.getMessage();
                if (TextUtils.isEmpty(crashInfo)) {
                    crashInfo = ex.getCause().getMessage();
                }
                mContext.startActivity(new Intent(mContext, CrashFallbackActivity.class)
                        .putExtra("crash_info", crashInfo));
            }
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        ex.printStackTrace();
        return true;
    }
}
```

这样无论是子线程还是主线程发生异常我们都可以做合适的处理了，当然这种用法不知道会不会带来什么副作用，具体的效果还有待考证，需要研究一下比较成熟的崩溃统计是如何处理的。

实践代码：[CrashTracker
](https://github.com/FioneraGH/CrashTracker.git, "FioneraGH/CrashTracker")
