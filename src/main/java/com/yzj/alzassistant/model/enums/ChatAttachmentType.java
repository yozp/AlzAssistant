package com.yzj.alzassistant.model.enums;

import cn.hutool.core.util.StrUtil;

import java.util.Locale;
import java.util.Set;

/**
 * 聊天附件业务类型（与扩展名对应，存入 attachments JSON 的 type 字段）。
 */
public enum ChatAttachmentType {

    /**
     * 图片
     */
    IMAGE("image"),
    /**
     * 文档（PDF、Office 文档等）
     */
    DOCUMENT("document"),
    /**
     * 表格
     */
    SPREADSHEET("spreadsheet"),
    /**
     * 纯文本 / Markdown
     */
    TEXT("text");

    private final String code;

    ChatAttachmentType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 根据文件扩展名（不含点）推断类型，与上传白名单一致。
     */
    public static ChatAttachmentType fromExtension(String ext) {
        if (StrUtil.isBlank(ext)) {
            return DOCUMENT;
        }
        String e = ext.toLowerCase(Locale.ROOT);
        if (Set.of("png", "jpg", "jpeg", "gif", "webp").contains(e)) {
            return IMAGE;
        }
        if (Set.of("pdf", "doc", "docx").contains(e)) {
            return DOCUMENT;
        }
        if (Set.of("xls", "xlsx").contains(e)) {
            return SPREADSHEET;
        }
        if (Set.of("txt", "md").contains(e)) {
            return TEXT;
        }
        return DOCUMENT;
    }
}
