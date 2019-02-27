# Beats

> `Beats`ͨ�׵������ǲɼ����ݲ��ϱ���`Logstash`��`ElasticSearch`��6.x��

## Filebeat

- ��װ`Filebeat`

  ```reStructuredText
  1.�ӹ�������Filebeat ���ص�ַ:https://www.elastic.co/downloads/beats/filebeat
  
  2.��ѹ��ָ���ļ��У��޸������ļ�filebeat.yml,ʹ������ ./filebeat -e -c filebeat.yml����
  ```

- `Filebeat`�ĳ�������

  ```shell
   # ������ǰ����
   # ./filebeat export config
   
   # ������ǰģ��
   # ./filebeat export template
  ```

- `Filebeat`�ռ�`Nginx`��־

  - ����`Nginx Modules`

    ```shell
    # ./filebeat modules enable nginx
    
    # ./filebeat modules list �鿴�Ƿ����óɹ�
    ```

  - �޸�`Nginx Modules`����`vim modules.d/nginx.yml`

    ```yaml
    - module: nginx
      access:
        enabled: true
        var.paths: ["/opt/nginx/logs/host_access.log*"]
    
      error:
        enabled: true
        var.paths: ["/opt/nginx/logs/host_errors.log*"]
        
    # /opt/nginx/logs/host_access.log*Ϊnginx����������־
    # /opt/nginx/logs/host_errors.log*Ϊnginx����������־
    ```

  - ע��:��ʹ��`Filebeat`�Դ���`Nginx Module`��Ҫ��`Nginx`��־��ʽָ��Ϊ

    ```reStructuredText
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';
    ```

  - Ϊ����`elasticsearch`�ڵ�������²����`Nginx Module`�����²��֧�֡�

    ```shell
    # ./elasticsearch-plugin install ingest-geoip
    
    # ./elasticsearch-plugin install ingest-user-agent
    ```

  - �޸������ļ���������׼���õ�`Nginx`��־����ָ���ļ����£�����`filebeat`�۲�

    ```shell
    1.�޸�ouput���ý����������elasticsearch
    output.elasticsearch:
      hosts: ["192.168.56.128:9200"]
    
    2.���ÿ���kibana��dashboards
    setup.dashboards.enabled: true
    
    3.����kibana��ַ
    setup.kibana:
      host: "192.168.56.128:5601"
    
    4.����filebeat��-d "publish"�ڿ���̨�����Ϣ���ֲ�6.x֮��beatsֻ֧��һ��output��
    # ./filebeat -e -c filebeat.yml -d "publish"
    ```

���õĻ���`MetricBeat`��`PacketBeat`��`Heartbeat`�ȣ��������`Beat`�ɲο�[�ٷ��ĵ�](https://www.elastic.co/guide/en/beats/libbeat/current/index.html)

