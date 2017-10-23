---
title: 'RxJavaPlugins Error: Didn''t find class ThrowableExtension'
date: 2017-08-26 14:05:37
tags: [Android,Issues]
---

### 0x81 问题来源

其实这个问题本身是不存在的,在昨天更新到Android Studio 3.0 Beta3 之后,在一些特殊情况下会抛出异常`RxJavaPlugins Error Didn't find class "com.google.devtools.build.android.desugar.runtime.ThrowableExtension"`,经过查阅资料(Google一番)后,发现这个问题曾经在Beta1出现过,不过Google之后迅速放出了Beta2更新修正了这个问题,而如今Beta3居然又出现了这个问题，看来是没有做好回归测试啊。

### 0x82 解决方法

参考[StackOverFlow](https://stackoverflow.com/questions/45604099/rxjavaplugins-error-didnt-find-class-com-google-devtools-build-android-desugar)上的解决方式，大概共有这么几种：

1. 降级gradle-plugin
    根据那个哥们的说法，这其实就是gradle-plugin的问题，故而降级可以解决这个问题，但是降级容易导致不兼容等不可预料的问题，Beta3的IDE也不允许使用Beta2的gradle-plugin，会强制要求升级。

1. 手动添加ThrowableExtension.java
    根据异常信息可以发现，google/devtools种的一个类在运行时找不到，有人提出了可以手动添加该类就可以避免这个问题，当然这不是一个好的方案，毕竟这其实是一种额外的操作。

1. minApi降级至19以下
    根据[@Xavier Rubio Jansana](https://stackoverflow.com/users/3286819/xavier-rubio-jansana)的回答，这个Issue Google在Beta1时已经意识到并标记为P0，并且在Beta2时修正了这个问题，但在Beta3 reopen了这个Issue。一个Googler给出了一个Workaround：
    > Temporary workaround is to set min sdk version below 19. Issue is that Desugar will process try-with-resources for API 19+, although platform supports it, but we will not package those classes.

    是Desugar导致了api19+的问题，因此minApi降到18可以暂时避免这个问题。还有一点，如果没有使用Java8 的特性可以考虑关闭Java8支持，它同样也对kotlin工程有效。

到目前为止，Google没有放出新的更新，只能先这样用着，相信Google很快会放出更新解决这个问题。
