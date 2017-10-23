---
title: 屏蔽Nougat的文件共享检测
date: 2017-05-06 10:53:43
tags: [FileProvider]
---

### 0x81 更改StrictMode检测

我之前做过Nougat上FileProvider的笔记，并且这种方式是存在很多坑的，其实网上也有很多办法关闭这种ExposedUri的检测，虽然这不是Google推荐的办法，但是在某些时候也能起到特殊的作用。这第一种办法就是修改严格模式的策略：

```Java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { 
    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build()); 
}
```

判断如果运行环境的API版本大于Android Nougat，就将虚拟机政策置空。

### 0x82 利用反射关闭检测

不知道Google既然想要强制处理文件共享，为什么还要允许关闭检测，也许是为了兼容性考虑，既然提供了判断那我们就可以依托Java强大的反射完成这件事情：

```Java
try {
    Method ddfu = StrictMode.class.getDeclaredMethod("disableDeathOnFileUriExposure");
    ddfu.invoke(null);
} catch (Exception e) {
    e.printStackTrace();
}
```

代码也很简单，利用反射取出StrictMode的disableDeathOnFileUriExposure方法，然后调用就可以了。

### 0x83 微信文件API传图

我们知道锤子OS3.0发布的时候由一个OneStep的重大特性，其中有一点就是摆脱微信分享的API限制，直接批量发送图片到微信，当然其实这也是微信的组件实现的，使用方法很简单：

```Java
Intent intent = new Intent();
intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI"));
intent.setAction("android.intent.action.SEND_MULTIPLE");

ArrayList<Uri> localArrayList = new ArrayList<>();
for (int i = 0, size = localPicsList.size(); i < size; i++) {
    localArrayList.add(Uri.parse("file:///" + localPicsList.get(i)));
}

intent.putParcelableArrayListExtra("android.intent.extra.STREAM", localArrayList);
intent.setType("image/*");
intent.putExtra("Kdescription", desc);
context.startActivity(intent);
```

方法很简单，打开微信的ShareToTimeLineUI，向发送的Intent中传递对应的数据即可，注意不能楼了Action
但是由于FileProvider的限制，File Uri共享已经不可用，需要重新寻找方法（比如转成Content Uri，但我还没有尝试）。
