# 文档服务

## Docker环境配置

- Link:[Centos7上安装配置Docker](https://github.com/RobertoHuang/RGP-LEARNING/blob/master/Docker/01.Centos7%E4%B8%8A%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AEDocker.md)

- 编写`DockerFile`文件

  ```reStructuredText
  FROM node:10-alpine
  
  RUN npm i docsify-cli -g --registry=https://registry.npm.taobao.org
  
  ONBUILD COPY src /srv/docsify/docs
  ONBUILD WORKDIR /srv/docsify
  
  CMD ["/usr/local/bin/docsify", "serve", "docs"]
  ```

  ```reStructuredText
  FROM hub.comm.com:5000/docsify:onbuild
  ```

## 安装Runner

- 添加`Gitlab`官方源

  ```reStructuredText
  $ curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.rpm.sh | sudo bash
  ```

- 安装`Runner`

  ```reStructuredText
  $ sudo yum install -y gitlab-runner
  ```

  以上安装步骤参考:[https://docs.gitlab.com/runner/install/linux-repository.html](https://docs.gitlab.com/runner/install/linux-repository.html)

- 为`Runner`添加操作`Docker`权限

  ```reStructuredText
  $ sudo groupadd docker
  $ sudo gpasswd -a gitlab-runner docker
  $ sudo systemctl restart docker
  $ sudo newgrp - docker
  ```

## Gitlab项目准备

- 安装`Gitlab`环境(略)

- 初始化项目

  - 新建一个`Gitlab`项目

  - 新建完成后进入项目，在左侧菜单中导航进入`Settings > CI / CD`菜单，在右侧面板中点击 `Runner` 选项卡的 `Expand` 展开Runner 详情，如下图

    ![runner-setting](<https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Others/images/runner-setting.png>)

    **注意:**上图红框中的内容`TOKEN`在下一步注册`Runner`会用到

## 将Runner注册到项目中

> 其中用中括号 `[...]` 包括的内容表示需要手动修改的

```reStructuredText
$ sudo gitlab-runner register \
  --non-interactive \
  --executor "shell" \
  --url "[Gitlab服务地址 对应上图红框上内容]" \
  --registration-token "[项目对应的TOKEN, 对应上图红框内容]" \
  --description "[描述, 例如: my description]" \
  --tag-list "[标签列表, 多个逗号隔开, 例如: commdoc-runner-tag-1]"
```

`Runner`注册完成之后，刷新 `Settings > CI / CD > Runner` 页面，如下图

![runner-setting-runner.png](<https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Others/images/runner-setting-runner.png>)

可以点击上图中的红框所示编辑按钮，对`Runner`进行编辑(修改`description，tags`等..)

**注意:**注册`Runner`时，`tag`很重要，后续的步骤会用到

## Docsify项目准备

未完待续...

## 附录

参考文档:

- [.gitlab-ci.yml语法详解 https://docs.gitlab.com/ee/ci/yaml/](https://docs.gitlab.com/ee/ci/yaml/)
- [docsify文档生成器 https://docsify.js.org/#/?id=docsify-494](https://docsify.js.org/#/?id=docsify-494)

- [VuePress静态网站生成器 https://vuepress.vuejs.org/zh/guide/](https://vuepress.vuejs.org/zh/guide/)