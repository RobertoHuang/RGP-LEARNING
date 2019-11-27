# Kafka Consumer源码

## Poll模型综述

当一个`Consumer`对象创建之后只有`poll`方法调用时，`Consumer`才会真正去连接`Kafka` 集群进行相关的操作

```java
// timeout(ms): buffer中的数据未就绪情况下等待的最长时间
// 如果设置为0立即返回buffer中已经就绪的数据（即使没有数据也立即返回）
public ConsumerRecords<K, V> poll(long timeout) {
    acquire();
    try {
        if (timeout < 0)
            throw new IllegalArgumentException("Timeout must not be negative");

        if (this.subscriptions.hasNoSubscriptionOrUserAssignment())
            throw new IllegalStateException("Consumer is not subscribed to any topics or assigned any partitions");

        // poll for new data until the timeout expires
        long start = time.milliseconds();
        long remaining = timeout;
        do {
            // 从订阅的partition中拉取数据
            // pollOnce()才是Consumer客户端拉取数据的核心实现
            Map<TopicPartition, List<ConsumerRecord<K, V>>> records = pollOnce(remaining);
            if (!records.isEmpty()) {
                // 在返回数据之前，发送下次的Fetch请求，避免用户在下次获取数据时线程block
                if (fetcher.sendFetches() > 0 || client.pendingRequestCount() > 0)
                    client.pollNoWakeup();

                // 如果配置了消费者拦截器则走消费者拦截器流程
                if (this.interceptors == null)
                    return new ConsumerRecords<>(records);
                else
                    return this.interceptors.onConsume(new ConsumerRecords<>(records));
            }

            long elapsed = time.milliseconds() - start;
            remaining = timeout - elapsed;
        } while (remaining > 0);
        return ConsumerRecords.empty();
    } finally {
        release();
    }
}
```

该方法完成如下几件事

- 检查这个`consumer`是否订阅的相应的`topic-partition`
- 调用`pollOnce()`方法获取相应的`records`
- 在返回获取的`records`前发送下一次的`fetch`请求，避免用户在下次请求时线程`block` 在`pollOnce()`方法中
- 如果在给定的时间`timeout`内获取不到可用的`records`返回空数据

这里可以看出`poll`方法的真正实现是在`pollOnce`方法中，`poll`方法通过`pollOnce`方法获取可用的数据

```java
// 一次poll过程，除了获取新数据外还会做一些必要的offset-commit和reset-offset的操作
private Map<TopicPartition, List<ConsumerRecord<K, V>>> pollOnce(long timeout) {
    // 1.获取GroupCoordinator地址并连接、加入Group、同步Group
    // 自动commit, join及sync期间group会进行rebalance(关于ConsumerCoordinator后续会有详细介绍)
    // 对于一个新建的group，group状态将会从Empty –> PreparingRebalance –> AwaiSync –> Stable
    coordinator.poll(time.milliseconds());
    // 2.更新订阅的topic-partition的offset(如果订阅的topic-partition list没有有效的offset的情况下)
    if (!subscriptions.hasAllFetchPositions())
        updateFetchPositions(this.subscriptions.missingFetchPositions());
    // 3.获取fetcher已经拉取到的数据
    Map<TopicPartition, List<ConsumerRecord<K, V>>> records = fetcher.fetchedRecords();
    if (!records.isEmpty())
        return records;
    // 到这里说明上次fetch到是的数据已经全部拉取了,需要再次发送fetch请求从broker拉取数据
    // 4.发送fetch请求,会从多个topic-partition拉取数据(只要对应的topic-partition没有未完成的请求)
    fetcher.sendFetches();
    long now = time.milliseconds();
    long pollTimeout = Math.min(coordinator.timeToNextPoll(now), timeout);
    // 5.调用poll方法发送请求(底层发送请求的接口)
    client.poll(pollTimeout, now, new PollCondition() {
        @Override
        public boolean shouldBlock() {
            // 有完成的fetcher请求的话,这里就不会block,但是block也是有最大时间限制
            return !fetcher.hasCompletedFetches();
        }
    });

    // 6.如果group需要rebalance直接返回空数据,这样更快地让group进行稳定状态
    if (coordinator.needRejoin())
        return Collections.emptyMap();
    return fetcher.fetchedRecords();
}
```

