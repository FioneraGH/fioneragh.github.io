---
title: Mysql命令速记
date: 2017-05-13 14:21:04
tags: [速记,Mysql]
---

### 0x80 前言

该篇文章主要用于记录我进行一系列mysql操作的命令笔记，会随着使用不断增加。

在Fedora上使用MariaDB进行测试。

### 0x81 命令

其中无前缀的是bash命令，以':'开头的是mycli命令，以'['和']'包裹的是术语，以';'开始的为SQL语句或部分SQL语句。

systemctl start mariadb // 开启mariadb service
systemctl stop mariadb // 关闭mariadb service
mycli -u root -p {password} // 以管理员登录mysql

[ACID] // 原子性（Atomicity）、一致性（Consistency）、隔离性（Isolation）、持久性（Durability）

:SHOW ENGINES // 显示支持的数据库引擎，InnoDB是MySql5.5.5后默认的支持事务的引擎，InnoDB支持行锁定和外键
:SHOW DATABASES // 显示所有的数据库
:USE {database} // 选择数据库
:SHOW CREATE DATABASE {database} // 查看数据库定义
:SHOW VARIABLES LIKE 'storage_engine' // 查看正在使用的数据库存储引擎

1. Structure

:SHOW TABLES // 显示当前数据库下的表
;PRIMARY KEY(key,kye,eky) // 设定key,kye,eky为联合主键
[参照完整性、引用完整性] // 一个表的外键约束，其外键值可为空，若不为空则必然有对应约束表的主键与之相等
;CONSTRAINT {name} FOREIGN KEY(key) REFERENCES tb_main(id) // 外键约束
;DESC {table} // 查看表定义
;SHOW CREATE TABLE {table} // 查看表定义DSL描述
;SHOW CREATE TABLE {table}\G // 查看表定义DSL语句
