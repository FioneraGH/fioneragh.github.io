---
title: 有关Node.js 试玩网络操作的简易笔记
date: 2015-07-07 21:14:02
tags: [node.js]
---

## [迁移]

### 0x80 nodejs网络操作
http模块，创建一个简单的http服务端。

```javascript
    var http = require('http');
    http.createServer(function (request,response){
	    response.writeHead(200,{'Content-Type':'text-plain'});//写statuscode和htmlhead
	    response.end('hello world');
        console.log(request.method);//请求方式
        console.log(request.headers);//请求头内容
    }).listen(8000);//listen on 8000
    var request = http.request(params, function (response) {});//客户端发起请求
    var request = http.get(url, function (response) {});//Get方式
    
    var params = {
        hostname: 'localhost',
        port: 8000,
        path: '/',
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    };
```

### 0x8F 没了哈哈哈哈
