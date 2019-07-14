# JAVA8

## 常用函数

```java
1.Predicate<T> 参数T 返回类型Boolean
2.Consumer<T> 参数T 返回类型void
3.Funcation<T, R> 参数T 返回类型R
4.Supplier<T> 参数None 返回类型T
5.UnaryOperator<T> 参数T 返回类型T 
6.BinaryOperator<T> 参数T, T 返回类型T
```

## 默认方法

```reStructuredText
1.类胜于接口，如果在继承链中有方法体或抽象的方法声明就可以忽略接口中定义的方法
2.子类胜于父类，如果一个接口继承了另一个接口，且两个接口都定义了一个默认方法则子类胜出
3.如果上面两条规则都不适用，那么子类需要实现该方法，或将该方法声明为抽象方法流式编程
```

## Optional 

```java
public static void main(String[] args) {
    Optional.ofNullable("Roberto").ifPresent(System.out::println);
    System.out.println(Optional.ofNullable("Roberto").map(String::length).orElse(-1));
}

创建Optional
    of
    ofNullable

使用Optional
    isPresent 是否有值
    filter 对Optional对象进行过滤
    orElse 当Optional对象为空时，提供一个备选值
    orElseGet 当Optional对象为空时，提供一个备选值
    orElseThrow 当Optional对象为空时，抛出异常
    map 对Optional对象进行映射 获取的返回值自动被Optional包装, 即返回值 -> Optional<返回值>
    flatMap 对Optional对象进行映射 返回值保持不变 即Optional<返回值> -> Optional<返回值>
```


## 流式编程

### 流创建

```java
1.Collection.stream();

2.Arrays.stream();

3.Stream stream = Stream.of();

4.IntStream/LongStream.rang()/rangeClosed()

5.Random.ints()/longs()/doubles()

6.Stream.generate()/iterate()
    
...
```

### 常用API    

#### **Terminal** 

- `forEach/forEachOrdered`遍历

  ```java
  public static void main(String[] args) {
      // 使用并行流
      Stream.of("Roberto", "Henry", "DreamT").parallel().forEach(str -> System.out.println(str));
  
      // 使用forEachOrdered保证顺序
      Stream.of("Roberto", "Henry", "DreamT").parallel().forEachOrdered(str -> System.out.println(str));
  }
  ```

- `toArray/collect`转为其他数据结构

  ```java
  public static void main(String[] args) {
      // 1.Array
      String[] strArray = Stream.of("Roberto", "Henry", "DreamT").toArray(String[]::new);
  
      // 2.Collection
      Set<String> collect = Stream.of("Roberto", "Henry", "DreamT").collect(Collectors.toSet());
      List<String> collect2 = Stream.of("Roberto", "Henry", "DreamT").collect(Collectors.toList());
      HashSet<String> collect3 = Stream.of("Roberto", "Henry", "DreamT").collect(Collectors.toCollection(HashSet::new));
      ArrayList<String> collect4 = Stream.of("Roberto", "Henry", "DreamT").collect(Collectors.toCollection(ArrayList::new));
  
      // 3.String
      String str = Stream.of("Roberto", "Henry", "DreamT").collect(Collectors.joining(", ", "[", "]")).toString();
  }
  ```

- `reduce()`对`Stream`中元素进行聚合求值

  ```java
  public static void main(String[] args) {
      // 不带初始化值的Reduce
      Optional<String> reduce = Stream.of("Roberto", "Henry", "DreamT").reduce((s1, s2) -> s1 + "|" + s2);
      reduce.ifPresent(str -> System.out.println(str));
  
      // 带初始化值的Reduce
      String reduceStr = Stream.of("Roberto", "Henry", "DreamT").reduce("Prefix:", (s1, s2) -> s1 + "|" + s2);
      System.out.println(reduceStr);
  }
  ```

- `max()/min()/count()`最大值/最小值/计算总和

  ```java
  public static void main(String[] args) {
      List<String> strList = Arrays.asList("Roberto", "Henry", "DreamT");
      System.out.println(strList.stream().count());
      System.out.println(strList.stream().min(Comparator.comparing(String::length)).get());
      System.out.println(strList.stream().max(Comparator.comparing(String::length)).get());
  }
  ```

