---
title: 使用btrfs-convert转换ext4
date: 2018-02-06 21:00:25
tags: [Linux,btrfs]
---

### 0x81 btrfs

前面提到APFS的时候,涉及到一个很棒的特性--COW(Copy On Write),即写时复制,它的工作原理不在这里展开,通俗的讲,就是复制或使用副本时不会真正的创建副本,而是藉由操作系统的支持,共享数据资源,而当有程序有写入需求时,此时不能再公用同一资源,因此要进行真正的复制.

其中High Sierra在Mac设备上默认启用的APFS自然支持这一特性,从而减少一些资源在使用时的占用空间,那Linux是否有文件系统支持这一特性呢?出于实验COW特性和做一些测试的目的,我找到了之前被诟病极不稳定的btrfs.

btrfs算是 比较年轻的文件系统,但算起它最开始进入kernel stage也有一段时间了,并且btrfs的维护者声称它目前已经足够稳定以投入生产使用甚至专门为SSD进行了优化,良好的trim支持让用户不必担心ext4"烧盘"的问题,虽然我在ext4下使用discard参数使用了很久也没出现很严重的性能问题或损坏,但我还是再次尝试了这一文件系统.

原则上讲,我们使用btrfs最好是从头开始,但是碍于自己在折腾了arch后实在是不想在重新安装并配置各种环境并且根挂载点是在单独的小分区上,我便决定直接转换ext4到btrfs,而转换时使用的工具就是btrfs-convert.

### 0x82 btrfs-convert

btrfs-convert是一个转换工具,使用方式很简单,看它的[Manpage](https://btrfs.wiki.kernel.org/index.php/Manpage/btrfs-convert),wiki里写到他可以转换ext2/3/4和reiserfs到btrfs.

转换工具使用就一句命令:`btrfs-convert /dev/sda2`,需要注意的是我们通常在live环境下执行,因为你无法转换一个已被挂载或正在使用的文件系统.以ext4为例,经过不确定时间的转换,原本的数据会以subvolume的形式保存,名字为ext2_saved.

<!--more-->

### 0x83 rollback

如果你后悔了,btrfs的快照功能也恰巧保证了回滚特性的支持,如上所说的ext2_saved子卷便是用于回滚的数据备份,因此如果我们想回滚,千万不能执行`btrfs balance`命令来平衡空间刷新metadata,否则回滚将会失败.

### 0x84 grub & boot

在转换完文件系统之后,你一定会意识到GPT上的对应UUID已经发生了变化,而你如果用到了UUID,那这个变化将可能导致原本的操作系统引导失败,它们主要影响了3个地方:

1. grub2 root=UUID mismatch

    grub2-efi提供的grub2-mkconfig工具生成的grub.cfg里,root=UUID=XXX中的XXX是根分区原本的UUID,如果我们不对grub.cfg作相应的更改,必将导致引导出错.通常情况下我们不被推荐直接修改grub.cfg,我们可以通过chroot切换root环境重新生成grub.cfg.

1. initramdisk failed

    在安装内核的时候,由于很多部分(包括内核模块,不包含外挂模块)是immutable的,所以内核安装工具会构建缓存文件成ramdisk以供设备启动时快速加载和构建一个隔离环境,和macoOS的PrelinkedKernels(`/System/Library/PrelinkedKernels)类似.现在我们转换了文件系统,原本映射的ext4需要启用btrfs模块来保证正确挂载我们的根分区,因此我们需要mkinitrd工具重新生成ramdisk.

    PS:我其实一开始没有执行这步,但是遇到btrfs没法使用的状况,查看mount参数发现根分区是ro的,经过查阅资料发现某些情况下遇到panic会导致btrfs从rw回落到ro以保护数据,而我mkinitrd重新生成ramdisk并更新grub2后便没在遇到这个问题.

1. fstab

    文件系统描述如果使用的是`/dev/`设备描述符则不需要修改,否则要修改对应的UUID.

### 0x85 后续工作

如果你用的还算正常不打算回滚,那接下来就可以做些清理工作从而减少不必要的空间占用:

* fsck: 使用fsck.btrfs或其他检查工具排错败修正错误的元数据

* ext2_saved: `btrfs subvolume delete /ext2_saved`,备份快照已经没有用了

* defrag: `btrfs filesystem defrag -v -r -f -t 32M FILE_SYSTEM`,不是必须的,并且可能会带来问题,但是它可以让数据更加连续,尤其对机械硬盘来讲能提高吞吐,建议live环境下进行

* balance: `btrfs balance start -m /mnt/btrfs`,平衡metadata空间,合并metadata group,释放不必要的空间浪费

到这就差不多了,至于btrfs其他的优化对SSD来讲显得没那么重要,随着不断地使用它能更好地适应我们的操作系统,当然对于这一实验性文件系统,没理由不跟着kernel的更新来保证btrfs更好更快得工作.
