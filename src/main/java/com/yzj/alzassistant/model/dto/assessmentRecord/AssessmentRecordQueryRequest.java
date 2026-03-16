package com.yzj.alzassistant.model.dto.assessmentRecord;

import com.yzj.alzassistant.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 评估记录查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AssessmentRecordQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评估人：1-AI 2-量表
     */
    private Integer assessorType;

    /**
     * 风险等级：0-无 1-低 2-中 3-高
     */
    private Integer riskLevel;

    /**
     * 量表id
     */
    private Long scaleId;
}
