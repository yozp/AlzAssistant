package com.yzj.alzassistant.model.dto.assessmentScale;

import com.yzj.alzassistant.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询量表请求类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentScaleQueryRequest extends PageRequest implements Serializable {

    /**
     * 量表名称（模糊查询）
     */
    private String scaleName;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建人(管理员)id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}

