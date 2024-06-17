package com.shanhai.log.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据防护配置
 * @author Shmily
 */
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
     * 启用山海Log
     */
    private boolean shanhaiLogStatus=true;
    /**
     * 启用全局跟踪Log
     */
    private boolean shanhaiTraceLogStatus=true;

    public List<String> getIgnoreRequestParams() {
        return ignoreRequestParams;
    }

    public void setIgnoreRequestParams(List<String> ignoreRequestParams) {
        this.ignoreRequestParams = ignoreRequestParams;
    }

    public boolean isConsoleShow() {
        return consoleShow;
    }

    public void setConsoleShow(boolean consoleShow) {
        this.consoleShow = consoleShow;
    }
}
