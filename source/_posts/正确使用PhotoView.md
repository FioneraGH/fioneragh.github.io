---
title: 正确使用PhotoView
date: 2017-05-23 18:34:37
tags: [Android,PhotoView]
---

### 0x81 PhototView

[PhotoView](https://github.com/chrisbanes/PhotoView)是一个多功能图片库，它为开发者提供了方便的图片缩放支持，大量的APP使用该开源库。引用它的方式很简单，在build.gradle文件中做好配置即可：

```Groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
dependencies {
    compile 'com.github.chrisbanes:PhotoView:2.0.0'
}
```

最新版本的PhotoView已经升级到了2.0.0，根据发布日志PhotoViewAttacher已经不再是多数情况下需要维护的实例，它的大部分功能都能使用PhotoView自身调用。因为涉及到一些覆盖测试的东西，目前我还没有向新版本迁移，仍然使用的1.3.x版本。

### 0x82 使用PhotoView

起初图片变换我是自定义的ImageView，然后手动控制矩阵，但是有了更好的轮子，没必要再浪费时间自己造轮子，去学习一个完善的轮子也许可以学到更多。因为PhotoView 2.0.0版本的PhotoView完成了近乎所有事情，已经不再需要Attacher，因此它的使用也变得和ImageView一样简单：

```XML
<com.github.chrisbanes.photoview.PhotoView
    android:id="@+id/photo_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

```Java
PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
photoView.setImageResource(R.drawable.image);
```

而1.x版本也就是需要我们手动创建Attacher并使用它完成一些功能：

```Java
PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);
photoViewAttacher.setRotationBy(180f);
```

为什么2.0.0不再需要Attacher了呢，其实查看源码，它依然初始化了Attacher，并且所有的原本在Attacher中的操作看似是PhotoView，实则都是PhotoView内的Attacher完成的：

```Java
public PhotoView(Context context, AttributeSet attr, int defStyle) {
    super(context, attr, defStyle);
    init();
}

private void init() {
    attacher = new PhotoViewAttacher(this);
    super.setScaleType(ScaleType.MATRIX);
}

@Override
public void setImageResource(int resId) {
    super.setImageResource(resId);
    attacher.update();
}

public void setRotationTo(float rotationDegree) {
    attacher.setRotationTo(rotationDegree);
}
```

这么做的好处就是可以让你完全感知不到Attacher的存在，更加方便实用者，而那些复杂的矩阵操作依然存在于Attacher当中。

<!--more-->

### 0x83 2.0.0的好处

虽然你仍可以显示的使用PhotoViewAttacher，但是我并不推荐这样。在我使用1.x版本的时候遇到过一个问题，其实PhotoView继承自ImageView做了一些适配以保证Attacher的操作不会存在兼容性问题，如果使用ImageView会出现图片显示不如你所想的那样。

用过Matrix矩阵API变换图形的都知道，ScaleType这个坑真的是说不完，Matrix提供的功能很多，根据数学知识更是能做很多有意思的事情，我曾经是以为Attacher就是执行些手势对应的矩阵操作，所以在ViewPager中显示的是普通的ImageView并使用PhotoViewAttacher关联，于是出现了图片总是显示在左上角，触摸一下就会回到中间，而ImageView的ScaleType我设置的是fitCenter。我实验了很多方法都没有办法很好的解决显示问题，比如缩小后回到左上角、缩放锚点不跟手等，当然这些都在我把ImageView换成了PhotoView后就不再出问题了。最终工作的比较好的用法是这样：

```XML
<?xml version="1.0" encoding="utf-8"?>
<uk.co.senab.photoview.PhotoView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/black" />
```

```Java
private PhotoViewAttacher photoViewAttacher;

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    return inflater.inflate(R.layout.fragment_image_gallery, container, false);
}

@Override
public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ImageView imageView = (ImageView) view;
    ImageUtil.loadImage(getArguments().getString("url"), imageView);
    photoViewAttacher = new PhotoViewAttacher(imageView);
    photoViewAttacher.setRotatable(true);
}

@Override
public void onDestroyView() {
    super.onDestroyView();
    if (photoViewAttacher != null) {
        photoViewAttacher.cleanup();
        photoViewAttacher = null;
    }
}
```

这里PhotoView我用的是带有旋转手势的[RotatePhotoView
](https://github.com/ChenSiLiang/RotatePhotoView)，虽然效果不是特别好，但还是感谢作者开源这个库节约了我的时间。

所以2.0.0将PhotoViewAttacher做的不透明，无论是易用性还是歧义上我觉得都是更加合理的，我有时也在想如果一开始就是这样也许我就不会偷懒到在普通的ImageView上加PhotoViewAttacher来用它:P。
