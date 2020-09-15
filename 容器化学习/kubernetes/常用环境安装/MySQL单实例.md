# MySQL单实例

```shell
cat > mysql.yaml <<\EOF
apiVersion: v1
kind: Secret
metadata:
  name: mysql-password
type: Opaque
data:
  mysql-root-password: 'BASE64加密后的密码'

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: mysql-config
data:
  mysqld.cnf: |
        [mysqld]
        bind-address                   = 0.0.0.0
        default-time_zone              = '+8:00'
        init_connect                   = 'SET NAMES utf8'
        character-set-server           = utf8mb4
        pid-file                       = /var/run/mysqld/mysqld.pid
        socket                         = /var/run/mysqld/mysqld.sock
        datadir                        = /var/lib/mysql
        lower_case_table_names         = 1
        ignore-db-dir                  = 'lost+found'
        max_connections                = 5000
        sql_mode                       = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'

---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  labels:
    app: mysql
  name: mysql-data
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
  storageClassName: store-class-test

---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: mysql
  name: mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  strategy:
    type: Recreate
  template:
    metadata:
      labels:
        app: mysql
    spec:
      imagePullSecrets:
      - name: qcloudregistrykey
      containers:
      - name: mysql
        image: mysql:5.7
        imagePullPolicy: IfNotPresent
        resources:
          requests:
            memory: "1Gi"
            cpu: "0.5"
          limits:
            memory: "2Gi"
            cpu: "1"
        securityContext:
          privileged: true
          runAsUser: 0
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-password
              key: mysql-root-password
        ports:
        - name: mysql-port
          containerPort: 3306
        readinessProbe:
          exec:
            command:
            - /bin/sh
            - "-c"
            - MYSQL_PWD="${MYSQL_ROOT_PASSWORD}"
            - mysql -h 127.0.0.1 -u root -e "SELECT 1"
          initialDelaySeconds: 10
          timeoutSeconds: 1
          successThreshold: 1
          failureThreshold: 3
        livenessProbe:
          exec:
            command:
            - /bin/sh
            - "-c"
            - MYSQL_PWD="${MYSQL_ROOT_PASSWORD}"
            - mysql -h 127.0.0.1 -u root -e "SELECT 1"
          initialDelaySeconds: 30
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 3
        volumeMounts:
        - name: mysql-config
          mountPath: /etc/mysql/conf.d/
        - name: mysql-data
          mountPath: /var/lib/mysql/
      volumes:
      - name: mysql-config
        configMap:
          name: mysql-config
      - name: mysql-data
        persistentVolumeClaim:
          claimName: mysql-data
          
---
apiVersion: v1
kind: Service
metadata:
  labels:
    app: mysql
  name: mysql-service
spec:
  type: NodePort
  selector:
    app: mysql
  ports:
  - name: mysql-port
    protocol: TCP
    port: 3306
    targetPort: 3306
    nodePort: 30306
EOF

kubectl apply -f mysql.yaml 
```

