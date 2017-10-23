---
title: 动态切换Launcher之ActivityAlias
date: 2017-02-23 18:59:25
tags: [Android,Activity]
---

### 0x81 背景

每逢双11或是重大节日来临，我们会发现像淘宝京东这种APP的Launcher会在不更新的情况下更换，动态切换Launcher图标的技术如果使用ActivityAlias功能将会特别容易。

### 0x82 ActivityAlias 是什么

看名字就能知道，ActivityAlias就是别名的意思，是Google官方为方便开发为Activity设置别名的时候提供的方便方法。
利用别名这个技术可以实现很多蹩脚的需求，比如这个动态更换Launcher图标，在比如wxapi的回调。

### 0x83 ActivityAlias 的简单利用

1. 别名基本功能——为一个Activity设置别名

    和定义普通的Activity一样，我们只需要在Manifest文件中定义对应的Activity即可，他不需要一个真实存在的Activity与之名字对应。
    ```XML
    <activity-alias
        android:name="com.package.wxapi.WXEntryActivity"
        android:icon="@mipmap/ic_launcher_another"
        android:label="@string/app_name_another"
        android:enabled="true"
        android:targetActivity=".WXShareCallbackActivity">
    </activity-alias>
    ```
    `android:name`即定义这一别名的ComponentName，虽然不需要与实际存在的对应，但应保持唯一以避免出现异常的BUG。
    `android:icon,android:label`定义了图标和名字。
    `android:enabled`设定是否可用。
    `android:targetActivity`最为重要，它指明了这个别名是给哪个Activity的。

    经过这些设置，微信的分享回调会调用到WXShareCallbackActivity。

1. 动态切换Launcher

    与上面的定义类似，只需添加对应的intent-filter:
    ```XML
    <activity
        android:name=".MainActivity"
        android:launchMode="singleTask">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />

            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    <activity-alias
        android:name=".AnotherMainActivity"
        android:icon="@mipmap/ic_launcher_another"
        android:label="@string/app_name_another"
        android:enabled="false"
        android:targetActivity=".MainActivity">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />

            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity-alias>
    ```
    基本属性一致，`android:enabled="false"`确保最开始不会出现两个启动图标。

### 0x84 动态切换的实现

实现其实也很简单，就是PackageManager的组件属性API。

```Java
ComponentName anotherComponentName = new ComponentName(this,
        "com.fionera.demo.AnotherMainActivity");
ComponentName originComponentName = new ComponentName(this,
        "com.fionera.demo.MainActivity");
PackageManager packageManager = getPackageManager();
if (PackageManager.COMPONENT_ENABLED_STATE_DISABLED == packageManager
        .getComponentEnabledSetting(anotherComponentName)) {
    packageManager.setComponentEnabledSetting(anotherComponentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    packageManager.setComponentEnabledSetting(originComponentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
} else {
    packageManager.setComponentEnabledSetting(anotherComponentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    packageManager.setComponentEnabledSetting(originComponentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
}
```

代码已经很明确了，判断启用状态交替设置另外一个组件生效。

Launcher检测到组件改变通常需要一段时间，如果想即时更新，需要杀死Launcher使之强行重启（不一定生效，killBackgroundProcesses在高版本已被限制）。

```Java
Intent intent = new Intent(Intent.ACTION_MAIN);
intent.addCategory(Intent.CATEGORY_HOME);
intent.addCategory(Intent.CATEGORY_DEFAULT);
List<ResolveInfo> resolves = packageManager.queryIntentActivities(intent, 0);
for (ResolveInfo res : resolves) {
    if (res.activityInfo != null) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        am.killBackgroundProcesses(res.activityInfo.packageName);
    }
}
```

### 0x85 一个坑

我本来把这个操作放在Application类当中，后来遇到了ComponentNameNotFound异常，应该是这个操作导致Component加载的判断出现异常，特别是操作完就启用这个组件极易出现。
因此最好把该操作放置到相对安全的地方。
