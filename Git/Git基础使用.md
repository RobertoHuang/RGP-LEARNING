# Git

- 官方中文文档:[https://www.git-scm.com/book/zh/v2](https://www.git-scm.com/book/zh/v2)

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
git add <file> 将文件添加到暂存区
git commit -m <message> 提交暂存区修改
git reset HEAD <file> 将之前添加到暂存区的内容从暂存区移除到工作区
git checkout -- <file> 丢弃掉相对于暂存区中最后一次添加的文件内容所做的变更
git commit --amend -m <message> 修改最后一次提交信息
git log [-n] [--oneline] [--graph] 以图形化方式展示提交历史记录
```

## .gitignore文件

关于`.gitignore`配置文件是用于不需要加入版本管理的文件

配置好该文件可以为我们的版本管理带来很大的便利，关于`.gitignore`文件配置规则可执行查阅资料获取

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

