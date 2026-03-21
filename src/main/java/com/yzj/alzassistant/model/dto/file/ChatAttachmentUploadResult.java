package com.yzj.alzassistant.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天附件上传结果
 */
@Data
public class ChatAttachmentUploadResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 可访问的完整 URL
     */
    private String url;

    /** 
     * 原始文件名 
     */
    private String fileName;

    /** 
     * 文件大小（字节） 
     */
    private Long size;

    /**
     * 附件类型：image / document / spreadsheet / text
     */
    private String type;
}
