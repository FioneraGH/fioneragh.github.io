---
title: Realm：在Android中使用Realm
date: 2017-05-10 19:01:56
tags: [Android,Realm]
---

### 0x81 Realm是什么
Realm是一个移动数据库，它可运行于手机、平板等移动设备，它的目的是取代SQLite。正是因为Realm并不是SQLite的封装，所以它不同于GreenDao、Suger这些ORM框架。Realm为Android平台提供支持，使用Realm Java Api管理数据库。

### 0x82 安装Realm支持
根据官方文档，Realm为Gradle依赖构建提供完美的支持，我们只需要在Project级的build.gradle配置文件中配置Realm插件：
```Groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.4.0-alpha7'
        classpath 'io.realm:realm-gradle-plugin:3.1.4'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
```
然后在需要的Module的build.gradle文件中应用插件即可使用Realm Java Api：
```Groovy
apply plugin: 'com.android.application'
apply plugin: 'realm-android'
```

### 0x83 简单使用
Realm的数据表默认对应一个POJO类，和GreenDao这些ORM框架的`@Table`注解类似，一个POJO类只要继承RealObject类即可在数据库中生成与之对应的表，各个成员变量作为表的列。
```Java
public class GankItem
        extends RealmObject {
    @PrimaryKey
    @SerializedName("_id")
    private String id;
    private String type;
    private String desc;
    private String who;
    private String url;
    @Ignore
    private List<String> images;
    private String createdAt;
    private String publishedAt;

    ......
```
上述代码中，有几个常见的注解，其中`@PrimaryKey`注解表示该字段为主键，`@SerializedName("_id")`表示在序列化时的字段名，`@Ignore`表示不在表中生成该列，这三个注解比较常见。

> PS：Realm默认支持基本类型及包装类型，boolean、byte、byte []、long（short、int、long都被映射为long）、float、double、String、Date以及RealmObject子类。

如何使用Realm查询数据呢？需要使用RealmInstance执行事务Transaction：
```Java
GankItem gankItem = new GankItem();
gankItem.setId("cz89a7s4ehsjkf");

Realm.init(context);
Realm realm = Realm.getDefaultInstance();
realm.beginTransaction();
RealmResults<GankItem> items = realm.where(GankItem.class).findAll(); // 查询
realm.copyToRealmOrUpdate(gankItem); // 插入或更新
items.deleteAllFromRealm(); // 删除
realm.commitTransaction();
realm.close();
```
Realm简单使用就是这样，后面我会写写具体的操作以及事务。