- 匹配查找

  ```reStructuredText
  AnyMatch()   流中一个元素能符合判断规则即为true
  AllMatch()   流中所有元素能符合判断规则即为true
  NoneMatch()  流中所有元素都不符合判断即为true
  findAny()    返回当前流中的任意元素 方法返回结果为Optional<T>
  findFirst()  返回当前流中的第一个元素 方法返回结果为Optional<T>
  ```

#### **Intermediate** 

- `map`作用是返回一个对当前所有元素执行执行`mapper`之后的结果组成的`Stream`。直观的说就是对每个元素按照某种操作进行转换，转换前后`Stream`中元素的个数不会改变，但元素的类型取决于转换之后的类型

  ```java
  public static void main(String[] args) {
      Stream<String> stream = Stream.of("Roberto", "Henry", "DreamT");
      stream.map(str -> str.toLowerCase()).forEach(str -> System.out.println(str));
  }
  ```

- `flatMap()`作用是对每个元素执行`mapper`指定的操作，并用所有`mapper`返回的`Stream`中的元素组成一个新的`Stream`作为最终返回结果。通俗的讲`flatMap()`的作用就相当于把原`stream`中的所有元素都摊平之后组成的`Stream`，转换前后元素的个数和类型都可能会改变

  ```java
  public static void main(String[] args) {
      Stream<List<Integer>> stream = Stream.of(Arrays.asList(1,2), Arrays.asList(3, 4, 5));
      stream.flatMap(list -> list.stream()).forEach(i -> System.out.println(i));
  }
  ```

- `filter()`过滤 返回一个只包含满足`predicate`条件元素的`Stream`

  ```java
  public static void main(String[] args) {
      Stream<String> stream = Stream.of("Roberto", "Henry", "DreamT");
      stream.filter(str -> str.length() == 5).forEach(str -> System.out.println(str));
  }
  ```

- `peek`遍历 常用于`Debug` 用法与`forEach`类似

  ```java
  public static void main(String[] args) {
      List<String> strList = Arrays.asList("Roberto", "Henry", "DreamT");
      strList.stream().peek(str -> System.out.println(str)).count();
  }
  ```

- `distinct()`返回一个去除重复元素之后的`Stream`

  ```java
  public static void main(String[] args) {
      Stream<String> stream = Stream.of("Roberto", "Henry", "Henry");
      stream.distinct().forEach(str -> System.out.println(str));
  }
  ```

- `sorted()`排序

  ```java
  public static void main(String[] args) {
      Stream<String> stream = Stream.of("Roberto", "Henry", "DreamT");
      stream.sorted(Comparator.comparingInt(String::length)).forEach(str -> System.out.println(str));
  }
  ```

- `limit()`截断

  ```java
  public static void main(String[] args) {
      List<String> strList = Arrays.asList("Roberto", "Henry", "DreamT");
      strList.stream().limit(2).forEach(str -> System.out.println(str));
  }
  ```

- `skip()`跳过

  ```java
  public static void main(String[] args) {
      List<String> strList = Arrays.asList("Roberto", "Henry", "DreamT");
      strList.stream().skip(2).forEach(str -> System.out.println(str));
  }
  ```

### 并行流

- `parallel()`并行流

- `sequential()`串行流

  ```javascript
  1.多次调用parallel/sequential，以最后一次调用为准
  2.并行流使用的线程池是ForkJoinPool.commonPool，默认线程数是当前机器的CPU个数
  3.并行不一定比串行快 要根据实际情况选择
  ```

- 使用自己的线程池不使用默认线程池，防止任务被阻塞

  ```java
  public static void main(String[] args) throws InterruptedException {
      ForkJoinPool forkJoinPool = new ForkJoinPool(20);
      forkJoinPool.submit(() -> IntStream.range(1, 100).parallel().peek(number -> System.out.println(number)).count());
      forkJoinPool.shutdown();
      // 防止主线程提前结束
      synchronized (forkJoinPool) { forkJoinPool.wait(); }
  }
  ```

- `RecursiveAction`是什么，`RecursiveTask`是什么

  ```reStructuredText
  1.创建一个对象代表所有的工作量.
  2.定义一个临界值，当工作量没到达这个临界值时继续将任务进行分解.
  3.当任务达到临界值或者在临界值之下时，开始执行任务
  
  RecusiveTask和RecusiveAction相似
  只不过每个子任务处理之后会带一个返回值，最终所有的子任务的返回结果会join(合并)成一个结果
  ```

