# DelayQueue

> 参考:[死磕JAVA集合之DelayQueue源码分析](https://github.com/alan-tang-tt/yuan/blob/master/%E6%AD%BB%E7%A3%95%20java%E9%9B%86%E5%90%88%E7%B3%BB%E5%88%97/23.%E6%AD%BB%E7%A3%95%20java%E9%9B%86%E5%90%88%E4%B9%8BDelayQueue%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.md)
>
> `DelayQueue`是`JAVA`并发包下的延时阻塞队列，常用于实现定时任务

- `DelayQueue`是阻塞队列
- `DelayQueue`内部结构使用优先级队列
- `DelayQueue`使用重入锁和条件来控制并发安全

