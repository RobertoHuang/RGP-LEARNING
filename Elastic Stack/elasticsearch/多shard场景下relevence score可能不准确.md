# 多shard场景下relevence score可能不准确

- 多`shard`场景下`relevence score`可能不准确的原因

  >如果你个`index`有多个`shard`的话可能搜索结果的排序会不准确。主要原因是`TF/IDF`的算法，`ES`在计算`IDF`值时默认只会计算当前`shard`的`IDF`值，而不会把整个`index`作为基数来计算，这样做的目的当前是为了性能，这也是多`shard`场景下`relevance score`不准确的原因

- 多`shard`场景下`relevence score`可能不准确解决方式

  - 生产环境下数据量大`ES`会尽可能实现均匀分配。在生产环境中一般数据量都是很大，在大数据量的概率学背景下`ES`都是在多个`shard`中均匀路由数据的，路由的时候根据`_id`实现负载均衡，此时各个`shard`中的个数基本一致。因此`IDF`值也基本一致
  - 在测试环境下可以在建立索引时将`primary shard`设置为1个`number_of_shards=1`，如果说只有一个`shard`那么当然所有的`document`都在这个`shard`里面，就没有这个问题了
  - 测试环境下搜索附带`search_type=dfs_query_then_fetch`参数，此时在计算一个`doc`的相关度分数的时候就会将所有`shard`中的`doc`来做为`IDF`的基数，这样做能确保准确性。但是在生产环境下不推荐设置这个参数，因为性能很差