package com.yzj.alzassistant.model.dto.aiModel;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 添加大模型请求类
 */
@Data
public class AiModelAddRequest implements Serializable {

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型唯一标识（如gpt-4、claude-3等）
     */
    private String modelKey;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * 模型类型（openai/claude/custom等）
     */
    private String modelType;

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

    private static final long serialVersionUID = 1L;
}

