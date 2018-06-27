---
title: 获取macOS的DeveloperBeta更新
date: 2018-01-31 18:32:34
tags: [macOS]
---

### 0x81 常规的获取更新方式

按道理来讲,我们如果想获得beta更新,只要注册AppleId为Developer账号就可以了,这样我们便可以获取beta更新工具从而配置我们的电脑为接收beta更新,即可下载并更新.但是Apple得开发者验证并不是随便就能申请的,他需要我们拥有可付费的应用或有接收支付的账户信息才能享受完整的开发者功能支持,而实际上成为一个真正的Apple开发者的年费也是不菲的.我作为一个称职的国内Android开发者,平时使用XCode也就是看看iOS应用源码或者是构建一些macOS应用及扩展.

受限于开发者账号的不完整性,我们没办法正常接收beta更新,那我们就没办法了么?其实原本的beta更新和Google释出的Android开发者镜像一样,是用于给开发者提前做应用适配的,后来为了让普通用户也能参与到测试当中来,Apple开启了PublicBeta众测计划,和Microsoft的Insider一样,每一个参与的用户都可以为系统的问题提交反馈.

我们从Apple开发者官网下载`macOSHighSierraPublicBetaAccessUtility`,运行工具同意协议输入密码后便可以加入PublicBeta,之后我们便可以在AppStore检查到beta更新.

### 0x82 AccessUtility实际做的事情

具有开发精神的我们肯定会思考,这个工具到底做了什么从而我们的macOS可以接受beta更新,并且一旦我们可以接收更新,在AppStore的偏好设置里便多出了不接收更新的选项,此时若我们取消接收,它实际上做了什么.

首先,安装过的pkg文件会释放bom文件到系统目录,Apple自家的通常在`/S/L/Receipts`目录下,第三方通常在`/var/db/receipts`
目录下,其中完整的安装历史在`/Library/Receipts/InstallHistory.plist`文件当中,其中记录了安装时间,pkg包名以及安装方式等.

对于bom文件,我们可以使用`lsbom`命令直接查看,当然我们也可以不在意bom文件的具体位置,直接使用`pkgutil --files ${PKG_NAME}`来查看pkg文件到底创建了那些文件(Payload中有那些文件),通过分析结果我们不难发现其实pkg文件只释放了一个lpdf文件:

```Bash
Library
Library/Documentation
Library/Documentation/Beta License.lpdf
```

lpdf是一种特殊的bundle,和app文件类似,里面存放了一系列签名配置信息,当然也有我们熟悉的`Library/Documentation/Beta License.lpdf/Contents/Resources/zh_CN.lproj/Beta License.pdf`普通pdf文件.

既然如此,那也就是说明pkg安装并不是释放了一个特殊的配置文件来实现接收beta更新这件事情,只有可能是安装脚本(postScript)了,关于pkg文件结构我们后面在分析.

### 0x83 seedutil 控制更新enroll

其实Apple提供了Seeding框架来干预macOS的更新,而pkg文件其实也就是执行了一段seedutil脚本,pkg的主要作用在于让用户阅读并同意软件协议而已,而偏好设置里的取消更新也是使用seedutil取消了更新的接收.

Seeding.framework 提供了seedutil工具来管理beta更新的接收,seedutil的位置:`/System/Library/PrivateFrameworks/Seeding.framework/Versions/A/Resources/seedutil`,Apple并没有开放链接在`/usr/bin`下并且该工具执行需要root权限,我们可以使用sudo来使用这个工具.

我们首先来看一下,在安装AccessUtility之后的seed状态是什么:

```Bash
⇒  sudo /System/Library/PrivateFrameworks/Seeding.framework/Versions/A/Resources/seedutil current
Password:
Currently enrolled in: PublicSeed

Program: 3
Build is seed: YES
CatalogURL: https://swscan.apple.com/content/catalogs/others/index-10.13seed-10.13-10.12-10.11-10.10-10.9-mountainlion-lion-snowleopard-leopard.merged-1.sucatalog.gz
NSShowFeedbackMenu: NO
DisableSeedOptOut: NO
```

`current`参数可以显示当前seed,我们可以看到`Currently enrolled in: PublicSeed`说明我们在PublicBeta上,然后使用`enroll`参数我们可以切换到DeveloperSeed:

```Bash
⇒  sudo /System/Library/PrivateFrameworks/Seeding.framework/Versions/A/Resources/seedutil enroll DeveloperSeed
Enrolling...

Program: 2
Build is seed: YES
CatalogURL: https://swscan.apple.com/content/catalogs/others/index-10.13seed-10.13-10.12-10.11-10.10-10.9-mountainlion-lion-snowleopard-leopard.merged-1.sucatalog.gz
NSShowFeedbackMenu: NO
DisableSeedOptOut: NO
```

这样,我们便可以接受DeveloperBeta更新,它往往比PublicBeta更早释出,当然,我之前虽然设置的是PublicBeta,但是安装的更新都是DeveloperBeta= =.

当然,seedutil还提供了`unenroll`参数供我们退出seed,他的作用和在AppStore的偏好设置选择取消是一样的.还有一个`fixup`参数可以用来修复角色,如果你接收不到beta更新的时候可以试试.

所以,如果你能下载到DeveloperBetaAccessUtility的话,你也可以直接pkg安装,他们的效果是一样的,但对于我这种登录开发者网站只能看到下载XCode的人(也有可能是我操作方式不对只能下载到PublicBeta,不过我原本一直不能登录iOS的Feedback应用,在我安装了DeveloperBeta后居然能登录了),用这种方式也能方便的接收更新,其实是手痒一定要试试macOS 10.13.4 的32位应用限制,也许只有苹果有推动力去做32位淘汰这件事情了,毕竟iOS算是一次成功的尝试.
