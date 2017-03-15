---
title: Android IPC：Messenger使用
date: 2017-03-15 18:35:35
tags: [Android,IPC,Messenger]
---

### 0x81 Messenger原理
书接前文，Messenger作为一个比较简易的IPC实现，仍然是基于Binder的机制，Android上层的跨进程通信都是通过Binder完成的。

Messenger实现了Parcelable接口，因此可以在内存之间传递。它的构造方法需要一个Handler类型的target参数，这个参数是Messenger通过send方法发送Message时的消息处理者，也是消息发送send方法的真正发送者，也是Messenger的getBinder方法返回的实质对象（`mTarget.asBinder();`），也就是说其实是这个传入的Handler自己发送消息给自己处理，有点类似多线程通信的处理方式。

虽然Handler担起了消息处理者的角色，但Messager的跨进程实际上仍是通过AIDL完成的，而Handler作为Android最为关键的消息通信者，的确可以准确高效的完成消息通信这件事情。所以Messenger还有一个构造方法，通过传入一个Binder完成构建，而这个Messenger可以认为就是另一个进程中Messenger的代理。

### 0x82 Messenger 的用法
1. 首先我们定义一个Messenger，然后让这个独立进程的Service返回一个Binder：
```Java
public Messenger messenger = new Messenger(new DealHandler());

@Nullable
@Override
public IBinder onBind(Intent intent) {
    return messenger.getBinder();
}
```

2. 然后在另一端，我们创建一个ServiceConnection用来绑定远程服务（RemoteService）：
```Java
public Messenger sMessenger;

public Messenger getsMessenger() {
    return sMessenger;
}

private ServiceConnection mServerConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        sMessenger = new Messenger(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        sMessenger = null;
    }
};
```
其中onServiceConnected回调中的IBinder就是在Service的onBind中返回的Binder，我们可以用这个Binder创建一个Messenger，这个Messenger中发的消息，Service中的Handler就可以收到并处理。

3. 发送消息
发送过程也很简单，和用Handler进行线程通信的方法差不多：
```Java
Message message = Message.obtain();
message.replyTo = clientMessenger;
message.what = type;
message.setData(bundle);
try {
    serviceMessage.send(message);
} catch (RemoteException e) {
    e.printStackTrace();
}
```
其中很关键的一句是`message.replyTo = clientMessenger;`，这一句在消息中带上了客户端的Messenger，用于服务端Service返回消息，放回消息的方法与发送无异。

### 0x83 跨进程传输数据类型
消息通信的过程比较简单，和Socket通信的过程类似，但是传输的数据类型却需要十分小心。写过AIDL进行过IPC开发的应该知道，基本类型可以直接传递，但是自定义类型却不行，如果想要传递自定义类型，必须在AIDL文件中指定parcelable类型，才能用于数据的传递。

Messenger基于AIDL，因此也必须满足这种状况。但是在我们的开发过程中，Messenger是通过BundleData的方式传递的，我们都知道Bundle是实现了Parcelable接口的，因此可以用于跨进程通信，所以我们可以通过Bundle传递数据。

但是这里有两个地方需要注意：
1. Message.obj
Android系统为Message提供了arg1（int）、arg2（int）和obj（Object）三个快捷变量用于数据传递，前两个变量一般不会有问题，但是第三个变量obj则必须实现Parcelable接口，否则就会阻断Message的marshal过程，因为Message也是实现了Parcelable接口并在传递时进行序列化的，此时若有组合类对象不能parcel就会报错，而这一点在编译阶段是不会报错的，所以还是推荐使用Bundle传递数据。

2. ClassLoader
虽然推荐使用Bundle传递数据，因为你在传输非固定类型的自定义对象列表时会要求你实现Parcelable接口，这是在编译阶段就会检查的，Lint也会给予你视觉提示，告知你必需传递可序列化对象。

但是对象经传递后到达另一个进程中，也就是接受数据的Service必然要反序列化数据，这里就有一种可能——你传递的对象类型在另一个进程的ClassLoader中是没有对应的解析对象的，这时候就会抛ClassNotFoundException这个异常。其实原因很简单，你没有用过这个类，类加载器自然不会找到这个类，所以我们反序列化数据时就很有可能需要手动指定ClassLoader：
```Java
Bundle bundle = msg.getData();
bundle.setClassLoader(DeviceArgument.class.getClassLoader());
ArrayList<DeviceArgument> body = bundle.getParcelableArrayList(DeviceControlConstant.SEND_GROUP_CONTROL_COMMAND_BODY);
```
这样Bundle在取数据就可以正确的反序列化出数据了。

Messenger的用法到这也就差不多结束了，基于Binder的IPC机制其实为我们带来了很大的方便，当然坑肯定不止这些，还要慢慢挖掘。