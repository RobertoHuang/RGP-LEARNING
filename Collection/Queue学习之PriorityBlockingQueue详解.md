# PriorityBlockingQueue

> 参考:[死磕JAVA集合之PriorityBlockingQueue源码分析](https://github.com/alan-tang-tt/yuan/blob/master/%E6%AD%BB%E7%A3%95%20java%E9%9B%86%E5%90%88%E7%B3%BB%E5%88%97/20.%E6%AD%BB%E7%A3%95%20java%E9%9B%86%E5%90%88%E4%B9%8BPriorityBlockingQueue%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.md)
>
> `PriorityBlockingQueue`是`JAVA`并发包下的优先级阻塞队列，它是线程安全的

## 总结

- `PriorityBlockingQueue`整个入队出队的过程与`PriorityQueue`基本是保持一致的
- `PriorityBlockingQueue`使用可重入锁`ReentrantLock`控制并发安全
- `PriorityBlockingQueue`扩容时使用一个单独变量的`CAS`操作来控制只有一个线程进行扩容
- 入队使用自下而上的堆化，出队使用自上而下的堆化
- `PriorityBlockingQueue`使用`notEmpty`条件实现阻塞效果