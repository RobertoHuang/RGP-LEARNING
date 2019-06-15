# Docker Compose

## 安装

- 下载`docker-compose(v1.24.0)`

  ```shell
  $ sudo curl -L "https://github.com/docker/compose/releases/download/1.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  ```

- 添加`docker-compose`的执行权限

  ```shell
  $ sudo chmod +x /usr/local/bin/docker-compose
  ```

- 添加软链接

  ```shell
  $ sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
  ```

- 验证

  ```shell
  [root@localhost ~]# docker-compose version
  docker-compose version 1.24.0, build 0aa59064
  docker-py version: 3.7.2
  CPython version: 3.6.8
  OpenSSL version: OpenSSL 1.1.0j  20 Nov 2018
  ```

  > 以上安装步骤参考:[https://docs.docker.com/compose/install/](<https://docs.docker.com/compose/install/>)

