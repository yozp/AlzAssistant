package com.yzj.alzassistant.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解类（自定义权限校验注解）
 * 实现统一的接口拦截和权限校验
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有某个角色
     * 表示方法调用所需的角色。默认值为空字符串，表示无需特定角色
     */
    String mustRole() default "";
}