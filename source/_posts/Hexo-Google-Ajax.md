---
title: Hexo Google Ajax
date: 2016-01-18 23:02:57
tags:
---

### 0x80 ajax.googleapis.com无法访问
ajax.googleapis.com和fonts.googleapis.com会无法访问，这带来了网页加载起来非常的慢，将themes文件夹下`layout/_partial/head.ejs` 和`layout/_partial/after-footer.ejs` 两个文件中的googleapis替换成ustclug。

### 0x81 hexo deploy 每次输入密码
修改目录下的.deploy_git/.git/config 文件