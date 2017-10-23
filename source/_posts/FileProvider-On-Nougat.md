---
title: FileProvider On Nougat
date: 2017-02-06 19:41:10
tags: [FileProvider]
---

### 0x80 干什么的

Android 7.0+ 又事事了！秉承着互联网安全这一大雉，Google爸爸无时无刻不在为增强Android系统的安全性而努力。
API 24开始加强了文件系统的安全，应用间文件共享不再像之前那样干脆直接。
File URI开始被限于应用内使用，而若想在应用之间共享（比如调用系统相机拍照）则必须转换成Content URI。
FileProvider就是为了简化这个过程而出现的，他继承自ContentProvider，也就是加了权限并生成一个虚拟目录用于操作。

### 0x81 基本配置

```XML
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="${applicationId}.FileProvider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths"/>
</provider>
```

AndroidManifest.xml中配置如上，其中authorities通常是appid+FileProvider，选择授予Uri权限。

元数据部分规则固定，键为`android.support.FILE_PROVIDER_PATHS`，值为配置路径的xml文件。

### 0x82 路径配置

承接上例，配置文件是`file_paths.xml`

```XML
<paths>
    <external-path path="Pictures/" name="camera_photos" />
</paths>
```

external-path 配置了一个以"/storage/emulated/0/"为根目录的可配置路径，path为路径，而name是暴露uri时的虚拟路径名。

另外随着Support API更新，还有另外几种路径：

* root-path 对应文件系统根目录
* files-path 对应 `Context.getFilesDir()`
* cache-path 对应 `Context.getCacheDir()`
* external-files-path 对应 `Context.getExternalFilesDir(String) Context.getExternalFilesDir(null)`
* external-cache-path 对应 `Context.getExternalCacheDir()`

### 0x83 生成Content URI

使用getUriForFile方法生成虚拟文件路径URI。
`Uri contentUri = FileProvider.getUriForFile(getContext(), "${applicationId}.FileProvider", sharedFile);`

### 0x84 通过URI打开文件

openFile方法会返回URI对应的ParcelFileDescriptor，进行相应操作即可。
