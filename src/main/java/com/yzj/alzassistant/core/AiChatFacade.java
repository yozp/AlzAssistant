package com.yzj.alzassistant.core;

import cn.hutool.json.JSONUtil;
import com.yzj.alzassistant.ai.AiChatServiceFactory;
import com.yzj.alzassistant.ai.agent.MedicalAgentService;
import com.yzj.alzassistant.ai.model.message.AiResponseMessage;
import com.yzj.alzassistant.ai.model.message.StreamMessage;
import com.yzj.alzassistant.ai.model.message.StreamMessageTypeEnum;
import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.model.enums.ChatTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 对话外观类，组合生成和保存功能，根据对话类型路由到不同的服务。
 */
@Service
@Slf4j
public class AiChatFacade {

    @Resource
    private AiChatServiceFactory aiChatServiceFactory;

    @Resource
    private MedicalAgentService medicalAgentService;

    /**
     * 统一入口：根据类型生成并保存对话结果
     */
    public File generateAndSaveFacade(String userMessage, ChatTypeEnum chatTypeEnum, Long appId) {
        if (chatTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "对话类型为空");
        }
        return switch (chatTypeEnum) {
            case CHAT_TYPE_ENUM -> generateAndSaveChat(userMessage, appId);
            default -> {
                String errorMessage = "不支持的对话类型：" + chatTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    private File generateAndSaveChat(String userMessage, Long appId) {
        String result = aiChatServiceFactory.getAiChatService(appId).chatToAi(userMessage);
        return ChatFileSaver.saveChatResult(ChatTypeEnum.CHAT_TYPE_ENUM.getValue(), result);
    }

    //------------------------------------------------------------------------------------------------------------------------

    /**
     * 统一入口：根据类型生成并保存对话结果（流式）
     *
     * @param userLocation 用户实时位置（经度,纬度），可选，供智能体地图工具使用
     */
    public Flux<String> generateAndSaveStreamFacade(String userMessage, ChatTypeEnum chatTypeEnum, Long appId, String userLocation) {
        if (chatTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "对话类型为空");
        }
        return switch (chatTypeEnum) {
            case CHAT_TYPE_ENUM -> generateAndSaveChatStream(userMessage, appId);
            case AGENT_TYPE_ENUM -> generateAndSaveAgentStream(userMessage, appId, userLocation);
            default -> {
                String errorMessage = "不支持的对话类型：" + chatTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 普通对话模式：调用 AiChatServiceFactory 运行 LLM 并流式输出。
     */
    private Flux<String> generateAndSaveChatStream(String userMessage, Long appId) {
        Flux<String> result = aiChatServiceFactory.getAiChatService(appId).chatToAiStream(userMessage);
        StringBuilder chatBuilder = new StringBuilder();
        return result
                .doOnNext(chatBuilder::append)
                .doOnComplete(() -> {
                    try {
                        File savedDir = ChatFileSaver.saveChatResult(ChatTypeEnum.CHAT_TYPE_ENUM.getValue(), chatBuilder.toString());
                        log.info("保存成功，路径为：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存对话失败", e);
                    }
                });
    }

    /**
     * 智能体模式：调用 MedicalAgentService 运行智能体，透传结构化 JSON 流式消息。
     */
    private Flux<String> generateAndSaveAgentStream(String userMessage, Long appId, String userLocation) {
        Flux<String> jsonFlux = medicalAgentService.runMedicalAgent(appId, userMessage, userLocation);
        StringBuilder agentBuilder = new StringBuilder();
        return jsonFlux
                .doOnNext(chunk -> agentBuilder.append(extractAgentTextForHistory(chunk)))
                .doOnComplete(() -> {
                    try {
                        File savedDir = ChatFileSaver.saveChatResult(ChatTypeEnum.AGENT_TYPE_ENUM.getValue(), agentBuilder.toString());
                        log.info("Agent 结果保存成功，路径为：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存 Agent 结果失败", e);
                    }
                });
    }

    /**
     * 从结构化 chunk 中提取适合落库的文本（仅保留 AI 正文）。
     */
    private String extractAgentTextForHistory(String chunk) {
        try {
            StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
            StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
            if (typeEnum == StreamMessageTypeEnum.AI_RESPONSE) {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                return aiMessage.getData() == null ? "" : aiMessage.getData();
            }
            return "";
        } catch (Exception e) {
            log.warn("解析智能体结构化消息失败，忽略该 chunk: {}", chunk, e);
            return "";
        }
    }
}
