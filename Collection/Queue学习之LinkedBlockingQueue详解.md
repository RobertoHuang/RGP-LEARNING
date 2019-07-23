# LinkedBlockingQueue

> 参考:[死磕JAVA集合之LinkedBlockingQueue源码分析](https://github.com/alan-tang-tt/yuan/blob/master/%E6%AD%BB%E7%A3%95%20java%E9%9B%86%E5%90%88%E7%B3%BB%E5%88%97/18.%E6%AD%BB%E7%A3%95%20java%E9%9B%86%E5%90%88%E4%B9%8BLinkedBlockingQueue%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.md)
>
> `LinkedBlockingQueue`是`JAVA`并发包下一个以单链表实现的阻塞队列，它是线程安全的

![LinkedBlockingQueue数据结构](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Collection/images/LinkedBlockingQueue%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84.png)

`LinkedBlockingQueue`头结点和尾结点一开始总是指向一个哨兵的结点，它不持有实际数据，当队列中有数据时头结点仍然指向这个哨兵，尾结点指向有效数据的最后一个结点。这样做的好处在于与计数器`count`结合后对队头、队尾的访问可以独立进行，而不需要判断头结点与尾结点的关系

## 总结

- `LinkedBlockingQueue`采用单链表的形式实现
- `LinkedBlockingQueue`采用两把锁的锁分离技术实现入队出队互不阻塞
- `LinkedBlockingQueue`是有界队列，不传入容量时默认为最大`int`值

