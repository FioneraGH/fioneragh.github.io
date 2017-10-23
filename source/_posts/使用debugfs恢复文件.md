---
title: 使用debugfs恢复文件
date: 2017-06-10 10:17:29
tags: [Linux,debugfs]
---

### 0x81 debugfs

> debugfs - ext2/ext3/ext4 file system debugger

上面这句话时是manual对debugfs工具的描述，它是ext2/ext3/ext4文件系统的调试器，用于调试或即时修改文件系统的状态，如inode信息等。

### 0x82 基本用法

1. 选择要debug的设备
    使用root用户或者有权限操作设备的用户执行debugfs {device}即可，如`debugfs /dev/sdb1`。

1. 交互界面
    debugsfs交互模式下支持很多指令，有一些和常见的如ls命令的作用与普通的bash命令差不多。

    ![debugfs交互信息](/images/2017_06_10_01.png)

1. 文件删除测试
    我们创建一个文本文件，然后将它删除，然后看一下debugfs所呈现的信息。

    ![创建一个文本文件并删除](/images/2017_06_10_02.png)

    不知道为什么我的lsdel命令返回的结果是空，可能和文件系统挂载时的参数有关，但是使用ls命令能看到我们删除的test.txt文件以及它的inode号，当然还看到了之前删除的vdi虚拟机磁盘文件。

    ![test.txt信息](/images/2017_06_10_03.png)

1. 查看已删除的文件inode信息
    我们都知道操作系统为了保证性能或者说为了避免误操作考虑，在我们使用操作系统的删除命令删除文件文件系统只是修改了索引信息而不会立即删除该文件的内容，除非我们进行了0擦除或覆盖了新文件。因此我们可以使用调试工具查看被删除文件的信息：

    ![test.txt信息](/images/2017_06_10_04.png)

    我们还可以使用`logdump -i {<inode>}`指令查看更详细的信息。

1. 恢复文件
    logdump会根据文件系统日志显示inode对应所在的块号，我们可以使用dd等命令恢复丢失的文件，例如block为3994712，则使用`dd if=/dev/sdb1 of=/tmp/test.txt bs=4096 count=1 skip=3994712`。

当然以上是对于小文件的恢复方式，如果是大文件或目录可以在debugfs中使用`mi {inode}`指令修改删除标记为0，然后使用fsck检查文件系统恢复到lost+found文件夹当中。
