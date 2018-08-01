---
title: 在macOS Mojave上使用VirtualBox
date: 2018-08-01 21:11:10
tags: [macOS, VirtualBox]
---

### 0x81 Mojave黑名单

Mojave对于kext（内和扩展）的加载有黑名单机制，该黑名单记录在一个kext文件中`/System/Library/Extensions/AppleKextExcludeList.kext/Contents/Info.plist`中，在该plist文件中有一段记录了阻止系统加载的配置：

```plist
<key>OSKextExcludeList</key>
<dict>
    <key>org.virtualbox.kext.VBoxDrv</key>
    <string>LT 5.2.14</string>
</dict>
```

可以看出在最新版本的Mojave中，系统阻止了低于5.2.14版本的vboxdrv.kext的加载，而这段配置在第一个Mojave的beta版本中是`LT 5.3`，意味着VirtualBox低于5.3版本的都不能正常使用，而当时VirtualBox的最新版本是5.2.12。在DB发生问题后不久，Oracle放出了5.2.13/14的测试版解决了导致kernel panic的问题，但是系统仍然认为VirtualBox不兼容当前操作系统，所以我们需要做一些修改以让Mojave允许加载。

### 0x82 修改VirtualBox

修改的方式无非两种——修改系统Kext、修改VirtualBox。本着vanilla system的原则，我们不去修改系统文件，因为修改后在系统升级后可能会被覆盖，而软件我们则可以更灵活的控制。VBox的内核扩展有四个：

![四个扩展](/images/2018_08_01_01.png)

其中VboxDrv.kext是能否启动虚拟机实例的核心，我们通过sed命令修改4个kext的plist：

```Bash
sudo sed -i '' 's/5\.2/5\.3/g' '/Library/Application Support/VirtualBox/VBoxDrv.kext/Contents/Info.plist'
sudo sed -i '' 's/5\.2/5\.3/g' '/Library/Application Support/VirtualBox/VBoxNetAdp.kext/Contents/Info.plist'
sudo sed -i '' 's/5\.2/5\.3/g' '/Library/Application Support/VirtualBox/VBoxNetFit.kext/Contents/Info.plist'
sudo sed -i '' 's/5\.2/5\.3/g' '/Library/Application Support/VirtualBox/VBoxUSB.kext/Contents/Info.plist'

sudo kextload '/Library/Application Support/VirtualBox/VBoxDrv.kext'
sudo kextload -d '/Library/Application Support/VirtualBox/VBoxNetAdp.kext'
sudo kextload -d '/Library/Application Support/VirtualBox/VBoxNetFit.kext'
sudo kextload -d '/Library/Application Support/VirtualBox/VBoxUSB.kext'
```

命令很简单，我们把4个kext的版本从5.2替换为5.3，然后手动load这几个扩展文件，这样虚拟机实例就可以启动了。

如果你真的遭遇了VirtualBox不能使用这种情况，你还会发现LaunchPad里的VirtualBox有禁行符号，我们是无法启动VirtualBox Client的，原因很明显，macOS打算杜绝你的相关操作。如果你想启动，我们可以通过`/Applications/VirtualBox.app/Contents/MacOS/VirtualBox`直接启动VirtualBox的binary文件，他会跳过app文件的检查直接执行。当然还有一个一劳永逸的方法，像修改kext一样修改app的Info.plist:

```Bash
sudo sed -i '' 's/5\.2/5\.3/g' '/Applications/VirtualBox.app/Contents/Info.plist'
```

这样操作之后，你会发现原本的禁行图标不见了，我们可以像往常一样使用VirtualBox。当然，如文章最开始所显示的那样，新测试版本的Mojave已经将版本限制到5.2.14，也就是说你只要安装最新的VirtualBox（截至文时最新版本5.2.16），并且使用比较新的Mojave就不会有这个问题。
