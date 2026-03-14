package com.yzj.alzassistant.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 量表视图对象
 */
@Data
public class AssessmentScaleVO implements Serializable {

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
     * 最小总分
     */
    private Integer totalScoreMin;

    /**
     * 最大总分
     */
    private Integer totalScoreMax;

    /**
     * 版本号
     */
    private Integer versionNo;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建人(管理员)id
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

