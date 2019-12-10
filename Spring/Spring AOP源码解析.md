# Spring AOP源码解析


> `AOP`动态代理指在程序运行期间动态的将某段代码切入到指定方法指定位置进行运行的编程方式

## EnableAspectJAutoProxy

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {
    // 表明该类采用CGLIB代理还是使用JDK的动态代理
    boolean proxyTargetClass() default false;

   /**
    * @since 4.3.1 代理的暴露方式：解决内部调用不能使用代理的场景  默认为false表示不处理
    * true：这个代理就可以通过AopContext.currentProxy()获得这个代理对象的一个副本（ThreadLocal里面）,从而我们可以很方便得在Spring框架上下文中拿到当前代理对象（处理事务时很方便）
    * 必须为true才能调用AopContext得方法，否则报错：Cannot find current proxy: Set 'exposeProxy' property on Advised to 'true' to make it available.
    */
    boolean exposeProxy() default false;
}
```

所有的`EnableXXX`驱动技术都得看他的`@Import`

上面最重要的是这一句`@Import(AspectJAutoProxyRegistrar.class)`

## AspectJAutoProxyRegistrar

```java
class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 这个工具类的主要作用是把AnnotationAwareAspectJAutoProxyCreator这个类定义为BeanDefinition放到spring容器中
        AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);
        AnnotationAttributes enableAspectJAutoProxy = AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
        // 获取注解属性配置，提供给AopConfigUtils使用【其本质实际上是往registry注入了对应的属性】
        if (enableAspectJAutoProxy != null) {
            if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
                AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
            }
            if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
                AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
            }
        }
    }
}
```

## AnnotationAwareAspectJAutoProxyCreator

<div align="center">    
    <img src="images/Spring AOP源码解析/AnnotationAwareAspectJAutoProxyCreator.png" align=center />
</div>

从类图是可以大致了解`AnnotationAwareAspectJAutoProxyCreator`这个类的功能【`bean`生命周期相关接口】

`Spring`基于`BeanPostProcessor`的自动代理创建器的实现类，它将根据一些规则在容器实例化`Bean`时为匹配的`Bean`生成代理实例。`AnnotationAwareAspectJAutoProxyCreator`是其子类因此会介入到`Spring IOC`容器`Bean`实例化的过程。`AbstractAutoProxyCreator`是`BeanPostProcessor`的抽象实现

```java
public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
    if (bean != null) {
        Object cacheKey = getCacheKey(bean.getClass(), beanName);
        if (this.earlyProxyReferences.remove(cacheKey) != bean) {
            // 如果需要则为bean生成代理对象
            return wrapIfNecessary(bean, beanName, cacheKey);
        }
    }
    return bean;
}
```

```java
protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
    if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
        return bean;
    }
    if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
        return bean;
    }
    // 如果是基础设施类（Pointcut、Advice、Advisor等接口的实现类），或是应该跳过的类则不应该生成代理，此时直接返回bean
    if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
        this.advisedBeans.put(cacheKey, Boolean.FALSE);
        return bean;
    }

    // 为目标bean查找合适的通知器
    Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
    if (specificInterceptors != DO_NOT_PROXY) {
        // 若通知器不为空则为bean生成代理对象，否则直接返回 bean
        this.advisedBeans.put(cacheKey, Boolean.TRUE);
        Object proxy = createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
        this.proxyTypes.put(cacheKey, proxy.getClass());
        return proxy;
    }

    this.advisedBeans.put(cacheKey, Boolean.FALSE);
    return bean;
}
```

以上流程可以分为以下三个步骤

- 若`bean`是`AOP`基础设施类型，则直接返回

- 为`bean`查找合适的通知器

- 如果通知器数组不为空，则为`bean`生成代理对象并返回该对象，否则返回原始`bean`

由此可见`SpringAOP`的本质其实是通过`BeanPostProcessor`进行拓展实现的。具体实现流程本文不展开说明