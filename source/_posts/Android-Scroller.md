---
title: Android Scroller
date: 2017-03-11 12:36:47
tags: [Android,Scroller]
---

### 0x81 Scroller 是什么

Scroller是Android提供的一个滚动计算器，我们通常用它在自定义View时形成动画，更是做滚性滚动提高用户体验的利器。Scroller跟踪整个View的内容位移变化，但是它不会主动做些什么，它只是计算了对应的数值用于返回给开发者使用。

### 0x82 startScroll 方法

该方法是触发滚动的最简单方法，它共接受5个参数，前四个分别为起始位置坐标和结束位置坐标，第五个参数是滚动计算时长，默认为250ms。

看一下startScroll的源码：

```Java
public void startScroll(int startX, int startY, int dx, int dy, int duration) {
    mMode = SCROLL_MODE;
    mFinished = false;
    mDuration = duration;
    mStartTime =AnimationUtils.currentAnimationTimeMillis();
    mStartX = startX;
    mStartY = startY;
    mFinalX = startX + dx;
    mFinalY = startY + dy;
    mDeltaX = dx;
    mDeltaY = dy;
    mDurationReciprocal = 1.0f / (float) mDuration;
}
```

我们可以看到实际上这个方法仅仅是保存了一下你传进去的值，并没有进行动画的相关代码。

### 0x83 fling 方法

fling方法与startScroll方法类似，只不过这个方法是基于一个fling的手势的，它接收8个参数，其中前四的是比较重要的，分别表示滚动开起点坐标和初始速度。

看一下fling的源码：

```Java
public void fling(int startX, int startY, int velocityX, int velocityY,
        int minX, int maxX, int minY, int maxY) {
    // Continue a scroll or fling in progress
    if (mFlywheel && !mFinished) {
        float oldVel = getCurrVelocity();

        float dx = (float) (mFinalX - mStartX);
        float dy = (float) (mFinalY - mStartY);
        float hyp = (float) Math.hypot(dx, dy);

        float ndx = dx / hyp;
        float ndy = dy / hyp;

        float oldVelocityX = ndx * oldVel;
        float oldVelocityY = ndy * oldVel;
        if (Math.signum(velocityX) == Math.signum(oldVelocityX) &&
                Math.signum(velocityY) == Math.signum(oldVelocityY)) {
            velocityX += oldVelocityX;
            velocityY += oldVelocityY;
        }
    }

    mMode = FLING_MODE;
    mFinished = false;

    float velocity = (float) Math.hypot(velocityX, velocityY);

    mVelocity = velocity;
    mDuration = getSplineFlingDuration(velocity);
    mStartTime = AnimationUtils.currentAnimationTimeMillis();
    mStartX = startX;
    mStartY = startY;

    float coeffX = velocity == 0 ? 1.0f : velocityX / velocity;
    float coeffY = velocity == 0 ? 1.0f : velocityY / velocity;

    double totalDistance = getSplineFlingDistance(velocity);
    mDistance = (int) (totalDistance * Math.signum(velocity));

    mMinX = minX;
    mMaxX = maxX;
    mMinY = minY;
    mMaxY = maxY;

    mFinalX = startX + (int) Math.round(totalDistance * coeffX);
    // Pin to mMinX <= mFinalX <= mMaxX
    mFinalX = Math.min(mFinalX, mMaxX);
    mFinalX = Math.max(mFinalX, mMinX);

    mFinalY = startY + (int) Math.round(totalDistance * coeffY);
    // Pin to mMinY <= mFinalY <= mMaxY
    mFinalY = Math.min(mFinalY, mMaxY);
    mFinalY = Math.max(mFinalY, mMinY);
}
```

发现它和startScroll一样，就只是计算保存了一部分数值而已。

### 0x84 滚动处理的真正实现

