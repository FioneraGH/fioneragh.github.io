---
title: Retrofit2 Converter的使用
date: 2017-04-20 18:05:28
tags: [Retrofit,Converter]
---

### 0x80 前言
今天来说一个比较简单的东西——Converter。看它的名字就知道它是转换器的意思，相信很多人都用过GsonConverter这个库，它的内容很简单并且它一直跟着Retrofit2主库的更新而更新。今天在重构代码的时候遇到了这样一个问题，我需要拿到返回内容的原始字符串而不是经过转换的POJO类，而GsonConverter没有办法做到，于是自己为了节省时间就做了点修改。

### 0x81 GsonConverter 的使用
用法很简单，先在build.gradle文件中引用该库：
```Groovy
compile 'com.squareup.retrofit2:retrofit:2.2.0'
compile 'com.squareup.retrofit2:converter-gson:2.2.0'
compile 'com.squareup.retrofit2:adapter-rxjava2:2.2.0'
```
这样我们就可以在RetrofitBuilder构造Retrofit实例时添加相应的功能：
```Java
ApiService apiService = new Retrofit.Builder().client(okHttpBuilder.build())
    .baseUrl(HttpConstants.BASE_URL)
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
    .build().create(ApiService.class);
```
上面代码我们为Retrofit添加了RxJava2的适配器以及使用Gson处理字符串的Converter。

### 0x82 ApiService
我们看下一般情况下的ApiService如何写，首先来看一个GET请求：
```Java
@GET(HttpConstants.HOME_PAGE)
Observable<BaseEntity<HomeBean>> getHomePage(@Query("time") String time);
```
这个请求很简单，@GET指明了在那个rest-path上执行GET请求，有一个查询参数time，接口方法的返回值是一个Observable，经过GsonConverter转换后的内容是BaseEntity<HomeBean>，我们还可以写过滤器取出HomeBean从而让代码更简洁。

再来看一个发送Json数据的POST请求：
```Java
@POST(HttpConstants.CART_ADD)
Observable<BaseEntity<Empty>> addToCart(@Body Map<String, String> info);
```
这个和上面的GET差不多，只不过注解变成了@POST，参数是@Body，返回值是然被转换成POJO，泛型中的Empty表示我不在意返回内容是什么，我只需要通过返回的规约code进行判断。这一切都工作的很好，知道我发现了有的接口的返回值是没办法通过Gson转换成POJO的，这时候我们需要原始的响应结果。

### 0x83 如何更好的处理返回结果
最开始的时候我在想，GsonConverter也是根据Type做的转换，那我使用Observable<String>作为返回值是不是就可以拿到原始的原始响应的字符串，后来发现是我想多了。

于是我用了一个比较笨的方法，我维护了两个Retrofit的实例，一个叫ApiInstance，另一个叫NoGsonApiInstance。即便是这样我们仍然不能使用Observable<String>作为返回值，除非你实现了类似的StringConverter，最省事的我用了Observable<ResponseBody>这一最原始的用法：
```Java
@GET(HttpConstants.GOODS_DETAIL)
Observable<ResponseBody> getGoodsDetail(
            @Query("goods_commonid") String goods_commonid,
            @Query("first_goods_id") String first_goods_id,
            @Query("key") String key);
```
依然是一个GET请求，这个返回的结果中由于有一些自定义的附加字符导致它不是一个合适的Json字符串，因此没办法使用GsonConverter，所以这样请求成功拿到了ResponseBody，我们可以做一些自定义处理。

到这这一切看起来都很顺利，直到我用原来的方式发送一个POST请求时，Logcat中明确的打印没有一个合适的Converter将Map型的@Body转换成Json字符串，我才意识到问题，因为之前一直是用FormBody的@Field参数向服务器POST数据，没有意识到Map类型的参数GsonConverter参与了转换。

### 0x84 如何更好的处理请求体
形如下面这种请求，我们需要向服务器递交Json串，还想要直接使用ResponseBody该怎么处理：
```Java
@POST(HttpConstants.ORDER_PREPAY)
Observable<ResponseBody> orderPrepay(@Body Map<String, String> info);
```
既然知道了使用GsonConverter时能转换成功，那说明GsonConverter肯定做了处理，我们看一下GsonConverterFactory的源码（由于我的Studio不会自己下载并关联源码了，就先将就着看Decompiler的代码吧，反正Java不经过混淆的话直接反编译也不怎么影响阅读）：
```Java
public final class GsonConverterFactory extends Factory {
    private final Gson gson;

    public static GsonConverterFactory create() {
        return create(new Gson());
    }

    public static GsonConverterFactory create(Gson gson) {
        return new GsonConverterFactory(gson);
    }

    private GsonConverterFactory(Gson gson) {
        if(gson == null) {
            throw new NullPointerException("gson == null");
        } else {
            this.gson = gson;
        }
    }

    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = this.gson.getAdapter(TypeToken.get(type));
        return new GsonResponseBodyConverter(this.gson, adapter);
    }

    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = this.gson.getAdapter(TypeToken.get(type));
        return new GsonRequestBodyConverter(this.gson, adapter);
    }
}
```
GsonConverterFactory的代码不多，我们能很直观的看到，确实有两个Converter，一个Response的，一个Request的，看Converter.Factory的源码中其实还有一个stringConverter的方法，用于将请求参数转成字符串。

看到这，不需要看两个Converter的源码我们也知道了，的确是GsonConverter可能有能力将Map转成对应的RequestBody，所以我便有了一下小trick哈哈。
```Java
public class RetrofitStringConverter extends Factory {

    public static RetrofitStringConverter create() {
        return new RetrofitStringConverter();
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations, Retrofit retrofit) {
        return GsonConverterFactory.create().requestBodyConverter(type, parameterAnnotations,
                methodAnnotations, retrofit);
    }
}
```
虽然可能不太优雅，但是目前用起来还没有什么副作用，就是自定义了一个Factory，代码很简单也很容易看懂就不啰嗦了。
> 多说一句，不重写对应的方法在父类中是返回null的，根据文档如果返回的Converter是null表明它不具有处理的能力，也就是说不由它处理，如果没有其他的Converter那就是默认的返回方式ResponseBody。
