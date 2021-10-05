# 使用Vagrant和Virtualbox构建开发环境

> `Vagrant`是一款用来构建虚拟开发环境的工具，它其实算是一个跨平台的虚拟机管理工具

- `MacOS`下安装`Vagrant`

    下载地址[https://www.vagrantup.com/downloads](https://www.vagrantup.com/downloads)

- 编写一个`Vagrantfile`文件，以下是个安装`Centos`的示例

    ```
    # -*- mode: ruby -*-
    # vi: set ft=ruby :
    
    # All Vagrant configuration is done below. The "2" in Vagrant.configure
    # configures the configuration version (we support older styles for
    # backwards compatibility). Please don't change it unless you know what
    # you're doing.
    Vagrant.configure("2") do |config|
      # The most common configuration options are documented and commented below.
      # For a complete reference, please see the online documentation at
      # https://docs.vagrantup.com.
    
      # Every Vagrant development environment requires a box. You can search for
      # boxes at https://vagrantcloud.com/search.
      # Vagrant的基础镜像，相当于docker images。可以在这些基础镜像的基础上制作自己的虚拟机镜像 centos/7的默认密码为vagrant
      config.vm.box = "centos/7"
    
      # Disable automatic box update checking. If you disable this, then
      # boxes will only be checked for updates when the user runs
      # `vagrant box outdated`. This is not recommended.
      # 如果设为true则Vagrant会在每次执行vagrant up时检查每个box的更新情况，如果发现更新Vagrant会告诉用户
      config.vm.box_check_update = false
    
      # Create a forwarded port mapping which allows access to a specific port
      # within the machine from a port on the host machine. In the example below,
      # accessing "localhost:8080" will access port 80 on the guest machine.
      # 端口映射:把宿主计算机的端口映射到虚拟机的某一个端口上，访问宿主计算机端口时，请求实际是被转发到虚拟机上指定端口的
      # 默认只转发TCP包，UDP需要额外添加以下语句  protocol: "udp"。十分容易实现外网访问虚拟机。不支持在宿主机器上使用小于1024的端口来转发
      # config.vm.network "forwarded_port", guest: 80, host: 8080
    
      # Create a private network, which allows host-only access to the machine
      # using a specific IP.
      # 只有主机可以访问虚拟机，如果多个虚拟机设定在同一个网段也可以互相访问，当然虚拟机是可以访问外部网络的
      config.vm.network "private_network", ip: "10.4.7.11"
    
      # Create a public network, which generally matched to bridged network.
      # Bridged networks make the machine appear as another physical device on
      # your network.
      # 虚拟机享受实体机器一样的待遇一样的网络配置，vagrant1.3版本之后也可以设定静态IP。设定语法如下config.vm.network "public_network", ip: "192.168.1.120"
      # 公有网络中还可以设置桥接的网卡，语法如下config.vm.network "public_network", :bridge => 'en1: Wi-Fi (AirPort)'。方便团队协作，别人可以访问你的虚拟机
      # config.vm.network "public_network"
    
      # Share an additional folder to the guest VM. The first argument is
      # the path on the host to the actual folder. The second argument is
      # the path on the guest to mount the folder. And the optional third
      # argument is a set of non-required options.
      # vagrant up后在虚拟机中会有一个/vagrant目录，这跟你定义Vagrantfile是同一级目录
      # config.vm.synced_folder
      #      "your_folder"(必须),
      #      // 挂载到虚拟机上的目录地址(必须是绝对路径)
      #      "vm_folder(必须)",
      #      create(boolean),                                    --可选   // 默认为false，若配置为true，挂载到虚拟机上的目录若不存在则自动创建
      #      disabled(boolean),                                  --可选   // 默认为false，若为true, 则禁用该项挂载
      #      owner(string):'www',                                --可选   // 虚拟机系统下文件所有者(确保系统下有该用户，否则会报错)，默认为vagrant
      #      group(string):'www',                                --可选   // 虚拟机系统下文件所有组( (确保系统下有该用户组，否则会报错)，默认为vagrant
      #      mount_options(array):["dmode=775","fmode=664"],     --可选   // dmode配置目录权限，fmode配置文件权限，默认权限777
      #      type(string):                                       --可选   // 指定文件共享方式，例如：'nfs'，vagrant默认根据系统环境选择最佳的文件共享方式
      # config.vm.synced_folder "../data", "/vagrant_data"
    
      # Provider-specific configuration so you can fine-tune various
      # backing providers for Vagrant. These expose provider-specific options.
      # Example for VirtualBox:
      #
      config.vm.provider "virtualbox" do |vb|
        # Display the VirtualBox GUI when booting the machine
        # vb.gui = true
        # 指定vm-name，也就是virtualbox管理控制台中的虚机名称   
        vb.name = "roberto-1"
        # Customize the amount of memory on the VM:
        # 指定vm内存，单位为MB
        vb.memory = "2048"
        # 指定CPU核数
        vb.cpus = 2
      end
      
      # View the documentation for the provider you are using for more
      # information on available options.
    
      # Define a Vagrant Push strategy for pushing to Atlas. Other push strategies
      # such as FTP and Heroku are also available. See the documentation at
      # https://docs.vagrantup.com/v2/push/atlas.html for more information.
      # config.push.define "atlas" do |push|
      #   push.app = "YOUR_ATLAS_USERNAME/YOUR_APPLICATION_NAME"
      # end
    
      # Enable provisioning with a shell script. Additional provisioners such as
      # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
      # documentation for more information about their specific syntax and use.
      # config.vm.provision "shell", inline: <<-SHELL
      #   apt-get update
      #   apt-get install -y apache2
      # SHELL
      # 在机器上配置provisioner，以便在创建机器时自动安装和配置软件
      # 以下配置是因为Centos7默认没提供用户名密码登陆方式，所以通过命令进行修改
      config.vm.provision "shell", inline: <<-SHELL
        whoami | cat > /whoami.txt
        sudo sed -i 's/PasswordAuthentication no/PasswordAuthentication yes/g' /etc/ssh/sshd_config
        sudo systemctl restart sshd
      SHELL
    end
    ```

- 关于`vagrant`使用命令可以使用`vagrant --help`进行查看

