---
title: Sudoers Configuration
date: 2017-02-05 12:16:10
tags: [Linux,Sudoers]
---

### 0x81 概述

通常我们在使用Linux某发行版系统时，为了避免误操作或其他的需求，我们会创建一个个人账户用于日常使用。
但是当需要对系统进行操作时，往往需要相应的权限，我们可以使用`su`命令切换到相关用户（比如root）来获得操纵某个文件或执行某个命令的目的。
`sudo`命令是为了方便用户提升权限执行相应操作的软件，他可以使用户验证通过自己身份时提权。
既然是为了方便使用，我们往往需要对其进行配置。

### 0x82 配置文件

sudo命令的配置文件路径是`/etc/sudoers`，该文件虽然可以直接编辑，但是为避免出现配置错乱问题，最好使用`visudo`这一标准命令进行编辑。

### 0x83 基本语法

* 主机别名

    Host_Alias SERVER = host1, host2

    可以定义一组主机，用于约束Sudoers可获取权限的主机。

* 用户别名

    User_Alias USER = user1, user2

    可以定义一组用户，用于约束哪些用户可以获取权限。

* 命令别名

    Cmnd_Alias COMMAND = /usr/bin/bin1, /usr/bin/bin2

    可以定义一组命令，用于约束哪些命令可以提权执行。

* 默认配置

    Defaults env_reset, timestamp_timeout = 10

    指定sudo通过验证的会话有效期。

* 约束规则

    user MACHINE=COMMANDS %group localhost=(ALL) NOPASSWD: COMMANDS

    某用户/组可在某HOST上通过sudo执行某些命令。`(ALL/root)`指所有/root用户
