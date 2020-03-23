# NodeJS

- 下载`NodeJS`最新`bin`包

  可以在下载页面https://nodejs.org/en/download/中找到下载地址

  ```shell
  wget https://nodejs.org/dist/v10.16.3/node-v10.16.3-linux-x64.tar.xz
  ```

- 解压包

  ```shell
  xz -d node-v10.16.3-linux-x64.tar.xz
  tar -xf node-v10.16.3-linux-x64.tar
  ```

- 部署`bin`文件

  ```shell
  ln -s /usr/local/node/node-v10.16.3-linux-x64/bin/node /usr/bin/node
  ln -s /usr/local/node/node-v10.16.3-linux-x64/bin/npm /usr/bin/npm
  ln -s /usr/local/node/node-v10.16.3-linux-x64/bin/npm /usr/bin/npx
  ```

  注意`ln`指令用于创建关联(类似与`Windows`的快捷方式)必须给全路径，否则可能关联错误

- 使用如下命令测试是否安装成功 

  ```shell
  node -v | npm | npx
  ```
  
  

