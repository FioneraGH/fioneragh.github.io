---
title: Material Design绚丽动画：transition framework
date: 2017-03-09 18:20:38
tags: [Android,Transition]
---

### 0x81 Transition Framework
Transition Framework是Android Kitkat开始添加的动画框架，在Lolipop才得到完美的支持，所以支持源码里会对当前版本进行判断，采用不同的实现，API 19和21是两个分水岭。Transition其实就是动效转场的意思，Google官网既推出PropertyAnimator之后，为充分利用RenderThread并解放Android开发者，官方实现了一些专长动画，而这些动画是根据View状态来动态计算的，而Scene就保存了这些状态，动画实际上描述的就是从一种Scene到另一种Scene变化的过程。

### 0x82 Scene
Scene中文意为场景，就是将当前页面（或者说Activity，亦或者可以视为一个场景的View）以及其子View的状态抽象为一个场景，而它们的位置大小可视状态就是场景里的元素。TransitionManager就是负责将View从一个场景转变为另一个场景，而变换的方式就是Transition。

### 0x83 生成一个场景
场景的生成官方提供了标准API，直接调用即可：
```Java
Scene scene1 = Scene.getSceneForLayout(clImageDetailRoot, R.layout.scene_1_change_bounds, mContext);
Scene scene2 = Scene.getSceneForLayout(clImageDetailRoot, R.layout.scene_2_change_bounds, mContext);
```
其中`Scene#getSceneForLayout`方法需要三个参数，第一个参数是要应用场景的RootView，第二个参数是描述了当前布局状态的layout文件，第三个是inflate布局要用到的上下文，API会读出所需要的参数保存到场景中并将这个场景缓存起来。

### 0x84 TransitionMangaer#go 方法
TransitionManager转场管理器提供了没几个方法，其中go是很重要的一个方法，因为它是直接输入Scene的唯一方法，用法很简单：
```Java
TransitionManager.go(scene1);
TransitionManager.go(scene2, new ChangeBounds());
```
它接收1个或2个参数，第二个参数就是Transition对象，如果不传这个参数，它会自动使用`AutoTransition`这个类，看源码它就是个很简单的集合：
```Java
public AutoTransition(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
}

private void init() {
    setOrdering(ORDERING_SEQUENTIAL);
    addTransition(new Fade(Fade.OUT)).
            addTransition(new ChangeBounds()).
            addTransition(new Fade(Fade.IN));
}
```
而第二个参数Transition，Framework已经提供了很多个实现，当然也可以书写`res/transition/*.xml`文件自己定义transitionSet。

>PS：Transition Framework的完整特性是需要Lolipop以后才能使用的，比如这个Transition默认实现，基于形状的有ChangeBounds、ChangeClipBounds、ChangeTransform、ChangeImageTransform等多种实现，基于Visbility（不是View的Visibility）有Slide、Explode、Fade的实现。而Google虽然为Api14之后提供了支持库，但目前android.support.transition下提供的只有ChangeBounds和Fade，也许后续会慢慢增加支持。唉，什么时候能只支持Lolipop之后的设备。

TransitionManager#go源码实际上调用了previousScene的exit()方法，然后调用了Scene的enter()方法，而这个两个方法分别会回调exitAction和enterAction，可以设置监听做些事情。

### 0x85 TransitionMangaer#beginDelayedTransition 方法
虽然Tranisition是使用Scene作为动画的根据，但是如果我们也这样用未免有点太麻烦，并且两个布局状态里的View是动态添加到RootSceneView的。经过我的测试，这种View添加后（go到某一个scene）对该View进行操作比如设置图片，转换到另一个场景时，View会恢复原状，猜测其实是两套View完成的动画（待进一步确认）。

所以我们能不能直接对已存在的View进行Transition的相关操作呢，那就是TransitionMangaer#beginDelayedTransition方法了，看字面意思就是开始一段延时的转场，之后该View会经过一段延时变到设置后的状态，也就是从当前状态慢慢变成设置后的状态，也就是动画。用法很简单，下面是放大场景里的ImageView：
```Java
ivImageDetailPreview.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        TransitionManager.beginDelayedTransition(clImageDetailRoot);

        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.width = (int) (1.5 * width);
        layoutParams.height = (int) (1.5 * width);
        v.setLayoutParams(layoutParams);
    }
});
```

### 0x86 Activity 转场
Activity 转场有两种方式，不带共享元素转场和带共享元素转场。

1. contentTransition
    这种方式很简单，有点像之前的Activity切换动画，用法也差不多，但是这种方式可以让所有的View都有转场动画而不是仅仅作为一个整体（如果想作为几个整体可以分别使用setTransitionGroup方法）。触发动画需要在调用startActivity方法时多传一个Bundle类型的options参数，该参数由`ActivityOptionsCompat`生成。
    ```Java
    context.startActivity(new Intent(context, ImageDetailActivity.class)
                .putExtra("imageUrl", gankItemGirl.url), ActivityOptionsCompat
                .makeSceneTransitionAnimation((Activity) context).toBundle());
    ```
    启用方式知道了，那我们如何设置效果呢，其实也很简单，首先设置`Window.FEATURE_CONTENT_TRANSITIONS`这个feature，用xml定义在style中或者用代码设置都可以：
    ```Java
    getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
    super.onCreate(savedInstanceState)；
    Explode explode = new Explode();
    explode.setDuration(500);
    explode.excludeTarget(TextView.class, true);
    getWindow().setEnterTransition(explode);
    ```
    用代码设置要注意放在onCreate之前，不过我在Nougat模拟器上测试不设置也可以。

    setExitTransition/setReenterTransition用于设置当前Acitivity，分别表示跳转和返回的Transition，如果不特别设置它们是一样的。
    
    setEnterTransition/setReturnTransition用于设置当前Acitivity，分别表示进入和退出的Transition，如果不特别设置它们是一样的。

    和Acitivity在style中设置动画方法一样，也可以在style中定义：
    ```XML
    <item name="android:windowExitTransition">@transition/test1</item>
    <item name="android:windowEnterTransition">@transition/test2</item>
    <item name="android:windowReturnTransition">@transition/test3</item>
    <item name="android:windowReenterTransition">@transition/test4</item>
    ```

    如果不想让所有的View或只允许个别的View作出动画，可以设置exclude或target，然后配合group，通过代码和transition配置文件都可以。

    如果动画时间太长，会发现原Acitivity动画还没完成就被后面覆盖了，也可以在style中配置不覆盖：
    ```XML
    <item name="android:windowAllowEnterTransitionOverlap">false</item>
    <item name="android:windowAllowReturnTransitionOverlap">false</item>
    ```
    其实关于Transition有很多属性可以配置，还有针对fragment的，就像原本的Animation一样，基本上原本Animation的都有对应的Transition属性可配置（貌似没有task的）。

2. sharedElementTransition
    这种方式和第一种差不多，就是有一组共享的元素会被单独处理，具体的配置也类似的属性，后面可以作为子篇单独记录一下，毕竟使用方法也很容易。

就这么多，当然其实还有很多细节，如果有机会后面再写。