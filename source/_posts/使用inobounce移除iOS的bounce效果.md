---
title: 使用inobounce移除iOS的bounce效果
date: 2017-11-16 18:11:30
tags: [Vue.js, inobounce]
---

### 0x81 问题来源

iOS的浏览器有个让开发人员非常诟病的地方--Bounce效果,从用户的角度讲,这个效果的观感对用户来说体验确实还不错,但是会出现页面整体滚走的状况,导致页面内的滚动失效.

### 0x82 inobounce

inobounce是一个解决这种弹簧效果的库,这个库的代码很简单,进行的工作也很纯,就是判断在满足某些条件的时候,监听'touchmove'事件并阻止该滚动事件的发生.

inobounce.js:

```Javascript
// Enable by default if the browser supports -webkit-overflow-scrolling
// Test this by setting the property with JavaScript on an element that exists in the DOM
// Then, see if the property is reflected in the computed style
var testDiv = document.createElement('div');
document.documentElement.appendChild(testDiv);
testDiv.style.WebkitOverflowScrolling = 'touch';
var scrollSupport = 'getComputedStyle' in window && window.getComputedStyle(testDiv)['-webkit-overflow-scrolling'] === 'touch';
document.documentElement.removeChild(testDiv);

if (scrollSupport) {
    enable();
}
```

上面的代码主要创建了一个testDiv去验证浏览器是否支持'-webkit-overflow-scrolling=touch'这一特性,如果支持则启用事件监听:

```Javascript
var enable = function() {
    // Listen to a couple key touch events
    window.addEventListener('touchstart', handleTouchstart, false);
    window.addEventListener('touchmove', handleTouchmove, false);
    enabled = true;
};
```

handleTouchmove方法就是监听事件的具体处理方法:

```Javascript
var scrolling = style.getPropertyValue('-webkit-overflow-scrolling');
var overflowY = style.getPropertyValue('overflow-y');
var height = parseInt(style.getPropertyValue('height'), 10);

// Determine if the element should scroll
var isScrollable = scrolling === 'touch' && (overflowY === 'auto' || overflowY === 'scroll');
var canScroll = el.scrollHeight > el.offsetHeight;

if (isScrollable && canScroll) {
    // Get the current Y position of the touch
    var curY = evt.touches ? evt.touches[0].screenY : evt.screenY;

    // Determine if the user is trying to scroll past the top or bottom
    // In this case, the window will bounce, so we have to prevent scrolling completely
    var isAtTop = (startY <= curY && el.scrollTop === 0);
    var isAtBottom = (startY >= curY && el.scrollHeight - el.scrollTop === height);

    // Stop a bounce bug when at the bottom or top of the scrollable element
    if (isAtTop || isAtBottom) {
        evt.preventDefault()
    }
}
```

代码也很简单,去除DOM元素的属性判断它是能滚动的,然后计算它是否在顶部或底部,如果满足条件就禁止原本的事件,如此递归地处理所有节点便完成了禁止滚动的作用.

### 0x83 缺陷与不足
inobounce能解决大部分状况,但是有的时候仍然会出现失败的情况,目前还没有更好的解决办法.但是使用了inobounce后又一个不足,就是webkit内核的'-webkit-overflow-scrolling'特性在处于文档顶和尾的时候都会失效,比如我们有个单页应用,它始终是填满viewport的,这时候页面内有个横向滑动的tab,当inobounce生效时,它将失去滑动的能力,解决办法就是为节点添加标记属性并忽略它们:

```Javascript
// ignore data-scroll
if(el.getAttribute('data-scroll') !== null){
  return
}
```

前端适配的问题非常的多,当你在浏览器中行为正常,在移动端微信内嵌的WebView当中很可能就适配不好,要不断的调试和调整,微信端的问题还远不止这些,像Android上很多WebView版本过低不支持一些特性则更是让人头疼.
