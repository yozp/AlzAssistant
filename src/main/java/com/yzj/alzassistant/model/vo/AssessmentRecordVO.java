package com.yzj.alzassistant.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评估记录视图
 */
@Data
public class AssessmentRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 关联会话id
     */
    private Long appId;

    /**
     * 症状描述
     */
    private String symptomDesc;

    /**
     * 评估人：1-AI 2-量表
     */
    private Integer assessorType;

    /**
     * 评估结果
     */
    private String assessmentResult;

    /**
     * 风险等级：0-无 1-低 2-中 3-高
     */
    private Integer riskLevel;

    /**
     * 建议
     */
    private String suggestion;

    /**
     * 量表总分
     */
    private Integer totalScore;

    /**
     * 量表id
     */
    private Long scaleId;

    /**
     * 量表名称快照
     */
    private String scaleNameSnapshot;

    /**
     * 量表版本快照
     */
    private Integer scaleVersionNo;

    /**
     * 作答详情(JSON)
     */
    private String answerJson;

    /**
     * 命中规则快照(JSON)
     */
    private String ruleSnapshotJson;

    /**
     * AI来源：1-agent报告
     */
    private Integer aiSourceType;

    /**
     * 报告链接
     */
    private String reportUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
