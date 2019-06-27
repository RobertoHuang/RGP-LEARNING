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

