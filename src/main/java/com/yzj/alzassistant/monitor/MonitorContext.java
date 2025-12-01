package com.yzj.alzassistant.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 监控上下文类
 * 用于在应用监控中传递上下文信息，如用户ID、应用ID等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorContext implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 应用ID
     */
    private String appId;

    @Serial
    private static final long serialVersionUID = 1L;
}
