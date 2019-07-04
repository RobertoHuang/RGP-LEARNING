# CopyOnWriteArrayList

![CopyOnWriteArrayList继承结构](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Collection/images/CopyOnWriteArrayList%E7%BB%A7%E6%89%BF%E4%BD%93%E7%B3%BB.png)

> `CopyOnWriteArrayList`是`ArrayList`的线程安全版本，内部也是通过数组实现的
>
> 每次对数据的修改都完全拷贝了一份新的数组来修改，修改完了再替换老数组【写:阻塞 读:不阻塞】

## 源码浅析

> `CopyOnWriteArrayList`与`ArrayList`相似，内部使用`Object[] array`保存集合元素

```java
public CopyOnWriteArrayList() {
    setArray(new Object[0]);
}

public CopyOnWriteArrayList(Collection<? extends E> c) {
    Object[] elements = c.toArray();
    // 防止Collection子类重写toArray方法修改了返回值
    if (elements.getClass() != Object[].class)
        elements = Arrays.copyOf(elements, elements.length, Object[].class);
    setArray(elements);
}

public CopyOnWriteArrayList(E[] toCopyIn) {
    setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
}
```

- `add(E e)`添加元素

  ```java
  public boolean add(E e) {
      final ReentrantLock lock = this.lock;
      // 加锁
      lock.lock();
      try {
          // 获取旧数组
          Object[] elements = getArray();
          int len = elements.length;
          // 将旧数组元素拷贝到新数组中
          // 新数组大小是旧数组的大小加一
          Object[] newElements = Arrays.copyOf(elements, len + 1);
          // 将新加的元素放在最后一位，
          newElements[len] = e;
          setArray(newElements);
          return true;
      } finally {
          // 解锁
          lock.unlock();
      }
  }
  ```

- `add(int index, E element)`添加一个元素在指定索引处

  ```java
  public void add(int index, E element) {
      final ReentrantLock lock = this.lock;
      // 加锁
      lock.lock();
      try {
          // 获取旧数组
          Object[] elements = getArray();
          int len = elements.length;
          // 检查是否越界, 可以等于len
          if (index > len || index < 0)
              throw new IndexOutOfBoundsException("Index: "+index+", Size: "+len);
          Object[] newElements;
          int numMoved = len - index;
          if (numMoved == 0)
              // 如果插入的位置是最后一位
              // 那么拷贝一个n+1的数组，前N个元素与旧数组一致
              newElements = Arrays.copyOf(elements, len + 1);
          else {
              // 如果插入的位置不是最后一位，那么新建一个N+1的数组
              newElements = new Object[len + 1];
              // 拷贝旧数组前index的元素到新数组中
              System.arraycopy(elements, 0, newElements, 0, index);
              // 拷贝index及其之后的元素往后挪一位到新数组中 这样index的位置是空出来的
              System.arraycopy(elements, index, newElements, index + 1, numMoved);
          }
          // 将元素放在index处
          newElements[index] = element;
          setArray(newElements);
      } finally {
          // 解锁
          lock.unlock();
      }
  }
  ```

- `addIfAbsent(E e)`添加一个元素如果这个元素不存在集合中

  ```java
  public boolean addIfAbsent(E e) {
      // 获取元素数组, 取名为快照
      Object[] snapshot = getArray();
      // 检查如果元素不存在,直接返回false
      // 如果存在再调用addIfAbsent()方法添加元素
      return indexOf(e, snapshot, 0, snapshot.length) >= 0 ? false : addIfAbsent(e, snapshot);
  }
  
  private boolean addIfAbsent(E e, Object[] snapshot) {
      final ReentrantLock lock = this.lock;
      // 加锁
      lock.lock();
      try {
          // 重新获取旧数组
          Object[] current = getArray();
          int len = current.length;
          // 如果快照与刚获取的数组不一致,说明有修改
          if (snapshot != current) {
              // 重新检查元素是否在刚获取的数组里，如果已存在直接返回
              int common = Math.min(snapshot.length, len);
              for (int i = 0; i < common; i++)
                  if (current[i] != snapshot[i] && eq(e, current[i]))
                      return false;
              if (indexOf(e, current, common, len) >= 0)
                      return false;
          }
          // 拷贝一份n+1的数组
          Object[] newElements = Arrays.copyOf(current, len + 1);
          // 将元素放在最后一位
          newElements[len] = e;
          setArray(newElements);
          return true;
      } finally {
          // 释放锁
          lock.unlock();
      }
  }
  ```
  
- `get(int index)`获取指定索引元素

  ```java
  public E get(int index) {
      // 获取元素不需要加锁
      // 直接返回index位置的元素
      // 这里是没有做越界检查的, 因为数组本身会做越界检查
      return get(getArray(), index);
  }
  ```

- `remove(int index)`删除指定索引位置的元素

  ```java
  public E remove(int index) {
      final ReentrantLock lock = this.lock;
      // 加锁
      lock.lock();
      try {
          // 获取旧数组
          Object[] elements = getArray();
          int len = elements.length;
          E oldValue = get(elements, index);
          int numMoved = len - index - 1;
          if (numMoved == 0)
              // 如果移除的是最后一位
              // 那么直接拷贝一份n-1的新数组, 最后一位就自动删除了
              setArray(Arrays.copyOf(elements, len - 1));
          else {
              // 如果移除的不是最后一位
              // 那么新建一个n-1的新数组
              Object[] newElements = new Object[len - 1];
              // 将前index的元素拷贝到新数组中
              System.arraycopy(elements, 0, newElements, 0, index);
              // 将index后面(不包含)的元素往前挪一位
              // 这样正好把index位置覆盖掉了, 相当于删除了
              System.arraycopy(elements, index + 1, newElements, index, numMoved);
              setArray(newElements);
          }
          return oldValue;
      } finally {
          // 释放锁
          lock.unlock();
      }
  }
  ```

## CopyOnWriteArrayList总结

- `CopyOnWriteArrayList`使用`ReentrantLock`重入锁加锁，保证线程安全
- 使用`CopyOnWriteArrayList`不会出现`ConcurrentModificationException`异常
- `CopyOnWriteArrayList`的读操作支持随机访问，时间复杂度为`O(1)`
- `CopyOnWriteArrayList`的写操作都要先拷贝一份新数组在新数组中做修改，修改完了再用新数组替换老数组，所以空间复杂度是`O(n)`性能比较低下，时间复杂度与`ArrayList`一致
- `CopyOnWriteArrayList`采用读写分离的思想，读操作不加锁写操作加锁，且写操作占用较大内存空间，所以适用于读多写少的场合，`CopyOnWriteArrayList`只保证最终一致性，不保证实时一致性
- 因为每次修改都是拷贝一份正好可以存储目标个数元素的数组，所以不需要`size`属性了。数组的长度就是集合的大小而不像`ArrayList`数组的长度实际是要大于集合的大小的。比如`add(E e)`操作先拷贝一份`n+1`个元素的数组，再把新元素放到新数组的最后一位，这时新数组的长度为`len+1`了，也就是集合的`size`了

