# Sigar

> `Sigar(System Information Gatherer And Reporter)`是一个开源的工具，提供了跨平台的系统信息收集的`API`，其核心由`C`语言实现的。可收集的信息包括`CPU信息`、文件系统信息、事件信息、内存信息、网络信息、进程信息、`IO`信息、服务状态信息、系统信息等

## 添加依赖

```java
<dependency>
    <groupId>org.fusesource</groupId>
    <artifactId>sigar</artifactId>
    <version>1.6.4</version>
</dependency>
```

## 下载本地库依赖

- 下载地址:[https://sourceforge.net/projects/sigar/files/sigar/1.6/hyperic-sigar-1.6.4.zip](https://sourceforge.net/projects/sigar/files/sigar/1.6/hyperic-sigar-1.6.4.zip)

- 解压后，拷贝`hyperic-sigar-1.6.4/sigar-bin/lib`文件夹 到`maven`工程的`resources`文件夹下

- 删除无用的文件 `.sigar_shellrc`、`log4j.jar`、`sigar.jar`

- 重命名 `lib` 文件夹为 `sigar`

- ```reStructuredText
  resources/
  └── sigar
      ├── libsigar-amd64-freebsd-6.so
      ├── libsigar-amd64-linux.so
      ├── libsigar-amd64-solaris.so
      ├── libsigar-ia64-hpux-11.sl
      ├── libsigar-ia64-linux.so
      ├── libsigar-pa-hpux-11.sl
      ├── libsigar-ppc-aix-5.so
      ├── libsigar-ppc-linux.so
      ├── libsigar-ppc64-aix-5.so
      ├── libsigar-ppc64-linux.so
      ├── libsigar-s390x-linux.so
      ├── libsigar-sparc-solaris.so
      ├── libsigar-sparc64-solaris.so
      ├── libsigar-universal-macosx.dylib
      ├── libsigar-universal64-macosx.dylib
      ├── libsigar-x86-freebsd-5.so
      ├── libsigar-x86-freebsd-6.so
      ├── libsigar-x86-linux.so
      ├── libsigar-x86-solaris.so
      ├── sigar-amd64-winnt.dll
      ├── sigar-x86-winnt.dll
      └── sigar-x86-winnt.lib
  
  1 directory, 23 files
  ```

## 初始化Sigar工具类

```java
public class SigarUtils {
    private static volatile Sigar sigar;

    static {
        // Linux MacOS分隔符: Windows是;
        String splitSymbol = System.getProperty("os.name", "generic").toLowerCase().contains("win") ? ";" : ":";

        URL sigarURL = SigarUtils.class.getResource("/sigar");
        if (null == sigarURL) {
            throw new MissingResourceException("miss classpath:/sigar folder", SigarUtils.class.getName(), "classpath:/sigar");
        } else {
            File classPath = new File(sigarURL.getFile());
            String oldLibPath = System.getProperty("java.library.path");
            try {
                // 追加库路径
                String newLibPath = oldLibPath + splitSymbol + classPath.getCanonicalPath();
                System.setProperty("java.library.path", newLibPath);
                log.info("set sigar java.library.path={}", newLibPath);
            } catch (IOException e) {
                log.error("append sigar to java.library.path error", e);
            }
        }
    }

    private SigarUtils() {

    }

    public static Sigar getInstance() {
        if (null == sigar) {
            synchronized (SigarUtils.class) {
                if (null == sigar) {
                    sigar = new Sigar();
                }
            }
        }
        return sigar;
    }
|
```

## Sigar支持的交互命令

启动方式: `java -jar hyperic-sigar-1.6.4/sigar-bin/lib/sigar.jar`

- ```
  sigar> help
  Available commands:
  	alias          - Create alias command
  	cpuinfo        - Display cpu information
  	df             - Report filesystem disk space usage
  	du             - Display usage for a directory recursively
  	free           - Display information about free and used memory
  	get            - Get system properties
  	help           - Gives help on shell commands
  	ifconfig       - Network interface information
  	iostat         - Report filesystem disk i/o
  	kill           - Send signal to a process
  	ls             - simple FileInfo test at the moment (like ls -l)
  	mps            - Show multi process status
  	netinfo        - Display network info
  	netstat        - Display network connections
  	nfsstat        - Display nfs stats
  	pargs          - Show process command line arguments
  	penv           - Show process environment
  	pfile          - Display process file info
  	pidof          - Find the process ID of a running program
  	pinfo          - Display all process info
  	pmodules       - Display process module info
  	ps             - Show process status
  	quit           - Terminate the shell
  	route          - Kernel IP routing table
  	set            - Set system properties
  	sleep          - Delay execution for the a number of seconds
  	source         - Read a file, executing the contents
  	sysinfo        - Display system information
  	time           - Time command
  	ulimit         - Display system resource limits
  	uptime         - Display how long the system has been running
  	version        - Display sigar and system version info
  	who            - Show who is logged on
  ```

