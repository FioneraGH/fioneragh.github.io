---
title: Nexus5 MACAddr Change Boom
date: 2016-01-30 12:00:09
tags: [Nexus5]
---

### 0x81 故事背景

说真的MAC地址过滤着实是一件很讨厌的事情，自己手机的WLANMAC被禁用了之后，连Wifi都不能连接，我一琢磨，这不行啊，我得改改Nexus5的WLANMAC逃出黑名单。

### 0x82 原理

可以用`busybox`的`ifconfig`命令修改MAC地址，但是这种方式在5.0之后貌似失效了。因此经过查阅发现Nexus5的`/persist`存有蓝牙和Wifi的配置，不过persist区做为Google为手机设置的DRM，系统启动时会从这里读取一些硬件信息，修改它很有可能带来非常大的风险，毕竟这里是刷新都不会去修改的地方。不过，为达目的仍要进行尝试，方法其实很简单，只要修改`/persist/wifi/.macaddr`文件就行。

<!--more-->

### 0x83 实践

`/persist/wifi/.macaddr`文件是2进制文件，直接用文本编辑器编辑是不可以的。因此我们借用Android平台的HexEditor进行编辑，通过对6个字节的16进制数据进行修改，修改结果就是对应的MAC地址。
当然，你也可以在PC上编辑一个包含MAC地址的文件，比如`echo -n '\x11\x22\x33\x44\x55\x66' > .macaddr`，然后将该文件放入到`/persist/wifi`目录当中，但是要注意权限的处理（大坑！）。

### 0x84 坑们

1. 修改`.macaddr`文件，重启wifi模块后会立即生效。通常改一位就有效果，想和其他设备冲突实在太难了，但有时候没效果你懂得= =。
1. 自己还是作个死，在/sdcard上新建了个文件.macaddr，万恶之源。编辑完了之后，拖到了`/persist/wifi`下，覆盖。。。。我覆盖了！！我不知道权限！！
1. 抱着试试的心态，重启wifi模块，MAC地址果然没变，重启看看，变了。。。每一次重启都在变。。我还挺开心。。。直到我发现另一台路由器是绑定MAC地址的。。。
1. 坑开始了，开始恢复原本的MAC。首先根据`/persist/bluetooth/.bdaddr`设置.macaddr权限`660`，然后观察所有者bluetooth:system修改wifi:system。重启看效果，无效！
1. 我想起了api21之后引入的selinux机制SE Android，仿照.bdaddr的Context:`u:object_r:persist_bluetooth_file:s0`更改.macaddr为`u:object_r:persist_wifi_file:s0`。重启看效果，无效！
1. 此时我开始慌了，各种查阅，权限和上下文都没错啊！外层文件夹的权限我都没有动过！最后看到一个post，他在修复/persist损坏时手动创建blue/wifi文件夹给的权限居然是770，可我的是666啊，抱着既然已经这样的心态，去尝试了一下，最终，还原了。。

### 0x85 教训

生命不息！折腾不止！！
