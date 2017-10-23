---
title: Linux 挂载点设置
date: 2017-02-07 19:16:10
tags: [Linux,Mount]
---

### 0x80 /etc/fstab

Linux 内核的操作系统支持多种多样的文件系统，而它的文件系统同样具有类似Windows分区的功能——挂载点。
`/etc/fstab`文件就是储存有文件系统信息和对应存储设备信息的配置文件。

### 0x81 配置规则

> `<file system> <mount point> <type> <options> <dump> <pass>`

### 0x82 文件系统

需要配置的存储设备或文件系统：

`UUID=d5bd8991-33f6-4bce-aa6b-506703c5adb5` 全局UUID

`/localhost:/` localhost对应主机的根目录

`LABEL=/home` 文件系统标签

`/dev/sdb4` 文件系统路径

`tmpfs` 内存文件系统

### 0x83 挂载点

挂载点路径：

`/dev/sda2 /home/fionera/Data` 表示将sda2挂载到/home/fionera/Data目录

### 0x84 文件系统/分区类型

挂载点以怎样的文件系统类型挂载到文件系统（或者说文件系统以何种类型挂载到挂载点）：
文件系统类型有很多，Linux下有btrfs、ext2/3/4、xfs、swap（交换分区）等，Windows有vfat、ntfs（用于读写的ntfs-3g），其他iso9660、nfs、hfs等

`tmpfs /var/log tmpfs` 内存除了提供内存基本功能外，还被/var/log用内存文件系统的方式挂载

### 0x85 挂载选项

挂载选项有非常非常多，具体可以看`mount`的男人是如何解释这件事情的，这里挑几个简单说说。

FILESYSTEM-INDEPENDENT MOUNT OPTIONS：

`atime,noatime,diratime,nodiratime` inode(dictionary) access time，文件/目录访问时间是否更新

`auto` 会被mount命令自动挂载

`defaults` 很常见，是`rw, suid, dev, exec, auto, nouser, async`的集合

`rw,ro` 可读写/只读

`suid` suid和sgid是否生效

FILESYSTEM-SPECIFIC MOUNT OPTIONS：

`discard` btrfs、ext4，用于SSD，主要用于trim

`subvol*` btrfs，子卷管理

`journal*` ext3/4，日志型文件系统选项

`mode/umask` 权限配置

### 0x86 DUMP备份选项

0为忽略，1为备份

### 0x87 文件系统检查

用于fsck，其中0为跳过检查，>0 为检查顺序。
