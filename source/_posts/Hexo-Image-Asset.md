---
title: Hexo Image Asset
date: 2017-03-02 10:37:53
tags: [Hexo]
---

### 0x81 官方推荐做法

source目录下默认有一个图片目录提供给模板引擎，这个目录就是Images，放在该目录下的文件可以直接被Hexo使用。

### 0x82 自定义管理

config.yml配置文件，这里面有Hexo的相关配置，其中`post_asset_folder`属性决定是否导出资源文件夹。
这个导出的资源文件夹就不仅仅局限于图片了，音乐视频都可以，它会在你使用`hexo n [template] name`创建一个新的Post时同时生成资源文件夹，并在生成时导出。
