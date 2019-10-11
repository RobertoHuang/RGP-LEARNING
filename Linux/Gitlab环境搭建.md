# Gitlab

> 环境:Centos 7.X

- 打开系统防火墙中的`HTTP`和`SSH`访问

  ```sh
  sudo yum install -y curl policycoreutils-python openssh-server
  sudo systemctl enable sshd
  sudo systemctl start sshd
  sudo firewall-cmd --permanent --add-service=http
  sudo systemctl reload firewalld
  ```

  在执行 `sudo firewall-cmd --permanent --add-service=http` 时可能会遇到 `FirewallD is not running` 错误提示，意思是`未运行防火墙`。 使用以下命令开启防火墙即可

  ```shell
  systemctl start firewalld.service
  ```

- 安装`Postfix`邮件服务

  ```
  sudo yum install postfix
  sudo systemctl enable postfix
  sudo systemctl start postfix
  ```

  这一步可能会遇到一个报错 `Job for postfix.service failed because the control process exited with error code. See "systemctl status postfix.service" and "journalctl -xe" for details.`解决方法是修改 `/etc/postfix/main.cf`的配置

  ```shell
  vi /etc/postfix/main.cf
  
  # 修改内容如下
  inet_interfaces = all
  inet_protocols = ipv4 // 或 all
  
  # 修改完成后执行
  sudo systemctl restart postfix
  ```

- 安装`Gitlab`

  - 添加`Gitlab`包的仓库

    ```shell
    curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ee/script.rpm.sh | sudo bash
    ```

  - 安装`Gitlab`包

    ```shell
    sudo EXTERNAL_URL="https://gitlab.example.com" yum install -y gitlab-ee
    ```

    将`https://gitlab.example.com`更改为您要访问`GitLab`实例的`URL`，由于我们这里没有域名则使用

    ```shell
    # 直接使用 IP + 端口号的形式
    sudo EXTERNAL_URL="0.0.0.0:6677" yum install -y gitlab-ee
    ```

    如果安装完之后要修改访问的域名或者`IP`则需修改 `/etc/gitlab/gitlab.rb` 文件中的 `external_url` 

  - 修改配置完成后重新配置服务

    ```
    gitlab-ctl reconfigure
    ```

- `Gitlab`常用命令

  ```shell
  //启动
  sudo gitlab-ctl start
  //停止
  sudo gitlab-ctl stop
  //重启
  sudo gitlab-ctl restart
  //查看状态
  sudo gitlab-ctl status
  //使更改配置生效
  sudo gitlab-ctl reconfigure
  ```

