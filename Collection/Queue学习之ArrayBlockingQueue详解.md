# ArrayBlockingQueue

> `ArrayBlockingQueue`是`JAVA`并发包下一个以数组实现的阻塞队列，它是线程安全的

## BlockingQueue

`BlockingQueue`是所有阻塞队列的顶级接口

| 操作 | 抛出异常  |    返回特定值    |  阻塞  |          超时           |
| :--: | :-------: | :--------------: | :----: | :---------------------: |
| 入队 |  add(e)   | offer(e) - false | put(e) | offer(e, timeout, unit) |
| 出队 | remove(e) |  poll() - null   | take() |   poll(timeout, unit)   |
| 检查 | element() |  peek() - null   |   -    |            -            |

## 总结

- `ArrayBlockingQueue`是线程安全的，利用重入锁和两个条件保证并发安全

- `ArrayBlockingQueue`不需要扩容，因为是初始化时指定容量，并循环利用数组

- `ArrayBlockingQueue`利用`takeIndex`和`putIndex`循环利用数组

- `ArrayBlockingQueue`有哪些缺点呢

  - 队列长度固定且必须在初始化时指定，所以使用之前一定要慎重考虑好容量
  - 如果消费速度跟不上入队速度则会导致提供者线程一直阻塞，且越阻塞越多非常危险
  - 只使用了一个锁来控制入队出队，效率较低。可以借鉴分段的思想把入队和出队分裂成两个锁

  