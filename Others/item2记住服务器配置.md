# Item2保存服务器信息



## 安装expect

```shell
brew install expect
```

**如果遇到 man 目录的权限问题可以执行以下命令后在执行安装命令**

```shell
sudo chown -R $(whoami) /usr/local/share/man/man5
```

## 创建except脚本

```shell
sudo vim /usr/local/bin/iterm2login.sh

#!/usr/bin/expect

set timeout 30
spawn ssh -p [lindex $argv 0] [lindex $argv 1]@[lindex $argv 2]
expect {
        "(yes/no)?"
        {send "yes\n";exp_continue}
        "password:"
        {send "[lindex $argv 3]\n"}
}
interact
```

这里`[lindex $argv 0 $argv 1 $argv 2 $argv 3]`分别代表着 **端口号 用户名 服务器地址 密码** 4个参数

还需要给脚本执行权限

```shell
sudo chmod +x /usr/local/bin/iterm2login.sh
```

## 新建 profiles

`iTerm2 -> Preferences -> Profiles`为每个服务器的连接，打上不同的`Tag`就自动按`Tag`分好组了

<div  align="center">    
    <img src="https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Others/images/item2%E8%AE%B0%E4%BD%8F%E5%AF%86%E7%A0%81%E9%85%8D%E7%BD%AE.jpg" alt="item2记住密码配置" align=center />
</div>

另外还可以在`Colors`中设置每个打开`Tab`的颜色，多个项目同时操作也不怕搞错了

<div  align="center">    
    <img src="https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Others/images/item2%E8%AE%B0%E4%BD%8F%E5%AF%86%E7%A0%81%E9%85%8D%E7%BD%AE2.jpg" alt="item2记住密码配置2" align=center />
</div>

<div  align="center">    
    <img src="https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Others/images/item2%E8%AE%B0%E4%BD%8F%E5%AF%86%E7%A0%81%E9%85%8D%E7%BD%AE3.jpg" alt="item2记住密码配置3" align=center />
</div>

## 最终效果

如图，使用起来方便多了

<div  align="center">    
    <img src="https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Others/images/item2%E8%AE%B0%E4%BD%8F%E5%AF%86%E7%A0%81%E9%85%8D%E7%BD%AE4.jpg" alt="item2记住密码配置4" align=center />
</div>

## 保持回话

编辑或创建`~/.ssh/config`

```shell
Host *
ServerAliveCountMax 3
ServerAliveInterval 300
# 3 是最多发送的次数，如果想一直保持连接，本项目可以不写
# 300 是发给服务端心跳的间隔，单位是秒，根据你自己的服务器情况设置
```

