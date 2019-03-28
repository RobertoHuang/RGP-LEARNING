# GroupCoordinator

`GroupCoordinator`的内容与`Consumer`端紧密相关

简单来说就是`GroupCoordinator`是负责`Consumer Group`的组成员管理与`Offset`管理

## GroupMetadataManager

`GroupMetadataManager`是`GroupCoordinator`中负责管理`Consumer Group`元数据以及`Offset`信息的组件

`storeGroup`:往`__consumer_offsets`写入`Consumer Group`元数据

`storeOffsets`:往`__consumer_offsets`写入`Consumer Group`的`Offset`信息

通过内部`Topic`记录`Group`元数据信息，可以保证

- 即使出现消费者宕机也可以找回之前提交的`Offset`信息

- 即使`Broker`宕机导致`Consumer Group`由新的`Group Coordinator`进行管理，新`Group Coordinator`也可以知道`Consumer Group`中每个消费者负责哪个分区信息

## FindCoordinatorRequest

- 通过组名称计算出落在`__consumer_offsets`的哪个`Partition`上
- 返回`__consumer_offsets`指定`Partition`的`Leader`所在的`Broker`节点信息

## JoinGroupRequest

- 如果组不存在并且`MemberID`为空则创建组后加入组
- 新成员加入调用`GroupCoordinator#addMemberAndRebalance`，创建新成员并加入组
- 已有成员加入调用`GroupCoordinator#updateMemberAndRebalance`，更新缓存的成员信息
- 可能会触发`Rebalance`操作【条件:组状态处于`Stable`、`CompletingRebalance`、`Empty`】，将消费组状态切换为`PreparingRebalance`，等待其他组成员发送`JoinGroup`请求。在收到所有组成员的加入组请求或者延迟重平衡超时后会执行回调完成加入组操作

## SyncGroupRequest

客户端加入`Consumer Group`后会发送`SyncGroupRequest`进行请求`Group`分配信息

一个`Consumer Group`中有两种角色:`Leader`和`Follower`，`Leader`会在`SyncGroupRequest`时带上经过运算得到的分配情况，而`Follower`发送的`SyncGroupRequest`的`assign`为空信息。`Broker`端根据是否为`Leader`会进行不同的处理，最后返回给消费者的是该`Group`对应`assign`信息。主要流程如下:

- 如果接收到`Leader`的`SyncGroupRequest`请求，根据`Leader`传递的分配消费分区结果构造`GroupMetadata`写入`__consumer_offsets`，将`Consumer Group`状态改为`Stable`。执行`Follow`的回调
- 如果接收到`Follower`的`SyncGroupRequest`请求，此时可能存在两种状态
  - 状态为`CompletingRebalance`则保存回调，在处理`Leader`发送的`SyncGroupRequest`完成时回调
  - 如果此时状态为`Stable`则直接从缓存中获取分配的消费分区结果，直接执行回调函数
- 处理`SyncGroupRequest`还包含一些健壮性代码:如在组状态切换时又有新的消费者加入等等...略

## LeaveGroupRequest

将对应的`MemberMetadata`的`isLeaving`字段设置为`true`并尝试完成相应的`DelayedHeartbeat`

之后将对应的`MemberMetadata`对象从`GroupMetadata`中删除，并尝试对组状态进行切换

## OffsetCommitRequest

消费者在进行正常的消费过程以及`Rebalance`操作之前，都会进行提交`Offset`操作

该过程实际上是将`Offset`信息转换成消息并追加到对应的`Offset Topic`中【`__consumer_offsets`】

## OffsetFetchRequest

当`Consumer Group`宕机后重新上线，可以通过向`GourpCoordinator`发送`OffsetFetchRequest`获取最近一次提交的`Offset`，并从该位置重新开始进行消费。该方法核心是调用了`GroupMetadataManager#getOffsets`

- 如果没有指定`TopicPartition`则返回`ConsumerGroupo`消费的所有`TopicPartition`的`Offset`，否则返回指定的`TopicPartition`对应的`Offset`信息，如果不存在则返回的`Offset`值为`-1`

## ListGroupsRequest

`ListGroupsRequest`由管理工具产生，将服务端`GroupMetadataManager`缓存的`groupMetadataCache`集合返回

## Consumer心跳处理

在每次对`Group`对应的`Member`进行操作或者每个`Consumer`定时向`GroupCoordinator`发起心跳时，会重新注册这个超时监听。通过`GroupCoordinator#completeAndScheduleNextHeartbeatExpiration`。该部分代码主要利用了延迟组件`Purgatory`，简而言之该方法主要作用就是将超时监听触发往后移动。当超时后将组成员移除

## GroupCoordinator转移

切换成`Follower`时，将`Consumer Group`元数据从缓存中删除，触发`onGroupUnloaded`响应对应阻塞请求



切换成`Leader`时，从`__consumer_offsets`恢复组数据到`GroupCoordinator`，触发`onGroupLoaded`重新注册心跳的延迟任务【从`__consumer_offsets`中恢复组元数据时，有些消费者已经下线但是元数据中依然存在消费者数据，导致`GroupCoordinator`认为消费者还在线的假象，通过心跳可以在心跳到期后移除未存活的`Topic`】

## GroupCoordinator状态分析

`GroupCoordinator`为其管理的每个`Consumer Group`都维护了一个状态机，各个转态之间转换如下

![GroupState状态切换示意图](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Kafka/images/GroupState%E7%8A%B6%E6%80%81%E5%88%87%E6%8D%A2%E7%A4%BA%E6%84%8F%E5%9B%BE.png)

消费组重新平衡操作流程:

- 当一些条件发生时将`Consumer Group`从`Stable`状态变为`PreparingRebalance`
- 然后触发延时任务等待`Consumer Grouop`中所有的`Consumer Member`发送`join-group`请求加入组，在超时后由`GroupCoordinator`向所有`Member`发送`join-group`响应，此时组状态变为`AwatingSync`状态
- `Leader Consumer`会收到各个`Member`订阅的`Topic`详细信息，等分配好`Partition`后通过`sync-group`请求将结果发送给`GroupCoordinator`，`Consumer Group`状态将变为`Stable`
- 组内所有`Consumer`获取到消费分区分配结果，开始消费
