# Cache

> `Guava Cache`构建本地缓存
>
> 本地缓存作用就是提高系统的运行速度，是一种空间换时间的取舍

## 淘汰策略

> 当缓存数量逼近或大于我们所设置的最大容量时，为了将缓存数量控制在我们所设定的阈值内就需要丢弃掉一些数据。由于缓存的最大容量恒定，为了提高缓存的命中率我们需要尽量丢弃那些我们之后不再经常访问的数据，保留那些即将被访问的数据。为了达到以上目的，我们往往会制定一些缓存淘汰策略

- `FIFO(First In First Out)`先进先出:一般采用队列的方式实现。这种淘汰策略仅仅是保证了缓存数量不超过我们所设置的阈值，而完全没有考虑缓存的命中率。所以在这种策略极少被使用
- `LRU(Least Recently Used)`最近最少使用:该算法其核心思想是"如果数据最近被访问过，那么将来被访问的几率也更高"，该算法是淘汰最后一次使用时间离当前最久的缓存数据保留最近访问的数据。所以该种算法非常适合缓存热点数据。但是该算法在缓存周期性数据时就会出现缓存污染，也就是淘汰了即将访问的数据反而把不常用的数据读取到缓存中【`LinkedHashMap`默认实现了`LRU`算法】
- `LFU(Least Frequently Used)`最不经常使用:该算法的核心思想是"如果数据在以前被访问的次数最多，那么将来被访问几率就会更高"。所以该算法淘汰的是历史访问次数最少数据，一般情况下`LFU`效率要优于`LRU`且能够避免周期性或者偶发性的操作导致缓存命中率下降的问题。但`LFU`需要记录数据的历史访问记录，一旦数据访问模式改变，`LFU`需要更长时间来适用新的访问模式，即`LFU`存在历史数据影响将来数据的"缓存污染"

在`Guava`中默认使用`LRU`淘汰算法，而且在不修改源码的情况下也不支持自定义淘汰算法

## LoadingCache

```java
CacheBuilder.newBuilder()
        .build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                // 缓存加载逻辑
                // load方法返回空值是会报错 可以用Optional包裹一层
            }
        });
// 获取缓存，当缓存不存在时会通过CacheLoader自动加载，该方法会抛出ExecutionException异常
loadingCache.get("k1");
// 以不安全的方式获取缓存，当缓存不存在时会通过CacheLoader自动加载，该方法不会抛出异常
loadingCache.getUnchecked("k1");
```

## 缓存的并发级别

`Guava`提供了设置并发级别的`API`使得缓存支持并发的写入和读取。同`ConcurrentHashMap`类似`Guava Cache`的并发也是通过分离锁实现。在一般情况下将并发级别设置为服务器`CPU`核心数是一个比较不错的选择

```java
CacheBuilder.newBuilder()
        // 设置并发级别为cpu核心数
        .concurrencyLevel(Runtime.getRuntime().availableProcessors()) .build();
```

## 缓存的初始容量

我们在构建缓存时可以为缓存设置一个合理大小初始容量，由于`Guava`的缓存使用了分离锁的机制，扩容的代价非常昂贵。所以合理的初始容量能够减少缓存容器的扩容次数

```java
CacheBuilder.newBuilder()
        // 设置初始容量为100
        .initialCapacity(100).build();
```

## 缓存过期和刷新策略

- 过期

  ```java
  // 写入多长时间后过期
  .expireAfterWrite(10, TimeUnit.MINUTES)
  // 多长时间没访问后过期
  .expireAfterAccess(10, TimeUnit.MINUTES)
  ```

- 刷新

  ```java
  // 设置缓存在写入多久后通过CacheLoader的load方法进行刷新
  .refreshAfterWrite(10, TimeUnit.SECONDS)
  ```

## 数据变化回调监听

```java
LoadingCache<String, String> cache = CacheBuilder.newBuilder()
        // 数据变化回调监听
        .removalListener((RemovalNotification<String, String> notification) -> {
            if (notification.wasEvicted()) {
                String key = notification.getKey();
                String value = notification.getValue();
                RemovalCause cause = notification.getCause();
            }
        }).build(loader);
```

使用`CacheBuilder`构建的`Cahe`不会"自动"执行清理数据，或者在数据过期后立即执行清除操作

它在写操作期间或偶尔读操作期间执行少量维护，这样做的原因是:如果我们想要连续地执行缓存维护，我们需要创建一个线程，它的操作将与共享锁的用户操作发生竞争，此外一些环境限制线程的创建，这会使`CacheBuilder`在该环境中不可用

## 缓存命中率

```java
// 开启缓存统计
CacheBuilder.newBuilder().recordStats().build();

// 通过stats获取缓存命中率相关信息
CacheStats stats = cache.stats();
```

## 缓存回收

### 基于数量/容量的回收

基于最大数量的回收

```java
CacheBuilder.newBuilder().maximumSize(10).build()
```

使用基于最大容量的回收策略时，需要制定两个参数

```java
CacheBuilder.newBuilder()
        .maximumWeight(1024 * 1024 * 1024)
        .weigher((Weigher<String, String>) (key, value) -> key.getBytes().length + value.getBytes().length).build()
```

缓存的最大数量/容量逼近或超过我们所设置的最大值时，`Guava`就会使用`LRU`算法对之前的缓存进行回收

### 基于软/弱引用的回收

> 基于引用的回收策略，是`JAVA`中独有的
>
> 在`Guava Cache`中支持软/弱引用的缓存回收方式
>
> ```java
> CacheBuilder.newBuilder()
>         // 使用弱引用存储键，当键没有其它(强或软)引用时，该缓存可能会被回收
>         .weakKeys()
>         // 使用弱引用存储值，当值没有其它(强或软)引用时，该缓存可能会被回收
>         .weakValues()
>         // 使用软引用存储值，当内存不足并且该值其它强引用引用时，该缓存就会被回收
>         .softValues().build()
> ```
>
> 通过软/弱引用的回收方式相当于将缓存回收任务交给了`GC`
>
> 使得缓存的命中率变得十分的不稳定，在非必要的情况下，还是推荐基于数量和容量的回收

#### 强引用

强引用是使用最普遍的引用。如果一个对象具有强引用，那垃圾回收器绝不会回收它

```java
Object o=new Object();
```

#### 软引用

相对于强引用软引用是一种不稳定的引用方式，如果一个对象具有软引用，当内存充足时`GC`不会主动回收软引用对象，而当内存不足时软引用对象就会被回收。软引用照样会引起内存溢出异常，因为通知虚拟机执行`GC`并不能保证马上执行，内存并不会马上释放，若在这段时间内内存持续增长则会发生内存溢出

```java
SoftReference<Object> softReference=new SoftReference<Object>(new Object());
```

#### 弱引用

弱引用是一种比软引用更不稳定的引用方式，因为无论内存是否充足弱引用对象都有可能被回收

```java
WeakReference<Object> weakReference = new WeakReference<Object>(new Object());
```

#### 虚引用

而虚引用这种引用方式就是形同虚设，因为如果一个对象仅持有虚引用，那么它就和没有任何引用一样

```java
PhantomReference<Object> phantomRef = new PhantomReference<Object>(new Object(), queue);
```

虚引用在实践中几乎没有使用，它可以用作类被回收后对相关资源进行回收工作，可参考`FileCleaningTracker`



