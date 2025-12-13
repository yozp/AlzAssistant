package com.yzj.alzassistant.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 大模型视图对象
 */
@Data
public class AiModelVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型唯一标识（如gpt-4、claude-3等）
     */
    private String modelKey;

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * 模型类型（openai/claude/custom等）
     */
    private String modelType;

    /**
     * 状态：active/inactive
     */
    private String status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 最大token数
     */
    private Integer maxTokens;

    /**
     * 温度参数（0-2）
     */
    private BigDecimal temperature;

    /**
     * top_p参数（0-1）
     */
    private BigDecimal topP;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 创建用户id（管理员）
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    private LocalDateTime editTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}

