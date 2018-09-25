---
title: 使用Vuex的namespace管理状态
date: 2018-01-25 20:37:29
tags: [Vue,Vuex]
---

### 0x81 Vue组件通信

Vue在升级到2.x之后,仍然提供了比较灵活的组件间通信机制,父向子组件的prop[broadcast],子向父组件的emit冒泡,但是如果我想子组件与子组件通信甚至是跨多组件通信,用这种关联的机制虽然能够做到,但是却极为麻烦.在这种情况下,我们可以使用Vue官方推荐的状态管理工具Vuex来统一控制整个SPA的状态.

### 0x82 Store

Vuex和Redux一样,是针对js的一种全局状态管理,他们都扮演状态控制中心的角色.Vuex使用action作为状态通知的唯一触发者,mutation作为状态的接受者,state则存储了我们需要管理的SPA的状态,通常情况下我们也会使用types来规约action.

在平时的的开发过程中,为了保证各模块或各功能的相对低耦合,我们会把有关的actions/mutations/state组合起来作为一个store,供给vuex进行统一管理.

<!--more-->

### 0x83 namespace

在最开始的时候,由于自己编写的小项目内容较少,大部分state都是全局状态下的state,我在需要他们的地方进行获取和修改.随着项目越来越大,我便使用某种prefix作为功能模块的划分,通过js代码编写上的层次来区分各个模块,但是这样随着项目打到一定程度以及命名越来越长,这种方式越来越不符合规范,在当时那段时间我便花时间把整个Vuex部分重构,使用官方推荐的namespace来管理各模块的状态.

namespace的使用很简单,他只是一个属性,通常情况下我们的store会这样声明:

```JavaScript
// store/index.js
let store = new Vuex.Store({
  state,
  mutations,
  actions,
  modules: {
    i18n: vuexI18n.store,
    customStore: CustomStore
  }
})
```

上面声明的store包含了两个部分:全局状态和模块状态,其中state/mutations/actions是全局store,而CustomStore便是我们的模块store,如果我们不添加namespace,那modules中的store便会被合并,这不是我们想要的,我们在CustomStore中添加namespace:

```JavaScript
// store/store/customStore.js
export default {
  // here for using namespace
  namespaced: true,
  state: {
    tabIndex: 0
  },
  mutations: {
    updateTabIndex(state, payload) {
      state.tabIndex = payload.tabIndex
    }
  },
  actions: {
    updateTabIndex({ commit }, tabIndex) {
      commit('updateTabIndex', { tabIndex })
    }
  }
}
```

这样我们的customStore便会被加上命名空间,在使用时更加灵活自由.

### 0x84 使用namespace获取actions/state

全局状态下的state控制我们通常使用mapActions/mapState(mapGetters)来获取修改状态的方法和修改后的状态数据(修改后的状态数据会自动observe从而触发computed计算属性的效果),那如果我们加上了namespace之后该如何使用?

```JavaScript
computed: {
  // need be replaced to mapGetters, now delegate
  ...mapState('customStore', {
    _tabIndex: state => state.tabIndex,
    xxx: state => state.xxx
  })
},
methods: {
  ...mapActions('customStore', ['updateTabIndex', 'xxx'])
}
```

上面这种方式获取到的触发action方法和获取的状态就是在customStore下的,从而满足我们的需要.

当然,Vuex其实灵活的用法还挺多的,官方文档一如Vue.js一样完整易懂,得益于尤大大对中文文档的完善,基本上看完文档那Vuex的用法都就明了了.
