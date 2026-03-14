package com.yzj.alzassistant.model.dto.assessmentScale;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新量表请求类
 */
@Data
public class AssessmentScaleUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 量表名称
     */
    private String scaleName;

    /**
     * 量表简介
     */
    private String scaleIntro;

    /**
     * 量表内容(JSON，含题目/选项/分数)
     */
    private String contentJson;

    /**
     * 评分规则(JSON，按分数段映射风险等级/评估结果/建议)
     */
    private String ruleJson;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}

