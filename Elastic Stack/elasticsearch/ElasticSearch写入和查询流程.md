# ElasticSearch写入和查询流程

## 写入流程

分片是`ElasticSearch`中最小的数据分配单位，即一个分片总是作为一个整体被分配到集群中的某个节点，一个分片是由多个`Segment`构成的，如下图所示

![img](images/ElasticSearch写入和查询流程/Ciqah158kASAPMPyAABiTVfbZ1w517.png)

`Segment`是最小的数据存储单元

`ElasticSearch`每隔一段时间会产生一个新的`Segment`，用于写入最新的数据，旧的`Segment`是不可改变的，只能用于数据查询，是无法继续向其中写入数据的

在很多分布式系统中都能看到类似的设计，这种设计有下面几点好处

- 旧`Segment`不支持修改，那么在读操作的时候就不需要加锁，省去了锁本身以及竞争锁相关的开销
- 只有最新的`Segment`支持写入，可以实现顺序写入的效果，增加写入性能
- 只有最新的`Segment`支持写入，可以更好的利用文件系统的`Cache`进行缓存，提高写入和查询性能

写入请求会首先发往协调节点`Coordinating Node`，协调节点根据`Document Id`找到对应的主分片所在的节点，接下来由主分片所在节点处理写入请求

先是写入`Transaction Log`【很多分布式系统都有`WAL(Write-ahead Log)`的概念，可以防止数据丢失】，而后将数据写入内存中

> 关于`Transaction Log`
>
> Changes to Lucene are only persisted to disk during a Lucene commit, which is a relatively expensive operation and so cannot be performed after every index or delete operation. Changes that happen after one commit and before another will be removed from the index by Lucene in the event of process exit or hardware failure. 如果每次都对index或delete等请求执行一次Lucene提交是比较消耗性能的，但是如果不这么做又可能导致一次提交之后和另一次提交之前的数据丢失。所以需要引入Transaction Log来解决这个问题

默认情况下内存中的数据每隔一秒会同步到`FileSystem Cache`中，`Cache`中的数据在后续查询中已经可以被查询了，所以`Elastic Search`被称为近实时查询

默认情况下每隔 30s会将`FileSystem cache`中的数据写入磁盘中，当然为了降低数据丢失的概率，可以将这个时间缩短甚至设置成同步的形式，但有性能损耗

客户端在进行操作的时候可以配置一致性策略，这里可以设置三种副本写入策略:

- `quorum`默认为`quorum`策略，即超过半数副本写入成功之后，相应写入请求即可返回给客户端
- `one`该策略是只要成功写入一个副本，即可向客户端返回
- `all`该策略是要成功写入所有副本之后，才能向客户端返回

`ElasticSearch`的删除操作只是逻辑删除，在每个`Segment`中都会维护一个`.del`文件，删除操作会将相应`Document`在`.del`文件中标记为已删除，查询时依然可以查到，但是会在结果中将这些"已删除"的`Document`过滤掉。由于旧`Segment`文件无法修改，`ElasticSearch`是无法直接进行修改的，而是引入了版本的概念，它会将旧版本的`Document`在`.del`文件中标记为已删除，而将新版本的`Document`索引到最新的`Segment`中。另外随着数据的不断写入，将产生很多小`Segment`文件，`ElasticSearch`会定期进行`Segment Merge`，从而减少碎片文件，降低文件打开数，提升 I/O 性能。在 `Merge`过程中可以同时根据`.del`文件，将被标记的`Document`真正删除，此时才是真正的物理删除

## 查询流程

读操作分为两个阶段:查询阶段和聚合提取阶段

在查询阶段中协调节点接受到读请求，并将请求分配到相应的分片上(如果没有特殊指定，请求可能落到主分片，也有可能落到副本分片，由协调节点的负载均衡算法来确定)。默认情况下每个分片会创建一个固定大小的优先级队列(其中只包含`Document Id`以及`Score`，并不包含`Document`的具体内容)，并以`Score`进行排序，返回给协调节点。如下图所示:

![img](images/ElasticSearch写入和查询流程/Cgq2xl58kASAVIIqAADyYyiGJl0221.png)

在聚合阶段中协调节点会将拿到的全部优先级队列进行合并排序，然后再通过`Document ID`查询对应的`Document`并将这些`Document`组装到队列里返回给客户端

## ElasticSearch通过Transaction Log保证数据可靠

我们知道了`Transaction Log`的目的是确保操作记录不丢失，那么问题就来了，`Transaction Log`有多可靠？

默认情况下`Transaction Log`会每隔5秒或者在一个写请求`(index，delete，update，bulk)`完成之后执行一次`fsync`操作，这个进程会在所有的主`shard`和副本`shard`上执行。 在`Transaction Log`同步刷盘前客户端是不会收到`200`的响应。在每个请求完成之后执行一次`Transaction Log`的`fsync`操作还是比较耗时的

默认的`Transaction Log`的配置如下:

```
"index.translog.durability": "request"
```

如果在一个大数据量的集群中数据并不是很重要，那么就可以设置成每隔5秒进行异步`fsync`操作`Transaction Log`，配置如下：

```
PUT /my_index/_settings
{
    "index.translog.durability": "async",
    "index.translog.sync_interval": "5s"
}
```

上面的配置可以在每个`index`中设置，并且随时都可以动态请求生效，所以如果我们的数据相对来说并不是很重要的时候，我们开启异步刷新`Transaction Log`这个操作这样性能可能会更好，但坏的情况下可能会丢失5秒之内的数据，所以在设置之前要考虑清楚业务的重要性