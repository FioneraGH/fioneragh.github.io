---
title: Android DI利器：Dagger2初探
date: 2017-03-07 19:54:36
tags: [Android,Dagger,DI]
---

### 0x81 DI

Dependency Injection，简称DI，中文昵称依赖注入，一作IoC（控制反转）。这种模式很容易理解，就是将原本有我们控制的实例化对象，改为又容器动态注入，所以也有人说这是一种控制反转，但我觉得这还是有一些差别的，毕竟他们的陈述对象不算一样。

### 0x82 DI带来的好处

最早在接触SSH的时候，Spring等框架便对JavaBean采用依赖注入的方式管理对象，所有的对象都是容器管理的，框架在我们需要某些实例对象的时候动态的注入以提供我们使用。

DI最明显的特点就是不再是我们自己手动管理类对象的创建，这也将我们从可能很繁杂的类组合中解救出来，解除编写代码时类与类之间的耦合，就好像响应式编程将我们带离了回调地狱，让我们能更优雅的编写代码，把更多的精力放到业务逻辑的实现上去。

<!--more-->

### 0x83 Dagger

Dagger是匕首的意思，意味着它是一把利器。没错，现今Android的开发已不是早期那样，业务逻辑简单，程序架构单纯，我们编写代码以实现需求为准。所以MVC架构下的代码造就了非常冗杂的Activity等类，为我们的维护造成巨大的困扰。在使用MVP架构重写App时，发现有了Dagger的帮助，更是让代码逻辑更加清晰，而不会出现大量的模板化实例化过程。

Dagger本是square发布的一个Android依赖注入框架（基于Guice改写），Google在Dagger的用户数达到一定规模时，fork了Dagger并命名Dagger2，塑造了这一Android开发利器。

### 0x84 Dagger2 配置

到本文编写的时间，Dagger2已经发布了2.9（2.10-rc也已经release），新的版本中已经弃用了apt插件，转而使用兼容性更好更加高效的annotationProcessor。build.gradle配置如下：

```Groovy
compile 'com.google.dagger:dagger-android:2.9'
annotationProcessor 'com.google.dagger:dagger-compiler:2.9'
```

这样便开启了Dagger2的支持。

### 0x85 Module, Inject, Component

Dagger2的实例提供者——Module，Module使用`@Module`注解类，经过注解的类便具有了提供实例化对象的功能，而提供对象的方法主要通过`@Provides`注解标识，Dagger会为依赖自动搜索需要的提供者，若没有便会查找`@Inject`注解修饰下的构造方法。Module是生产者，`@Module`注解的includes参数还可包含其他的Module，它被Component用来注入对象。

Dagger2的被注入对象——Inject Field，被`@Inject`注解修饰的成员，在对应的DaggerComponent注入后，生成类中会主动为该field注入对象，注入后的对象可以直接拿来使用。

`@Inject`注解除了前面的修饰构造方法和成员变量分别作为对象输出和注入对象外，还可以修饰方法，看生成类的源码可以知道，被注入的方法会在对象注入完成后调用。

Dagger2的桥梁——Component，Component是一个接口，`@Component`注解通常会带有modules参数，指定它能为我们注入那些对象，还可能会有dependencies参数来指定组件依赖以及SubComponent等相互关系的组件。Component一般会有一个injectXXX的方法用于传入需要注入的组件，并且Dagger会为Component生成一个用于注入的DaggerXXX类，我们就是使用该类完成对象的注入。

Dagger的几大注解完成了依赖图DG（Dependency Graph）的绘制，在编译时生成需要的辅助类，从而实现了Android上的DI，理解生成类的代码有时间余力可以看一下Dagger2的源码，对ButterKnife,DataBinding等库的理解也能加深。
