---
title: Android 存储方式
date: 2016-01-18 22:55:20
tags: [Android,Storage]
---

## [迁移]

### 0x80 Android 主要存储方式

File，SharedPreference，SQLite，Content Provider(安卓四大组件之一)

### 0x81 File

File方式：与Java的文件存储方式一致，采用流的方式，Android封装了Api用于方便使用文件的流操作，通过openInput/OutputFile(filename)获取文件的输入输出流，再调用read/write方法读出写入流，完成后调用close方法关闭。

### 0x82 SP

SP方式：与文件操作类似，其实Api实质上就是完成了xml文件的读写，有点类似一个微型的xml数据库，读写速度较快，用于对一些需要快速读写的属性进行保存。获取到SP实例后，若要采用写入操作，需要获取一个Editor实例`editor=sp.edit()`，使用editor的put方法写入数据并commit()提交。若要读取，直接使用sp的get方法即可。默认采用覆盖的方式写入。

### 0x83 SQLite

SQLite方式：关系型数据库，使用SQLiteDatabase类以及`openOrCreateDatabase()`方法获取操作数据库的实例，`execSQL(sql)`可以进行DDL操作，也可使用提供的`query(),insert(),update(),delete()`方法进行数据库操作。也可单独继承SQLiteOpenHelper实现相应的方法。

### 0x84 ContentProvider

ContentProvider方式
