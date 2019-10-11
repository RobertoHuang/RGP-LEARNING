# Query DSL详解

> `Elasticsearch`提供基于`JSON`的完整查询`DSL`来定义查询. 将查询`DSL`视为查询的`AST`(抽象语法树)，组成:
>
> ```
> Leaf query clauses:叶子查询子句在特定字段中查找特定值，例如match，term或range查询. 这些查询可以自己使用
> 
> Compound query clauses:复合查询子句包装其他叶查询或复合查询，并用于以逻辑方式组合多个查询(例如bool或dis_max查询)，或更改其行为(例如constant_score查询)
> ```
>
> 查询子句的行为会有所不同，具体取决于它们是在查询上下文中还是在过滤器上下文中使用
>
> ```
> Query contextedit:在查询上下文中，查询子句回答问题"此文档与该查询子句的匹配程度如何？"除了决定文档是否匹配外，查询子句还计算_score元字段中的相关性得分
> 
> Filter contextedit:在过滤器上下文中，查询子句回答问题"此文档是否与此查询子句匹配？"答案是简单的是或否-不计算分数. 过滤器上下文主要用于过滤结构化数据
> ```
>
> 语法示例参考:
>
> ```
> GET /_search
> {
>   "query": { (1)
>     "bool": { (2)
>       "must": [
>         { "match": { "title":   "Search"        }},
>         { "match": { "content": "Elasticsearch" }}
>       ],
>       "filter": [ (3)
>         { "term":  { "status": "published" }},
>         { "range": { "publish_date": { "gte": "2015-01-01" }}}
>       ]
>     }
>   }
> }
> ```
>
> (1)`query`参数指示查询上下文
>
> (2)`bool`和两个`match`子句用于查询上下文，这意味着它们用于对每个文档的匹配程度进行评分
>
> (3)`filter`参数指示过滤器上下文. 其`term`和`range`子句用于过滤器上下文，但不会影响匹配文档的分数

## term

`term`根据`exact value`进行搜索，数字、`boolean`、`date`天然支持，`text`需要建索引时指定为`not_analyzed`

- 插入一些测试帖子数据

  ```
  POST /forum/_bulk
  {"index":{"_id":1}}
  {"articleID":"XHDK-A-1293-#fJ3","userID":1,"hidden":false,"postDate":"2017-01-01"}
  {"index":{"_id":2}}
  {"articleID":"KDKE-B-9947-#kL5","userID":1,"hidden":false,"postDate":"2017-01-02"}
  {"index":{"_id":3}}
  {"articleID":"JODL-X-1937-#pV7","userID":2,"hidden":false,"postDate":"2017-01-01"}
  {"index":{"_id":4}}
  {"articleID":"QQPX-R-3956-#aD8","userID":2,"hidden":true,"postDate":"2017-01-02"}
  ```

- 查看索引映射关系

  ```
  GET /forum/_mapping
  ```

  `ES5.x`版本开始`type=text`默认会设置两个`field`

  一个是`field`本身是分词的，还有一个`field.keyword`默认不分词，会最多保留256个字符

- 根据用户`ID`搜索帖子

  ```
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"term":{"userID":1}}}}}
  ```

  `term filter/query`对搜索文本不分词直接拿去倒排索引中匹配

- 根据帖子`ID`搜索帖子

  ```
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"term":{"articleID":"XHDK-A-1293-#fJ3"}}}}}
  ```

  由于`articleID`是`text`类型，在建立倒排索引时已经进行了分词，故上述查询没有结果返回。正确打开方式

  ```
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"term":{"articleID.keyword":"XHDK-A-1293-#fJ3"}}}}}
  ```

  `articleID.keyword`是`ES`最新版本内置建立的`field`就是不分词的

  所以一个`articleID`过来的时候会建立两次索引，一次是自己本身是要分词的，分词后放入倒排索引；另外一次是基于`articleID.keyword`不分词，保留256个字符最多，直接一个字符串放入倒排索引中

  `term filter`对`text`过滤可以考虑使用内置的`field.keyword`来进行匹配。但是有个问题默认就保留256个字符。所以尽可能还是自己去手动建立索引指定`not_analyzed`。在最新版本的`ES`中设置`type=keyword`即可

- 查看分词

  ```
  GET /forum/_analyze
  {"field":"articleID","text":"XHDK-A-1293-#fJ3"}
  ```

