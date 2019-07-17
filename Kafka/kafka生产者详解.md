# Kafka Producer

> 从编程角度来讲生产者就是向`Kafka`发送消息的应用，`KafkaProducer`是线程安全的

## 生产者示例

```java
public class KafkaProducerTest {
    public static void main(String[] args) {
        /**
         * 1.配置生产者客户端参数及创建响应的生产者实例
         * 2.构建待发送的消息(ProducerRecord)
         * 3.发送消息，最后关闭生产者实例
         ***/
        Properties properties = new Properties();
        // 集群地址，多个服务器用"，"分隔
        properties.put("bootstrap.servers", "127.0.0.1:9092");
        // key、value的序列化，此处以字符串为例，使用kafka已有的序列化类
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // 创建生产者
        Producer<String, String> producer = new KafkaProducer<>(properties);
        for (int i = 0; i < 10; i++) {
            long runtime = new Date().getTime();
            String message = runtime + "发送的消息内容:" + i;
            // 写入名为"topic-test"的topic
            ProducerRecord<String, String> producerRecord = new ProducerRecord("topic-test", message);
            producer.send(producerRecord);
            System.out.println("写入topic-test：" + message);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        producer.close();
    }
}
```

## ProducerRecord

```java
public class ProducerRecord<K, V> {
    // 主题
    private final String topic;
    // 分区
    private final Integer partition;
    // 消息头
    private final Headers headers;
    // 消息键
    private final K key;
    // 消息值
    private final V value;
    // 消息的时间戳
    private final Long timestamp;
}
```

> 其中`topic`和`partition`字段分别代表消息要发往的主题和分区号。`Headers`是附加属性
>
> `key`是用来指定消息的键，它不仅是消息的附加消息，还可以用来计算分区号进而让消息发往特定的分区

## Producer Exception

> `KafkaProducer`中一般会发生两种类型的异常:可重试异常和不可重试异常
>
> 常见的不可重试异常:
>
> `RecordTooLargeException`等，`Kafka`不会对这类异常进行任何重试(重试也无济于事)，直接抛出异常
>
> 常见的可重试异常:
>
> `NetworkException` ，`LeaderNotAvailableException`， `UnknownTopicOrPartitionException`，`NotEnoughReplicasException`，`NotCoordinatorException`等，对于可重试异常如果配置了`retries`参数`props.put(ProducerConfig.RETRIES_CONFIG, 10)`，只要在规定重试次数内自行恢复就不会抛出异常

## 序列化

> 在`Kafka`发送接收消息时，`Producer`端需要序列化，`Consumer`端需要反序列化
>
> 自定义序列化器通过继承`org.apache.kafka.common.serialization.Serializer`实现，序列化配置
>
> ```java
> // key、value的序列化，此处以字符串为例，使用kafka已有的序列化类
> properties.put("key.serializer", "xxxSerializer");
> properties.put("value.serializer", "xxxSerializer");
> ```

## 分区器

> 消息在经过序列化之后就需要确定它发往哪个分区，如果消息`ProducerRecord`中指定了`partition`字段那么就不需要分区器的作用，因为`partition`代表的就是所要发往的分区号。如果消息`ProducerRecord`中没有指定`partition`字段，那么就需要依赖分区器，根据`key`这个字段来计算`partition`的值。`Kafka`提供了默认的分区选择器`DefaultPartitioner`:
>
> ```java
> 1.如果key为空消息将以轮询的方式发往各个可用分区
> 2.如果key不为空则对key进行hash，最终根据hash值来计算分区号
> ```
>
> 自定义分区器通过继承`org.apache.kafka.clients.producer.Partitioner`实现，分区器配置
>
> ```java
> properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "xxxPartitioner");
> ```

## 生产者拦截器

> 生产者拦截器可以用来在消息发送前做一些准备工作，比如按照某个规则过滤不符合要求的消息、修改消息的内容等，也可以用来在发送回调逻辑前做一些定制化的需求，比如统计工作。`KafkaProducer`可以指定多个拦截器以形成拦截器链，拦截器链按照`INTERCEPTOR_CLASSES_CONFIG.INTERCEPTOR_CLASSES_CONFIG`参数配置的拦截器顺序一一执行(多个拦截器之间使用逗号隔开)
>
> 自定义拦截器通过继承`org.apache.kafka.clients.producer.ProducerInterceptor`实现，拦截器配置
>
> ```java
> properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, "xxxProducerInterceptor");
> ```

## 生产者客户端整体架构

![生产者客户端整体架构](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Kafka/images/%E7%94%9F%E4%BA%A7%E8%80%85%E5%AE%A2%E6%88%B7%E7%AB%AF%E6%95%B4%E4%BD%93%E6%9E%B6%E6%9E%84.png)

