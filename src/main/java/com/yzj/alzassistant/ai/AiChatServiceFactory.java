package com.yzj.alzassistant.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yzj.alzassistant.ai.tools.DocumentParseTool;
import com.yzj.alzassistant.ai.tools.ImageRecognitionTool;
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
import java.util.Objects;

/**
 * AI 聊天服务工厂类
 */
@Configuration
@Slf4j
public class AiChatServiceFactory {

    /**
     * 缓存键：同一 appId 下「是否启用知识库检索」对应不同 AiServices 构建结果
     */
    private static final class ServiceCacheKey {
        private final long appId;
        private final boolean useRag;

        ServiceCacheKey(long appId, boolean useRag) {
            this.appId = appId;
            this.useRag = useRag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ServiceCacheKey that = (ServiceCacheKey) o;
            return appId == that.appId && useRag == that.useRag;
        }

        @Override
        public int hashCode() {
            return Objects.hash(appId, useRag);
        }
    }

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

    @Resource
    private ImageRecognitionTool imageRecognitionTool;

    @Resource
    private DocumentParseTool documentParseTool;

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
        return getAiChatService(0L, false);
    }

    //--------------------------------------------------------------------------------------------------------------------

    /**
     * 根据 appId 获取服务（默认不启用 RAG，与流式对话默认行为一致）
     */
    public AiChatService getAiChatService(long appId) {
        return getAiChatService(appId, false);
    }

    /**
     * 根据 appId 与是否启用知识库检索获取服务实例
     */
    public AiChatService getAiChatService(long appId, boolean useRag) {
        return serviceCache.get(new ServiceCacheKey(appId, useRag), key -> createAiChatService(key.appId, key.useRag));
    }

    /**
     * AI 服务实例缓存（按 appId + useRag 区分）
     */
    private final Cache<ServiceCacheKey, AiChatService> serviceCache = Caffeine.newBuilder()
            .maximumSize(2000) // 约为 app 数 × 2（有/无 RAG）
            .expireAfterWrite(Duration.ofMinutes(30)) //写入后 30 分钟过期
            .expireAfterAccess(Duration.ofMinutes(10)) //访问后 10 分钟过期
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，key: {}, 原因: {}", key, cause);
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
     *
     * @param useRag 为 true 时注入 {@link ContentRetriever}，启用知识库检索
     */
    private AiChatService createAiChatService(long appId, boolean useRag) {
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

        var builder = AiServices.builder(AiChatService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .tools(timeInfoTool,
                        webScrapingTool,
                        webSearchTool,
                        imageRecognitionTool,
                        documentParseTool)
                .chatMemory(chatMemory);
        if (useRag) {
            builder.contentRetriever(contentRetriever);
        }
        return builder.build();
    }

}