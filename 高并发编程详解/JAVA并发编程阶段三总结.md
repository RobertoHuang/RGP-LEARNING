[toc]

# `JAVA`并发编程阶段三总结

## CAS相关知识

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

**针对只能保证一个共享变量的原子操作问题** - `JDK`提供了`AutomicReference`解决



