---
title: UnitTest插入Mysql数据中文乱码
date: 2017-05-22 18:05:04
tags: [Mysql]
---

### 0x81 数据库中文乱码问题

相信很多人都遇到过mysql数据库存储数据，中文变成“？”的问题，而出现这个问题的原因也很简单，就是字符集里没有中文，从而导致存入的数据在编码时不识别，而到使用的时候解码出来的符号也不是我们需要的汉字。

### 0x82 解决方案

解决方案很简单，更改创建数据库时的字符集即可。网上有提供修改默认字符集的办法，修改/etc/my.cnf文件中的default charset为utf8即可，对于已创建的数据库有人说直接修改表的字符集也可以但是我实验了发现并没有生效。当然了，我个人认为最好的处理方式是在创建数据库及数据表时显式的指明使用的字符集，例如：

```SQL
drop database test;
create database test character set utf8 collate utf8_general_ci;
use test;
create table city(city_id int not null auto_increment, city_name varchar(20) not null default '',description varchar(50) not null,primary key(city_id)) default charset=utf8;
create table if not exists activity( id int not null auto_increment, title varchar(25) not null default '', description text not null, imgPath varchar(255) not null, start_time datetime not null default now(), end_time datetime not null default now(), primary key(id), unique key(imgPath) ) default charset=utf8 comment "活动表" auto_increment=10;
```

这样创建的数据表就是使用utf8字符集，自然也是支持中文的，那我在使用mysql-client向mysql-server传输数据时也需要使用对应的字符集，比如使用SpringBoot并用jdbc连接mysql数据库：

```Yaml
spring.datasource.url=jdbc\:mysql\://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf8&useSSL=false
spring.datasource.username=root
spring.datasource.password=root
```

这样就能保证插入的中文不会乱码了。

### 0x83 单元测试的坑

工程使用的是Mybatis，在Service中最终使用DAO将数据插入数据库时没有任何问题，但是当我在JUnit中使用SqlSession进行测试时，发现插入的中文一直都是“？”。直到后来我才发现，单元测试的@SetUp我使用的Mybatis的配置文件是mybatis-config.xml：

```Java
try {
    reader = Resources.getResourceAsReader("mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
} catch (Exception e) {
    e.printStackTrace();
}
```

而我在mybatis-config.xml里的配置是没有字符集的：

```XML
<transactionManager type="JDBC"/>
<dataSource type="POOLED">
    <property name="driver" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://127.0.0.1:3306/test" />
    <property name="username" value="root"/>
    <property name="password" value="root"/>
</dataSource>
```

把url改成`jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false`就可以了。真是坑= =
