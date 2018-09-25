---
title: 解密FileVault2加密的分区
date: 2018-02-05 19:41:05
tags: [macOS,FileVault]
---

### 0x81 FileVault

像Windows的BitLocker一样,Apple也为macOS(最早应该是OSX Lion开始支持)提供了全盘加密的支持,它就是FileVault,目前该项技术名字叫做FileVault2.当你第一次进入macOS的配置向导,或是从某一次重要的更新重新配置你的mac时,FileVault2会有很大的几率露脸,询问你是否要启用加密特性来保护你的个人数据安全.

当然,所有的安全特性包括磁盘加密都会带来性能损失,因为从安全的角度上讲,你的数据要保证安全加密是必须的,而当你要使用这些数据时,就需要通过验证并解密才能成为程序可读的数据.不过话说回来,操作系统会尽可能正确的权衡性能与安全的比重,尤其是Apple整个新产品线都采用nvme的固态硬盘以及使用专门的硬件模块来保证性能.但是第三方SSD就不见得能更好的配合这些为原生苹果硬件提供的特性了,比如臭名昭著的黑苹果,几乎大部分硬件都是fake来得.

### 0x82 APFS

APFS,全程APple FileSystem,译作苹果文件系统,很多人坚持它由ZFS发展而来,但是这不重要.APFS支持COW,对SSD友好,支持SSD的Trim特性以增高IO吞吐,更重要的是它支持逻辑卷管理.正因为APFS支持这些新特性,Apple终于决定换掉用了十几二十年的HFS(+J),从iOS,tvOS,watchOS到macOS都采用了这一文件系统.

APFS的出现对黑苹果用户来讲可不见得是好事,High Sierra默认已经将APFS作为默认的文件系统,在升级或安装若检测到你的硬盘是固态硬盘,将会在升级或安装过程中进行APFS的转换,要注意这个转换是不可逆的.纵然如此,我们仍可以干涉macOS的升级进程,既然升级过程是从HFS+转换到APFS的,说明这个过程是使用标志位控制的,在这个过程中有一个转换操作.

仔细研究过macOS升级过程的小伙伴应该都了解用于安装的app文件里有个非常庞大的dmg文件,dmg文件我们都了解就是磁盘映像文件,和iso/img文件类似,而dmg文件内是一些pkg安装文件和plist或xml配置文件,安装过程首先就是释放这些安装程序和配置文件,我们只需要把配置文件中的ConvertToApfs设置为false就可以阻止转换过程.

为什么要阻止转换成APFS呢,一个是前面说的SSD兼容问题,一个是黑苹果导致的一些乱七八糟的问题,比如刚开始APFS出现导致Clover识别不到APFS分区,后来是Clover通过实现加载macOS安装镜像的apfs.efi来读取APFS,但是APFS在hackintosh上的问题远不止这些.

<!--more-->

### 0x83 CoreStorage

前面说了APFS这么多和FileVault有什么关系呢?

这里不得不说说CoreStorage这一让人又爱又恨的技术,最初被cs坑到是安装10.11时遭遇的4主分区限制,由于个人需要分区多分出了两个,由于原本的分区表存在EFI/RecoveryHD分区,因此加上MachintoshHD和额外的两个分区正好超出了4个分区,导致安装时包报CoreStorage错误,原来是安装程序主动转换HFS分区为能进行逻辑卷管理的CoreStorage,然后在CoreStorage中进行灵活的分卷管理,而这灵活的分卷管理就是相对让人爱的地方,和基础版的LVM用起来感觉差不多.第二个坑是黑苹果升级的坑,CoreStorage下的分区在进行Software Update之后无法向分区写入安装更新引导信息从而导致更新失败,害我那天一直以为是升级没接电源导致的.

那最新的APFS是否也支持逻辑卷管理呢?苹果不会开倒车,APFS天生支持Container并且APFS Volume是建立在APFS Container之中.从Fushion Drive的支持来看,逻辑卷不是某一文件系统的专利,它是一种更为上层的控制逻辑,或者可以认为它是特殊的软RAID,而Fushion Drive就可以视作软RAID,它能让两块硬盘看起来像一块硬盘甚至一个分区在工作.

终于该说FileVault了,这里其实源于我一个同事的黑苹果升级后误选了启用FileVault2加密,乖乖,电脑卡了一下午最后不得已才重启发现Clover居然读不到macOS分区,Clover能识别HFS和APFS,现在读不出来只有一个可能--分区被加密了,通过查阅资料发现FileVault Preboot(Preboot是APFS Container的一项特殊技术,有点类似我之前说的更新需要使用的更新引导信息的载体,不过之前是在一个分区上,而APFS将他们放到一个卷上)也许可以进入,然后输入密码进入设置进行FileVault解密,但是悲剧的是进去之后黑苹果的键鼠都是失效的,干瞪眼= =

### 0x84 强大的diskutil

