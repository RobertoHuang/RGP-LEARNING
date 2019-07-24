# Kafka Consumer

> 从编程角度来讲消费者就是向`Kafka`拉取消息的应用，`KafkaConsumer`是线程不安全的

## 消费者与消费组

消费者`Consumer`负责订阅`Kafka`中的主题`Topic`，并且从订阅的主题上拉取消息。与其他消息中间件不同的是在`Kafka`消费理念中还有一层消费组`Consumer Group`的概念，每一个消费者都有一个对应消费组，当消息发布到主题后只会投递给订阅它的消费组中的一个消费者，即每个分区只能被一个消费组中的一个消费者消费

- 如果所有的消费者都隶属于同一个消费组，那么生产者发送一条消息只会被一个消费者处理【点对点】
- 如果所有的消费者都隶属于不同的消费组，那么生产者发送的消息会被广播给所有的消费者【发布/订阅】

![消费者与消费组](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Kafka/images/%E6%B6%88%E8%B4%B9%E8%80%85%E4%B8%8E%E6%B6%88%E8%B4%B9%E7%BB%84.png)

消费者与消费组这种模型可以让整体的消费能力具备横向伸缩性

我们可以增加(或减少)消费者的个数来提高(或降低)整体的消费能力

对于分区数固定的情况一味地增加消费者并不会让消费能力一直得到提升，如果消费者过多(大于主题的分区个数)就会有消费者分配不到任何分区而无法消费任何消息。关于分配逻辑是通过分区分配策略进行分配的

## 消费者示例

```java
public class KafkaConsumerTest {
    private static Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerTest.class);

    public static void main(String[] args) {
        /**
         * 1.配置消费者客户端参数及创建相应的消费者实例
         * 2.拉取消息并消费，提交消费offset(默认异步提交)
         * 3.关闭消费者实例
         ***/
        Properties properties = new Properties();
        // 集群地址，多个服务器用","分隔
        properties.put("bootstrap.servers", "127.0.0.1:9092");
        // 消费组ID
        properties.put("group.id", "test-consumer-group");
        // 客户端ID
        properties.put("client.id", "test-consumer-client");
        // key、value的序列化，此处以字符串为例，使用kafka已有的序列化类
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // 创建生产者
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList("topic-test"));
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("key=" + record.key() + ", value=" + record.value());
                    System.out.println("topic=" + record.topic() + ", partition=" + record.partition() + ",offset=" + record.offset());
                }
            }
        } catch (Exception e) {
            LOGGER.error("occur exception ", e);
        } finally {
            consumer.close();
        }
    }
}
```

> `Kafka`支持集合订阅方式`subscribe(Collection)`、正则表达式订阅方式`subscribe(Pattern)`、和指定分区订阅方式`assign(Collection)`，它们分别代表三种不同的订阅状态:`AUTO_TOPICS`、`AUTO_PATTERN`、和`USER_ASSIGNED`【如果没有订阅状态为`NONE`，这三种状态是互斥的，一个消费者中只能使用其中一种】
>
> 
>
> 通过`subscribe()`方法订阅主题具有消费者自动再均衡的功能，在多个消费者的情况下可以根据分区分配策略来自动分配各个消费者与分区的关系。当消费组内的消费者增加或减少时分区分配关系会自动调整，以实现消费负载均衡及故障自动转移。而通过`assign()`方法订阅分区时是不具备消费者自动均衡的功能的

## ConsumerRecord

```java
public class ConsumerRecord<K, V> {
    // 主题
    private final String topic;
    // 分区
    private final int partition;
    // 消息在所属分区的偏移量
    private final long offset;
    // 时间戳
    private final long timestamp;
    // 时间戳的类型
    private final TimestampType timestampType;
    // 序列化后key的大小
    private final int serializedKeySize;
    // 序列化后value的大小
    private final int serializedValueSize;
    // 消息头
    private final Headers headers;
    // Key的值
    private final K key;
    // Value的值
    private final V value;
    // CRC32校验的值
    private volatile Long checksum;
}
```

> 其中`topic`和`partition`字段分别代表消息要发往的主题和分区号。`Headers`是附加属性
>
> `offset`是消息在所属分区的偏移量，`key`和`value`分别为消息的键和值，一般业务应用要读取的就是`value`

