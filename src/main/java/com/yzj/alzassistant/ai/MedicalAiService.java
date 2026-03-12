package com.yzj.alzassistant.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * 医疗智能体 AI 服务接口。
 * 使用 AiServices + TokenStream 实现 token 级流式输出及工具调用过程展示。
 */
public interface MedicalAiService {

    /**
     * 运行医疗智能体（流式）
     *
     * @param appId       应用 ID，用于对话记忆隔离
     * @param userMessage 用户消息
     * @return TokenStream，可注册 onPartialResponse、onPartialToolExecutionRequest、onToolExecuted 等回调
     */
    @SystemMessage(fromResource = "prompt/agent_system_prompt.txt")
    TokenStream runMedicalAgentStream(@MemoryId long appId, @UserMessage String userMessage);
}