其实系统遇到问题,我们最直接的想法就是进入恢复模式,macOS的Recovery是我认为恢复环境里做的比较好的,虽然它通常只用来在线恢复系统= =,其实它还提供了磁盘工具可以进行磁盘检查或者删掉多余的分区,但是这个磁盘工具也是被编码猴子们一直诟病的存在,它呈现的功能实在太少了,也许对于普通用户来讲这足够且合理.所幸Recovery提供了终端,我们有了diskutil和一些列命令行工具,就能进行更多的修复工作.

接下来是解密的过程,过程很简单,但是等了好久= =,毕竟要等数据全部解密然后自然而然FileVault会自动关闭.

1. 获取apfs container 信息

    ```Bash
    fionera@Fioneras-MacBook-Air:~|⇒  diskutil apfs list
    APFS Container (1 found)
    |
    +-- Container disk2 9CC08098-8466-435A-ABAD-722607D6E6ED
        ====================================================
        APFS Container Reference:     disk2
        Capacity Ceiling (Size):      89768812544 B (89.8 GB)
        Capacity In Use By Volumes:   47518322688 B (47.5 GB) (52.9% used)
        Capacity Not Allocated:       42250489856 B (42.3 GB) (47.1% free)
        Container Shrink Limit:       58255740928 B (58.3 GB)
        |
        +-< Physical Store disk1s2 D559D0FF-154F-4D2E-A5A2-8FC9F23F97B8
        |   -----------------------------------------------------------
        |   APFS Physical Store Disk:   disk1s2
        |   Size:                       89768812544 B (89.8 GB)
        |
        +-> Volume disk2s1 0070DBC0-5E43-3CB5-9E7F-87863226444D
        |   ---------------------------------------------------
        |   APFS Volume Disk (Role):   disk2s1 (No specific role)
        |   Name:                      M10134 (Case-insensitive)
        |   Mount Point:               /
        |   Capacity Consumed:         46854705152 B (46.9 GB)
        |   FileVault:                 No
        |
        +-> Volume disk2s2 9B3B032E-1B8D-4F16-AC1E-BDCF50C0B15B
        |   ---------------------------------------------------
        |   APFS Volume Disk (Role):   disk2s2 (VM)
        |   Name:                      VM (Case-insensitive)
        |   Mount Point:               /private/var/vm
        |   Capacity Consumed:         20480 B (20.5 KB)
        |   FileVault:                 No
        |
        +-> Volume disk2s3 D89A01D6-70B7-4625-97AB-8876F36F188C
        |   ---------------------------------------------------
        |   APFS Volume Disk (Role):   disk2s3 (Preboot)
        |   Name:                      Preboot (Case-insensitive)
        |   Mount Point:               Not Mounted
        |   Capacity Consumed:         21127168 B (21.1 MB)
        |   FileVault:                 No
        |
        +-> Volume disk2s4 ED0CDC7F-A5EA-42D8-A25E-0FF720CC1A02
            ---------------------------------------------------
            APFS Volume Disk (Role):   disk2s4 (Recovery)
            Name:                      Recovery (Case-insensitive)
            Mount Point:               Not Mounted
            Capacity Consumed:         518991872 B (519.0 MB)
            FileVault:                 No

    ```

    输出结果类似上图,不得不说macOS的diskutil对apfs的信息输出还是比较全面的,我们也可以明确的看到FileVault的开启情况.

1. 获取APFS Volume上的CryptoUser

    ```Bash
    fionera@Fioneras-MacBook-Air:~|⇒  diskutil apfs listUsers /
    Cryptographic user for disk2s1 (1 found)
    |
    +-- 1FA5C6EC-29DF-4132-9B2A-AC61886ADACD
        Type: Local Open Directory User
    ```

    其中那段UUID就是用于解密FileVault的用户标识,这里只有一个LODU,对于加密过的还会有一个iCloud账户,当然如果你手动添加过,也会一一列举出来.

1. decryptVolume

    没什么好说的,解密分卷关闭FileVault,执行之后可以再第一步中看到进度,等到解密完成一切就可以恢复正常.

    但是注意这里有一个坑,就是我第二步讲到的用户标识符UUID,如果我在执行`diskutil apfs decryptVolume <apfsVolumeDisk>`不添加`--user`参数,工具将默认使用`--user disk`也就是分区标识符作为用户标识符.那鉴权是一定会失败的,因此正确的命令应该是至少类似这样`diskutil apfs decryptVolume disk2s1 --user 1FA5C6EC-29DF-4132-9B2A-AC61886ADACD`,等待执行完成,就可以在Clover中看到久违的APFS分区.

不得不说奋斗在黑苹果一线的rah等大牛就是在和苹果的开发人员作斗争,而折腾黑苹果也变成我了解macOS工作机制和系统特性的重要手段,由此出发而接触到APCI汇编,UEFI环境知识都不是我在日常开发工作中所能学习和了解的,在这些盲区学一些用不到的知识不一定没用,或许他们可以拓宽我们的思维,更透彻的了解他们的工作机理.
