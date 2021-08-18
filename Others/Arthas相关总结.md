

```
// 下载安装Arthas
curl -O https://arthas.aliyun.com/arthas-boot.jar
java -jar arthas-boot.jar
```



```
// 系统实时数据面板总览 -i多少s统计一次 -n 总的统计多少次
dashboard -i 1000 -n 10
```



```
// 查询3秒内占用CPU资源最多的5个线程
thread -n 5 -i 3000 
可以重点观察一下deltaTime值，该值表示在3S时间内该线程占用的CPU时间 time是该线程占用CPU的总时间
另外一种方式:https://github.com/oldratlee/useful-scripts/blob/master/docs/java.md#-show-busy-java-threads

// 打印指定线程栈
thread 1

// 找出当前阻塞其他线程的线程
// (有时候我们发现应用卡住了，通常是由于某个线程拿住了某个锁
// 并且其他线程都在等待这把锁造成的。为了排查这类问题，arthas提供了thread -b，一键找出那个罪魁祸首)
thread -b

// 查看指定状态的线程
thread --state WAITING
```



```
// 查看当前JVM信息
jvm

该指令同时可用于查看jvm线程使用情况

COUNT: JVM当前活跃的线程数
DAEMON-COUNT: JVM当前活跃的守护线程数
PEAK-COUNT: 从JVM启动开始曾经活着的最大线程数
STARTED-COUNT: 从JVM启动开始总共启动过的线程次数
DEADLOCK-COUNT: JVM当前死锁的线程数
```



```
sysprop

// 查看单个系统属性
$ sysprop user.country
user.country=US

// 修改单个系统属性
$ sysprop user.country CN
Successfully changed the system property.
user.country=CN
```



```
// 查看，更新VM诊断相关的参数
vmoption
参考文档: https://arthas.aliyun.com/doc/vmoption.html#vmoption
```



```
// 日志相关
logger
可以从打印信息中看到所有的Logger以及对应的级别，从appenders中可以看到每个appender输出

// 动态调整日志级别
logger -c 2a139a55 --name ROOT --level debug
默认情况下，logger命令会在SystemClassloader下执行，如果应用是传统的war应用，或者spring boot fat jar启动的应用，那么需要指定classloader

// 使用OGNL修改日志级别 
ognl -c 22d6cac2 '@org.slf4j.LoggerFactory@getILoggerFactory().getLogger("com.gaoding.pt.cms.service.impl").setLevel(@ch.qos.logback.classic.Level@ERROR)'
```



```
// 便捷的查看或监控Mbean的属性信息

// 列出所有Mbean的名称
mbean

// 如查看druid的mbean信息 -i刷新时间 -n监控次数
mbean -i 1000 -n 10 com.alibaba.druid:type=DruidDataSource,id=1823076816
```



```
ognl
关联文档:

常用Ognl表达式

获取类的静态属性:@className@field

调用实例的方法:@className@field.methodName()

变量引用:ognl (@com.gaoding.rocketmq.TestClass@student.intArray.length).(#this*2) 此处的#this即数组的长度

链式表达式:ognl @com.gaoding.rocketmq.TestClass@student.(sayHello(),toString()) 此处的含义是先调用student的sayHello()方法，再调用toString()方法，括号的最右边是方法的返回值

构造集合:{"xxx","yyy"} 判断字段是否在集合中'zzz in {"xxx","yyy"}'

构造数组:new int[] {1,2,3 } 判断数据是否在数组中'n in new int[] {1,2,3 }'

构造Map:#{"a":"b", "c":"d"} 判断数据是否在map的key中:'"x" in #{"a":"b", "c":"d"}' 如果想要创建指定类型的Map可以在大括号前指定'#@java.util.concurrent.ConcurrentHashMap@{"a":"b", "c":"d"}'

集合投影:{"xxx","yyy"}.{#this} 集合投影即在.后面使用{}。也就是通常我们说的遍历集合

从集合中筛选元素:{1,2,3}.{?#this>1} 筛选出所有大于1的元素 {1,2,3}.{^#this>1}筛选出第一个大于1的元素 {1,2,3}.{$#this>1}筛选出最后一个大于1的元素

调用构造函数:new com.gaoding.rocketmq.Student() 需要传完整的全限定命名

更多语法参考:https://blog.csdn.net/ShiJunzhiCome/article/details/106923163 白话文 说的比较清楚
```



```
// 加载的类信息
sc

// 查看已加载类的方法信息 
sm

// dump已加载类的bytecode到特定目录
dump
这几个命令常用于排查类冲突的时候使用
```



```
// 内存对象查询 强制GC 非常强大
vmtool

// 强制GC
vmtool --action forceGc

// 获取Spring上下文对象
// getInstances action返回结果绑定到instances变量上，它是数组。可以通过--express参数执行指定的表达式
vmtool --action getInstances -c 77e80a5e --className org.springframework.context.ApplicationContext --express 'instances[0].getBean("migrateCmsProperties")'
```



```
// 查看classloader的继承树，urls，类加载信息
classloader
```



```
// 反编译加载的代码
jad

// Memory Compiler/内存编译器，编译.java文件生成.class
mc

// 加载外部的.class文件，retransform jvm已加载的类
retransform

这三个命令通常结合来使用，用于热更新代码。平时比较少用到
// jad命令反编译，然后可以用其它编译器，比如vim来修改源码
jad --source-only com.example.demo.arthas.user.UserController > /tmp/UserController.java
// mc命令来内存编译修改过的代码
mc /tmp/UserController.java -d /tmp
// 用retransform命令加载新的字节码
retransform /tmp/com/example/demo/arthas/user/UserController.class
```



```
// 方法执行监控
monitor

// 方法执行数据观测
watch watch使用方式比较多 查看文档:https://arthas.aliyun.com/doc/watch.html

// 方法内部调用路径，并输出方法路径上的每个节点上耗时
trace

// 很多时候我们都知道一个方法被执行，但这个方法被执行的路径非常多，或者你根本就不知道这个方法是从那里被执行了，此时你需要的是stack命令
stack

// 方法执行数据的时空隧道，记录下指定方法每次调用的入参和返回信息，并能对这些不同的时间下调用进行观测
// watch虽然很方便和灵活，但需要提前想清楚观察表达式的拼写，这对排查问题而言要求太高，因为很多时候我们并不清楚问题出自于何方，只能靠蛛丝马迹进行猜测。
// 这个时候如果能记录下当时方法调用的所有入参和返回值、抛出的异常会对整个问题的思考与判断非常有帮助
tt
```



```
火焰图
profiler

cpu 按CPU消耗分析
wall 耗时跟踪, 默认按CPU消耗分析，如果方法在等待远程调用，数据库返回的阶段，不会统计在内。而耗时跟踪会更适合于找慢调用的情况
alloc 顶部框架是已分配对象的类，计数器是堆中的记录
lock 顶部框架是锁/监视器的类,计数器是进入此锁/监视器所需的纳秒数
itimer 纯java方法的cpu消耗跟踪,如果内核参数不符合又无法改变,那cpu模式可以降级为itimer模式,不读取perf_event仅采集java方法,不采集内核调用
```

