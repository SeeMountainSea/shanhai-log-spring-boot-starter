package com.shanhai.log.component;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.shanhai.log.annotation.RequestLog;
import com.shanhai.log.service.RequestLogService;
import com.shanhai.log.service.impl.DefaultRequestLogService;
import com.shanhai.log.utils.Logger;
import com.shanhai.log.utils.RequestLogInfo;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用日志处理(支持Spel)
 */
@Aspect
@Configuration
public class RequestLogAspect {
    @Value("${spring.application.name:XXX}")
    private String applicationName;
    @Value("${server.port:8080}")
    private String applicationPort;

    @Bean
    @ConditionalOnMissingBean
    public RequestLogService generateDefaultRequestLogService() {
        return new DefaultRequestLogService(applicationName,applicationPort);
    };

    @Autowired
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
        RequestLog socLog = method.getAnnotation(RequestLog.class);
        requestLogInfo.setModule(socLog.module());
        requestLogInfo.setLevel(socLog.level());
        requestLogInfo.setReqTime(DateUtil.date(beginTime));
        requestLogInfo.setRespTime(DateUtil.date(endTime));
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        requestLogInfo.setReqSourceIp(requestLogService.getReqSourceIp(request));
        requestLogInfo.setReqUrl(request.getRequestURI());
        requestLogInfo.setHttpMethod(request.getMethod());
        if (socLog.message().contains("#{")) {
            requestLogInfo.setMessage(executeTemplate(socLog.message(), point));
        } else {
            requestLogInfo.setMessage(socLog.message());
        }
        if (socLog.currentUser().contains("#{")) {
            requestLogInfo.setCurrentUser(executeTemplate(socLog.currentUser(), point));
        } else {
            requestLogInfo.setCurrentUser(requestLogService.getCurrentUser(request));
        }
        if (respContent != null) {
            requestLogInfo.setRespInfo(JSONUtil.toJsonStr(respContent));
            requestLogInfo.setRespStatusCode(200);
        }
        if (ex != null) {
            final Writer writer = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            requestLogInfo.setRespInfo(writer.toString());
            requestLogInfo.setRespStatusCode(500);
        }
        String reqInfo="-";
        Map<String, String> rtnMap = converMap(request.getParameterMap());
        if(rtnMap!=null&&rtnMap.size()>0){
            reqInfo= "[form-data]"+JSONObject.toJSONString(rtnMap);
        }else {
            //获取请求参数
            Object[] args = point.getArgs();
            if(args.length>0&&"POST".equals(requestLogInfo.getHttpMethod())){
                reqInfo="[body]"+JSONUtil.toJsonStr(args[0]);
            }
        }
        requestLogInfo.setReqInfo(reqInfo);
        requestLogInfo.setExtLogInfo(requestLogService.getExtLogInfo(request));
        requestLogService.saveLog(requestLogInfo);
        if (consoleShow) {
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
            for (int len = 0; len < params.length; len++) {
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
            String[] values=paramMap.get(key);
            rtnMap.put(key, String.join(",",values));
        }
        return rtnMap;
    }

}
