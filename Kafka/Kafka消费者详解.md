# Kafka Consumer

> 从编程角度来讲消费者就是向`Kafka`拉取消息的应用，`KafkaConsumer`是线程不安全的
>
> 消费者`Consumer`负责订阅`Kafka`中的主题`Topic`，并且从订阅的主题上拉取消息。与其他消息中间件不同的是在`Kafka`消费理念中还有一层消费组`Consumer Group`的概念，每一个消费者都有一个对应消费组，当消息发布到主题后只会投递给订阅它的消费组中的一个消费者，即每个分区只能被一个消费组的消费者消费

![消费者与消费组](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Kafka/images/%E6%B6%88%E8%B4%B9%E8%80%85%E4%B8%8E%E6%B6%88%E8%B4%B9%E7%BB%84.png)

消费者与消费组这种模型可以让整体的消费能力具备横向伸缩性，我们可以增加(或减少)消费者的个数来提高(或降低)整体的消费能力。对于分区数固定的情况一味地增加消费者并不会让消费能力一直得到提升，如果消费者过多大于主题的分区个数就会有消费者分配不到任何分区。关于分配逻辑是通过分区分配策略进行分析的

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

> 除了使用`subscribe`订阅主题外还可以使用`assign`方法订阅指定主题分区
>
> // TODO

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

## 消息投递模式

- 如果所有的消费者都隶属于同一个消费组，那么生产者发送一条消息只会被一个消费者处理【点对点】
- 如果所有的消费者都隶属于不同的消费组，那么生产者发送的消息会被广播给所有的消费者【发布/订阅】

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

## 消息消费

// TODO 推拉模型差异

## 位移提交

> 对于`Kafka`中的分区而言它的每条消息都有唯一的`offset`，用来表示消息在分区中对应的位置