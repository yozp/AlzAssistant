package com.yzj.alzassistant.service;

import com.yzj.alzassistant.model.dto.file.ChatAttachmentUploadResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 聊天附件上传到 COS
 */
public interface ChatAttachmentUploadService {

    /**
     * 上传单个附件，路径前缀 /chat/
     */
    ChatAttachmentUploadResult uploadChatAttachment(MultipartFile file);
}
