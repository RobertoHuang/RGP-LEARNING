# Nginx

- 安装依赖

  ```shell
  yum -y install gcc gcc-c++
  yum -y install zlib zlib-devel openssl openssl-devel pcre-devel
  ```

  如出现`Multilib version problems found`则使用`yum -y install --setopt=protected_multilib=false`

- 下载`Nginx`的`bin`包解压

  可以在下载页面`http://nginx.org/download/`中找到下载地址

  ```shell
  wget http://nginx.org/download/nginx-1.14.2.tar.gz
  tar -zxvf nginx-1.14.2.tar.gz
  cd nginx-1.14.2
  ```

- 添加`Nginx`组和用户

  ```shell
  groupadd nginx
  useradd nginx -g nginx -s /sbin/nologin -M
  ```

- 对`Nginx`进行配置

  ```shell
  ./configure --user=nginx --group=nginx --prefix=/usr/local/nginx --with-http_stub_status_module --with-http_ssl_module --with-http_realip_module --with-http_gzip_static_module
  ```

- 查看是否配置成功(`$?`是显示最后命令的退出状态，0表示没有错误，其他表示有错误)

  ```shell
  echo $?
  ```

- 检查完成后进行编译安装

  ```shell
  make && make install
  ```

- 检查编译安装是否成功(`$?`是显示最后命令的退出状态，0表示没有错误，其他表示有错误)

  ```shell
  echo $?
  ```

- 查看当前`Nginx`版本号

  ```shell
  /usr/local/nginx/sbin/nginx -v
  ```

- 编写`Nginx`服务启动脚本，设置`Nginx`开机启动

  ```shell
  cd /lib/systemd/system/
  vi nginx.service
  ```
  
  内容如下
  
  ```shell
  [Unit]
  Description=nginx
  After=network.target remote-fs.target nss-lookup.target
   
  [Service]
  Type=forking
  PIDFile=/usr/local/nginx/logs/nginx.pid
  ExecStartPost=/bin/sleep 0.1
  ExecStartPre=/usr/local/nginx/sbin/nginx -t -c /usr/local/nginx/conf/nginx.conf
  ExecStart=/usr/local/nginx/sbin/nginx -c /usr/local/nginx/conf/nginx.conf
ExecReload=/bin/kill -s HUP $MAINPID
  ExecStop=/bin/kill -s QUIT $MAINPID
  PrivateTmp=true
  
  [Install]
  WantedBy=multi-user.target
  ```
  
  命令如下
  
  ```shell
  systemctl start|stop|reload|restart|status nginx.service
    
  # 开机自启
  systemctl enable nginx.service
    
  # 关闭开机自启
  systemctl disable nginx.service
  ```

- `Nginx` `HTTPS`配置 - 阿里云证书

  ```
  server {
      listen 443;
      server_name 你的域名;
      ssl on;
      root html;
      index index.html index.htm;
      ssl_certificate      /xxx.pem;
      ssl_certificate_key  /xxx.key;
      ssl_session_timeout 5m;
      ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
      ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
      ssl_prefer_server_ciphers on;
  
      location / {
          root html;
          index  index.html index.htm;
      }
      
      error_page  404              /404.html;
  }
  
  server {
      listen       80;
      server_name  你的域名;
      rewrite ^(.*)$ https://${server_name}$1 permanent;
  }
  ```


- `Nginx`部署`VUE`项目路由配置

  ```
  location ^~ /adminManager {
      alias   /usr/local/live-training-admin-front/html/;
      index  index.html; #默认访问文件
      try_files $uri $uri/ /adminManager/index.html; #目录不存在则执行index.html
  }
  
  location / {
      root   /usr/local/live-training-front/html; #默认访问目录
      index  index.html; #默认访问文件
      try_files $uri $uri/ /index.html; #目录不存在则执行index.html
  }
  ```

  