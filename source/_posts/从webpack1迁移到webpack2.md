---
title: 从webpack1迁移到webpack2
date: 2017-06-22 09:39:22
tags: [node.js,webpack]
---

### 0x81 webpack 3.0.0的发布
webpack是一个模块化构建工具，它与一般的构建任务工具gulp不同，它不仅能完成构建任务，webpack还可以完成项目的模块化功能，从而更方便的处理项目中的依赖关系，并且在构建打包时可以根据依赖图高效的增量构建。JS的依赖管理其实有非常多种实现方式，如Node.js的CommonJS方式、AMD/CMD等等，但是这些依赖管理方式要么过于繁琐要么不支持异步，因此ECMAScript6规范了import语法来像python一样管理依赖。虽然官方规范很好，但是浏览器的支持程度是一步步推进的，在平常的开发当中我们仍然使用es5语法，但是webapck默认实现了import/export的依赖管理方式，如果项目要使用其他es6语法，则需要babel等转换工具的支持，而webpack有相应的loader来做无缝的转换。

webpack 3.0.0于几天前发布，其实它相对于webpack2的变化不是很大，更多的是内部的更新，对开发者而言broken api较少，不需要做webpack1向webpack2那样大的迁移变动。由于我从接触webpack开始就是1.x，并且用的比较简单也没遇到什么问题，所以年初发布了2.x之后很意外的没有立即向webpack2迁移（虽然官方给出了迁移文档），如今3.0.0已经发布，我觉得是时候开始用webpack3了，所以这篇文章就是根据官方的文档进行迁移。

### 0x82 迁移过程
1. resolve的变更
    在webpack1中我的输出配置是这么写的：
    ```JavaScript
    output: {
    -   path: 'dist'
    +   path: path.resolve(__dirname, 'dist')
    }
    ```
    意为webpack打包后输出到相对目录dist下，更新后提示path不能使用相对路径，需修改成绝对路径。根据官方文档的说法，`resolve.root, resolve.fallback, resolve.modulesDirectories`为`resolve.modules`取代：
    ```JavaScript
    resolve: {
    -   root: path.join(__dirname, "src")
    +   modules: [
    +       path.join(__dirname, "src"),
    }
    ```
2. loaders的变更
    loader是webpack如此强大的一个很重要原因，webpack本身只能依赖和打包js文件，但是有了各种各样的loader支持，我们将可以处理任何类型的文件。

    webpack2使用了新的rules系统，原本的module.loaders依然可用来保证兼容性，但是官方推荐使用新的命名约定。
    ```JavaScript
    - preLoaders: [
    -    {
    -       test: /\.js$/,
    -       loader: 'eslint',
    -       exclude: /node_modules/
    -    }
    - ],
    - loaders: [
    + rules: [
    +    {
    +       test: /\.js$/,
    +       loader: 'eslint',
    +       enforce: 'pre',
    +       exclude: /node_modules/
    +   },
        {
            test: /\.js$/,
    -       loader: 'babel',
    +       loader: 'babel-loader',
            exclude: /node_modules/
        }, 
        {
            test: /\.css$/,
    -       loader: 'style-loader!css-loader'
    +       use: ['style-loader', 'css-loader']
        },
        {
            test: /\.vue(\?[^?]+)?$/,
    -       loaders: []
    +       use: []
        },
        {
            test: /\.gif$/,
    -       loader: 'file',
    +       loader: 'file-loader',
            exclude: /node_modules/
        }
    ]
    ```
    loader系统的变更可以说的非常大的变更了，虽然webpack2保留了兼容语法，但是我们不知道什么时候会被移除。上面的例子中一些重要的变更基本上都有了，其中很重要的一点preLoaders和postLoaders被移除使用loaders属性替代，还有一点要注意就是loader不会再自动添加'-loader'后缀了，官方这么做是为了避免混淆。
    PS：json-loader已经默认存在于webpack当中，不再需要手动添加。

3. BannerPlugin的变更
    BannerPlugin不再支持双参数banner和options：
    ```JavaScript
    plugins: [
    -   new webpack.BannerPlugin('Banner', {raw: true, entryOnly: true});
    +   new webpack.BannerPlugin({banner: 'Banner', raw: true, entryOnly: true});
    ]
    ```
    
4. 其他变更
    主要是内置插件的变更以及对Promise异步的支持，更多的参阅官方文档。

迁移的过程就这些，比向AndroidGradlePlugin3的迁移容易得多。
