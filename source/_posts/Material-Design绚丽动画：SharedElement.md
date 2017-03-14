---
title: Material Design绚丽动画：SharedElement
date: 2017-03-14 19:32:36
tags: [Android,Transition]
---

### 0x81 Transition Framework
如之前提到的，Transition Framework是Android Kitkat开始添加的动画框架，在Lolipop才得到完美的支持，所以支持源码里会对当前版本进行判断，采用不同的实现，API 19和21是两个分水岭。Transition其实就是动效转场的意思，Google官网既推出PropertyAnimator之后，为充分利用RenderThread并解放Android开发者，官方实现了一些专长动画，而这些动画是根据View状态来动态计算的，而Scene就保存了这些状态，动画实际上描述的就是从一种Scene到另一种Scene变化的过程。

### 0x82 SharedElementTransition
这种方式与之前记录的ContentTransition差不多，它不过是有一组共享的元素会被单独处理，具体的配置也有类似的属性，用法很简单：
```Java
startActivity(new Intent(context, ImageDetailActivity.class)
    .putExtra("imageUrl", gankItemGirl.url), ActivityOptionsCompat
    .makeSceneTransitionAnimation((Activity) context,
    gankDayGirlHolder.iv_girl, context.getString(R.string.share_image)).toBundle());
```
其中，`R.string.share_image`是`gankDayGirlHolder.iv_girl`的transitionName，不需要任何其他的东西便可以开始一个共享元素的转场，你只需要保证目标Activity也有这样一个元素。

### 0x83 手动控制转场
主要是两个API，`postponeEnterTransition()`暂停目标Activity入场，`startPostponedEnterTransition()`还原被暂停的入场。

提供这样的API的原因很简单，当图片是动态加载的时候，如果图片本身的内容会发生变化，比如我设定了adjustViewBounds并且设置控件高度为wrap_content，那转场前系统计算的坐标就是错的，会出现图片移动异常的现象。这时候我们就可以使用这两个API动态控制，但是注意这两个API必须成对且时间不要太长，否则Activity的跳转会卡住等待你调用start，下面是使用Glide的一个不怎么好的实践，但是能说明和解决问题：
```Java
postponeEnterTransition();
Glide.with(mContext).load(getIntent().getStringExtra("imageUrl")).into(new SimpleTarget<GlideDrawable>() {
    @Override
    public void onResourceReady(GlideDrawable resource,
                                GlideAnimation<? super GlideDrawable> glideAnimation) {
        ivImageDetailPreview.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        L.d("ImageDetail onPreDraw");
                        ivImageDetailPreview.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
        ivImageDetailPreview.setImageDrawable(resource);
    }

    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        startPostponedEnterTransition();
    }
});
```
其实代码也很简单，就是提前停止转场，在控件接收到新的图片重绘完成时恢复即可。

### 0x84 Fresco 的坑
在用Fresco提供的SimpleDraweeView的时候，发现转场完全不工作，应该是透明度动画产生了冲突，毕竟这转场是根据可见度来计算并开始的，不知道透明度会不会有什么影响。这时候如果返回会发现图片一闪而过，这个问题也有很多人在gayhub上提过issue，issue也是关于transition与fresco一起不工作的问题，官方给出了解决方案，自己设置SharedTransition为Drawee提供的：
```Java
getWindow().setSharedElementEnterTransition(DraweeTransition
    .createTransitionSet(ScalingUtils.ScaleType.CENTER_CROP, ScalingUtils.ScaleType.FIT_CENTER));
getWindow().setSharedElementReturnTransition(DraweeTransition
    .createTransitionSet(ScalingUtils.ScaleType.FIT_CENTER, ScalingUtils.ScaleType.CENTER_CROP));
```
这样子的确可以工作了，并且不使用上述两个API系统也能计算准确，但是返回时会有闪动，也许是处理还不够完善，或者是我有漏掉的地方。注意上方window的API要求`targetApi > 21`才行，否则会抛出NoSuchMethod异常。

到这Transition的基本功能使用差不多就说完了，后面还会记录一下和Google新宠ConstraintLayout搭配的时候体现的效果。