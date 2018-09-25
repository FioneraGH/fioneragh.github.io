---
title: 配置webpack-dev-server
date: 2017-07-04 19:13:55
tags: [webpack,webpack-dev-server]
---

### 0x81 webpack配置文件

webpack作为一个模块化工具，在提供打包功能时我们通常不会直接调用webpack命令去生成目标文件。因为开发过程中代码是频繁变化的，如果我们手动去处理将会增加非常多的无意义的体力劳动，因此虽然webpack提供的默认配置文件webpack.config.js文件已经帮我们省了很多事了，但这仍然是不够的。对于这种情况webpack2官方由一个构建脚本模板，模板没有特殊需求可以直接拿来用，它启动一个基于express的小型server来观察文件变化自动编译并热交换，这样我们就能在开发当中“所见即所得”。

### 0x82 webpack-dev-server

在最开始学习使用webpack的时候，官方推荐了辅助开发工具，其实这个工具就是express的一个小封装，与webpack的配置相辅相成，webpack-dev-server会主动调用webpack.config.js作为webpack配置文件，而webpack本身也提供devServer选项用于配置webpack-dev-server。

<!--more-->

### 0x83 使用webpack-dev-server

首先webpack-dev-server是可以直接读取webpack.config.js配置文件直接使用的，对于express服务器它将使用默认配置。webpack-dev-server提供了很多参数用于无侵入的配置它的行为，比如`webpack-dev-server --colors --progress`在启用后终端中的log将会带有颜色并展现完整的构建处理过程。

可是webpack-dev-server默认的服务器监听是在`localhost:8080`上，我们可以通过`--host 0.0.0.0`参数让其监听在本地任意ip上，这样局域网内的设备就可以直接访问，端口也可以通过如下方式配置：

```JavaScript
rules: [],
devServer: {
    port: 8080
},
plugins: []
```

可是经过上述的配置后，局域网内虽然可以访问了，但是会返回`Invalid Host Header`错误，经过查阅是webpack-dev-server对host的检查，要想访问成功需要关掉host-check功能，这边需要我们手动配置webpack-dev-server并启动。

### 0x84 配置webpack-dev-server

因为要进行配置，便不能向之前那样直接使用`webpack-dev-server [args]`命令了，我们需要效仿webpack2官方构建脚本来自己实现server调用，我们先来看下webpack-dev-server在tsd中的定义：

```TypeScript
namespace WebpackDevServer {
    export interface Configuration {
        contentBase?: string;
        hot?: boolean;
        https?: boolean;
        historyApiFallback?: boolean;
        compress?: boolean;
        proxy?: any;
        staticOptions?: any;
        quiet?: boolean;
        noInfo?: boolean;
        lazy?: boolean;
        filename?: string| RegExp;
        watchOptions?: webpack.WatchOptions;
        publicPath: string;
        headers?: any;
        stats?: webpack.compiler.StatsOptions| webpack.compiler.StatsToStringOptions;

        setup?(app: core.Express): void;
    }

    export interface WebpackDevServer {
        new (
            webpack: webpack.compiler.Compiler,
            config: Configuration
        ):WebpackDevServer;

        listen(port: number,
            hostname: string,
            callback?: Function
        ): http.Server;

        listen(port: number,
            callback?: Function
        ): http.Server;
    }
}
```

我们可以看到在new的时候需要参数Compiler和自身的定义，listen方法需要指定端口和回调函数，并可以添加hostname，至于Configuration我们就可以使用webpack中定义的。然后我们重新创建一个webpack-dev-server.jsw文件：

```JavaScript
const webpack = require('webpack')
const webpackConfig = require('./webpack.config')
const WebpackDevServer = require('webpack-dev-server')

new WebpackDevServer(webpack(webpackConfig), {
  // for invalid host header
  disableHostCheck: true,
  stats: {
    colors: true
  }
}).listen(8080, '0.0.0.0', err => {
  if (err) {
    console.log(err)
  }
  console.log('Listening started ...')
})
```

```TypeScript
declare function webpack(options?: webpack.Configuration): webpack.Compiler;
```

其中webpack构造函数传入一个webpack.Configuration就可以返回一个Compiler，其中tsd中并没有定义disableHostCheck这个配置量，毕竟tsd作为类型定义文件并不一定完整，通过查阅才找到可以通过配置它来避免`Invalid Host Header`错误，这样局域网内的设备（其实就是手机微信浏览器）就可以访问了。

其实webpack还有跨域或代理的问题，后面等这方面都处理好了就写下来，不过这种东西配置完了就可以当作模板工程用了，也自然不会再重头开始，所以学习的机会没多少，每一次实践都要有所收获。
