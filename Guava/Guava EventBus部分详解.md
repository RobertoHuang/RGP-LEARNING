# EventBus

```java
EventBus(String identifier, Executor executor, Dispatcher dispatcher, SubscriberExceptionHandler exceptionHandler) {
    this.identifier = checkNotNull(identifier);
    this.executor = checkNotNull(executor);
    this.dispatcher = checkNotNull(dispatcher);
    this.exceptionHandler = checkNotNull(exceptionHandler);
}
```

- `identifier`:`EventBus`标识符
- `executor`:事件分发过程中使用到的线程池
- `dispatcher`:是`Dispatcher`类型的子类，用来在发布事件的时候分发消息给监听者
- `exceptionHandler`:是`SubscriberExceptionHandler`类型的，它用来处理异常信息

## SubscriberRegistry

> `subscribers`是`SubscriberRegistry`类型的，它维护了所有观察者的实例信息
>
> `SubscriberRegistry`作为`EventBus`的成员变量，在`EventBus`初始化的时候会被初始化

在`SubscriberRegistry`中通过`Map`维护了【事件类型和观察者列表】的关系

```java
private final ConcurrentMap<Class<?>, CopyOnWriteArraySet<Subscriber>> subscribers = Maps.newConcurrentMap();
```

`SubscriberRegistry`中的`register()`方法与`unregister()`方法本质都是对该`Map`进行操作

```java
public void post(Object event) {
    Iterator<Subscriber> eventSubscribers = subscribers.getSubscribers(event);
    if (eventSubscribers.hasNext()) {
        dispatcher.dispatch(event, eventSubscribers);
    } else if (!(event instanceof DeadEvent)) {
        // 如果没有观察者则发送到死信队列
        post(new DeadEvent(this, event));
    }
}
```

当调用`EventBus.post()`方法的时候会先调用`SubscriberRegistry#getSubscribers`方法获取该事件对应的全部观察者【源码发现通过事件类型获取观察者列表支持继承】，然后使用`Dispatcher`将事件派发给对应的观察者

## Dispatcher

> `ImmediateDispatcher`:直接在当前线程中遍历所有的观察者并进行事件分发
>
> `LegacyAsyncDispatcher`: `AsyncEventBus` 默认使用该分发器，内部有个全局队列保存所有事件和订阅者关系，即会对多线程发布的事件进行汇总。可能的执行结果`[a1, a2, a3, b1, b2], [a1, b2, a2, a3, b2], [a1, b2, b3, a2, a3]`。从名字来看这个事件分发器应该是要废弃掉的【感觉多此一举的派发器】
>
> `PerThreadQueuedDispatcher`:保证同一线程上发送事件能够按照他们发布的顺序被分发给所有的订阅者，对于该派发器的说法网上文章都没能说清楚。其实它是用了两个线程变量保证了事件`A`的订阅者收到了事件进行处理过程中发布了事件`B`，它能保证事件`A`的所有观察者在事件`B`的观察者之前被执行

上述三个分发器内部最终都会调用`Subscriber#dispatchEvent()`方法进行事件分发

```java
final void dispatchEvent(final Object event) {
    // 使用指定的执行器执行任务
    executor.execute(new Runnable() {
        @Override
        public void run() {
            try {
                // 使用反射触发监听方法	
                invokeSubscriberMethod(event);
            } catch (InvocationTargetException e) {
                // 使用EventBus内部的SubscriberExceptionHandler处理异常
                bus.handleSubscriberException(e.getCause(), context(event));
            }
    	}
    });
}
```

在事件派发的过程中使用`executor`进行任务处理，所以影响`EventBus`是同步还是异步的核心是取决于`executor`

