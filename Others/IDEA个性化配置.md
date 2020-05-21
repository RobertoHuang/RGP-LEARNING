# IDEA个性化配置



## 插件篇

- 字节码查看插件`jclasslib bytecode viewer`

- 代码格式化插件`Safe Action`，[插件地址](https://github.com/dubreuia/intellij-plugin-save-actions)。推荐配置如下图

    ![image-20200312135908596](images/IDEA个性化配置/image-20200312135908596.png)

- 热部署插件`Jrebel`，至于怎么破解网上找一下一堆

    需要关闭`IDEA`自动编译功能，否则会导致`IDEA`运行卡顿

    ```
    Setting -> Build,Execution,Deployment -> Compiler -> Build project automatically
    ```

    当代码修改后使用`Ctrl + F9`进行手动编译，项目就会热加载

    

