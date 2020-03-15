# ThreadLocal

- `ThreadLocal`是什么

    `ThreadLocal`类并不是用来解决多线程环境下的共享变量问题

    而是用来提供线程内部的共享变量，在多线程环境下可以保证各个线程之间的变量互相隔离、相互独立

- `TreadLocal`如何保证各个线程的数据互不干扰

    每个线程都有一个`ThreadLocalMap`类型的类成员变量

    `TreadLocal`使用的是`threadLocals`、`InheritableThreadLocal`则是使用到了`inheritableThreadLocals`

- `ThreadLocalMap`解决`hash`冲突

    与`HashMap`不同，`ThreadLocalMap`结构非常简单，没有`next`引用

    也就是说`ThreadLocalMap`中解决`Hash`冲突的方式并非链表的方式，而是采用线性探测的方式，所谓线性探测就是根据初始`key`的`hashcode`值确定元素在`table`数组中的位置，如果发现这个位置上已经被其他的`key`值占用，则利用固定的算法寻找一定步长的下个位置，依次判断，直至找到能够存放的位置   

- `ThreadLocalMap`为什么要使用弱引用

    `key`使用强引用:引用的`ThreadLocal`的对象被回收了，但`ThreadLocalMap`还持有`ThreadLocal`的强引用，如果没有手动删除`ThreadLocal`不会被回收，则会导致内存泄漏

    `key`使用弱引用:当使用`ThreadLocal`保存一个`value`时，会在`ThreadLocalMap`中的数组插入一个`Entry`对象，按理说`key-value`都应该以强引用保存在`Entry`对象中，但在`ThreadLocalMap`的实现中，`key`被保存到了`WeakReference`对象中。这就导致了一个问题，`ThreadLocal`在没有外部强引用时，发生`GC`时会被回收如果创建`ThreadLocal`的线程一直持续运行，那么这个`Entry`对象中的`value`就有可能一直得不到回收，发生内存泄露

    比较以上两种情况我们可以发现:由于`ThreadLocalMap`的生命周期跟`Thread`一样长，如果都没有手动删除对应`key`都会导致内存泄漏，但是使用弱引用可以多一层保障，弱引用`ThreadLocal`不会内存泄漏，对应`value`在下一次`ThreadLocalMap`调用`set`、`get`、`remove`的时候被清除，算是最优的解决方案

- 如何防止`ThreadLocal`内存泄漏

    【记得显式调用`ThreadLocal`的`remove()`方法】

    将`ThreadLocal`变量定义成`private static`的

    这样的话`ThreadLocal`的生命周期就更长，由于一直存在`ThreadLocal`的强引用，所以`ThreadLocal`也就不会被回收，也就能保证任何时候都能根据`ThreadLocal`的弱引用访问到`Entry`的`value`值，然后`remove`它  

- `Netty`的`FastThreadLocal`为什么更快并且不会内存泄漏

    【速度更快】

    `FastThreadLocal`是每一个`FastThreadLocalThread`线程绑定了一个`Object[]`数组来存储数据

    `FastThreadLocal`内部存储了一个索引值，利用该索引直接在数组中定位所存储的元素，相比于`HashMap`用`hash`值定位而且还得解决冲突、扩容等的确要更快一些

    【不会内存泄漏】

     `FastThreadLocal`使用`ObjectCleaner`来清理软引用被`GC`后，清理失效的`FastThreadLocal`，原理是在构建软引用`key`的时候多传递个`queue`参数，在软引用被`GC`的时候会将软应用对象放到这个`queue`中，到时候消费这个`queue`就可以获取被`gc`掉的软引用对象，实现资源清理