- 重建索引

  ```
  DELETE /forum
  
  PUT /forum
  {"mappings":{"properties":{"articleID":{"type":"keyword"}}}}
  
  POST /forum/_bulk
  {"index":{"_id":1}}
  {"articleID":"XHDK-A-1293-#fJ3","userID":1,"hidden":false,"postDate":"2017-01-01"}
  {"index":{"_id":2}}
  {"articleID":"KDKE-B-9947-#kL5","userID":1,"hidden":false,"postDate":"2017-01-02"}
  {"index":{"_id":3}}
  {"articleID":"JODL-X-1937-#pV7","userID":2,"hidden":false,"postDate":"2017-01-01"}
  {"index":{"_id":4}}
  {"articleID":"QQPX-R-3956-#aD8","userID":2,"hidden":true,"postDate":"2017-01-02"}
  ```

- 重新根据帖子`ID`进行搜索

  ```
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"term":{"articleID":"XHDK-A-1293-#fJ3"}}}}}
  ```

## bool

- 搜索发帖日期为`2017-01-01`或帖子`ID`为`XHDK-A-1293-#fJ3`且发帖日期绝对不为`2017-01-02`

  ```
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"bool":{"should":[{"term":{"postDate":"2017-01-01"}},{"term":{"articleID":"XHDK-A-1293-#fJ3"}}],"must_not":{"term":{"postDate":"2017-01-02"}}}}}}}
  ```

  `must，should，must_not，filter`必须匹配，可以匹配其中任意一个即可，必须不匹配

- 搜索帖子`ID`为`XHDK-A-1293-#fJ3`或帖子`ID`为`JODL-X-1937-#pV7`而且发帖日期为`2017-01-01`的帖子

  ```
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"bool":{"should":[{"term":{"articleID":"XHDK-A-1293-#fJ3"}},{"bool":{"must":[{"term":{"articleID":"JODL-X-1937-#pV7"}},{"term":{"postDate":"2017-01-01"}}]}}]}}}}}
  ```

  上述例子为`bool`的嵌套查询

- `bool`组合多个搜索条件，如何计算`relevance score`

  `must`和`should`搜索对应的分数加起来，除以`must`和`should`的总数

  `must`是确保必须有这个关键字，同时根据`must`的条件计算出`document`对这个搜索条件的`relevance score`

  在满足`must`的基础之上`should`中的条件(可不匹配)若匹配更多那么`document`的`relevance score`就会更高

## terms

- 为帖子数据增加`tag`字段

  ```
  POST /forum/_bulk
  {"update":{"_id":"1"}}
  {"doc":{"tag":["java","hadoop"]}}
  {"update":{"_id":"2"}}
  {"doc":{"tag":["java"]}}
  {"update":{"_id":"3"}}
  {"doc":{"tag":["hadoop"]}}
  {"update":{"_id":"4"}}
  {"doc":{"tag":["java","elasticsearch"]}}
  ```

- 搜索`articleID`为`KDKE-B-9947-#kL5`或`QQPX-R-3956-#aD8`的帖子，搜索`tag`中包含`java`的帖子

  ```
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"terms":{"articleID":["KDKE-B-9947-#kL5","QQPX-R-3956-#aD8"]}}}}}
  
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"terms":{"tag":["java"]}}}}}
  ```

## range

- 为帖子数据增加浏览量的字段

  ```
  POST /forum/_bulk
  {"update":{"_id":"1"}}
  {"doc":{"view_cnt":30}}
  {"update":{"_id":"2"}}
  {"doc":{"view_cnt":50}}
  {"update":{"_id":"3"}}
  {"doc":{"view_cnt":100}}
  {"update":{"_id":"4"}}
  {"doc":{"view_cnt":80}}
  ```

- 搜索浏览量在30~60之间的帖子

  ```
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"range":{"view_cnt":{"gt":30,"lt":60}}}}}}
  ```

- 搜索发帖日期在最近1个月的帖子

  ```
  # 添加一条测试数据
  POST /forum/_bulk
  {"index":{"_id":5}}
  {"articleID":"DHJK-B-1395-#Ky5","userID":3,"hidden":false,"postDate":"2017-03-01","tag":["elasticsearch"],"tag_cnt":1,"view_cnt":10}
  
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"range":{"postDate":{"gt":"2017-03-10||-30d"}}}}}}
  
  GET /forum/_search
  {"query":{"constant_score":{"filter":{"range":{"postDate":{"gt":"now-30d"}}}}}}
  ```

## 全文检索精确度

- 为帖子数据增加标题字段

  ```
  POST /forum/_bulk
  {"update":{"_id":"1"}}
  {"doc":{"title":"this is java and elasticsearch blog"}}
  {"update":{"_id":"2"}}
  {"doc":{"title":"this is java blog"}}
  {"update":{"_id":"3"}}
  {"doc":{"title":"this is elasticsearch blog"}}
  {"update":{"_id":"4"}}
  {"doc":{"title":"this is java, elasticsearch, hadoop blog"}}
  {"update":{"_id":"5"}}
  {"doc":{"title":"this is spark blog"}}
  ```

