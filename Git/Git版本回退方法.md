# 版本回退方法

> 开发过程中，你肯定会遇到这样的场景
>
> - 把不想要的代码`commit`到本地仓库中了，但是还没有做`push`操作
> - 彻底完了，刚线上更新的代码出现问题了需要还原这次提交的代码
> - 刚才我发现之前的某次提交太愚蠢了，现在想要干掉它

## 本地分支版本回退的方法

如果你在本地做了错误提交，那么回退版本的方法很简单

- `git reflog`找到要回退的版本的`commit id`
- 回退版本`git reset --hard <commit id>`

## 自己的远程分支版本回退的方法

如果你的错误提交已经推送到自己的远程分支了，那么就需要回滚远程分支了

- 回退本地分支

  ```reStructuredText
  git reflog
  git reset --hard <commit id>
  ```

- 紧接着强制推送到远程分支

  ```reStructuredText
  git push -f
  ```

## 公共远程分支版本回退的问题

相信你已经能够回滚远程分支的版本了，那么你也许会问了，回滚公共远程分支和回滚自己的远程分支有区别吗？ 

```reStructuredText
一个显而易见的问题：如果你回退公共远程分支，把别人的提交给丢掉了怎么办？
```

假如你的远程master分支情况是这样的:`A1–A2–B1`，其中`A`、`B`分别代表两个人，`A1`、`A2`、`B1`代表各自的提交。并且所有人的本地分支都已经更新到最新版本，和远程分支一致。这个时候你发现`A2`这次提交有错误，你用`reset`回滚远程分支`master`到`A1`，那么理想状态是你的队友一拉代码`git pull`，他们的`master`分支也回滚了，然而现实却是，你的队友会看到下面的提示：

```text
$ git status
On branch master
Your branch is ahead of 'origin/master' by 2 commits.
  (use "git push" to publish your local commits)
nothing to commit, working directory clean
```

也就是说你的队友的分支并没有主动回退，而是比远程分支超前了两次提交，因为远程分支回退了嘛

- 理智的队友

  这个时候你大吼一声:兄弟们老子回退版本了。如果你的队友都是神之队友比如:`Tony`(腾讯`CTO`)，那么`Tony`会冷静的使用下面的命令来找出你回退版本后覆盖掉的他的提交，也就是`B1`那次提交，然后冷静的把自己的分支回退到那次提交，并且拉个分支

  ```reStructuredText
  git checkout tony_branch        // 先回到自己的分支  
  git reflog                      // 接着看看当前的commit id,例如:0bbbbb    
  git reset --hard B1             // 回到被覆盖的那次提交B1
  git checkout -b tony_backup     // 拉个分支，用于保存之前因为回退版本被覆盖掉的提交B1
  git checkout tony_branch        // 拉完分支，迅速回到自己分支
  git reset --hard 0bbbbbb        // 马上回到自己分支的最前端
  ```

  通过上面一通敲`Tony`暂时舒了一口气，还好`B1`那次提交找回来了。这时`tony_backup`分支最新的一次提交就是`B1`，接着`Tony`要把自己的本地`master`分支和远程`master`分支保持一致

  ```reStructuredText
  git reset --hard origin/master
  ```

  执行了上面这条命令后，`Tony`的`master`分支才真正的回滚了，也就是说你的回滚操作才能对`Tony`生效，这个时候`Tony`的本地`maser`是这样的:`A1`，接着`Tony`要再次合并那个被丢掉的`B1`提交

  ```text
  git checkout master             // 切换到master
  git merge tony_backup           // 再合并一次带有B1的分支到master
  ```

  好了，`Tony`终于长舒一口气，这个时候他的`master`分支是这样的:`A1 – B1`，终于把丢掉的`B1`给找回来了，接着`push`然后你拉取后也能同步。同理对于所有的队友也要这么做，但是如果该队友没有提交被你丢掉，那么他拉完代码`git pull`之后，只需要强制用远程`master`覆盖掉本地`master`就可以了

  ```reStructuredText
  git reset --hard origin/master
  ```

- 不理智的队友

  然而很不幸的是，现实中我们经常遇到的都是猪一样的队友，他们一看到下面提示

  ```reStructuredText
  $ git status
  On branch master
  Your branch is ahead of 'origin/master' by 2 commits.
    (use "git push" to publish your local commits)
  nothing to commit, working directory clean
  ```

  就习惯性的`git push`一下，或者他们直接用的`SourceTree`这样的图形界面工具，一看到界面上显示的是推送的提示就直接点了推送按钮，你辛辛苦苦回滚的版本就这样轻松的被你猪一样的队友给还原了，所以只要有一个队友`push`之后，远程`master`又变成了：`A1 – A2 – B1`，这就是分布式每个人都有副本。这个时候你连揍他的心都有了，怎么办呢？你不能指望每个人队友都是`git`高手，下面我们用另外一种方法来回退版本

## 公共远程分支版本回退的方法

使用`git reset`回退公共远程分支的版本后，需要其他所有人手动用远程`master`分支覆盖本地`master`分支，显然这不是优雅的回退方法，下面我们使用另个一个命令来回退版本

```reStructuredText
git revert HEAD                     // 撤销最近一次提交
git revert HEAD~1                   // 撤销上上次的提交，注意:数字从0开始
git revert <commit id>              // 撤销指定commit id这次提交
```

`git revert`命令意思是撤销某次提交。它会产生一个新的提交，虽然代码回退了但是版本依然是向前的，所以当你用`revert`回退之后，所有人`pull`之后，他们的代码也自动的回退了

## 附录

### Head

> 在`Git`中`Head`是一直指向当前分支的最新的`commit`，即使当前分支是一个游离的分支

### Git Reset

> 为了理解好`Git Reset`需要先对`Git`三棵树的概念有所了解，关于三棵树的概念可执行百度查阅

- `git reset [ –soft | –mixed | –hard] [commit id] [--] [file]`版本回退

  ```reStructuredText
  soft:将仓库的修改丢弃
  mixed:将仓库和暂存区的修改丢弃
  hard:将仓库、暂存区和工作区的修改都丢弃
  
  如果<commit id>未指定默认为Head
  如果[ –soft | –mixed | –hard]未指定默认为–mixed
  ```

  - `git reset --soft`

    ![git reset --soft](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Git/images/git%20reset%20--soft.png)

  - `git reset --mixed`

    ![git reset --mixed](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Git/images/git%20reset%20--mixed.png)

  - `git reset --hard`

    ![git reset --hard](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Git/images/git%20reset%20--hard.png)

- `git reset`常见使用场景【取消暂存、版本回退、合并多个提交】