---
title: Android 无法扫描蓝牙设备踩坑
date: 2018-09-04 19:15:10
modified: 2018-09-24 20:31:04
tags: [Android, BLE]
---

### 0x81 从BluetoothLeScanner说起

早在几年前，Google开始在Android 4.3（Api 18）引入BLE支持的时候，使用的是一套支持不算完善的api，通过BluetoothAdapter的startLeScan方法，传入我们需要接受结果的Callback，这套api到目前为止（Api 28）都是可用的，虽然从Android 5.0开始引入了新的api并且将原来的api标记为废弃。通过观察源码我们可以发现原来的startLerScan已经转换成了BleScanner的api，通过改变原本的实现来保证我们的app仍然可以调用原来的方法并按预期工作。

Android 4.3甚至4.4对BLE的支持始终比较弱，默认不能作为外围设备不说，通信的稳定性也存在问题。对于4.3之前的设备，更有厂家自行实现Android平台的蓝牙协议栈以提供BLE支持，但是这就会导致最终app的适配十分麻烦且产生碎片化，就像指纹api一样，Android 5.0之后的api则提供了更全面的支持。

### 0x82 Android 8.1 Offscreen Pause Scanning

如果你使用BLE api搜索周围所有设备，你会发现即使你使用了新的api，在Android 8.1平台上，当你关闭屏幕LogCat会打印类似“pause scanning, need to be resumed”类似的消息，这是因为在Android 8.1这个例行小更新上，Google对BLE的行为再次做了限制，原本Android O上的后台行为限制继续保留，对于无限制无过滤条件（这里指的就是ScanFilter）的扫描，在你关闭屏幕的时候会立即停止。StackOverflow上也有不少人问及这个问题，解决办法就是根据文档对Scanner的描述，在扫描条件中添加ScanFilter，哪怕添加空的，都能规避这一问题。但是有一点要注意，这种行为属于后台行为，应该处理好使用的方式，后台长时间无休止无限制高功率的扫描本身也违背设计规范，并且也不再算是低功耗蓝牙。

### 0x83 Location Service对扫描结果的影响

这个是真的天坑，我们都知道从Android 6.0（Api 23）开始，Google为了加强用户隐私控制，开始推出动态权限机制，开发人员除了需要在清单文件中指明要使用的feature对应的权限以外，还需要在使用权限的地方动态的获取，用户明确授权之后才能使用。这本身是个很好的设计，但是对于蓝牙伴随而来的有一个改动，就是开启蓝牙某些功能需要定位权限（目前来看粗滤位置与精确位置都可以，对应CORSE_LOCATION和FINE_LOCATION），很多解释是因为新的Beacon设备开始提供地理围栏功能，通过从设备上发送的位置信息可以获取到用户的位置信息，而这种行为属于用户没有授权app获取位置信息，但是你却获取了，于是一刀切，使用蓝牙需要定位权限。

这一切看起来都算合理，哪怕我们不需要定位，只要和用户做好交互，告知用户我们使用蓝牙通信定位是必须的，都能使之按预期工作，但是往往事情没那么简单，看Google官方的[IssueTracker](https://issuetracker.google.com/issues/37065090)。

这个行为很有意思，你单单处理好权限是不行的，在文档之外的部分是，在某些设备上，你获取权限并开始了BLE扫描，但是没有任何结果，而这个理由很荒谬的是Location Service没有开启运行，Location Service和定位权限没有直接关系，它是手机用来获取GPS位置的一个服务。包括我的Nexus5测试机从版本MR58K开始就出现了这个问题，而解决办法只有两个：开启定位服务、使用传统蓝牙API扫描设备并使用BLE的api连接和操作设备，其中后者Github有一个库[SweetBlue](https://github.com/iDevicesInc/SweetBlue)做了这种兼容并写了专门的QA。

参考链接：

[https://github.com/iDevicesInc/SweetBlue/wiki/Android-BLE-Issues](https://github.com/iDevicesInc/SweetBlue/wiki/Android-BLE-Issues)[https://github.com/AltBeacon/android-beacon-library/issues/301](https://github.com/AltBeacon/android-beacon-library/issues/301)

这个问题到现在依然没有解决，因为它就是这样一种行为，无法从app层面去处理，想要最省事的方式在这种手机上拿到扫描结果，就是开启Location Service。
