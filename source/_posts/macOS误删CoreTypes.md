---
title: macOS误删CoreType
date: 2018-07-14 13:57:28
tags: [macOS]
---

### 0x81 事故背景——完整卸载Xcode

自High Sierra升级到Mojave后，Xcode也相对应的推出了beta版，一方面因为我不是依赖Xcode的开发者，另一方面Xcode占用的空间是真的大。

基于以上原因我决定彻底删除Xcode9，只使用Xcode10beta。

为了给予Xcode10一个相对干净的工作环境，我分别删除了`~/Library/Developer`、`/Library/Developer`以及`/Application/Xcode.app/`下的内容，其中第一个目录是用户使用Xcode产生的辅助文件，第二个目录主要是macOS SDK和CLTool所以我们只删除SDK，第三个是Xcode的本体，里面包含framework和toolchain。删除完后我们使用`sudo xcode-select -s %path%`设置我们要用的Xcode路径，此时运行Xcode10将会提示安装相应的组件，之后便可以正常使用。

### 0x82 bom文件的内容要仔细判断

正常来讲，经过上述操作，我们应该可以完成Xcode版本的切换，但是事与愿违，在清理Xcode9之前打开了Xcode10并安装了相关组件，我在删除完相关文件后后（其实主要是SDK等相关文件被删除了），导致了Xcode的闪退，猜测macOS记录了Xcode安装的组件，被我直接删除的文件Xcode是不知道的，而我之前执行了组件安装操作，所以Xcode也没有再次弹出安装组件的弹窗，最尴尬的是重新下载Xcode是没有用的。那解决这个问题的办法只有一个了，就是从头模拟一遍Xcode要正常运行需要进行的操作。

依然是那一个目的，为了给予Xcode10一个相对干净的工作环境，我决定去Receipts下找到经过pkginstaller安装的相关Xcode文件并删除，然后手动安装Xcode10里提供的pkg文件，bom文件里的内容非常多，但是主要集中在`/Library/Developer`和`/System/Library/PrivateFrameworks`下。

<!--more-->

### 0x83 悲剧的开始

bom文件位置：`/System/Library/Receipts`，我们主要关注`com.apple.pkg.MobileDevice.bom`、`com.apple.pkg.MobileDeviceDevelopment.bom`和`com.apple.pkg.XcodeSystemResources.bom`这三个文件，这个目录下是系统组件的信息，有一些其他的组件感兴趣的可以自己看看。

使用lsbom命令可以解析bom文件，内容显示pkg释放了哪些文件，我们要做的就是先删除这些文件。

![com.apple.pkg.MobileDevice.bom](/images/2018_07_14_01.png)

经过分析，我们看到MobileDevice主要向`/System/Library/CoreServices/CoreTypes.bundle/Contents/Library/`写入了`MobileDevices.bundle`，向`/System/Library/Extensions/`写入了`AppleMobileDevice.kext`、`AppleUSBEthernetHost.kext`，向`/System/Library/LaunchAgents/`写入了`com.apple.mobiledeviceupdater.plist`，向`/System/Library/LaunchDaemons/`写入了`com.apple.usbmuxd.plist`，向`/System/Library/PrivateFrameworks/`写入了`AirTrafficHost.framework`、`DeviceLink.framework`、`MobileDevice.framework`，这些文件主要是用于移动设备支持。悲剧就发上在第一个目录上，CoreServices是系统的基础服务，像是Finder.app就在这个位置，而CoreTypes.bundle内有基础类型的定义，我因为失误不小心删除了整个CoreTypes.bundle，随之而来的就是Finder等系统内置App的崩溃，重启后不显示Dock等问题，完全没办法使用，最后我通过另一台10.13.3设备将CoreTypes打了个tar包，解压到了`/System/Library/CoreServices/`才得以解决这个问题，虽然系统是Mojave，但是并没有遇到什么大的问题，后面重新下载了PublicBeta全量更新了整个系统，这幕悲剧则彻底结束。

其实对于`com.apple.pkg.MobileDevice.bom`和`com.apple.pkg.MobileDeviceDevelopment.bom`这两部分，他们的内容主要在/S*/L*，我感觉不应该去，甚至我连它们到底是不是Xcode的组件都不清楚，虽然在Xcode中能找到他们的pkg文件，否则也不会出现这次悲剧。我们看一下XcodeSystemResources：

![com.apple.pkg.XcodeSystemResources.bom](/images/2018_07_14_02.png)

很明显，内容都在`/Library/Developer`下，可以放心大胆地删除。

### 0x84 手动安装组件

剩下的工作就很简单了，打开Xcode的资源目录：`/Applications/Xcode.app/Contents/Resources/Packages`：

![Packages](/images/2018_07_14_03.png)

我们能看到三个pkg文件，这就是我们刚刚分析三个bom文件对应的安装包，依次安装它们，之后Xcode应该就能正常工作了。