### 收集器

```java
public class CollectorTest {
    public static List<Student> students;

    static {
        students = Arrays.asList(
                new Student("小明", 10, Gender.MALE, Grade.ONE),
                new Student("大明", 9, Gender.MALE, Grade.THREE),
                new Student("小白", 8, Gender.FEMALE, Grade.TWO),
                new Student("小黑", 13, Gender.FEMALE, Grade.FOUR),
                new Student("小红", 7, Gender.FEMALE, Grade.THREE),
                new Student("小黄", 13, Gender.MALE, Grade.ONE),
                new Student("小青", 13, Gender.FEMALE, Grade.THREE),
                new Student("小紫", 9, Gender.FEMALE, Grade.TWO),
                new Student("小王", 6, Gender.MALE, Grade.ONE),
                new Student("小李", 6, Gender.MALE, Grade.ONE),
                new Student("小马", 14, Gender.FEMALE, Grade.FOUR),
                new Student("小刘", 13, Gender.MALE, Grade.FOUR));
    }

    public static void main(String[] args) {
        // 获取连接
        testJoining();

        // 获取统计
        getCountInfo();

        // 聚合成集合
        testToCollections();

        // 测试分模块和分组
        testPartitionAndGroupBy();

        // collectingAndThen 聚合后继续处理
    }

    private static void testJoining() {
        // 获取所有班级学生名字连接字符串
        String names = students.stream().map(student -> student.getName()).collect(Collectors.joining(", ", "[", "]"));
        System.out.println("学生名字汇总" + names);
        // 获取所有班级学生名字连接字符串
        names = students.stream().collect(Collectors.mapping(Student::getName, Collectors.joining(", ", "[", "]")));
        System.out.println("学生名字汇总" + names);
    }

    private static void getCountInfo() {
        // 获取所有学生年龄总和
        Integer totalAge = students.stream().map(Student::getAge).collect(Collectors.reducing(0, (x, y) -> x + y));
        System.out.println("所有学生年龄总和:" + totalAge);
        totalAge = students.stream().collect(Collectors.reducing(0, Student::getAge, (x, y) -> x + y));
        System.out.println("所有学生年龄总和:" + totalAge);
        // summingLong summingDouble同理
        totalAge = students.stream().collect(Collectors.summingInt(Student::getAge));
        System.out.println("所有学生年龄总和:" + totalAge);

        // 获取所有年级年龄最大值
        Integer maxAge = students.stream().collect(Collectors.mapping(Student::getAge, Collectors.maxBy(Comparator.comparing(Function.identity())))).get();
        System.out.println("所有年级年龄最大值为:" + maxAge);
        maxAge = students.stream().mapToInt(Student::getAge).max().getAsInt();
        System.out.println("所有年级年龄最大值为:" + maxAge);

        // 获取所有年级年龄最小值
        Integer minAge = students.stream().collect(Collectors.mapping(Student::getAge, Collectors.minBy(Comparator.comparing(Function.identity())))).get();
        System.out.println("所有年级年龄最小值为:" + minAge);
        minAge = students.stream().mapToInt(Student::getAge).min().getAsInt();
        System.out.println("所有年级年龄最小值为:" + minAge);

        // 获取学生平均年龄 averagingDouble averagingLong同理
        double avgAge = students.stream().collect(Collectors.averagingInt(Student::getAge));
        System.out.println("所有学生平均年龄:" + avgAge);
        avgAge = students.stream().mapToInt(Student::getAge).average().getAsDouble();
        System.out.println("所有学生平均年龄:" + avgAge);

        // 获取学生年龄相关聚合信息 summarizingDouble summarizingLong同理
        IntSummaryStatistics ageSummarizing = students.stream().collect(Collectors.summarizingInt(Student::getAge));
        System.out.println("学生年龄相关的聚合信息:" + ageSummarizing);
    }

    private static void testToCollections() {
        List<Student> studentList = students.stream().collect(Collectors.toList());
        studentList = students.stream().collect(Collectors.toCollection(ArrayList::new));

        Set<Student> studentSet = studentList.stream().collect(Collectors.toSet());
        studentSet = studentList.stream().collect(Collectors.toCollection(HashSet::new));

        Map<String, Student> studentNameToStudent = students.stream().collect(Collectors.toMap(Student::getName, Function.identity()));
        ConcurrentMap<String, Student> concurrentStudentNameToStudent = students.stream().collect(Collectors.toConcurrentMap(Student::getName, Function.identity()));

        Map<Gender, Integer> genderAgeMapper = students.stream().collect(Collectors.toMap(Student::getGender, Student::getAge, (a, b) -> a + b));
        Map<Gender, Integer> concurrentGenderAgeMapper = students.stream().collect(Collectors.toConcurrentMap(Student::getGender, Student::getAge, (a, b) -> a + b));

        HashMap<Gender, Integer> genderAgeMapperBySupplier = students.stream().collect(Collectors.toMap(Student::getGender, Student::getAge, (a, b) -> a + b, HashMap::new));
        ConcurrentSkipListMap<Gender, Integer> concurrentGenderAgeMapperBySupplier = students.stream().collect(Collectors.toConcurrentMap(Student::getGender, Student::getAge, (a, b) -> a + b, ConcurrentSkipListMap::new));
    }

    private static void testPartitionAndGroupBy() {
        // 分块统计信息
        Map<Boolean, List<Student>> genders = students.stream().collect(Collectors.partitioningBy(s -> s.getGender() == Gender.MALE));
        MapUtils.verbosePrint(System.out, "男女学生列表", genders);
        // 分块后再聚合 获取男生女生平均年龄
        Map<Boolean, Double> avgAgeGroupByGender = students.stream().collect(Collectors.partitioningBy(s -> s.getGender() == Gender.MALE, Collectors.averagingInt(Student::getAge)));
        MapUtils.verbosePrint(System.out, "男生女生平均年龄分别是:", avgAgeGroupByGender);

        // 分组统计信息 groupingByConcurrent将分组返回结果封装成ConcurrentMap
        Map<Grade, List<Student>> grades = students.stream().collect(Collectors.groupingBy(Student::getGrade));
        MapUtils.verbosePrint(System.out, "学生班级列表", grades);
        // 分组后再聚合 获取每个年级学生的平均年龄
        Map<Grade, Double> avgAgeGroupByGrade = students.stream().collect(Collectors.groupingBy(Student::getGrade, Collectors.averagingInt(Student::getAge)));
        MapUtils.verbosePrint(System.out, "每个年级学生的平均年龄", avgAgeGroupByGrade);
        // 分组后再聚合 得到每个年级对应学生的个数
        Map<Grade, Long> gradesCount = students.stream().collect(Collectors.groupingBy(Student::getGrade, Collectors.counting()));
        MapUtils.verbosePrint(System.out, "班级学生个数列表", gradesCount);
    }
}

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
class Student {
    /** 姓名 **/
    private String name;

    /** 年龄 **/
    private int age;

    /** 性别 **/
    private Gender gender;

    /** 班级 **/
    private Grade grade;
}

enum Gender {
    MALE, FEMALE
}

enum Grade {
    ONE, TWO, THREE, FOUR;
}
```

