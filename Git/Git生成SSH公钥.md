# SSH公钥

> 许多`Git`服务器都使用`SSH`公钥进行认证

## 生成SSH公钥

- 检查是否已存在公钥

  ```reStructuredText
  cd ~/.ssh
  ```

  能进去说明已经生成过公钥，如果需要重新生成就删掉该文件夹

- 使用命令生成`Git SSH`公钥

  ```reStructuredText
  ssh-keygen -t rsa -C “robertohuang@foxmail.com”
  ```

  连续按回车使用默认提供的配置，生成的密钥默认存放位置`~/.ssh`

## SourceTree配置SSH

- `工具 -> 选项 -> SSH客户端选择OpenSSH`

  `SSH`密钥这一栏自然会去选择当前用户下的`.ssh`目录下的`id_rsa`这个私钥，配置完成

