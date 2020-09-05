# Redis单实例

- 创建`ConfigMap`

    ```shell
    # 清理ConfigMap
    kubectl delete configmap redis-conf
    
    # 创建redis配置文件，按需添加修改
    cat >redis.conf <<\EOF
    protected-mode no
    requirepass 
    logfile /data/redis.log
    EOF
    
    # 创建ConfigMap
    kubectl create configmap redis-conf --from-file=redis.conf
    ```

    上述的配置文件`redis.conf`是从网上`COPY`过来的，具体配置项需根据具体情况进行修改

- 创建`redis`容器

    ```shell
    # 清理Deployment资源
    kubectl delete -f redis.yaml 
    
    cat > redis.yaml <<\EOF
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: redis
    spec:
      selector:
        matchLabels:
          app: redis
      replicas: 1
      template:
        metadata:
         labels:
           app: redis
        spec:
         containers:
         - name: redis
           image: redis
           command:
             - "redis-server"
           args:
             - "/etc/redis/redis.conf"
           readinessProbe:
             exec:
               command:
               - sh
               - -c
               - "redis-cli -h $(hostname) ping"
             initialDelaySeconds: 15
             timeoutSeconds: 5
           livenessProbe:
             exec:
               command:
               - sh
               - -c
               - "redis-cli -h $(hostname) ping"
             initialDelaySeconds: 20
             periodSeconds: 3
           volumeMounts:
           - name: redis-conf
             mountPath: "/etc/redis"
         volumes:
         - name: redis-conf
           configMap:
             name: redis-conf
             items:
               - key: redis.conf
                 path: redis.conf
    EOF
    
    # 创建Deployment资源
    kubectl apply -f redis.yaml 
    ```

    由于单节点主要用户测试环境测试使用，`Redis`对于我的业务只是用户缓存。所以这里没有将数据文件进行挂载

- 创建`redis-service`服务

    ```shell
    # 清理SVC
    kubectl delete -f redis-service.yaml
    
    # 编写redis-service.yaml
    cat >redis-service.yaml<<\EOF
    apiVersion: v1
    kind: Service
    metadata:
      name: redis-service
    spec:
      type: NodePort
      ports:
      - name: redis
        port: 6379
        targetPort: 6379
        nodePort: 30379
        protocol: TCP
      selector:
        app: redis
    EOF
    
    # 创建SVC
    kubectl apply -f redis-service.yaml
    
    # 查看SVC
    kubectl get svc redis-service
    
    # 查看SVC详情
    kubectl describe svc redis-service
    ```

- 验证`redis`实例

    ```shell
    kubectl exec -it `kubectl get pods| grep redis| awk '{print $1}'` -- /bin/bash
    
    # 查看Redis相关日志
    tail -100f /data/redis.log
    
    # 连接Redis实例进行相关操作
    redis-cli -h 127.0.0.1 -a redispassword -p 6379
    ```