### 经典案例

- 历史遗留代码

  ```java
  public static class Step0 implements LongTrackFinder {
      public Set<String> findLongTracks(List<Album> albums) {
          Set<String> trackNames = new HashSet<>();
          for (Album album : albums) {
              for (Track track : album.getTrackList()) {
                  if (track.getLength() > 60) {
                      String name = track.getName();
                      trackNames.add(name);
                  }
              }
          }
          return trackNames;
      }
  }
  ```

- 优化第一步

  ```java
  public static class Step1 implements LongTrackFinder {
      public Set<String> findLongTracks(List<Album> albums) {
          Set<String> trackNames = new HashSet<>();
          albums.stream().forEach(album -> album.getTracks().forEach(track -> {
              if (track.getLength() > 60) {
                  String name = track.getName();
                  trackNames.add(name);
              }
          }));
          return trackNames;
      }
  }
  ```

- 优化第二步

  ```java
  public static class Step2 implements LongTrackFinder {
      public Set<String> findLongTracks(List<Album> albums) {
          Set<String> trackNames = new HashSet<>();
          albums.stream().forEach(album -> album.getTracks().filter(track -> track.getLength() > 60).map(track -> track.getName()).forEach(name -> trackNames.add(name)));
          return trackNames;
      }
  }
  ```

