---
title: webpack3集成vux的坑
date: 2017-06-27 19:24:43
tags: [webpack,vux]
---

### 0x81 使用Vux
最近一段时间又忙的要死，每每年中就是重灾期，感觉自己忙的手忙脚乱。值得庆幸的是在忙碌之中自己仍旧在坚持在学习道路上，由于公司起了一个比较大的合作项目，并打算投入使用Vue作为javascript框架并最终选用了Vux作为组件库简化开发，由于刚从webpack1转到webpack3，我这两天在手动集成vux上废了不少时间。

官方关于如何使用vux提供了两种方案——初始化模板和手动集成。
1. 模板工程
    这种比较适合以Vux新起的项目，它的内部使用webpack2作为模板，已经处理好了Vux的配置，可以直接开始使用。

2. 手动集成
    这种适合需要使用自己webpack配置的工程，通过按照文档的描述完成配置来引入Vux组件库。

### 0x82 rules和loaders对vux-loader的影响
webpack从1到2很大的一个变化就是对于加载器的支持方式，webpack1中的loaders被更换为更加灵活的rules，而vux-loader就以module中是否存在rules来判断开发者使用的webpack版本：
```JavaScript
// check webpack version by module.loaders
let isWebpack2

if (typeof vuxConfig.options.isWebpack2 !== 'undefined') {
    isWebpack2 = vuxConfig.options.isWebpack2
} else if (oldConfig.module && oldConfig.module.rules) {
    isWebpack2 = true
} else if (oldConfig.module && oldConfig.module.loaders) {
    isWebpack2 = false
}
```
当然这只是判断条件之一，后面还根据package.json配置文件进一步判断。

上面这种判断方式导致会出现一个问题，就是webpack2其实为了兼容性仍旧保留了loaders语法，如果不是使用webpack2推荐的方式进行配置，就可能导致vux-loader认为开发者使用的是webpack1从而配置失败。

### 0x83 use和loader对options/query的影响
之前我们提到，webpack1当中的loader配置是确定性的，因此如果想要链式loader就需要我们使用`loader1!loader2`式的语法进行输出传递，而在webpack2当中换用了`use:[]`来配置多个loader的状况。webpack2的改变导致了一种结果，原本的options/query配置不能在rules层级配置，否则会导致`Error: options/query provided without loader. `错误，正确的处理方式是将选项配置在loader上，即：
```JavaScript
module: {
    rules: [
        {
            test: /\.vue(\?[^?]+)?$/,
            loader: 'vue-loader',
            options: vueLoaderConfig
        },
        {
            test: /\.vue(\?[^?]+)?$/,
            use: ['vue-loader'],
            options: vueLoaderConfig
        },
        {
            test: /\.vue(\?[^?]+)?$/,
            use: [{
                loader: 'vue-loader',
                options: vueLoaderConfig
                //query: {
                //    inactive: true
                //}
            }]
        }
    ]
}
```
然而在配置Vux的时候，上面那种方式是没有问题的，和使用脚手架产生的模板代码一致，也就是对于.vue文件我们使用带选项的vue-loader加载文件，而第二种方式则会触发上面提到的错误。对于第三种方式，看起来配置没有什么问题，甚至在我看来这是更合理的配置方式，但是会导致vux-loader进行merge操作之后无法渲染内容的问题，具体的问题原因我还没有查到，而不使用Vux的话不影响Vue工程的渲染，其根源应该还是在vux-loader源码当中。

不得不说前端的东西越来越复杂了，这还只是集成我就遇到了这么多问题，当然也学习到了很多东西（比如我终于静下心来看了一下webpack2模板构建配置的代码），在使用的过程中我相信会遇到更多的问题，我还是希望能有一个互相探讨的人齐头并进啊，唉～
