# Logstash

> `Logstash`是开源的服务器端数据收集管道，能够同时从多个来源采集数据、转换数据，然后将数据发送到您最喜欢的存储库中(例如`ElasticSearch`中)，本篇博客旨在帮助大家对`logstash`有个初步的认识。【6.x】

## 环境安装

- 官网下载`Logstash`:[下载地址](https://www.elastic.co/downloads/logstash)

- 解压到指定文件夹，然后再终端运行如下命令

  ```shell
  # bin/logstash -e 'input{stdin{}}output{stdout{codec=>rubydebug}}'
  ```

- 在服务启动成功后输入`Hello World`回车后，返回如下结果

  ```ruby
  {
      "@version" => "1",
      "message" => "Hello World",
      "@timestamp" => 2019-02-25T10:59:08.606Z,
      "host" => "localhost.localdomain"
  }
  ```

## 配置文件

关于`Logstash`配置文件相关可参考官方文档:[配置相关](https://www.elastic.co/guide/en/logstash/current/config-setting-files.html)

`Logstash`主要包含配置文件:【`logstash.yml`、`pipelines.yml`、`jvm.options`、`log4j2.properties`】

## 插件相关

### 插件安装

- 插件安装

  ```shell
  # https://github.com/logstash-plugins/下查找插件
  
  # bin/logstash-plugin install logstash-output-webhdfs
  ```

- 插件更新

  ```shell
  # bin/logstash-plugin update logstash-input-tcp
  ```

- 本机插件列表

  ```shell
  # bin/logstash-plugin list=
  ```

### 插件配置

#### 输入插件(input)

- `file`

  - 配置示例

    ```ruby
    input {
        file {
            path => ["/var/log/*.log", "/var/log/message"]
            type => "system"
            start_position => "beginning"
        }
    }
    ```

  - 常用配置项

    |          配置项          |                             说明                             |
    | :----------------------: | :----------------------------------------------------------: |
    |   `discover_interval`    | `logstash`每隔多久去检查一次被监听的`path`下是否有新文件(默认`15s`) |
    |        `exclude`         | 不想被监听的文件可以排除出去，这里跟`path`一样支持`glob`匹配 |
    |      `close_older`       | 一个已经监听中的文件如果超过这个值的时间内没有更新内容，就关闭监听它的文件句柄(默认`300s`即一个小时) |
    |      `ignore_older`      | 在每次检查文件列表的时候如果一个文件的最后修改时间超过这个值，就忽略这个文件(默认`86400s`即一天) |
    |      `sincedb_path`      | 如果你不想用默认的`$HOME/.sincedb`(Windows平台上在 `C:\Windows\System32\config\systemprofile\.sincedb`)，可以通过这个配置定义`sincedb`文件到其他位置 |
    | `sincedb_write_interval` |      `logstash`每隔多久写一次`sincedb`文件(默认是`15s`)      |
    |     `stat_interval`      | `logstash`每隔多久检查一次被监听文件状态是否有更新(默认`1s`) |
    |     `start_position`     | `logstash`从什么位置开始读取文件数据(默认是结束位置)。也就是说 `logstash`进程会以类似`tail -F`的形式运行，如果你是要导入原有数据把这个设定改成 `beginning`，`logstash`进程就从头开始读取类似`less +F`的形式运行。**注意:如果 sincedb 文件中已经有这个文件的`inode`记录了，那么`logstash`依然会从记录过的`pos`开始读取数据。所以重复测试的时候每回需要删除 sincedb 文件(官方博客上提供了[另一个巧妙的思路](https://www.elastic.co/blog/logstash-configuration-tuning):将`sincedb_path`定义为`/dev/null`，则每次重启自动从头开始读** |

  `input`插件使`Logstash`能够读取特定的事件源，更多`input`插件使用方式可参考官方文档:[input plugins](https://www.elastic.co/guide/en/logstash/current/input-plugins.html)

#### 编解码配置(codec)

- `json`

  - 配置示例

    ```shell
    bin/logstash -e 'input{stdin{
    	codec => "json"
    }}output{stdout{codec=>rubydebug}}'
    ```

  - 在服务启动成功后输入`{"name": "roberto","userId": "201210704116"}`返回结果如下

    ```ruby
    {
        "name" => "roberto",
        "userId" => "201210704116",
        "@version" => "1",
        "@timestamp" => 2019-02-25T15:15:51.861Z,
        "host" => "localhost.localdomain"
    }
    ```

`codec`插件更改事件的数据表示，更多`codec`插件使用方式可参考官方文档:[codec plugins](https://www.elastic.co/guide/en/logstash/current/codec-plugins.html)

#### 过滤器配置(filter)

- `date`将日期字符串解析为日期类型，然后替换`@timestamp`字段或指定其他字段

  - 配置示例

    ```shell
    bin/logstash -e 'input{stdin{codec=>json}}
    filter {
      date {
        match => [ "logdate", "MMM dd yyyy HH:mm:ss" ]
      }
    }
    output{stdout{codec=>rubydebug}}'
    ```

  - 在服务启动成功后输入`{"logdate": "December 12 2012 12:12:12"}`返回结果如下

    ```ruby
    {
        "host" => "192.168.56.1",
        "logdate" => "December 12 2012 12:12:12",
        "@version" => "1",
        "@timestamp" => 2012-12-12T04:12:12.000Z
    }
    ```

- `grok`正则捕获，可以在`grok`里预定好命名正则表达式，在稍后`grok`参数或者其他正则表达式里引用它

  `grok`

  - 配置示例

    - 在`/opt/logstash/logstash-6.5.4/pattern`目录下新建正则表达式文件`grok`，内容如下

      ```reStructuredText
      APACHE_LOG %{IPORHOST:addre} %{USER:ident} %{USER:auth} \[%{HTTPDATE:timestamp}\] \"%{WORD:http_method} %{NOTSPACE:request} HTTP/%{NUMBER:httpversion}\" %{NUMBER:status} (?:%{NUMBER:bytes}|-) \"(?:%{URI:http_referer}|-)\" \"%{GREEDYDATA:User_Agent}\"
      ```

    - 使用如下配置启动`logstash`服务端，待服务启动成功后发送`HTTP`请求，整个流程如下

      - 启动服务端

        ```sh
        bin/logstash -e 'input{
          http {
              port => 8080
              codec => "plain"
          }
        }
        filter {
          grok {
            patterns_dir => ["/opt/logstash/logstash-6.5.4/pattern"]
            match => {"message" => "%{APACHE_LOG}"}
            remove_field => ["message","headers"]
          }
        }
        output{stdout{codec=>rubydebug}}'
        ```

      - 发送`HTTP`请求

        ```htt
        POST 192.168.56.128:8080
        
        192.168.10.97 - - [19/Jul/2016:16:28:52 +0800] "GET / HTTP/1.1" 200 23 "-" "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36"
        ```

      - `logstash`服务端输出如下

        ```ruby
        {
            "http_method" => "GET",
            "status" => "200",
            "auth" => "-",
            "request" => "/",
            "bytes" => "23",
            "timestamp" => "19/Jul/2016:16:28:52 +0800",
            "User_Agent" => "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
            "@version" => "1",
            "@timestamp" => 2019-02-25T17:44:49.582Z,
            "addre" => "192.168.10.97",
            "httpversion" => "1.1",
            "ident" => "-",
            "host" => "192.168.56.1"
        }
        ```

  **注意:可以使用[Grok Debugger](http://grokdebug.herokuapp.com/)来调试自己的`grok`表达式**

- `dissect`由于`grok`在性能和资源损耗方面受人诟病，并且有时候日志格式并没有那么复杂，故引入`dissect`

  - 启动服务端

    ```shell
    bin/logstash -e 'input{
      http {
          port => 8080
          codec => "plain"
      }
    }
    filter {
      dissect {
        mapping => {
          "message" => "%{ts} %{+ts} %{+ts} %{src} %{} %{prog}[%{pid}]: %{msg}"
        }
        remove_field => ["headers"]
        convert_datatype => {
          pid => "int"
        }
      }
    }
    output{stdout{codec=>rubydebug}}'
    ```

  - 发送`HTTP`请求

    ```http
    POST 192.168.56.128:8080
    
    Apr 26 12:20:02 localhost  systemd[1]: Starting system activity accounting tool...
    ```

  - `logstash`服务端输出如下

    ```ruby
    {
        "message" => "Apr 26 12:20:02 localhost  systemd[1]: Starting system activity accounting tool...",
        "@timestamp" => 2019-02-25T18:12:43.525Z,
        "src" => "localhost",
        "prog" => "systemd",
        "msg" => "Starting system activity accounting tool...",
        "ts" => "Apr 26 12:20:02",
        "@version" => "1",
        "host" => "192.168.56.1",
        "pid" => 1
    }
    ```

- `mutate`提供了丰富的基础数据类型的处理能力。包括数据转换、字符串处理和字段处理等

- `geoip`库可以根据`IP`地址提供对应的地域信息，包括国家、省市、经纬度等等【可视化地图和区域统计】

`filter`插件对事件执行中间处理，它虽然名为过滤器但提供的不单单是过滤功能。它扩展了进入过滤器的原始数据进行复杂的逻辑处理，甚至可以无中生有的添加新的`logstash`事件到后续流程中。更多`codec`插件使用方式可参考官方文档:[filter plugins](https://www.elastic.co/guide/en/logstash/current/filter-plugins.html)

#### 输出插件(output)

- `elasticsearch`将数据写入到`elasticsearch`中

  - 启动服务端

    ```shell
    bin/logstash -e 'input{
      http {
          port => 8080
          codec => "plain"
      }
    }
    filter {
      grok {
        patterns_dir => ["/opt/logstash/logstash-6.5.4/pattern"]
        match => {"message" => "%{APACHE_LOG}"}
        remove_field => ["message","headers"]
      }
      mutate {
        add_field => {
          "type" => "apache"
        }
      }
    }
    output{elasticsearch {
            hosts => ["192.168.56.128:9200"]
            index => "logstash-%{type}-%{+YYYY.MM.dd}"
            document_type => "%{type}"
            sniffing => true
            template_overwrite => true
        }
    }'
    ```

  - `logstash`服务端输出如下

    ```reStructuredText
    [2019-02-26T03:01:41,327][INFO ][logstash.outputs.elasticsearch] Elasticsearch pool URLs updated {:changes=>{:removed=>[], :added=>[http://192.168.56.128:8200/, http://192.168.56.128:7200/]}}
    [2019-02-26T03:01:41,358][WARN ][logstash.outputs.elasticsearch] Restored connection to ES instance {:url=>"http://192.168.56.128:8200/"}
    [2019-02-26T03:01:41,418][WARN ][logstash.outputs.elasticsearch] Restored connection to ES instance {:url=>"http://192.168.56.128:7200/"}
    
    ```

  - 发送`HTTP`请求

    ```http
    POST 192.168.56.128:8080
    
    192.168.10.97 - - [19/Jul/2016:16:28:52 +0800] "GET / HTTP/1.1" 200 23 "-" "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36"
    ```

  - 从`elasticsearch`查询数据，看`logstash`是否已经将数据存储到`elasticsearch`

    ```http
    POST /logstash-apache-2019.02.25/apache/_search
    {
      "query": { "match_all": {} }
    }
    ```

  `output`插件将事件数据发送到特定目标，更多`output`插件使用方式可参考官方文档:[output plugins](https://www.elastic.co/guide/en/logstash/current/output-plugins.html)

## `Logstash`实战

- 解析`apache log`实战

  - 编写配置文件`test-apache-log.conf`

    ```ruby
    input{
      file{
        path => "/opt/logstash/logstash-6.5.4/test-file/apache_logs"
        start_position => "beginning"
      }
    }
    filter{
        # mutate{add_field => {"[@metadata][debug]"=>true}}
    
      grok{
        match => {
          "message" => '%{IPORHOST:clientip} %{USER:ident} %{USER:auth} \[%{HTTPDATE:[@metadata][timestamp]}\] "(?:%{WORD:verb} %{NOTSPACE:request}(?: HTTP/%{NUMBER:httpversion})?|%{DATA:rawrequest})" %{NUMBER:response} (?:%{NUMBER:bytes}|-) %{QS:referrer} %{QS:agent}'
        }
      }
    
      ruby{
        code => "event.set('@read_timestamp',event.get('@timestamp'))"
      }
    
      # 20/May/2015:21:05:56 +0000
      date{
        match => ["[@metadata][timestamp]","dd/MMM/yyyy:HH:mm:ss Z"]
      }
    
      mutate{
        convert => {"bytes" => "integer"}
      }
    
      geoip{
        source => "clientip"
        fields => ["location","country_name","city_name","region_name"]
      }
    
      useragent{
        source => "agent"
        target => "useragent"
      }
    
      mutate{
        remove_field => ["headers"]
        add_field=>{"[@metadata][index]" => "apache_logs_%{+YYYY.MM}"}
      }
    
      if "_grokparsefailure" in [tags] {
        mutate {
          replace=>{
            "[@metadata][index]" => "apache_logs_failure_%{+YYYY.MM}"
          }
        }
      } else {
        mutate{remove_field=>["message"]}
      }
    }
    output{
      if [@metadata][debug]{
        stdout{
          codec => rubydebug{metadata=>true}
        }
      }else{
        stdout{
          codec=>dots
        }
        elasticsearch{
          hosts => ["192.168.56.128:9200"]
          index => "%{[@metadata][index]}"
          document_type => "doc"
          sniffing => true
          template_overwrite => true
        }
      }
    }
    ```

  - 将`apache log`文件放置`/opt/logstash/logstash-6.5.4/test-file/`目录下

    ```reStructuredText
    该文件在 数据文件/apache_logs下
    ```

  - 启动`logstash`，启动命令为`bin/logstash -f config/test-apache-log.conf`，观察控制台输出