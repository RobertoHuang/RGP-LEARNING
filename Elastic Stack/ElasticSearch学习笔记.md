# ElasticSearch

`ElasticSearch`是一个基于`Lucene`构建的开源、分布式、高性能、高可用、可伸缩的搜索和分析系统

## ElasticSearch安装

- 单机版安装

  ```reStructuredText
  1.从官网下载ElasticSearch并解压
  
  2.进入bin目录执行./elasticsearch脚本运行ElasticSearch
  
  3.访问http://192.168.56.128:9200/查看当前ElasticSearch服务状态
  ```

- 分布式安装

  - 修改`master`节点配置文件`elasticsearch.yml`

    ```yaml
    # 节点名称
    node.name: master
    # 指定该节点是否有自个被选举为master节点
    node.master: true
    
    # 对外服务的HTTP端口
    http.port: 9200
    # 绑定的IP地址
    network.host: 192.168.56.128
    
    # 节点间交互的TCP端口
    transport.tcp.port: 9300
    # 集群的名称
    cluster.name: roberto-cluster
    
    # elasticsearch-head 跨域问题
    http.cors.enabled: true
    http.cors.allow-origin: "*"
    ```

  - 修改`slave`节点配置文件`elasticsearch.yml`，分别设置端口号为`8200/9301`、`7200/9302`

    ```yaml
    # 节点名称
    node.name: master
    # 指定该节点是否有自个被选举为master节点
    node.master: true
    
    # 对外服务的HTTP端口
    http.port: 8200
    # 绑定的IP地址
    network.host: 192.168.56.128
    
    # 节点间交互的TCP端口
    transport.tcp.port: 9301
    # 集群的名称
    cluster.name: roberto-cluster
    
    # elasticsearch-head 跨域问题
    http.cors.enabled: true
    http.cors.allow-origin: "*"
    
    # 集群中的主节点的初始列表,当节点(主节点或者数据节点)启动时使用这个列表进行探测
    discovery.zen.ping.unicast.hosts: ["192.168.56.128:9300","192.168.56.128:9301","192.168.56.128:9302"]
    ```

  - 分别执行`./elasticsearch`脚本启动`ElasticSearch`，使用`Head`插件连接查看节点状态

