# Zookeeper单机版安装

- 下载`Zookeeper`，以`Zookeeper3.6.2`为例子

    ```shell
    wget https://mirrors.tuna.tsinghua.edu.cn/apache/zookeeper/zookeeper-3.6.2/apache-zookeeper-3.6.2-bin.tar.gz
    ```

- 解压`Zookeeper`安装包

  ```shell
  tar -zxvf apache-zookeeper-3.6.2-bin.tar.gz && mv apache-zookeeper-3.6.2-bin apache-zookeeper-3.6.2
  ```

- 配置`Zookeeper`数据目录

  - 创建保存文件及日志的目录

    ```shell
    cd /usr/local/zookeeper/ && mkdir data
    ```
    
  - 修改`Zookeeper`的配置文件

    - 将`apache-zookeeper-3.6.2/conf`目录下的`zoo_sample.cfg`文件拷贝一份命名为为`zoo.cfg`

      ```shell
      # cp zoo_sample.cfg zoo.cfg
      ```

    - 编辑`zoo.cfg`将`zookeeper`的`dataDir`指向之前创建好的文件及日志目录，其他配置保持默认即可

      ```properties
      dataDir=/usr/local/zookeeper/data
      ```
    
    - 在`dataDir`下创建`myid`文件(编辑`myid`文件并在对应的`IP`的机器上输入对应的编号)
    
      ```shell
      # echo 1 > myid
      ```

- 添加`Zookeeper`环境变量配置(修改`/etc/profile`添加如下配置)，并使环境变量配置生效

  ```shell
  ZOOKEEPER_HOME=/usr/local/zookeeper/apache-zookeeper-3.6.2/
  export PATH=$ZOOKEEPER_HOME/bin:$PATH
  ```

- 启动并测试`Zookeeper`

  ```shell
  # 启动Zookeeper
  zkServer.sh start
   
  # 查看Zookeeper状态
  zkServer.sh status
   
  # 停止zookeeper
  zkServer.sh stop
  ```
  
- 设置`Zookeeper`服务开机启动

  - 在`/etc/rc.d/init.d`添加可执行文件`zookeeper`

    ```shell
    # 切换到/etc/rc.d/init.d/目录下
    cd /etc/rc.d/init.d
    
    # 创建zookeeper文件
    touch zookeeper
    
    # 将zookeeper修改为可执行文件
    chmod +x zookeeper
    
    # 编辑文件，在zookeeper里面输入如下内容
    #!/bin/bash
    #chkconfig:2345 20 90
    #description:zookeeper
    #processname:zookeeper
    export JAVA_HOME=/usr/local/java/jdk1.8
    export PATH=$JAVA_HOME/bin:$PATH
    case $1 in
         start) su root /usr/local/zookeeper/apache-zookeeper-3.6.2/bin/zkServer.sh start;;
         stop) su root /usr/local/zookeeper/apache-zookeeper-3.6.2/bin/zkServer.sh stop;;
         status) su root /usr/local/zookeeper/apache-zookeeper-3.6.2/bin/zkServer.sh status;;
         restart) su root /usr/local/zookeeper/apache-zookeeper-3.6.2/bin/zkServer.sh restart;;
         *) echo "require start|stop|status|restart";;
    esac
    ```

  - 这个时候我们就可以用`service zookeeper start/stop`来启动停止`Zookeeper`服务了

  - 使用命令把`zookeeper`添加到开机启动任务中

    ```shell
    chkconfig zookeeper on
    chkconfig --add zookeeper
    
  # 添加完成之后使用下面的命令来看看我们添加的zookeeper是否在里面
    chkconfig --list
    ```
  
    如果上面的操作都正常的话，你就可以重启你的`Linux`服务器了验证上述配置是否都生效了
