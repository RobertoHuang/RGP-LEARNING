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

- `CountDownLatch`在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待

    `CountDownLatch`主要有两个方法:`countDown`和`await`

    `countDown`方法用于使计数器减一，其一般是执行任务的线程调用

    `await`方法则使调用该方法的线程处于等待状态，其一般是主线程调用

    这里需要注意的是`countDown`方法并没有规定一个线程只能调用一次，当同一个线程调用多次`countDown`方法时每次都会使计数器减一;另外`await`方法也并没有规定只能有一个线程执行该方法，如果多个线程同时执行`await`方法，那么这几个线程都将处于等待状态，并且以共享模式享有同一个锁

- 