<div align="center">
  <p>
    <img src="logo.jpg"  height="200px" />
  </p>
  <p>山海Log- 基于SpringBoot 的通用业务日志记录组件</p>
  <p>ShanHaiLog-based SpringBoot Service Log component</p>
  <p>
    <a href="https://github.com/SeeMountainSea/shanhai-log-spring-boot-starter/releases/latest"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SeeMountainSea/shanhai-log-spring-boot-starter"/></a>
    <a href="https://github.com/SeeMountainSea/shanhai-log-spring-boot-starter/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SeeMountainSea/shanhai-log-spring-boot-starter?color=009688"/></a>
    <a href="https://github.com/topics/java"><img alt="GitHub top language" src="https://img.shields.io/github/languages/top/SeeMountainSea/shanhai-log-spring-boot-starter?color=eb8031"/></a>
    <br>
    <a href="https://github.com/SeeMountainSea/shanhai-log-spring-boot-starter/find/master"><img alt="GitHub Code Size" src="https://img.shields.io/github/languages/code-size/SeeMountainSea/shanhai-log-spring-boot-starter?color=795548"/></a>
    <a href="https://github.com/SeeMountainSea/shanhai-log-spring-boot-starter/find/master"><img alt="GitHub Code Lines" src="https://img.shields.io/tokei/lines/github/SeeMountainSea/shanhai-log-spring-boot-starter?color=37474F"/></a>
    <a href="https://github.com/SeeMountainSea/shanhai-log-spring-boot-starter/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SeeMountainSea/shanhai-log-spring-boot-starter?color=534BAE"/></a>
  </p>
</div>

ShanHaiLog 主要提供以下能力：

- 支持基于Spring Aop+@RequestLog注解方式记录API(请求报文/响应报文/请求时间/请求用户/请求URI等)标准化日志
- 支持Spel表达式进行动态参数获取
- 支持自定义获取当前用户信息
- 支持自定义日志存储方式
- 支持日志标准化写入操作系统临时目录（日志存储默认实现）

# 1.引入依赖

```xml
        <dependency>
            <groupId>com.wangshanhai.log</groupId>
            <artifactId>shanhai-log-spring-boot-starter</artifactId>
            <version>1.0.0</version>
        </dependency>
```

# 2.启用ShanHaiLog 组件

使用注解@EnableShanHaiLog 即可启用ShanHaiLog 组件

```java
@Configuration
@EnableShanHaiLog
public class LogConfig {
}

```

# 3.@RequestLog详解

使用@RequestLog即可对需要记录的API进行日志记录，相关定义如下：

```java
public @interface RequestLog {
    /**
     * 模块名称
     * @return
     */
    String module() default "";
    /**
     * 接口级别 (用户级/系统级)
     * sys-biz 系统级
     * user-biz 用户级
     */
    String level() default "sys-biz";
    /**
     * 日志内容
     */
    String message();
    /**
     * 当前用户
     * @return
     */
    String currentUser() default "";
}
```



使用方式样例如下：

```java
@RequestLog(module = "Order",currentUser ="#{#currentUser}", message = "分页查询订单-当前用户：#{#currentUser},当前页：#{#current}，每页条数：#{#size}")
public HttpResponse<IPage<WorkOrder>> queryByPage(@RequestParam("currentUser") String currentUser,@RequestParam("size") Long size,@RequestParam("current")Long current){
    ……
}
```

**注:#{#currentUser} 为Spel表达式用法，可以提取对应方法的相关参数**

# 4.自定义日志存储

Log组件预留了自定义日志存储接口，可以自行进行扩展，实现自己的日志存储方式。

接口定义如下：

```java
public interface RequestLogService {
  /**
   * 存储日志
   * @param requestLogInfo
   */
  public void saveLog(RequestLogInfo requestLogInfo);
}
```

# 5.自定义获取当前登录用户信息

Log组件预留了自定义获取当前登录用户信息接口，可以自行进行扩展，实现与自己系统的用户鉴权体系的无缝对接。

接口定义如下：

```
public interface RequestLogService {
  /**
   * 获取当前登录用户
   * @return
   */
  public String getCurrentUser(HttpServletRequest request);
}
```

注：对于登录等非鉴权的场景，可以使用@RequestLog中的currentUser 并结合Spel进行使用。

**当@RequestLog中的currentUser 存在Spel表达式时，表达式的优先级高于自定义接口实现的优先级。**

# 6.自定义参数写入通用RequestLogInfo

Log组件预留了自定义参数写入接口，方便用户在APILOG进行日志存储时，写入自定义参数。

接口定义如下：

```java
public interface RequestLogService {
  /**
   * 补充自定义扩展日志信息
   * @return
   */
  public Map<String,Object> getExtLogInfo(HttpServletRequest request);
}
```

此扩展会在执行saveLog之前自动调用并写入RequestLogInfo->extLogInfo 字段

# 7.自定义实现源IP获取方法

Log组件预留了自定义源IP获取方法，方便用户自定义实现源IP获取策略。

```java
public interface RequestLogService {
  /**
   * 自定义源IP获取方法
   * @return
   */
  public String getReqSourceIp(HttpServletRequest request);
}
```

# 8.启用API Log 控制台输出

由于Log组件提供了默认存储实现(存储于操作系统临时目录)，因此API LOG的控制台输出默认是被关闭的。

在开发阶段，可以考虑打开API LOG 控制台输出查看相关报文。生产环境在自定义存储实现时，也可以考虑打开控制台输出，做日志存储灾备。

```
shanhai.log.consoleShow=true
```

# 9.标准化日志对象参数说明(RequestLogInfo)

```java
/**
 * 当前用户
 */
private String currentUser;

/**
 * 接口日志
 */
private String message;

/**
 * 日志级别
 */
private String level;
/**
 * 所属模块
 */
private String module;

/**
 * 请求开始时间
 */
private Date reqTime;
/**
 * 请求结束时间
 */
private Date respTime;

/**
 * 浏览器信息
 */
private String agentInfo;

/**
 * 用户ip
 */
private String reqSourceIp;

/**
 * 请求url
 */
private String reqUrl;

/**
 * 请求报文
 */
private String reqInfo;

/**
 * 响应报文
 */
private String respInfo;
/**
 * 响应状态码
 */
private Integer respStatusCode;
/**
 * HTTP请求类型
 */
private String httpMethod;
```