package com.yzj.alzassistant.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * 网页抓取工具类
 * 用于抓取指定URL的网页内容，并提取关键信息
 */
@Slf4j
@Component
public class WebScrapingTool {

    private static final int TIMEOUT_MS = 10000; // 10秒超时
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int MAX_CONTENT_LENGTH = 50000; // 最大内容长度，避免返回过长内容

    @Tool("抓取指定URL的网页内容，提取网页的标题和主要文本信息。适用于用户提供了具体URL并希望了解网页内容的场景。")
    public String scrapeWebPage(@P("要抓取的网页URL，必须是完整的URL（包含http://或https://）") String url) {
        log.info("开始抓取网页：{}", url);
        
        try {
            // 验证URL格式
            if (url == null || url.trim().isEmpty()) {
                return "❌ 错误：URL不能为空";
            }
            
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return "❌ 错误：URL格式不正确，必须以 http:// 或 https:// 开头";
            }
            
            // 连接并获取网页
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(false)
                    .get();
            
            // 提取网页信息
            StringBuilder result = new StringBuilder();
            
            // 1. 提取标题
            String title = doc.title();
            if (title != null && !title.isEmpty()) {
                result.append("📄 网页标题：").append(title).append("\n\n");
            }
            
            // 2. 提取meta描述
            Element metaDescription = doc.selectFirst("meta[name=description]");
            if (metaDescription != null && metaDescription.hasAttr("content")) {
                String description = metaDescription.attr("content");
                if (!description.isEmpty()) {
                    result.append("📝 网页描述：").append(description).append("\n\n");
                }
            }
            
            // 3. 提取主要内容
            // 移除script、style等不需要的标签
            doc.select("script, style, nav, header, footer, aside, iframe").remove();
            
            // 尝试提取主要内容区域（优先选择article、main等语义化标签）
            String mainContent = extractMainContent(doc);
            
            if (mainContent != null && !mainContent.isEmpty()) {
                result.append("📄 主要内容：\n");
                
                // 限制内容长度
                if (mainContent.length() > MAX_CONTENT_LENGTH) {
                    mainContent = mainContent.substring(0, MAX_CONTENT_LENGTH) + "\n\n...(内容过长，已截断)";
                }
                
                result.append(mainContent);
            } else {
                result.append("⚠️ 无法提取到有效的文本内容");
            }
            
            // 4. 提取关键链接（可选）
            Elements links = doc.select("a[href]");
            if (!links.isEmpty() && links.size() <= 10) {
                result.append("\n\n🔗 页面包含的链接：\n");
                for (Element link : links) {
                    String linkText = link.text();
                    String linkHref = link.attr("abs:href");
                    if (!linkText.isEmpty() && !linkHref.isEmpty()) {
                        result.append("- ").append(linkText).append(": ").append(linkHref).append("\n");
                    }
                }
            }
            
            log.info("网页抓取成功：{}", url);
            return result.toString();
            
        } catch (SocketTimeoutException e) {
            log.error("网页抓取超时：{}", url, e);
            return "❌ 网页抓取失败：连接超时，该网页可能响应过慢或无法访问";
        } catch (IOException e) {
            log.error("网页抓取失败：{}", url, e);
            String errorMsg = e.getMessage();
            if (errorMsg.contains("404")) {
                return "❌ 网页抓取失败：页面不存在（404错误）";
            } else if (errorMsg.contains("403")) {
                return "❌ 网页抓取失败：访问被拒绝（403错误），该网页可能禁止爬虫访问";
            } else if (errorMsg.contains("500")) {
                return "❌ 网页抓取失败：服务器错误（500错误）";
            } else {
                return "❌ 网页抓取失败：" + errorMsg;
            }
        } catch (Exception e) {
            log.error("网页抓取发生未知错误：{}", url, e);
            return "❌ 网页抓取失败：发生未知错误 - " + e.getMessage();
        }
    }
    
    /**
     * 提取网页的主要内容
     */
    private String extractMainContent(Document doc) {
        // 优先选择语义化标签
        Element mainElement = doc.selectFirst("article");
        if (mainElement == null) {
            mainElement = doc.selectFirst("main");
        }
        if (mainElement == null) {
            mainElement = doc.selectFirst("div.content, div.main-content, div.article-content");
        }
        if (mainElement == null) {
            // 如果没有找到特定标签，使用body
            mainElement = doc.body();
        }
        
        if (mainElement != null) {
            // 获取文本内容，保留换行
            String text = mainElement.text();
            // 清理多余的空白字符
            text = text.replaceAll("\\s+", " ").trim();
            return text;
        }
        
        return null;
    }
}