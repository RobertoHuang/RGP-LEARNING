# 命令

> 吾生也有涯，而知也无涯，以有涯随无涯。殆己
>
> 我们的时间是有限的但是知识是无限的，不要用有限的时间去追求无限的知识，掌握工作中需要用到的即可

- 帮助命令

    - `man`有问题找男人
    - `help`内部命令使用`help cd` 外部命令使用`ls --help`，可以使用`type`名称查看对应命令类型

    - `info`帮主比`help`更详细，作为`help`的补充

- `ls`常用参数

    - `l`以长格式显示
    - `a`显示隐藏文件夹
    - `t`按时间进行排序
    - `r`对排序结果进行逆向

    - `R`递归显示

- `mkdir`常用参数
- `p`创建多级目录
    -  



## 用户相关

`useradd`用来建立用户帐号，最常用的参数是`-g`在创建用户的同时制定用户所属组

创建完用户可以使用`id`命令判断用户是否存在，例如`id roberto`

在不指定额外参数情况下默认会在`/home`下创建用户的家目录，在`/etc/password`和`/etc/shadow`产生用户配置

关于`/etc/password`和`/etc/shadow`配置文件每个字段的含义可以自行百度【需要了解掌握】

在建立完用户之后可以使用`passwd`修改用户密码，使用`userdel`【`-r`参数删除用户同时删除家目录】删除用户

`usermod`可以修改一个用户账户相关信息(如家目录、用户组等)，`chage`可以设定用户有效期相关参数

用户组相关操作可以使用`groupadd`添加用户组、`groupdel`删除用户组，关联的配置文件`/etc/group`

在对用户进行切换的时候可以使用`su - username`进行切换，下面是关于`su 和 su -`去区别

> `su`只会切换用户，但是当前的环境变量还是以前用户的环境变量
>
> `su -`会切换用户，也会把用户变量也切换到对应用户的环境变量

`sudo`暂时切换到超级用户模式以执行超级用户权限，提示输入密码时该密码为当前用户密码而不是超级账户密码

>`sudo -s`不加载用户变量，不跳转目录
>
>`sudo -i`加载用户变量，并跳转到目标用户`home`目录