`pollOnce`主要可以分为如下几个步骤

- `coordinator.poll()`获取`GroupCoordinator`的地址，并建立相应`TCP`连接，发送 `join-group`、`sync-group`之后才真正加入到了一个`group`中，这时会获取其要消费的`topic-partition`列表，如果设置了自动 `commit`也会在这一步进行`commit`
- `updateFetchPositions()`在上一步中已经获取到了这个`consumer`实例要订阅的`topic-partition list`，这一步更新其`fetch-position offset`以便进行拉取
- `fetcher.sendFetches()`返回其`fetched records`，并更新其`fetch-position offset`，只有在`offset-commit`时(自动`commit` 时，是在第一步实现的)，才会更新其`committed offset`
- `fetcher.sendFetches()`只要订阅的`topic-partition list`没有未处理的`fetch` 请求，就发送对这个`topic-partition`的`fetch`请求，在真正发送时还是会按`node`级别去发送，`leader`是同一个`node`的`topic-partition`会合成一个请求去发送(关于`fetcher`后续也会有详细介绍)
- `client.poll()`调用底层`NetworkClient`提供的接口去发送相应的请求
- `coordinator.needRejoin()`检查是否需要重新加入组。如果当前消费组元数据发生变化或订阅的`Topic`发生变化等，那么这个`consumer group`就需要进行`rebalance`

## Consumer如何加入一个Group

以下几种情况会触发`Rebalance`操作，`Rebalance`操作其实就是一个重新加入组的过程

- 有新的消费者加入`Consumer Group`
- 有消费者宕机下线(长时间未收到`HeartbeatRequest`)
- 有消费者主动退出`Consumer Group`
- `Consumer Group`订阅的任一`Topic`出现分区数量的变化
- 消费者调用`unsubscrible()`取消对某个`Topic`的订阅

`ConsumerCoordinator.poll`该方法用于加入组、同步组、提交`offset`等

```java
public void poll(long now) {
    // 1.处理异步的响应
    invokeCompletedOffsetCommitCallbacks();

    // 2.通过subscribe()方法订阅topic并且coordinator未知,初始化Consumer Coordinator
    if (subscriptions.partitionsAutoAssigned() && coordinatorUnknown()) {
        // 获取GroupCoordinator地址,并且建立连接
        ensureCoordinatorReady();
        now = time.milliseconds();
    }

    // 2.判断是否需要重新加入group,如果订阅的partition变化或则分配的partition变化时,需要rejoin
    if (needRejoin()) {
        // due to a race condition between the initial metadata fetch and the initial rebalance,
        // we need to ensure that the metadata is fresh before joining initially. This ensures
        // that we have matched the pattern against the cluster's topics at least once before joining.
        // Rejoin group之前先刷新一下metadata(对于 AUTO_PATTERN 而言)
        if (subscriptions.hasPatternSubscription())
            client.ensureFreshMetadata();

        // 确保group是active;加入group;分配订阅的partition
        ensureActiveGroup();
        now = time.milliseconds();
    }

    // 3.检查心跳线程运行是否正常
    // 如果心跳线程失败则抛出异常,反之更新poll调用的时间
    pollHeartbeat(now);
    // 4.自动commit时,当定时达到时进行自动commit
    maybeAutoCommitOffsetsAsync(now);
}
```

- 通过 `subscribe()`方法订阅` topic` 并且`coordinator`未知，就初始化`Consumer Coordinator`(在 `ensureCoordinatorReady()`中实现，主要的作用是发送查找`GroupCoordinator`请求并建立连接)

- 判断是否需要重新加入`group`，如果订阅的`partition`变化或则分配的`partition`变化等需要`rejoin`，通过 `ensureActiveGroup()` 发送`join-group`、`sync-group`请求，加入`group`并获取其`assign`的`tp list`

- 检测心跳线程运行是否正常

- 如果设置的是自动`commit`，如果定时达到自动`commit`

在上述步骤一中查找并连接到对应的`GroupCoordinator`后，加入组过程交由`ensureActiveGroup`完成

