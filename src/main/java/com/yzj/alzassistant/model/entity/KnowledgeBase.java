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
 * 知识库文档 实体类。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("knowledge_base")
public class KnowledgeBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
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
    @Column("fileType")
    private String fileType;

    /**
     * 文件路径
     */
    @Column("filePath")
    private String filePath;

    /**
     * 文件URL（对象存储）
     */
    @Column("fileUrl")
    private String fileUrl;

    /**
     * 状态：active/inactive
     */
    private String status;

    /**
     * 上传用户id（管理员）
     */
    @Column("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    @Column("editTime")
    private LocalDateTime editTime;

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
