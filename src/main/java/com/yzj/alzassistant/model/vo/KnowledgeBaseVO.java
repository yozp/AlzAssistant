package com.yzj.alzassistant.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库文档视图对象
 */
@Data
public class KnowledgeBaseVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档内容（可能截断）
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

    /**
     * 上传用户id（管理员）
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