```java
// 确保Group是active并且加入该Group
public void ensureActiveGroup() {
    // always ensure that the coordinator is ready because we may have been disconnected
    // when sending heartbeats and does not necessarily require us to rejoin the group.
    // 确保GroupCoordinator已经连接
    ensureCoordinatorReady();
    // 启动心跳发送线程(并不一定发送心跳,满足条件后才会发送心跳)
    startHeartbeatThreadIfNeeded();
    // 发送JoinGroup请求,并对返回的信息进行处理【加入组核心流程】
    joinGroupIfNeeded();
}
```

`join-group`的请求是在`joinGroupIfNeeded()`中实现的

```java
// 加入组请求
public void joinGroupIfNeeded() {
    while (needRejoin() || rejoinIncomplete()) {
        ensureCoordinatorReady();
        // call onJoinPrepare if needed. We set a flag to make sure that we do not call it a second
        // time if the client is woken up before a pending rebalance completes. This must be called
        // on each iteration of the loop because an event requiring a rebalance (such as a metadata
        // refresh which changes the matched subscription set) can occur while another rebalance is
        // still in progress.
        // 触发onJoinPrepare, 包括offset commit和rebalance listener
        if (needsJoinPrepare) {
            onJoinPrepare(generation.generationId, generation.memberId);
            needsJoinPrepare = false;
        }

        // 初始化JoinGroup请求并发送该请求
        RequestFuture<ByteBuffer> future = initiateJoinGroup();
        client.poll(future);
        resetJoinGroupFuture();

        // join succeed这一步时时间上sync-group已经成功了
        if (future.succeeded()) {
            needsJoinPrepare = true;
            onJoinComplete(generation.generationId, generation.memberId, generation.protocol, future.value());
        } else {
            RuntimeException exception = future.exception();
            if (exception instanceof UnknownMemberIdException || exception instanceof RebalanceInProgressException || exception instanceof IllegalGenerationException)
                continue;
            else if (!future.isRetriable())
                throw exception;
            time.sleep(retryBackoffMs);
        }
    }
}
```

加入组、同步组是在`initiateJoinGroup`中完成的，此处不详细展开说明。大致流程如下

- 如果`group`是新的`group.id`，那么此`group`初始化的状态为**Empty**
- 当`GroupCoordinator`接收到`consumer`的`join-group`请求后，由于此时这个`group`的`member`列表还是空，对于一个新的`consumer group`而言当第一个`consumer`实例加入后将会被选为`leader`
- 如果`GroupCoordinator`接收到`Leader`发送`join-group`请求将会触发`rebalance`，此时`GroupCoordinator`将会在服务端新建一个延迟任务，等待`Follow`发送`join-group`请求且状态变为**PreparingRebalance**
- `GroupCoordinator`将会等待一定的时间，如果在一定时间内接收到`join-group`请求的`consumer`会将延迟任务时间窗口继续往后，待延迟任务结束后`group`会变为**AwaitSync**状态，并且`GroupCoordinator`会向这个`group`的所有`member`返回其`response`
- `consumer`在接收到`GroupCoordinator`的`response`后，如果这个`consumer`是`group`的`leader`，那么这个`consumer`将会负责为整个`group assign partition`订阅分配。所有组内成员将往`GroupCoordinator`发送`SyncGroupRequest`请求【`leader`将在请求中带上订阅分配结果，而`follower`会发送一个空列表】
- `GroupCoordinator`在接收到`leader`发来的请求后，会将`assign`的结果返回给所有已经发送`sync-group`请求的`consumer`实例，并且`group`的状态将会转变为**Stable**，如果后续再收到`sync-group`请求由于`group`的状态已经是`Stable`，将会直接返回其分配结果【该部分流程涉及到`GroupCoordinator`相关，后续会介绍】

