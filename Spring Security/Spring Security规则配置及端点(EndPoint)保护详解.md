## 匹配规则

- `HttpSecurity.antMatcher(...)`表示该安全规则只针对参数指定的路径进行过滤
- `HttpSecurity.requestMatchers`同上，唯一区别是可以接受多个参数【两者不能同时使用】
- `HttpSecurity.authorizeRequests()`该方法用于配置权限，如`hasAnyRole`、`access(...)`
- `.authorizeRequests().anyRequest()`除去已经配置了的路径之外的其他路径，即包含在`(1)/(2)`中不包含在`HttpSecurity.authorizeRequests().antMatchers(…)`中的其他路径

## 端点保护配置

了解到这部分的知识是因为在做`OAuth2`认证的时候，我发现项目中的安全配置已经开放所有请求(即`/**`请求不进行拦截)，但是当我访问`/oauth/token`的时候竟然提示`401`，百思不得其解。最后发现原来在`Spring Security`中预制了一些默认断点保护策略。具体配置是在`AuthorizationServerSecurityConfiguration`中

## 过滤规则踩到的坑

```java
.requestMatchers().antMatchers("/test/**").and()
.authorizeRequests().antMatchers("/test/authenticated").authenticated()
.anyRequest().permitAll().and()
```

通过匹配规则我们可以知道这部分配置的意思是针对`/test/**`的请求将使用安全配置，`/test/authenticated`是需要认证的，匹配`/test/**`且不是`/test/authenticated`的请求是不需要认证的。但是在实际项目中却遇到了一个坑，就是我访问`/test/**`的任何请求都是需要认证的，跟了源码发现是使用错误，具体原因是因为我在继承`WebSecurityConfigurerAdapter`重写`configure(HttpSecurity http)`方法的最后多写了一行代码

```java
super.configure(http);
```

我在最后又去调用了`WebSecurityConfigurerAdapter`的`configure(HttpSecurity http)`方法

```java
protected void configure(HttpSecurity http) throws Exception {
   logger.debug("Using default configure(HttpSecurity). If subclassed this will potentially override subclass configure(HttpSecurity).");
   http
      .authorizeRequests().anyRequest().authenticated().and()
      .formLogin().and()
      .httpBasic();
}
```

这个是`WebSecurityConfigurerAdapter.configure`方法的源码，它默认会对所有请求进行过滤。有兴趣的同学可以跟踪源码会发现`Spring Security`对`URL`拦截规则最后是存放在`Map`中，即在`super`的配置会覆盖掉自定义配置导致自定义配置失效，写的比较简洁可能不是很好理解。