- 实用插件`Head`安装

  - `Node.js`环境安装，可参考:[Node.js环境安装](https://github.com/RobertoHuang/RGP-LEARNING/blob/master/01.%E7%8E%AF%E5%A2%83%E9%85%8D%E7%BD%AE/%E5%9F%BA%E7%A1%80%E7%8E%AF%E5%A2%83%E5%AE%89%E8%A3%85%E6%95%99%E7%A8%8B.md)

  - 下载`Head`插件:[Head插件下载地址](https://github.com/mobz/elasticsearch-head)并解压到指定文件夹下

  - 进入`elasticsearch-head`目录下执行`npm install`命令

    ```shell
    # 安装过程中可能出现如下错误
    npm ERR! code ELIFECYCLE
    npm ERR! errno 1
    npm ERR! phantomjs-prebuilt@2.1.16 install: `node install.js`
    npm ERR! Exit status 1
    npm ERR!
    npm ERR! Failed at the phantomjs-prebuilt@2.1.16 install script.
    npm ERR! This is probably not a problem with npm. There is likely additional logging output above.
    
    npm ERR! A complete log of this run can be found in:
    npm ERR!     /root/.npm/_logs/2019-02-18T09_41_54_982Z-debug.log
    
    # 使用如下命令解决
    # npm install phantomjs-prebuilt@2.1.16 --ignore-scripts
    ```

  - 后台启动`elastichsearch-head`【`npm run start &`】，默认端口`9100`


- 可能遇到的问题

  - `max virtual memory areas vm.max_map_count [65530] likely too low`

    ```reStructuredText
    切换到root用户修改配置sysctl.conf vi /etc/sysctl.conf 
    
    添加如下配置:vm.max_map_count=655360
    
    并执行命令:sysctl -p
    ```

  - `max file descriptors [4096] for elasticsearch process likely too low`

    ```reStructuredText
    切换到root用户，编辑limits.conf添加类似如下内容 vi /etc/security/limits.conf
    
    * soft nofile 65536
    * hard nofile 131072
    ```

- **注意:因为安全问题`ElasticSearch`不让用`Root`用户直接 运行，所以需要创建新用户**

  ```reStructuredText
  1.创建新用户 adduser xxx 然后给创建的用户设置密码 passwd xxx 输入两次密码
  
  2.切换刚才创建的用户 su xxx 然后执行elasticsearch脚本，会显示Permission denied权限不足
  
  3.给新建的xxx用户赋权限，切换到root用户执行chown -R xxx /你的elasticsearch安装目录
  ```

## ElasticSearch简单使用

其实本来是计划写一下`ElasticSearch`的操作文档的，看了很久的官方文档。但是这玩意吧骚操作实在是多(各种`API`)，如果要写博客的话得花很多时间(可能还写不明白)，想了想还是等用到的时候去查[官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)吧

### Mapping相关

- 查看索引`Mapping`

    ```
    GET index/_mapping
    ```

### 重建索引

- 创建好新的索引

- 使用`reindex`进行索引数据转移

    ```
    POST _reindex
    {
      "source": {
        "index": "question"
      },
      "dest": {
        "index": "question_new"
      }
    }
    ```

- 等待`reindex`完成后`question_new`就结构就就是你想要的`mapping`了，使用 `alias`别名功能来切换

    ```
    POST _aliases
    {
      "actions": [
        {
          "add": {
            "index": "question_new",
            "alias": "question"
          }
        },
        {
          "remove_index": {
            "index": "question"
          }
        }
      ]
    }
    ```

    【新增别名删除旧的索引】这个动作是个原子操作，所以对用户是无感知的【至此完成索引的重建工作】

## ElasticSearch白金版破解

- 覆盖`x-pack-core`包

    ```
    使用下载好的x-pack-core-6.4.3.jar替换掉原有的jar包
    
    cd /usr/local/elasticsearch/elasticsearch-6.4.3/modules/x-pack-core
    ```

- 去官网申请`license`证书

    ```
    https://license.elastic.co/registration
    ```

    邮箱认真写用来接收`JSON`文件的，`country`写`china`。点击申请后邮箱马上会收到一个邮件

- 修改申请到的证书

    `"type":"basic"`替换为`"type":"platinum"`基础版变更为铂金版

    `"expiry_date_in_millis":1561420799999`替换为`"expiry_date_in_millis":3107746200000`1年变50年

    好好保存，修改后的文件可以重复使用到其它`ElasticSearch`服务器

- 通过`Kibana`上传修改后的证书

    上传前准备，打开`elasticsearch.yml`配置文件加入`xpack.security.enabled: false`

    重启`elasticsearch服务` 和 `kibana服务`，在`Management`页面对证书进行上传，上传成功后出现如下提示

    ![image-20200325132635687](images/ElasticSearch学习笔记/image-20200325132635687.png)

- 开启`ElasticSearch`登入功能

    - 修改`elasticsearch.yml`配置，添加如下两行

        ```
        xpack.security.enabled: true
        xpack.security.transport.ssl.enabled: true
        ```

    - 重置登陆权限密码

        ```
        bin/elasticsearch-setup-passwords interactive
        ```

        ![image-20200325134151991](images/ElasticSearch学习笔记/image-20200325134151991.png)

    - 修改`Kibana`配置，在`kibana.yml`下添加如下两行

        ```
        elasticsearch.username: elastic
        elasticsearch.password: {你修改的password}
        ```

    - 重启`ElasticSearch`和`Kibana`服务，出现登录界面说明配置成功

- 关于`x-pack`角色的说明

    - 集群权限

        ![image-20200325144600961](images/ElasticSearch学习笔记/image-20200325144600961.png)

    - 索引权限

        ![image-20200325144621976](images/ElasticSearch学习笔记/image-20200325144621976.png)

