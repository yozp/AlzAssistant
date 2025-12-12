package com.yzj.alzassistant.model.dto.knowledgeBase;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新知识库文档请求类
 */
@Data
public class KnowledgeBaseUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 文档来源
     */
    private String source;

    /**
     * 文档分类
     */
    private String category;

    /**
     * 文件类型：pdf/txt/docx等
     */
    private String fileType;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件URL（对象存储）
     */
    private String fileUrl;

    /**
     * 状态：active/inactive
     */
    private String status;

    private static final long serialVersionUID = 1L;
}


