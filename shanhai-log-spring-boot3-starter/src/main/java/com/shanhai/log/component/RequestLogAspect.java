package com.shanhai.log.component;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.shanhai.log.annotation.RequestLog;
import com.shanhai.log.config.ShanHaiLogConfig;
import com.shanhai.log.diff.DiffItem;
import com.shanhai.log.diff.HttpDiffResponse;
import com.shanhai.log.diff.ObjectDiffComparator;
import com.shanhai.log.service.RequestLogService;
import com.shanhai.log.service.impl.DefaultRequestLogService;
import com.shanhai.log.utils.JsonUnescapeUtil;
import com.shanhai.log.utils.Logger;
import com.shanhai.log.utils.RequestLogInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
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
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 通用日志处理(支持Spel)
 * @author Shmily
 */
@Aspect
@Configuration
@EnableConfigurationProperties(ShanHaiLogConfig.class)
public class RequestLogAspect {
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
    /**
     * 是否将日志打印控制台
     */
    @Value("${shanhai.log.consoleShow:false}")
    private boolean consoleShow;

    @Pointcut("@annotation(com.shanhai.log.annotation.RequestLog)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object checkResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行方法
        Object result = joinPoint.proceed();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        if(shanHaiLogConfig.getIgnoreRequestUri().stream().anyMatch(blackPattern -> pathMatcher.match(blackPattern,request.getRequestURI()))){
            return result;
        }
        long endTime = System.currentTimeMillis();
        if(result!=null){
            buildLog(joinPoint, beginTime, endTime, result, null);
            if(result instanceof HttpDiffResponse){
                if(shanHaiLogConfig.isOverrideHttpDiffResponse()){
                    return requestLogService.getOverrideHttpDiffResponse(result);
                }
            }
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
        RequestLog socLog = method.getAnnotation(RequestLog.class);
        requestLogInfo.setModule(socLog.module());
        requestLogInfo.setLevel(socLog.level());
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
        if (socLog.message().contains("#{")) {
            requestLogInfo.setMessage(executeTemplate(socLog.message(), point));
        } else {
            requestLogInfo.setMessage(socLog.message());
        }

        if (respContent != null) {
            if(socLog.fileDownload()||socLog.ignoreResponse()){
                requestLogInfo.setRespInfo("Ignore response");
            }else{
                try{
                    requestLogInfo.setRespInfo(JSONUtil.toJsonStr(respContent));
                    if(shanHaiLogConfig.isRespJsonPrettyFormat()){
                        requestLogInfo.setRespInfoPretty(JsonUnescapeUtil.parseNestedJson(requestLogInfo.getRespInfo()));
                    }
                }catch (Exception e){
                    requestLogInfo.setRespInfo(String.valueOf(respContent));
                }
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
                try {
                    requestLogInfo.setReqInfo(JSONObject.toJSONString(rtnMap));
                    if(shanHaiLogConfig.isReqJsonPrettyFormat()){
                        requestLogInfo.setReqInfoPretty(JsonUnescapeUtil.parseNestedJson(requestLogInfo.getReqInfo()));
                    }
                }catch (Exception e){
                    requestLogInfo.setReqInfo("json format exception,source:"+rtnMap);
                }
            }
            if("POST".equals(request.getMethod())){
               if(contentType.contains("multipart/form-data") ||contentType.contains("application/x-www-form-urlencoded")){
                   try{
                       requestLogInfo.setReqInfo(JSONObject.toJSONString(rtnMap));
                       if(shanHaiLogConfig.isReqJsonPrettyFormat()){
                           requestLogInfo.setReqInfoPretty(JsonUnescapeUtil.parseNestedJson(requestLogInfo.getReqInfo()));
                       }
                   }catch (Exception e){
                       requestLogInfo.setReqInfo("json format exception,source:"+rtnMap);
                   }

               }
               //适配post json|xml类型请求，但URL包含参数的情况
               if(contentType.contains("application/json") ||contentType.contains("application/xml")){
                   try{
                       postData.put("urlParam",JSONObject.parseObject(JSONObject.toJSONString(rtnMap)));
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
                                    postData.put("bodyParam",JSONObject.parseObject(JSONObject.toJSONString(args[0])));
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
                            postData.put("bodyParam",JSONObject.parseObject(JSONObject.toJSONString(bodyParams)));
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
            try{
                if(shanHaiLogConfig.isReqJsonPrettyFormat()){
                    requestLogInfo.setReqInfoPretty(JsonUnescapeUtil.parseNestedJson(requestLogInfo.getReqInfo()));
                }
            }catch (Exception e){
                Logger.error("json format exception,source:"+requestLogInfo.getReqInfo());
            }
        }
        try {
            if(socLog.fileUpload()){
                Collection<Part> parts= request.getParts();
                if(parts!=null && parts.size()>0){
                    Map<String, List<String>> fileReqInfo=new HashMap<>();
                    for(Part p:parts){
                        if(!StrUtil.isBlank(p.getSubmittedFileName())){
                            String k=p.getName();
                            List<String> files= fileReqInfo.get(k);
                            if(files!=null){
                                files.add(p.getSubmittedFileName());
                                fileReqInfo.put(k,files);
                            }else{
                                files=new ArrayList<>();
                                files.add(p.getSubmittedFileName());
                                fileReqInfo.put(k,files);
                            }
                        }
                    }
                    if(fileReqInfo.keySet().size()>0){
                        requestLogInfo.setFileReqInfo(JSONObject.toJSONString(fileReqInfo));
                        try{
                            if(shanHaiLogConfig.isReqJsonPrettyFormat()){
                                requestLogInfo.setFileReqInfoPretty(JsonUnescapeUtil.parseNestedJson(requestLogInfo.getFileReqInfo()));
                            }
                        }catch (Exception e){
                            Logger.error("json format exception,source:"+fileReqInfo);
                        }
                        requestLogInfo.setFileUploadRequest(true);
                    }else{
                        requestLogInfo.setFileUploadRequest(false);
                        requestLogInfo.setFileReqInfo("-");
                    }
                }
            }else{
                requestLogInfo.setFileUploadRequest(false);
                requestLogInfo.setFileReqInfo("-");
            }
        }catch (Exception e){
            requestLogInfo.setFileUploadRequest(false);
            requestLogInfo.setFileReqInfo("-");
        }
        try{
            if(socLog.fileDownload()&&socLog.fileDownloadLog()){
                requestLogInfo.setFileDownloadRequest(true);
                requestLogInfo.setFileDownloadInfo(requestLogService.getFileDownLoadLog(requestLogInfo,socLog.fileDownloadLogRule()));
            }
        }catch (Exception e){
            requestLogInfo.setFileDownloadRequest(false);
            requestLogInfo.setFileDownloadInfo("-");
        }
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
        if (socLog.currentUser().contains("#{")) {
            requestLogInfo.setCurrentUser(executeTemplate(socLog.currentUser(), point));
        } else {
            if(socLog.queryUserBySelf()){
                requestLogInfo.setCurrentUser(requestLogService.getCurrentUser(requestLogInfo));
            }else{
                requestLogInfo.setCurrentUser(requestLogService.getCurrentUser(request));
            }
        }
        requestLogInfo.setUserGuardFlag(socLog.bmbLevel());
        requestLogInfo.setServerNode(requestLogService.getCurrentServerNodeFlag(request));
        if(respContent instanceof HttpDiffResponse&&socLog.diffData()){
            try{
                HttpDiffResponse result=(HttpDiffResponse)respContent;
                List<DiffItem> diffs = ObjectDiffComparator.compare(result.getSourceData(),  result.getTargetData());
                requestLogInfo.setDiffItems(diffs);
                requestLogInfo.setRespInfo(JSONObject.toJSONString(result));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(socLog.dataMasking()){
            requestLogService.saveLog(requestLogInfo,socLog.dataMaskingRule());
        }else{
            requestLogService.saveLog(requestLogInfo);
        }
        if (shanHaiLogConfig.isConsoleShow()) {
            Logger.info("[RequestLog]-{}", JSONObject.toJSONString(requestLogInfo));
        }
    }

    /**
     * 解析SPEL
     *
     * @param message   含SPEL的日志消息
     * @param joinPoint
     * @return
     */
    private String executeTemplate(String message, JoinPoint joinPoint) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            String[] params = discoverer.getParameterNames(method);
            Object[] args = joinPoint.getArgs();
            EvaluationContext context = new StandardEvaluationContext();
            for (int len = 0; len < Objects.requireNonNull(params).length; len++) {
                context.setVariable(params[len], args[len]);
            }
            return parser.parseExpression(message, new TemplateParserContext()).getValue(context, String.class);
        } catch (Exception e) {
            Logger.error("[executeTemplate]-msg:{}", e.getMessage());
        }
        return message;
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