- 搜索标题中包含`java`或`elasticsearch`的`blog`

  ```
  GET /forum/_search
  {"query":{"match":{"title":"java elasticsearch"}}}
  ```

  `match query`进行全文检索，若要检索的`field`是`not_analyzed`类型那么`match query`相当于`term query`

- 搜索标题中包含`java`和`elasticsearch`的`blog`

  ```
  GET /forum/_search
  {"query":{"match":{"title":{"query":"java elasticsearch","operator":"and"}}}}
  ```

  如果你是希望所有的搜索关键字都要匹配的那么就用`and`，可以实现单纯`match query`无法实现的效果

- 搜索包含`java`，`elasticsearch`，`spark`，`hadoop`4个关键字中至少3个的`blog`

  ```
  GET /forum/_search
  {"query":{"match":{"title":{"query":"java elasticsearch spark hadoop","minimum_should_match":"75%"}}}}
  ```

- 搜索包含`java`，`elasticsearch`，`spark`，`hadoop`4个关键字中至少3个的`blog`

  ```
  GET /forum/_search
  {"query":{"bool":{"should":[{"match":{"title":"java"}},{"match":{"title":"elasticsearch"}},{"match":{"title":"hadoop"}},{"match":{"title":"spark"}}],"minimum_should_match":3}}}
  ```

  默认情况下`should`是可以不匹配任何一个的，但是有个例外的情况如果没有`must`的话，那么`should`中必须至少匹配一个才可以。比如下面的搜索`should`中有4个条件，默认情况下只要满足其中一个条件就可以匹配作为结果返回，但是可以精准控制`should`的4个条件中至少匹配几个才能作为结果返回


- `match`查询转换

  - 普通`match`如何转换为`term+should`

    ```
    {"match":{"title":"java elasticsearch"}}
    
    es会在底层自动将这个match query转换为bool的语法。bool should指定多个搜索词，同时使用term query
    
    {"bool":{"should":[{"term":{"title":"java"}},{"term":{"title":"elasticsearch"}}]}}
    ```

  - `and match`如何转换为`term+must`

    ```
    {"match":{"title":{"query":"java elasticsearch","operator":"and"}}}
    
    {"bool":{"must":[{"term":{"title":"java"}},{"term":{"title":"elasticsearch"}}]}}
    ```

  - `minimum_should_match`如何转换

    ```
    {"match":{"title":{"query":"java elasticsearch hadoop spark","minimum_should_match":"75%"}}}
    
    {"bool":{"should":[{"term":{"title":"java"}},{"term":{"title":"elasticsearch"}},{"term":{"title":"hadoop"}},{"term":{"title":"spark"}}],"minimum_should_match":3}}
    ```

## boost

- 搜索标题中包含`blog`的帖子，同时如果标题中包含`java`、`hadoop`、`elasticsearch`或`spark`就优先搜索出来，同时如果一个帖子包含`spark`最优先搜索出来

  ```
  GET /forum/_search
  {"query":{"bool":{"must":[{"match":{"title":"blog"}}],"should":[{"match":{"title":{"query":"java"}}},{"match":{"title":{"query":"hadoop"}}},{"match":{"title":{"query":"elasticsearch"}}},{"match":{"title":{"query":"spark","boost":5}}}]}}}
  ```

## dis_max

- 为帖子数据增加`content`字段

  ```
  POST /forum/_bulk
  {"update":{"_id":"1"}}
  {"doc":{"content":"i like to write best elasticsearch article"}}
  {"update":{"_id":"2"}}
  {"doc":{"content":"i think java is the best programming language"}}
  {"update":{"_id":"3"}}
  {"doc":{"content":"i am only an elasticsearch beginner"}}
  {"update":{"_id":"4"}}
  {"doc":{"content":"elasticsearch and hadoop are all very good solution, i am a beginner"}}
  {"update":{"_id":"5"}}
  {"doc":{"content":"spark is best big data solution based on scala ,an programming language similar to java"}}
  ```

- 搜索`title`或`content`中包含`java`或`solution`的帖子

  ```
  GET /forum/_search
  {"query":{"bool":{"should":[{"match":{"title":"java solution"}},{"match":{"content":"java solution"}}]}}}
  
  GET /forum/_search
  {"query":{"dis_max":{"queries":[{"match":{"title":"java solution"}},{"match":{"content":"java solution"}}]}}}
  ```

  `best fields`策略就是说搜索到的结果，应该是某一个`field`中匹配到了尽可能多的关键词被排在前面，而不是尽可能多的`field`匹配到了少数的关键词排在了前面

