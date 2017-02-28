---
title: FileProvider 踩坑
date: 2017-02-28 17:51:46
tags: [FileProvider]
---

### 0x81 问题
之前简单介绍过FileProvider的使用，由于Android Nougat的出现，我们不得不为7.x带来的安全文件访问策略作出兼容。
在低于Nougat的设备上，我们仍然使用File Uri，而在Nougat及更新平台的设备上，我们需要使用Content Uri共享文件来避免触发UriExposed异常。
问题出现在图片裁剪的文件导出上，原本需求通过`return-data`返回Bitmap并显示即可，现在需要对该图片按照一定的比例裁剪并上传，考虑放弃手动保存Bitmap的方式，让图库裁剪完直接导出文件即可。
但是导出的Uri使用的是FileProvider，从而导致了图库等三方应用的崩溃。

### 0x82 是怎样的异常
这个异常很多人应该都见过，也不是什么罕见的异常，就是跨进程组件调用的SecurityException，描述类似如下这样：
```Java
java.lang.SecurityException: Permission Denial: opening provider android.support.v4.content.FileProvider from 
ProcessRecord{52a99eb0 3493:com.android.gallery3d/u0a57} 
(pid=3493, uid=10057) that is not exported from uid 10071
```
ErrorTrace说得很明白，`com.android.gallery3d`尝试开启FileProvider但是权限被拒绝从而触发了安全异常。

### 0x83 问题分析
没错，我们的APP在清单文件中声明的FileProvider的exported属性是false，这意味着其他应用确实不能直接访问这个组件。
```XML
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="${applicationId}.FileProvider"
    android:grantUriPermissions="true"
    android:exported="false">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths"/>
</provider>
```
那我们的FileProvider生成的Uri是如何给第三方应用使用的呢，就是那个`grantUriPermissions="true"`，它表明我们在使用Provider时会临时授予权限。
第三方应用通过临时权限访问FileProvider，这一切看起来确实是非常的美好，但是当我把Uri告诉Intent并发起裁剪，当裁剪完成返回APP时，图库挂了，自然而然这次裁剪就失败了。

为什么拍照的时候可以用，而裁剪就不行了。通过阅读FileProvider的源码，我们发现这些校验都是写在FileProvider#attachInfo内部的，有人说可以直接拷贝一份修改源码，但我觉得这不是正确的处理方式。
而真正有效的解决方式居然是不是用FileProvider产生的Uri，而是使用普通的File Uri，而这样居然也不会像拍照那样出发ExposedException，原因我目前仍然没有找到。

### 0x84 各种解决办法
1. 再授权法
    通过query出所有的Activity并获取他们的包名，挨着授予Uri权限。
    ```Java
    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    for (ResolveInfo resolveInfo : resInfoList) {
        String packageName = resolveInfo.activityInfo.packageName;
        context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }
    ```
    依然是看起来很美好，权限也有了，但是并没有什么卵用。因为这个异常没有提到你需要grantPermission（个别的状况好像真的有这种现象，我不知道有没有用），系统只是告诉你组件没有exported。

2. Intent Flag
    这种方法就是使用`Intent#addFlags`或`Intent#setFlag`方法为响应这次Intent的组件添加临时Uri权限，感觉和`grantUriPermissions="true"`的作用是一样的，仍然没有效果。

3. 使用File Uri    
    本来我以为这是只在低于24的设备上，后来我才发现指的是在Nougat设备上也不使用FileProvider的Uri，并且这种方法真的有效。

### 0x85 FileProvider#getUriForFile方法
我们就是通过这个方法来获取Content Uri的，从源码里也能看到，Provider获得了PathStrategy后拼接成了Uri返回。
这个方法的注释说的很明确，该方法返回了一个Content Uri，这个Uri可以用于`Context#grantUriPermission(String, Uri, int)`方法授予Uri权限，亦或者是通过`Intent#setData(Uri)`方法，
把Uri添加到Intent并通过setFlag方法设置权限。而实际上我在使用它拍照保存时，都没有使用，就是生成一个uri直接传给Intent，保存图片成功。
所以我猜测这个和XML里的定义可能是等效的，但是attachInfo只校验了清单中的设置，可能是因为Provider注册时先检查符不符合条件。

PS：也有人提出，不显式授予Uri权限可能出现“图片不能小于50x50”这种提醒，我还没遇到过。

