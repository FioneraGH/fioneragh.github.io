---
title: Fedora 26 下编译axel
date: 2017-03-31 18:12:32
tags: [Linux,Axel]
---

### 0x81 axel
axel是一款轻量级的命令行加速下载工具，它支持多线程下载和断点续传，由C语言编写，在Linux上是一款非常实用的下载工具。

### 0x82 为什么要自己编译
在大部分发行版当中，由于axel是比较热门的Linux工具，包管理器通常能够在发行版仓库中找到稳定版本的二进制包，比如在Fedora 26（Branched）的repo中就提供axel2.5版本的二进制包，但是这个二进制包出于稳定性考虑有可能并不会是由最新的源代码编译而成的，所以可能缺少部分特性支持。

今天遇到了一个特殊的问题，我使用axel下载文件时，在过了很长时间的初始化下载之后，终端中报出了Too many redirects错误并终止下载，其意思是太多重定向，可能axel对重定向的支持还不是特别好。经过一番Google，我发现大部分人的问题是出现在http重定向到https时发生的，比如gayhub上release的一些源码打包，而我也发现我的下载地址默认打开其实是一个web页面之后触发的下载。通过查看axel的github issues，我发现在较新的版本已经做了重定向支持，而Fedora仓库提供的又是2.5版本，于是我决定自己编译它。

### 0x83 配置与编译
1. 下载源码包
    github上的源码包已经release到了2.12。[点我可以下载](https://github.com/eribertomota/axel/archive/2.12.zip)

2. autogen 脚本
    在这里遇到了一个坑，autogen执行时遇到了autopoint不存在的问题，经过查询autopoint现在在gettext包里，grub2-efi也依赖了这个包，也就是说这个包肯定已经安装了。经过多次尝试，我才想到，这种手动编译通常需要头文件或开发包，于是我安装了gettext-devel这个包，autogen顺利跑完了并生成了configure文件

3. configure 配置编译环境
    `./configure`又遇到了问题，libssl bot found，我第一感觉是怎么可能？！后来一想，安装了openssl-devel就成功生成了makefile，有了makefile就可以进行编译了。

4. 编译
    老套路，`make & make install`编译完成后会安装到`/usr/local/bin/axel`，安后就可以使用2.12版本的axel工具了～
