package com.yzj.alzassistant.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yzj.alzassistant.ai.tools.TimeInfoTool;
import com.yzj.alzassistant.ai.tools.WebScrapingTool;
import com.yzj.alzassistant.ai.tools.WebSearchTool;
import com.yzj.alzassistant.service.AiModelSwitchService;
import com.yzj.alzassistant.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 聊天服务工厂类
 */
@Configuration
@Slf4j
public class AiChatServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private AiModelSwitchService aiModelSwitchService;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ContentRetriever contentRetriever;

    //--------------------------------------------------------------------------------------------------------------------

    @Resource
    private TimeInfoTool timeInfoTool;

    @Resource
    private WebScrapingTool webScrapingTool;

    @Resource
    private WebSearchTool webSearchTool;

    //--------------------------------------------------------------------------------------------------------------------

    /**
     * 默认提供一个 Bean
     * 意义：
     * 1. 提供一个默认的 AiChatService 实例
     * 2. 可以被其他类调用动态创建（实现独立的AiChatGeneratorService对象）
     * 3. 可以被 aiChatGeneratorService 方法自动创建默认实例（appId 为 0）
     */
    @Bean
    public AiChatService aiChatService() {
        return getAiChatService(0L);
    }

    //--------------------------------------------------------------------------------------------------------------------

    /**
     * 根据 appId 获取服务
     * 可以被 aiChatGeneratorService 方法自动创建默认实例（appId 为 0）
     * 也可以被其他类调用动态创建（实现独立的AiChatGeneratorService对象）
     */
    public AiChatService getAiChatService(long appId) {
        return serviceCache.get(appId, this::createAiChatService);
    }

    /**
     * AI 服务实例缓存
     */
    private final Cache<Long, AiChatService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000) //最大缓存 1000 个实例
            .expireAfterWrite(Duration.ofMinutes(30)) //写入后 30 分钟过期
            .expireAfterAccess(Duration.ofMinutes(10)) //访问后 10 分钟过期
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 清除所有缓存（用于模型切换时重启服务）
     */
    public void clearCache() {
        log.info("清除AI服务缓存，重新初始化所有服务实例");
        serviceCache.invalidateAll();
    }

    /**
     * 创建 AI 聊天服务实例
     * AI 对话 => 从数据库中加载对话历史到 Redis => Redis 为 AI 提供对话记忆
     */
    private AiChatService createAiChatService(Long appId) {
        // 获取当前活跃的StreamingChatModel
        StreamingChatModel streamingChatModel = aiModelSwitchService.getCurrentStreamingChatModel();
        
        if (streamingChatModel == null) {
            log.warn("未找到活跃的AI模型，使用默认配置");
            // 如果没有活跃模型，尝试初始化默认模型
            aiModelSwitchService.initializeDefaultModel();
            streamingChatModel = aiModelSwitchService.getCurrentStreamingChatModel();
        }

        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        
        return AiServices.builder(AiChatService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .tools(timeInfoTool,
                        webScrapingTool,
                        webSearchTool)
                .chatMemory(chatMemory)
                .contentRetriever(contentRetriever)// 开启 RAG 功能
                .build();
    }

}