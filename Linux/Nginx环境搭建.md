# Nginx环境搭建

## 安装Nginx

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

- `Nginx HTTPS`配置 - 阿里云证书

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

  ```shell
  location ^~ /adminManager/ {
      alias   /usr/local/live-training-admin-front/html;
      index  index.html; # 默认访问文件
      try_files $uri $uri/ /adminManager/index.html; # 目录不存在则执行内部重定向
  }
  
  location ^~ /live-training/ {
      proxy_read_timeout      60;
      proxy_pass              http://localhost:8899/;
      # proxy_set_header        X-Real-IP $remote_addr;
      # proxy_set_header        X-Forwarded-For $proxy_add_x_forwarded_for;
  }
  ```
  

## 安装第三方模块

- 查看`Nginx`当前编译安装的模块

    ```shell
    nginx -V
    ```

- 加入需要安装的模块，重新编译。以`nginx_upstream_check_module`为例

    ```shell
    # 下载脚本到指定目录
    cd /usr/local/nginx/third
    wget https://codeload.github.com/yaoweibin/nginx_upstream_check_module/zip/master
    unzip master
    ```

- 为`Nginx`打补丁

    ```shell
    # 进入Nginx源码目录
    cd /usr/local/nginx/nginx-1.14.2/
    # 如果patch命令不存在则安装patch命令
    yum -y install patch
    # -p0，是“当前路径”  -p1，是“上一级路径”
    patch -p1 < /usr/local/nginx/third/nginx_upstream_check_module-master/check_1.14.0+.patch
    ```

- 编译添加新模块

    ```shell
    # 这里是ngin-v拿到旧模块配置
    ./configure --user=nginx --group=nginx --prefix=/usr/local/nginx --with-http_stub_status_module --with-http_ssl_module --with-http_realip_module --with-http_gzip_static_module 
    # 这里是新添加的模块配置信息
    --add-module=../third/nginx_upstream_check_module-master
    ```

- 编译生成的`Nginx`执行文件

    ```shell
    make
    ```

- 验证编译后的`Nginx`模块信息

    ```shell
    /usr/local/nginx/nginx-1.14.2/objs
    ./nginx -V
    ```

- 替换现有版本的`Nginx`执行文件

    ```shell
    mv /usr/local/nginx/sbin/nginx{,.ori}
    cp /usr/local/nginx/nginx-1.14.2/objs/nginx /usr/local/nginx/sbin/
    ```

- 检查配置是否正常， 查看`Nginx`现在的模块

    ```shell
    /usr/local/nginx/sbin/nginx -t
    /usr/local/nginx/sbin/nginx -V
    ```

- 配置`nginx_upstream_check_module`，开放监控页面

    ```shell
    upstream www_server_pools {
        server 192.168.190.131:80 weight=1;
        server 192.168.190.132:80 weight=1;
        # interval检测间隔时间，单位为毫秒
        # rsie请求2次正常的话，标记此realserver的状态为up
        # fall表示请求5次都失败的情况下，标记此realserver的状态为down
        # timeout为超时时间，单位为毫秒
        # 官方文档:https://github.com/yaoweibin/nginx_upstream_check_module
        check interval=3000 rise=3 fall=5 timeout=1000 type=http;
    }
    
    server {
        listen       80;
        server_name  www.xxx.com;
    
        location / {
            proxy_pass http://www_server_pools;
        }
    
        location /status {
            check_status;
            access_log off;
        }
    }
    ```

    **注意:配置完成Nginx必须重启，不能重新加载。监控页地址:http://hostname/status**

## 聊一聊Location匹配

- 语法规则

    ```nginx
    location [ = | ~ | ~* | ^~ | @ ] uri { ... }
    ```

    一个`location`关键字后面跟着可选的修饰符，后面是要匹配的字符，花括号中是要执行的操作

    ```reStructuredText
    / 通用匹配，任何请求都会匹配到
    ~ 区分大小写的正则匹配
    ~* 不区分大小写的正则匹配
    = 绝对匹配，如果匹配到了这个将停止匹配并处理该请求
    ^~ 如果把这个前缀用于一个常规字符串,那么告诉nginx如果路径匹配那么不再去匹配正则
    ```

