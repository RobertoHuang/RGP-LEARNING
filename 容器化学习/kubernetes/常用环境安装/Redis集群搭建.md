# Redis集群搭建

- 创建`ConfigMap`

    ```shell
    # 配置文件redis.conf
    cat >redis.conf<<\EOF
    # 文件目录
    dir /var/lib/redis
    # 开启AOF模式 
    appendonly yes
    # 启动集群模式
    cluster-enabled yes
    # 15秒中联系不到对方node，即认为对方有故障可能
    cluster-node-timeout 15000
    # 集群模式配置文件保存位置
    cluster-config-file /var/lib/redis/nodes.conf
    EOF
    
    # 删除名为redis-conf的ConfigMap
    kubectl delete configmap redis-conf
    
    # 创建名为redis-conf的ConfigMap
    kubectl create configmap redis-conf --from-file=redis.conf
    
    # 查看创建的ConfigMap
    kubectl describe cm redis-conf
    Name:         redis-conf
    Namespace:    default
    Labels:       <none>
    Annotations:  <none>
    
    Data
    ====
    redis.conf:
    ----
    # 文件目录
    dir /var/lib/redis
    # 开启AOF模式
    appendonly yes
    # 启动集群模式
    cluster-enabled yes
    # 15秒中联系不到对方node，即认为对方有故障可能
    cluster-node-timeout 15000
    # 集群模式配置文件保存位置
    cluster-config-file /var/lib/redis/nodes.conf
    
    Events:  <none>
    # 如上redis.conf中的所有配置项都保存到redis-conf这个ConfigMap中。该配置里头的内容可被启动参数覆盖
    ```

- 创建`Headless service`

    ```shell
    # 清理SVC
    kubectl delete -f redis-hs.yaml
    
    # 编写SVC
    cat >redis-hs.yaml<<\EOF 
    apiVersion: v1
    kind: Service
    metadata:
      name: redis-hs
      labels:
        app: redis
    spec:
      ports:
      - name: redis-port
        port: 6379
      clusterIP: None
      selector:
        app: redis
    EOF
    
    # 创建SVC
    kubectl create -f redis-hs.yaml
    
    # 查看SVC
    kubectl get svc
    NAME           TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)             AGE
    redis-hs       ClusterIP   None             <none>        6379/TCP            5s
    ```

- 利用`StatefulSet`创建`Redis`集群节点

    ```shell
    # 清理Statefulset资源
    kubectl delete -f redis.yaml
    
    # 编写yaml
    cat >redis.yaml<<\EOF
    apiVersion: apps/v1
    kind: StatefulSet
    metadata:
      name: redis
    spec:
      selector:
        matchLabels:
          app: redis
      serviceName: "redis-hs"
      replicas: 6
      updateStrategy:
        type: RollingUpdate
      podManagementPolicy: OrderedReady
      template:
        metadata:
          labels:
            app: redis
        spec:
          terminationGracePeriodSeconds: 30
          affinity:
            podAntiAffinity:
              preferredDuringSchedulingIgnoredDuringExecution:
              - weight: 100
                podAffinityTerm:
                  labelSelector:
                    matchExpressions:
                    - key: app
                      operator: In
                      values:
                      - redis
                  topologyKey: kubernetes.io/hostname
          containers:
          - name: redis
            imagePullPolicy: IfNotPresent
            image: redis
            resources:  
              requests:  
                cpu: "500m"
                memory: "1024Mi"
            ports:
            - name: redis
              containerPort: 6379
              protocol: "TCP"
            - name: cluster
              containerPort: 16379
              protocol: "TCP"
            command:
              # redis启动命令
              - "redis-server"
            args:
              - "/etc/redis/redis.conf"
              # redis-server --option 会覆盖配置文件中的参数配置
              - "--protected-mode no"
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
              - name: "redis-conf"
                mountPath: "/etc/redis"
              - name: "redis-data"
                mountPath: "/var/lib/redis"
          volumes:
          - name: "redis-conf"  
            configMap:
              # 引用configMap卷
              name: "redis-conf"
              items:
                - key: "redis.conf"  
                  path: "redis.conf"  
          securityContext:
            runAsUser: 1000
            fsGroup: 1000
      volumeClaimTemplates:
        - metadata:
            name: redis-data
          spec:
            accessModes: [ "ReadWriteOnce" ]
            storageClassName: store-class-test-delete
            resources:
              requests:
                storage: 10Gi
    EOF
    
    # 创建Statefulset资源
    kubectl apply -f redis.yaml
    ```

