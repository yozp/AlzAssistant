package com.yzj.alzassistant.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 生成“猜你想问”推荐问题（无记忆、单次生成）。
 */
@Service
@Slf4j
public class FollowUpSuggestionsService {

    // 推荐问题数量
    private static final int SUGGESTION_COUNT = 3;
    // AI 回答最大长度
    private static final int MAX_AI_RESPONSE_LENGTH = 2000;

    // 提示词模板
    private static final String PROMPT_TEMPLATE = """
            你是一个对话助手。根据下面这轮【用户问题】和【AI回答】，生成 %d 个用户可能想继续问的“后续问题”。
            要求：
            1) 每行仅输出一个问题
            2) 不要编号，不要引号，不要额外解释
            3) 问题尽量简短、具体、可直接提问
            
            用户问题：
            %s
            
            AI回答：
            %s
            """;

    @Resource
    private AiModelSwitchService aiModelSwitchService;

    /**
     * 生成推荐问题列表（最多 3 个）。
     *
     * @param userQuestion 当前轮用户问题
     * @param aiResponse   当前轮 AI 回复
     * @return 推荐问题列表，失败或不可用时返回空列表
     */
    public List<String> generateSuggestions(String userQuestion, String aiResponse) {
        if (StrUtil.isBlank(userQuestion) || StrUtil.isBlank(aiResponse)) {
            return Collections.emptyList();
        }
        ChatModel chatModel = aiModelSwitchService.getCurrentChatModel();
        if (chatModel == null) {
            return Collections.emptyList();
        }
        try {
            String trimmedUserQuestion = userQuestion.trim();
            String trimmedAiResponse = trimAiResponse(aiResponse);
            String prompt = String.format(PROMPT_TEMPLATE, SUGGESTION_COUNT, trimmedUserQuestion, trimmedAiResponse);
            String raw = chatModel.chat(prompt);
            return parseSuggestions(raw);
        } catch (Exception e) {
            log.warn("生成猜你想问失败，返回空列表", e);
            return Collections.emptyList();
        }
    }

    /**
     * 裁剪 AI 回答
     */
    private static String trimAiResponse(String aiResponse) {
        String trimmed = aiResponse.trim();
        if (trimmed.length() <= MAX_AI_RESPONSE_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_AI_RESPONSE_LENGTH);
    }

    /**
     * 解析推荐问题列表
     */
    private static List<String> parseSuggestions(String raw) {
        if (StrUtil.isBlank(raw)) {
            return Collections.emptyList();
        }
        String normalized = raw
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();
        if (StrUtil.isBlank(normalized)) {
            return Collections.emptyList();
        }
        String[] lines = normalized.split("\n");
        List<String> result = new ArrayList<>(SUGGESTION_COUNT);
        for (String line : lines) {
            if (result.size() >= SUGGESTION_COUNT) {
                break;
            }
            if (line == null) {
                continue;
            }
            String cleaned = cleanLine(line);
            if (StrUtil.isBlank(cleaned)) {
                continue;
            }
            result.add(cleaned);
        }
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyList();
        }
        return result;
    }

    /**
     * 清理行
     */
    private static String cleanLine(String line) {
        String s = line.trim();
        if (StrUtil.isBlank(s)) {
            return "";
        }
        // 去掉常见的列表前缀（如 "1. "、"1) "、"- "、"• "）
        s = s.replaceFirst("^(?:\\d+\\s*[\\.|\\)|、]|[-•])\\s*", "");
        // 去掉包裹引号
        s = s.replaceAll("^[\"'“”]+|[\"'“”]+$", "").trim();
        // 避免输出空行
        return s;
    }
}

