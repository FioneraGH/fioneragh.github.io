---
title: 正确的配置StartupWMClass
date: 2017-08-29 10:59:52
tags: [Linux,StartupWMClass]
---

### 0x81 启动器与窗口分组

众所周知,Linux各大发行版默认使用的桌面环境不一,所以为了尽可能的规范化应用Launcher的使用,各大窗口管理器和桌面环境按照WM_CLASS区分启动器以及进行窗口分组操作.以gnome-desktop为例,系统内置的Nautilus文件管理器能够很好的处理窗口分组:

![Nautilus的窗口分组](/images/2017_08_29_01.png)

但是如果我们使用menulibre或者手动创建.desktop启动器描述文件,很多程序却没办法正确的分组,甚至桌面环境无法正确的识别程序的启动,这个时候就需要修改WM_CLASS以保证程序的各个窗口归入一个分组.

### 0x82 Desktop描述文件

.desktop是GNU下默认的程序描述启动器描述文件,通过包管理安装的包的二进制启动器通常在`/usr/share/applications`目录下有对应的描述文件,它会显示在LauncherPad当中供用户点击启动程序.相对应的,在个人用户目录的`.local/share/applications`当中可以添加用户自定以的启动器,而menulibre就是一个图形化编辑器,方便我们创建或修改启动器.

![典型的.desktop](/images/2017_08_29_02.png)

### 0x83 WM_CLASS与StartupWMClass

WM_CLASS是一个特殊的运行时变量,用来指示xdg程序的窗口管理器识别类型,桌面环境通常读取窗口的这一变量来区分某些窗口所属,将相同的WM_CLASS窗口归为一组并与启动器关联.我们可以使用.desktop文件的StartupWMClass属性来手动控制这一行为,那我们如何获取窗口的类型呢?使用`xprop WM_CLASS`可以读出窗口的X11属性WM_CLASS,通过获取的值和StartupWMClass一致就可以做到启动器与对应的窗口关联,对于自定义应用,GNOME3的收藏夹在也不是单纯的快捷方式了.
