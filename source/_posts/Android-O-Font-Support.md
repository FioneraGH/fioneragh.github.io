---
title: Android O Font Support
date: 2017-08-01 18:58:57
tags: [Android,Font]
---

### 0x81 早期的字体支持
在平时的开发当中，我们通常不会去手动干预应用的字体，因为使用系统的字体时，如果系统的字体发生改变，相应的我们的APP也会跟着改变。可是有的时候产品会有特殊的需求，我们需要保持整体的界面风格或者在某些位置我们需要使用特殊风格的艺术字体，更有甚者使用iconfont作为app的图标来源，在这种状况下我们就需要修改我们应用内的字体从而达到我们想要的效果。

在Android O之前的平台，字体一直是作为普通资源存在的，它不像Drawable中的图片拥有自己的resId从而通过R文件的方式引用它。Google推荐我们将字体放入Android的静态资源目录assets下，然后通过Typeface提供的方法createFromAsset获取Typeface对象并设置给TextView：
```Java
Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "number.otf");
        setTypeface(typeFace);
```

这种方式是在Java带马当中进行设置，在xml中通常是没有效果的，xml的typeface和fontFamily的支持也有限，我们有时候会使用自定义属性来自定义字体的处理。

所幸，在Android O之后，Google终于又将字体支持提上了台面。

### 0x82 Android Studio 3.0 对字体的支持
Android Studio已经可以直接预览字体文件，并且提供了对font资源的支持，我们只需要将字体文件放入res/font资源文件夹，AS会为字体文件生成资源ID，但是这个字体文件还不推荐直接使用，我们需要创建一个xml资源描述文件来描述一个fontFamily才能在程序中使用它：
```XML
<font-family xmlns:tools="http://schemas.android.com/tools"
             xmlns:android="http://schemas.android.com/apk/res/android"
             xmlns:app="http://schemas.android.com/apk/res-auto">
    <font
        android:font="@font/number"
        android:fontStyle="normal"
        android:fontWeight="400"
        app:font="@font/number"
        app:fontStyle="normal"
        app:fontWeight="400"
        tools:targetApi="o" />
</font-family>
```
上面描述了一个字重400普通风格的number字体，其中app命名空间是为了向后兼容，其中对于字重的信息可以去看维基百科。

对于Java代码中字体资源的使Google提供了新的ResourceCompat类来帮助我们返回Typeface：
```Java
Typeface typeFace = ResourcesCompat.getFont(context, R.font.number_font);
setTypeface(typeFace);
```

### 0x83 ResourceCompat
这个类提供了兼容API的方式获取资源，其中部分方法与ContextCompat类似但他们大都废弃了，基本上这个类就是专门为Font服务的，getFont最终调用了loadFont，源码很简单：
```Java
private static Typeface loadFont(
        @NonNull Context context, Resources wrapper, TypedValue value, int id, int style,
         @Nullable TextView targetView) {
    if (value.string == null) {
        throw new NotFoundException("Resource \"" + wrapper.getResourceName(id) + "\" ("
                + Integer.toHexString(id) + ") is not a Font: " + value);
    }

    final String file = value.string.toString();
    if (!file.startsWith("res/")) {
        // Early exit if the specified string is unlikely to the resource path.
        return null;
    }

    Typeface cached = TypefaceCompat.findFromCache(wrapper, id, style);
    if (cached != null) {
        return cached;
    }

    try {
        if (file.toLowerCase().endsWith(".xml")) {
            final XmlResourceParser rp = wrapper.getXml(id);
            final FamilyResourceEntry familyEntry =
                    FontResourcesParserCompat.parse(rp, wrapper);
            if (familyEntry == null) {
                Log.e(TAG, "Failed to find font-family tag");
                return null;
            }
            return TypefaceCompat.createFromResourcesFamilyXml(
                    context, familyEntry, wrapper, id, style, targetView);
        }
        return TypefaceCompat.createFromResourcesFontFile(context, wrapper, id, file, style);
    } catch (XmlPullParserException e) {
        Log.e(TAG, "Failed to parse xml resource " + file, e);
    } catch (IOException e) {
        Log.e(TAG, "Failed to read xml resource " + file, e);
    }
    return null;
}
```
其实就是解析了xml并最终使用了TypefaceCompat这个类去获取Typeface，对于具体的解析分析就不在这里展开了。
