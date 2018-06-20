---
title: KabyLake DVMT Fix
date: 2018-06-20 18:19:59
tags: [macOS, Clover, Hackintosh]
---

### 0x81 Clover

Hackintosh，这应该是这个系列的第二篇，第一篇参考[利用Hotpatch禁用DGPU](https://fioneragh.github.io/2018/03/24/%E5%88%A9%E7%94%A8Hotpatch%E7%A6%81%E7%94%A8DGPU/)主要记录了如何利用Clover禁用部分笔记本未提供BIOS选项的独显。

Clover，一个强大的引导工具，基于rEFInd魔改而来，提供强大的驱动注入和二进制patch的功能，[@RehabMan](https://github.com/RehabMan)等大牛的参与更是为Clover带来更加强大的诸如AutoMerge等功能，让Clover可以为黑苹果工作的更好。

### 0x82 DVMT

DVMT，全称Dynamic video memory technology，意为动态显存技术，这个显存还不是传统意义上我们在使用显卡的显存，它更像一种预申请用于做缓冲区初始化的存储空间。对于大部分PC厂商来说，BIOS通常会提供一个用于设置大小的选项，而对于很多笔记本来说，BIOS本身提供的选项设置就很少，更别说提供DVMT的设置。

在比较新的硬件平台上(Broadwell+)，比如我现在使用的KabyLake，也就是7代英特尔CPU，在安装macOS时如果不进行相应的patch，就会导致对应的Framerbuffer程序crash从而导致kernel panic，这是个很致命的问题，因为这个过程发生在你刚引导家在进入系统的时候。究其原因，是因为macOS对于新硬件平台申请了至少64m的prealloc空间，而大部分笔记本设备厂商出厂都设置在32m，当系统想要申请比其大的空间时必然失败，就跟我们平时编程遇到的allocation memory failed是类似的情况，知道问题就知道对应的解决方案了。

### 0x83 minStolenSize patch

macOS的Framebuffer中有一个非常magic的值——minStolenSize，论坛上叫这个名字我也没有去细查，这个值只要从76修改到EB即能规避DVMT不足的问题，但是貌似他只是修改了检查，而实际上binary向系统申请的值仍然是原值，只是申请这么大空间不一定用得到这么大空间，所以反正能用。

以10.14 Beta为例，因为这个特征值的确定在10.14发生了修改，我们先找到binary里对应的特征值位置：

![minStolenSize位置](/images/2018_06_20_01.png)

为了避免不同版本因为某些状况刚好会修改其他的值，我们通常会多添加几个特征字节用来确保这个patch仅在我们已知的情况下起作用，当然用Clover的patch filter也可以做到，但是如果要修改的这组值在ninary理由多个备选，我们就得找到真正影响minStolenSize的地方。介于上述原因，KabyLake平台下的Clover-KextsToPatch中关于minStolenSize的patch通常这样写：

```Clover
Comment:
minStolenPatch (10.14 Beta1)

Name:
AppleIntelKBLGraphicsFramebuffer

Find:
76 46 48 FF 05 BA 48 08

Repl:
EB 46 48 FF 05 BA 48 08

Comment:
minStolenPatch (10.14 Beta2)

Name:
AppleIntelKBLGraphicsFramebuffer

Find:
76 46 48 FF 05 02 52 08

Repl:
EB 46 48 FF 05 02 52 08
```

### 0x84 32mb DVMT-prealloc patch

minStolenPatch着实是一个不错的方案，我们只要注意找到正确的minStolenSize值并修改，通常就可以正常使用了。不过根据[RehabMan](https://github.com/RehabMan)的说法，这种做法他认为可能会带来一些其他的问题，最好的解决方式还是将macOS认为需要申请64m空间的设置调整到32m，从而保证申请申请与使用的一致性，毕竟我们的笔记本可能真的只有32m的dvmt可分配空间。

与minStolenPatch的解决思路一致，我们先要找到对应ig-platform-id下的配置信息的二进制位置，然后进行相应的patch。在我们仅仅是安装macOS时，因为不需要硬件加速我们完全可以设置一个根本不存在的platform-id来进入安装界面，此时整个界面都是没有显卡加速的，所以也没有流畅的动画，但是此时我们不需要任何patch就能进入系统，原因也很简单就是因为AppleIntelKBLGraphicsFramebuffer(KabyLake)这个binary所在的kextbundle根本没有加载，自然也不会出现申请内存不足的状况，所以我们还是从AppleIntelKBLGraphicsFramebuffer下手，寻找合适的patch。以10.14 Beta为例，我使用的KabyLake平台的platform-id是0x56160000，搜索结果：

![platform-id位置](/images/2018_06_20_02.png)
![platform-id位置](/images/2018_06_20_03.png)

之所以这样修改是因为我已经没办法通过High Sierra的`01030303 00002002`找到原来的值了，很有可能已经完全变了。根据High Sierra的经验，10.13的KabyLakeFramebuffer的patch是：

```Clover
Comment:
HD620, 32MB BIOS, 19MB framebuffer 9MB cursor bytes 2048MB vram

Name:
AppleIntelKBLGraphicsFramebuffer

Find:
01030303 00002002 00000000 00000060

Repl:
01030303 00003001 00009000 00000080
```

内容比较多，但主要分为四块分别是pipe count、framebuffer size、cursor bytes和dmvm，其中第一个通常用来做特征值，第二个是主要的framebuffer大小，第三个cursor的大小，他太小的话通常会导致画面抖动，0x00000000指的是自动，第四个是系统内动态显示内存的最大值，也就是macOS关于信息里的显存大小，加大它可以减少一些花屏幕但作用不是很明显。

根据之前的信息推测，第二张里是我需要patch的信息，因为我们能在platform-id后找到对应的0x00002002， 指framebuffer的大小为34m，因此我们通过以下patch完成我们需求：

```Clover
Comment:
HD620, 32MB BIOS, 19MB framebuffer 2048MB vram

Name:
AppleIntelKBLGraphicsFramebuffer

Find:
200248BA 00000060

Repl:
300148BA 00000080
```

### 0x85 IntelGraphicsDVMTFixup (required Lilu)

Clover的patch有一个最致命的地方，想要正确的patch必须已经通过kextcache建立了内核缓存，kextcache执行时将所有已经加载的kext都加到了缓存当中，这样才能被Clover正确的进行二进制替换，具体的原理我没有看Clover的源码，大体意思就是Clover要hook这一过程kext被内核缓存是必须的。因为这些局限，[vit9696](https://github.com/vit9696)写了一个内核扩展，它叫Lilu，通过较早的加载Lilu.kext并加载其他有hook功能的插件kext，这样当系统加载对应kext时可以直接进行patch，这样就可以做到无痛打patch。

IntelGraphicsDVMTFixup实际上做的事情和上面是一致的，最大的特点就是借助了Lilu的特性。如果你有一定的编程能力，我建议使用这个repo，当macOS新版本发布时自行修改fork的repo编译一个kext来使用，虽然sherlock已经更新了IntelGraphicsDVMTFixup支持了10.14，但我发现他只是修改了Lilu hook的版本匹配以不需要使用`-lilubeta*`参数来加载它，patch的内容并没有改，可行性我没有测试，不过鉴于很多人都用它应该没什么问题，感兴趣的人可以试试。

DVMT相关的内容差不多就这些，有其他新的内容我再补充。