> 整个生产者客户端是由两个线程协调运行的，这两个线程分别为主线程和`Sender`线程
>
> 在主线程中由`KafkaProducer`创建消息，然后通过可能的拦截器，序列化器和分区器的作用之后缓存到消息累加器`RecordAccumulator`，`Sender`线程负责中`RecordAccumulator`中获取消息并发送到`Kafka`中

## RecordAccumulator

> `RecordAccumulator`主要用来缓存消息以便`Sender`线程可以批量发送，进而减少网络传输的资源消耗
>
> `RecordAccumulator`的缓存大小可以通过生产者客户端参数`buffer.memory`配置，默认`32MB`
>
> 如果生成者发送消息的速度超过发送到服务器的速度则会导致生产者空间不足，这时`KafkaProducer.send`要么被阻塞，要么抛出异常。这个取决于参数`max.block.mx`配置，默认值`60s`

![RecordAccumulator结构示意图](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Kafka/images/RecordAccumulator%E7%BB%93%E6%9E%84%E7%A4%BA%E6%84%8F%E5%9B%BE.jpg)

`ProducerBatch`的大小和`batch.size`参数有着密切关系

当一条消息流入`RecordAccumulator`时会先寻找与消息分区对应的双端队列(如果没有则新建)，再从这个双端队列尾部获取一个`ProducerBatch`(如果没有则新建)，查看`ProducerBatch`是否可以写入这个`ProducerRecord`，如果可以则写入，如果不可以则需要创建一个新的`ProducerBatch`，新的`ProducerBatch`大小取这条消息的评估大小和`batch.size`的值。如果不超过`batch.size`的值则以`batch.size`参数的大小来创建`ProducerBatch`，这样在使用完这段内存区域后，可以通过`BufferPool`的管理来进行复用，如果超过则以评估的大小来创建且这段内存区域不会被复用【这与`BufferPool`的设计有关】

## BufferPool设计

> 关于`BufferPool`大致的内存管理流程如下

![BufferPool内存管理流程图](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Kafka/images/BufferPool%E5%86%85%E5%AD%98%E7%AE%A1%E7%90%86%E6%B5%81%E7%A8%8B%E5%9B%BE.png)

具体可参考:[Kafka-生产者-BufferPool](https://www.jianshu.com/p/4ac7d8710e72)

## Sender线程发送消息

> `Sender`从`RecordAccumulator`中获取缓存的消息之后，会进一步将原本`<分区, Deque<ProducerBatch>>`的保存形式转变成`<Node, List<ProducerBatch>>`形式，其中`Node`表示`Kafka`集群的`broker`节点。因为对网络连接来说生产者客户端是与具体的`Broker`节点建立连接，也就是向具体的`Broker`节点发送消息，而并不关心消息属于哪一个分区

## InFlightRequests

> 请求在从`Sender`线程发往`Kafka`之前还会保存到`InFlightRequests`，`InFlightRequests`保存对象的具体形式为`Map<NodeId, Deque<Request>>`，它的主要作用是缓存已经发出去但还没收到响应的请求。可以通过参数`ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION`配置最多缓存的请求数(默认为5)，即每个连接最多只能缓存5个未响应请求。通过`InFlightRequests`可以获取`LeastLoadedNode`即所有`Node`中负载最小的节点【选择`LeastLoadedNode`发送请求可以使它能够尽快发出，避免因网络堵塞等异常而影响整体进度，常用于元数据请求，消费者组播协议的交互】

![判定LeastLoadedNode](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Kafka/images/%E5%88%A4%E5%AE%9ALeastLoadedNode.jpg)

## 生产者元数据请求

> 到目前为止我们只知道主题名称，对于其他一些必要消息却一无所知。`KafkaProducer`需要将此消息追加到指定主题的某个分区所对应的`Leader`副本之前，首先需要知道主题的分区数量，然后经过计算得出指定目标分区，之后`KafkaProducer`需要知道目标分区的`Leader`副本所在的`Broker`节点地址、端口等信息才能建立连接，最终才能将消息发送到`Kafka`，在这一过程中所需要的信息都属于元数据信息

`Producer Metadata`在下面两种情况下会进行更新

- `KafkaProducer`第一次发送消息时强制更新，其他时间周期性更新
- 强制更新调用`Metadata.requestUpdate()`将`needUpdate`置为`true`来强制更新
  - `initConnect`方法调用时，初始化连接
  - `poll`方法中对`handleDisconnections()`方法调用来处理连接断开的情况
  - `poll`方法中对`handleTimeOutRequests()`来处理请求超时的情况
  - 发送消息时无法找到`TopicPartition`对应的`Leader`的情况
  - 处理`Producer`响应`handleProduceResponse`如果返回关于`Metadata`过期相关的异常
  - 总而言之发生各式各样的异常，数据不同步，都认为`metadata`可能出问题了，要求更新