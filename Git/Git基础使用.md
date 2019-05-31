# Git

- 官方中文文档:[https://www.git-scm.com/book/zh/v2](https://www.git-scm.com/book/zh/v2)

## 配置管理

- 添加配置

  ```text
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
git reset HEAD <file> 将文件从暂存区恢复到工作区
git checkout -- <file> 将文件在工作区中的修改丢弃
git commit --amend -m <message> 修改最后一次提交信息
git log [-n] [--oneline] [--graph] 以图形化方式展示提交历史记录
```

