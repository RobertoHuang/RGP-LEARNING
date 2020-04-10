# Mac环境下安装Docer

手动下载安装，点击以下链接下载[Stable](https://download.docker.com/mac/stable/Docker.dmg)或[Edge](https://download.docker.com/mac/edge/Docker.dmg)版本的`Docker Desktop for Mac`，与安装其他软件无异

## 设置镜像加速

对于使用`MacOS`的用户，在任务栏点击`Docker Desktop`应用图标 -> `Perferences`，在左侧导航菜单选择`Docker Engine`，在右侧像下边一样编辑`JSON`文件。修改完成之后点击 `Apply & Restart` 按钮，`Docker` 就会重启并应用配置的镜像地址了

```json
{
  "registry-mirrors": [
    "https://hub-mirror.c.163.com"
  ]
}
```

检查加速器是否生效，执行`$ docker info`，如果从结果中看到了如下内容，说明配置成功

```bash
Registry Mirrors:
 https://hub-mirror.c.163.com/
```

