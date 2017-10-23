---
title: Fragment 日常笔记 StateLoss
date: 2017-02-08 18:14:10
tags: [Android,Fragment]
---

### 0x81 StateLoss 问题

FragmentTransaction 在调用`commit()`时，其意图是执行某种操作，而若此时`onSaveInstanceState()`方法已被调用，将会抛出`IllegalStateException`。
为什么会这样？`onSaveInstanceState()`方法已被调用，说明FragmentManager已经保存了它管理的Fragment的FragmentState，继续操作的状态将不会被保存，`checkStateLoss()`方法认为已经保存过这样会发生状态丢失，因此会抛出`IllegalStateException`。

### 0x82 StateLoss 触发时机

之前我们常常觉得Support Library提供的API似乎更容易出现问题，而旧平台设备出现问题的次数也非常少，难道真的是Android系统随着更新问题越来越多？
其实这主要取决于生命周期发生的变化上，在Android 3.0 之前，`onSaveInstanceState()`方法在onPause回调之前就会被调用，而在Android 3.0 之后，这一方法被推迟到了onStop回调之前。
这一重要变化意味着什么，系统回收Activity从原本的onPause回调前都不可能被杀掉转变为部分版本（新版本）能保证onStop回调之前不被杀掉，而其他版本（旧版本）在onPause到onStop这段时间会被系统回收。
这个时候Support Library不得不根据状况兼容不同的版本——允许onPause到onStop之间的StateLoss。

### 0x83 StateLoss 的避免

尽量避免在除了onCreate之外的生命周期回调中调用`commit()`方法。如果想在onResume中调用，由于某些特殊情况这一回调会在Activity恢复之前被调用，最好放在`FragmentActivity#onResumeFragments`或`Activity#onPostResume`中。
异步执行要注意调用时机，异步操作完成时Activity出于哪个状态是不确定的。这种commit方式的提交是不被推崇的，因为你的提交可能完全没有保存下来，如果你不在乎这次操作的状态是否需要保存，那你可以使用`commitAllowingStateLoss()`方法。

### 0x84 onBackPress 的坑

FragmentManager管理的栈中一个Fragment也没有，当然这不代表真的没有Fragment，因为FragmentTransaction的操作是异步的（异步是很多棘手问题的罪魁祸首），所以这便带来了一个明知是问题还不得不这样的问题。
`Activity#onBackPress`有这么一段：

```Java
if (!mFragments.popBackStackImmediate()) {
    finishAfterTransition();
}

@Override
public boolean popBackStackImmediate() {
    checkStateLoss();
    executePendingTransactions();
    return popBackStackState(mActivity.mHandler, null, -1, 0);
}
```

这里说明了什么，当onBackPress触发时，必然会发生一次出栈操作，而出栈就检查了mStateSaved，而Support Library中在onStop这一hook就手动将这一变量置true，所以stop后commit几乎必现，但是Activity#onStop后不再响应返回键，所以onBackPress正常使用出现机率不是很大。
而这一切都是因为FragmentTransaction的操作是异步的，Android又不得不做这些事情。
