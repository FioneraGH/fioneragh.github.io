---
title: JUnit下缺失MockJar的解决方法
date: 2017-10-23 10:21:08
tags: [JUnit,Mock]
---

### 0x81 TestCase执行失败

最近升级几次Android Studio的beta版本经常遇到几个问题,JUnit执行单元测试时总是失败,原因是android包下的类找不到.虽然原则上讲JUnit的测试类中应该尽可能避免Android Framework的类出现,但有时候为了方便(偷懒)也会出现这种状况.然而神奇的是在之前的状况下是可以正常运行测试类的,因为IDE会生成对应的MockJar来模拟实际上不存的类,经过排查,发现Root Project下build文件夹中原应该生成的`mockable-android-[api]-*.jar`并没有出现,这也是导致TestCase执行失败的原因.

### 0x82 手动重新生成MockJar

我们在执行Gradle的Clear任务或Rebuild任务时,会删除build文件夹的内容重新生成,但是不管是配置有问题或者是IDE本身存在的bug,都有可能导致MockJar的生成失败,从而导致Android JUnit执行失败,如果要保证正常的执行测试用例,我们需要重新生成对应的文件.

修复方式通常有两种:

1. 通过拷贝其他工程生成的MockJar,因为对应api平台下IDE生成的MockJar通常是一致的,我们可以从其他工程中拷贝该文件到对应工程下,但是一但触发Clear任务会导致该文件被删除.

1. 使用Gradle任务重新生成MockJar,可能个别版本的IDE或Gradle存在bug,导致MockJar没有生成,我们可以使用以下命令强制运行生成任务:

* `gradle mockableAndroidJar --info` 查看MockJar的状态信息

* `gradle mockableAndroidJar --rerun-tasks` 重新执行生成任务

### 0x83 "xxx" is not mocked method

在执行单元测试时还有可能遇到一个很奇葩的问题,某某类的某个方法没有被mock,对于这种状况其实也可以理解,某些情况下个别方法不存在模拟的普适性就不会被模拟.

对于这种状况解决方法其实不难,我们可以自己模拟实现没有被mock的方法,也可以通过一个Gradle配置忽略结果让他返回默认结果从而保证测试代码能顺利执行下去:

```Groovy
testOptions {
    unitTests.returnDefaultValues = true
}
```

> 对于上面这种方式,只能用在其结果没有影响的情况下,不可作为解决问题的根本方式
