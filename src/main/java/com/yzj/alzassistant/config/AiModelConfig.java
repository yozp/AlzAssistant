package com.yzj.alzassistant.config;

import com.yzj.alzassistant.monitor.AiModelMonitorListener;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * AI 模型配置类
 * 配置模型的 API 密钥、模型名称、最大令牌数、监听器（用于监控模型调用）等参数
 */
@Configuration
@Deprecated
public class AiModelConfig {

    @Resource
    private AiModelMonitorListener aiModelMonitorListener;

    @Value("${langchain4j.ollama.streaming-chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.ollama.streaming-chat-model.model-name}")
    private String modelName;

    @Bean
    @Primary
    public StreamingChatModel streamingChatModel() {
        return OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .listeners(List.of(aiModelMonitorListener))
                .build();
    }

    //---------------------------------------------------------------------------------------------------------------------------

//    @Value("${langchain4j.community.dashscope.streaming-chat-model.api-key}")
//    private String apiKey;
//
//    @Value("${langchain4j.community.dashscope.streaming-chat-model.model-name}")
//    private String modelName;
//
//    @Value("${langchain4j.community.dashscope.streaming-chat-model.max-tokens:8129}")
//    private Integer maxTokens;
//
//    @Bean
//    @Primary
//    public StreamingChatModel streamingChatModel() {
//        return QwenStreamingChatModel.builder()
//                .apiKey(apiKey)
//                .modelName(modelName)
//                .maxTokens(maxTokens)
//                .listeners(List.of(aiModelMonitorListener))
//                .build();
//    }
}

