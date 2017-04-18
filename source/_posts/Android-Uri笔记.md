---
title: Android Uri笔记
date: 2017-04-18 19:30:00
tags: [Android,Uri]
---

### 0x81 Uri
最近一段事件接触到OAuth登录，验证通过后通过一定的规则调起Android页面，而这个规则就是老生常谈的东西——Uri。Uri，即全局资源标识符，有的时候也可以是Url（全局资源定位符，多用于Web），它对某一种资源做了通用的描述，因此，我们通常将资源分配一个独一无二的Uri以帮助使用者准确定位。

### 0x82 Uri的结构
我们所讲述的Uri是android.net包下的Uri类，在java.net包下有一个URI类，它们之间并没有非常直接的关系，但是它们所要描述的内容是如出一辙的，Uri类是Android平台下可适性更好的类，ContentProvider及Intent等操作的都是这一个Uri。

Uri的结构遵循[scheme:]scheme-specific-part[#fragment]，这一部分摘自Android API References中对java.net.URI的介绍。其中scheme-specific-part起主要的标识作用，可拆解成[//authority][path][?query-string]，其中authority又可以细分为[host]:[port]，path通常以'/'开头且可以组合，我们在Web中经常打交道的Http Url也符合这一规范，例如https://www.baidu.com:80/search?t=test#top。

```
scheme: https;
scheme-specific-part: //www.baidu.com:80/search?t=test
authority: www.baidu.com:80
host: www.baidu.com
port: 80
path: /search
query-string: t=test
fragment: top
```

### 0x83 Uri提供的方法
Uri提供了parse方法将一个字符串解析成可用的Uri对象，我们便可以使用这个对象取出Uri字符串中的每一部分。

```
scheme: getScheme();
scheme-specific-part: getSchemeSpecificPart();
authority: getAuthority();
host: getHost();
port: getPort();
path: getPath();
query-string: getQuery();
fragment: getFragment();
per-path: getPathSegments(); // 取出path数组
query-string-value: getQueryParameter(String key); // 根据key取出value
```

好啦，Uri就是这么简单，而对于Intent传递的Uri等也是遵循这个规则，在AndroidManifest.xml文件中的<intent-filter>标签</intent-filter>中的<data>标签</data>部分也是按照这些规则进行筛选，后面有机会说一下UriMatcher这些辅助类是如何匹配Uri以及它们的用法～
