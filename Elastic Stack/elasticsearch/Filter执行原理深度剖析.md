# Filter执行原理深度剖析

- 在倒排索引中查找搜索串，获取`document list`，以`date`来举例

  |    word    | doc1 | doc2 | doc3 |
| :--------: | :--: | :--: | :--: |
  | 2017-01-01 |  *   |  *   |      |
  | 2017-02-02 |      |  *   |  *   |
  | 2017-03-03 |  *   |  *   |  *   |
  
  `filter：2017-02-02`到倒排索引中一找，发现`2017-02-02`对应的`document list`是`doc2`，`doc3`

- 为每个在倒排索引中搜索到的结果，构建一个`bitset`

  这一步非常重要，使用找到的`doc list`构建一个`bitset`(就是一个二进制的数组)，数组每个元素都是`0`或`1`【匹配就是`1`，不匹配就是`0`】用来标识一个`doc`对一个`filter`条件是否匹配

  - `doc1`不匹配这个`filter`的
  - `doc2`和`doc3`是匹配这个`filter`的

  所以构成的`bitset`为`[0, 1, 1]`尽可能用简单的数据结构去实现复杂的功能，可以节省内存空间提升性能

- 遍历每个过滤条件对应的`bitset`，优先从最稀疏的开始搜索查找满足所有条件的`document`

  由于一次性其实可以在一个`search`请求中发出多个`filter`条件，每个`filter`条件都会对应一个`bitset`，遍历每个`filter`条件对应的`bitset`先从最稀疏的开始遍历，如`[0, 0, 0, 1, 0, 0] -> [0, 1, 0, 1, 0, 1]`。先遍历比较稀疏的`bitset`，就可以先过滤掉尽可能多的数据

- 缓存`bitset`

  在最近的`256`个`filter`中有某个`filter`超过了一定的次数(次数不固定)，就会自动缓存这个`filter`对应的`bitset`。`filter`针对小的`segment`获取到的结果是可以不缓存的(`segment`记录数小于`1000`，或者`segment`大小小于`index`总大小的`3%`)。因为此时`segment`数据量很小哪怕是扫描也是很快的，`segment`会在后台自动合并，小`segment`很快会跟其它小`segment`合并成大`segment`，此时缓存就没有什么意思了

  **`filter`比`query`好的原因除了不计算相关度分数以外还有这个`caching bitset`。所以`filter`性能会很高**

- `filter`大部分情况下来说在`query`之前执行，先尽量过滤掉尽可能多的数据
  
  - `query`是会计算`doc`对搜索条件的`relevance score`，还会根据这个`score`去排序
- `filter`只是简单过滤出想要的数据，不计算`relevance score`也不排序
  
- 如果`document`有新增或修改，那么`cached bitset`会被自动更新

  这个过程是`ES`内部做的，比如之前的`bitset`是`[0,0,1]`，那么现在插入或更新了一条数据`doc4`，而且`doc4`也在缓存的`bitset[0,0,1]`的`filter`查询条件中，那么`ES`会自动更新这个`bitset`变为`[0,0,1,1]`。以后只要有相同的`filter`条件的查询请求打过来，就会直接使用这个过滤条件对应的`bitset`。这样查询性能就会很高，一些热的`filter`查询，就会被`cache`住

