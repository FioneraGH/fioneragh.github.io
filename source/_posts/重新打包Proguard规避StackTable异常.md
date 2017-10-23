---
title: 重新打包Proguard规避StackTable异常
date: 2017-08-30 16:36:13
tags: [Android,Multidex,Proguard]
---

### 0x81 Unknown verification type

`Unknown verification type [17] in stack map frame`是最近打包App时遇到的一个问题,然而很有意思的是如果不开启Multidex支持,便不存在这个问题,minApi>21也不存在这个问题.如果我们在release包的时候开启了proguard,也会出现这个问题,虽然发生这种状况的条件非常多,但其实归根结底都是proguard的问题,在minApi<21时开启Multidex也会进行Proguard操作,因为Multidex的功能实质上就是一个特殊的Proguard过程,它作为中间件处理将要生成的字节码文件.

### 0x82 问题原因

出现这种问题时完整的错误信息通常是`Can't read [\build\intermediates\transforms\jarMerging\debug\jars\1\1f\xxx.jar] (Can't process class [xxx/xxx/xxx/a$b$c.class] (Unknown verification type [xx] in stack map frame))`,其根源是某个jar在做jarMerge操作时某个类的StackMapTable attributes有问题,通常的做法是我们需要修正这个被混淆的jar包的问题,但是某些第三方库的jar包我们时不可控的,因此我们只能让Proguard自己忽略掉StackMapTable的问题.

### 0x83 Patch Proguard

[Use a custom ProGuard build with Gradle](http://innodroid.com/blog/post/use-a-custom-proguard-build-with-gradle)中提到了如何在早期Android Studio中使用一个自定义的Proguard,我们参考这种方式在AS中用自定义的Proguard进行打包.

首先,我们需要下载Proguard的源代码,Proguard源代码托管在SourceForge上,由于网络问题我使用的是Github上的[dweiss/proguard](https://github.com/dweiss/proguard),当然facebook有Proguard的一个fork仓库[facebook/proguard](https://github.com/facebook/proguard)也可以使用.

接下来我们需要修改Proguard的源代码,修改的文件是`proguard-renamer/src/proguard/classfile/ClassConstants.java`,我们将StackMapTable变量的值进行修改:

```Java
public static final String ATTR_StackMapTable = "StackMapTable";
public static final String ATTR_StackMapTable = "dummy";
```

这样做就关闭了一部分元数据的解析,可以让Proguard不要校验StackMapTable.

然后就是构建Patch过后的proguard.jar,我们使用ant这一古老的构建工具来构建,因为Proguard提供了ant的buildscript,到[Ant](http://ant.apache.org/bindownload.cgi)下载对应的ant二进制包,然后执行
`ant -f buildscripts/build.xml proguard`会在lib目录下生成proguard.jar.

### 0x84 使用Patched Proguard

其中Android Studio使用的Proguard是集成在IDE的Maven仓库中的,我们可以直接替换,但是AS往往自带了好几个版本,因此替换的方式并不推荐,这样会破坏增量升级.在sdktools中也有proguard.jar但是不知道有没有使用.

最好的办法是直接在工程中使用Gradle进行配置,通过`New Module`引入jar作为一个module,然后在Root-build.gradle中添加本地repo,通过`classpath ':proguard'`的方式就可以启用自定义的Proguard了.
