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
