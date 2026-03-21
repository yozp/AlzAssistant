package com.yzj.alzassistant.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天消息中的单个附件元数据（与前端、DB JSON 一致）
 */
@Data
public class ChatAttachmentItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 可访问的完整 URL
     */
    private String url;

    /**
     * 原始文件名
     */
    private String name;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 附件类型：image / document / spreadsheet / text
     */
    private String type;
}