`offset`的获取和提交功能也是由`ConsumerCoordinator`实现的，在上文介绍到`Kafka`使用`SubscriptionState`用于追踪`TopicPartition`和`Offset`的对应关系。在`SubscriptionState`中使用 `TopicPartitionState`记录了每个`TopicPartition`的消费状况。`TopicPartitionState.position`字段则记录了消费者下次要从服务端获取的消息的`offset`，因为在下次`poll`的时候可以保证上次的数据已经处理完成，故可以发送`OffsetCommitRequest`提交`offset`，当项目第一次启动(没有消费历史数据)或消费组发生`rebalance`等会重置消费组记录的`offset`信息，此时可以发送`OffsetFetchRequest`请求获取到当前消费分区对应的`offset`信息

**消费者分区分配策略由`PartitionAssignor`完成，可以实现`PartitionAssignor`完成自定义分区分配**

## Consumer Heartbeat分析

- `heartbeat.interval.ms`心跳间隔
- `session.timeout.ms`当`consumer`由于某种原因不能发送`heartbeat`到`GroupCoordinator`并且超时时间超过`session.timeout.ms`时，就会默认该`consumer`已经退出。并且触发`rebalance`操作

心跳是确定`consumer`存活、加入或者退出`group`的有效手段。客户端可以根据心跳响应是否有错误码做对应处理

## Fetch拉取消息

`Fetch`主要用于从服务端拉取消息，它主要分为两步

- `fetcher.sendFetches`发送拉取消息请求，将获取到消息结果放到队列中
- `fetcher.fetchedRecords`从队列中获取到上一步已经拉取到的消息列表

```java
// 向订阅的所有partition(只要该leader暂时没有拉取请求)所在leader发送fetch请求
public int sendFetches() {
    // 1.创建 Fetch Request
    Map<Node, FetchRequest.Builder> fetchRequestMap = createFetchRequests();
    for (Map.Entry<Node, FetchRequest.Builder> fetchEntry : fetchRequestMap.entrySet()) {
        final FetchRequest.Builder request = fetchEntry.getValue();
        final Node fetchTarget = fetchEntry.getKey();
        log.debug("Sending fetch for partitions {} to broker {}", request.fetchData().keySet(), fetchTarget);

        // 2.发送Fetch Request
        client.send(fetchTarget, request)
                .addListener(new RequestFutureListener<ClientResponse>() {
                    @Override
                    public void onSuccess(ClientResponse resp) {
                        FetchResponse response = (FetchResponse) resp.responseBody();
                        if (!matchesRequestedPartitions(request, response)) {
                            // obviously we expect the broker to always send us valid responses, so this check
                            // is mainly for test cases where mock fetch responses must be manually crafted.
                            log.warn("Ignoring fetch response containing partitions {} since it does not match the requested partitions {}", response.responseData().keySet(), request.fetchData().keySet());
                            return;
                        }

                        Set<TopicPartition> partitions = new HashSet<>(response.responseData().keySet());
                        FetchResponseMetricAggregator metricAggregator = new FetchResponseMetricAggregator(sensors, partitions);

                        for (Map.Entry<TopicPartition, FetchResponse.PartitionData> entry : response.responseData().entrySet()) {
                            TopicPartition partition = entry.getKey();
                            long fetchOffset = request.fetchData().get(partition).offset;
                            FetchResponse.PartitionData fetchData = entry.getValue();
                            // 3.成功获取到消息后加入CompletedFetch
                            completedFetches.add(new CompletedFetch(partition, fetchOffset, fetchData, metricAggregator, request.version()));
                        }

                        sensors.fetchLatency.record(resp.requestLatencyMs());
                        sensors.fetchThrottleTimeSensor.record(response.getThrottleTime());
                    }

                    @Override
                    public void onFailure(RuntimeException e) {
                        log.debug("Fetch request to {} for partitions {} failed", fetchTarget, request.fetchData().keySet(), e);
                    }
                });
    }
    return fetchRequestMap.size();
}
```

在每次发送`fetch`请求时都会向所有可发送的`topic-partition`发送`fetch`请求(调用`fetcher.sendFetches`)，拉取到的数据可需要多次`pollOnce`循环才能处理完，因为`Fetcher`线程是在后台运行，这也保证了尽可能少地阻塞用户的处理线程，因为如果`Fetcher`中没有可处理的数据用户的线程是会阻塞在`poll`方法中的

