# GIT冲突解决

## Beyond Compare视图

|  名字  |                  含义                  |
| :----: | :------------------------------------: |
|  BASE  | 需要合并的两个文件的最近的共同祖先版本 |
| LOCAL  |          需合并的分支上的文件          |
| Remote |  从服务器将上一次提交拉取到本地的文件  |

最下方为合并之后的文件输出区域

## 配置Git mergetool & difftool

```shell
# difftool配置
git config --global diff.tool bc3
git config --global difftool.bc3.path "D:\Develop\BCompare\Beyond Compare 4\BComp.exe"

# mergetool配置
git config --global merge.tool bc3
git config --global mergetool.bc3.path "D:\Develop\BCompare\Beyond Compare 4\BComp.exe"

# 让git mergetool不再生成配置文件(*.orig)
git config --global mergetool.keepBackup false

# 解决冲突使用git mergetool
```

## 附录

- `beyond compare 4 This license key has been revoked 解决办法`

  删除以下目录中的所有文件，然后重新输入密钥激活就能够重新正常使用了

  ```reStructuredText
  C:\Users\Administrator\AppData\Roaming\Scooter Software\Beyond Compare 4
  ```

  
