# Docker一键安装常用软件

该文档安装的脚本只可用于本地测试使用

- `Zookeeper`

    ```shell
    docker run --name docker-zookeeper -p 2181:2181 --restart always -d zookeeper:3.4.1
    ```

- `MySQL`

    ```shell
    docker run --name docker-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql
    ```

    

