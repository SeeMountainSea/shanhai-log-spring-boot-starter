package com.wangshanhai.examples.service;

import com.shanhai.log.annotation.RequestLog;
import org.springframework.stereotype.Service;

/**
 * 多参数测试
 */
@Service
public class ManyParamService {

    @RequestLog(module = "ManyParamService", message = "多入参测试X")
    public String test(String logConfig,String body){
        return "success";
    }
}
