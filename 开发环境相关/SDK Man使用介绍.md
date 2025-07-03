- 安装
  ```java
  curl -s "https://get.sdkman.io" | bash
  ```
  默认下载的文件在/Users/(用户名)/.sdkman/candidates目录下
  
- 列出可用版本
  ```java
  sdk list [candidates]
  如需要查看可用的JDK版本: sdk list java
  ```

- 安装指定版本
  ```java
  sdk install [candidates] [version]
  如安装jdk: sdk install java 8.0.452-zulu 
  ```

- 要将给定的版本设置为默认版本
  ```java
  sdk default [candidates] [version]
  如设置jdk8为默认jdk版本: sdk default java 8.0.452-zulu
  ```

- 要在当前终端会话中使用给定的版本
  ```java
  sdk use [candidates] [version]
  如设置当前窗口使用jdk8: sdk use java 8.0.452-zulu
  ```

- 卸载某个版本
  ```java
  sdk uninstall [candidates] [version]
  如如果需要卸载jdk8: sdk uninstall java 8.0.452-zulu
  ```
