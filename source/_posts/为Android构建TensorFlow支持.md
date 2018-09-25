---
title: 为Android构建TensorFlow支持
date: 2017-04-12 17:54:55
tags: [TensorFlow,Bazel]
---

### 0x80 前言

本文参考[《Android TensorFlow Machine Learning Example》](https://blog.mindorks.com/android-tensorflow-machine-learning-example-ff0e9b2654cc#.i8l12xa39)

### 0x81 TensorFlow

TensorFlow是一个机器学习的开源库，出自Google之手，它的核心由C++编写而成，当然Google推荐使用python进行上层API的开发，我们可以通过JNI在Android进行调用。

### 0x82 Bazel

[Bazel](https://bazel.build/)，一个构建系统，根据它官网的描述，Bazel是Google使用的构建系统。Bazel现在仍然是beta版本，但不可否认的，它是一个快速可靠可扩展的构建系统，你可以根据自己的需要书写构建规则。

<!--more-->

### 0x83 编译Bazel

要使用Bazel构建TensorFlow，首先我们要编译Bazel。由于官方只对Ubuntu和macOS提供二进制文件支持，如果我们使用的是其他发行版，就需要我们自己从源码编译可用的二进制文件，编译很简单。当然官方也提供了installer，如果有需要可以自行下载。

编译Bazel有一个硬性条件，就是需要JDK8的支持（JDK7已被标记为废弃）。准备好编译环境后我们只需要到[发行说明](https://github.com/bazelbuild/bazel/releases)网站下载对应版本的dist压缩包，解压后执行`./compile.sh`即可开始编译，编译后的文件输出到output/bazel，然后我们就可以使用Bazel构建TensorFlow需要的库了。

### 0x84 下载TensorFlow源码

TensorFlow的源码托管在Github上，可以使用Git工具直接克隆下来：`git clone --recurse-submodules  https://github.com/tensorflow/tensorflow.git` 使用recurse参数可以连同thirdparty这些子模块一起克隆下来，而不用再手动处理子模块问题。

### 0x85 添加Android支持

修改WORKSPACE文件，把其中的Android配置部分依次修改为自己的SDK、NDK环境信息，例如：

```Property
android_sdk_repository(
    name = "androidsdk",
    api_level = 25,
    build_tools_version = "25.0.2",
    # Replace with path to Android SDK on your system
    path = "/home/fionera/LinuxIDE/android-sdk-linux",
)

android_ndk_repository(
    name="androidndk",
    path="/home/fionera/LinuxIDE/android-sdk-linux/ndk-bundle",
    api_level=21)
```

这样在我们进行Android的so库和jar包编译时便可以顺利进行了。

### 0x86 编译so和jar

```Shell
bazel build -c opt //tensorflow/contrib/android:libtensorflow_inference.so \
   --crosstool_top=//external:android/crosstool \
   --host_crosstool_top=@bazel_tools//tools/cpp:toolchain \
   --cpu=armeabi-v7a
```

该命令将在`bazel-bin/tensorflow/contrib/android/libtensorflow_inference.so`目录下生成armeabi-v7a架构的so库。

```Shell
bazel build //tensorflow/contrib/android:android_tensorflow_inference_java
```

该命令将在`bazel-bin/tensorflow/contrib/android/libandroid_tensorflow_inference_java.jar`下生成对应的jni调用jar包。

### 0x87 下载模型和训练文件

[下载地址点我](https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip)，下载完成后便可以使用了～
