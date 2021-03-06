---
title: 安装weex-toolkit小记
date: 2017-05-15 18:45:06
tags: [npm,Weex]
---

### 0x81 权限问题

其实这个问题由来已久，在很久之前第一次尝试使用Weex的时候遇到过这个问题，当时按照提示解决了之后便抛于脑后，直到前一段时间我在Home目录下执行了清理命令（rm -rf * :P），当我意识到部分工具坏掉了，不得不重新进行一些工具的重装，所以我再一次遇到了权限问题。

### 0x82 解决方案

其实权限的问题是最容易解决的，但也是最容易忽略和出问题的。我们使用npm的-g参数全局安装weex-toolkit的时候，如果不使用类似sudo的命令提权，后出现npm没有权限像/usr/lib/node_modules目录写入文件的问题。通常的处理办法都是使用sudo再去执行命令，但是这样有一个问题，npm命令执行写入的文件的权限信息是nobody组user用户的：

![npm全局安装的权限信息](/images/2017_05_15_01.png)

而安装weex-toolkit的时候，有一个脚本会向/root目录写入.xtoolkit文件夹，而/root目录的权限是550，显然会写入失败。于是我尝试手动创建了.xtoolkit文件夹，这简直异想天开，这个错误我犯了两次，原因就是我对Linux的权限控制仍然理解不透彻。不得已我放开了/root的权限到777，安装完之后再改回550，这样weex-toolkit就可以安装成功，/root目录下npm相关的权限信息是user组和user用户，这样.xtoolkit就能写入文件了。
