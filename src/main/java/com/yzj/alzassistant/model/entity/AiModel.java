package com.yzj.alzassistant.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型管理 实体类。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_model")
public class AiModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 模型名称
     */
    @Column("modelName")
    private String modelName;

    /**
     * 模型唯一标识（如gpt-4、claude-3等）
     */
    @Column("modelKey")
    private String modelKey;

    /**
     * API密钥
     */
    @Column("apiKey")
    private String apiKey;

    /**
     * API基础URL
     */
    @Column("baseUrl")
    private String baseUrl;

    /**
     * 模型类型（openai/claude/custom等）
     */
    @Column("modelType")
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
    @Column("maxTokens")
    private Integer maxTokens;

    /**
     * 温度参数（0-2）
     */
    private BigDecimal temperature;

    /**
     * top_p参数（0-1）
     */
    @Column("topP")
    private BigDecimal topP;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 创建用户id（管理员）
     */
    @Column("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
