package com.yzj.alzassistant.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.yzj.alzassistant.config.CosClientConfig;
import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.exception.ThrowUtils;
import com.yzj.alzassistant.manager.CosManager;
import com.yzj.alzassistant.model.dto.file.ChatAttachmentUploadResult;
import com.yzj.alzassistant.model.enums.ChatAttachmentType;
import com.yzj.alzassistant.service.ChatAttachmentUploadService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * 聊天附件：通用文件上传至 COS /chat/ 前缀
 */
@Service
@Slf4j
public class ChatAttachmentUploadServiceImpl implements ChatAttachmentUploadService {

    private static final long MAX_BYTES = 20L * 1024 * 1024;

    private static final Set<String> ALLOWED_EXT = Set.of(
            "png", "jpg", "jpeg", "gif", "webp",
            "pdf", "txt", "md",
            "doc", "docx", "xls", "xlsx"
    );

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Override
    public ChatAttachmentUploadResult uploadChatAttachment(MultipartFile file) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        String originalFilename = file.getOriginalFilename();
        ThrowUtils.throwIf(StrUtil.isBlank(originalFilename), ErrorCode.PARAMS_ERROR, "文件名不能为空");
        long size = file.getSize();
        ThrowUtils.throwIf(size <= 0, ErrorCode.PARAMS_ERROR, "文件无效");
        ThrowUtils.throwIf(size > MAX_BYTES, ErrorCode.PARAMS_ERROR, "单文件不超过 20MB");

        String suffix = FileUtil.getSuffix(originalFilename).toLowerCase(Locale.ROOT);
        ThrowUtils.throwIf(StrUtil.isBlank(suffix) || !ALLOWED_EXT.contains(suffix),
                ErrorCode.PARAMS_ERROR, "不支持的文件格式");

        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), RandomUtil.randomString(16), suffix);
        String uploadPath = String.format("/chat/%s", uploadFilename);

        File tempFile = null;
        try {
            tempFile = File.createTempFile("chat_att_", "." + suffix);
            file.transferTo(tempFile);

            cosManager.putObject(uploadPath, tempFile);

            ChatAttachmentUploadResult result = new ChatAttachmentUploadResult();
            result.setUrl(cosClientConfig.getHost() + uploadPath);
            result.setFileName(FileUtil.getName(originalFilename));
            result.setSize(size);
            result.setType(ChatAttachmentType.fromExtension(suffix).getCode());
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("聊天附件上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
            }
        }
    }
}
