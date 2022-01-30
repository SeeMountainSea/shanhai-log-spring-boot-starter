package com.shanhai.log.annotation;

import java.lang.annotation.*;

/**
 * 系统日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
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
