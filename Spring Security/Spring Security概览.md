# Spring Security

> 基于`Spring`的强大且高度可定制的身份验证和访问控制框架

## SpringBoot内嵌应用容器并且使用自动配置

- `SecurityFilterAutoConfiguration`
- `DelegatingFilterProxyRegistrationBean`在`SpringBoot`环境下通过`TomcatStarter`等内嵌容器启动类来注册一个`DelegatingFilterProxy`，此处注册的过滤器名称为`springSecurityFilterChain`

## DelegatingFilterProxy

> `DelegatingFilterProxy`是`SpringSecurity`的"门面"，实现了`javax.servlet.Filter`接口使得它可以作为一个`JAVA Web`的标准过滤器，其职责也很简单只负责调用真正的`SpringSecurityFilterChain`

- `DelegatingFilterProxy`的逻辑主要是调用`delegate`的`doFilter`方法，那`delegate`到底是啥呢？

  `DelegatingFilterProxy`尝试去容器中获取名为`targetBeanName`的类，而`targetBeanName`的默认值便是`Filter`的名称也就是`springSecurityFilterChain`。也就是说`DelegatingFilterProxy`最后会委托给`Bean(name=”springSecurityFilterChain”) `的过滤器进行处理

- 通过`debug`发现真正的`springSecurityFilterChain`其实是`FilterChainProxy`，那这东西在哪里初始化呢
  - `spring-boot-autoconfig/META-INF/spring.factories`
    - `org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration`
      - `@import(WebSecurityEnablerConfiguration.class)`
        - `@EnableWebSecurity`
          - `@Import(WebSecurityConfiguration.class)`
            - `FilterChainProxy`在`WebSecurityConfiguration`中完成初始化

## FilterChainProxy

看`FilterChainProxy`的名字就可发现它依旧不是真正实施过滤的类，它内部维护了`SecurityFilterChain`，这个过滤器链才是请求真正对应的过滤器链。并且同一个`Spring`环境下可能同时存在多个安全过滤器链

如`private List filterChains`所示，需要经过`chain.matches(request)`判断到底哪个过滤器链匹配成功，每个`request`最多只会经过一个`SecurityFilterChain`，值得再次强调:每次请求最多只有一个安全过滤器链被返回

为何要这么设计？因为`Web`环境下可能有多种安全保护策略，每种策略都需要有自己的一条链路

## SecurityFilterChain

`SecurityFilterChain`才是真正意义上的`SpringSecurityFilterChain`

`SecurityFilterChain`的初始化最终在`WebSecurity`中完成，可参见`WebSecurity#performBuild`

## 链接被拦截如何排查

- 从决策器反推

  - `AffirmativeBased#decide` - `attributes`属性
  - `AbstractSecurityInterceptor#beforeInvocation`
  - `FilterSecurityInterceptor的securityMetadataSource`属性

  - `DefaultFilterInvocationSecurityMetadataSource#getAttributes` - 关注`requestMap`属性
  - 常规情况下我们使用的是`ExpressionBasedFilterInvocationSecurityMetadataSource`，该类的构造会调用到`DefaultFilterInvocationSecurityMetadataSource`构造完成`requestMap`的初始化
  - `ExpressionUrlAuthorizationConfigurer#createMetadataSource` - 那这里`requestMap`是从何而来，通过断点可以发现`requestMap`是由`REGISTRY`装换而来，而`REGISTRY`正是我们配置的拦截规则