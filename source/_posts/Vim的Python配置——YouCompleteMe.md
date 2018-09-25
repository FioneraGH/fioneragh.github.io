---
title: Vim的Python配置——YouCompleteMe
date: 2017-03-06 17:54:35
tags: [Vim,YCM]
---

### 0x81 YouCompleteMe

[YouCompleteMe: a code-completion engine for Vim](https://github.com/Valloric/YouCompleteMe)

正如它的README所说的，YCM是一个用于Vim自动补全的插件。它的功能十分强大，支持非常多的编程语言补全。

* Clang 为C/C++/OC/OC++提供补全支持
* Jedi 为Python2/Python3提供补全支持
* OmniSharp 为C#提供补全支持
* Gocode、Godef 为Golang提供支持
* TSServer、Tern 分别为TypeScript和JavaScript提供支持
* Racer 支持Rust
* 其他补全支持

### 0x82 ycm_core的编译使用

YCM提供了基于python的编译脚本，一般情况下，安装好依赖直接执行install.py脚本文件就可以了，用户还可以出入不同的参数以提供不同的补全支持。

对于我的DST-Fedora 25，需要先使用dnf包管理工具安装基本的构建工具`sudo dnf install automake gcc gcc-c++ kernel-devel cmake`，还需要安装python的头文件`sudo dnf install python-devel python3-devel`，处理完这些依赖便可以进行编译工作了。

PS：根据官方README的描述，如果YCM更新了，要使用新的特性，需要重新编译。

<!--more-->

### 0x83 Vundle

[Vundle, the plug-in manager for Vim](https://github.com/VundleVim/Vundle.vim)

Vim的插件管理器，简单易用扩展性好，使用YCM我们需要使用该工具加载YCM插件，以获得良好的补全支持。

.vimrc文件中关于Python支持的配置如下：

```vim
set nocompatible              " be iMproved, required
filetype off                  " required

" set the runtime path to include Vundle and initialize
set shell=/bin/bash
set rtp+=~/.vim/bundle/Vundle.vim
call vundle#begin()
" alternatively, pass a path where Vundle should install plugins
"call vundle#begin('~/some/path/here')

" let Vundle manage Vundle, required
Plugin 'VundleVim/Vundle.vim'

" All of your Plugins must be added before the following line
Plugin 'tmhedberg/SimpylFold'
Plugin 'scrooloose/syntastic'
Plugin 'scrooloose/nerdtree'
Plugin 'kien/ctrlp.vim'
Plugin 'vim-scripts/indentpython.vim'
Plugin 'Valloric/YouCompleteMe'

call vundle#end()            " required
filetype plugin indent on    " required
" To ignore plugin indent changes, instead use:
"filetype plugin on
"
" Brief help
" :PluginList       - lists configured plugins
" :PluginInstall    - installs plugins; append `!` to update or just :PluginUpdate
" :PluginSearch foo - searches for foo; append `!` to refresh local cache
" :PluginClean      - confirms removal of unused plugins; append `!` to auto-approve removal
"
" see :h vundle for more details or wiki for FAQ
" Put your non-Plugin stuff after this line
```

其中的Plugin决定了Vundle需要加载的插件，在启动Vim时使用`:PluginInstall`或`:PluginUpdate`命令对插件进行同步安装和更新。各插件都有自己的配置选项，可以根据自己的需求进行配置。

### 0x84 补全的使用及可能存在的问题

插件编译完成并配置完毕后，打开.py文件应该就能感受YCM到提供的即时补全弹窗了，当然，我们也有可能遇到问题。

由于YCM的Python补全是基于Jedi的，而Jedi的具体使用是[ycmd-server:A code-completion & code-comprehension server](https://github.com/Valloric/ycmd)启动了[JediHTTP](https://github.com/vheon/JediHTTP)。这种补全具体的模式我的水平不够还看不懂，但这种模式无疑是极大的增加了扩展性，而我之前遇到的补全无效的问题也是由于JediHTTP服务启动失败，虽然我在`:YcmDebugInfo`中看到它是正常启动的，但是在/tmp下的log文件中显示该服务一直链接失败，解决了这个问题就能使用补全了。

> YCM有很多SubModule，个别找不到依赖的错误可能是子模块没有检出，使用`git submodule update --init --recursive`可能解决问题。