- 搜索`title`或`content`中包含`java beginner`的帖子

  ```
  GET /forum/_search
  {"query":{"dis_max":{"queries":[{"match":{"title":"java beginner"}},{"match":{"content":"java beginner"}}]}}}
  ```

  `dis_max`取多个`query`中分数最高的一个`query`的分数，使用`tie_breaker`将其他`query`的分数也考虑进去

  ```
  GET /forum/_search
  {"query":{"dis_max":{"queries":[{"match":{"title":"java beginner"}},{"match":{"content":"java beginner"}}],"tie_breaker":0.3}}}
  ```

  `tie_breaker`参数的意义在于说将其他`query`的分数乘以`tie_breaker`，然后与最高分数的那个`query`综合在一起进行计算。除了取最高分以外还会考虑其他的`query`的分数，`tie_breaker`的值在0~1之间，是个小数

## multi_match

- `best field`策略

  ```
  GET /forum/_search
  {"query":{"dis_max":{"queries":[{"match":{"title":{"query":"java beginner","minimum_should_match":"50%","boost":2}}},{"match":{"content":{"query":"java beginner","minimum_should_match":"50%"}}}],"tie_breaker":0.3}}}
  
  # 上述查询等价于下面的
  
  GET /forum/_search
  {"query":{"multi_match":{"query":"java beginner","type":"best_fields","fields":["title^2","content"],"tie_breaker":0.3,"minimum_should_match":"50%"}}}
  ```

  `best_fields`是对多个`field`进行搜索，挑选某个`field`匹配度最高的那个分数，同时在多个`query`最高分相同的情况下，在一定程度上考虑其他`query`的分数。简单来说你对多个`field`进行搜索，就想搜索到某一个`field`尽可能包含更多关键字的数据

  - 优点
    - 通过`best_fields`策略以及综合考虑其他`field`
    - 还有`minimum_should_match`支持可以尽可能精准地将匹配的结果推送到最前面
  - 缺点
     - 除了那些精准匹配的结果，其他差不多大的结果，排序结果不是太均匀，没有什么区分度了

  实际的例子:百度之类的搜索引擎，最匹配的到最前面，但是其他的就没什么区分度了

- `most field`策略

  ```
  # 添加测试数据
  POST /forum/_mapping
  {"properties":{"sub_title":{"type":"text","analyzer":"english","fields":{"std":{"type":"text","analyzer":"standard"}}}}}
  
  POST /forum/_bulk
  {"update":{"_id":"1"}}
{"doc":{"sub_title":"learning more courses"}}
  {"update":{"_id":"2"}}
  {"doc":{"sub_title":"learned a lot of course"}}
  {"update":{"_id":"3"}}
  {"doc":{"sub_title":"we have a lot of fun"}}
  {"update":{"_id":"4"}}
  {"doc":{"sub_title":"both of them are good"}}
  {"update":{"_id":"5"}}
  {"doc":{"sub_title":"haha, hello world"}}
  
  # best field策略
  GET /forum/_search
  {"query":{"match":{"sub_title":"learning courses"}}}
  
  使用best field策略查询出来的两条数据评分是一样的，可能这里有个疑问为什么learning评分不会比learned更高呢
  
  因为sub_title用的是enligsh analyzer，所以还原了单词
  learning --> learn
  learned --> learn
  courses --> course
  sub_titile: learning coureses --> learn course
  
  # most field策略
  GET /forum/_search
  {"query":{"multi_match":{"query":"learning courses","type":"most_fields","fields":["sub_title","sub_title.std"]}}}
  
  使用most field策略查询出来的分值有明显的区别，因为sub_title.std影响了评分（sub_title.std的分词器类型为standard不会还原单词时态）
  ```
  
  `most_fields`综合多个`field`一起进行搜索，尽可能多地让所有`field`的`query`参与到总分数的计算中来，此时就会是个大杂烩，出现类似`best_fields`案例最开始的那个结果，结果不一定精准，某一个`document`的一个`field`包含更多的关键字，但是因为其他`document`有更多`field`匹配到了所以排在了前面
  
  - 优点:
    - 将尽可能匹配更多field的结果推送到最前面，整个排序结果是比较均匀的
  - 缺点:
    - 可能那些精准匹配的结果，无法推送到最前面
  
  实际的例子:`wiki`明显的`most_fields`策略，搜索结果比较均匀，但是的确要翻好几页才能找到最匹配的结果

