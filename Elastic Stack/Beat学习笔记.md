# Beats

> `Beats`通俗的理解就是采集数据并上报到`Logstash`或`ElasticSearch`【6.x】

## Filebeat

- 安装`Filebeat`

  ```reStructuredText
  1.从官网下载Filebeat 下载地址:https://www.elastic.co/downloads/beats/filebeat
  
  2.解压到指定文件夹，修改配置文件filebeat.yml,使用命令 ./filebeat -e -c filebeat.yml启动
  ```

- `Filebeat`的常用命令

  ```shell
   # 导出当前配置
   # ./filebeat export config
   
   # 导出当前模板
   # ./filebeat export template
  ```

- `Filebeat`收集`Nginx`日志

  - 启用`Nginx Modules`

    ```shell
    # ./filebeat modules enable nginx
    
    # ./filebeat modules list 查看是否启用成功
    ```

  - 修改`Nginx Modules`配置`vim modules.d/nginx.yml`

    ```yaml
    - module: nginx
      access:
        enabled: true
        var.paths: ["/opt/nginx/logs/host_access.log*"]
    
      error:
        enabled: true
        var.paths: ["/opt/nginx/logs/host_errors.log*"]
        
    # /opt/nginx/logs/host_access.log*为nginx正常请求日志
    # /opt/nginx/logs/host_errors.log*为nginx错误请求日志
    ```

  - 注意:若使用`Filebeat`自带的`Nginx Module`需要将`Nginx`日志格式指定为

    ```reStructuredText
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';
    ```

  - 为所有`elasticsearch`节点添加如下插件【`Nginx Module`需如下插件支持】

    ```shell
    # ./elasticsearch-plugin install ingest-geoip
    
    # ./elasticsearch-plugin install ingest-user-agent
    ```

  - 修改配置文件并将事先准备好的`Nginx`日志放在指定文件夹下，启动`filebeat`观察

    ```shell
    1.修改ouput配置将数据输出到elasticsearch
    output.elasticsearch:
      hosts: ["192.168.56.128:9200"]
    
    2.配置开启kibana的dashboards
    setup.dashboards.enabled: true
    
    3.配置kibana地址
    setup.kibana:
      host: "192.168.56.128:5601"
    
    4.启动filebeat【-d "publish"在控制台输出消息，弥补6.x之后beats只支持一个output】
    # ./filebeat -e -c filebeat.yml -d "publish"
    ```

常用的还有`MetricBeat`、`PacketBeat`、`Heartbeat`等，更多关于`Beat`可参考[官方文档](https://www.elastic.co/guide/en/beats/libbeat/current/index.html)

