# Kafka集群Metadata管理

- 对于集群中的每一个`Broker`都保存着相同的完整的整个集群的`metadata`信息
- `metadata`信息里包括每个`topic`的所有`partition`的信息
- `Kafka`客户端可以从任一`broker`都可以获取到需要的`metadata`信息

## Metadata从哪里来

这个问题实际上就是在问`UpdateMetaRequest`是谁在*什么时候*发送的

- 来源肯定是`KafkaController`发送的

- `Broker`变动, `Topic`创建, `Partition`增加等等时机都需要更新`Metadata`

## Metadata在服务端存储在哪里

在每个`Broker`的`KafkaServer`对象中都会创建`MetadataCache`组件, 负责缓存所有的`metadata`信息

```java
// 控制器ID
private int controllerId = -1;
// Metadata版本号
private MetadataVersion metadataVersion = new MetadataVersion();
// 分区状态
private Map<String/* topic */, Map<Integer/* partitionId */, PartitionState>> partitionCaches = new HashMap<>(16);
// 当前存活的Broker信息
private Map<Integer, Broker> livingBrokers = new HashMap<>(16);
```

```java
public class PartitionState {
    // leaderEpoch
    private int leaderEpoch;
    // ISR列表
    private List<Integer> isrReplicas;
    // 所有分区副本列表
    private List<Integer> allReplicas;
    // 当前分区所在Leader副本
    private int leaderId;
    // 离线副本
    private List<Integer> offlineReplicas;
}
```

## 谁使用Metadata信息

主要是客户端，客户端从`metadata`中获取`topic`的`partition`信息，知道`leader`是谁才可以发送和消费

## DoveMQ元数据管理较Kafka的优势

- 使用长轮询有独立线程管理元数据
- 使用`MetadataVersion`来保持`Metadata`数据最新