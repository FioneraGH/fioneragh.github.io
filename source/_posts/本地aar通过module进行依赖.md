---
title: 本地aar通过module进行依赖
date: 2017-07-12 18:17:28
tags: [Android,Gradle]
---

### 0x81 不支持本地aar的插件
Android Studio 3.0.0 canary 之后的更新使用了新的Gradle API，对原本的DSL配置作出了诸多改变，之前的[升级到gradle-plugin 3.0.0 ](https://fioneragh.github.io/2017/05/18/%E5%8D%87%E7%BA%A7%E5%88%B0gradle-plugin-3-0-0/)也参照Google的官方文档讲述了如何迁移到Plugin 3.0.0。在Android Studio 进行一次次更新之后，到了canery5这个版本，该插件的行为再次发生了很大的变化，flavorSelection不能在外层定义以及`Local Jars In Libraries`带来的变更。这其中最坑的一个问题就是Local AAR 文件将不能被正确依赖：[While using this plugin with Android Studio, dependencies on local AAR files are not yet supported.](https://developer.android.google.cn/studio/build/gradle-plugin-3-0-0.html)。

### 0x82 曲线救国——aar module
很多人在gradle的repo下提了issue，也表示会出现类似`cannot resolve *@xxx dependencies`的错误[Gradle 4.1 M1 break Android local aar import](https://github.com/gradle/gradle/issues/2370)，有人提出了一个临时的解决办法，将aar作为module提供依赖支持，通过Android Studio 的添加新module的方法，选择到入aar文件，即可根据aar文件生成一个library，该module的build.gradle文件内容如下：
```Groovy
configurations.maybeCreate("default")
artifacts.add("default", file('ptr_lib-1.0.0.aar'))
```
![目录结构](/images/2017_07_12_01.png)

这样配置过后，移除原本的flatLibs{}配置并将implementation换成library便能成功处理依赖了。