```java
// 返回获取到的fetched records，并更新the consumed position
public Map<TopicPartition, List<ConsumerRecord<K, V>>> fetchedRecords() {
    Map<TopicPartition, List<ConsumerRecord<K, V>>> drained = new HashMap<>();
    // 在max.poll.records中设置单词最大的拉取条数
    int recordsRemaining = maxPollRecords;

    while (recordsRemaining > 0) {
        // nextInLineRecords为空或nextInLineRecords已经被消费过
        if (nextInLineRecords == null || nextInLineRecords.isDrained()) {
            // 当一个nextInLineRecords处理完,就从completedFetches处理下一个完成的Fetch请求 
            CompletedFetch completedFetch = completedFetches.poll();
            if (completedFetch == null)
                break;
            // 获取下一个要处理的nextInLineRecords
            nextInLineRecords = parseCompletedFetch(completedFetch);
        } else {
            TopicPartition partition = nextInLineRecords.partition;
            // 拉取records更新position
            List<ConsumerRecord<K, V>> records = drainRecords(nextInLineRecords, recordsRemaining);
            if (!records.isEmpty()) {
                List<ConsumerRecord<K, V>> currentRecords = drained.get(partition);
                if (currentRecords == null) { 
                    // 正常情况下一个node只会发送一个request,一般只会有一个
                    drained.put(partition, records);
                } else {
                    List<ConsumerRecord<K, V>> newRecords = new ArrayList<>(records.size() + currentRecords.size());
                    newRecords.addAll(currentRecords);
                    newRecords.addAll(records);
                    drained.put(partition, newRecords);
                }
                recordsRemaining -= records.size();
            }
        }
    }

    return drained;
}
```

```java
private List<ConsumerRecord<K, V>> drainRecords(PartitionRecords<K, V> partitionRecords, int maxRecords) {
    if (!subscriptions.isAssigned(partitionRecords.partition)) {
        // this can happen when a rebalance happened before fetched records are returned to the consumer's poll call
        log.debug("Not returning fetched records for partition {} since it is no longer assigned", partitionRecords.partition);
    } else {
        // note that the consumed position should always be available as long as the partition is still assigned
        long position = subscriptions.position(partitionRecords.partition);
        // 这个TopicPartition不能来消费了,比如调用 pause
        if (!subscriptions.isFetchable(partitionRecords.partition)) {
            log.debug("Not returning fetched records for assigned partition {} since it is no longer fetchable", partitionRecords.partition);
        } else if (partitionRecords.fetchOffset == position) {//note: offset 对的上,也就是拉取是按顺序拉的
            // 获取该TopicPartition对应的records,并更新partitionRecords的fetchOffset(用于判断是否顺序)
            List<ConsumerRecord<K, V>> partRecords = partitionRecords.drainRecords(maxRecords);
            if (!partRecords.isEmpty()) {
                long nextOffset = partRecords.get(partRecords.size() - 1).offset() + 1;
                log.trace("Returning fetched records at offset {} for assigned partition {} and update position to {}", position, partitionRecords.partition, nextOffset);
                // 更新消费的到offset(the fetch position)
                subscriptions.position(partitionRecords.partition, nextOffset);
            }

            // 获取Lag(即position与hw之间差值),hw为null时才返回null
            Long partitionLag = subscriptions.partitionLag(partitionRecords.partition);
            if (partitionLag != null)
                this.sensors.recordPartitionLag(partitionRecords.partition, partitionLag);
            return partRecords;
        } else {
            // these records aren't next in line based on the last consumed position, ignore them they must be from an obsolete request
            log.debug("Ignoring fetched records for {} at offset {} since the current position is {}", partitionRecords.partition, partitionRecords.fetchOffset, position);
        }
    }

    partitionRecords.drain();
    return Collections.emptyList();
}
```

- 通过`completedFetches.poll()`获取已经成功的`fetch response`(在`sendFetches()`方法中会把成功的结果放在这个集合中，是拆分为`topic-partition`的粒度放进去的)

- `parseCompletedFetch()` 处理上面获取的`completedFetch`，构造成`PartitionRecords`类型

