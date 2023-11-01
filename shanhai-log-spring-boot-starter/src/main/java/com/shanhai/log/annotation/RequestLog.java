package com.shanhai.log.annotation;

import java.lang.annotation.*;

/**
 * 系统日志注解
 * @author Shanhai
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

    /**
     * 本次请求是否为文件上传请求
     * @return
     */
    boolean fileUpload() default false;
    /**
     * 本次请求是否为文件下载
     * @return
     */
    boolean fileDownload() default false;
}
