# ConcurrentLinkedQueue

> 参考:[死磕JAVA集合之ConcurrentLinkedQueue源码分析](https://github.com/alan-tang-tt/yuan/blob/master/%E6%AD%BB%E7%A3%95%20java%E9%9B%86%E5%90%88%E7%B3%BB%E5%88%97/22.%E6%AD%BB%E7%A3%95%20java%E9%9B%86%E5%90%88%E4%B9%8BConcurrentLinkedQueue%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.md)
>
> `ConcurrentLinkedQueue`只实现了`Queue`接口并没有实现`BlockingQueue`接口，所以它不是阻塞队列，也不能用于线程池中，但是它是线程安全的，可用于多线程环境中

## 总结

- `ConcurrentLinkedQueue`不是阻塞队列，所以不能用在线程池中
- `ConcurrentLinkedQueue`使用`CAS+自旋`更新头尾节点控制出队入队操作
- `ConcurrentLinkedQueue`与`LinkedBlockingQueue`对比
  - 两者都是线程安全的队列
  - 两者都可以实现取元素时队列为空直接返回`null`，后者的`poll()`方法可以实现此功能
  - 前者全程无锁(效率较高)，后者全部都是使用重入锁(效率较低)控制的
  - 前者无法实现如果队列为空等待元素到来的操作，即前者是非阻塞队列，后者是阻塞队列