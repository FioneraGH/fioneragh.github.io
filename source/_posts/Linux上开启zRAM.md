---
title: Linux上开启zRAM
date: 2017-06-05 18:10:25
tags: [Linux,zRAM]
---

### 0x81 压缩内存
OS X 10.9添加了一项新特性，名字叫做压缩内存，在系统资源监视器里可以看到每个进程压缩后的内存大小，可以有效增加内存空间的利用率，带来更顺畅的系统使用体验。

### 0x82 zRAM
Linux还很早的时候就在内核当中集成了这一特性，zRAM是一种压缩内存的内核模块实现，它允许操作系统对不活跃的内存进行压缩存储，从而减少内存占用，压缩算法通常是lzo和lz4等。

### 0x83 开启zRAM支持
开启zRAM的方法很简单，就是对内核的模块特性进行配置而已，通过以下步骤可以开启一个简单的zram设备并使用压缩内存的功能：

1. 加在内核模块zram `modprobe zram`
2. 分配zram内存大小 `echo $((1024*1024*1024)) > /sys/block/zram0/disksize`
3. 转换`/dev/zram*`为swap分区 `mkswap /dev/zram0`
4. 挂载`/dev/zram*`到swap分区 `swapon /dev/zram0`

通过以上几个简单的步骤，就可以启用压缩内存的功能了，当内存比较紧张开始使用swap分区时，便会压缩内存数据放入zram当中，其中有需要的大部分参数都可以在第二步各自对应的文件中进行参数配置，例如`echo lz4hc > /sys/block/zram0/comp_algorithm`可以设置压缩内存的算法为`lz4hc`。

### 0x84 zramctl
systemd的普及带来了大量的*ctl工具，其中zramctl便是提供了一些常用的zram配置，使用zramctl有一个前提就是需要先加载zram模块。

![zramctl常用参数](/images/2017_06_05_01.png)

![直接执行查看状态](/images/2017_06_05_02.png)

常用参数：
* -f/--find 寻找一个空闲的设备，如果所有设备均忙则生成一个新的zram回环设备。
* -r/--reset {loop} 重置一个空闲的设备，如果该设备忙将被拒绝，空闲将被删除。
* -a 寻找新设备时用于指定压缩算法，可以直接修改设备描述文件。
* -s/--size 寻找新设备时用于指定尺寸，默认为字节，可以直接修改设备描述文件。
* -t 寻找新设备时用于指定压缩流数量，默认为1。
* zramctl -f --size 1024M 创建一个1G大小的zram设备。
* zramctl --reset /dev/zram0 删除设备zram0，仅在空闲时可删除。

常用参数差不多就这么多，如果有需要可以到/sys/block/zram*目录下做对应的更改。

### 0x85 Copr zram
Copr是Fedora下的三方软件源，有点类似Arch Linux的AUR，这上面提供了不少好用的第三方脚本，其中就有zram支持，并且维护比较活跃。

以 frantisekz / zram 为例，用systemd启动脚本加载了[FedoraZram](https://github.com/zezinho42/FedoraZram)的脚本用于启用和关闭zram支持，包内容不多，我们主要来看看mkzram.service和zramstart文件。

```
/usr/lib/systemd/system/mkzram.service

[Unit]
Description=Enable compressed swap in memory using zram
After=multi-user.target

[Service]
RemainAfterExit=yes
ExecStart=/usr/sbin/zramstart
ExecStop=/usr/sbin/zramstop
Type=oneshot

[Install]
WantedBy=swap.target
```
可以看到启动时调用了`/usr/sbin/zramstart`：
```Bash
#!/bin/sh

num_cpus=$(nproc)
[ "$num_cpus" != 0 ] || num_cpus=1

last_cpu=$((num_cpus - 1))
FACTOR=33
[ -f /etc/sysconfig/zram ] && source /etc/sysconfig/zram || true
factor=$FACTOR # percentage

memtotal=$(grep MemTotal /proc/meminfo | awk ' { print $2 } ')
mem_by_cpu=$(($memtotal/$num_cpus*$factor/100*1024))

modprobe -q zram num_devices=$num_cpus

for i in $(seq 0 $last_cpu); do
	#enable lz4 if that supported
	grep -q lz4 /sys/block/zram$i/comp_algorithm && echo lz4 > /sys/block/zram$i/comp_algorithm
	echo $mem_by_cpu > /sys/block/zram$i/disksize
	mkswap /dev/zram$i
	swapon -p 100 /dev/zram$i
done
```
启动脚本内容也很简单，设置了zram设备占用内存比例，计算出逻辑CPU个数，然后就是和之前手动开启一样的操作开启多个zram设备。

很多系统特性其实在Linux内核当中早就有了实现，只是各发行版为了照顾不同的机器可能默认不会开启，而内存压缩除了zRAM还有zSwap和zCache，感兴趣的可以尝试一下效果有什么不同。

