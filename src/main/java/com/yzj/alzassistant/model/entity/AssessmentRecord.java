package com.yzj.alzassistant.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估记录 实体类。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("assessment_record")
public class AssessmentRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户id
     */
    @Column("userId")
    private Long userId;

    /**
     * 关联会话id
     */
    @Column("appId")
    private Long appId;

    /**
     * 症状描述
     */
    @Column("symptomDesc")
    private String symptomDesc;

    /**
     * 评估人：1-AI 2-量表
     */
    @Column("assessorType")
    private Integer assessorType;

    /**
     * 评估结果
     */
    @Column("assessmentResult")
    private String assessmentResult;

    /**
     * 风险等级：0-无 1-低 2-中 3-高
     */
    @Column("riskLevel")
    private Integer riskLevel;

    /**
     * 建议
     */
    private String suggestion;

    /**
     * 量表总分
     */
    @Column("totalScore")
    private Integer totalScore;

    /**
     * 量表id
     */
    @Column("scaleId")
    private Long scaleId;

    /**
     * 量表名称快照
     */
    @Column("scaleNameSnapshot")
    private String scaleNameSnapshot;

    /**
     * 量表版本快照
     */
    @Column("scaleVersionNo")
    private Integer scaleVersionNo;

    /**
     * 作答详情(JSON)
     */
    @Column("answerJson")
    private String answerJson;

    /**
     * 命中规则快照(JSON)
     */
    @Column("ruleSnapshotJson")
    private String ruleSnapshotJson;

    /**
     * AI来源：1-agent报告
     */
    @Column("aiSourceType")
    private Integer aiSourceType;

    /**
     * 报告链接
     */
    @Column("reportUrl")
    private String reportUrl;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

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
