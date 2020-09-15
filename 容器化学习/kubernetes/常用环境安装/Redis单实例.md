# Redis单实例

```shell
cat >redis.yaml <<\EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: redis-config
data:
  redis.conf: |
        protected-mode no
        requirepass redis的密码
        logfile /data/redis.log

---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: redis
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
      imagePullSecrets:
      - name: qcloudregistrykey
      containers:
      - name: redis
        image: redis
        imagePullPolicy: IfNotPresent
        resources:
          requests:
            memory: "1Gi"
            cpu: "0.5"
          limits:
            memory: "4Gi"
            cpu: "1"
        securityContext:
          privileged: true
          runAsUser: 0
        command:
          - "redis-server"
        args:
          - "/etc/redis/redis.conf"
        ports:
        - name: redis-port
          containerPort: 6379
        readinessProbe:
          exec:
            command:
            - sh
            - -c
            - "redis-cli -h $(hostname) ping"
          initialDelaySeconds: 15
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 3
        livenessProbe:
          exec:
            command:
            - sh
            - -c
            - "redis-cli -h $(hostname) ping"
          initialDelaySeconds: 20
          periodSeconds: 3
          successThreshold: 1
          failureThreshold: 3
        volumeMounts:
        - name: redis-config
          mountPath: "/etc/redis"
      volumes:
      - name: redis-config
        configMap:
          name: redis-config

---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
spec:
  type: NodePort
  selector:
    app: redis
  ports:
  - name: redis-port
    protocol: TCP
    port: 6379
    targetPort: 6379
    nodePort: 30379
EOF

kubectl apply -f redis.yaml 
```

验证是否正常

```shell
kubectl exec -it `kubectl get pods| grep redis| awk '{print $1}'` -- /bin/bash

# 查看Redis相关日志
tail -100f /data/redis.log

# 连接Redis实例进行相关操作
redis-cli -h 127.0.0.1 -a redispassword -p 6379
```

