## maven-compiler-plugin

> 使用`maven-compiler-plugin`插件可以指定项目源码的`JDK`版本，编译后的`JDK`版本，以及编码

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${maven-compiler-plugin.version}</version>
    <configuration>
        <source>${jdk.version}</source>
        <target>${jdk.version}</target>
        <encoding>UTF-8</encoding>
        <showWarnings>true</showWarnings>
        <showDeprecation>true</showDeprecation>
    </configuration>
</plugin>
```

## appassembler-maven-plugin

> `appassembler-maven-plugin`可以自动生成跨平台的启动脚本，省去了手工写脚本的麻烦，而且还可以生成`JSW`的后台运行程序。接下来简单分析下`appassembler-maven-plugin`配置文件

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>appassembler-maven-plugin</artifactId>
    <version>2.1.0</version>
    <configuration>
        <!-- 输出文件夹 -->
        <target>${project.build.directory}</target>

        <!-- 配置文件的目标目录 -->
        <configurationDirectory>conf</configurationDirectory>
        <!-- 拷贝配置文件到上面的目录中 -->
        <copyConfigurationDirectory>true</copyConfigurationDirectory>
        <!-- 从哪里拷贝配置文件 (默认src/main/config) -->
        <configurationSourceDirectory>../config/appassembler</configurationSourceDirectory>
        <!-- 使用通配符路径 解决CLASSPATH太长的问题 -->
        <useWildcardClassPath>true</useWildcardClassPath>
        <!-- Include the target configuration directory in the beginning of the classpath declaration in the bin scripts -->
        <includeConfigurationDirectoryInClasspath>true</includeConfigurationDirectoryInClasspath>
        <filterConfigurationDirectory>true</filterConfigurationDirectory>

        <!-- 打包的jar，以及maven依赖的jar放到这个目录里面 -->
        <repositoryName>lib</repositoryName>
        <!-- lib目录中jar的存放规则 -->
        <!-- 默认是${groupId}/${artifactId}的目录格式 flat表示直接把jar放到lib目录 -->
        <repositoryLayout>flat</repositoryLayout>

        <!-- 可执行脚本的目录 -->
        <binFolder>bin</binFolder>
        <binFileName>loghub</binFileName>

        <encoding>UTF-8</encoding>
        <logsDirectory>logs</logsDirectory>
        <tempDirectory>temp</tempDirectory>

        <daemons>
            <daemon>
                <id>${project.build.finalName}</id>
                <!-- 启动类 -->
                <mainClass>com.ucarinc.loghub.application.wrapper.StartUp</mainClass>
                <commandLineArguments>
                    <commandLineArgument>conf/server.properties</commandLineArgument>
                </commandLineArguments>
                <platforms>
                    <platform>jsw</platform>
                </platforms>
                <generatorConfigurations>
                    <generatorConfiguration>
                        <generator>jsw</generator>
                        <includes>
                            <include>linux-x86-32</include>
                            <include>linux-x86-64</include>
                            <include>windows-x86-32</include>
                            <include>windows-x86-64</include>
                        </includes>
                        <configuration>
                            <property>
                                <name>configuration.directory.in.classpath.first</name>
                                <value>conf</value>
                            </property>
                            <property>
                                <name>wrapper.ping.timeout</name>
                                <value>120</value>
                            </property>
                            <property>
                                <name>set.default.REPO_DIR</name>
                                <value>lib</value>
                            </property>
                            <property>
                                <name>wrapper.logfile</name>
                                <value>logs/wrapper.log</value>
                            </property>
                        </configuration>
                    </generatorConfiguration>
                </generatorConfigurations>
                <jvmSettings>
                    <extraArguments>
                        <extraArgument>-server</extraArgument>
                        <extraArgument>-Xmx2G</extraArgument>
                        <extraArgument>-Xms2G</extraArgument>
                        <!-- Remote Debug -->
                        <!--<extraArgument>-Xdebug</extraArgument>-->
                        <!--<extraArgument>-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5010</extraArgument>-->
                        <!-- Heap Dump -->
                        <extraArgument>-XX:+HeapDumpOnOutOfMemoryError</extraArgument>
                        <extraArgument>-XX:HeapDumpPath=logs/heap-dump.hprof</extraArgument>
                        <!-- GC Config -->
                        <!--<extraArgument>-XX:+UseG1GC</extraArgument>
                        <extraArgument>-XX:MaxGCPauseMillis=200</extraArgument>
                        <extraArgument>-XX:InitiatingHeapOccupancyPercent=45</extraArgument>
                        <extraArgument>-XX:G1ReservePercent=10</extraArgument>
                        <extraArgument>-XX:NewRatio=2</extraArgument>
                        <extraArgument>-XX:SurvivorRatio=8</extraArgument>
                        <extraArgument>-XX:MaxTenuringThreshold=15</extraArgument>-->

                        <!-- GC Log -->
                        <!--<extraArgument>-Xloggc:logs/gc.log</extraArgument>
                        <extraArgument>-XX:GCLogFileSize=10M</extraArgument>
                        <extraArgument>-XX:NumberOfGCLogFiles=10</extraArgument>
                        <extraArgument>-XX:+UseGCLogFileRotation</extraArgument>
                        <extraArgument>-XX:+PrintGCDateStamps</extraArgument>
                        <extraArgument>-XX:+PrintGCTimeStamps</extraArgument>
                        <extraArgument>-XX:+PrintGCDetails</extraArgument>
                        <extraArgument>-XX:+PrintHeapAtGC</extraArgument>
                        <extraArgument>-XX:+PrintGCApplicationStoppedTime</extraArgument>
                        <extraArgument>-XX:+DisableExplicitGC</extraArgument>
                        <extraArgument>-verbose:gc</extraArgument>-->
                    </extraArguments>

                    <systemProperties>
                        <!--
                            <systemProperty>com.sun.management.jmxremote</systemProperty>
                            <systemProperty>com.sun.management.jmxremote.port=7777</systemProperty>
                            <systemProperty>com.sun.management.jmxremote.authenticate=false</systemProperty>
                            <systemProperty>com.sun.management.jmxremote.ssl=false</systemProperty>
                        -->
                        <systemProperty>log4j.configurationFile=conf/log4j2.xml</systemProperty>
                    </systemProperties>
                </jvmSettings>
            </daemon>
        </daemons>
    </configuration>
    <executions>
        <execution>
            <id>make-assembly</id>
            <phase>package</phase>
            <goals>
                <goal>generate-daemons</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## maven-assembly-plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
        <!-- 不发布到maven -->
        <attach>false</attach>
        <!-- 打包文件的文件名中包含assembly id -->
        <appendAssemblyId>false</appendAssemblyId>
        <finalName>${project.build.finalName}</finalName>
        <descriptors>
            <!-- 指定maven-assembly-plugin的配置文件 -->
            <descriptor>../config/assembly.xml</descriptor>
        </descriptors>
        <!-- 如果出现【group id xxx is too big】就需要加上以下配置项 -->
        <!-- <tarLongFileMode>posix</tarLongFileMode> -->
    </configuration>
    <executions>
        <execution>
            <id>make-assembly</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

```xml
<assembly>
    <id>bundle</id>
    <formats>
        <!-- format format=zip设置打包的最终文件格式为zip. 支持的其他格式还有gz,tar,tar.gz,tar.bz2等 -->
        <format>tar.gz</format>
    </formats>
    <includeBaseDirectory>true</includeBaseDirectory>
    <fileSets>
        <fileSet>
            <directory>bin</directory>
            <outputDirectory>bin</outputDirectory>
            <!-- 将bin目录下的文件打包到根目录(bin)下.<fileMode>0755</fileMode>指明bin目录下所有文件的权限为755 -->
            <fileMode>0755</fileMode>
        </fileSet>
        <fileSet>
            <directory>conf</directory>
            <outputDirectory>conf</outputDirectory>
            <fileMode>0644</fileMode>
        </fileSet>
    </fileSets>
    <dependencySets>
        <dependencySet>
            <!-- 将依赖打包到lib目录下 -->
            <outputDirectory>lib</outputDirectory>
        </dependencySet>
    </dependencySets>