- 验证`StatefulSet`

    ```shell
    kubectl get pods -o wide
    NAME          READY   STATUS      RESTARTS   AGE     IP            NODE          NOMINATED NODE   READINESS GATES
    redis-0       1/1     Running     0          8m37s   172.16.1.35   172.27.0.12   <none>           <none>
    redis-1       1/1     Running     0          7m58s   172.16.2.23   172.27.0.11   <none>           <none>
    redis-2       1/1     Running     0          7m18s   172.16.0.28   172.27.0.10   <none>           <none>
    redis-3       1/1     Running     0          6m43s   172.16.1.36   172.27.0.12   <none>           <none>
    redis-4       1/1     Running     0          6m9s    172.16.2.24   172.27.0.11   <none>           <none>
    redis-5       1/1     Running     0          5m33s   172.16.0.29   172.27.0.10   <none>           <none>
    
    如上可以看到这些Pods在部署时是以{0…N-1}的顺序依次创建的。注意直到redis-0状态启动后达到Running状态之后redis-1才开始启动
    
    同时，每个Pod都会得到集群内的一个DNS域名，格式为$(podname).$(service name).$(namespace).svc.cluster.local，也即是:
    redis-0.redis-hs.default.svc.cluster.local
    redis-1.redis-hs.default.svc.cluster.local
    ...以此类推...
    
    这里我们可以验证一下
    # kubectl run -it --rm --image=busybox:1.28 --restart=Never busybox -- nslookup redis-0.redis-hs
    在K8S集群内部，这些Pod就可以利用该域名互相通信。我们可以使用busybox镜像的nslookup检验这些域名(一条命令)
    ```

