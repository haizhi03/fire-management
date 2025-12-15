package com.fire.management.annotation;

import java.lang.annotation.*;

/**
 * 权限验证注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 需要的权限标识
     */
    String value();
}
