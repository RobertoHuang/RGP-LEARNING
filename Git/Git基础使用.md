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
  git config [--local | --global | --system] user.name 'taihong.huang'
  git config [--local | --global | --system] user.email 'robertohuang@foxmail.com'
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
git clone <repository url> [folder name] 从远程仓库拉取

git add <file> 将文件添加到暂存区
git reset HEAD <file> 将之前添加到暂存区的内容从暂存区移除到工作区
git checkout -- <file> 丢弃掉相对于暂存区中最后一次添加的文件内容所做的变更

git commit -m <message> 提交暂存区修改
git commit --amend -m <message> 修改最后一次提交【可以继续添加文件到最后一次的commit，如果不想修改提交信息可使用git commit --amend --no-edit】

git rm --cache <file> 当我们需要删除暂存区或分支上的文件但本地又需要使用,只是不希望这个文件被版本控制
git log [-n] [--oneline] [--graph] 以图形化方式展示提交历史记录，查看指定文件的提交记录可使用git log [filename]
git blame <filename> 查看文件每个区块修改责任人

git pull 拉取远程分支修改应用到本地
git push 将本地分支修改推送到远程分支
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
  在用户目录下面创建一个文件.gitignore，其实路径和文件名可以自选。配置变量core.excludesFile
  ```

  适用于你想某个规则对所有仓库都生效并且不需要共享给其他用户，则可以配置变量`core.excludesFile`
  
- 如果不知道自己所用的工具或语言通常会忽略哪些文件可参考[GitHub整理的.gitignore文件](https://github.com/github/gitignore)

忽略配置只针对未被追踪文件生效，如果文件已被追踪需先使用命令`git rm --cache <file>`取消追踪才可生效

## 分支管理相关命令

```
# 更新本地remote分支信息
git remote update origin -p
```

```reStructuredText
git branch <branch ame> [commit id] 创建分支
git branch -av 显示所有分支 v:显示提交信息
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
git reset --hard HEAD~n 回退到前N个版
git reset --hard <commit id> 回退到指定提交

git reflog 显示所有提交过的版本信.更多关于版本回退内参考同目录下另一篇博客
在进行危险操作的时候Git会创建一个安全点，用于恢复到危险操作之前的状态:git reset ORIG_HEAD --hard
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
git show <tag name> 查看标签详细信息

git push origin <srcTagName>:<dstTagName> 推送标签到远程
git push origin :refs/tags/<tagName> 删除远程标签

