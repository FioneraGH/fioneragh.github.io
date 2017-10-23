---
title: Realm：使用Realm存储数据
date: 2017-05-11 18:12:31
tags: [Android,Realm]
---

### 0x81 Realm简单使用

昨天我写了如何在Android上启用Realm数据库[在Android中使用Realm](https://fioneragh.github.io/2017/05/10/%E5%9C%A8Android%E4%B8%AD%E4%BD%BF%E7%94%A8Realm/)，并举了一个小例子来演示Realm操纵数据库最简单的办法，并且提到了我们的主角——事务。

### 0x82 Realm事务

先看昨天的例子：

```Java
Realm.init(context);
Realm realm = Realm.getDefaultInstance();
realm.beginTransaction();
RealmResults<GankItem> items = realm.where(GankItem.class).findAll(); // 查询
realm.copyToRealmOrUpdate(gankItem); // 插入或更新
items.deleteAllFromRealm(); // 删除
realm.commitTransaction();
realm.close();
```

先初始化后获取了Realm的默认实例，之后启用一个事务，进行增删改查等操作。

通过上面的例子，我们会发现，Realm的事务机制和Fragment等事务的处理方式类似，启用事务进行操作，最后提交事务更新数据。这是事务的一种基本写法，当然这种开闭式的写法会让代码显得一块一块的，所以Realm提供一种类似回调式的写法：

```Java
Realm realm = Realm.getDefaultInstance();
realm.executeTransaction(new Realm.Transaction() {
    @Override
    public void execute(Realm realm) {
        gankDayResults.cascadeDelete();
    }
});
```

或

```Java
// 引用自官网文档
Realm realm = Realm.getDefaultInstance();
realm.executeTransactionAsync(new Realm.Transaction() {
    @Override
    public void execute(Realm bgRealm) {
        Dog dog = bgRealm.where(Dog.class).equals("age", 1).findFirst();
        dog.setAge(3);
    }
}, new Realm.Transaction.OnSuccess() {
    @Override
    public void onSuccess() {
        // Original queries and Realm objects are automatically updated.
        puppies.size(); // => 0 because there are no more puppies younger than 2 years old
        managedDog.getAge();   // => 3 the dogs age is updated
    }
});
```

这两种方式分别代表同步事务和异步事务，后者不会阻塞线程并通过回调通知事务完成。

### 0x83 RealmObject的持久性

引用官方文档的说法RealmObject是“Auto-Updating Objects”，意为自动更新，其含义就是通过Realm查询出来的同一对象，比如它们的主键相同，对任何一个对象进行修改，所有的结果都会发生变化。既然这里提到了主键不得不说说Realm创建数据的方法，最简单的方法是使用`Realm#createObject`方法，它返回一个RealmObject对象，这个对象是自动更新的，还可以使用`Realm#copyToRealm`方法像Realm数据库表中插入数据，此时若RealmObject存在主键，还可以使用`Realm#copyToRealmOrUpdate`插入新数据或更新旧数据，当然如果没有主键，将会出现异常。

如果RealmObject存在主键，不推荐使用`Realm#createObject`方法创建数据对象，因为该方法使用默认值创建对象很有可能引起对象冲突，这种状况使用new对象并更新到Realm的方法创建数据。

关于数据存储的部分就先说这么多，后面写一下如何在Realm中完成一对多，多对多这种常用的数据库关系。