如前面所见，Scroller只是一个辅助类，而它的触发滚动的方法都只是保存了一些初始化数据，那这个滚动究竟是怎么实现的。这其中有一个`computeScroll()`方法，这个方法是View里的一个空方法，根据API 25的源码，这个方法会被`updateDisplayListIfDirty()`方法调用（如果drawingWithRenderNode是false，draw也会直接调用computeScroll），而`updateDisplayListIfDirty()`又被View的`draw(Canvas canvas, ViewGroup parent, long drawingTime)`方法调用，这个方法根据注释是ViewGroup调用drawChild()时触发的，而这个之后会调用`dispatchDraw(Canvas canvas)`或`draw(Canvas canvas)`方法，进而触发了onDraw回调。也就是说，View在绘制时会主动触发`computeScroll()`这个方法，由于原本是空实现（TextView等继承类都做出了实现，因此不能完全复写而不调用super，要视具体情况而定）所以没有任何效果，而这个方法就是配合Scroller的`computeScrollOffset()`方法计算滚动值的，而连续的滚动值就形成了平滑滚动等动画。

Scroller计算值主要通过两部分的帮助，一个是前面提到的`computeScrollOffset()`方法，另一个就是这个方法内部使用的一个插值器`ViscousFluidInterpolator`。

1. computeScrollOffset 方法
    我们通过接受computeScroll的回调，使用computeScrollOffset方法计算offset值给onDraw时绘制使用，根据16ms的绘制间隔，如果不会出现计算或绘制延时掉帧，那就是60帧的连续动画。这个方法也很简单，就是判断两种模式，如果是SCROLL_MODE则使用插值器获取数据，如果是FLING_MODE则根据速度和时间获取当前滚动值：
    ```Java
    if (timePassed < mDuration) {
        switch (mMode) {
            case SCROLL_MODE:
                final float x = mInterpolator.getInterpolation(timePassed * mDurationReciprocal);
                mCurrX = mStartX + Math.round(x * mDeltaX);
                mCurrY = mStartY + Math.round(x * mDeltaY);
                break;
            case FLING_MODE:
                final float t = (float) timePassed / mDuration;
                final int index = (int) (NB_SAMPLES * t);
                float distanceCoef = 1.f;
                float velocityCoef = 0.f;
                if (index < NB_SAMPLES) {
                    final float t_inf = (float) index / NB_SAMPLES;
                    final float t_sup = (float) (index + 1) / NB_SAMPLES;
                    final float d_inf = SPLINE_POSITION[index];
                    final float d_sup = SPLINE_POSITION[index + 1];
                    velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                    distanceCoef = d_inf + (t - t_inf) * velocityCoef;
                }

            mCurrVelocity = velocityCoef * mDistance / mDuration * 1000.0f;

            mCurrX = mStartX + Math.round(distanceCoef * (mFinalX - mStartX));
            // Pin to mMinX <= mCurrX <= mMaxX
            mCurrX = Math.min(mCurrX, mMaxX);
            mCurrX = Math.max(mCurrX, mMinX);

            mCurrY = mStartY + Math.round(distanceCoef * (mFinalY - mStartY));
            // Pin to mMinY <= mCurrY <= mMaxY
            mCurrY = Math.min(mCurrY, mMaxY);
            mCurrY = Math.max(mCurrY, mMinY);

            if (mCurrX == mFinalX && mCurrY == mFinalY) {
                mFinished = true;
            }

            break;
        }
    }
    else {
        mCurrX = mFinalX;
        mCurrY = mFinalY;
        mFinished = true;
    }
    ```

1. ViscousFluidInterpolator
    这个类用途很窄，就是用于SCROLL_MODE辅助计算返回当前时间对应值，它的实现也很简单这里就不贴了。

Scroller是个很简单的类，源代码也就不到600行，但是这里的确能看出Google设计的精妙，这种能符合所有View绘制需求的解耦的确不是一人一日之功。总而言之，Android源码虽然不能说无可挑剔，毕竟存在历史包袱，但在兼容性和扩展性上能做到这样，的确是很值得学习的。
