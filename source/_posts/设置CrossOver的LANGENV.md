---
title: 设置CrossOver的LANGENV
date: 2018-05-14 20:48:21
tags: [macOS, CrossOver]
---

### 0x81 CrossOver

CrossOver是CodeWeaver对Wine稳定版本的一种定制打包。Wine means Wine Is Not an Emulator，意为Wine本身不是一个模拟器，正如其所说的Wine确实不是一个模拟器或虚拟机，它是一个*nix兼容层，兼容Windows应用，其主要工作是将Windows系统调用转换为*nix系统调用，从而做到在非Windows平台上运行Windows软件。

Wine是开源且免费的，而CrossOver提供的软件服务不是，CrossOver对常见的Windows软件的运行环境做了预配置，以保证用户进行最少的配置便可以比较稳定的运行软件，但是由于它依赖核心组件Wine，因此通常来讲Wine上游存在的问题它都或多或少地存在。

### 0x82 macOS下中文运行环境配置

其实CrossOver的配置方法有很多，甚至于即使在英文环境下生成的Launcher都可以通过修改plist的方式修改它的运行环境，而今天要说的是通过修改它内置的wine启动脚本来做到这件事情。

方法很简单，首先找到CrossOver内置Wine的启动脚本位置`/Applications/CrossOver.app/Contents/SharedSupport/CrossOver/bin -> /Applications/CrossOver.app/Contents/SharedSupport/CrossOver/CrossOver-Hosted Application`，这个目录下有大量的启动脚本，打开wine的perl脚本：

``` Perl
# Start Wine
if ($log or CXLog::is_on())
{
    print STDERR "\n** ",scalar(localtime(time)),"\n";
    print STDERR "Starting '",join("' '",$cmd,@wine_args),"'\n";
    print STDERR "'",join("' '",@args),"'\n\n";
}
$ENV{LANG} = "zh_CN.UTF-8";
exec $cmd, @wine_args, @args
or cxerr("unable to start '$cmd': $!\n");
exit 1;
```

其中在试用exec执行外部命令之前，添加一句`$ENV{LANG} = "zh_CN.UTF-8";`便可以讲LANGENV设置为"zh_CN.UTF-8"，之后启动CrossOver并启动对应的Windows应用时，便会通过wine脚本正确的启动到对应的语言环境。不过这里我遇到一个问题，对于中文的显示没什么问题，但是导出到剪贴板的中文会全部乱码，应该还是字符集的问题，找个时间扒扒原因，ANSI下的Windows字符真的很不友好。

### 0x83 macOS自带的语言环境设置

曾几何时，操作系统便一致使用英文，作为一只合格的猴子，熟悉并接纳英文的开发环境我觉得还是有必要的。在使用macOS之后（其实很多Linux软件也是这样），在系统设置为英文时，软件所采用的默认语言也是英文并且通常不提供设置，而实际上很多macOS软件尤其是自带的软件在中英文环境下会存在功能上的差异，比如日历的农历仅在中文环境下显示（iOS最让我诟病的地方）。那么在macOS上有没有办法强制或者说欺骗软件使用我们指定的运行环境呢？起始官方提供了支持，那就是`-AppleLanguages`参数，用法很简单：

``` Bash
open -a CrossOver --args -AppleLanguages '(zh_CN)' # 其中zh_CN是在.app/Contents/Resources中可以找到的对应的资源
```

当然如果我们一直想通过Dock或LaunchPad启动，可以将脚本写到脚本编辑器然后导出Application就可以了，当然还有个办法，可以通过default（类似dbus）直接设定应用的配置，这里就不赘述了。
