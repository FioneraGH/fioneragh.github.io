---
title: 基于Vue的Weex工程构建
date: 2017-03-27 18:22:23
tags: [Weex,Vue]
---

### 0x81 工程初始化

早期我们通过编写Weex特有的.we文件来实现我们的需求，we文件的语法和vue.js的语法非常类似，都是`<template></template>、<style></style>、<script></script>`三大标签构成模板、样式和逻辑。
现如今，我们只需要使用最新的weex-toolkit即可以创建出基于webpack模块构建系统的工程，更让人欢喜的是，Weex在最新的SDK中已经引入了Vue2 Runtime，我们可以使用vue.js来开发我们的Weex应用。

使用`weex init project`命令创建的工程目录结构如下：

![工程结构](/images/2017_03_27_01.png)

其中.vscode是Visual Studio Code生成的配置文件夹，src是我们的源码目录，webpack.config.js是webpack（1.14.0）的配置文件，package.json是项目配置信息文件。

### 0x82 安装必要的包依赖

使用nodejs安装包依赖很简单，只需要在工程目录下使用npm工具执行`cnpm i`命令就可以了（cnpm是淘宝源的npm工具，由于npmjs在国外，由于国内特殊的状况速度很慢）。

我们看一下package.json:

```Json
{
  "name": "weex",
  "description": "A weex project.",
  "version": "0.1.0",
  "private": true,
  "main": "index.js",
  "keywords": [
    "weex",
    "vue"
  ],
  "scripts": {
    "build": "webpack",
    "dev": "webpack --watch",
    "serve": "node build/init.js && serve -p 8080",
    "debug": "weex-devtool"
  },
  "dependencies": {
    "axios": "^0.15.3",
    "uuid": "^3.0.1",
    "vue": "^2.1.8",
    "vue-awesome-swiper": "^2.3.8",
    "vue-router": "^2.1.1",
    "vuex": "^2.1.1",
    "vuex-router-sync": "^4.0.1",
    "weex-vue-render": "^0.1.4"
  },
  "devDependencies": {
    "babel-core": "^6.20.0",
    "babel-loader": "^6.2.9",
    "babel-preset-es2015": "^6.18.0",
    "css-loader": "^0.26.1",
    "ip": "^1.1.4",
    "serve": "^1.4.0",
    "vue-loader": "^10.0.2",
    "vue-template-compiler": "^2.1.8",
    "webpack": "^1.14.0",
    "weex-devtool": "^0.2.64",
    "weex-loader": "^0.4.1",
    "weex-vue-loader": "^0.2.5"
  }
}
```

和一般的包依赖配置一样，"script"下是npm命令脚本别名，"dependencies"和"devDependencies"分别是一般依赖和开发依赖。一般依赖主要是Vue和Weex，开发依赖主要是Babel翻译器和Webpack的Loader。

<!--more-->

### 0x83 Gif加载图标的支持

我们有时候会使用一张旋转的gif作为加载内容的占位图，但Webpack可能会不识别Gif文件，这个时候我们需要添加"file-loader"到工程依赖，并在webpack.config.js中配置：

```JavaScript
loaders: [
  {
    test: /\.gif$/,
    loader: 'file',
    exclude: /node_modules/
  }
]
```

### 0x84 Webpack的EsLint支持

根据weex-toolkit生成的webpack.config.js配置文件中的描述，如今weex开发得益于webpack丰富的loader已经可以使用eslint做代码检查，如果有需要已经可以使用样式处理器处理postcss这些样式库了。EsLint的支持也特别容易，在工程目录下运行`cnpm i babel-eslint eslint eslint-config-standard eslint-loader eslint-plugin-html eslint-plugin-promise eslint-plugin-standard --save-dev`后，eslint的基本依赖便会添加到工程目录下。接下来在webpack.config.js的preLoaders下添加配置：

```JavaScript
preLoaders: [
  {
    test: /\.vue$/,
    loader: 'eslint',
    exclude: /node_modules/
  },
  {
    test: /\.js$/,
    loader: 'eslint',
    exclude: /node_modules/
  }
]
```

这样webpack在构建工程时便会先通过eslint检查代码，如果发现错误会给出提醒并中止编译。

### 0x85 VS Code的EsLint

其实在安装EsLint的相关依赖后，VS Code便具有了代码检查的能力，根据自己的需要配置好eslint的配置文件`.eslintrc/.eslintrc.json`，EsLint Server便会在代码编写时就能提供代码警示：

```Json
.vscode/.settings.json

"emmet.syntaxProfiles": {
  "vue-html": "html",
  "vue": "html"
},
"eslint.validate": [
  "javascript",
  "javascriptreact",
  "html",
  "vue"
],
"eslint.options": {
  "plugins": [
    "html"
  ]
}

.eslintrc

{
  "root": true,
  "parser": "babel-eslint",
  "parserOptions": {
    "sourceType": "module"
  },
  "globals": {
    "Vue": false
  },
  "extends": "standard",
  "plugins": [
    "html"
  ],
  "rules": {
    "arrow-parens": "off",
    "generator-star-spacing": "off",
    "semi": "error",
    "no-extra-semi": "error",
    "space-before-function-paren": "off"
  }
}
```

### 0x86 使用stage-2支持Object Spread Operator

根据官方的说法，rest spread等对象展开操作并不在es2015的标准中，因此babel默认并不支持对象展开，我们可以使用babel-stage-2开启支持。

使用npm安装`cnpm i babel-preset-stage-2 --save-dev`安装stage-2 preset后配置.bablerc后便可以使用对象展开符了。

```Json
{
  "presets": [
    "es2015",
    "stage-2"
  ]
}
```

以上是目前学习新的weex和从头学习vue所遇到的几个简单的小问题，以后会再补充。
