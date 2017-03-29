---
title: Fresco与SharedElement共用的问题
date: 2017-03-29 18:55:32
tags: [Android,Fresco]
---

### 0x81 Fresco 的坑
在之前的Android Transition动画框架中的共享元素部分我们提到过Fresco的SimpleDraweeView会导致图片消失，在gayhub上也有很多针对这个问题的issue提出来，后来官方给出的解决方案是不使用Android自带的ImageTransaction，而是使用Fresco提供的辅助工具生成Transaction：
```Java
getWindow().setSharedElementEnterTransition(DraweeTransition
    .createTransitionSet(ScalingUtils.ScaleType.CENTER_CROP, ScalingUtils.ScaleType.FIT_CENTER));
getWindow().setSharedElementReturnTransition(DraweeTransition
    .createTransitionSet(ScalingUtils.ScaleType.FIT_CENTER, ScalingUtils.ScaleType.CENTER_CROP));
```
当初发现这样可以工作了，但是返回时会有闪动，后来经过测试发现并不是闪动，而是原Activity里的空间不见了。至于闪动，是由于我在图片详情的页面里使用了沉浸式的界面，从目标Activity返回时重绘了界面使他又显示了出来，所以才出现闪动。

### 0x82 Nougat 上Fresco 新的问题
之所以官方的解决方案最终使issue们关闭了，是因为这种方式在Android L/M上工作的很好，是在Android N上才又出现了这个问题，由于WindowFlag的切换导致原来的ImageView又显示出来，虽然界面出现闪动但也不影响使用，但这样毕竟不够完美。gayhub上也有人指出调用一次requestLayout就能显示出来，但会闪动。

所以，又有人提出了看起来更合理的解决方案，使用ExitSharedElementsCallback，通过这个回调将消失的View显示出来：
```Java
setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(
                    List<String> sharedElementNames,
                    List<View> sharedElements,
                    List<View> sharedElementSnapshots) {
                super.onSharedElementStart(sharedElementNames,
                        sharedElements, sharedElementSnapshots);
                for (View view : sharedElements) {
                    if (view instanceof SimpleDraweeView) {
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
```
原理很简单，就是找出时共享元素的SimpleDraweeView并手动让它显示出来，目前看起来工作良好。

虽然这样也能达到目的，我还是希望官方能出一个完备的解决方案，从根本上解决问题。