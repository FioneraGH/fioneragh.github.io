---
title: Nginx 功能试用
date: 2015-07-03 09:56:38
tags: [Linux,Nginx]
---

## [迁移]

### 0x81 基本功能

基本的反向代理，负载均衡，访问缓存，URl重写，读写分离。

### 0x82 反向代理

反向代理：使用location 进行相应地标识

```Config
location ~*\.php$ { #location 原生支持RE ～用于匹配敏感大小写 ～*用于匹配不敏感大小写 ^~普通匹配（一般用于匹配目录） =用于固定精确匹配（通常用于单一文件，例如=404 ）@指代另一个location
    fastcgi_pass http://host:port; #代理指向的地址端口
    proxy_set_header X-Forwarded-For $remote_addr; #设置请求头添加X-Forwarded-For域，在后端若是apache使用$(X-Forwarded-For)!的方式将真正的请求网站打印出来
}
```Config

反向代理的严苛性

```Config
location /forum/ {
    proxy_pass http://host:port/forum/;
    #proxy_pass http://host:port/bbs/; #本地名称替换
}
```

<!--more-->

### 0x83 负载均衡

负载均衡：使用upstream模块，即上游服务器

```Config
upstream srvs { #srvs 为上游服务器组名
    server 192.168.1.100 [weight=1]; #组内主机地址 [weight指权重]
    server 192.168.1.101 [weight=1]; #组内主机地址 [weight指权重]
    ip_hash; #采用ip_hash的方式进行负载均衡，该方式不能进行backup设置 还有轮询式round_robin等
}
```

upstream的调用

```Config
location / { #/路径全匹配
    proxy_pass http://srvs; #srvs为定义的上游服务器组名
}
```

### 0x84 访问缓存

访问缓存：使用proxy_cache模块

```Config
proxy_temp_path   /data0/proxy_temp_dir; #临时目录
proxy_cache_path  \
/nginx/proxy_cache \ #缓存目录
levels=1:2 \ #缓存目录等级 /nginx/proxy_cache/$1/$2
keys_zone=cache:500m \ #关键缓存区cache 一般指内存，大小500m
inactive=1d \ #非活跃超时 超过一天便清楚
max_size=10g; #最大磁盘空间10g
```

proxy_cache的使用

```Config
location / { #/路径全匹配
    proxy_cache cache; #cache为缓存名
}
```

### 0x85 URL重写

URL重写：使用rewrite模块

```Config
rewrite regex replacement flag; #rewrite模块支持正则表达式，将匹配的替换为replacement，flag通常为last
```

rewite的使用

```Config
location / { #/路径全匹配
    rewrite /forum/(.*)$ /bbs/$1$ last; #last 循环检查、break 终止检查、redirect 临时重定向302、permanent 永久重定向301
    rewrite /forum/index.html http://host/forum/index.html; #该redirect 会发生域名替换 
}
```

### 0x86 读写分离

读写分离：if(condition)判断

```Config
location / { #/路径全匹配
    proxy_pass http://192.168.1.100; #普通只读
    if ($request_method = "PUT"){ #若访问方式为文件上传
        proxy_pass http://192.168.1.101;
    }
}
```

在192.168.1.101上开启webdav功能，编辑httpd.conf：
`WebDAV on`
此时若上传时服务器回应405 Not Allowed则应赋予apache用户/var/www/html/目录权限（`setfacl -m u:apache:rwx /var/www/html/` ）

### 0x87 这就是配置sample了啦啦啦～
