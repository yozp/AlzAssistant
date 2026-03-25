package com.yzj.alzassistant.service;

/**
 * 从可访问 URL 下载并解析聊天附件中的文档类文件为纯文本（供 {@code parseDocument} 工具使用）。
 */
public interface ChatDocumentParseService {

    /**
     * 下载并解析文档，返回 UTF-8 文本（可能按配置截断）。
     *
     * @param documentUrl      http(s) 链接，路径中宜含正确扩展名
     * @param originalFileName 可选；当 URL 无后缀时用于推断类型（如用户上传时的文件名）
     * @return 抽取的正文；无文本时返回简短说明字符串
     */
    String parseDocumentFromUrl(String documentUrl, String originalFileName);
}
