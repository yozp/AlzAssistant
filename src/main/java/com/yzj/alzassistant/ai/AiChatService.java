package com.yzj.alzassistant.ai;

import dev.langchain4j.service.SystemMessage;

/**
 * AI 服务接口
 */
public interface AiChatService {

    /**
     * 与 AI 进行聊天
     * @param userMessage 用户消息
     * @return AI 回复消息
     */
    @SystemMessage(fromResource = "prompt/system_prompt.txt")
    String chatToAi(String userMessage);
}
