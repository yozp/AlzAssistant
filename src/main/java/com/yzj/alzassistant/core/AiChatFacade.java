package com.yzj.alzassistant.core;

import com.yzj.alzassistant.ai.AiChatServiceFactory;
import com.yzj.alzassistant.ai.agent.MedicalAgentService;
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
     */
    public Flux<String> generateAndSaveStreamFacade(String userMessage, ChatTypeEnum chatTypeEnum, Long appId) {
        if (chatTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "对话类型为空");
        }
        return switch (chatTypeEnum) {
            case CHAT_TYPE_ENUM -> generateAndSaveChatStream(userMessage, appId);
            case AGENT_TYPE_ENUM -> generateAndSaveAgentStream(userMessage, appId);
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
     * 智能体模式：调用 MedicalAgentService 运行智能体并流式输出。
     */
    private Flux<String> generateAndSaveAgentStream(String userMessage, Long appId) {
        Flux<String> result = medicalAgentService.runMedicalAgent(userMessage);
        StringBuilder agentBuilder = new StringBuilder();
        return result
                .doOnNext(agentBuilder::append)
                .doOnComplete(() -> {
                    try {
                        File savedDir = ChatFileSaver.saveChatResult(ChatTypeEnum.AGENT_TYPE_ENUM.getValue(), agentBuilder.toString());
                        log.info("Agent 结果保存成功，路径为：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存 Agent 结果失败", e);
                    }
                });
    }
}
