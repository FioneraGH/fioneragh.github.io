---
title: PopupWindow中使用Fragment的坑
date: 2017-07-24 18:25:30
tags: [Android,PopupWindow]
---

### 0x81 产品需求
项目中存在一个弹出窗口，其内部有很多筛选条件用于筛选，后来这个筛选条件分了一个大组，变成了具体到组内的筛选条件，这样就必然要进行View结构上的更改。因为之前是用PopupWindow做的，新需求又是要分页切换筛选，我首先想到干脆扔个ViewPager加个FragmentPagerAdapter，把SupportFragmentManager传进去就可以了，这样原来的筛选View复用，省不少事。后来发现我果然想得太简单了，遇到了十分熟悉的一个异常`java.lang.IllegalArgumentException`。这个异常通常指代参数非法，比如使用了一个根本不存在的参数，而具体到错误描述，居然是`No view found for id 0x7f0d0136(com.fionera.base:id/vp_filter_container) for Fragment...`，意思是Android根据id并没有找到对应的View。

### 0x82 问题原因
根据描述`No view found for id 0x7f0d0136(com.fionera.base:id/vp_filter_container) for Fragment...`，源码中异常抛出的部分如下：
```Java
ViewGroup container = null;  
if (f.mContainerId != 0) {  
    container = (ViewGroup)mContainer.onFindViewById(f.mContainerId);  
    if (container == null && !f.mRestored) { 
        throwException(new IllegalArgumentException("No view found for id 0x" + Integer.toHexString(f.mContainerId) + " (" + f.getResources().getResourceName(f.mContainerId) + ") for fragment " + f));  
    }  
}  
```
我们可以发现，在通过FragmentManager为Fragment寻找Container的时候发生了container变量为空，从而触发了异常，而这个f.mContainerId就是ViewPager的id。由此可以猜测，使用FragmentManager的Transition应该也会触发这个错误（待验证）。现在核心的问题就在于FragmentManager的`mContainer.onFindViewById(f.mContainerId)`返回null，这意味着mContainer中没有这个id的控件，根据平时开发的理解，mContainer通常是Activity或Fragment中指定的（我们通过AppCompatActivity#getSupportFragmentManager或v4.Fragment#getChildFragmentManager对Fragment进行管理），而PopupWindow是一个非常独特的东西，它的ContentView通常是独立的，虽然它使用了Activity的Window，但是在Activity中无法获取PopupWindow中的View（指的是没办法在Activity的根View中通过findViewById找到，Dialog也一样且Dialog有自己的Window），所以FragmentManager中是拿不到View的，自然会抛出异常。也就是说，在所有没有直接attach在RootView上的自定义View中使用Fragment都会出现这个问题，因为它附加Container是我们没办法控制的。

### 0x83 解决方案
1. 使用PagerAdapter

    我们可以不使用Fragemnt*PagerAdapter等涉及到FragmentManager的Adapter，而是使用最原始的PagerAdapter，这样我们便可以自己控制View，将原本的Fragment切换成有状态保护的自定义View即可。

2. 使用DialogFragment

    其实Dialog如果不做处理也会出现这种问题，Google提供了DialogFragment这一方案，它比Dialog更灵活，还具有Fragment的特性，还能通过tag让FragmentManager进行缓存。但是有一点需要自行处理，就是DialogFragment的展示行为形同Dialog，诸如PopupWindow的showAsDropDown可能要自己进行具体的实现。

这个问题遇到的人可能不多，因为很少会有这种情况，StackOverFlow上有关的讨论也不多，支持较多的都是替换为DialogFragment。说句题外话，我觉得BottomSheet更适合实现这种需求，但是原型设计的事情我们说的往往不算：（