`sudo`执行命令相关的可参考博客[sudo配置文件/etc/sudoers详解及实战用法](https://blog.csdn.net/Field_Yang/article/details/51547804)，作为`Java`开发很少会主动去修改

`last`可以查看用户登陆历史记录，方便排查问题【主要是跟踪误操作责任人】

## 文件权限

![image-20200301131206152](images/作为Java开发应该要掌握的Linux命令/image-20200301131206152.png)

至于文件权限每个字段的含义啥的这里就不展开说明了，如果不会的可自行百度一下

关于文件类型`Linux`定义的其中，我们记住常见的`-`代表普通文件、`d`代表该文件是个文件夹即可

对于文件而言`rwx`分别代表读、写、执行权限，而针对目录而言`rwx`分别代表是否可以显示目录下文件、是否可以对目录下文件名进行修改、是否可以进入文件夹。文件权限相关命令主要掌握`chmod`和`chown`命令即可

修改文件权限命令使用`chmod`，该命令有字母表示法和数字表示法。其中字母表示法`u g o a`分别代表属主、属组、其他用户、和所有用户，如我们需要给属主用户添加读权限可以使用命令`chmod u+r filename`。数字表示法`rwx`分别代表`421`，如果我们只希望给属主用户读权限可以使用命令`chmod 400 filename`。使用`chown`可以修改文件属主和属组信息，如使用`chown username filename`将文件属主赋给`username`用户，`chown :groupname filename`将文件属组赋给`groupname`组，可以使用`chown username:group filename`同时修改用户属主和属组

印象深刻的案例分析:

- 使用`Jenkins`上传`Jar`包到服务器指定目录`/usr/local/xxx`目录，该目录的权限是`drwxr-xr-x`

- 但是我在`Jenkins`那边配置的用户是属于该目录的`other`组用户对该目录没`w`权限的，但是上传成功
- 原因分析:因为早期是使用`root`权限进行安装包上传的，所以在`/usr/local/xxx`目录下已经有安装包文件，并且该文件的权限为`-rwxrwxrwx`，所以当使用其他用户进行安装包上传时是以现有安装包文件权限来判断的

**注意当发生权限冲突时以属主用户为准，如一个文件对应的属主用户同时属于该文件对应的属组，以属主权限为准**

`Linux`还提供了特殊的文件权限`SUID GUID BUID`，了解即可。反正作为`Java`开发的我平时没用到这些东西

## 进程管理

进程管理常用的三个命令【`ps pstree top`】，暂时列出开发中常用的命令后续有遇到新的再补充

```shell
ps –u [username] 列出指定用户相关进程信息
ps -ef | grep [keyword] 根据提供的关键字查找相关的进程信息

top是检查机器当前运行状况的第一个命令，就好比是机器体检时的第一张报告单，常见参数如下
-p显示指定的进程信息 -u显示指定用户的进程 -d一般默认是3秒钟数据更新一次，这个可以更改多长时间更新一次 -n 表示数据更新多少次后就结束命令的执行，默认是不结束
在使用top命令后输入1可展开查看多核CPU每个CPU的运行状态，关于top命令输出的结果含义可以百度一下不具体介绍
```

### 守护进程相关

[阮一峰的Linux 守护进程的启动方法](http://www.ruanyifeng.com/blog/2016/02/linux-daemon.html)

在`Centos7`中使用`Systemd`来关系系统的启动，关于`Systemd`可参考

[阮一峰的Systemd 入门教程:命令篇](http://www.ruanyifeng.com/blog/2016/03/systemd-tutorial-commands.html)和[阮一峰的Systemd 入门教程:实战篇](http://www.ruanyifeng.com/blog/2016/03/systemd-tutorial-part-two.html)

## 网络管理

作为一个开发人员大部分是不需要熟悉网络配置这部分内容的

因为从运维那拿到机器这部分功能已经都搞定了，`Java`开发我觉得掌握如下命令已经够日常故障排查使用了

`ping`测试网络到底通不通

`nslookup`查找域名对应的服务器地址

`telnet`在网络是通的情况下测试端口正不正常

`netstat`常用这个命令查看端口监听情况，及遇到`Address already in use`查看哪个进程占用了端口，常用参数

```shell
- a:显示所有连线中的Socket
- l:显示所有状态为Listen的连接
- n:直接使用ip地址，而不通过域名服务器
- t:显示TCP传输协议的连线状况
- p:显示正在使用Socket的程序识别码和程序名称

# 示例
# 查看当前正在监听的服务端口
netstat -ntlp

# 找出运行在指定端口的进程
netstat -anp | grep ':80'
```

## 软件包管理

`Yum`是一个`Shell`前端软件包管理器。基于`RPM`包管理能够从指定的服务器自动下载`RPM`包并且安装，可以自动处理依赖性关系，并且一次安装所有依赖的软件包。【使用国内`Yum`镜像源配置可参考:[阿里巴巴开源镜像站](https://developer.aliyun.com/mirror/centos?spm=a2c6h.13651102.0.0.3e221b11htIpC3)】

## 服务器字体安装

- `yum install -y fontconfig mkfontscale`
- 查看当前系统已安装的字体库`fc-list`
- 创建字体库目录`mkdir -p /usr/share/fonts/chinese`
- 拷贝字体文件至创建的字体库目录`cp SIMSUN.TTC /usr/share/fonts/chinese`
- 更新缓存使生效`fc-cache -fv`【关于字体可在网上下载到】

## 命令行小技巧

`CTRL+R`搜索执行过的历史命令

`du -a /var | sort -n -r | head -n 10`