# SynchronousQueue

> 参考:[死磕JAVA集合之SynchronousQueue源码分析](https://github.com/alan-tang-tt/yuan/blob/master/%E6%AD%BB%E7%A3%95%20java%E9%9B%86%E5%90%88%E7%B3%BB%E5%88%97/19.%E6%AD%BB%E7%A3%95%20java%E9%9B%86%E5%90%88%E4%B9%8BSynchronousQueue%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.md)
>
> `SynchronousQueue`是`JAVA`并发包下无缓冲阻塞队列，它用来在两个线程之间移交元素

## 总结

- 默认使用非公平模式，也就是栈结构(默认使用非公平模式，也就是栈结构)

- 栈方式中的节点有三种模式:生产者、消费者、正在匹配中

- 栈方式的大致思路是如果栈顶元素跟自己一样的模式就入栈并等待被匹配，否则就匹配，匹配到了就返回

- `SynchronousQueue`真的是无缓冲的队列吗

  通过源码分析可以发现其实`SynchronousQueue`内部或者使用栈或者使用队列来存储包含线程和元素值的节点，如果同一个模式的节点过多的话它们都会存储进来且都会阻塞着，所以严格上来说`SynchronousQueue`并不能算是一个无缓冲队列

- `SynchronousQueue`有什么缺点呢

  试想一下如果有多个生产者但只有一个消费者，如果消费者处理不过来，是不是生产者都会阻塞起来？反之亦然。这是一件很危险的事，所以`SynchronousQueue`一般用于生产、消费的速度大致相当的情况，这样才不会导致系统中过多的线程处于阻塞状态