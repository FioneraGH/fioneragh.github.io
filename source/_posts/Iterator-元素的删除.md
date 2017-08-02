---
title: Iterator 元素的删除
date: 2017-08-02 13:34:23
tags: [Java,Iterator]
---

### 0x81 问题情景
有一个链式列表LinkedList，我们需要对其中的元素进行遍历，如果遇到符合条件的元素，则从LinkedList中删除该元素，为了方便遍历我们通常使用forE的方式进行遍历，满足条件再remove掉元素：
```Java
for (RequirementBean requirementBean : requirementBeanList) {
    if (TextUtils.equals(requirementBean.getUuid(),
        (String) cardView.getTag())) {
        requirementBeanList.remove(requirementBean);
    }
}
```
但是执行这段代码很不幸的触发了异常ConcurrentModificationException。

![异常结果](/images/2017_08_02_01.png)

### 0x82 异常原因
首先我们要明白forE的遍历机制，其实它的内部实现就是通过迭代器Iterator实现的，也就是说它本身也是Collection.iterator()后使用iterator.next()的方式遍历元素。关于Iterator网上的解释是`Iterator 是工作在一个独立的线程中，并且拥有一个 mutex 锁。Iterator 被创建之后会建立一个指向原来对象的单链索引表，当原来的对象数量发生变化时，这个索引表的内容不会同步改变，所以当索引指针往后移动的时候就找不到要迭代的对象，所以按照 fail-fast 原则 Iterator 会马上抛出 java.util.ConcurrentModificationException 异常。`

### 0x83 解决办法
最简单的办法就是使用index逆序遍历删除元素，之所以逆序是因为这样可以忽略列表本身的变化导致的index变化。还有一种变法是新建一个待删除列表，把要删除的元素存入这个列表，最终调用removeAll删除元素。当然，forE是基于Iterator的，如果我们使用Iterator该如何避免这个异常？

首先我们看Iterator的next()方法实现：
```Java
public E next() {
    checkForComodification();
    try {
        E next = get(cursor);
        lastRet = cursor++;
        return next;
    } catch(IndexOutOfBoundsException e) {
        checkForComodification();
        throw new NoSuchElementException();
    }
}
```
其中又一个checkForComodification方法，就是这个方法抛出的异常：
```Java
final void checkForComodification() {
    if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
}
```
modCount是当前状况下发生修改的数量，而expectedModCount是在生成Iterator时的修改数量，如果发生过修改导致modCount与expectedModCount不相等，就会触发ConcurrentModificationException异常，这是一种检查线程安全的手段。

modCount在Collection的实现类的add/remove方法中都会增加，所以都会导致异常的发生，那我们要如何更改expectedModCount呢？Iterator也有一个无参的remove方法：
```Java
public void remove() {
    if (lastRet == -1)
        throw new IllegalStateException();
    checkForComodification();

    try {
        AbstractList.this.remove(lastRet);
        if (lastRet < cursor)
            cursor--;
        lastRet = -1;
        expectedModCount = modCount;
    } catch(IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException();
    }
}
```
源码中先对Comodification进行了校验，然后移除了原列表AbstractList对应位置的元素并重置了expectedModCount为modCount，所以我们在使用Iterator时使用Iterator的remove方法就可以删除符合条件的元素了。当然Iterator只提供了删除方法，因为添加没有办法处理添加的位置，所以对于某些特殊的状况还是使用index遍历的方法比较可控。
