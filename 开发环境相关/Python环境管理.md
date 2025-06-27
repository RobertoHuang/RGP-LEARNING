在日常工作中可能会遇到需要在一个机器中同时有多个版本的Python的诉求

解决办法:

- 安装pyenv

  ```
  brew install pyenv
  ```

- 设置pyenv运行环境，这里指定了Pyenv的工作目录

  ```
  echo 'export PYENV_ROOT="$HOME/.pyenv"' >> ~/.zshrc
  echo '[[ -d $PYENV_ROOT/bin ]] && export PATH="$PYENV_ROOT/bin:$PATH"' >> ~/.zshrc
  echo 'eval "$(pyenv init - zsh)"' >> ~/.zshrc
  ```

  source ~/.zshrc让配置环境生效

- 查看当前系统安装了哪些版本的Python

  ```
  pyenv versions
  ```

- 列出所有可用的Python版本

  ```
  pyenv install -l
  ```

- 安装指定版本Python

  ```
  pyenv install xx.xx.xx (pyenv install 3.13.0)
  pyenv rehash (记得一定要rehash)
  ```

- 切换和使用指定的Python版本3种方式

  系统全局 用系统默认的Python比较好，不建议直接对其操作

  ```
  pyenv global system
  ```

  用local(文件夹级别)进行指定版本切换，一般开发环境使用

  ```
  pyenv local 3.13.0
  pyenv local --unset 取消某版本切换
  ```

  对当前用户的临时设定的Python版本，退出后失效

  ```
  pyenv shell 3.13.0
  ```

  优先级关系: shell > local > global