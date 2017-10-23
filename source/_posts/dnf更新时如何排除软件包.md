---
title: dnf更新时如何排除软件包
date: 2017-05-05 18:09:40
tags: [dnf]
---

### 0x81 为什么要这么做

最早在使用ubuntu的时候，我使用hostapd来开启AP共享，但是较新的软件包有一个bug，会导致AP一直卡在启动中的状态，如果手动将软件包降级不做任何处理的话，每次升级软件包都会连同它一起升级，因此就需要将它在dpkg重标记为hold，这样检测更新就会跳过这种软件包。

由于我升级了Fedora 26的branch版本，每天的更新非常多，尤其是kernel，几乎就是跟着[内核站](https://kernel.org/)同步更新，每天都会经历着rc1到rc8更新的折磨，恰巧昨天更新了update-testing源里的4.11.0正式版内核，我就想着把kernel加入忽略更新的列表。

### 0x82 如何忽略

方法很简单，使用dnf命令更新的时候提供了一个叫做exclude的参数可以用于排除软件包的更新，因此只要使用一下这个命令就可以了：

```Shell
sudo dnf makecache
sudo dnf upgrade --exclude=kernel*
```

其中`kernel*`使用通配符过滤所有以kernel开头的软件包，到然如果每次都这样肯定会非常麻烦，dnf提供了配置文件来让我们方便的配置这个参数：

```Shell
fionera@fionera:~|⇒  cat /etc/dnf/dnf.conf
[main]
gpgcheck=1
installonly_limit=2
clean_requirements_on_remove=True
exclude=kernel*

fastestmirror=true
deltarpm=true
fionera@fionera:~|⇒
```

这样exclude后面的软件包就会自动被忽略。
