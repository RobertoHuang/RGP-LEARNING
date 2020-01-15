# ElasticSearch快速入门

## 基础的文档CRUD操作

- 添加文档数据

  ```
  POST /ecommerce/_doc/1
  {"name":"gaolujie yagao","desc":"gaoxiao meibai","price":30,"producer":"gaolujie producer","tags":["meibai","fangzhu"]}
  
  PUT /ecommerce/_doc/2
  {"name":"jiajieshi yagao","desc":"youxiao fangzhu","price":25,"producer":"jiajieshi producer","tags":["fangzhu"]}
  
  PUT /ecommerce/_doc/3
  {"name":"zhonghua yagao","desc":"caoben zhiwu","price":40,"producer":"zhonghua producer","tags":["qingxin"]}
  ```

- 查询商品检索文档

  ```
  GET /ecommerce/_doc/1
  ```

- 修改商品替换文档

  ```
  PUT /ecommerce/_doc/1
  {"name":"jiaqiangban gaolujie yagao","desc":"gaoxiao meibai","price":30,"producer":"gaolujie producer","tags":["meibai","fangzhu"]}
  ```

  替换方式有一个不好即必须带上所有的`field`才能去进行信息的修改

- 修改商品更新文档

  ```
  POST /ecommerce/_update/1
  {"doc":{"name":"jiaqiangban gaolujie yagao"}}
  ```

- 删除商品删除文档

  ```
  DELETE /ecommerce/_doc/1
  ```

## 几个典型的聚合案例

- 计算每个`tag`下的商品数量

  ```
  1.将fielddata设置为true
  PUT /ecommerce/_mapping
  {"properties":{"tags":{"type":"text","fielddata":true}}}
  
  2.进行聚合统计
  GET /ecommerce/_search
  {"aggs":{"group_by_tags":{"terms":{"field":"tags"}}}}
  ```

- 对名称中包含`yagao`的商品，计算每个`tag`下的商品数量

  ```
  GET /ecommerce/_search
  {"size":0,"query":{"match":{"name":"yagao"}},"aggs":{"all_tags":{"terms":{"field":"tags"}}}}
  ```

- 先分组再算每组的平均值，计算每个`tag`下的商品的平均价格

  ```
  GET /ecommerce/_search
  {"size":0,"aggs":{"group_by_tags":{"terms":{"field":"tags"},"aggs":{"avg_price":{"avg":{"field":"price"}}}}}}
  ```

- 计算每个`tag`下的商品的平均价格，并且按照平均价格降序排序

  ```
  GET /ecommerce/_search
  {"size":0,"aggs":{"all_tags":{"terms":{"field":"tags","order":{"avg_price":"desc"}},"aggs":{"avg_price":{"avg":{"field":"price"}}}}}}
  ```

- 按照指定的价格范围区间进行分组，然后在每组内再按照`tag`进行分组，最后再计算每组的平均价格

  ```
  GET /ecommerce/_search
  {"size":0,"aggs":{"group_by_price":{"range":{"field":"price","ranges":[{"from":0,"to":20},{"from":20,"to":40},{"from":40,"to":50}]},"aggs":{"group_by_tags":{"terms":{"field":"tags"},"aggs":{"average_price":{"avg":{"field":"price"}}}}}}}}
  ```

## 悲观锁与乐观锁并发控制

- 基于`version`的乐观锁

  ```
  # 先构造一条数据出来
  PUT /test_index/_doc/1
  {"test_field":"test test"}
  
  # 模拟两个客户端，都获取到了同一条数据
  GET test_index/1
  
  # 其中一个客户端，先更新了一下这个数据
  # 同时带上数据的版本信息，确保ES中的数据的跟客户端中的数据的版本是相同的，才能修改
  PUT /test_index/_doc/1?if_seq_no=0&if_primary_term=1
  {"test_field":"test client 1"}
  
  # 另外一个客户端尝试基于if_seq_no=0&if_primary_term=1的数据去进行修改，进行乐观锁的并发控制
  PUT /test_index/_doc/1?if_seq_no=0&if_primary_term=1
  {"test_field":"test client 2"}
  # 由于_seq_no已经被客户端修改，所以报错:version_conflict_engine_exception
  ```

- 基于`external version`进行乐观锁并发控制

  如果你的主数据库已经有了版本号`version numbers`或者诸如`timestamp`字段可以用于作为版本号码，这时候你可以在`Elasticsearch`中通过增加`version_type=external`来重新利用这些版本号码，版本号码必须是大于0并且小于`9.2e+18`的整数。`Elasticsearch`处理外部的版本号码和处理内部版本号码的方式是不一样的，它不检测当前`_version`是否和请求参数中的版本一致，而是检测当前`_version`是否小于指定的版本。如果请求成功，那么外部的版本号就会被存储到文档中的`_version`中

  ```
  # 其中一个客户端先更新了一下这个数据，同时指定版本号为2
  PUT /test_index/_doc/1?version=2&&version_type=external
  {"test_field":"test client 1"}
  
  # 另外一个客户端尝试更新了一下这个数据，同时指定版本号为2
  PUT /test_index/_doc/1?version=2&&version_type=external
  {"test_field":"test client 2"}
  # 由于version已经被客户端A修改成2，当B客户端尝试修改时报错:version_conflict_engine_exception
  ```

## 批量操作

- 批量查询

  ```
  GET /_mget
  {"docs":[{"_index":"test_index","_id":1},{"_index":"test_index","_id":2}]}
  
  # 如果查询的document是一个index下 可以进一步简化为
  GET /test_index/_mget
  {"docs":[{"_id":1},{"_id":2}]}
  ```

- 批量增删改

  ```
  POST /_bulk
  {"index":{"_index":"test_index","_id":"2"}}
  {"test_field":"replaced test2"}
  {"create":{"_index":"test_index","_id":"3"}}
  {"test_field":"create test3"}
  {"delete":{"_index":"test_index","_id":"1"}}
  {"update":{"_index":"test_index","_id":"3"}}
  {"doc":{"test_field":"bulk test1"}}
  
  （1）delete：删除一个文档，只要1个json串就可以了
  （2）create：PUT /index/id/_create，强制创建
  （3）index：普通的put操作，可以是创建文档，也可以是全量替换文档
  （4）update：执行的partial update操作
  bulk操作中，任意一个操作失败，是不会影响其他的操作的，但是在返回结果里会告诉你异常日志
  bulk request会加载到内存里，如果太大的话性能反而会下降，因此需要反复尝试一个最佳的bulk size。一般从1000~5000条数据开始尝试逐渐增加。另外如果看大小的话，最好是在5~15MB之间
  ```


## 附录

- [ElasticSearch写一致性及quorum机制](https://zhouze-java.github.io/2018/11/20/Elasticsearch-15-%E5%86%99%E4%B8%80%E8%87%B4%E6%80%A7%E5%8E%9F%E7%90%86%E5%8F%8Aquorum%E6%9C%BA%E5%88%B6/)
- [ElasticSearch multi-index搜索模式](https://segmentfault.com/a/1190000019003460)
- `ElasticSearch深度分页的原理及解决方案`
- 