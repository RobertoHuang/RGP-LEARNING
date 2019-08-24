# Jenkins环境搭建

## 安装

- 拉取`Jenkins`库

  ```shell
  sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
  sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io.key
  ```

- 安装`Jenkins`包

  ```shell
  yum install -y jenkins
  ```

- 更新`Jenkins`包

  ```shell
  yum update jenkins
  ```

- 开启和关闭`Jenkins`服务

  ```shell
  service jenkins start
  
  service jenkins stop
  ```

- 可能出现的问题`fixed`

  - `Starting Jenkins bash: /usr/bin/java: No such file or direct`

    ```
    # 1.编辑Jenkins服务启动文件
    vim /etc/init.d/jenkins
    
    # 将candidates中的/usr/bin/java修改为系统的JAVA路径 如下
    /usr/local/sdkman/candidates/java/current/bin/java
    ```

## 插件安装

- `Maven`项目支持:`Maven Integration`
- 通过`SSH`发布项目:`Publish Over SSH`
- 构建时候支持`Git`分支选择:`Git Parameter`

## 权限管理

<div  align="center">    
    <img src="https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Linux/images/Jenkins%E6%9D%83%E9%99%90%E9%85%8D%E7%BD%AE.png" alt="jenkins权限配置" align=center />
</div>

## SpringBoot项目部署脚本(deploy.sh)

```shell
#!/bin/bash
# loading profile and env
if [ -f "/etc/profile" ]; then
	source /etc/profile
fi
if [ -f "~/.bashrc" ]; then
	source ~/.bashrc
fi
if [ -f "~/.profile" ]; then
	source ~/.profile
fi
if [ -f "~/.bash_profile" ]; then
	source ~/.bash_profile
fi

SERVICE_HOME=/usr/local
SERVICE_NAME="live-training"

FILENEW=$SERVICE_HOME/$SERVICE_NAME/filenew
FILEBAK=$SERVICE_HOME/$SERVICE_NAME/filebak

echo "**********   DEPLOY START   **********"
# 如果文件夹不存在则创建
if [ ! -d $FILENEW  ];then
	mkdir -p $FILENEW
fi
# 如果文件夹不存在则创建
if [ ! -d $FILEBAK  ];then
	mkdir -p $FILEBAK
fi

# 获取服务器端项目的进程
echo "NOW start closing the old process..."
tpid=`ps -ef | grep "$SERVICE_NAME" | grep -v "grep" | awk '{print $2}'`
# 如果存在已有的进程则进行关闭
for id in $tpid
do
	echo "founding running instance of " $SERVICE_NAME $id ", stoping..."
	kill -15 $id
done
echo "Closing the old process finished..."

# 备份当前版本数据
echo "NOW backup and remove the current version..."
TIMESTAMP=`date +%Y%m%d%H%M%S`
cd $FILENEW

# 备份日志文件
if [ -d logs  ];then
	echo "backup log..."
	tar cf $FILEBAK/$SERVICE_NAME.logs.$TIMESTAMP.tar.gz logs/
fi

# 备份安装包文件
if [ -f $SERVICE_NAME-*.jar ];then
	echo "backup install package..."
	tar cf $FILEBAK/$SERVICE_NAME.$TIMESTAMP.tar.gz $SERVICE_NAME-*.jar
fi
echo "backup and remove the current version finished..."

# 删除旧版安装包
echo "Start deleting old installation packages..."
rm -rf $FILENEW/*
echo "deleting old installation packages finished..."

# 将最新上传的war包从上传目录移动到项目启动目录
mv $SERVICE_HOME/$SERVICE_NAME/$SERVICE_NAME-*.jar $FILENEW

# 启动项目
cd $FILENEW
echo "Start preparing to deploy a new version..."
nohup java -Dspring.profiles.active=test -jar $SERVICE_NAME-*.jar >/dev/null 2>&1 &
echo "Deployment of new version is finished..."
echo "**********   DEPLOY SUCCESS   **********
```

