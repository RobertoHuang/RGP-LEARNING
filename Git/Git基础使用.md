# Git

> 官方中文文档:[https://www.git-scm.com/book/zh/v2](https://www.git-scm.com/book/zh/v2)
>
> `Git`是一个分布式版本控制系统，关于分布式和集中式版本控制系统的差异(略)
>
> `Git`和其他版本控制系统对待数据的差异:传统版本控制系统记录文件差异，`Git`记录文件快照

## 配置管理

- 添加配置

  ```reStructuredText
  git最小配置:
  git config --global user.name 'roberto.huang'
  git config --global user.email 'robertohuang@foxmail.com'
  ```

- 删除配置

  ```reStructuredText
  git config --unset [--local | --global | --system] user.name
  ```

- 查看配置

  ```reStructuredText
  git config --list [--local | --global | --system]
  ```

- `local:区域为本地仓库 global:当前用户的所有仓库 system:本系统的所有用户 local > global > system`

## 常用命令

```reStructuredText
git init <repository name> 初始化Git仓库

git add <file> 将文件添加到暂存区
git commit -m <message> 提交暂存区修改

git reset HEAD <file> 将之前添加到暂存区的内容从暂存区移除到工作区
git checkout -- <file> 丢弃掉相对于暂存区中最后一次添加的文件内容所做的变更

git commit --amend -m <message> 修改最后一次提交信息
git log [-n] [--oneline] [--graph] 以图形化方式展示提交历史记录
```

## 忽略某些文件

- `.gitignore`文件

  适用于如果一个`.gitignore`规则应该被`Git`追踪，或者希望别人`clone`仓库后这些规则也生效

- `.git/info/exclude`文件

  适用于只想规则在某一个仓库中生效，但是不需要共享给其他用户，则可以修改`.git/info/exclude`文件

- `core.excludesFile`环境变量

  ```reStructuredText
  $ touch ~/.gitignore 
  $ git config --global core.excludesFile ~/.gitignore 
  在用户目录下面创建一个文件`.gitignore`，其实路径和文件名可以自选。配置变量`core.excludesFile`
  ```

  适用于你想某个规则对所有仓库都生效并且不需要共享给其他用户，则可以配置变量`core.excludesFile`

## 分支管理相关命令

```reStructuredText
git branch <branch ame> [commit id] 创建分支
git branch -d <branch name> 删除分支
git branch -D <branch name> 强制删除分支
git branch -m <oldbranch> <newbranch> 修改分支名称

git checkout <commit id> 切换到指定提交
git checkout <branch name> 切换到指定分支
git checkout -b <branch name> 创建并切换到指定分支

git merge [--no-ff] <branch name> 将branch name分支合并到当前分支 

--no-ff表示禁用fast-faorward，合并后的历史有分支能看出曾经做过合并，而fast-forward看不出曾经做过合并
```

## 版本回退相关命令

```reStructuredText
git reset --hard HEAD^ 回退到之前的版本
git reset --hard HEAD~n 回退到前N个版本
git reset --hard <commit id> 回退到指定提交

git reflog 显示所有提交过的版本信息
```

## 贮藏修改相关命令

```reStructuredText
git stash save <message> 贮藏工作区修改
git stash list 获取贮藏区列表
git stash pop <stash> 恢复的同时也将stash内容删除
git stash apply <stash> 恢复的同时stash的内容不会删除
git stash drop <stash> 删除指定暂存内容
git stash clear 删除所有暂存内容
```

## 标签相关命令

```reStructuredText
git tag 获取标签列表
git tag <tag name> 创建标签
git tag -a <tag name> -m <remark> 创建一个带有附注的标签
git tag -d <tag name> 删除标签

注:标签名称不可与分支名称重复
```

## 比对相关命令

```reStructuredText
git diff 比较的是暂存区与工作区文件之间的差别
git diff HEAD 比较的是最新的提交与工作区之间的区别
git diff --cached 比较的是最新的提交与暂存区之间的差别
```

## 附录

- `GitBash`中文乱码问题解决

  `打开GitBash -> 窗口右键 -> Options -> Text`

  `将Locale(改成zh_CN) Character set改为GBK`，退出再打开`GitBash`即可

- `Git Status`中文乱码问题解决，添加如下配置

  ```reStructuredText
  git config --global core.quotepath false
  ```

  