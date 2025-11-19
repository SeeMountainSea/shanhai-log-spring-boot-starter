package com.shanhai.log.annotation;

import com.shanhai.log.utils.BmBLevel;
import com.shanhai.log.utils.LogsLevel;

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
     * 日志级别
     * @see LogsLevel
     */
    String level() default LogsLevel.DATA_QUERY_OPERATE;
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
     * 本次请求是否为文件下载
     * @return
     */
    boolean fileDownload() default false;
    /**
     * 本次请求忽略响应报文
     * @return
     */
    boolean ignoreResponse() default false;
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
     * BMB日志级别
     * @see BmBLevel
     * @return
     */
    String bmbLevel() default BmBLevel.SYS_USER;

    /**
     * 是否启用数据差异比对
     * @return
     */
    boolean diffData() default false;
}
