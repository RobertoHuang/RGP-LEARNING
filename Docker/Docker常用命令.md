# Docker常用命令



## 镜像相关

```shell
搜索镜像 docker search
获取镜像 docker pull [image] 例如docker pull ubuntu:18.04，如果不指定tag默认下载为latest
查看镜像列表 docker images
查看指定镜像详细信息 docker inspect [image] 例如docker inspect centos:latest，可以使用-f参数指定查看某一个值 如docker inspect -f {{".Id"}} centos:latest
给镜像加标签 例如docker tag centos:latest centos:customtag 它们实际上指向了同一个镜像文件，只是别名不同而巳
删除镜像 docker rmi [image] 当有该镜像创建的容器存在时，镜像文件默认是无法被删除的【可以使用—f参数强制删除 但不建议这么做】
清理镜像 docker image prune 清理使用Docker一段时间后，系统中可能会遗留一些临时的镜像文件以及一些没有被使用的镜像

创建镜像
  1、基于已有容器
      1.1.启动一个镜像并在其中进行修改操作。例如创建一个test文件之后退出
          docker run -it --name custom-ubuntu ubuntu:18.04 /bin/bash
          touch test && exit
      1.2.记住容器的ID，此时该容器与原ubuntu:18.04镜像相比已经发生了改变，可以使用docker commit命令提交一个新镜像
          docker commit -m "custom ubuntu" -a "RobertoHuang" 773c33a1839b custom-ubuntu:0.1
      1.3.顺利的话会返回新创建镜像的ID信息，可以使用docker images进行查看
  2、基于本地模板导入
      2.1.直接从一个操作系统模板文件导入一个镜像。推荐使用OpenVZ提供的模板来创建
          http://openvz.org/Download/templates/precreated
      2.2.比如，下载一个 ubuntu-14.04 的模板压缩后，可以使用以下命令导入
          cat ubuntu-14.04-x86_64-minimal.tar.gz | docker import - ubuntu:14.04
  3、使用DockerFile进行构建
      3.1.基于Dockerfile创建是最常见的方式。Dockerfile是一个文本文件，利用给定的指令描述基于某个父镜像创建新镜像的过程
存入镜像 docker [image] save 如果要导出镜像到本地文件可以使用docker [image] save命令导出镜像到指定的文件中，例如 docker save -o ubuntu.18.04.tar ubuntu:18.04
载入镜像 docker [image] load 将导出的tar文件再导人到本地镜像库，例如docker load -i ubuntu.18.04.tar。导入成功后可以使用docker images查看与原镜像一致
上传镜像 docker push [image]，用户user上传本地的镜像需先添加标签，如:docker tag custom-ubuntu:0.1 201210704116/custom-ubuntu:0.1再进行push操作，如docker push 201210704116/custom-ubuntu:0.1
```

> 一般来说镜像的`latest`标签意味着该镜像的内容会跟踪最新版本的变更而变化， 内容是不稳定的。因此从稳定性上考虑，不要在生产环境中忽略镜像的标签信息或使用默认的`latest`标记的镜像

## 容器相关

