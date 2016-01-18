---
title: 修正Android Api 21 PopupWindow遮盖导航栏
date: 2016-01-18 09:37:06
tags:
---

### 问题
在Api 21 以后，若PopupWindow选择显示在屏幕底部，可能由于5.0支持透明导航栏的问题，弹出窗口会遮盖住导航栏的背景，而导航栏显示在PopupWindow上。

### 解决方法
修改Api 21 的样式，文件v21/styles.xml或styles-v21.xml，修改 android:windowDrawsSystemBarBackgrounds = "false"
```xml
<item name="windowActionBar">false</item>
<item name="windowNoTitle">true</item>
<item name="android:windowDrawsSystemBarBackgrounds">false</item>
<item name="android:statusBarColor">@android:color/transparent</item>
```
	
### 其他方法
上述方法一般能够解决问题，也可以通过计算导航栏的高度（或者状态栏）手动实现偏移，但是可能在不同的机型上会出现不该偏移但是便宜的状况。
```Java
public class NavigationBarUtil {
		
	private static String P = "android";
    	
	public static int getNavigationBarHeight(Context context) {
		int rId = context.getResources().getIdentifier("navigation_bar_height", "dimen", P);
		if (rId > 0) {
			return context.getResources().getDimensionPixelSize(rId);
		}
		return 0;
	}
		
	public static int getStatusBarHeight(Context context) {
		int rId = context.getResources().getIdentifier("status_bar_height", "dimen", P);
		if (rId > 0) {
			return context.getResources().getDimensionPixelSize(rId);
		}
		return 0;
	}
}
```