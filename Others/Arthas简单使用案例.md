# Arthas

# 常用命令

- `dashboard`查看当前系统实时数据面板
- `thread`查看当前线程信息，查看线程的堆栈【查看最忙的线程、查看阻塞其他线程的线程】
- `jvm`查看当前`JVM`信息【查看线程概览、查看文件描述符相关信息】
- `sysenv`查看当前`JVM`环境变量
- `sysprop`查看当前`JVM`的系统属性【可以修改当前系统属性】
- `vmoption`查看、更新`VM`诊断相关的参数【调整`JVM`参数】
- `logger`查看`logger`信息、更新`logger level`
- `mbean`可以便捷的查看或监控`Mbean`的属性信息【待了解】
- `getstatic`方便查看类的静态属性【推荐直接使用`ognl`命令，更加灵活】
- `ognl`执行`ognl`表达式【使用方式比较灵活、可参考使用案例】
- `sc`查找`JVM`已加载的类【输出类的详细信息及成员变量信息】
- `sm`查找已加载的类的方法信息
- `dump` `dump`已加载的类的二进制到特定目录
- `heapdump` `dump java heap`、类似`jmap`命令的`heap dump`功能
- `jad`反编译指定已加载类的源码，通过`--source-only`参数可以只打印出反编译的源代码
- `classloader`查看`classloader`的继承树、`urls`、类加载信息
- `mc`内存编译器，编译`.java`文件生成`.class`文件
- `redefine`加载外部的`.class`文件，`redefine jvm`已加载的类
- `monitor`方法执行监控【查看方法调用次数、成功次数、失败次数、平均`RT`、失败率等】
- `watch`让你能方便的观察到指定方法的调用情况【入参、返回值、抛出异常】
- `trace`方法内部调用路径，并输出方法路径上的每个节点上耗时
- `stack`很多时候我们都知道一个方法被执行，但这个方法被执行的路径非常多，或者你根本就不知道这个方法是从哪里被执行了，此时可以使用`stack`命令来获取调用堆栈信息
- `tt`方法执行数据时空隧道，记录下指定方法每次调用的入参和返回值并能对这些不同的时间下调用进行观测

命令详细使用方式参考:[Arthas命令列表](https://alibaba.github.io/arthas/commands.html)

# 使用案例

### 热更新代码

通过`jad/mc/redefine`命令实现动态更新代码的功能

- `jad`反编译需要修改的类

  `jad --source-only *TestController > /Users/roberto/temp/TestController.java`

- 修改反编译后的源代码到预期

  `vim /Users/roberto/temp/TestController.java`

- `sc`查找加载需要修改的类的`ClassLoader`

  `sc -d *TestController | grep classLoaderHash`

- 使用`mc`命令编译修改后的类

  `mc -c 18b4aac2 /Users/roberto/temp/TestController.java -d /Users/roberto/temp`

- 使用`redefine`命令重新加载编译好的修改后的类

  `redefine /Users/roberto/temp/zc/edu/live/training/business/controller/TestController.class`

### 动态更新应用Logger Level

`logger -c 18b4aac2  -n "loggerName" -l loggerLevel`

### 获取Spring Context

