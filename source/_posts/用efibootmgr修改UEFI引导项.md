---
title: 用efibootmgr修改UEFI引导项
date: 2017-12-26 10:18:49
tags: [efibootmgr, UEFI]
---

### 0x81 efibootmgr的作用

相比校传统的MBR引导方式,Intel坚持的UEFI则更加可控,灵活和自由.UEFI又叫通用嵌入式固件接口,近几年的主板BIOS通常都实现了UEFI支持,可以运行EFI程序.早期出于好奇,我试用了非常多的Linux发行版,而这些版本由于采用传统的MBR引导方式安装,基本每一次fresh install都能清理掉就的引导记录.在某次偶然的机会将硬盘转换为了GPT分区表后,便不能使用传统的安装方式,借助grub2-efi的支持,在GPT分区上使用EFI分区内的efi引导程序进行系统的引导.

在使用EFI后,大约过了一年半的时间,期间因为各种问题也切换过发行版,后来在一次清理中发现了好几个残留的UEFI引导项,到这时候才意识到之前的选项都没有删掉,于是便找到了efibootmgr这个工具,它可以管理UEFI的引导项.

efibootmgr支持引导项的查看,添加,修改和删除,功能比较强大,但是操作时尤其是删除操作要极为慎重,不小心删掉的硬件引导(比如网卡引导)可不太容易恢复.

### 0x82 使用efibootmgr修改EFI

1. 查看引导项

    使用efibootmgr可以直接查看当前固件里的引导项:

    ```Shell
    fionera@fionera:~|⇒  efibootmgr
    BootCurrent: 0003
    Timeout: 2 seconds
    BootOrder: 0003,0008,0009,0005
    Boot0003* fedora
    Boot0005  Windows Boot Manager
    Boot0008* UEFI: IP4 Realtek PCIe FE Family Controller
    Boot0009* UEFI: IP6 Realtek PCIe FE Family Controlle
    ```

1. 删除引导项

    efibootmgr提供删除参数-B(--delete-bootnum),该参数后接要删除的项编号,使用该指令可以删除Windows Boot Manager:

    ```Shell
    efibootmgr -B -b 0005 // 0005 为bootnum
    ```

1. 新增引导项

    如果要新增一个引导项,我们需要进行多个参数的组合,包括名称和EFI路径等,下面的指令可以为`p{EFI}/EFI/CLOVER/CLOVERX64.efi`添加一个引导项:

    ````Shell
    efibootmgr -c -w -L "Clover" -d /dev/sda -p 1 -l \\EFI\\CLOVER\\CLOVERX64.efi
    ```

    上述指令中各参数的意义:
    * [-c] 创建一条引导
    * [-w] 这一次创建会写入EFI引导信息
    * [-L] 引导项的Label
    * [-d] 指定使用的物理硬盘描述符
    * [-p] 分区号,从1开始默认为1
    * [-l] EFI文件位置,'/'位置代表前面指定的根分区,由于通用性考虑,命令使用'\\'代表文件路径分隔符

1. 编辑引导项

    在未知时创造是困难的,在已知时修改是可控的,与删除类似,efibootmgr提供了很多参数来修改引导项或整个引导过程:
    * [-a] 激活某一条引导
    * [-A] 反激活
    * [-o] 对bootnum排序
    * [-O] 删除bootnum排序
    * [-t] 设置EFI超时时间
    * [-T] 删除EFI超时时间

efibootmgr的常用操作差不多就这些,man手册其实写的很明白,这里主要是记录一下添加一条新引导的方式,方便自己灵活控制系统加载,比如用来Clover来引导Hachintosh:P.
