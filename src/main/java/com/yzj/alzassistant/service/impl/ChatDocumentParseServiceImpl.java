package com.yzj.alzassistant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.yzj.alzassistant.service.ChatDocumentParseService;
import com.yzj.alzassistant.util.AttachmentUrlDownloader;
import com.yzj.alzassistant.util.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * 下载 URL 文档并调用 {@link DocumentParser} 抽文本。
 */
@Service
@Slf4j
public class ChatDocumentParseServiceImpl implements ChatDocumentParseService {

    /**
     * 最大字节数 50MB
     */
    @Value("${alzassistant.chat-attachment.document.max-bytes:52428800}")
    private int maxBytes;

    /**
     * 最大字符数 80000
     */
    @Value("${alzassistant.chat-attachment.document.max-chars:80000}")
    private int maxChars;

    @Override
    public String parseDocumentFromUrl(String documentUrl, String originalFileName) {
        if (StrUtil.isBlank(documentUrl)) {
            throw new IllegalArgumentException("文档 URL 为空");
        }
        String url = documentUrl.trim();
        String ext = extensionFromUrl(url);
        if (StrUtil.isBlank(ext) && StrUtil.isNotBlank(originalFileName)) {
            ext = extensionFromFileName(originalFileName);
        }
        if (StrUtil.isBlank(ext)) {
            throw new IllegalArgumentException("无法识别文件类型：请在 URL 路径中含扩展名，或在调用时传入原始文件名（含 .docx 等）");
        }
        String normalized = DocumentParser.normalizeExtensionKey(ext);
        if (!DocumentParser.isSupportedExtension(normalized)) {
            throw new IllegalArgumentException("不支持的文档类型: " + ext);
        }
        byte[] data = AttachmentUrlDownloader.download(url, maxBytes);
        String content = DocumentParser.parseDocument(data, normalized);
        if (content == null) {
            content = "";
        }
        content = content.trim();
        if (content.isEmpty()) {
            return "文档解析完成，但未提取到文本内容。";
        }
        if (content.length() > maxChars) {
            return content.substring(0, maxChars) + "\n\n[正文已截断，原始长度约 " + content.length() + " 字符]";
        }
        return content;
    }

    /**
     * 从文件名中提取扩展名
     */
    static String extensionFromFileName(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return "";
        }
        String name = fileName.trim();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot >= name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1);
    }

    /**
     * 从 URL 中提取扩展名
     */
    static String extensionFromUrl(String url) {
        try {
            String path = URI.create(url).getPath();
            if (path == null || path.isEmpty()) {
                return "";
            }
            int slash = path.lastIndexOf('/');
            String name = slash >= 0 ? path.substring(slash + 1) : path;
            int dot = name.lastIndexOf('.');
            if (dot <= 0 || dot >= name.length() - 1) {
                return "";
            }
            return name.substring(dot + 1);
        } catch (Exception e) {
            log.debug("解析 URL 扩展名失败: {}", url, e);
            return "";
        }
    }
}