</assembly>
```

详细语法介绍可参考:[Assembly Descriptor Format reference](http://maven.apache.org/plugins/maven-assembly-plugin/assembly.html)

## maven-shade-plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.2.1</version>
    <configuration>
        <!-- 自动将所有不使用的类全部排除掉 -->
        <minimizeJar>false</minimizeJar>
        <createDependencyReducedPom>true</createDependencyReducedPom>
        <!-- 表示把shade过的jar作为项目默认的包 -->
        <shadedArtifactAttached>false</shadedArtifactAttached>
        <!-- 当上面为属性为ture时生效 修改生成jar的后缀名 -->
        <!-- <shadedClassifierName>jackofall</shadedClassifierName> -->
        <artifactSet>
            <includes>
                <include>com.google.guava:*</include>
                <include>com.ucarinc:loghub-core</include>
            </includes>
        </artifactSet>
        <relocations>
            <relocation>
                <pattern>com.google</pattern>
                <shadedPattern>com.ucarinc.loghub.com.google</shadedPattern>
            </relocation>
        </relocations>
        <filters>
            <filter>
                <artifact>*:*</artifact>
                <excludes>
                    <exclude>LICENSE</exclude>
                    <exclude>META-INF/*.SF</exclude>
                    <exclude>META-INF/*.DSA</exclude>
                    <exclude>META-INF/*.RSA</exclude>
                </excludes>
            </filter>
        </filters>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

- 解决二方库`Profiler`解析问题，及修改`package`后`pom`的信息

    ```xml
    <!-- 解析、压缩pom插件; 一个小缺陷: parent中的依赖、成员会被拷贝到每个子模块中, 但不影响使用 -->
    <plugin>
        <groupId>com.carrotgarden.maven</groupId>
        <artifactId>flatten-maven-plugin</artifactId>
        <version>1.6.20190428102750</version>
        <configuration>
            <!-- 依赖解析开关 -->
            <performDependencyResolve>false</performDependencyResolve>
            <!-- 只解析指定级别以上的scope, 不在范围内的scope依赖会被删除; 这里设置了runtime那么test依赖就会删除 -->
            <includeScope>runtime</includeScope>
            <!-- 排除掉依赖解析, 设置为false时可以看到一个模块的完整依赖(不管有多少层级依赖,它会把这些依赖全部写入当前模块生成的pom文件中) -->
            <excludeTransitive>true</excludeTransitive>
    
            <!-- performEraseScopes和performDependencyResolve是二选一的 -->
            <!-- 如果不启用解析(performDependencyResolve=false), 又想要删除scope, 就可以用这个来删除指定scope列表的所有依赖, 这里把test依赖删掉没啥用 -->
            <performEraseScopes>true</performEraseScopes>
            <scopeEraseList>
                <scope>test</scope>
            </scopeEraseList>
    
            <!-- 开启pom.xml成员删除, 被删除的成员列表通过:memberRemoveList设置 -->
            <performRemoveMembers>true</performRemoveMembers>
            <memberRemoveList>
                <member>properties</member>
                <member>distributionManagement</member>
                <member>dependencyManagement</member>
                <member>repositories</member>
                <member>pluginRepositories</member>
                <member>build</member>
                <member>profiles</member>
                <member>reporting</member>
            </memberRemoveList>
    
            <!-- 开启pom.xml的maven坐标替换. -->
            <performOverrideIdentity>true</performOverrideIdentity>
            <overrideGroupId>${project.groupId}</overrideGroupId>
            <overrideArtifactId>${project.artifactId}</overrideArtifactId>
    
            <!-- 将指定packaging类型的pom.xml用生成的pom.xml.flatten替换 -->
            <performSwitchPomXml>true</performSwitchPomXml>
            <packagingSwitchList>
                <packaging>jar</packaging>
                <packaging>pom</packaging>
            </packagingSwitchList>
        </configuration>
        <executions>
            <!-- 在"prepare-package"期间执行"flatten:flatten" -->
            <execution>
                <goals>
                    <goal>flatten</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    ```


## versions-maven-plugin聚合工程版本控制

`versions-maven-plugin`插件可以管理项目版本，特别是当`Maven`工程项目中有大量子模块时可以批量修改`pom`版本号，插件会把父模块更新到指定版本号然后更新子模块版本号与父模块相同，可以避免手工大量修改和遗漏的问题

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>versions-maven-plugin</artifactId>
    <version>${versions-maven-plugin.version}</version>
    <executions>
        <execution>
            <id>update-mc-version</id>
            <phase>install</phase>
            <inherited>false</inherited>
            <goals>
                <goal>set</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <newVersion>${mc.version}</newVersion>
        <generateBackupPoms>false</generateBackupPoms>
        <processAllModules>true</processAllModules>
    </configuration>
</plugin>
```

