---
title: 为Python构建TensorFlow支持
date: 2017-04-13 18:50:35
tags: [Python,TensorFlow]
---

### 0x81 直接使用pip安装whl

之前提到过使用Bazel构建系统为Android构建so库和jar包的支持，今天我们构建whl并为Python提供TensorFlow的支持。安装TensorFlow的方式很简单，使用Google提供的whl进行安装即可，根据网上的教程（具体是哪个网站的忘记了，都差不多也都比较旧，安装的都是0.x版本，而最新的已经到了1.1.0-rc2，根据官方的changelog版本1.0.0是一次比较大的更新），都是采用类似的pip安装命令`sudo pip install --upgrade https://storage.googleapis.com/tensorflow/linux/cpu/tensorflow-0.8.0-cp27-none-linux_x86_64.whl`进行安装的，通常情况下安装完成即可在python脚本中通过`import tensorflow`使用tensorflow提供的API。

### 0x82 使用源码构建whl

手动构建tensorflow仍然需要Bazel构建系统的支持，而Bazel依赖于JDK8，准备过程与为Android编译依赖库时一致，这里就不赘述了。

1. ./configure
    在tensorflow目录下运行configure配置文件，就会询问你一系列问题用于配置，包括python解释器位置，hdfs支持以及python库位置等等，然后会出现以下错误：

    ![缺少numpy](/images/2017_04_13_01.png)

    我们需要安装python2-numpy这一科学计算库，例如`sudo dnf install python2-numpy`，再次configure。

1. build pip package
    和使用Bazel构建Android库时类似，执行构建命令`bazel build -c opt tensorflow/tools/pip_package:build_pip_package`，Bazel开始下载一些列依赖并开始构建，期间占用内存会比较多，不弱于编译Bazel时：

    ![编译过程](/images/2017_04_13_02.png)

    最终耗时：INFO: Elapsed time: 2518.980s, Critical Path: 2429.50s

1. 生成whl
    创建一个临时目录比如`~/tensorflow`用于存放whl文件，使用生成的命令`bazel-bin/tensorflow/tools/pip_package/build_pip_package ~/tensorflow`生成whl，你可能会遇到以下错误：

    ![缺少wheel](/images/2017_04_13_03.png)

    使用pip执行`sudo pip install wheel`安装wheel库即可，再次生成wheel文件。

    ![生成的whl文件](/images/2017_04_13_03.png)

1. 安装whl
    `pip install tensorflow-1.1.0rc1-cp27-cp27mu-linux_x86_64.whl`

    ```Shell
    Installing collected packages: html5lib, bleach, markdown, funcsigs, pbr, mock, werkzeug, protobuf, tensorflow
    Successfully installed bleach-1.5.0 funcsigs-1.0.2 html5lib-0.9999999 markdown-2.2.0 mock-2.0.0 pbr-2.1.0 protobuf-3.2.0 tensorflow-1.1.0rc1 werkzeug-0.12.1
    ```
    最终看到以上提示信息的时候，说明安装成功了。

### 0x83 测试tensorflow的python支持

进入交互式python shell，`import tensorflow`如果没有错误提示表明成功。
