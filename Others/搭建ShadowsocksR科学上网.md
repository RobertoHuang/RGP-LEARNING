# 翻墙教程

科学上网`SSR`搭建教程，首先需要购买一款`VPS`，推荐大家使用

> https://www.vultr.com
>
> https://www.hostwinds.com

注册账号后可以自己选择一款符合需求的`VPS`按小时计费，不想用了或者`IP`被墙可以删掉重建，下次用了再重新搭建

- 部署实例

    在完善信息后对自己的账户进行充值

    ![image-20201116101113055](images/搭建ShadowsocksR科学上网/image-20201116101113055.png)

    充值完成后部署实例，选个中意的地区 > Centos7 > 带`IPV4`的每个月3.5刀部署起来

    <img src="images/搭建ShadowsocksR科学上网/image-20201116101556951.png"/>


## 脚本方式部署

```shell
yum -y install git
git clone -b master https://github.com/flyzy2005/ss-fly
ss-fly/ss-fly.sh -ssr 

# 设置好相关参数后进行安装
# 全部选择结束后，会看到如下界面，就说明搭建ssr成功了
Congratulations, ShadowsocksR server install completed!
Your Server IP        :  
Your Server Port      :  
Your Password         :  
Your Protocol         :  
Your obfs             :  
Your Encryption Method:  

Welcome to visit:https://shadowsocks.be/9.html
Enjoy it!

# 相关操作ssr命令
启动：/etc/init.d/shadowsocks start
停止：/etc/init.d/shadowsocks stop
重启：/etc/init.d/shadowsocks restart
状态：/etc/init.d/shadowsocks status
 
配置文件路径：/etc/shadowsocks.json
日志文件路径：/var/log/shadowsocks.log
代码安装目录：/usr/local/shadowsocks

# 卸载ssr服务
./shadowsocksR.sh uninstall

# 一键开启BBR加速，装完后需要重启系统，输入y即可立即重启，或者之后输入reboot命令重启
ss-fly/ss-fly.sh -bbr
# 判断BBR加速有没有开启成功。输入以下命令
sysctl net.ipv4.tcp_available_congestion_control
# 如果返回值为net.ipv4.tcp_available_congestion_control = bbr cubic reno后面有bbr，则说明已经开启成功了
```

更多关于SSR部署可以参考:https://github.com/flyzy2005/ss-fly。启动后使用客户端配置后即可科学上网了

## 科学上网配置

下文是介绍终端窗口如何进行科学上网(阅读以下内容建议建立在浏览器已经能科学上网的前提下)

配置`socks5`和`http`以及`https`全都代理

```
alias setproxy='export socks5_proxy=socks5://127.0.0.1:1086;
                export http_proxy=http://127.0.0.1:1087;
                export https_proxy=http://127.0.0.1:1087;
                git config --global http.proxy "socks5://127.0.0.1:1086"
                git config --global https.proxy "socks5://127.0.0.1:1086"'
                
alias unsetproxy='unset socks5_proxy http_proxy https_proxy
                  git config --global --unset http.proxy
                  git config --global --unset https.proxy'
```

如果使用`Shadowsocks`的话，上诉配置的地址查看界面分别是

![image-20210224111504485](images/搭建ShadowsocksR科学上网/image-20210224111504485.png)

这里`Socks5`地址我修改成`0.0.0.0`的原因是因为我的虚拟机也想共享宿主机的`Shadowsocks`配置

![image-20210224111527967](images/搭建ShadowsocksR科学上网/image-20210224111527967.png)

这里的`HTTP`地址修改为`0.0.0.0`的原因同上。通过上诉配置后那么`wget` `curl git clone` 这类命令都会经过代理

`Pac`规则配置默认地址为`https://raw.githubusercontent.com/gfwlist/gfwlist/master/gfwlist.txt`

