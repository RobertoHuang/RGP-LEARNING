# Java泛型
在日常开发过程中，使用到泛型的地方有很多

但可能因为使用了泛型，也会带来一个疑问:为什么这么写会编译报错

为了更好的使用泛型，就需要更加深入的去了解它的原理

# Java泛型是什么

## 泛型的定义
泛型(generic type)又称为"参数化类型"(parameterized type)

泛型允许程序员在定义类、接口、方法时，将类型作为参数，在实例化时指明这些类型

## 为什么需要泛型
> 一般的类和方法只能使用具体的类型，要么是基础类型，要么是自定义类型
>
> 如果要编写可以应用于多种类型的代码，这种刻板的限制对代码的束缚就会很大

泛型的出现是为了让编写的代码可以应用于多种类型，解除只能使用具体类型的限制

下面以ArrayList为例:
- 没有泛型之前
    ```java
    public class ArrayList ... {
        // 用于存储ArrayList对象的数组
        transient Object[] elementData;
        
        // 获取数组对象
        public Object get(int index) {
            ...
        }
        
        // 添加数组对象
        public void add(Object o) {
            ...
        } 
    }
    ```
    在Java1.4版本以前，ArrayList使用Object类作为对象存取的出入参
    
    这样做的好处是可以让ArrayList类满足一次编写适用多种类型的设计，但是也带来了一些问题
    
    ```java
    import java.util.ArrayList;
    import java.util.Date;
    
    public class Main {
        public static void main(String[] args) {
            ArrayList list = new ArrayList();
            // 十分不安全的行为
            list.add(new Date());
            // 强制类型转换
            String res = (String) list.get(0);
        }
    }
    ```
    1.在插入的时候没办法限制具体类型
    
    2.从集合中获取元素的时候，需要强制类型转换。可能出现异常
    
- 有了泛型之后
    ```java
    public class ArrayList<E> ... {
        transient Object[] elementData;
        
        public E get(int index) {
            ...
        }
        
        public boolean add(E e) {
            ...
        }
    }
    ```
    在Java1.5之后，ArrayList使用泛型E作为对象存取的出入参
    
    从而更优雅、更安全的让容器类解除只能使用具体类型的束缚，适用于多种类型
    
    ```java
    import java.util.ArrayList;
    import java.util.Date;
    
    public class Main {
        public static void main(String[] args) {
            ArrayList<String> objects = new ArrayList<>();
            objects.add("Hello");
            // 编译错误
            objects.add(new Date());
        }
    }
    ```
    1.编译器可以检查一个插入操作是否符合要求
    
    2.代码可读性好，在调用get()的时候，无需进行强转，安全

# 泛型的基础使用

- 泛型类
    ```java
    public class Class<T> {
        private T student;
        
        public T getStudent(){
            return student;
        }
    }
    ```
    
    多个泛型的情况
    ```java
    public class Class<T, U> {
        private T student;
        private U teacher;
        
        public T getStudent() {
            return student;
        }
        
        public U getTeacher() {
            return teacher;
        }
    }
    ```

- 泛型接口
    ```java
    public interface Generator<T> {
      T next();
    }
    ```
    在使用上和泛型类是一致的
  
- 泛型方法
    ```java
    public class New {
      public static <T> void show(T title) {
          System.out.println(title);
      }
    }
    ```
    泛型方法可以在普通类中定义，也可以在泛型类中定义

# 什么是类型擦除

在Java虚拟机中是没有泛型类型对象的，也就是说所有对象在虚拟机中都是普通类。实现类型擦除通常要经过下面几个步骤:
-  用类型参数替换为它们的界限，无界类型参数替换为Object
-  如有必要，插入类型强转以保持类型安全
-  生成桥接方法以保留继承泛型类型中的多态性

泛型经过类加载器加载到虚拟机之后，泛型被擦除了，但是泛型的信息仍然存在字节码中，并且可以通过反射获取到

# 泛型的限制
Java泛型的限制主要源于类型擦除的机制

- 泛型不支持基本类型
  
  Java的泛型通过类型擦除实现
  
  无界的泛型参数会被替换为Object类型
  
  而Java的基础类型不是对象，无法用Object表示
  
- 无法创建类型参数泛型的实例
  
  **编译器**无法获取泛型参数实际类型信息
  
- 静态字段static不能修饰类型参数
  ```java
  public class ClassService<T> {
      private static T student;
  
  }
  ```
  如果允许静态属性是泛型参数类型
  ```java
  ClassService<A> a = new ClassService<>();
  ClassService<B> b = new ClassService<>();
  ```
  静态属性只跟随类存在一份，多个泛型参数的对象实例化会产生歧义

- 不能使用类型参数进行强制转换或instanceof
  
  **编译器**无法获取泛型参数实际类型信息
  
- 无法创建泛型类型的数组
  
  **编译器**无法获取泛型参数实际类型信息
  
- 不能创建、捕获或抛出参数化类型的异常
  
  **编译器**无法获取泛型参数实际类型信息
  
- 不能重载仅泛型参数不同的方法
  
  类型擦除后，方法签名相同，会导致冲突。(Java集成泛型的多态性是通过方法桥接完成的)
# 泛型的变型及PECS
在Java泛型中有一个反直觉的地方
```java
ArrayList<Animal> list = new ArrayList<Cat>()
```
这段代码其实是会编译报错的，这是因为Java泛型默认是不变的，Java引入了类型通配符（wildcards）来解决这个问题

## 变型
在了解通配符之前，先介绍程序语言里的一个概念: 变型

逆变与协变用来描述类型转换后的子类型关系，其定义:如果A、B表示类型，f表示类型转换，≤表示子类型关系(比如，A≤B表示A是B的子类型)
- f是协变(covariant)的，当A≤B时有f(A)≤f(B)成立
- f是逆变(contravariant)的，当A≤B时有f(B)≤f(A)成立
- f是不变(invariant)的，当A≤B时上述两个式子均不成立，即f(A)与f(B)相互之间没有子类型关系

在Java设计中，数组是协变的，而泛型列表是不变的
```java
Animal[] animals = new Cat[10]; //OK
ArrayList<Animal> list = new ArrayList<Cat>(); // Error
```

## 通配符
Java泛型里的通配符实现了程序语言里的协变和逆变。类型参数默认是不变的，使用上界通配符带来协变的特征；使用下界通配符带来逆变的特征

- 上界通配符
  
  使用上界限制类型参数，需要借助extends关键字
  
  Java设计时为了安全起见，只要声明了上界，除了null之外一律不准传入给泛型。说白了，就是只读不写，这样可以保证安全性
  
- 下界通配符
  
  使用下界限制类型参数，需要借助super关键字
  
  定义了下界的可以添加元素，不过添加的元素类型只能是指定类型和其子类
  
  读取元素时将不能确定具体的类型，只能用Object来接收
  
- 无界通配符
  
  无界通配符使用单独一个？，无界通配符等同于？extends Object

正如上面对上下界的描述，我们已经明白了大致的应用场景

当我们需要只读不写时就用协变，只写不读就用逆变。又想读又想写我们应该指明准确的泛型类型

著名的PECS原则就总结了这一点，PECS（Prodcuer extends Consumer super），也就是说作为元素的生产者Prodcuer要用协变，支持元素的读取，而作为消费者Consumer要支持逆变，支持元素的写入