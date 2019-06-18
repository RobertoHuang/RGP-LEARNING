# Disconf

> `Disconf`是一个分布式配置管理平台
>
> 专注于各种分布式系统配置管理的通用组件/通用平台
>
> 提供统一的配置管理服务，是一套完整的基于`Zookeeper`的分布式配置统一解决方案

## 架构图

![disconf架构图](https://raw.githubusercontent.com/RobertoHuang/RGP-LEARNING/master/Others/images/disconf%E6%9E%B6%E6%9E%84%E5%9B%BE.png)

即在`disconf-web`上更新配置后利用`zookeeper`监听机制告知`disconf-client`

`disconf-client`收到配置变化后会再从`disconf-web`上拉取配置，进行配置的动态更新

`disconf`的功能是基于`Spring`的，初始化是在`postProcessBeanDefinitionRegistry`开始的

配置动态更新也是要更新到`Spring IOC`中对应的`bean`，所以使用`disconf`项目必须基于`Spring`

## 初始化流程分析

- 启动入口:`DisconfMgrBean#postProcessBeanDefinitionRegistry`

  `DisconfMgrBean`实现了`PriorityOrdered`接口，它的`Bean`初始化`Order`是最高优先级

  当`Spring`扫描了所有`Bean`信息后，在所有`Bean`初始化之前`postProcessBeanDefinitionRegistry`被调用

  ```java
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
      // 为了做兼容
      DisconfCenterHostFilesStore.getInstance().addJustHostFileSet(fileList);
  
      List<String> scanPackList = StringUtil.parseStringToStringList(scanPackage, SCAN_SPLIT_TOKEN);
      // unique
      Set<String> hs = new HashSet<String>();
      hs.addAll(scanPackList);
      scanPackList.clear();
      scanPackList.addAll(hs);
  
      // 进行扫描
      DisconfMgr.getInstance().setApplicationContext(applicationContext);
      DisconfMgr.getInstance().firstScan(scanPackList);
  
      // register java bean
      registerAspect(registry);
  }
  ```

  该方法主要是调用`DisconfMgr#firstScan`完成第一次扫描

- `DisconfMgr#firstScan`第一次扫描

  ```java
  protected synchronized void firstScan(List<String> scanPackageList) {
      // 该函数不能调用两次
      if (isFirstInit) {
          LOGGER.info("DisConfMgr has been init, ignore........");
          return;
      }
  
      try {
          // 导入配置
          ConfigMgr.init();
  
          LOGGER.info("******************************* DISCONF START FIRST SCAN *******************************");
          // registry
          Registry registry = RegistryFactory.getSpringRegistry(applicationContext);
  
          // 扫描器
          scanMgr = ScanFactory.getScanMgr(registry);
  
          // 第一次扫描并入库
          scanMgr.firstScan(scanPackageList);
          
          // 获取数据/注入/Watch
          disconfCoreMgr = DisconfCoreFactory.getDisconfCoreMgr(registry);
          disconfCoreMgr.process();
  
          isFirstInit = true;
          LOGGER.info("******************************* DISCONF END FIRST SCAN *******************************");
      } catch (Exception e) {
          LOGGER.error(e.toString(), e);
      }
  }
  
  ```

  - `ConfigMgr.init()`导入配置

    ```java
    public synchronized static void init() throws Exception {
        LOGGER.info("--------------- LOAD CONFIG START ---------------");
    
        // 导入系统配置
        DisClientSysConfig.getInstance().loadConfig(null);
    
        // 校验 系统配置
        DisInnerConfigHelper.verifySysConfig();
    
        // 导入用户配置
        DisClientConfig.getInstance().loadConfig(null);
    
        // 校验 用户配置
        DisInnerConfigHelper.verifyUserConfig();
    
        isInit = true;
    
        LOGGER.info("--------------- LOAD CONFIG END ---------------");
    }
    ```

    `disconf`的配置文件包括系统自带和用户配置两部分

    分别由`DisClientSysConfig`和`DisClientConfig`解析处理

    先看`DisClientSysConfig`所有属性都有注解`@DisInnerConfigAnnotation`，会通过反射的方式来赋值

    ```java
    /**
     * STORE URL
     *
     * @author
     * @since 1.0.0
     */
    @DisInnerConfigAnnotation(name = "disconf.conf_server_store_action")
    public String CONF_SERVER_STORE_ACTION;
    
    /**
     * STORE URL
     *
     * @author
     * @since 1.0.0
     */
    @DisInnerConfigAnnotation(name = "disconf.conf_server_zoo_action")
    public String CONF_SERVER_ZOO_ACTION;
    
    /**
     * 获取远程主机个数的URL
     *
     * @author
     * @since 1.0.0
     */
    @DisInnerConfigAnnotation(name = "disconf.conf_server_master_num_action")
    public String CONF_SERVER_MASTER_NUM_ACTION;
    
    /**
     * 下载文件夹, 远程文件下载后会放在这里
     *
     * @author
     * @since 1.0.0
     */
    @DisInnerConfigAnnotation(name = "disconf.local_download_dir")
    public String LOCAL_DOWNLOAD_DIR;
    ```

    `DisClientSysConfig`默认的系统配置文件为`disconf_sys.properties`

    `DisClientSysConfig`的`loadConfig`方法会调用`DisconfAutowareConfig.autowareConfig()`

    `DisconfAutowareConfig`主要的实现思路是通过`DisconfAutowareConfig`将配置的内容到导入到`Properties`中，然后通过反射的方式将上面`DisClientSysConfig`的各个属性和配置对应赋值

    最后通过`DisInnerConfigHelper.verifySysConfig`进行配置校验，`DisClientConfig`配置同理(略)

  - `Config`配置文件读取完毕，继续扫描工作

    ```java
    // 将上下文处理成上下文处理工具
    Registry registry = RegistryFactory.getSpringRegistry(applicationContext);
    
    // 扫描器 通过扫描器工厂，获取扫描器工具
    scanMgr = ScanFactory.getScanMgr(registry);
    
    // 第一次扫描并入库
    scanMgr.firstScan(scanPackageList);
    ```

    `ScanFactory`工厂得到的`ScanMgrImpl`构造函数有很多信息，`ScanMgrImpl`作为扫描模块的中心

    ```java
    public ScanMgrImpl(Registry registry) {
        this.registry = registry;
        // 配置文件
        staticScannerMgrList.add(StaticScannerMgrFactory.getDisconfFileStaticScanner());
        // 配置项
        staticScannerMgrList.add(StaticScannerMgrFactory.getDisconfItemStaticScanner());
        // 非注解 托管的配置文件
        staticScannerMgrList.add(StaticScannerMgrFactory.getDisconfNonAnnotationFileStaticScanner());
    }
    ```

    除了`registry`上下文的赋值还包括将三种配置类型的扫描工具载入到`staticScannerMgrList`中，在后面的扫描过程中会遍历`staticScannerMgrList`分别处理`Disconf`上的配置，接下来开始扫描静态文件

    ```java
    public void firstScan(List<String> packageNameList) throws Exception {
        // 获取扫描对象并分析整合
        scanModel = scanStaticStrategy.scan(packageNameList);
        // 增加非注解的配置
        scanModel.setJustHostFiles(DisconfCenterHostFilesStore.getInstance().getJustHostFiles());
        // 放进仓库
        for (StaticScannerMgr scannerMgr : staticScannerMgrList) {
            // 扫描进入仓库
            scannerMgr.scanData2Store(scanModel);
            // 忽略哪些KEY
            scannerMgr.exclude(DisClientConfig.getInstance().getIgnoreDisconfKeySet());
        }
    }
    ```

    `ScanStaticModel`的作用是配置扫描内容的存储对象

    `ReflectionScanStatic`将路径下文件扫描得到静态注解，并整合到`ScanStaticModel`中

    `ReflectionScanStatic`的主要处理方式是通过反射将`Disconf`支持的所有注解获取到，初步扫描以后会进行解析赋值到`ScanStaticModel`对象中，最后是对`staticScannerMgrList`的遍历，通过各种类型的扫描工具分别处理`scanModel`，将结果添加到`DisconfCenterStore`仓库中

    看下扫描工具的实现拿`StaticScannerFileMgrImpl`举例

  - `disconfCoreProcessorList`包含两种处理器

    `DisconfFileCoreProcessorImpl`和`DisconfItemCoreProcessorImpl`

    处理器实现了`DisconfCoreProcessor`接口，`disconfCoreMgr.process()`方法遍历两种处理器调用其`processAllItems()`方法处理，此处以`DisconfFileCoreProcessorImpl`为例说明

    ```java
    public void processAllItems() {
        // 获取所有配置文件名 遍历
        for (String fileName : disconfStoreProcessor.getConfKeySet()) {
            processOneItem(fileName);
        }
    }
    ```

    ```java
    public void processOneItem(String key) {
        // 获取仓库中的元素
        DisconfCenterFile disconfCenterFile = (DisconfCenterFile) disconfStoreProcessor.getConfData(key);
        try {
            updateOneConfFile(key, disconfCenterFile);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }
    ```

  - 

- `DisconfStoreProcessor`

  - 

- `DisconfFileTypeProcessor`配置处理器

  > 实现类:
  >
  > - `DisconfXmlProcessorImpl`空实现
  > - `DisconfAnyFileProcessorImpl`空实现
  > - `DisconfPropertiesProcessorImpl`支持读取Properties文件
  >
  > 根据配置项的类型和文件路径，读取配置文件值到`Map`，目前只支持`properties`文件



- `NodeWatcher`对`ZK`节点进行监听

  如果配置`disconf.enable.remote.conf`为`true`，`disconf`会监听远端文件变化来更新本地配置

  监听远程文件变化是通过`NodeWatcher`完成的，`NodeWatcher`是`zookeeper api`接口`Watch`的实现

  当`NodeWatcher`监听到节点数据变化时会执行回调函数执行`DisconfSysUpdateCallback#reload`方法

  而`reload()`方法会调用`disconfCoreMgr`的`updateOneConfAndCallback()`方法

- 



------------------------------------------------------------



## 第二次扫描

第二次扫描的`XML`配置如下

```xml
<bean id="disconfMgrBean2" class="com.baidu.disconf.client.DisconfMgrBeanSecond" init-method="init" destroy-method="destroy"></bean>
```

在`DisconfMgrBeanSecond#init`会调用`DisconfMgr#secondScan`

```java
protected synchronized void secondScan() {
    // ...
    try {
        // 扫描回调函数
        if (scanMgr != null) {
            scanMgr.secondScan();
        }

        // 注入数据至配置实体中
        // 获取数据/注入/Watch
        if (disconfCoreMgr != null) {
            disconfCoreMgr.inject2DisconfInstance();
        }
    } catch (Exception e) {
        LOGGER.error(e.toString(), e);
    }
    isSecondInit = true;
}
```

第二次扫描的主要处理逻辑如下

```java
// 将回调函数实例化并写入仓库
ScanDynamicStoreAdapter.scanUpdateCallbacks(scanModel, registry);
```

`ScanDynamicStoreAdapter`是动态扫描与`Store`模块的转换器。下面主要对回调函数的处理

```java
// ScanStaticModel是第一次扫描结束得到的静态配置存储的对象
public static void scanUpdateCallbacks(ScanStaticModel scanModel, Registry registry) {
    // 扫描出来
    ScanDynamicModel scanDynamicModel = analysis4DisconfUpdate(scanModel, registry);

    // 写到仓库中
    transformUpdateService(scanDynamicModel.getDisconfUpdateServiceInverseIndexMap());
    transformPipelineService(scanDynamicModel.getDisconfUpdatePipeline());
}
```

