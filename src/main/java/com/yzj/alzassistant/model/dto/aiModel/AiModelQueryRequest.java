package com.yzj.alzassistant.model.dto.aiModel;

import com.yzj.alzassistant.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询大模型请求类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiModelQueryRequest extends PageRequest implements Serializable {

    /**
     * 模型名称（模糊查询）
     */
    private String modelName;

    /**
     * 模型类型
     */
    private String modelType;

    /**
     * 状态：active/inactive
     */
    private String status;

    private static final long serialVersionUID = 1L;
}