- 初始化`Redis`集群

    创建好6个`Redis Pod`后，我们还需要利用常用的`Redis-tribe`工具进行集群的初始化

    创建`Ubuntu`容器

    由于`Redis`集群必须在所有节点启动后才能进行初始化，而如果将初始化逻辑写入`Statefulset`中，则是一件非常复杂而且低效的行为。这里本人不得不称赞一下原项目作者的思路值得学习。也就是说我们可以在`K8S`上创建一个额外的容器，专门用于进行`K8S`集群内部某些服务的管理控制。 这里我们专门启动一个`Ubuntu`的容器，可以在该容器中安装`Redis-tribe`进而初始化`Redis`集群，执行

    ```shell
    1、# 创建一个ubuntu容器
    kubectl run -it ubuntu --image=ubuntu --restart=Never /bin/bash
    
    2、# 我们使用阿里云的Ubuntu源，执行
    cat > /etc/apt/sources.list << EOF
    deb http://mirrors.aliyun.com/ubuntu/ bionic main restricted universe multiverse
    deb-src http://mirrors.aliyun.com/ubuntu/ bionic main restricted universe multiverse
    
    deb http://mirrors.aliyun.com/ubuntu/ bionic-security main restricted universe multiverse
    deb-src http://mirrors.aliyun.com/ubuntu/ bionic-security main restricted universe multiverse
    
    deb http://mirrors.aliyun.com/ubuntu/ bionic-updates main restricted universe multiverse
    deb-src http://mirrors.aliyun.com/ubuntu/ bionic-updates main restricted universe multiverse
    
    deb http://mirrors.aliyun.com/ubuntu/ bionic-proposed main restricted universe multiverse
    deb-src http://mirrors.aliyun.com/ubuntu/ bionic-proposed main restricted universe multiverse
     
    deb http://mirrors.aliyun.com/ubuntu/ bionic-backports main restricted universe multiverse
    deb-src http://mirrors.aliyun.com/ubuntu/ bionic-backports main restricted universe multiverse
    EOF
    
    3、# 成功后原项目要求执行如下命令安装基本的软件环境：
    apt-get update
    apt-get install -y vim wget python2.7 python-pip redis-tools dnsutils
    
    4、# 初始化集群
    首先，我们需要安装redis-trib
    pip install redis-trib==0.5.1
    
    然后，创建只有Master节点的集群
    redis-trib.py create \
      `dig +short redis-0.redis-hs.default.svc.cluster.local`:6379 \
      `dig +short redis-1.redis-hs.default.svc.cluster.local`:6379 \
      `dig +short redis-2.redis-hs.default.svc.cluster.local`:6379
      
    其次为每个Master添加Slave
    redis-trib.py replicate \
      --master-addr `dig +short redis-0.redis-hs.default.svc.cluster.local`:6379 \
      --slave-addr `dig +short redis-3.redis-hs.default.svc.cluster.local`:6379
    
    redis-trib.py replicate \
      --master-addr `dig +short redis-1.redis-hs.default.svc.cluster.local`:6379 \
      --slave-addr `dig +short redis-4.redis-hs.default.svc.cluster.local`:6379
    
    redis-trib.py replicate \
      --master-addr `dig +short redis-2.redis-hs.default.svc.cluster.local`:6379 \
      --slave-addr `dig +short redis-5.redis-hs.default.svc.cluster.local`:6379
      
    5、# 至此我们的Redis集群就真正创建完毕了，连到任意一个Redis Pod中检验一下
    kubectl exec -it redis-0 -- /bin/bash
    I have no name!@redis-0:/data$ /usr/local/bin/redis-cli -c
    127.0.0.1:6379> cluster nodes
    eebf17879e9e26fc7efffb2ebe8074c855cb37d8 172.16.2.24:6379@16379 slave 551916621f12b15305892487afc4858c45c9df81 0 1599269495222 1 connected
    c918f05894b9527ee19f9f514b64d220d25d4c9b 172.16.0.29:6379@16379 slave eee6fc225cd84ad8419ab9103c7d8322d3aeb220 0 1599269493218 0 connected
    551916621f12b15305892487afc4858c45c9df81 172.16.2.23:6379@16379 master - 0 1599269494219 1 connected 10923-16383
    eee6fc225cd84ad8419ab9103c7d8322d3aeb220 172.16.0.28:6379@16379 master - 0 1599269496225 0 connected 0-5461
    c88cd076f210bd2c7827f56a35027fb9a0b32c4f 172.16.1.36:6379@16379 slave 1003c52c3e5a79489bead8cc5233f9f6b683dc6c 0 1599269493000 2 connected
    1003c52c3e5a79489bead8cc5233f9f6b683dc6c 172.16.1.35:6379@16379 myself,master - 0 1599269494000 2 connected 5462-10922
    
    127.0.0.1:6379> cluster info
    cluster_state:ok
    cluster_slots_assigned:16384
    cluster_slots_ok:16384
    cluster_slots_pfail:0
    cluster_slots_fail:0
    cluster_known_nodes:6
    cluster_size:3
    cluster_current_epoch:5
    cluster_my_epoch:2
    cluster_stats_messages_ping_sent:125
    cluster_stats_messages_pong_sent:120
    cluster_stats_messages_meet_sent:2
    cluster_stats_messages_sent:247
    cluster_stats_messages_ping_received:120
    cluster_stats_messages_pong_received:127
    cluster_stats_messages_received:247
    ```

