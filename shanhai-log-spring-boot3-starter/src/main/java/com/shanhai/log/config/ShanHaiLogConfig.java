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
     * 是否启用组件
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
     * 启用山海Log
     */
    private boolean shanhaiLogStatus=true;
    /**
     * 启用全局跟踪Log
     */
    private boolean shanhaiTraceLogStatus=true;
}
