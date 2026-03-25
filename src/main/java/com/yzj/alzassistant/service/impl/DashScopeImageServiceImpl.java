package com.yzj.alzassistant.service.impl;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.service.DashScopeImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用 DashScope qwen-vl 多模态接口理解图片（base64 入参）。
 */
@Service
@Slf4j
public class DashScopeImageServiceImpl implements DashScopeImageService {

    // 图片理解提示词
    private static final String PROMPT = "请详细描述这张图片的内容，包括：1. 图片中的所有文字内容（如果有）"
            + "2. 图片中的物体、场景、人物等 3. 图片的整体含义和上下文关系。请用中文回答，描述要准确详细。";

    // DashScope API Key
    @Value("${langchain4j.community.dashscope.embedding-model.api-key:}")
    private String embeddingApiKey;

    // DashScope 模型名称
    @Value("${alzassistant.dashscope.image.model:qwen-vl-plus}")
    private String vlModel;

    // DashScope 温度
    @Value("${alzassistant.dashscope.image.temperature:0.1}")
    private double temperature;

    // DashScope 最大 token 数
    @Value("${alzassistant.dashscope.image.max-tokens:2000}")
    private int maxTokens;

    @Override
    public String understandImage(byte[] imageData) {
        String apiKey = embeddingApiKey == null ? "" : embeddingApiKey.trim();
        if (apiKey.isBlank() || "xxx".equalsIgnoreCase(apiKey)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "DashScope API Key 未正确配置（与 embedding 共用 langchain4j.community.dashscope.embedding-model.api-key）");
        }
        try {
            String imageDataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
            List<Map<String, Object>> contentList = new ArrayList<>();
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("image", imageDataUri);
            contentList.add(imageContent);
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("text", PROMPT);
            contentList.add(textContent);

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(contentList)
                    .build();

            MultiModalConversation conv = new MultiModalConversation();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(apiKey)
                    .model(vlModel)
                    .temperature((float) temperature)
                    .maxTokens(maxTokens)
                    .messages(List.of(userMessage))
                    .build();

            MultiModalConversationResult result = conv.call(param);
            if (result == null || result.getOutput() == null
                    || result.getOutput().getChoices() == null
                    || result.getOutput().getChoices().isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 图片理解返回结果为空");
            }
            List<Map<String, Object>> resultContent = result.getOutput().getChoices().get(0)
                    .getMessage().getContent();
            if (resultContent == null || resultContent.isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 图片理解返回内容为空");
            }
            for (Map<String, Object> item : resultContent) {
                if (item.containsKey("text")) {
                    String description = (String) item.get("text");
                    if (description != null && !description.isEmpty()) {
                        log.info("DashScope 图片理解成功，描述长度: {}", description.length());
                        return description;
                    }
                }
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 图片理解返回文本为空");
        } catch (NoApiKeyException e) {
            log.error("DashScope API Key 未配置", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "DashScope API Key 未配置");
        } catch (ApiException e) {
            log.error("DashScope 图片理解接口调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片理解失败：" + e.getMessage());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("图片 AI 理解异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片理解失败：" + e.getMessage());
        }
    }
}