## 附录

- `Maven`的`setting.xml`中的`MirrorOf`配置

    > 当`maven`需要到的依赖jar包不在本地仓库时, 就需要到远程仓库下载
    >
    > 这个时候如果`maven setting.xml`中配置了镜像 , 而且镜像配置的规则中匹配到目标仓库时，`maven`认为目标仓库被镜像了, 不会再去被镜像仓库下载依赖`jar`包，而是直接去镜像仓库下载。简单而言`mirror`可以拦截对远程仓库的请求，改变对目标仓库的下载地址。以下是一些配置示例

    ```xml
    <mirrorOf>central</mirrorOf> 表示该配置为中央仓库的镜像
    <mirrorOf>*</mirrorOf> 匹配所有远程仓库
    <mirrorOf>external:*</mirrorOf> 匹配所有远程仓库，使用localhost的除外，使用file://协议的除外。也就是说匹配所有不在本机上的远程仓库
    <mirrorOf>*,!repo1</miiroOf> 匹配所有远程仓库，repo1除外，使用感叹号将仓库从匹配中排除
    <mirrorOf>repo1,repo2</mirrorOf> 匹配仓库repo1和repo2，使用逗号分隔多个远程仓库。这个的repo1和repo2对应的是repository的id标签的值
    ```

    附上一个常用的`Maven setting`文件配置

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">  
      <!-- 本地仓库地址 -->  
      <localRepository>/Users/roberto/Repository</localRepository>  
      
      <proxies> 
        <!-- 
          <proxy>
            <id>optional</id>
            <active>true</active>
            <protocol>http</protocol>
            <username>proxyuser</username>
            <password>proxypass</password>
            <host>proxy.host.net</host>
            <port>80</port>
            <nonProxyHosts>local.net|some.host.com</nonProxyHosts>
          </proxy>
        --> 
      </proxies>  
      <servers> 
        <!-- 配置仓库的认证信息 -->  
        <server> 
          <!-- 对应下面的Repository的ID/Mirror的ID -->  
          <id>company-nexus</id>  
          <username>admin</username>  
          <password>zcjy996</password> 
        </server> 
      </servers>  
      <mirrors> 
        <mirror> 
          <id>company-nexus</id>  
          <!-- 对应下面的Repository的ID -->  
          <mirrorOf>central</mirrorOf>  
          <name>company-nexus</name>  
          <url>http://118.24.206.22:8081/repository/maven-public/</url> 
        </mirror>  
        <mirror> 
          <id>aliyun-nexus</id>  
          <!-- 对应下面的Repository的ID -->  
          <mirrorOf>home</mirrorOf>  
          <name>aliyun maven</name>  
          <url>http://maven.aliyun.com/nexus/content/repositories/central/</url> 
        </mirror> 
      </mirrors>  
     
      <profiles> 
        <profile> 
          <id>nexus-home</id>  
          <!-- 用来下载[非插件]的依赖 -->  
          <repositories> 
            <repository> 
              <id>home</id>  
              <url>http://central</url>  
              <releases> 
                <enabled>true</enabled> 
              </releases>  
              <snapshots> 
                <enabled>true</enabled>  
                <updatePolicy>always</updatePolicy> 
              </snapshots> 
            </repository> 
          </repositories>  
          <!-- 用来下载[插件]的依赖 -->  
          <pluginRepositories> 
            <pluginRepository> 
              <id>home</id>  
              <url>http://central</url>  
              <releases> 
                <enabled>true</enabled> 
              </releases>  
              <snapshots> 
                <enabled>true</enabled>  
                <updatePolicy>always</updatePolicy> 
              </snapshots> 
            </pluginRepository> 
          </pluginRepositories> 
        </profile> 
    
        <profile> 
          <id>nexus-company</id>  
          <!-- 用来下载[非插件]的依赖 -->  
          <repositories> 
            <repository> 
              <id>central</id>  
              <url>http://central</url>  
              <releases> 
                <enabled>true</enabled> 
              </releases>  
              <snapshots> 
                <!-- 是否允许该仓库为artifact提供发布版/快照版下载功能 -->  
                <enabled>true</enabled>  
                <!-- 
                  每次执行构建命令时Maven会比较本地POM和远程POM的时间戳, 该元素指定比较的频率
                  always每次构建都检查, daily默认距上次构建检查时间超过一天, interval:x 距上次构建检查超过x分钟、 never从不
                 -->  
                <updatePolicy>always</updatePolicy> 
              </snapshots> 
            </repository> 
          </repositories>  
          <!-- 用来下载[插件]的依赖 -->  
          <pluginRepositories> 
            <pluginRepository> 
              <id>central</id>  
              <url>http://central</url>  
              <releases> 
                <enabled>true</enabled> 
              </releases>  
              <snapshots> 
                <enabled>true</enabled>  
                <updatePolicy>always</updatePolicy> 
              </snapshots> 
            </pluginRepository> 
          </pluginRepositories> 
        </profile> 
      </profiles>  
    
      <activeProfiles> 
        <!-- 默认需要激活的Profile -->  
        <activeProfile>nexus-home</activeProfile> 
      </activeProfiles> 
    </settings>
    ```

    