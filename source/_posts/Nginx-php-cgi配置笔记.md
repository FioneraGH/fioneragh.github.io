---
title: Nginx php-cgi配置笔记
date: 2015-07-02 20:52:01
tags:
---

## [迁移]

### 0x81 Nginx是什么
Nginx 是一个服务器引擎，其基本作用类似于Apache，支持单进程多线程响应多个服务器请求（与Apache的prefork不同），并支持反向代理。

### 0x82 正反向代理
简单的讲：
* 正向代理即是一般的代理服务器，用于帮助客户端访问自己不能访问的服务器；
* 反向代理即是服务器的代理，通常服务器指定用于反向代理的地址，用于接受服务器请求。
在本例中，Nginx就相当于代理服务器，而php-cgi就是真正用于解析php脚本的后端。

### 0x83 Nginx的安装
1. Nginx的安装（deb）：`apt-get install nginx` OR `nginx-extras`
2. apt解决依赖之后，在`/etc/nginx/` 目录下存放配置文件，nginx.conf 中读取./conf.d/目录下的配置，./sites-avaliable/ OR ./sites-enabled/目录下的可用server配置镜像。

### 0x84 PHP5的安装
1. PHP5的安装（deb）：`apt-get install php5-cli php5-cgi` ADD `php5-fpm` （用于unix的socket文件方式绑定代理）
2. apt解决依赖之后，在`/etc/php5/` 目录下存放配置文件（fpm目录在安装php5-fpm包之后出现），相应的子目录下的./conf.d/目录中存放相关配置以及加载模块。

### 0x85 Nginx的配置
Server镜像的配置：
```
server {
	listen 80 #监听端口
	root /var/www #镜像根目录
	index index.htm index.html index.php #可视别主页
	#开启php5支持
	location ~*\.php$ { #正则表达式，匹配目录下所有以.php结尾的文件
	fastcgi_pass 127.0.0.1:4444 #php-cgi代理交付cgi地址端口
	#fastcgi_pass /var/run/php5-fpm.sock #php-fpm方式代理交付绑定socket
	fastcgi_index index.php #默认php的主页
	include fastcgi_params #位于/etc/nginx/目录下的CGI参数文件，参考具体安装位置
}
```

### 0x86 Nginx的启动
配置完成后，常规方式（非systemd）使用`service nginx start` 启用nginx服务。
* 若采用php-cgi alone的方式，使用php-cgi -b 4444 & 启用解析php的通道。
* 若安装php5-fpm 且配置server为socket绑定方式则需执行`service php5-fpm start` 才能使php正常解析，否则易出现php文件下载状况。

> 写php文件的?php ?中的php一定不能忘了！一定不能！