注:标签名称不可与分支名称重复
```

## 查看某个远程仓库的详细信息

```
# 查看某个远程仓库的详细信息
git remote show [remote-name]
```

## fetch与refspecs

假设我们用命令添加了一个远程版本库

```
git remote add origin git@github.com:RobertoHuang/test.git
```

【**`origin`是`git`仓库的别名，可指定其它名称。只是约定俗成一般不建议修改**】上述命令会在 `.git/config` 文件中添加几行并在其中指定远程版本库名称`origin`、`URL`和一个用于获取`fetch`操作的引用规格`refspec`

```
[remote "origin"]
	url = git@github.com:RobertoHuang/test.git
	fetch = +refs/heads/*:refs/remotes/origin/*
```

第1行:表示远程仓库的简称是`origin`

第2行:指明远程仓库的`URL`

第3行:引用规格的格式由一个可选的`+`号和紧随其后的`<src>:<dst>`组成。其中`<src>`是一个模式`pattern`，代表远程版本库中的引用；`<dst>`是远程版本库的引用在本地所对应的位置；开头的`+`号告诉`Git`即使在不能快进的情况下也要强制更新引用

默认情况下引用规格由 `git remote add` 命令自动生成【无特殊情况不要进行修改】

`Git`会获取服务器中`refs/heads/`下面的**所有**引用，并将它写入到本地的`refs/remotes/origin/`中

所以如果远程有一个`master`分支，在本地可以通过下面的这种方式来访问他的记录

```
git log origin/master
git log remotes/origin/master
git log refs/remotes/origin/master
```

它们全是等价的，因为`git`会把它们都扩展成`resf/remotes/origin/master`

如果你想让`git`每次拉取只拉取远程的`master`，而不是远程所有分支，你可以把`fetch`这一行修改成这样

```
fetch = +refs/heads/master:refs/remotes/origin/master
```

`git fetch`操作默认`fech`远端名称为`origin`，而如果你只想做一次该操作，也可以在命令行指定这个`refspecs`

```
git fetch origin +refs/heads/master:refs/remotes/origin/master
```

关于`refspecs`大概就介绍到这里，个人理解它主要用于**绑定远程分支和本地远程分支**的对应关系。[参考链接](https://blog.csdn.net/longintchar/article/details/84480862)

## 远程协作

- 设置本地分支和远程分支关联关系

  ```
  git push --set-upstream origin <src>:<dst> src:本地分支 dst:远程分支
  git push -u origin <src>:<dst> src:本地分支 dst:远程分支 这两个命令是等价的
  
  强烈建议src和dst分支名称相同
  ```

- 推送命令

  ```
  git push origin <src>:<dst> src:本地分支 dst:远程分支
  
  利用该命令可以删除远程分支，推送空分支到远程(如下删除master2分支)
  git push origin :master2
  
  删除远程分支还可以使用如下命令(删除master3分支)
  git push origin --delete master3
  ```

- 拉取命令

  ```
  git pull origin <src>:<dst> src:远程分支 dst:本地分支 如果是合并到当前分支则可以省略dst
  
  该命令其实可以拆分成如下命令:
      1.拉取远程分支与本地的远程分支建立关系
      git fetch origin +refs/heads/<src>
      
      2.如果本地分支不存在，则新建并切换到本地分支
      git checkout -b <dst> origin/<src>或者git checkout --track origin/<src>
      
      3.将远程分支改动合并到本地分支
      git merge origin/<src>
  ```

- 删除本地远程分支(本地远程分支对应的远程分支已删除时)

  ```
  git remote prune origin
  ```

## cherry-pick

`cherry-pick`主要应用在本地分支上，主要用户解决本来需要添加到`A`分支的代码误添加到了`B`分支上的问题

```
git cherry-pick [commit-id] --no-commit
```

## merge和rebase

加入有两个分支分别为`A>B1>B2`和`A>C1>C2`，使用变基将`B`的修改合并到`C`分支上操作为

```
git checkout C && git rebase B
```

该操作完成的功能是将`C`分支的改动在`B`分支上进行重播。即执行完上述命令后分支为`A>B1>B2>C1>C2`

- `rebase`和`merge`的区别

  `merge`历史一旦发生即存在事实 `rebase`着眼于现在

  从某种程度上来说`rebase`与`merge`可以完成类似的工作，不过两者的工作方式有着显著的区别

  `rebase`会修改提交历史，一般来说执行`rebase`的分支都是自己的本地分支，没有推送到远程版本库

  不要与其他人共享的分支上执行`rebase`操作【切勿在`master`分支上执行`rebase`，否则会引起很多问题】

- `rebase`过程冲突合并

  - `rebase`过程中也会出现冲突
  - 可以使用`git rebase --skip`跳过重播
  - 解决冲突后使用`git add`添加然后执行`git rebase --continue`
  - 接下来`git`会继续应用余下的补丁【查看剩余的补丁命令为`git rebase --edit-todo`】
  - 任何时候都可以通过如下命令终止`rebase`，分支会恢复到`rebase`开始前状态`git rebase --abort`

- 使用交互式变基来合并多次提交`git rebase -i <arg>`或者`git rebase -i HEAD~ `

  `-i`后参数为提交对象`ID`比较特殊的一个参数是`origin/master`，使用`git rebase -i origin/master`，可以获取最后一次从`origin`远端仓库拉取`pull`或推送`push`之后的所有提交

关于`rebase`可以参考:[Git变基](https://git-scm.com/book/zh/v2/Git-%E5%88%86%E6%94%AF-%E5%8F%98%E5%9F%BA) 和 [Git由浅入深说变基](http://blog.codingplayboy.com/2017/04/14/git_rebase/)

## 附录

- `GitBash`中文乱码问题解决

  `打开GitBash -> 窗口右键 -> Options -> Text`

  `将Locale(改成zh_CN) Character set改为GBK，退出再打开GitBash即可`

- `Git Status`中文乱码问题解决，添加如下配置

  ```reStructuredText
  git config --global core.quotepath false
  ```

  