# MySQL

- 下载安装包:[下载地址](http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.20-linux-glibc2.12-x86_64.tar.gz)，并解压

  ```shell
  # tar -zxvf mysql-5.7.20-linux-glibc2.12-x86_64.tar.gz
  
  # mv mysql-5.7.20-linux-glibc2.12-x86_64 mysql
  ```

- 创建`mysql`用户和用户组，并授权

  ```shell
  # groupadd mysql
  
  # useradd -r -g mysql mysql
  
  # chown -R mysql mysql
  
  # chgrp -R mysql mysql
  ```

- 创建配置文件

  ```reStructuredText
  [client]
  port = 3306
  socket = /tmp/mysql.sock
  
  [mysqld]
  character_set_server=utf8
  init_connect='SET NAMES utf8'
  basedir=/usr/local/mysql
  datadir=/usr/local/mysql/data
  socket=/tmp/mysql.sock
  log-error=/var/log/mysqld.log
  pid-file=/var/run/mysqld/mysqld.pid
  user=mysql
  
  # 最大连接数
  max_connections=5000
  # 表名不区分大小写
  lower_case_table_names = 1
  # SQL模式 可参考:https://blog.csdn.net/sunyadong_/article/details/86491139
  sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION
  
  [mysql]
  default-character-set=utf8
  socket=/tmp/mysql.sock
  ```

- 初始化数据库

  ```shell
  # yum install libaio
  
  # cd /var/log/
  # echo -> mysqld.log
  # chmod 777 mysqld.log
  # chown mysql:mysql mysqld.log
  
  # /usr/local/mysql/bin/mysqld --initialize --user=mysql --basedir=/usr/local/mysql --datadir=/usr/local/mysql/data --lc_messages_dir=/usr/local/mysql/share --lc_messages=en_US
  ```

  如果执行该命令时出现如下错误

  ```shell
  [root@VM_0_2_centos mysql]#  /usr/local/mysql/bin/mysqld --initialize --user=mysql --basedir=/usr/local/mysql --datadir=/usr/local/mysql/data --lc_messages_dir=/usr/local/mysql/share --lc_messages=en_US
  /usr/local/mysql/bin/mysqld: error while loading shared libraries: libnuma.so.1: cannot open shared object file: No such file or directory
  ```

  解决方案为:

  ```shell
  yum -y install numactl.x86_64
  ```

- 查看初始密码

  ```shell
  # cat /var/log/mysqld.log
  ```

- 启动服务，进入`MySQL`修改初始密码

  ```shell
  # cp /usr/local/mysql/bin/mysqld /etc/init.d/
  
  # service mysqld start 
  
  # cd /var/run/
  # mkdir mysqld
  # chown mysql:mysql mysqld
  
  # cd mysqld
  # echo -> mysqld.pid
  # chown mysql:mysql mysqld.pid 
  
  # /usr/local/mysql/support-files/mysql.server start
  # /usr/local/mysql/bin/mysql -uroot -p 你在上面看到的初始密码
  ```

- 修改初始密码

  ```mysql
  set password for root@localhost = password('root');
  ```

- 修改用户权限

  ```mysql
  use mysql;
  
  # 查看访问权限，如果host字段里面没有一个“%”(%代表所有人都可以远程访问)
  select user,host from user;
  
  #【第一个root是用户名，第二个root是密码。%代表所有IP，可以设置你自己的IP】
  GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION; 
  
  # 立即生效
  flush privileges;
  ```

- 开机自启动

  ```shell
  # cd /usr/local/mysql/support-files
  # cp mysql.server /etc/init.d/mysqld
  # chkconfig --add mysqld
  ```

- 使用`service mysqld`命令启动/停止服务

  ```shell
  # su mysql
  ~ service mysqld start/stop/restart
  ```

- 添加系统环境变量

  ```shell
  vim /etc/profile
  
  export PATH=/usr/local/mysql/bin:$PATH
  
  source /etc/profile
  ```


## 附录

如果出现错误:

- `Too many arguments (first extra is 'start')`

  ```
  遇到该问题通过直接输入 /路径/mysqld —user=mysql，的方式启动
  ```

  