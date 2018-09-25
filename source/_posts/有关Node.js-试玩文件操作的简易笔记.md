---
title: 有关Node.js 试玩文件操作的简易笔记
date: 2015-07-06 21:27:40
tags: [node.js]
---

## [迁移]

### 0x80 iojs

node.js与io.js的故事真是黄金档连续剧啊2333

### 0x81 干嘛的

node.js旨在搭建高性能的web服务用于处理并发数万的连接，但是我感觉拿来玩玩真不错～

### 0x82 nodejs安装

可通过tarball方式编译安装 debian系安装非常容易 `apt-get install nodejs npm`即可。  
nodejs 是解释器 npm 是nodejs包管理工具。

<!--more-->

### 0x83 nodejs权限

若要开启端口绑定服务器80443需要root权限，可以sudo也可以运行时提权。  
`chmod +s /usr/bin/nodejs` 赋予nodejs在运行是获得其拥有者的权限。

### 0x84 nodejs基础

nodejs交互命令行 直接在tty输入nodejs即可启动console，可以进行简单的代码段，比如正则表达式的测试。

1. nodejs模块

模块化，毋庸置疑 nodejs模块只初始化一次，有三个预置变量可用，分别是require,exports,module。
require用于导入模块对象，其返回一个模块导出对象['.js'尾缀不是必须得]。
exports用于导出模块对象，即是导出require获得的对象。
module通常使用module.exports来更改导出对象。

1. nodejs包

包即是模块的集合，其通常包含一个入口模块，导入时导入该入口模块。为达到“包”的目的通常不希望路径中出现模块名，
因此通常使用index.js来作为入口模块即可在路径中不写入口模块的情况下使用该包。一般使用package.json配置文件来配置包的内容。

### 0x85 nodejs文件操作

使用fs即可实现nodejs本身不内置的文件拷贝操作
对于小文件：`fs.writeFileSync(dst, fs.readFileSync(src));`使用同步读写的方式，将src拉入内存再写入到dst。
对于大文件：`fs.createReadStream(src).pipe(fs.createWriteStream(dst));`使用管道连接流的方式，因为大文件不能采用占用大量内存的方式进行文件拷贝，最好是采用Stream的方式。

对于传入参数：nodejs copyfile.js src dst 其实分别对应argv[0],argv[1],argv[2],argv[3]，
因此拷贝源文件目的文件分别对应argv[2],argv[3]，为方便记忆，在使用argv参数时，使用内置变量process.argv.slice(2)使argv[0]指向原本的argv[2]。

nodejs构造函数Buffer
由于JS默认只提供String数据类型，因此nodejs构造了对等构造函数Buffer，它与String一样可使用'.length'获取长度，像字符数组一样使用但不像String一样是只读的，且可互相转换
nodejs采用异步IO，其其回调包括error和result两种处理，分别对应失败的处理和成功返回，例如readFile；而readFileSync对应同步IO版本
nodejs的fs模块提供丰富的文件操作fs.stat/chmod/chown（与shell一样的做作用）/readFile/mkdir/open/write/close等，nodejs的path模块用于规范化路径path.normalize(path).replace(('/\\/g', '/'))[正则表达式替换windows的'\'为Unix的'/']

> 以上便是nodejs有关文件操作的小tips～
