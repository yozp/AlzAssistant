package com.yzj.alzassistant.core;

import com.yzj.alzassistant.ai.AiChatService;
import com.yzj.alzassistant.ai.AiChatServiceFactory;
import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.model.enums.ChatTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiChatFacade {

    @Resource
    private AiChatServiceFactory aiChatServiceFactory;

    /**
     * 统一入口：根据类型生成并保存对话结果
     *
     * @param userMessage  用户提示词
     * @param chatTypeEnum 对话类型
     * @return 保存的目录
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

    /**
     * 生成对话并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveChat(String userMessage, Long appId) {
        String result = aiChatServiceFactory.getAiChatService(appId).chatToAi(userMessage);
        return ChatFileSaver.saveChatResult(ChatTypeEnum.CHAT_TYPE_ENUM.getValue(), result);
    }

    //------------------------------------------------------------------------------------------------------------------------

    /**
     * 统一入口：根据类型生成并保存对话结果（流式）
     *
     * @param userMessage  用户提示词
     * @param chatTypeEnum 对话类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveStreamFacade(String userMessage, ChatTypeEnum chatTypeEnum, Long appId) {
        if (chatTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "对话类型为空");
        }
        return switch (chatTypeEnum) {
            case CHAT_TYPE_ENUM -> generateAndSaveChatStream(userMessage, appId);
            default -> {
                String errorMessage = "不支持的对话类型：" + chatTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成对话并保存（流式）
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveChatStream(String userMessage, Long appId) {
        Flux<String> result = aiChatServiceFactory.getAiChatService(appId).chatToAiStream(userMessage);
        // 当流式返回生成代码完成后，再保存代码
        StringBuilder chatBuilder = new StringBuilder();
        return result
                .doOnNext(chunk->{
                    // 实时收集对话片段
                    chatBuilder.append(chunk);
                })
                .doOnComplete(()->{
                    try {
                        // 保存完整的对话
                        File savedDir = ChatFileSaver.saveChatResult(ChatTypeEnum.CHAT_TYPE_ENUM.getValue(), chatBuilder.toString());
                        log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存对话失败", e.getMessage());
                    }
                });

    }
}
