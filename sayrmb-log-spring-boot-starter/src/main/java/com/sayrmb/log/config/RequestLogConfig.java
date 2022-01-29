// package com.sayrmb.log.config;

// import com.sayrmb.log.service.RequestLogService;
// import com.sayrmb.log.service.impl.DefaultRequestLogService;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// @ConditionalOnExpression("${sayhi.log.enable:false}")
// public class RequestLogConfig {
//     @Value("${spring.application.name:XXX}")
//     private String applicationName;
//     @Value("${server.port:8080}")
//     private String applicationPort;

//     @Bean
//     @ConditionalOnMissingBean
//     public RequestLogService generateDefaultRequestLogService() {
//        return new DefaultRequestLogService(applicationName,applicationPort);
//     };
// }