## 反序列化

> 在`Kafka`接收到消息时，`Consumer`端需要反序列化
>
> 自定义反序列化通过继承`org.apache.kafka.common.serialization.Deserializer`实现，反序列化配置
>
> ```java
> // key、value的反序列化，此处以字符串为例，使用kafka已有的反序列化类
> properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
> properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
> ```
>
> 在实际应用中如果`Kafka`提供的序列化器/反序列化器满足不了应用需求的前提下，推荐使用`Avro`、`JSON`、`Thrift`、`ProtoBuf`、`Protostuff`、`Hession`等通用的序列化工具来包装，尽可能更加通用且前后兼容

## 消息消费模型Push/Pull

|               | Push                                                         | Pull                                                         |
| ------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 实时性        | 较好，收到数据可立即发送给客户端                             | 取决于pull间隔时间                                           |
| 服务端状态    | 需要保存push状态，哪些客户端已经发送成功，哪些发送失败       | 服务端无状态                                                 |
| 客户端状态    | 客户端无状态                                                 | 需要保持当前拉取信息的状态，以便故障或重启的时候恢复         |
| 集中式/分布式 | 集中在服务端，服务端压力大                                   | 分布式，分散在各个客户端                                     |
| 负载均衡      | 服务端统一处理和控制                                         | 客户端之间分配，需要协调机制                                 |
| 其他          | 服务端需要做流量控制，无法最大化客户端的处理能力<br/>其次在客户端故障情况下，无效的push对服务端有一定负载 | 客户端的请求可能很多无效或者没有数据可供传输，浪费带宽和服务器处理能力(可以使用长轮询进行改进) |

在面对大量甚至海量客户端的时候使用`push`模型，保存大量的状态信息是个沉重的负担，加上复制`N`份数据分发的压力也会使得实时性这唯一的优点也被放小。使用`pull`模型通过将客户端状态保存在客户端，大大减轻了服务器端压力，通过客户端自身做流量控制也更容易，更能发挥客户端的处理能力，但是需要面对如何在这些客户端之间做协调的难题

## 位移提交

> 对于`Kafka`中的分区而言它的每条消息都有唯一的`offset`，用来表示消息在分区中对应的位置
>
> 在每次调用`poll()`方法时服务端返回的是还没有被消费国的消息集，要做到这一点就需要记录上一次消费时的`Offset`。并且这个`Offset`必须持久化保存而不是单单保存在内存中，否则消费者重启之后就无法知晓之前消费的`Offset`。再考虑一种情况当有新消费者加入的时候如果不持久化保存消费位移，那么这个新的消费者也无法知晓之前的消费位移(旧版`Offset`信息保存在`ZK`、新版保存在内部主题`__consumer_offsets`中)

- 过早提交未消费数据的`Offset`可能会导致丢消息
- 先消费成功后再提交`Offset`可能会导致重复消费

`Kafka`的`commitAsync()`异步提交`Offset`可能会失败的情况发生，我们首先想到的是重试

如果某一次异步提交`Offset`为`x`但是失败了然后下一次又异步提交了消费位移`x+y`这次成功了，如果这里引入了重试机制那么前一次的异步提交的`Offset`在重试的时候提交成功了，那么此时的消费位移又变为`x`。如果此时发生异常(或者重平衡)那么恢复之后的消费者(或者新的消费者)就会从`x`处开始消费消息，这样就产生重复消费的问题。为此我们可以设置一个递增的序号来维护异步提交的顺序，每次位移提交之后就增加序号相对应的值。这样在`Offset`提交失败或者需要重试的时候，可以检查所提交的`Offset`和序号值的大小，如果前者大于后者则说明有更大的位移已经提交了，不需要再进行本次重试；如果两者相同则说明可以进行重试提交

一般情况下位移提交失败的情况很少发生，后面的提交也会有成功的，所以不重试也没关系。重试会增加代码逻辑的复杂度，不重试会增加重复消费的概率。如果消费者异常退出那么这个重复消费的问题就很难避免，因为这种情况下无法及时提交`Offset`。如果消费者正常退出或发生重平衡我们可以在退出或重平衡执行之前使用同步提交的方式做最后的把关(`consumer.commitSync()`)

