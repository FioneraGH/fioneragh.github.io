---
title: 有关Kernel Compile的简易笔记
date: 2015-07-19 20:15:30
tags: [Linux,Kernel]
---

## [迁移]

### 0x80 编译准备

所需组件：make gcc libncurses-dev(用于menuconfig终端图形化支持)
deb:`apt-get install make gcc libncurses-dev -y`
PS：make menuconfig 进行终端图形化配置

### 0x81 内核配置及模块选择

64-bit kernel 64位内核支持
General setup 通用设置
Enable loadable module supprt 可加载模块支持 -Compress modules 模块压缩
Enable the block layer 块层支持 -IO Schedulers IO 算法 -CFQ（绝对平等队列算法）
Processor type and features 处理器类型 -Kernel Live Patching 内核热补丁（4.0new）
Pover management 电源管理 -Suspend to RAM 挂起到内存 -ACPI 高级电源接口
Bus options（PCI） 总线控制 -PCI support PCI支持
Network support 网络支持 -Bluetooth subsystem support 蓝牙子系统支持 -NFS subsystem support NFS子系统支持
Device Drivers 设备驱动 -Network device support 网络设备支持 -Hardware I/O ports 硬件IO端口 -Watchdog
Timer Support watchdog 支持 -Graphics support 图形支持 -Laptop Hybird Graphics 笔记本混合图形支持 -USB support USB支持 -Android 安卓支持
Firmware Drivers 固件驱动
File systems 文件系统 -The Extended 4 filesystem ext4 文件系统支持
Kernel hacking 内核调整
Security options 安全选项
Save 为.config配置文件8

### 0x82 其他配置方法

make config 初始化配置，包含默认选项，可使用ARCH=""参数指定默认选项，此方式为问询式CLI

make oldconfig 检测先前内核配置或使用.config作为默认配置，原.config被另存为.config.old

make xconfig X11下的内核配置图形端，默认QT端支持，GTK+通常使用gconfig

make localmodconfig 本地模块筛选（lsmod），将已加载并正在使用的模块导入.config，会减少大量的模块编译从而减少编译时间（4.1.2 i3默认编译时间由两小时缩短至22分钟，取决于lsmod所显示加载模块），但坏处也很明显，会导致必要模块漏编译，从而影响正常使用。因此采用这种方法，推荐使用时间长久一些并进可能的覆盖自己的操作以确保需求模块加载从而能够加入编译列表。

### 0x83 编译

make 编译 (此处的zImage 和bzImage 决定使用gzip还是bzip2进行压缩)
make modules_install install 安装模块到内核到/boot

> 最后grub2快捷生成引导 grub2-mkconfig -o /boot/grub2/grub.cfg
