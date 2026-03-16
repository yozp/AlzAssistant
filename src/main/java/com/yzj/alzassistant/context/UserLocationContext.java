package com.yzj.alzassistant.context;

import cn.hutool.core.util.StrUtil;

/**
 * 用户位置上下文（请求级），用于本次请求内将前端传来的用户实时位置提供给地图工具。
 * 仅当次请求有效，不持久化。
 */
public final class UserLocationContext {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    /**
     * 设置当前请求的用户位置（高德坐标系 经度,纬度）
     */
    public static void set(String lngLat) {
        HOLDER.set(lngLat);
    }

    /**
     * 获取当前请求的用户位置，若无则返回 null
     */
    public static String get() {
        String v = HOLDER.get();
        return StrUtil.isNotBlank(v) ? v : null;
    }

    /**
     * 清除当前请求的用户位置，避免线程复用导致泄漏
     */
    public static void clear() {
        HOLDER.remove();
    }

    private UserLocationContext() {}
}