- 通过`drainRecords()`方法处理`PartitionRecords`对象，在这个里面会去验证`fetchOffset`是否能对得上，只有`fetchOffset`是一致的情况下才会去处理相应的数据并更新`the fetch offset`，如果`fetchOffset`不一致，这里就不会处理`the fetch offset`就不会更新，下次`fetch`请求时是会接着`the fetch offset`的位置去请求相应的数据。最后返回相应的`Records`数据


## Consumer两种订阅模式下Offset的提交

- 订阅模式(`AUTO_TOPICS`和`AUTO_PATTERN`) `Offset`提交如上所述

- 分配模式(`USER_ASSIGNED`) - `Offset`提交比较特殊，我们回过头来看`ConsumerCoordinator`的`Poll`方法

  ```java
  // 通过subscribe()方法订阅topic并且coordinator未知,初始化Consumer Coordinator
  if (subscriptions.partitionsAutoAssigned() && coordinatorUnknown()) {
      // 获取GroupCoordinator地址,并且建立连接
      ensureCoordinatorReady();
      now = time.milliseconds();
  }
  ```

  如果使用的是`assign`模式也即是非`AUTO_TOPICS`或`AUTO_PATTERN`模式时，`Consumer`实例在调用`poll`方法时是不会向`GroupCoordinator`发送`join-group/sync-group/heartbeat`请求的，即`GroupCoordinator`是拿不到这个`Consumer`实例的相关信息，也不会去维护这个`member`是否存活，这种情况下就需要用户自己管理自己的处理程序。但是在这种模式是可以进行`offset commit`的

服务端对`OffsetCommitRequest`请求的处理流程如下

```scala
def handleCommitOffsets(groupId: String, memberId: String, generationId: Int, offsetMetadata: immutable.Map[TopicPartition, OffsetAndMetadata], responseCallback: immutable.Map[TopicPartition, Short] => Unit) {
  if (!isActive.get) {
    responseCallback(offsetMetadata.mapValues(_ => Errors.GROUP_COORDINATOR_NOT_AVAILABLE.code))
  } else if (!isCoordinatorForGroup(groupId)) {
    responseCallback(offsetMetadata.mapValues(_ => Errors.NOT_COORDINATOR_FOR_GROUP.code))
  } else if (isCoordinatorLoadingInProgress(groupId)) {
    responseCallback(offsetMetadata.mapValues(_ => Errors.GROUP_LOAD_IN_PROGRESS.code))
  } else {
    groupManager.getGroup(groupId) match {
      case None =>
        if (generationId < 0) {
          // the group is not relying on Kafka for group management, so allow the commit
          // note: 不使用group-coordinator管理的情况
          // note: 如果groupID不存在就新建一个GroupMetadata, 其group状态为Empty,否则就返回已有的groupid
          // note: 如果simple的groupId与一个active的group重复了,这里就有可能被覆盖掉了
          val group = groupManager.addGroup(new GroupMetadata(groupId))
          doCommitOffsets(group, memberId, generationId, offsetMetadata, responseCallback)
        } else {
          // 过期的offset提交
          // or this is a request coming from an older generation. either way, reject the commit
          responseCallback(offsetMetadata.mapValues(_ => Errors.ILLEGAL_GENERATION.code))
        }
      case Some(group) =>
        doCommitOffsets(group, memberId, generationId, offsetMetadata, responseCallback)
    }
  }
}
```

如果这个`group`还不存在(`groupManager`没有这个`group`信息)，并且`generation`为`-1`(一般情况下应该都是这样)，就新建一个`GroupMetadata`其`Group`状态为`Empty`，现在`group`已经存在就调用`doCommitOffsets()`提交`offset`。所以`offset`提交的核心处理逻辑是在`doCommitOffsets`中完成的

