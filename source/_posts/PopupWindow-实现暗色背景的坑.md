---
title: PopupWindow 实现暗色背景的坑
date: 2017-04-19 18:48:58
tags: [Android,PopupWindow]
---

### 0x81 PopupWindow

PopupWindow是一个比较特殊的类，它不继承任何类，是一个相对而言比较独立而又非常具有依赖性的东西。独立自然指的就是它不像Dialog那样继承并实现了很多东西，而具有依赖性则正是因为它不像Dialog那样具有自己的Window，所以我们要操作Window时操作的是它所在的Activity的Window。出于这些原因，PopupWindow有些时候虽然非常实用和简便，我们却有可能遇到一些让人匪夷所思的问题，比如向下事件传递等。

### 0x82 PopupWindow 暗色背景实现

要实现暗色背景，我们有时候会在PopupWindow的ContentView中使用占有整个屏幕的画布，整个背景是半透明的，而我们真正需要的View只占据了一小部分，这样实现出来的效果和Activity差不多，我们还可以通过处理外部的View点击事件来达到点击外部区域关闭PopupWindow的效果。但是这种用法又一个坏处，当我们使用一个比较缓慢的动画开启它时，会发现背景也跟着参与动画，这样视觉体验就会大打折扣。

在平时的开发当中，PopupWindow出现的几率虽然不是很高，因为有很多中办法可以实现差不多的效果，但是既然Google提供了它，哪怕用的再少我们也要明白如何去用它。无论是网上查阅资料还是参看别人的源码，我们还会看到一种特殊的设置暗色背景的方法，这个方法其是有点trick的意思，没错，这也是我遇到的一个大坑——透明度。

### 0x83 修改透明度实现背景变暗

其实方法很简单，看代码：

```Java
window = ((BaseActivity) context).getWindow();
WindowManager.LayoutParams params = window.getAttributes();
alphaAnimator = new ValueAnimator();
alphaAnimator.setDuration(250);
alphaAnimator.addUpdateListener(animation -> {
    params.alpha = (float) animation.getAnimatedValue();
    window.setAttributes(params);
});
```

通过上面的代码我们通过BaseActivity取得了Window对象，从这也能发现PopupWindow想要使用Window的话只能使用Acitivity的Window对象。

看下面这段源码，在PopupWindow的invokePopup时，其实是使用了Activity的WindowManager添加了一个View：

```Java
private void invokePopup(WindowManager.LayoutParams p) {
    if (mContext != null) {
        p.packageName = mContext.getPackageName();
    }

    final PopupDecorView decorView = mDecorView;
    decorView.setFitsSystemWindows(mLayoutInsetDecor);

    setLayoutDirectionFromAnchor();

    mWindowManager.addView(decorView, p);

    if (mEnterTransition != null) {
        decorView.requestEnterTransition(mEnterTransition);
    }
}
```

接下来要实现变暗效果就是在显示和隐藏启用属性动画就可以了：

```Java
@Override
public void showAsDropDown(View anchor, int xoff, int yoff) {
    alphaAnimator.setFloatValues(1f, 0.8f);
    alphaAnimator.start();
    super.showAsDropDown(anchor, xoff, yoff);
}

@Override
public void dismiss() {
    alphaAnimator.setFloatValues(0.8f, 1f);
    alphaAnimator.start();
    super.dismiss();
}
```

### 0x84 windowIsTranlucent

这个属性是在Style中设置的，并且Activity一旦加载视图完成后就无法在代码中直接更改。这个属性有什么用呢？顾名思义就是把你的Window设置成透明的，我们知道Window是有背景的，但是如果你想透过一个Window看另一个Window（比如看被覆盖的Activity）只设置Window的背景为透明是不行的，你还需要将Window设置成透明的，否则将会看到Window的黑底。这个现象和启动页加载慢黑屏是一样的，所以网上也有对应的解决方案，其中很重要的一条就是设置`<item name="android:windowIsTranslucent">true</item>`，原因很容易理解，而正是这个原因，让上面的通过修改透明度使背景变暗的方法出现了错误。

### 0x85 合用的坑

由于项目中使用滑动返回的缘故，windowIsTranlucent必须设置为true，否则Activity内容以外的部分是不显示的（其实是不渲染，处理不好会出现拖影），只有设成透明才能保证滑动时看到前一个Activity的内容。回到刚才说的设置Window透明度，为什么能实现变暗呢，其实就是因为背景是黑的，界面变得透明的话后面的黑色就会“渗”出来，也就形成了所谓的变暗，那如果Window能够透明了，很显然前一个Activity的内容就显示出来了，就和滑动返回一样。

### 0x86 解决方案

知道了原因，解决方法就比较明确了，我们需要在特定的时候关掉透明，但是前面也说过一旦Activity的视图加载完成就很难再通过代码的方式去修改它，而设置Window的Background是没有用的（至少我试了不效果）。所以我们找一种特别的方法，就是通过控制Window的dimAmount属性，来让Window的黑背景发生变化。首先在Style中开启dim，其中0代表透明，1代表全黑：

```XML
<item name="android:backgroundDimEnabled">true</item>
<item name="android:backgroundDimAmount">0.0</item>
```

然后我们就可以修改Window的Attributes来修改dimAmount的值：

```Java
@Override
public void showAsDropDown(View anchor, int xoff, int yoff) {
    if (window != null) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.dimAmount = 1.0f;
        window.setAttributes(params);
    }
    alphaAnimator.setFloatValues(1f, 0.8f);
    alphaAnimator.removeAllListeners();
    alphaAnimator.start();
    super.showAsDropDown(anchor, xoff, yoff);
}

@Override
public void dismiss() {
    alphaAnimator.setFloatValues(0.8f, 1f);
    alphaAnimator.removeAllListeners();
    alphaAnimator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.dimAmount = 0.0f;
                window.setAttributes(params);
            }
        }
    });
    alphaAnimator.start();
    super.dismiss();
}
```

代码做的事情很简单，显示时将dimAmount设为1为全黑，这样就看不到前一个Activity的内容；PopupWindow消失动画结束后再将dimAmount设回0，这样可以避免闪动，要注意`alphaAnimator.removeAllListeners();`这一句，负责会影响动画的表现。

好了～这就是遇到的坑以及一个比较蹩脚的解决方案，如果有机会学习到其实可以不用这么麻烦的话，这就当一篇PopupWindow的考古文吧～
