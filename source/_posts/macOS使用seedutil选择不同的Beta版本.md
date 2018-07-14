---
title: macOS使用seedutil选择不同的Beta版本
date: 2018-06-27 16:55:27
tags: [macOS]
---

### 0x81 获取macOS的Beta更新

之前我写过一片获取macOS Developer Beta更新的笔记，[获取macOS的DeveloperBeta更新](https://fioneragh.github.io/2018/01/31/%E8%8E%B7%E5%8F%96macOS%E7%9A%84DeveloperBeta%E6%9B%B4%E6%96%B0/)简单剖析了seedutil是如何控制更新的。

出于一些特殊的需求，我又深入研究一下为什么seedutil的命令执行后会起到这种效果，最终发现根源还在Seeding.framework上。

### 0x82 Seeding.framenwork

如之前说的，macOS内置了Seeding.framework，它提供了seedutil工具来管理beta更新的接收，seedutil的位置：`/System/Library/PrivateFrameworks/Seeding.framework/Versions/A/Resources/seedutil`，Apple并没有开放链接在`/usr/bin`下并且该工具执行需要root权限，我们可以使用sudo来使用这个工具。

我们再看一次seed状态:

```Bash
⇒  sudo /System/Library/PrivateFrameworks/Seeding.framework/Versions/A/Resources/seedutil current
Password:
Currently enrolled in: PublicSeed

Program: 3
Build is seed: YES
CatalogURL: https://swscan.apple.com/content/catalogs/others/index-10.14beta-10.14-10.13-10.12-10.11-10.10-10.9-mountainlion-lion-snowleopard-leopard.merged-1.sucatalog.gz
NSShowFeedbackMenu: YES
DisableSeedOptOut: NO
```

仔细看其实CatalogURL与之前10.13的时候不一样了，没错，这是Mojave配置的地址，其实更新获取的参照就是这个链接，用浏览器打开链接我们会发现它的内容和plist差不多，定义了需要增量更新的标识。

### 0x83 seedutil 影响的配置文件

我们都知道，macOS的配置文件主要存放在三个位置：`/S*/L*/Preferences`、`/L*/Preferences`和`~/L*/Preferences`，这三个目录分别存放了系统基本设置、系统软件扩展设置和用户个人设置，而控制获取更新Catalog的文件就是`/Library/Preferences/com.apple.SoftwareUpdate.plist`：

![SoftwareUpdate.plist](/images/2018_06_27_01.png)

我们可以看到其中定义了很多操作系统当前版本信息，AppStore或者说softwareupdated使用这些信息来获取系统更新（当然所需的数据远远不止这些），可以看到CatalogURL的值正是我们使用seedutil设置的，当我们unenroll的时候，这个配置文件里的CatalogURL也会被删除。

如果你仔细观察CustomerBeta、DeveloperBeta和PublicBeta这三个Seed的CatalogURL，你会发现他们是不完全一样的，当然不仅仅是值上，获取的内容也不一样:)

### 0x84 Seeding提供的配置

那这些配置都定义在哪呢，很自然的我们可以想到seedutil所在的位置，我们去Seeding.framework里一探究竟。

`/System/Library/PrivateFrameworks/Seeding.framework/Versions/A/Resources/`目录全景：

![Seeding.framework](/images/2018_06_27_02.png)

目下有一个SeedCatalogs.plist文件，它定义了10.14下三个Seed对应的CatalogURL：

```plist
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CustomerSeed</key>
    <string>https://swscan.apple.com/content/catalogs/others/index-10.14customerseed-10.14-10.13-10.12-10.11-10.10-10.9-mountainlion-lion-snowleopard-leopard.merged-1.sucatalog.gz</string>
    <key>DeveloperSeed</key>
    <string>https://swscan.apple.com/content/catalogs/others/index-10.14seed-10.14-10.13-10.12-10.11-10.10-10.9-mountainlion-lion-snowleopard-leopard.merged-1.sucatalog.gz</string>
    <key>PublicSeed</key>
    <string>https://swscan.apple.com/content/catalogs/others/index-10.14beta-10.14-10.13-10.12-10.11-10.10-10.9-mountainlion-lion-snowleopard-leopard.merged-1.sucatalog.gz</string>
</dict>
</plist>
```

仔细看的话还有一个ObsoleteSeedCatalogs.plist文件，它定义了已废弃的Seed，其中包括可怜的10.13，感兴趣的可以自己看看，这里就不贴了。
