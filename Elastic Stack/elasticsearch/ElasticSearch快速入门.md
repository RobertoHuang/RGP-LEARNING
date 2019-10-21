# ElasticSearch快速入门

- 基础的文档CRUD操作

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

- 几个典型的聚合案例

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

- 