```scala
private def doCommitOffsets(group: GroupMetadata, memberId: String, generationId: Int, offsetMetadata: immutable.Map[TopicPartition, OffsetAndMetadata], responseCallback: immutable.Map[TopicPartition, Short] => Unit) {
  var delayedOffsetStore: Option[DelayedStore] = None

  group synchronized {
    if (group.is(Dead)) {
      responseCallback(offsetMetadata.mapValues(_ => Errors.UNKNOWN_MEMBER_ID.code))
    } else if (generationId < 0 && group.is(Empty)) {
      // 来自assign的情况
      // the group is only using Kafka to store offsets
      delayedOffsetStore = groupManager.prepareStoreOffsets(group, memberId, generationId, offsetMetadata, responseCallback)
    } else if (group.is(AwaitingSync)) {
      // 消费组在重平衡，返回错误信息
      responseCallback(offsetMetadata.mapValues(_ => Errors.REBALANCE_IN_PROGRESS.code))
    } else if (!group.has(memberId)) {
      // 可能assign方式和auto方式使用了同一个消费组，拒绝assign方式提交offset
      responseCallback(offsetMetadata.mapValues(_ => Errors.UNKNOWN_MEMBER_ID.code))
    } else if (generationId != group.generationId) {
      // generationId值不一致 不能提交offset(可能是已经过期的请求)
      responseCallback(offsetMetadata.mapValues(_ => Errors.ILLEGAL_GENERATION.code))
    } else {
      val member = group.get(memberId)
      completeAndScheduleNextHeartbeatExpiration(group, member)
      delayedOffsetStore = groupManager.prepareStoreOffsets(group, memberId, generationId, offsetMetadata, responseCallback)
    }
  }

  // store the offsets without holding the group lock
  delayedOffsetStore.foreach(groupManager.store)
}
```

- 如果是来自`assign`模式的请求，并且其对应的`group`的状态为`Empty`，那么就记录这个`offset`
- 如果是来自`assign`模式的请求但这个`group`的状态不为`Empty(!group.has(memberId))`，即这个`group`已经处在活跃状态，`assign`模式下的`group`是不会处于的活跃状态的，可以认为是`assign`模式使用的`group.id`与`subscribe`模式下使用的`group`相同，这种情况下就会拒绝`assign`模式下的这个`offset commit`请求

|   模式   |                   不同之处                   |                           相同之处                           |
| :------: | :------------------------------------------: | :----------------------------------------------------------: |
| 订阅模式 | 使用`Kafka Group`管理自动进行`Rebalance`操作 |                 可以在`Kafka`中保存`Offset`                  |
| 分配模式 |            用户自己进行相关的处理            | 也可以进行`offset commit`，但是尽量保证`group.id`唯一性，如果使用一个与上面模式一样的`group`，`offset commit`请求将会被拒绝 |

## 附录

### SubScriptionState

`SubScriptionState`主要用于跟踪`TopicPartition`和`offset`对应关系，一下是`SubScriptionState`的主要字段

```java
// 订阅的模式
private SubscriptionType subscriptionType;

// 正则表达式相关
private Pattern subscribedPattern;

// 如果使用AUTO_PATTERN/AUTO_TOPICS时订阅的Topic列表数据
private Set<String> subscription;

// 针对USER_ASSIGNED订阅模式，该集合包含分配给当前消费者的TopicPartition集合
private Set<TopicPartition> userAssignment;

// 无论使用什么订阅模式都使用此集合记录每个TopicPartition的消费状态
private Map<TopicPartition, TopicPartitionState> assignment;

// 我们知道消费者组会有一个leader，它会使用该集合记录消费者组中所有消费者的订阅的topic,而其他follower的该集合只保存自己的订阅的topic
private Set<String> groupSubscription;

// 是否需要从GroupCoordinator获取最近提交的offset
private boolean needFetchCommitedOffsets;

// 默认OffsetResetStrategy策略(主要用于初次启动消费者时设置从哪里开始继续消费)
private OffsetResetStrategy defaultResetStrategy;

// 用于监听分区分配
private ConsumerRebalanceListener listener;
```

### TopicPartitionState

`TopicpartitionState`记录`TopicPartition`的一些信息

```java
// 下次去拉取消息的offset
private Long position; 

// 最后一次拉取时的高水位
private Long highWatermark; 

// consumer已经处理完的最新一条消息的offset，消费者提交offset时会更新这个值
private OffsetAndMetadata committed;  

// 这个分区是否被用户手动暂停
private boolean paused;  

// 这个topic-partition offset重置的策略
private OffsetResetStrategy resetStrategy;
```