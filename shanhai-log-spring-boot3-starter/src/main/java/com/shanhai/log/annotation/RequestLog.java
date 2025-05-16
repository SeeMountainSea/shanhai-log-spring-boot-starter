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
     * 是否根据报文实现用户获取逻辑
     * @return
     */
    boolean queryUserBySelf() default false;
    /**
     * 本次请求是否为文件上传请求
     * @return
     */
    boolean fileUpload() default false;

    /**
     * 本次请求忽略响应报文
     * @return
     */
    boolean ignoreResponse() default false;
    /**
     * 本次请求是否为文件下载
     * @return
     */
    boolean fileDownload() default false;

    /**
     * 本次请求是否启用文件下载日志
     * @return
     */
    boolean fileDownloadLog() default false;
    /**
     * 文件下载日志处理规则
     * @return
     */
    String fileDownloadLogRule() default "";

    /**
     * 报文脱敏
     * @return
     */
    boolean dataMasking()  default false;

    /**
     * 数据脱敏规则
     * @return
     */
    String dataMaskingRule() default "";
}
