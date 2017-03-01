---
title: Wine 解决中文字体方框
date: 2017-03-01 09:34:23
tags: [Wine]
---

### 0x81 Wine
Wine的全称Wine Is not An Emulator，意思是Wine并不是一个模拟器，的确是这样。
Wine和hyperv这种虚拟机的实现原理不一样，它并没有模拟整个OS逻辑，而是采用有点类似Arm Translator的解释器的方式，动态的将Windows的系统调用转换成Posix调用，从而使用Windows软件。
Wine 2.2 于2月末发布，并且官方将默认系统兼容曾提升到Windows 7。而由于我有使用IDA的需要，Linux版的Decompiler只有Arm支持，因此决定采用Wine的方式模拟运行Windows版IDAPro6.8。

### 0x82 Wine 中文字体问题
安装完Wine，并启动软件时，我们会发现只有一部分中文字体显示正常，其余的都显示方框（Wine 1.9.2的时候我记得所有的中文字都是方框）。
这是因为Wine使用的字体是Tahoma，而它对中文字体的支持并不好，或者说不提供支持，能显示一部分中文或许是因为个别地方使用了SimSun。

### 0x83 字体更换
字体的配置主要在注册表当中，`HKEY_LOCAL_MACHINE\Software\Microsoft\Windows NT\CurrentVersion\Fonts`键下映射了当前系统中已存在并缓存的所有字体。
`HKEY_LOCAL_MACHINE\Software\Microsoft\Windows NT\CurrentVersion\FontSubstitutes`键下保存了字体的替代方案。
`HKEY_LOCAL_MACHINE\Software\Microsoft\Windows NT\CurrentVersion\FontLink\SystemLink`键下保存了字体的链接。
所以更换时一般有两种方式：修改FontSubstitutes或SystemLink：

1. FontSubstitutes
    这里面是字体的替代方案，指定当系统需要这种字体时用哪一种字体替代，它配置的是要替代的字体名，网上很多解决办法就是通过在`~/.wine/dosdrive_c/windows/Fonts`下放置需要的字体（如果不是用Linux系统已有字体，而想要给特定的Wine Enviroment设定字体，可以放在这），
    然后配置替代方案。

2. FontLink\SystemLink
    这里配置字体链接，当系统加载某一种字体时实际上读取了哪个字体文件就可以在这里指定。我的配置如下：
    
    ![01](/images/2017_03_01_01.png)
    
    这样重启Wine程序就能看到效果了。