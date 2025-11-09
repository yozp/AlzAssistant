package com.yzj.alzassistant.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 聊天服务工厂类
 */
@Configuration
public class AiChatServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Bean
    public AiChatService aiChatService() {
        return AiServices.create(AiChatService.class, chatModel);
    }
}