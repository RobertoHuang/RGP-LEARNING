# ArrayList

![ArrayList继承体系](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Collection/images/ArrayList%E7%BB%A7%E6%89%BF%E4%BD%93%E7%B3%BB.png)

> 实现了`Cloneable`可以被克隆，实现了`Serializable`可以被序列化
>
> 实现了`RandomAccess`提供了随机访问的能力，实现了`List`提供了基础的添加、删除、遍历等操作

## 源码浅析

> `ArrayList`内部使用`Object[] elementData`来存储集合元素
>
> 可以在初始化时指定初始容量，如果未指定则默认初始化为时`elementData`为空数组

```java
public ArrayList() {
    super();
    this.elementData = EMPTY_ELEMENTDATA;
}

public ArrayList(int initialCapacity) {
    super();
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal Capacity: "+ initialCapacity);
    this.elementData = new Object[initialCapacity];
}

public ArrayList(Collection<? extends E> c) {
    elementData = c.toArray();
    size = elementData.length;
    // 这一步是为了确保elementData是Object[].class
    // 因为子类在重写父类的toArray()方法时，可能修改了返回值类型
    if (elementData.getClass() != Object[].class)
        elementData = Arrays.copyOf(elementData, size, Object[].class);
}
```

- `add(E element)`添加元素
  - 添加元素前判断当前数组是否需要扩容
  - 将待添加的元素插入到数组的末尾，平均时间复杂度为`O(1)`

- `add(int index, E element)`往指定位置添加元素

  与`add(E element)`最大的区别是需要将索引位置后的元素都往后移动一位。所以时间复杂度为`O(n)`

- `get(int index)`获取指定索引位置的元素

  根据索引返回数组`index`位置的元素，时间复杂度为`O(1)`

- `remove(int index)`删除指定索引位置的元素

  将指定索引后的元素都往前移动一位，并且将数组最后一个元素删除，时间复杂度为`O(n)`

- `ensureCapacityInternal(int minCapacity)`判断当前数组是否需要扩容

  - 检查是否需要扩容
  - 新容量是老容量的1.5倍`oldCapacity + (oldCapacity >> 1`
  - 如果扩容后任小于实际需要的容量则以需要的容量为准，创建新容量数组并把老数组拷贝到新数组

## 序列化和反序列化

`ArrayList`将`elementData`定义为`transient`的优势，自己根据`size`序列化真实的元素，而不是根据数组的长度序列化元素，减少了空间占用。通过`writeObject`和`readObject`方法实现

## ArrayList总结

- `ArrayList`支持随机访问，通过索引访问元素极快，时间复杂度为`O(1)`
- `ArrayList`添加元素到尾部极快，平均时间复杂度为`O(1)`
- `ArrayList`添加元素到中间比较慢，因为要搬移元素，平均时间复杂度为`O(n)`
- `ArrayList`从尾部删除元素极快，平均时间复杂度为`O(1)`
- `ArrayList`从中间删除元素比较慢，因为要搬移元素，平均时间复杂度为`O(n)`
- `ArrayList`内部使用数组存储元素，当数组长度不够时进行扩容每次加一半空间，`ArrayList`不会进行缩容

