package com.yzj.alzassistant.model.dto.assessmentRecord;

import lombok.Data;
import java.io.Serializable;

/**
 * 评估记录创建请求
 */
@Data
public class AssessmentRecordAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

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
}
