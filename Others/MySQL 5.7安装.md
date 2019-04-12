# MySQL

- 下载安装包:[下载地址](wget http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.20-linux-glibc2.12-x86_64.tar.gz)，并解压

  ```shell
  # tar -zxvf mysql-5.7.20-linux-glibc2.12-x86_64.tar.gz
  
  # mysql-5.7.20-linux-glibc2.12-x86_64.tar.gz mysql
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
  vim /etc/my.cnf
  
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
  
  # 不区分大小写
  lower_case_table_names = 1
  
  sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION
  
  max_connections=5000
  ```

- 初始化数据库

  ```shell
  # yum install libaio
  
  # cd /var/log/
  # echo mysqld.log
  # chmod 777 mysqld.log
  # chown mysql:mysql mysqld.log
  
  # /usr/local/mysql/bin/mysqld --initialize --user=mysql --basedir=/usr/local/mysql --datadir=/usr/local/mysql/data --lc_messages_dir=/usr/local/mysql/share --lc_messages=en_US
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
  # chmod 777 mysqld
  
  # cd mysqld
  # echo -> mysqld.pid
  # chown mysql:mysql mysqld.pid 
  
  # /usr/local/mysql/support-files/mysql.server start
  # /usr/local/mysql/bin/mysql -uroot -p 你在上面看到的初始密码
  ```

  ```sql
  set password=password('root');
  
  use mysql;
  
  UPDATE `mysql`.`user` SET `Host`='%', `User`='root', `Select_priv`='Y', `Insert_priv`='Y', `Update_priv`='Y', `Delete_priv`='Y', `Create_priv`='Y', `Drop_priv`='Y', `Reload_priv`='Y', `Shutdown_priv`='Y', `Process_priv`='Y', `File_priv`='Y', `Grant_priv`='Y', `References_priv`='Y', `Index_priv`='Y', `Alter_priv`='Y', `Show_db_priv`='Y', `Super_priv`='Y', `Create_tmp_table_priv`='Y', `Lock_tables_priv`='Y', `Execute_priv`='Y', `Repl_slave_priv`='Y', `Repl_client_priv`='Y', `Create_view_priv`='Y', `Show_view_priv`='Y', `Create_routine_priv`='Y', `Alter_routine_priv`='Y', `Create_user_priv`='Y', `Event_priv`='Y', `Trigger_priv`='Y', `Create_tablespace_priv`='Y', `ssl_type`='', `ssl_cipher`='', `x509_issuer`='', `x509_subject`='', `max_questions`='0', `max_updates`='0', `max_connections`='0', `max_user_connections`='0', `plugin`='mysql_native_password', `authentication_string`='*6BB4837EB74329105EE4568DDA7DC67ED2CA2AD9', `password_expired`='N', `password_last_changed`='2017-11-20 12:41:07', `password_lifetime`=NULL, `account_locked`='N' WHERE  (`User`='root');
  
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
  # su - mysql
  ~ service mysqld start/stop/restart
  ```

- 远程用户建立

  ```sql
  grant all privileges on *.* to '新用户名'@'%' identified by '新密码';
  
  flush privileges;
  ```

- 添加系统环境变量

  ```shell
  vim /etc/profile
  
  export PATH=/usr/local/mysql/bin:$PATH
  
  source /etc/profile
  ```

  

