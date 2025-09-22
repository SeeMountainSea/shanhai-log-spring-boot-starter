package com.shanhai.log.utils;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 标准化操作日志
 * @author log
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestLogInfo implements Serializable {
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
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date reqTime;
    /**
     * 请求结束时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
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
     * 请求报文美化版本（开启respJsonPrettyFormat可用）
     */
    private JsonNode reqInfoPretty;
    /**
     * 响应报文
     */
    private String respInfo;
    /**
     * 响应报文美化版本（开启reqJsonPrettyFormat可用）
     */
    private JsonNode respInfoPretty;
    /**
     * 响应状态码
     */
    private Integer respStatusCode;
    /**
     * HTTP请求类型
     */
    private String httpMethod;
    /**
     * HTTP请求内容类型
     */
    private String contentType;
    /**
     * 是否为上传文件请求
     */
    private Boolean fileUploadRequest;
    /**
     * 文件上传清单
     */
    private String fileReqInfo;
    /**
     * 文件上传清单美化版本（开启reqJsonPrettyFormat可用）
     */
    private JsonNode fileReqInfoPretty;
    /**
     * 是否为文件下载请求
     */
    private Boolean fileDownloadRequest;
    /**
     * 文件下载清单
     */
    private String fileDownloadInfo;
    /**
     * 接口调用时长
     */
    private Long cost;
    /**
     * 服务器节点标识
     */
    private String serverNode;
    /**
     * 用户日志保护标识
     */
    private String userGuardFlag;
    /**
     * 自定义扩展日志信息
     */
    private Map<String,Object> extLogInfo;
}
