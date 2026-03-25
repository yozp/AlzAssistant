package com.yzj.alzassistant.ai.tools;

import com.yzj.alzassistant.service.ChatDocumentParseService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文档解析工具：从可访问 URL 下载并抽取文本，供主模型理解 doc/docx/pdf/txt/md/xls/xlsx 等附件。
 */
@Component
@Slf4j
public class DocumentParseTool {

    @Resource
    private ChatDocumentParseService chatDocumentParseService;

    @Tool("解析文档链接中的正文（支持 doc、docx、pdf、txt、md、xls、xlsx 等）。当用户上传文档或消息中含文档/表格 URL 时，必须先对每条链接调用本工具获取正文，再结合用户问题作答。若 URL 路径无扩展名，请传入消息中与该链接同行的 filename 值。")
    public String parseDocument(
            @P("文档的 http/https URL") String documentUrl,
            @P("可选：原始文件名含扩展名；URL 无后缀时必填（可从用户消息中的 [filename=...] 取得）") String originalFileName) {
        log.info("调用文档解析工具，URL 长度: {}", documentUrl == null ? 0 : documentUrl.length());
        try {
            String content = chatDocumentParseService.parseDocumentFromUrl(documentUrl, originalFileName);
            return content != null && !content.isEmpty() ? content : "文档解析完成，但未提取到文本内容。";
        } catch (Exception e) {
            log.error("文档解析失败", e);
            return "文档解析失败：" + e.getMessage();
        }
    }
}
