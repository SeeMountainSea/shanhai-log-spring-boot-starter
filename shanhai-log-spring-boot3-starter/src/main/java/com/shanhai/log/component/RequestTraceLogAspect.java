package com.shanhai.log.component;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.shanhai.log.config.ShanHaiLogConfig;
import com.shanhai.log.service.RequestLogService;
import com.shanhai.log.service.impl.DefaultRequestLogService;
import com.shanhai.log.utils.Logger;
import com.shanhai.log.utils.RequestLogInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 全局跟踪日志处理
 * @author Shmily
 */
@Aspect
@Configuration
@EnableConfigurationProperties(ShanHaiLogConfig.class)
public class RequestTraceLogAspect {

    @Value("${spring.application.name:XXX}")
    private String applicationName;
    @Value("${server.port:8080}")
    private String applicationPort;
    @Autowired
    private ShanHaiLogConfig shanHaiLogConfig;
    @Bean
    @ConditionalOnMissingBean
    public RequestLogService generateDefaultRequestLogService() {
        return new DefaultRequestLogService(applicationName,applicationPort);
    };
    @Autowired
    @Lazy
    private RequestLogService requestLogService;
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PutMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PathVariable)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object checkResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行方法
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        if(result!=null){
            buildLog(joinPoint, beginTime, endTime, result, null);
        }else{
            buildLog(joinPoint, beginTime, endTime, "StreamReq is not record ResponseContent", null);
        }
        return result;
    }

    @AfterThrowing(value = "pointCut()", throwing = "ex")
    public void exceptionServiceActions(JoinPoint point, Exception ex) {
        buildLog(point, System.currentTimeMillis(), System.currentTimeMillis(), null, ex);
    }

    /**
     * 构建标准化日志
     *
     * @param point       切点
     * @param beginTime   开始调用时间
     * @param endTime     结束调用时间
     * @param respContent 响应报文
     * @param ex          异常信息
     */
    public void buildLog(JoinPoint point, long beginTime, long endTime, Object respContent, Exception ex) {
        RequestLogInfo requestLogInfo = new RequestLogInfo();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        requestLogInfo.setModule("Project_All");
        requestLogInfo.setLevel("Level_All");
        requestLogInfo.setReqTime(DateUtil.date(beginTime));
        requestLogInfo.setRespTime(DateUtil.date(endTime));
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();
        String contentType=request.getContentType();
        if(!StrUtil.isBlank(contentType)){
            contentType=contentType.toLowerCase(Locale.ENGLISH);
        }else{
            contentType="";
        }
        requestLogInfo.setReqSourceIp(requestLogService.getReqSourceIp(request));
        requestLogInfo.setAgentInfo(request.getHeader("User-Agent"));
        requestLogInfo.setReqUrl(request.getRequestURI());
        requestLogInfo.setHttpMethod(request.getMethod());
        requestLogInfo.setContentType(contentType);
        requestLogInfo.setMessage("-");
        requestLogInfo.setCurrentUser(requestLogService.getCurrentUser(request));
        if (respContent != null) {
            try {
                requestLogInfo.setRespInfo(JSONUtil.toJsonStr(respContent));
            }catch (Exception e){
                requestLogInfo.setRespInfo(String.valueOf(respContent));
            }
            requestLogInfo.setRespStatusCode(200);
        }
        if (ex != null) {
            final Writer writer = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            requestLogInfo.setRespInfo(writer.toString());
            requestLogInfo.setRespStatusCode(500);
        }
        Map<String, String> rtnMap = converMap(request.getParameterMap());
        JSONObject postData=new JSONObject();
        if(rtnMap.size()>0){
            if("GET".equals(request.getMethod())){
                requestLogInfo.setReqInfo(JSONObject.toJSONString(rtnMap));
            }
            if("POST".equals(request.getMethod())){
                if(contentType.contains("multipart/form-data") ||contentType.contains("application/x-www-form-urlencoded")){
                    try{
                        requestLogInfo.setReqInfo(JSONObject.toJSONString(rtnMap));
                    }catch (Exception e){
                        requestLogInfo.setReqInfo("ContentType is error,no find log!");
                    }

                }
                //适配post json|xml类型请求，但URL包含参数的情况
                if(contentType.contains("application/json") ||contentType.contains("application/xml")){
                    try{
                        postData.put("urlParam",JSONObject.toJSONString(rtnMap));
                    }catch (Exception e){
                        postData.put("urlParam","ContentType is error,no find log!");
                    }
                }
            }
        }

        if("POST".equals(request.getMethod())&&(contentType.contains("application/json") ||contentType.contains("application/xml"))){
            Object[] args = point.getArgs();
            if(args!=null&&args.length>0){
                if(args.length==1){
                    if(contentType.contains("application/json")){
                        try{
                            if(!shanHaiLogConfig.getIgnoreRequestParams().contains(args[0].getClass().getName())){
                                if(!(args[0] instanceof HttpServletRequest || args[0] instanceof HttpServletResponse)){
                                    postData.put("bodyParam",JSONObject.toJSONString(String.valueOf(args[0])));
                                }
                            }
                        }catch (Exception e){
                            postData.put("bodyParam","ContentType is error,no find log!");
                        }
                    }else{
                        try{
                            postData.put("bodyParam",String.valueOf(args[0]));
                        }catch (Exception e){
                            postData.put("bodyParam","ContentType is error,no find log!");
                        }
                    }
                }else{
                    Map<String,Object> bodyParams=new HashMap<>();
                    String[] parameterNames=  ((MethodSignature) point.getSignature()).getParameterNames();
                    for(int i=0;i< args.length;i++){
                        if(args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse
                                ||rtnMap.containsKey(parameterNames[i])){
                            continue;
                        }
                        if(shanHaiLogConfig.getIgnoreRequestParams().contains(args[i].getClass().getName())){
                            continue;
                        }
                        bodyParams.put(parameterNames[i],args[i]);
                    }
                    if(contentType.contains("application/json")){
                        try{
                            postData.put("bodyParam",JSONObject.toJSONString(bodyParams));
                        }catch (Exception e){
                            postData.put("bodyParam","ContentType is error,no find log!");
                        }
                    }else{
                        postData.put("bodyParam",bodyParams);
                    }
                }

            }else{
                postData.put("bodyParam","-");
            }
            requestLogInfo.setReqInfo(postData.toJSONString());
        }
        requestLogInfo.setFileUploadRequest(false);
        requestLogInfo.setFileReqInfo("-");
        if(StrUtil.isBlank(requestLogInfo.getReqInfo())){
            requestLogInfo.setReqInfo("-");
        }
        requestLogInfo.setExtLogInfo(requestLogService.getExtLogInfo(request));
        if(requestLogInfo.getReqTime()!=null
                &&requestLogInfo.getRespTime()!=null){
            requestLogInfo.setCost(DateUtil.between(requestLogInfo.getReqTime(),
                    requestLogInfo.getRespTime(), DateUnit.MS));
        }else{
            requestLogInfo.setCost(-1L);
        }
        requestLogService.saveLog(requestLogInfo);
        if (shanHaiLogConfig.isConsoleShow()) {
            Logger.info("[RequestLog]-{}", JSONObject.toJSONString(requestLogInfo));
        }
    }
    /**
     * 转换为MAP
     * @param paramMap
     * @return
     */
    private Map<String, String> converMap(Map<String, String[]> paramMap) {
        Map<String, String> rtnMap = new HashMap<String, String>();
        for (String key : paramMap.keySet()) {
            try{
                JSONObject reqInfo= JSONObject.parseObject(key);
                for(String rkey:reqInfo.keySet()){
                    rtnMap.put(rkey, reqInfo.getString(rkey));
                }
            }catch (Exception e){
                String[] values=paramMap.get(key);
                rtnMap.put(key, String.join(",",values));
            }
        }
        return rtnMap;
    }
}
