# LinkedList

![LinkedList继承体系](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Collection/images/LinkedList%E7%BB%A7%E6%89%BF%E4%BD%93%E7%B3%BB.png)

> 实现了`List`提供了基础的添加、删除、遍历等操作
>
> 实现了`Queue`和`Deque`接口，所以它既能作为`List`使用也能作为双端队列使用，当然也可以作为栈使用

## 源码浅析

> `LinkedList`内部是通过双链表结构来保存集合元素

```java
transient int size = 0;

transient Node<E> first;

transient Node<E> last;
```

```java
private static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;

    Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}
```

- 添加元素

  作为一个双端队列添加元素主要由两种，在队头`linkFirst`/队尾`linkLast`添加元素

  ```java
  private void linkFirst(E e) {
      // 头节点
      final Node<E> f = first;
      // 创建新节点，并将新节点的next指向旧的头节点
      final Node<E> newNode = new Node<>(null, e, f);
      // 将新节点指定为新的头节点
      first = newNode;
      // 如果是第一次添加元素，就把尾节点也置为新节点
      // 即如果队列中只有一个元素时,头尾节点相同都指向新添加的元素，并且前驱和后继节点都为空
      // 否则将旧的头结点的前驱节点指向新添加的节点
      if (f == null)
          last = newNode;
      else
          f.prev = newNode;
      size++;
      modCount++;
  }
  ```

  ```java
  void linkLast(E e) {
      // 尾节点
      final Node<E> l = last;
      // 创建新节点，并将新节点的前驱节点指向旧的尾节点
      final Node<E> newNode = new Node<>(l, e, null);
      // 将新节点指定为新的尾节点
      last = newNode;
      // 如果是第一次添加元素，就把头结点也置为新节点
      // 即如果队列中只有一个元素时,头尾节点相同都指向新添加的元素，并且前驱和后继节点都为空
      // 否则将旧的尾节点的后继节点指向新添加的节点
      if (l == null)
          first = newNode;
      else
          l.next = newNode;
      size++;
      modCount++;
  }
  ```

  作为`List`是要支持在中间添加元素的，主要是通过下面这个方法实现的

  ```java
  // 在指定index位置处添加元素
  public void add(int index, E element) {
      // 判断是否越界
      checkPositionIndex(index);
      // 如果index是在队列尾节点之后的一个位置
      // 把新节点直接添加到尾节点之后
      // 否则调用linkBefore()方法在中间添加节点
      if (index == size)
          linkLast(element);
      else
          linkBefore(element, node(index));
  }
  
  // 寻找index位置的节点
  Node<E> node(int index) {
      // 因为是双链表
      // 所以根据index是在前半段还是后半段决定从前遍历还是从后遍历
      // 这样index在后半段的时候可以少遍历一半的元素
      if (index < (size >> 1)) {
          // 如果是在前半段就从前遍历
          Node<E> x = first;
          for (int i = 0; i < index; i++)
              x = x.next;
          return x;
      } else {
          // 如果是在后半段就从后遍历
          Node<E> x = last;
          for (int i = size - 1; i > index; i--)
              x = x.prev;
          return x;
      }
  }
  
  void linkBefore(E e, Node<E> succ) {
      final Node<E> pred = succ.prev;
      // 创建节点
      // 将当前节点的前驱节点指向旧节点的前驱节点，将当前节点的后继节点指向旧节点
      final Node<E> newNode = new Node<>(pred, e, succ);
      succ.prev = newNode;
      // 如果旧节点的前驱节点为空，即插入位置为头节点。将头结点指向为当前新增的节点
      // 否则将旧节点的前驱节点的后继节点指向新增加的节点。该过程是双向链表插入的标准流程
      if (pred == null)
          first = newNode;
      else
          pred.next = newNode;
      size++;
      modCount++;
  }
  ```

  添加元素的三种方式大致如下图所示

  ![LinkedList添加元素示意图](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Collection/images/LinkedList%E6%B7%BB%E5%8A%A0%E5%85%83%E7%B4%A0%E7%A4%BA%E6%84%8F%E5%9B%BE.png)

- 删除元素

  作为双端队列删除元素也有两种方式，队首`unlinkFirst`/队尾`unlinkLast`删除元素

  ```java
  private E unlinkFirst(Node<E> f) {
      // 首节点的元素值
      final E element = f.item;
      final Node<E> next = f.next;
      // 添加首节点的内容，协助GC
      f.item = null;
      f.next = null; 
      // 把首节点的后继节点置为新的头结点
      first = next;
      // 如果只有一个元素，删除了把last置为空，否则把新的头结点前驱节点置空
      if (next == null)
          last = null;
      else
          next.prev = null;
      size--;
      modCount++;
      return element;
  }
  ```

  ```java
  private E unlinkLast(Node<E> l) {
      // 尾节点的元素值
      final E element = l.item;
      final Node<E> prev = l.prev;
      // 清空尾节点的内容，协助GC
      l.item = null;
      l.prev = null; 
      // 让尾节点的前驱节点置为新的尾节点
      last = prev;
      // 如果只有一个元素，删除了把first置为空，否则把新的尾节点的后继节点置空
      if (prev == null)
          first = null;
      else
          prev.next = null;
      size--;
      modCount++;
      return element;
  }
  ```

  作为`List`是要支持在中间删除元素的，主要是通过下面这个方法实现的

  ```java
  public E remove(int index) {
      checkElementIndex(index);
      return unlink(node(index));
  }
  
  // 删除指定节点x
  E unlink(Node<E> x) {
      // x的元素值
      final E element = x.item;
      // x的前置节点
      final Node<E> next = x.next;
      // x的后置节点
      final Node<E> prev = x.prev;
  
      // 如果前置节点为空
      // 说明是首节点，让first指向x的后置节点，否则修改前置节点的next为x的后置节点
      if (prev == null) {
          first = next;
      } else {
          prev.next = next;
          x.prev = null;
      }
  
      // 如果后置节点为空
      // 说明是尾节点，让last指向x的前置节点，否则修改后置节点的prev为x的前置节点
      if (next == null) {
          last = prev;
      } else {
          next.prev = prev;
          x.next = null;
      }
  
      // 清空x的元素值，协助GC
      x.item = null;
      size--;
      modCount++;
      return element;
  }
  ```

  删除元素的三种方式大致如下图所示

  ![LinkedList删除元素示意图](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Collection/images/LinkedList%E5%88%A0%E9%99%A4%E5%85%83%E7%B4%A0%E7%A4%BA%E6%84%8F%E5%9B%BE.png)

- 栈

  > `LinkedList`是双端队列，因此可以当做栈来使用

  ```java
  public void push(E e) {
      addFirst(e);
  }
  
  public E pop() {
      return removeFirst();
  }
  ```

  栈的特性是`LIFO(Last In First Out)`，所以作为栈使用也很简单，添加删除元素都只操作队列首节点即可

- 

## LinkedList总结

- `LinkedList`不支持随机访问，所以访问非队列首尾的元素比较低效
- 在队列首尾添加元素很高效，时间复杂度为`O(1)`
- 在中间添加元素比较低效，首先要找到插入位置的节点再修改前后指针，时间复杂度为`O(n)`
- 在队列首尾删除元素很高效，时间复杂度为`O(1)`
- 在中间删除元素比较低效，首先要找到删除位置的节点再修改前后指针，时间复杂度为`O(n)`