- 创建用于访问的`Service`

    ```shell
    # 删除服务
    kubectl delete -f redis-cs.yaml
    
    # 编写yaml
    cat >redis-cs.yaml<<\EOF
    apiVersion: v1
    kind: Service
    metadata:
      name: redis-cs
      labels:
        app: redis
    spec:
      ports:
      - name: redis-port
        protocol: "TCP"
        port: 6379
        targetPort: 6379
      selector:
        app: redis
    EOF
    
    # 创建SVC
    kubectl create -f redis-cs.yaml
    
    kubectl get svc redis-cs -o wide
    NAME       TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE   SELECTOR
    redis-cs   ClusterIP   172.16.202.170   <none>        6379/TCP   20s   app=redis
    
    # 如上在K8S集群中所有应用都可以通过172.16.202.170:6379来访问Redis集群
    kubectl describe svc redis-cs
    Name:              redis-cs
    Namespace:         default
    Labels:            app=redis
    Annotations:       Selector:  app=redis
    Type:              ClusterIP
    IP:                172.16.202.170
    Port:              redis-port  6379/TCP
    TargetPort:        6379/TCP
    Endpoints:         172.16.0.28:6379,172.16.0.29:6379,172.16.1.35:6379 + 3 more...
    Session Affinity:  None
    Events:
      Type    Reason         Age   From                Message
      ----    ------         ----  ----                -------
      Normal  EnsureService  92s   service-controller  Ensured Loadbalancer
    ```

- `Redis`集群测试

    ```shell
    yum install redis -y
    
    # 集群内测试（service ip测试）
    redis-cli -h 172.16.202.170 -p 6379
    172.16.202.170:6379> cluster info
    cluster_state:ok
    cluster_slots_assigned:16384
    cluster_slots_ok:16384
    cluster_slots_pfail:0
    cluster_slots_fail:0
    cluster_known_nodes:6
    cluster_size:3
    cluster_current_epoch:5
    cluster_my_epoch:0
    cluster_stats_messages_ping_sent:353
    cluster_stats_messages_pong_sent:373
    cluster_stats_messages_sent:726
    cluster_stats_messages_ping_received:372
    cluster_stats_messages_pong_received:353
    cluster_stats_messages_meet_received:1
    cluster_stats_messages_received:726
    ```
    
- 为集群设置认证信息

    ```shell
    # 在集群启动完毕后修改redis.yaml文件 添加启动参数
    args:
      - "/etc/redis/redis.conf"
      # redis-server --option 会覆盖配置文件中的参数配置
      - "--protected-mode no"
      - "--masterauth 密码"
      - "--requirepass 密码"
      
    kubectl apply -f redis.yaml  
    ```

- 测试主从切换

    在`K8S`上搭建完好`Redis`集群后我们最关心的就是其原有的高可用机制是否正常

    ```shell
    进入redis-0查看：
    kubectl exec -it redis-0 -- /bin/bash
    root@redis-app-0:/data # /usr/local/bin/redis-cli -c
    127.0.0.1:6379> role
    1) "master"
    2) (integer) 10836
    3) 1) 1) "172.16.1.41"
          2) "6379"
          3) "10836"
    如上可以看到redis-0为master，slave为172.16.1.41即redis-3
    
    接着，我们手动删除redis-0：kubectl delete pod redis-0
    pod "redis-0" deleted
    稍后Kubernetes立马为我们创建新的Pod顶上去，这时候查看Pods状态
    
    kubectl get pod redis-0 -o wide
    NAME      READY   STATUS              RESTARTS   AGE   IP       NODE          NOMINATED NODE   READINESS GATES
    redis-0   0/1     ContainerCreating   0          9s    <none>   172.27.0.12   <none>           <none>
    
    我们再进入redis-0内部查看:
    kubectl exec -it redis-0 -- /bin/bash
    I have no name!@redis-0:/data$ /usr/local/bin/redis-cli -c
    127.0.0.1:6379> role
    1) "slave"
    2) "172.16.1.41"
    3) (integer) 6379
    4) "connected"
    5) (integer) 3304
    如上redis-0变成了slave，从属于它之前的从节点172.16.1.41即redis-3
    ```