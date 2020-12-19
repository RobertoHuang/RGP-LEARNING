# ElasticSearch安装部署

> 本文基于`Elastic Cloud on Kubernetes`进行`ElasticSearch`集群的安装部署
>
> 更多说明可以参考`ECK`官方文档:https://www.elastic.co/guide/en/cloud-on-k8s/current/index.html

## Deploy ECK in your Kubernetes cluster

```shell
# 部署eck-operator到Kubernetes集群中
kubectl apply -f https://download.elastic.co/downloads/eck/1.3.0/all-in-one.yaml

# 查看eck-operator启动日志，可判断是否正常启动
kubectl -n elastic-system logs -f statefulset.apps/elastic-operator
```

## Deploy an Elasticsearch cluster

```yaml
apiVersion: elasticsearch.k8s.elastic.co/v1
kind: Elasticsearch
metadata:
  name: elastic-cluster
  namespace: elastic-system
spec:
  version: 7.5.1
  http:
    tls:
      selfSignedCertificate:
        disabled: true
  nodeSets:
  - name: roberto-elastic
    count: 3
    volumeClaimTemplates:
    - metadata:
        name: elasticsearch-data
      spec:
        accessModes:
        - ReadWriteMany
        storageClassName: roberto-cfs-retain
        resources:
          requests:
            storage: 10Gi
    config:
      node.store.allow_mmap: false
    podTemplate:
      spec:
        containers:
          - name: elasticsearch
            env:
              - name: ES_JAVA_OPTS
                value: "-Xms2g -Xmx2g"
            resources:
              requests:
                cpu: 1
                memory: 4Gi
              limits:
                cpu: 2
                memory: 4Gi
```

- 查看集群状态

    ```shell
    kubectl get elasticsearch -n elastic-system
    ```

- 查看指定集群`Pod`详细信息

    ```shell
    kubectl get pods --selector='elasticsearch.k8s.elastic.co/cluster-name=elastic-cluster' -n elastic-system
    ```

- 查看`Pod`启动日志，可判断是否正常启动

    ```shell
    kubectl logs elastic-cluster-es-roberto-elastic-0 -n elastic-system
    ```

- 查看`ECK`帮忙创建的`SVC`信息

    ```shell
    kubectl get svc -n elastic-system
    NAME                                 TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
    elastic-cluster-es-http              ClusterIP   172.16.254.184   <none>        9200/TCP   22m
    elastic-cluster-es-roberto-elastic   ClusterIP   None             <none>        9200/TCP   22m
    elastic-cluster-es-transport         ClusterIP   None             <none>        9300/TCP   22m
    elastic-webhook-server               ClusterIP   172.16.209.120   <none>        443/TCP    74m
    ```

- 查看`ECK`帮忙创建的认证授权信息

    ```shell
    echo PASSWORD=$(kubectl get secret elastic-cluster-es-elastic-user -n elastic-system -o go-template='{{.data.elastic | base64decode}}')
    ```

- 验证与`ElasticSearch`的连接

    ```shell
    # 创建端口转发
    kubectl port-forward service/elastic-cluster-es-http 9200 -n elastic-system
    
    # 访问ElasticSearch集群
    curl -u "elastic:$PASSWORD" -k "http://localhost:9200"
    ```

## Deploy a Kibana instance

```yaml
apiVersion: kibana.k8s.elastic.co/v1
kind: Kibana
metadata:
  name: kibana
  namespace: elastic-system
spec:
  version: 7.5.1
  http:
    tls:
      selfSignedCertificate:
        disabled: true
  count: 1
  podTemplate:
    spec:
      containers:
        - name: kibana
          env:
            - name: I18N_LOCALE
              value: zh-CN
  elasticsearchRef:
    name: elastic-cluster
```

- 查看`Kibana`集群状态

    ```shell
    kubectl get kibana -n elastic-system
    ```

- 查看指定集群`Pod`详细信息

    ```shell
    kubectl get pod --selector='kibana.k8s.elastic.co/name=kibana' -n elastic-system
    ```

- 查看`ECK`帮忙创建的`SVC`信息

    ```shell
    kubectl get service kibana-kb-http -n elastic-system
    ```

- 验证与`Kibana`的连接

    ```shell
    # 创建端口转发
    kubectl port-forward service/kibana-kb-http 5601 -n elastic-system
    
    # 访问Kibana集群
    curl "http://localhost:5601"
    ```