- 匹配过程

    一个具体的请求`path`过来之后，`Nginx`的具体匹配过程可以分为这么几步

    - 先匹配普通字符串，然后再匹配正则表达式。
    - 一般情况下，匹配成功了普通字符串`location`后还会进行正则表达式的`location`匹配。有两种方法能够改变这种方式【一个是使用`=`进行绝对匹配，另一个是使用`^~`前缀匹配】，它匹配到普通字符 `location`之后不会再去寻找正则匹配
    - 普通字符串匹配顺序是根据配置中的字符长度从长到短，也就是使用普通字符串的匹配顺序和`location`之间的先后顺序是无关的，最后`Nginx`都会根据配置的字符长短来进行匹配
    - 正则表达式则是按照配置文件里的顺序来匹配，找到第一个匹配的正则表达式将停止搜索

- `@name`的用法

    `@`用来定义一个命名`location`。主要用于内部重定向，不能用来处理正常的请求。其用法如下

    ```nginx
    location / {
        try_files $uri $uri/ @custom
    }
    
    location @custom {
        # ...do something
    }
    ```

- `try_files`详解

    ```nginx
    location ^~ /adminManager {
        alias   /usr/local/html/;
        index  index.html; # 默认访问文件
        try_files $uri $uri/ /adminManager/index.html; # 目录不存在则执行index.html
    }
    ```

    当用户请求http://hostname/adminManager/xxx时，这里的`$uri`就是`/xxx`

    `try_files`会到硬盘里尝试找这个文件

    如果存在名为`/usr/local/html/adminManager`的文件，就直接把这个文件的内容发送给用户

    如果没有则在 `$uri/`增加了一个`/`，也就是看有没有名为`/usr/local/html/example/`的目录

    又找不到就会`fall back`到`try_files`的最后一个选项`/adminManager/index.html`发起一个内部"子请求"，也就是相当于`Nginx`发起一个`HTTP`请求到`http://hostname/adminManager.html`

    **不同于`index`的是`try_files`只有最后一个参数可以引起一个内部重定向，之前的参数只设置内部URI的指向**

- `index`详解

    `index`指令的作用是在前后端分离的基础上通过`Nginx`配置，指定网站初始页

    ```nginx
    location ^~ /adminManager {
        alias   /usr/local/html;
        index  index.html index.php; # 默认访问文件
    }
    ```

    如果你使用http://hostname/adminManager/直接发起请求

    结合`alias`与`index`指令，会先判断`/usr/local/html/index.html`是否存在

    如果不则接着查看`/usr/local/html/index.php` ，如果存在则使用`/index.php`发起内部重定向，就像从客户端再一次发起请求一样，相当于`Nginx`发起一个`HTTP`请求到`http://hostname/adminManager/index.php`

    **配置root或alias时文件结尾可以不需要添加/，个人理解try_files和index是两个不同的东西**

    **try_files生命周期在index之前，内部重定向与内部URI指向的差异是内部重定向需要重新location规则匹配**

- `localtion`中的`root`和`alias`区别

    ```nginx
    location /image {
        root /home/image/;
    }
    ```

    ```nginx
    location /image {
        alias /home/image/;
    }
    ```

    上面的两个配置当用户访问http://hostname/image/test.jpg 时，访问的文件路径是不一样的

    - 当使用`root`时路径为http://hostname/home/image/image/test.jpg
    - 当使用`alias`时路径为http://hostname/home/image/test.jpg

    两者的区别在于`root`是将实际访问文件路径即`root`后面的路径拼接`URL`中的路径，而`alias`是实际访问文件路径即`alias`后面的路径不去拼接`URL`中的路径

## 聊一聊Nginx的rewrite重写

```nginx
if ($uri !~ ^/nestle){
    rewrite ^/(.*)$ /nestle/$1;
}
```

