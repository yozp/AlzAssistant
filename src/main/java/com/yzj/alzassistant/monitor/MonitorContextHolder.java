package com.yzj.alzassistant.monitor;

import lombok.extern.slf4j.Slf4j;

/**
 * 监控上下文持有者类
 * 用于在应用监控中传递上下文信息，如用户ID、应用ID等
 */
@Slf4j
public class MonitorContextHolder {

    /**
     * 线程本地变量，用于存储当前线程的监控上下文
     */
    private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置监控上下文
     */
    public static void setContext(MonitorContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取当前监控上下文
     */
    public static MonitorContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除监控上下文
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }
}
