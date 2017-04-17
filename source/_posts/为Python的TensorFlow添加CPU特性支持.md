---
title: 为Python的TensorFlow添加CPU特性支持
date: 2017-04-14 18:35:49
tags: [Python,TensorFlow]
---

### 0x81 性能警告
昨天经过漫长的编译时间等待后，我们在python解释器成功导入tensorflow模块说明安装成功，但是当我们测试使用时，却会遇到性能警告的提示。

我们执行以下命令：
```Python
import tensorflow as tf
hello = tf.constant("hello tensorflow")
session = tf.Session()
print session.run(hello)
```
得到了如下结果：

![AVX和SSE警告](/images/2017_04_14_01.png)

### 0x82 添加CPU支持
添加支持的方式很简单，只需要在之前的bazel构建命令里添加对应的参数，这样gcc在执行编译时便会添加对应的cpu特性支持。

`bazel build -c opt --copt=-msse4.1 --copt=-msse4.2 --copt=-mavx --copt=-mavx2 --copt=-mfma -k //tensorflow/tools/pip_package:build_pip_package
`执行又要等待半个多小时，其中5个额外的copt参数便分别对应sse4.1、sse4.2、avx、avx2和fma。编译完成后按之前的流程生成wheel文件并安装即可。
