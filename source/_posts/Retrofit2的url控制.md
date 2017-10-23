---
title: Retrofit2的url控制
date: 2017-05-25 20:13:33
tags: [Android,Retrofit]
---

### 0x81 Retrofit的url匹配方案

Retrofit是Square推出的Restful网络请求框架，由于它可以和RxJava无缝衔接而非常受开发者欢迎。Retrofit基于同属Square旗下的OkHttp这一高效网络请求库，Android4.4开始Google甚至将其作为系统默认的网络请求库。

Retrofit使用Builder初始化时，我们通常需要传入一个BaseUrl，而这个url就是我们进行http请求的基础路径。Retrofit2的url有几个规则：

1. BaseUrl遵循Rest规范且需以'/'结尾
    使用`http://cloud.in/api/`是正确的，而`http://cloud.in/api`会抛出异常。

1. @GET/@POST等注解路径以'/'开头
    如`@GET("/apiv3/join/3/")`，则最终请求url为`http://cloud.in/apiv3/join/3/`。

1. @GET/@POST等注解路径不以'/'开头
    如`@POST("/find/4/name/")`，则最终url为`http://cloud.in/api/find/4/name/`。

1. @GET/@POST等注解路径以'http'开头
    如`@DELETE(http://cloud.in/api/member/45177/)`，则最终url为`http://cloud.in/api/member/45177/`。

使用这种特殊的匹配规则，有的时候可以满足我们特殊的需求。

### 0x82 使用规则做api测试
