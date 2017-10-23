---
title: Gradle Plugin DexCount
date: 2016-02-02 17:32:24
tags: [Gradle,Dex]
---

### 0x81 DexCount作用

DexCount用于统计项目中所有的方法数，
[DexCount：https://github.com/KeepSafe/dexcount-gradle-plugin](https://github.com/KeepSafe/dexcount-gradle-plugin "Github地址")

### 0x82 为什么要统计`method count`

众所周知，Android在设计初期有一个设计缺陷：Google改进了JVM虚拟机，制造了一个更适合于移动设备使用的虚拟机`Dalvik VM`，Dalvik VM的可执行文件格式为.dex，该文件是将编译后的字节码通过dx工具转换生成的。
因此经过dx工具的处理，原本的method就被映射到了dex文件中并在其中维护了一张映射表，而这个映射表的长度被限制到了16位长，也就是总方法数不能超过`65535`。
如果很不幸，或者说大项目必然的结果，你的方法数超过了65535，就要对你的APK进行分包处理，以保证APK能在设备上正确的安装。
至于会触发的`dexopt exception`和`linearalloc`限制，我们之后再谈。

### 0x83 DexCount的使用

按照README的介绍，共三步

1. project buildscript

```Groovy
buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath 'com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.4.1'
    }
}
```

1. module plugin

```Groovy
apply plugin: 'com.getkeepsafe.dexcount'
```

1. gradlew assemble

```Bash
./gradlew assembleDebug
```

> 如果想查看详情，执行一次后在`${buildDir}/outputs/dexcount/${variant}`会生成一个详情文件。

### 0x84 扩展

如果要对插件进行修改，只需要修改gradle文件，添加一个配置项

```Groovy
dexcount {
    format = "list" // "list", "tree", "json", or "yaml"
    includeClasses = false
    includeFieldCount = true
    includeTotalMethodCount = false
    orderByMethodCount = false
    verbose = false
}
```
