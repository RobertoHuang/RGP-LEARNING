# GIT冲突解决

## Beyond Compare视图

|  名字  |                  含义                  |
| :----: | :------------------------------------: |
|  BASE  | 需要合并的两个文件的最近的共同祖先版本 |
| LOCAL  |          需合并的分支上的文件          |
| Remote |  从服务器将上一次提交拉取到本地的文件  |

最下方为合并之后的文件输出区域

## Mac配置Git mergetool & difftool

- 打开`Beyond Compare`安装命令行工具

- 修改`git`配置，使用`beyond compare`作为合并冲突工具

  ```
  # difftool配置
  git config --global diff.tool bc
  
  # mergetool配置
  git config --global merge.tool bc
  
  # 让git mergetool不再生成配置文件(*.orig)
  git config --global mergetool.keepBackup false
  
  # 解决冲突使用git mergetool
  
  # 差异比对工具git difftool
  ```

- `Source Tree`的`diff`选项中可视化对比工具和合并工具选择`Beyond Compare`

## Mac Beyond Compare破解

- 首先下载`Beyond Compare`最新版本，链接如下[下载地址。](https://www.scootersoftware.com/download.php)下载完成后，直接安装

- 进入`Mac`应用程序目录下，找到刚刚安装好的`Beyond Compare`，路径如下`/Applications/Beyond Compare.app/Contents/MacOS`，修改启动程序文件`BCompare`为`BCompare.real`

- 在当前目录下新建一个文件`BCompare`，文件内容如下

  ```
  #!/bin/bash
  
  rm "/Users/$(whoami)/Library/Application Support/Beyond Compare/registry.dat"
  
  "`dirname "$0"`"/BCompare.real $@
  ```

- 修改文件的权限

  ```
  chmod a+x /Applications/Beyond\ Compare.app/Contents/MacOS/BCompare
  ```

- 以上步骤完成后再次打开`Beyond Compare`就可以正常使用了