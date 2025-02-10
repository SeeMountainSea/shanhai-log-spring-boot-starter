package com.shanhai.log.annotation;

import com.shanhai.log.LogTraceImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用山海跟踪Log
 * @author Shmily
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LogTraceImportSelector.class)
public @interface EnableShanHaiTraceLog {
}
