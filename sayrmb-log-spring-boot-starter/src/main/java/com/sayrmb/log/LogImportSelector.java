package com.sayrmb.log;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 自定义注解扫描的自动配置组件
 */
public class LogImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{
                "com.sayrmb.log.component.RequestLogAspect"
        };
    }
}
