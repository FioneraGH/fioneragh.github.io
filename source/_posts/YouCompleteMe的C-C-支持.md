---
title: YouCompleteMe的C/C++支持
date: 2017-04-06 18:20:44
tags: [Vim,YCM,Clang]
---

### 0x81 为YCM添加C系语言的支持

我曾经记录过一篇使用YouCompleteMe这一Vim插件补全Python脚本的方法，现在需要对C/C++进行支持，根据官方的解释，我们只需要在原来的install.py脚本后添加参数`--clang-compiler`即可以在ycm_core中添加C补全支持。YCM的C补全依托的是Clang编译器，通过编写时实时编译查询你可能需要的补全关键词，因此我们需要添加Clang的支持。

### 0x82 编译带C-Compiler的YCM

执行命令`python3 install.py --clang-compiler`便可以开始插件的编译，最新的编译脚本已经对Clang做了支持，如果你没有指定Clang环境，脚本会自动从llvm官网下载最新的二进制包。

在这里遇到了一个问题，就是脚本下载的二进制包速度非常非常慢，可能受限于天朝网络的影响，我不得不手动下载该包并放到对应的目录以便YCM直接开始编译。文件路径如下：

![归档位置](/images/2017_04_06_01.png)

### 0x83 使用配置文件

安装完插件后，补全其实还不能正确使用，还需要一个flags配置文件指定编译需要的头文件路径，我们可以使用clang工具轻松的拿到路径，键入命令`echo | clang -v -E -x c++ -`：

![搜索路径](/images/2017_04_06_02.png)

之后根据模板文件添加刚刚查到的搜索路径：

```Python
flags = [
'-Wall',
'-Wextra',
'-Werror',
'-Wno-long-long',
'-Wno-variadic-macros',
'-fexceptions',
'-DNDEBUG',
'-std=c++11',
'-isystem',
'/usr/include',
'-isystem',
'/usr/local/include',
'-isystem',
'/usr/include/c++/7',
'-isystem',
'/usr/include/c++/7/x86_64-redhat-linux',
'-isystem',
'/usr/include/c++/7/backward',
'-I',
'.'
]
```

我们把文件命名为.ycm_extra_conf.py并保存到工程目录下，这个时候补全依然是不能用的，我们还需要配置。

### 0x84 指定配置文件

YCM可以指定全局的配置文件，只需要在.vimrc文件中添加：

```vimrc
let g:ycm_global_ycm_extra_conf = '~/.vim/bundle/.ycm_extra_conf.py'
```

其中.ycm_extra_conf.py是一个全局路径配置。当然我们也可以为每一个工程配置一个文件，只需要在.vimrc中添加：

```vimrc
let g:ycm_confirm_extra_conf=0
```

这样当你使用vim编辑cpp文件时，会自动搜寻并加载工程目录下的配置文件。

### 0x85 生成配置文件

借助Ycm-Generator并编写模板文件，把我们的搜索路径添加进去，便可以使用YG根据build-system生成所需的YCM补全配置文件，使用方式很简单，这里就不赘述了。

好了，这样vim就可以稍微愉快的编写cxx文件了～
