- `Node.js`环境安装

  - 下载地址:https://nodejs.org/en/download/

  - 下载`Binaries `包并解压到指定文件夹下，修改环境变量

    ```shell
    export NODEJS_HOME=/opt/node/node-v10.15.1-linux-x64/bin
    export PATH=$NODEJS_HOME:$PATH
    ```

  - 测试是否已配置成功【`node -v`、`npm version`、`npx -v`】

## 附录

- 修改环境变量

  ```shell
  # vim /etc/profile
     ...
     
  #source /etc/profile
  ```

