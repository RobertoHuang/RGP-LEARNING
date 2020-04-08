[toc]

# `JAVA`并发编程阶段三总结

## Atomic相关知识

`CAS - compare and swap`的缩写，中文翻译成比较并交换

实现的本质是:利用`CPU`的`CAS`指令(`cmpxchg`)，同时借助`JNI`来完成`Java`的非阻塞算法

优势：【`Lock Free`】可以减少重量级锁引起频繁线程上下文切换带来的开销

缺点：带来了`ABA`问题、循环时间长开销大、只能保证一个共享变量的原子操作的问题

**针对`ABA`问题** - `JDK`提供了`AtomicStampedReference`解决

```java
public static void main(final String[] args) {
    final String str1 = "A";
    final String str2 = "B";

    // 初始化
    final AtomicStampedReference<String> reference = new AtomicStampedReference<String>(str1, 1);
    final int initStamp = reference.getStamp();

    // 将值由A > B
    reference.compareAndSet(str1, str2, reference.getStamp(), reference.getStamp() + 1);
    System.out.println("reference.getReference() = " + reference.getReference());

    // 将值由B > A
    reference.compareAndSet(str2, str1, reference.getStamp(), reference.getStamp() + 1);
    System.out.println("reference.getReference() = " + reference.getReference());

    // 修改
    final boolean isSuccess = reference.compareAndSet(str1, str2, initStamp, reference.getStamp() + 1);
    System.out.println("isSuccess:" + isSuccess);
    System.out.println("reference.getReference() = " + reference.getReference());
}
```

**针对只能保证一个共享变量的原子操作问题** - `JDK`提供了`AtomicReference`解决，`AtomicReference`还可以用来解决匿名内部类的`Final`问题，示例代码如下

```java
public static void main(String[] args) {
    final AtomicReference<Object> atomicReference = new AtomicReference<>(new Object());
    new Thread(() -> {
        System.out.println("invoke something.");
        atomicReference.set(new Object());
    }).start();
}
```

`Atmoic`数组操作相关的包使用(略)

`AtomicReferenceFieldUpdater`更节约内存【[Atomic包之FieldUpdater深度解析](https://github.com/aCoder2013/blog/issues/10)】

`Atomic`类底层使用了`Unsafe`类，关于`Unsafe`类可参考美团出品:[Java魔法类：Unsafe应用解析](https://tech.meituan.com/2019/02/14/talk-about-java-magic-class-unsafe.html)

## 并发辅助工具类

- `CountDownLatch`一个或多个线程等待其他所有线程完毕之后再继续进行操作

    `CountDownLatch`主要有两个方法:`countDown`和`await`

    `countDown`方法用于使计数器减一，其一般是执行任务的线程调用

    `await`方法则使调用该方法的线程处于等待状态，其一般是主线程调用

    这里需要注意的是`countDown`方法并没有规定一个线程只能调用一次，当同一个线程调用多次`countDown`方法时每次都会使计数器减一;另外`await`方法也并没有规定只能有一个线程执行该方法，如果多个线程同时执行`await`方法，那么这几个线程都将处于等待状态，并且以共享模式享有同一个锁

- `CyclicBarrier`用于N个线程相互等待，当所有线程到达之后继续执行，它和`CountDownLatch`区别如下

    - `CountDownLatch`可以阻塞1个或N个线程，`CyclicBarrier`必须要阻塞N个线程
    - `CountDownLatch`用完之后就不能再次使用了，`CyclicBarrier`用完之后可以再次使用通过`reset`操作
    - `CyclicBarrier`可以在构造方法中传递回调函数进行通知，`CountDownLatch`则不行
    - `CountDownLatch`底层使用的是共享锁，`CyclicBarrier`底层使用的是`ReentrantLock`和这个`lock`的条件对象`Condition`【这部分没看过源码，网上扒过来的。有空得自个研究一下】

- `Exchanger`用于两个线程间交换数据的【从工作到现在没用到过，不知道是我见识少还是这东西就用的少】

- `Semaphonre`信号量，用来实现流量控制。它可以控制同一时间内对资源的访问次数

- `ReadWriteLock&ReentrantReadWriteLock`**这部分源码还没看，待有空补充**
- `Condition`实现线程生产消费模型【类似于`Object`的`wait()`和`notify()`，等待队列`API`使用】

#### StampedLock

`StampedLock`是`ReentrantReadWriteLock`的增强版，是为了解决`ReentrantReadWriteLock`的一些不足

- 我们都知道`ReentrantReadWriteLock`是读写锁，在多线程环境下大多数情况是读的情况远远大于写的操作，因此可能导致写的饥饿问题。(换人话来说的话，读操作一直都能抢占到CPU时间片，而写操作一直抢不了)
- 可以使用公平锁来避免上述的问题，但是公平锁同时会牺牲吞吐率
- `StampedLock`的设计思路也比较简单，就是在读的时候发现有写操作，再去读多一次【参考:[StampedLock](https://www.liaoxuefeng.com/wiki/1252599548343744/1309138673991714)】

