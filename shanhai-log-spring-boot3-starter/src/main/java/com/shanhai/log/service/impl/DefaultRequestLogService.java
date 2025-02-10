package com.shanhai.log.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileMode;
import com.alibaba.fastjson.JSONObject;
import com.shanhai.log.service.RequestLogService;
import com.shanhai.log.utils.RequestLogInfo;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认日志存储实现
 */
public class DefaultRequestLogService implements RequestLogService {
    private List<String> logLines;
    private String applicationName;
    private String applicationPort;

    public DefaultRequestLogService(String applicationName, String applicationPort) {
        this.applicationName = applicationName;
        this.applicationPort = applicationPort;
        this.logLines=new ArrayList<>();
    }

    @Override
    public void saveLog(RequestLogInfo requestLogInfo) {
        logLines.add(JSONObject.toJSONString(requestLogInfo));
        if(logLines.size()>10){
            String tmpdir=System.getProperty("java.io.tmpdir");
            String realPath=tmpdir+"/RequestLog_"+applicationName+"_"+applicationPort+"_"+ DateUtil.today() +".log";
            File f=FileUtil.file(realPath);
            if(f.exists()){
                FileUtil.appendUtf8Lines(logLines,realPath);
                logLines=new ArrayList<>();
            }else{
                FileUtil.createRandomAccessFile(new File(realPath), FileMode.rw);
                FileUtil.appendUtf8Lines(logLines,realPath);
            }
        }
    }

    @Override
    public String getReqSourceIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    @Override
    public Map<String, Object> getExtLogInfo(HttpServletRequest request) {
        return new HashMap<>();
    }

    @Override
    public String getCurrentUser(HttpServletRequest request) {
        return "Default";
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationPort() {
        return applicationPort;
    }

    public void setApplicationPort(String applicationPort) {
        this.applicationPort = applicationPort;
    }
}
