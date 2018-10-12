---
title: Java Core
date: 2017-05-13 14:21:04
tags: [速记,Java]
---

### ArrayList

早期的ArrayList是使用数组完成的列表功能，内部关键数组 Object[] elementData和计数值size。

ArrayList的容量是动态扩充的，newCapacity = oldCapacity + (oldCapacity >> 1)，即增加50%。

容量扩充使用copyarray的方式，向index位置添加元素也是同一道理，只是需要将对应位置元素全部后移。

添加新元素都要进行容量检查，容量不足时进行容量扩充，再进行具体的增加元素操作。

序列化反序列化自行实现writeObject和readObject方法，不以容量数组践行序列化，而是以实际数组进行序列化，序列化writeObject先写入size，同理readObject先读出size，再进行对应的序列化反序列化操作。该操作可能导致并发异常，比如序列化时添加内容导致size不一致。

Vector与ArrayList类似，对应操作方法由synchronized修饰保证线程安全，因此效率相对较低。

### LinkedList

双向链表：HEAD NULL <- prev E next <-> prev E next <-> prev E -> NULL END  

新增add实际调用linkLast，元素叫做Node，linkLast做三件事——生成新的Node、关联prev、关联next。LinkedList有一个last变量用于记录最后一个元素，如果为空，则新增的元素为第一个元素`first = newNode`，否则关联原本的last.next为新的Node。

查询采用折半，index小于size的1/2从HEAD开始，否则从END开始，因为没有明显的顺序数组关系，只能遍历，复杂度O(n/2)。

### HashMap-JDK7

数组和链表，容量和负载因子确定了扩容策略，默认容量16～0.75

新增Element调用put方法，key通过hash得到HashCode，根据数组长度取模作为数组index。

位运算效率通常高于除模运算，因此HashMap内的数组长度通常为2^n，然后使用2^n - 1做位运算，运算方式是hashcode & (2^n - 1) = hashcode % 2^n

至于hash后取模冲突，则通过链表处理，table[index]为链表。

获取Element与put类似，key通过hash算出index，如果连标志有一个元素则就是这个元素，否则遍历链表。。

红黑树-JDK8，遍历链表是一个极为低效的行为，红黑树能将复杂度从O(n)提升到O(logn)

红黑树仅仅在某个index下元素个数达到阈值（8）才会替代链表。

建议单线程使用，否则可能死循环，多线程并发环境使用ConcurrentHashMap。

### HashSet

里面有一个transient的HashMap和一个dummy Object PRESENT，add方法以E为key、PRESENT为value写入HashMap。

key的不重复性保证了HashSet中Element的不重复性。

### LinkedHashMap

HashMap是无序的，LinkedHashMap是有序的。

与LinkedList有相似的顺序记录功能，通过双向链表实现。

根据写入/访问顺序排序。

存储方式仍是HashMap，通过继承HashMap的Entry添加before和after两个变量来记录顺序。

重写了主要方法以支持顺序。

### MultiThread

单核心分配时间片切换线程需要做上下文切换，即保存现场，十分低效，但远比等待一个IO高效。

安全的多线程有两个条件：互相独立（没有共享变量，数据分离）和加锁。

尽量避免创建过的线程，线程池是一个很好的方案。

一个线程只获取一个锁（占用一个资源）来避免死锁。

原子性、可见性（volatile）、顺序性。

#### synchronized

同步锁，用于多线程安全，修饰实例方法锁对象，修饰静态方法锁Class，修饰代码块需指定锁对象。

JVM 进入退出Monitor实现同步，monitor.enter/exit，锁的获取和释放是低效的。

JDK1.6引入偏向锁，使用Compare And Swap更新锁对象的Mark Word为ThreadID，解锁时会暂停锁线程判断锁对象Mark Word设置无锁或降级轻量锁，这一时刻发生在全局安全点（无字节码运行）。

#### ReentrantLock

重入锁，线程获得锁可以重新加锁，使用加锁计数确定锁状态，自己不会阻塞自己，基于AQS（抽象队列同步）。

非公平锁不会像公平锁一样判断队列是否有其他线程，而是直接获取锁。

#### ConcurrentHashMap-JDK7

rehash的HashMap线程不安全，可能在get时死循环。

并发安全的原因是数组使用Segment，Segment继承ReentrantLock，通过锁控制。HashEntry中的value使用volatile保证可见性。

提前扩容，HashMap插入数据后扩容。

JDK8中使用CAS和synchronized保证并发安全，HashEntry直接改为Node。

### 内存划分

方法区、堆、虚拟机栈、本地方法栈、程序计数器，前两者线程共享。

虚拟机栈由栈帧组成，每调用一次方法都会产生。

堆，垃圾回收器工作区域，分代回收。

方法区（永久代）主要是常量、静态变量等。

类加载双亲委派模型：Bootstrap - Extension - Application - Custom，类加载先交由父加载器，无法加载再降级。这有利于像Object这种类在加载中唯一。
