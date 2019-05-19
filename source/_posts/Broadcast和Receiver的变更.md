---
title: Broadcast和Receiver的变更
date: 2019-04-23 13:27:08
tags: [Android,BroadcastReceiver]
---

### 0x81 后台限制变更

从Android Nougat开始，Google开始逐渐收紧后台运行权限，到Oreo几乎禁掉了所有匿名广播。

广播的唤起进程确实非常拖慢设备的运行速度，甚至由于大量App被唤起占用CPU资源会让很多人觉得手机卡顿，所以Google终于开始限制各大App对这些系统资源的滥用。其实从另一方面讲，这些限制的出现也意味着很多功能成了系统软件的专属，用户软件相当于失去了对应的能力，当然Google其实也考虑到这些事情，提出了很多方案来弥补这些不足，也对一些功能放宽限制来保证App在前台可以正常使用。Lolipop引入的JobScheduler以及最新的Jetpack组件WorkManager都是不错的替代方案。

### 0x82 Nougat的变更

Nougat的变更算是一次试水，主要砍掉了两个广播并削弱了一个广播的能力。

`ACTION_NEW_VIDEO`和`ACTION_NEW_PICTURE`两个广播被完全砍掉，即使你的App的Target级别不是24+，只要运行在Nougat设备上，就无法再接收这两个广播，如果想做类似的功能可以利用JobScheduler对ContentUri创建的相应的Job，待收到相应的Uri，相应的JobService会被系统适时完成你的逻辑。

`CONNECTIVITY_ACTION`便是被削弱的广播，这个广播非常常用，我们通常用它来处理我们的App在Wifi和Cellar下的行为，静态注册的广播接收器将不再工作：

```Xml
<receiver android:name=".nougat.receiver.ManifestConnectivityReceiver">
    <intent-filter>
        <action
            android:name="android.net.conn.CONNECTIVITY_CHANGE"
            tools:ignore="BatteryLife" />
    </intent-filter>
</receiver>
```

如果我们真的想接受网络变化怎么办，Google考虑到这个广播的易用性，保留了动态注册的能力，这意味着只要我们是通过上下文注册的，我们仍然能收到广播及其信息，并且这个广播是粘性的，我们可以依赖它作为当前的网络状态：

```Kotlin
val intentFilter = IntentFilter()
intentFilter.addAction(ImplicitAction.CONNECTIVITY_ACTION)
registerReceiver(registerConnectivityReceiver, intentFilter)
```

当然，实际上Google建议我们用更加细粒度的控制，而不是真的依赖沉重的广播组件，毕竟这个广播已被弱化，并根据路线图以后很可能被直接砍掉，因此我们可以通过向ConnectivityManager注册回调的方式完整我们需求，并且他能做到的不仅仅是Wifi这种无线连接，像是蓝牙网络等等都可以：

```Kotlin
private val connCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network?) {
        println("Connectivity Callback Available: ${network?.toString()}")
    }

    override fun onLost(network: Network?) {
        println("Connectivity Callback Lost: ${network?.toString()}")
    }

    override fun onCapabilitiesChanged(network: Network?, networkCapabilities: NetworkCapabilities?) {
        println("Connectivity Callback Changed: ${network?.toString()} / ${networkCapabilities?.toString()}")
    }
}
val connManger = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
connManger.registerNetworkCallback(
        NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(),
        connCallback)
```

当然，如果对实时性不是那么依赖，例如我们只是想在某些情况下帮用户做一些辅助任务，提高易用性，还是建议使用JobScheduler，更加内存友好、电池友好：

```Kotlin
val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
val job = JobInfo.Builder(CONNECTIVITY_JOB_ID, ComponentName(this, ConnectivityJobService::class.java))
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .build()
jobScheduler.schedule(job)
```

JobService相关不在这里展开，Google文档描述的非常详细，并且有很丰富的样例。

### 0x83 Oreo的变更

Oreo做了更加激进的变更，甚至大量变更是忽视掉Target级别的，只要运行在Oreo设备上，它就生效，当然这里关注的还是广播的问题。

首先，

* Apps that are running in the background now have limits on how freely they can access background services.

* Apps cannot use their manifests to register for most implicit broadcasts (that is, broadcasts that are not targeted specifically at the app).

其中第二条，清单再也不能注册大部分的匿名广播（未明确指定），这是一个相当大的变更，可以看出是紧随Nougat来的。当然他们默认是指Target级别达到26+，但是Oreo放开了用户设置，用户可以强制限制。

对于系统广播，都是匿名广播，基本上可以认为静态注册都不再工作（除了极少数和带指定信息的），我们用自定义的注册在清单中的广播接收器做个实验，一共三种情况——发送匿名Action、发送带PackageName的匿名Action和显式发送：

```Xml
<receiver android:name=".oreo.receiver.ManifestStaticReceiver"
            android:exported="false">
    <intent-filter>
        <action android:name="action.STATIC"/>
    </intent-filter>
</receiver>
```

```Kotlin
class ManifestStaticReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val msg = "Manifest Static Received" +
                ": ${intent?.action} " +
                "/ ${intent?.`package`} " +
                "/ ${intent?.component}}"
        context?.showToast(msg)
        println(msg)
    }
}

btn_static_send.setOnClickListener {
    val intent = Intent()
    intent.action = Actions.STATIC
    sendBroadcast(intent)
}

btn_static_send_with_pkg.setOnClickListener {
    val intent = Intent()
    intent.action = Actions.STATIC
    intent.setPackage(packageName)
    sendBroadcast(intent)
}

btn_static_send_explicit.setOnClickListener {
    val intent = Intent()
    intent.setClass(this@MainActivity, ManifestStaticReceiver::class.java)
    sendBroadcast(intent)
}
```

上述三个广播发送，只有第二个和第三个有结果：

```Console
Manifest Static Received: action.STATIC / com.fionera.receiverchanges / ComponentInfo{com.fionera.receiverchanges/com.fionera.receiverchanges.oreo.receiver.ManifestStaticReceiver}}
Manifest Static Received: null / null / ComponentInfo{com.fionera.receiverchanges/com.fionera.receiverchanges.oreo.receiver.ManifestStaticReceiver}}
```

很明显，我们自定义的纯匿名广播在静态注册时也会失效，解决方法只有两个——指定目标和显式指定组件，而动态注册仍可以正常响应匿名广播，但是在我的测试里显式广播是失效的。

当然了，更加鲁棒的解决方案依然是JobScheduler。

由于Oreo砍掉了几乎所有能砍掉的匿名广播，所以Pie相对不这一部分没有大的变更，如今各大厂商商店开始陆续要求Target提升到26+，是该向GooglePlay看齐规范规范了。
