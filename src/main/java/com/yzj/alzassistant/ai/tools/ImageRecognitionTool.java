package com.yzj.alzassistant.ai.tools;

import com.yzj.alzassistant.service.DashScopeImageService;
import com.yzj.alzassistant.util.AttachmentUrlDownloader;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * 图片识别：将 URL 或 base64 转为字节后调用 DashScope 视觉模型，供纯文本主模型使用。
 */
@Component
@Slf4j
public class ImageRecognitionTool {

    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;

    @Resource
    private DashScopeImageService dashScopeImageService;

    @Tool("识别和理解图片内容，包括图片中的文字（OCR）、物体、场景、人物等。当用户上传图片或消息中含图片 URL 时，必须先调用本工具（可对每张图各调用一次），再结合识别结果回答用户。")
    public String recognizeImage(@P("图片的 URL（http/https）或 data:image 开头的 base64，或裸 base64 字符串") String imageUrl) {
        log.info("调用图片识别工具，参数长度: {}", imageUrl == null ? 0 : imageUrl.length());
        if (imageUrl == null || imageUrl.isBlank()) {
            return "图片地址为空";
        }
        String trimmed = imageUrl.trim();
        try {
            byte[] imageData = loadImageBytes(trimmed);
            if (imageData.length > MAX_IMAGE_BYTES) {
                return "图片过大（超过 10MB），无法识别";
            }
            String description = dashScopeImageService.understandImage(imageData);
            return description;
        } catch (Exception e) {
            log.error("图片识别失败", e);
            return "图片识别失败：" + e.getMessage();
        }
    }

    /**
     * 加载图片字节
     */
    private static byte[] loadImageBytes(String imageUrl) {
        if (imageUrl.startsWith("data:image")) {
            int comma = imageUrl.indexOf(',');
            if (comma < 0) {
                throw new IllegalArgumentException("无效的 data URI");
            }
            return Base64.getDecoder().decode(imageUrl.substring(comma + 1));
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return AttachmentUrlDownloader.download(imageUrl, MAX_IMAGE_BYTES);
        }
        return Base64.getDecoder().decode(imageUrl);
    }
}
