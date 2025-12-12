package com.yzj.alzassistant.model.dto.knowledgeBase;

import com.yzj.alzassistant.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询知识库文档请求类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBaseQueryRequest extends PageRequest implements Serializable {

    /**
     * 文档标题（模糊查询）
     */
    private String title;

    /**
     * 文档分类
     */
    private String category;

    /**
     * 状态：active/inactive
     */
    private String status;

    /**
     * 文件类型
     */
    private String fileType;

    private static final long serialVersionUID = 1L;
}


