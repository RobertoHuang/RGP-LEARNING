# Zookeeper集群搭建

- 拉取镜像并推送到私有仓库

    ```shell
    docker pull mirrorgooglecontainers/kubernetes-zookeeper:1.0-3.4.10
    
    docker tag mirrorgooglecontainers/kubernetes-zookeeper:1.0-3.4.10 ccr.ccs.tencentyun.com/default-space/roberto-repository:kubernetes-zookeeper
    
    docker push ccr.ccs.tencentyun.com/default-space/roberto-repository:kubernetes-zookeeper
    ```

    过程需要认证

    `Docker`私有仓库认证

    ```shell
    sudo docker login --username=xxx ccr.ccs.tencentyun.com 回车后输入密码
    ```

    `K8S`私有仓库认证可参考[K8S私有仓库认证](https://kubernetes.io/zh/docs/tasks/configure-pod-container/pull-image-private-registry/)，如果使用`TKE`容器服务并且镜像也是使用云上的则可以直接使用`qcloudregistrykey`

- 开始部署`Zookeeper`

    ```yaml
    apiVersion: v1
    kind: Service
    metadata:
      name: zookeeper-hs
      labels:
        app: zookeeper
    spec:
      ports:
      - port: 2888
        name: server
      - port: 3888
        name: leader-election
      clusterIP: None
      selector:
        app: zookeeper
    ---
    apiVersion: v1
    kind: Service
    metadata:
      name: zookeeper-cs
      labels:
        app: zookeeper
    spec:
      ports:
      - port: 2181
        name: client
      selector:
        app: zookeeper
    ---
    apiVersion: policy/v1beta1
    kind: PodDisruptionBudget
    metadata:
      name: zookeeper-pdb
    spec:
      selector:
        matchLabels:
          app: zookeeper
      maxUnavailable: 1
    ---
    apiVersion: apps/v1
    kind: StatefulSet
    metadata:
      name: zookeeper
    spec:
      selector:
        matchLabels:
          app: zookeeper
      serviceName: zookeeper-hs
      replicas: 3
      updateStrategy:
        type: RollingUpdate
      podManagementPolicy: OrderedReady
      template:
        metadata:
          labels:
            app: zookeeper
        spec:
          affinity:
            # 保证Zookeeper被部署到不同节点上
            podAntiAffinity:
              requiredDuringSchedulingIgnoredDuringExecution:
                - labelSelector:
                    matchExpressions:
                      - key: "app"
                        operator: In
                        values:
                        - zookeeper
                  topologyKey: "kubernetes.io/hostname"
          imagePullSecrets:
          - name: qcloudregistrykey
          containers:
          - name: kubernetes-zookeeper
            imagePullPolicy: Always
            image: "ccr.ccs.tencentyun.com/default-space/roberto-repository:kubernetes-zookeeper"
            resources:
              requests:
                memory: "1Gi"
                cpu: "0.5"
            ports:
            - containerPort: 2181
              name: client
            - containerPort: 2888
              name: server
            - containerPort: 3888
              name: leader-election
            command:
            - sh
            - -c
            - "start-zookeeper \
              --servers=3 \
              --data_dir=/var/lib/zookeeper/data \
              --data_log_dir=/var/lib/zookeeper/data/log \
              --conf_dir=/opt/zookeeper/conf \
              --client_port=2181 \
              --election_port=3888 \
              --server_port=2888 \
              --tick_time=2000 \
              --init_limit=10 \
              --sync_limit=5 \
              --heap=512M \
              --max_client_cnxns=60 \
              --snap_retain_count=3 \
              --purge_interval=12 \
              --max_session_timeout=40000 \
              --min_session_timeout=4000 \
              --log_level=INFO"
            readinessProbe:
              exec:
                command:
                - sh
                - -c
                - "zookeeper-ready 2181"
              initialDelaySeconds: 10
              timeoutSeconds: 5
            livenessProbe:
              exec:
                command:
                - sh
                - -c
                - "zookeeper-ready 2181"
              initialDelaySeconds: 10
              timeoutSeconds: 5
            volumeMounts:
            - name: zookeeper-data
              mountPath: /var/lib/zookeeper
          securityContext:
            runAsUser: 1000
            fsGroup: 1000
      volumeClaimTemplates:
      - metadata:
          name: zookeeper-data
        spec:
          accessModes: [ "ReadWriteOnce" ]
          storageClassName: store-class-test
          resources:
            requests:
              storage: 10Gi
    ```

- 验证集群

    - 查看每个节点`ID`

        ```shell
        for i in 0 1 2; do kubectl exec zookeeper-$i cat /var/lib/zookeeper/data/myid; done
        ```

    - 查看每个节点配置

        ```shell
        for i in 0 1 2; do kubectl exec zookeeper-$i cat /opt/zookeeper/conf/zoo.cfg; done
        ```

    - 查看每个节点状态

        ```shell
        for i in 0 1 2; do kubectl exec zookeeper-$i zkServer.sh status; done
        ```

    - 数据测试

        ```shell
        kubectl exec zookeeper-0 zkCli.sh create /hello world
        
        kubectl exec zookeeper-1 zkCli.sh get /hello
        
        kubectl exec zookeeper-2 zkCli.sh get /hello
        ```

- 关于`K8S`集群安装`Zookeeper`可以参考官方文档[运行ZooKeeper， 一个CP分布式系统](https://kubernetes.io/zh/docs/tutorials/stateful-application/zookeeper/)

