package com.shanhai.log.annotation;

import com.shanhai.log.LogImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用山海Log
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LogImportSelector.class)
public @interface EnableShanHaiLog {
}
