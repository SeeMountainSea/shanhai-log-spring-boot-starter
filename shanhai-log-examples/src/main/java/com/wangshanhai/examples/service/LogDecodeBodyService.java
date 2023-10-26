package com.wangshanhai.examples.service;

import com.alibaba.fastjson.JSONObject;
import com.wangshanhai.guard.service.DecodeBodyService;
import org.springframework.stereotype.Service;

/**
 * 测试报文拦截
 */
@Service
public class LogDecodeBodyService implements DecodeBodyService {
    @Override
    public String decodeRequestBody(String body) {
        JSONObject resp=new JSONObject();
        resp.put("guard","lisi");
        return resp.toJSONString();
    }
}
