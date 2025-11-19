package com.shanhai.log.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据防护配置
 * @author Shmily
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "shanhai.log")
public class ShanHaiLogConfig {
    /**
     * 是否启用控制台打印
     */
    private boolean consoleShow=false;
    /**
     * 忽略范围（类名）
     */
    private List<String> ignoreRequestParams=new ArrayList<>();
    /**
     * 忽略范围（URL）
     */
    private List<String> ignoreRequestUri=new ArrayList<>();
    /**
     * 启用请求报文JSON格式化
     */
    private boolean reqJsonPrettyFormat=false;
    /**
     * 启用请求报文JSON格式化
     */
    private boolean respJsonPrettyFormat=false;
    /**
     * 重写HttpDiffResponse响应内容
     */
    private boolean overrideHttpDiffResponse=false;
}
