# Kibana

> `Kibana`是一款开源的数据分析和可视化平台，它是`Elastic Stack`成员之一，设计用于和`ElasticSearch`协作。使用`Kibana`可以对`ElasticSearch`索引中的数据进行搜索、查看、交互操作，并且可以很方便的利用图表、表格及地图对数据进行多元化的分析和呈现。本文使用的`Kibana`为`6.6.1`

## 安装和配置

- 安装`Kibana`

  ```reStructuredText
  1.下载Kibana
  	
  2.解压到指定文件夹
  
  3.修改kibana.yml修改如下配置
      server.port: "0.0.0.0"
      elasticsearch.url: "elasticsearch服务端地址"
  
  4.然后从命令行启动Kibana【./bin/kibana】
  
  5.访问http://192.168.56.128:5601/status查看kibana状态
  ```

- `Kibana`配置详解

  > `Kibana Server`启动时从`kibana.yml`文件中读取配置属性
  >
  > 
  >
  > 关于`Kibana`配置详解可参考官网提供的:[Kibana配置项](https://www.elastic.co/guide/cn/kibana/current/settings.html)，开始动手吧

## Kibana使用

与其他组件相比`Kibana`做的比较好的是它的文档完全国际化了，具体使用可直接参考[官方文档](https://www.elastic.co/guide/cn/kibana/current/index.html)

