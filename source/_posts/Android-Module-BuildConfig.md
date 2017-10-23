---
title: Android Module BuildConfig
date: 2017-02-10 19:49:07
tags: [Gradle]
---

### 0x81 问题起因

抽时间把一个Project 抽成了一个个的Module，意图尽可能的分层分治，但是却遇到了一个很有意思的问题，log打不出来。

### 0x82 问题分析

log打印如下，非常简单的只是判断了一下是否是调试模式，如果不是则不打印
。
```Java
public static void d(String content) {
    if (!isDebug) {
        return;
    }
    String tag = generateTag();
    Log.d(tag, content);
}
```

上面的代码看似十分正常，而实际上即使是Debug模式，仍然不打印log，原因只有一个，`isDebug`变量为`false`。
看变量定义`private static boolean isDebug = BuildConfig.DEBUG;`，BuildConfig来自`import com.fionera.base.BuildConfig;`是这个Module的。

```Java
package com.fionera.base;

public final class BuildConfig {
  public static final boolean DEBUG = Boolean.parseBoolean("true");
  public static final String APPLICATION_ID = "com.fionera.base";
  public static final String BUILD_TYPE = "debug";
  public static final String FLAVOR = "";
  public static final int VERSION_CODE = 1;
  public static final String VERSION_NAME = "1.0";
}
```

可以看到DEBUG值为true，那为什么会不打印呢，只能调试程序看这个变量到底是什么，不用猜也知道，肯定是false。

### 0x83 问题原因

Module中BuildConfig在最终打包后（即使是Debug包）都会是Release包，也就是说无论编译时BuildConfig生成值是什么，最终DEBUG值都为false。

### 0x84 解决方案

一种方法，直接指定`defaultPublishConfig "debug"`，但是这样正式打包还要改成release，这便失去了意义，毕竟我们本来就想让他自动配置。

还有第二种方法，Module中使用`publishNonDefault true`忽略打包工具默认配置，而采用主模块给他的配置。

```Groovy
debugCompile project(path:':base',configuration:'debug')
releaseCompile project(path:':base',configuration:'release')
```

这样便可以自动识别了。
