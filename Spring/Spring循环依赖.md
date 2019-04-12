# 循环依赖

> 循环依赖就是循环引用(两个或多个`Bean`相互之间的持有对方)，比如`CircleA`引用`CircleB`，`CircleB`引用`CircleC`，`CircleC`引用`CircleA`则它们最终反映为一个环【循环依赖非循环调用】

## 如何检测循环依赖

对于单例来说在`Spring`容器整个生命周期内有且只有一个对象，所以很容易想到这个对象应该存在`Cache`中，`Spring`为了解决单例的循环依赖问题，使用了三级缓存

```java
/** Cache of singleton objects: bean name --> bean instance */
private final Map<String, Object> singletonObjects // 单例对象的Cache 

/** Cache of singleton factories: bean name --> ObjectFactory */
private final Map<String, ObjectFactory<?>> singletonFactories // 单例对象工厂Cache

/** Cache of early singleton objects: bean name --> bean instance */
private final Map<String, Object> earlySingletonObjects // 提前曝光的单例对象的Cache
```

`Spring`创建单例对象代码`DefaultSingletonBeanRegistry#getSingleton`

- 先从`singletonObjects`缓存中获取单例对象
- 获取不到且对象正在创建中`isSingletonCurrentlyInCreation()`，从二级缓存`earlySingletonObjects`中获取【比如`A`的构造器依赖了`B`对象所以得先去创建`B`对象， 或者在`A`的`populateBean`过程中依赖了`B`对象得先去创建`B`对象，这时的`A`就是处于创建中的状态】
- 如果还是获取不到且允许`singletonFactories`通过`getObject()`获取则从三级`singletonFactories`获取，如果获取到了则从`singletonFactories`中移除，并放入`earlySingletonObjects`中。其实也就是从三级缓存移动到了二级缓存

从上面三级缓存的分析我们可以知道，`Spring`解决循环依赖的诀窍就在于`singletonFactories`这个三级`Cache`

```java
protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
    Assert.notNull(singletonFactory, "Singleton factory must not be null");
    synchronized (this.singletonObjects) {
        if (!this.singletonObjects.containsKey(beanName)) {
            this.singletonFactories.put(beanName, singletonFactory);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }
}
```

这里就是解决循环依赖的关键，这段代码发生在`createBeanInstance`之后，也就是说单例对象此时已经被创建出来(调用了构造器)。这个对象已经被生产出来了虽然还不完美(还没有进行初始化的第二步和第三步)，但是已经能被人认出来了(根据对象引用能定位到堆中的对象)，所以`Spring`此时将这个对象提前曝光出来让大家认识，让大家使用。这样做有什么好处呢？让我们来分析一下`A`的某个`field`或者`setter`依赖了`B`的实例对象，同时`B`的某个`field`或者`setter`依赖了`A`的实例对象”这种循环依赖的情况

`A`首先完成了初始化的第一步，并且将自己提前曝光到`singletonFactories`中

此时进行初始化的第二步，发现自己依赖对象`B`，此时就尝试去`get(B)`发现`B`还没有被`create`，所以走`create`流程，`B`在初始化第一步的时候发现自己依赖了对象`A`于是尝试`get(A)`，尝试从一级缓存`singletonObjects`(没有，未初始化)，尝试二级缓存`earlySingletonObjects`(没有)，尝试从三级缓存`singletonFactories`，由于`A`通过`ObjectFactory`将自己提前曝光了，所以`B`能够通过`ObjectFactory.getObject`拿到`A`对象(虽然`A`还没有初始化完全，但是总比没有好呀)

`B`拿到`A`对象后顺利完成了初始化阶段`1、2、3`，完全初始化之后将自己放入到一级缓存`singletonObjects`中。此时返回`A`中，`A`此时能拿到`B`的对象顺利完成自己的初始化阶段`2、3`，最终`A`也完成了初始化并放入一级缓存`singletonObjects`中。知道了这个原理时候肯定就知道为啥`Spring`不能解决`A`的构造方法中依赖了`B`的实例对象，同时`B`的构造方法中依赖了`A`的实例对象这类问题了！因为加入`singletonFactories`三级缓存的前提是执行了构造器，所以构造器的循环依赖没法解决

