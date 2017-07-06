---
title: Android中启动多个Activity
date: 2017-07-06 19:01:14
tags: [Android,Activity]
---

### 0x81 Notification
Android操作系统开放给我们Notification的相关API，使得我们可以轻松的控制通知的显示和行为。其中我们想要对通知的点击进行相应的响应时，就会使用到PendingIntent这个东西，PendingIntent有三个非常重要的成员方法getActivity、getBroadcast和getService，根据官方文档的解释，它们分别用点击通知时打开一个Activity、发送一个广播、启动一个Service，从而达到某些目的。

比如，收到一篇新闻的通知，我们想要通过通知进入新闻详情页，这时候我们就可以构造一个打开Activity的PendingIntent来完成这件事，PendingIntent中的Intent就是我们使用startActivity时的Intent参数：
```Java
final Notification.Builder builder = new Notification.Builder(this);
builder.setContentTitle("Open One").setContentText("Click Open One Activity").setSmallIcon(
        R.mipmap.ic_launcher).setContentIntent(PendingIntent
        .getActivity(mContext, 0, new Intent(mContext, ConstraintLayoutActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT));
notificationManager.notify(NOTIFICATION_ID, builder.build());
```
上述代码在Android O 以下已经可以正常运行了，这种状况对于程序正在后台运行并且Activity栈中存在根Activity时会直接在栈顶创建新打开的Activity，和startActivity行为一致，但是若App已经不再运行，而我们又是使用ApplicationContext开启Activity，则必须在Intent上追加参数`Intent.FLAG_ACTIVITY_NEW_TASK`才行。对于发送广播或者是启动Service，只需要使用PendingIntent特定的API即可。

如果你的程序tagetApi是Android O，只是这样是无法成功发送通知的，因为Android O 对通知系统又做了改进，添加了NotificationChannel的支持，并支持在Pixel Launcher上添加通道通知显示和iOS的Badge提示。因此我们需要创建一个通知通道：
```Java
NotificationChannel channel = new NotificationChannel("testChannel", "TestChannel", NotificationManager.IMPORTANCE_DEFAULT);
channel.setDescription("Description");
channel.enableLights(true);
channel.setLightColor(Color.RED);
notificationManager.createNotificationChannel(channel);
```
之后在NotificationBuilder中指定ChannelId就可以弹出通知了。

### 0x82 startActivities和getActivities
继续之前的例子，打开了新闻详情的页面，如果是正常情况下我们还可以按返回键回到根Activity，但若是我们的Activity是通过重新创建Task后启动的，那按返回键就会直接退出到先前的Task，这种体验有时候不是很好。我们知道startActivities就是用于启动多个Activity的，而PendingIntent提供了类似的方法getActivities，这样PendingIntent就会在触发时按顺序打开Activity，从而做到从新闻详情返回到首页的需求：
```Java
final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
builder.setAutoCancel(true).setContentTitle("Open One").setContentText(
        "Click Open One Activity").setSmallIcon(R.mipmap.ic_launcher).setContentIntent(
        PendingIntent.getActivities(mContext, 0, new Intent[]{new Intent(mContext,
                        ConstraintLayoutActivity.class).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK), new Intent(mContext,
                        GameActivity.class),
                new Intent(mContext,
                        OpenGLActivity.class)},
                PendingIntent.FLAG_UPDATE_CURRENT)).setChannelId("testChannel");
notificationManager.notify(NOTIFICATION_ID, builder.build());
```
其中第一个Intent因为众所周知的原因必须带有Flag：`Intent.FLAG_ACTIVITY_NEW_TASK`，后面的Intent便不是必须的，最后一个Intent的Activity会被作为primary key，整个行为和startActivities一致。

### 0x83 TaskStackBuilder
除了使用多Intent来开启多个Activity，我们还可以使用TaskStackBuilder，其中v4包中有做了相应兼容的实现的TaskStackBuilder可以使用。仔细阅读TaskStackBuilder的源码，代码不多，其实它也支持startActivities方法，获取PendingIntent时也就是做了一些封装，v4包里的代码因为考虑到更好的兼容性所以在使用API时有更全面的检查，这其实是我们编写代码所应学习的地方，写健壮鲁棒的代码是编码猴子的基本素养。

我们来简单看下TaskStackBuilder特有的用法，构建新的符合要求的任务栈，并确保满足API 11 之后的不跨任务回退行为。首先需要在Manifest文件创建目标Activity的ParentActivity：
```XML
<activity android:name=".activity.OpenGLActivity"
        android:parentActivityName=".activity.ConstraintLayoutActivity"/>
```
然后构建任务栈，并获得PendingIntent设置给Notification：
```Java
final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext);
taskStackBuilder.addNextIntentWithParentStack(new Intent(mContext, OpenGLActivity.class));
builder.setAutoCancel(true).setContentTitle("Open One").setContentText(
        "Click Open Task Activity").setSmallIcon(R.mipmap.ic_launcher).setContentIntent(
        taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT))
        .setChannelId("testChannel");
notificationManager.notify(NOTIFICATION_ID, builder.build())
```

关于启动多个Activity内容就这么多，其实使用TaskStackBuilder是最省时省力的，Google推出的众多支持工具和向后兼容的支持库真的为我们开发带来了很大的方便，所以说AOSP和Support Source其实是最值得学习的Android开发资料。
