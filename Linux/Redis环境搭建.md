# Redis安装

- 下载最新稳定版的`redis`

  ```shell
  wget https://github.com/antirez/redis/archive/5.0.2.tar.gz
  ```

- 安装依赖包

  ```shell
  yum install -y epel-release
  yum install -y gcc
  ```

- 解压及编译

  ```shell
  # 解压
  cd /usr/local/redis/
  tar -zxvf 5.0.2.tar.gz
  
  # 编译
  cd redis-5.0.2/deps
  make jemalloc
  make hiredis
  make linenoise
  make lua
  cd ..
  make
  make install
  ```

- 修改配置文件

  ```shell
  cd /usr/local/redis/redis-5.0.2
  vi redis.conf
  
  # 修改支持远程访问
  bind 127.0.0.1 修改为 # bind 127.0.0.1
  # 进程在后台运行
  daemonize no 修改为 daemonize yes
  # 日志输出文件等信息
  logfile "" 修改为指定的日志文件 logfile "/var/log/redis/6379.log"
  ```

- 设置启动服务

  ```shell
  # 将上述配置好的配置文件复制到指定目录
  mkdir /etc/redis && cp /usr/local/redis/redis-5.0.2/redis.conf /etc/redis/redis.conf
  ```

  ```shell
  cat > /usr/lib/systemd/system/redis.service <<-EOF
  [Unit]
  Description=Redis 6379
  After=syslog.target network.target
  [Service]
  Type=forking
  PrivateTmp=yes
  Restart=always
  ExecStart=/usr/local/bin/redis-server /etc/redis/redis.conf
  ExecStop=/usr/local/bin/redis-cli -h 127.0.0.1 -p 6379 -a jcon shutdown
  User=root
  Group=root
  LimitCORE=infinity
  LimitNOFILE=100000
  LimitNPROC=100000
  [Install]
  WantedBy=multi-user.target
  EOF
  ```

  ```shell
  # 使服务自动运行
  systemctl daemon-reload
  systemctl enable redis
  
  # 启动服务
  systemctl restart redis
  systemctl status redis
  ```

- 如果启动服务过程出错，可以使用如下命令查看错误详细信息

  ```shell
  /usr/local/bin/redis-server /etc/redis/redis.conf
  ```