- 优化第三步

  ```java
  public static class Step3 implements LongTrackFinder {
      public Set<String> findLongTracks(List<Album> albums) {
          Set<String> trackNames = new HashSet<>();
          albums.stream().flatMap(album -> album.getTracks()).filter(track -> track.getLength() > 60).map(track -> track.getName()).forEach(name -> trackNames.add(name));
          return trackNames;
      }
  }
  ```

- 优化第四步

  ```java
  public static class Step4 implements LongTrackFinder {
      public Set<String> findLongTracks(List<Album> albums) {
          return albums.stream().flatMap(album -> album.getTracks()).filter(track -> track.getLength() > 60).map(track -> track.getName()).collect(toSet());
      }
  }
  ```

### Eager&Lazy

```java
判断一个操作是惰性求值还是及早求值只需要看它的返回值
如果返回值是Stream那么是惰性求值，如果返回值是另一个值或为空，那么就是及早求值
```

### Stream运行机制

```reStructuredText
1.链式调用 一个元素只迭代一次
2.每一个中间操作返回一个新的流，流的属性sourceStage指向Head
3.Head->nextStage->nextStage...->null
4.有状态操作会把无状态操作截断单独处理 有状态操作:2个入参 无状态操作:1个入参
5.并行环境下有状态的中间操作不一定能并行操作
6.parrallel/sequetial也是中间操作(也是返回Stream)，它们不创建流只修改Head的并行标志
```

### 断点调试

```java
插件:Java Stream Debugger
```

## 日期API

```java
public class DateTest {
    public static void main(String[] args) throws InterruptedException {
        // DateAPI太恶心
        // Date date = new Date(116, 2, 18);

        // SimpleDateFormat多线程情况下会有问题
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        testLocalDate();
        testLocalTime();
        combineLocalDateAndTime();
        testInstant();
        testDuration();
        testPeriod();
        testDateFormat();
        testDateParse();
    }

    private static void testLocalDate() {
        LocalDate localDate = LocalDate.of(2016, 11, 13);
        System.out.println(localDate.getYear());
        System.out.println(localDate.getMonth());
        System.out.println(localDate.getMonthValue());
        System.out.println(localDate.getDayOfYear());
        System.out.println(localDate.getDayOfMonth());
        System.out.println(localDate.getDayOfWeek());
    }

    private static void testLocalTime() {
        LocalTime time = LocalTime.now();
        System.out.println(time.getHour());
        System.out.println(time.getMinute());
        System.out.println(time.getSecond());
    }

    private static void combineLocalDateAndTime() {
        // 获取当前时间
        LocalDate localDate = LocalDate.now();
        LocalTime time = LocalTime.now();
        LocalDateTime localDateTime = LocalDateTime.of(localDate, time);
        System.out.println(localDateTime.toString());

        // 获取当前时间
        localDateTime = LocalDateTime.now();
        System.out.println(localDateTime.toString());
    }

    private static void testInstant() throws InterruptedException {
        // 获取时间戳
        Instant start = Instant.now();
        Thread.sleep(1000L);
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        System.out.println(duration.toMillis());
    }

    private static void testDuration() {
        // 获取时间间隔
        LocalTime time = LocalTime.now();
        LocalTime beforeTime = time.minusHours(1);
        Duration duration = Duration.between(time, beforeTime);
        System.out.println(duration.toHours());
    }

    private static void testPeriod() {
        // 获取两个日期之间包含多少天，多少月，多少年
        Period period = Period.between(LocalDate.of(2014, 1, 10), LocalDate.of(2016, 1, 10));
        System.out.println(period.getMonths());
        System.out.println(period.getDays());
        System.out.println(period.getYears());
    }

    private static void testDateFormat() {
        // 日期格式化
        LocalDate localDate = LocalDate.now();
        String format1 = localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        System.out.println(format1);

        DateTimeFormatter mySelfFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String format = localDate.format(mySelfFormatter);
        System.out.println(format);
    }

    private static void testDateParse() {
        // 日期格式解析
        String date1 = "20161113";
        LocalDate localDate = LocalDate.parse(date1, DateTimeFormatter.BASIC_ISO_DATE);
        System.out.println(localDate);

        DateTimeFormatter mySelfFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date2 = "2016-11-13";
        LocalDate localDate2 = LocalDate.parse(date2, mySelfFormatter);
        System.out.println(localDate2);
    }
}
```

