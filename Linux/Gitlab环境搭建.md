# Gitlab环境搭建

> 环境:Centos 7.X

- 安装相关依赖

    ```shell
    sudo yum install -y curl policycoreutils-python openssh-server
    ```

- 启动`SSH`服务&设置为开机启动 打开系统防火墙中的`HTTP`和`SSH`访问

  ```sh
  sudo systemctl enable sshd
  sudo systemctl start sshd
  
  sudo firewall-cmd --add-service=ssh --permanent
  sudo firewall-cmd --add-service=http --permanent 
  sudo systemctl reload firewalld
  ```

  在执行 `sudo firewall-cmd --add-service=xxx --permanent` 时可能会遇到 `FirewallD is not running` 错误提示，意思是未运行防火墙。 使用以下命令开启防火墙即可

  ```shell
  systemctl start firewalld.service
  ```

- 安装`Postfix`邮件服务&设置开机启动

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
  # 修改内容如下或all
  inet_protocols = ipv4 
  
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

  - 配置邮件服务器

    ```shell
    vim /etc/gitlab/gitlab.rb
    
    gitlab_rails['smtp_enable'] = true
    gitlab_rails['smtp_address'] = "smtp.qq.com"
    gitlab_rails['smtp_port'] = 465
    gitlab_rails['smtp_user_name'] = "robertohuang@foxmail.com"
    gitlab_rails['smtp_password'] = "xxx xxx xxx xxx"
    gitlab_rails['smtp_domain'] = "smtp.qq.com"
    gitlab_rails['smtp_authentication'] = "login"
    gitlab_rails['smtp_enable_starttls_auto'] = true
    gitlab_rails['smtp_tls'] = true
    gitlab_rails['gitlab_email_from'] = "robertohuang@foxmail.com" #注意这个一定要填写，不然会报502错误
    ```

    修改配置完成后重新配置服务

    ```shell
    gitlab-ctl reconfigure
    ```

    测试配置是否成功。执行`gitlab-rails console`进入控制台然后执行测试发送邮件命令

    ```shell
    Notify.test_email(‘收件人邮箱’, ‘邮件标题’, ‘邮件正文’).deliver_now
    ```

    当你看到以下提示时，那么恭喜你你配置成功啦

    ```
    => #<Mail::Message:69831111856280, Multipart: false, Headers: <Date: Fri, 11 Oct 2019 15:59:35 +0800>, <From: GitLab <robertohuang@foxmail.com>>, <Reply-To: GitLab <noreply@0.0.0.0>>, <To: 756858620@qq.com>, <Message-ID: <5da0366743734_59ce3f82ec0cf98c468d7@VM_0_2_centos.mail>>, <Subject: test>, <Mime-Version: 1.0>, <Content-Type: text/html; charset=UTF-8>, <Content-Transfer-Encoding: 7bit>, <Auto-Submitted: auto-generated>, <X-Auto-Response-Suppress: All>>
    ```

  - 禁用创建组权限

      `GitLab`默认所有的注册用户都可以创建组。但对于团队来说通常只会给`Leader`相关权限。虽然可以在用户管理界面取消权限但毕竟不方便。我们可以通过配置`GitLab`默认禁用创建组权限

      ```shell
      # 修改配置文件
      sudo vi /etc/gitlab/gitlab.rb
      
      # 开启gitlab_rails['gitlab_default_can_create_group'] 选项，并将值设置为false
      gitlab_rails['gitlab_default_can_create_group'] = false
      
      # 保存后，重新配置并启动GitLab
      sudo gitlab-ctl reconfigure
      ```

  - 防火墙开放端口

    ```shell
    firewall-cmd --zone=public --add-port=6677/tcp --permanent
    firewall-cmd --reload
    ```

  - 设置成中文

    ```reStructuredText
    User Settings > Preferences > Localization 设置成中文
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

- 启动完成后需要在控制台禁止用户注册功能