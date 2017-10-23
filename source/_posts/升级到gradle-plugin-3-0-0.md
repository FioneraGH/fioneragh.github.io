---
title: 升级到gradle-plugin 3.0.0
date: 2017-05-18 18:24:30
tags: [Android,Gradle]
---

### 0x81 Google I/O 2017

北京时间5月18日凌晨1点，Google开发者大会是周年纪念版开始，第一天就放出了很多激动人心的东西。作为一名实打实的Android开发新手，自然注意到新发布的Android Studio 3.0，3.0对我已经用了一年多的JVM语言Kotlin提供了官方支持，并将Kotlin作为Android开发的一级语言。

### 0x82 Android Studio 3.0 的变化

3.0其实完全可以看作是2.5 preview3改名字而来，整合了gradle-plugin 2.5的特性，使用了最新的构建工具Gradle 4.0，因为存在broken api所以干脆将Major版本提到了3，gradle-plugin 3.0.0 alpha1的出现需要我们做一些工程级别的更改，同时跟上的Gradle 4.0也废弃了很多API（其实很多API在3.4就已经标记为废弃了，并提示在4.0很可能会移除）。除此之外，就是为IDEA Android Support Plugin添加了Kotlin支持，例如在我们创建Activity时可以选择Source Type为Kotlin，Kotlin的主要功能仍要依靠JetBrains的Kotlin插件。还有一个很重要的变化，启动画面变漂亮了。

### 0x83 Migrate to the New Plugin

标题就是Google官方文档的标题，随着gradle-plugin 3.0.0 alpha1的推出，文档也随之更新。本篇主要根据官方文档调整各build.gradle文件，以确保插件能正确的运行。

1. 基础集成
    打开先前的工程，AS3.0通常会提示你升级到最新的插件版本，也可以自己手动更改：
    ```Groovy
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.0-alpha1'
    }
    ```
    使用它至少需要Gradle 4.0 milestone1的支持，我使用了官网最新的milestone2版本。

1. 使用Flavor Dimensions 
    Dimension的概念好久之前就有了，可以认为它和flavor是相辅相成的，可以使构建更加灵活，但是这次更新的插件要求必须使用Flavor Dimensions，从而方便gradle正确的构建代码。几个月前曾经记录过一个坑，module中的BuildConfig的DEBUG值永远为false，[Android Module BuildConfig](https://fioneragh.github.io/2017/02/10/Android-Module-BuildConfig/)中说的很明白，Android Studio在打包的时候，module使用的永远是release包，所以导致了这个问题。当时的解决办法是这样：

    ```Groovy
    debugCompile project(path:':base',configuration:'debug')
    releaseCompile project(path:':base',configuration:'release')
    ```
    然后再gradle文件中配置`publishNonDefault true`，现在Google强制使用Flavor Dimensions后，module也能根据需要选择variant。

    根据官方文档的说法，plugin 3.0采用新的机制来自动匹配构建变量，app的debug会匹配module的debug，并且使用flavor的xxDebug和匹配module的xxDebug，这要求所有的Flavor都必须指定一个已存在的dimension，即使flavor只有一个，否则会遇到如下错误：

    ![缺少dimension](/images/2017_05_18_01.png)

    因此我们需要声明Flavor Dimensions并分配：
    ```Groovy
    flavorDimensions "eson","test"

    productFlavors {
        fuck {
            dimension "eson"
            return 0
        }

        hell {
            dimension "eson"
            return 0
        }

        cake {
            dimension "eson"
            return 0
        }
    }
    ```
    为了方便说明问题，我们用2个dimension加3个flavor的方式演示。首先我们将三个flavor都设置为"eson"，会报一个错误：

    ![test未分配](/images/2017_05_18_02.png)

    这种状况也就是说flavor不能不关联dimension，dimension也不能存在空闲，否则这个dimension就是多余的，我们将cake的dimension改成test，最终编译通过后我们看一下构建变量：

    ![四种构建变量](/images/2017_05_18_03.png)

    如果我们不使用dimension，那原本构建变量应该是迟迟迟迟3(flavors)*2(debug + release)=6种，那现在为什么是4种呢，看构建变量名字我们就可以推断出来4=2(dimensions)*2(debug + release)，所以dimension可以看作维度的意思，不同维度加构建类型形成最终的构建变量，当你只有一个维度的时候，flavor中可以省略绑定步骤。

1. module中使用Flavor Dimension
    我在moudle中通常是不配置flavor的，因为它们通常是共用代码的存在，但是有特殊的需求还是会用到，因此也需要指定dimension，原则上和app一样，毕竟app本身也是一个module。在library module中使用有一点要注意，就是定义的dimension最好不要重名，官方文档也提到，如果都使用一个名字可能会出现不可控的结果，经过我的实验所有共用的资源文件都失效了。

1. dimension不匹配
    既然使用了flavor和dimension，那很有可能出现module的一个dimension存在多个flavor，这时候后见会出现如下问题：

    ![consumer&producer mismatch1](/images/2017_05_18_04.png)

    ![consumer&producer mismatch2](/images/2017_05_18_05.png)

    这时候可以使用flavorSelection指定某个dimension下选择某个flavor，最终构建变量关系如下：

    ![wonderful dependencies](/images/2017_05_18_06.png)

    这里Google的官方文档表述稍有歧义，但是也有可能是我理解的不好，但是例子很明确，尝试一下就能得得到结论了。

### 0x84 Use the new dependency configurations

这是另一个比较常用的变化，由于Gradle升级到了4.0，很多API发生了变化，其中configrations在3.4就推出了新的API，官方有个表格说得很明白，这里主要说说重要的变化。

1. compile -> implementation
    关键字长了不少，其主要是用来代替compile关键字，它使用在module中时只在编译期有效，对于依赖module的app（官方文档称之为消费者）在编译期运行时都有效，也就是说多个依赖一次打包，可以很好的节约我们的时间。

1. compile -> api
    这个行为类似implmentation，只是即使在module中也会在运行时有效，也就是说无论在哪个module里都会参与打包，这种行为最像原本的compile，根据文档app应该使用implementation，除非想暴露api给独立的test模块。

1. provided -> compileOnly & apk -> runtimeOnly
    和原来行为类似。

差不多就这些，其他更新也有，但是不一定都常用，有需要再去翻文档就行了。
