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

```
容器是镜像的一个运行实例，所不同的是镜像是静态的只读文件，而容器带有运行时需要的可写层，同时容器中的进程处于可写状态

新建容器 docker create 例如docker create -it centos:latest
启动容器 docker start 新建的容器处于停止状态，可以使用docker start来启动容器，例如docker start 69b96643ed7d
新建并启动容器 docker run，等价于先执行docker create命令，再执行 docker start命令，例如docker run -it centos:latest
以守护态运行容器 使用—d参数，例如docker run -d centos /bin/sh -c 'while true; do echo hello world; sleep 1; done'
等待容器退出 docker wait 等待容器退出并打印退出结果，并打印退出返回结果
查看容器运行日志 docker logs，例如docker logs -tf --tail 10 69b96643ed7d【- t:显示时间戳信息，- f:持续保持输出，- tail:输出最近的若干日志】

暂停容器 docker pause，例如docker pause 69b96643ed7d
恢复容器 docker unpause，例如docker unpause 69b96643ed7d
重启容器 docker restart，例如docker restart 69b96643ed7d
停止容器 docker stop，例如docker stop 69b96643ed7d，此时若执行docker container prune会自动清理掉所有已停止的容器
强制停止容器 docker kill，例如docker kill 69b96643ed7d，直接发送SIGKILL信号来强行终止容器

进入容器 docker attach，例如docker attach 69b96643ed7d，默认退出容器使用CTRL+P CTRL+Q，然而使用docker attach命令有时候不方便，当多个窗口同时attach到同一个容器的时候，所有窗口都同步显示，当因为某个窗口命令阻塞的时候所有窗口都无法执行操作了
在运行的容器内执行命令 docker exec，例如docker exec -it 69b96643ed7d /bin/bash，可以看到会打开一个新的bash终端，在不影响容器内其他应用的前提下，用户可以与容器交互
删除容器 docker rm，例如docker rm 69b96643ed7d，默认情况下docker rm只能删除已经处于终止状态或退出的容器，可以添加-f参数，Docker会先给容器发送SIGKILL信号给容器，终止其中应用之后强制删除

导出容器 docker export，可以使用-o参数指定导出的文件名称，例如docker export -o centos.tar.gz 69b96643ed7d
导入容器 docker import，例如docker import centos.tar.gz centos:v1.0，实际上既可以使用docker load命令来导入镜像存储文件到本地镜像库，也可以使用docker import命令来导入一个容器快照到本地镜像库。这两者的区别在于容器快照文件将丢弃所有的历史记录和元数据信息(即仅保存容器当时的快照状态)， 而镜像存储文件将保存完整记录，体积更大。 此外从容器快照文件导人时可以重新指定标签等元数据信息

查看容器详情 docker container inspect，例如docker container inspect 69b96643ed7d，可以使用-f参数指定查看某一个值 如docker container inspect -f {{".Id"}} 69b96643ed7d
查看容器内进程 docker ps，例如docker ps 69b96643ed7d，这个子命令类似于Linux系统中的top命令，会打印出容器内的进程信息，包括PID、用户、时间、命令等
查看统计信息 docker stats，例如docker stats 69b96643ed7d

其他容器相关命令
复制文件到容器中 docker cp，例如docker cp /tmp/data centos:/将本地/tmp/data复制到容器名为centos的根目录下
查看容器内文件系统的变更 docker diff，例如docker diff 69b96643ed7d
查看端口映射 docker port，例如docker port 69b96643ed7d
更新容器运行时配置 docker update，命令可以更新容器的一些运行时配置，主要是一些资源限制份额

更多docker容器相关命令可以使用docker container --help命令查看Docker支持的容器操作子命令
```

