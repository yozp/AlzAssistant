package com.yzj.alzassistant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.yzj.alzassistant.ai.AiChatServiceFactory;
import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.model.entity.AiModel;
import com.yzj.alzassistant.monitor.AiModelMonitorListener;
import com.yzj.alzassistant.service.AiModelService;
import com.yzj.alzassistant.service.AiModelSwitchService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * AI模型动态切换服务实现
 */
@Slf4j
@Service
public class AiModelSwitchServiceImpl implements AiModelSwitchService {

    @Resource
    @Lazy
    private AiModelService aiModelService;

    @Resource
    private AiModelMonitorListener aiModelMonitorListener;

    @Resource
    @Lazy
    private AiChatServiceFactory aiChatServiceFactory;

    @Value("${ai-model.default.model-key:}")
    private String defaultModelKey;

    @Value("${ai-model.default.base-url:}")
    private String defaultBaseUrl;

    @Value("${ai-model.default.api-key:}")
    private String defaultApiKey;

    @Value("${ai-model.default.model-name:}")
    private String defaultModelName;

    // 当前活跃的模型
    private volatile AiModel currentActiveModel;

    // 当前使用的StreamingChatModel实例
    private volatile StreamingChatModel currentStreamingChatModel;

    @Override
    public void switchToModel(AiModel aiModel) {
        if (aiModel == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型不能为空");
        }

        log.info("开始切换AI模型：{}", aiModel.getModelName());

        try {
            // 根据模型类型创建对应的StreamingChatModel
            StreamingChatModel newModel = createStreamingChatModel(aiModel);

            // 保存当前模型
            this.currentActiveModel = aiModel;
            this.currentStreamingChatModel = newModel;

            // 重启所有AI服务
            restartAiServices();

            log.info("AI模型切换成功：{}", aiModel.getModelName());
        } catch (Exception e) {
            log.error("AI模型切换失败：{}", aiModel.getModelName(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI模型切换失败：" + e.getMessage());
        }
    }

    @Override
    public AiModel getCurrentActiveModel() {
        if (currentActiveModel == null) {
            // 尝试从数据库加载当前活跃模型
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("status", "active")
                    .orderBy("priority", false)
                    .limit(1);
            List<AiModel> models = aiModelService.list(queryWrapper);
            if (!models.isEmpty()) {
                currentActiveModel = models.get(0);
            }
        }
        return currentActiveModel;
    }

    @Override
    public StreamingChatModel getCurrentStreamingChatModel() {
        if (currentStreamingChatModel == null) {
            // 如果没有初始化，尝试初始化默认模型
            initializeDefaultModel();
        }
        return currentStreamingChatModel;
    }

    @Override
    public void restartAiServices() {
        log.info("开始重启所有AI服务...");
        try {
            // 清除AiChatServiceFactory中的缓存
            aiChatServiceFactory.clearCache();
            log.info("AI服务重启成功");
        } catch (Exception e) {
            log.error("AI服务重启失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI服务重启失败：" + e.getMessage());
        }
    }

    @Override
    public void initializeDefaultModel() {
        log.info("初始化默认AI模型...");

        // 1. 首先尝试从数据库中获取活跃模型
        AiModel activeModel = getCurrentActiveModel();
        if (activeModel != null && StrUtil.isNotBlank(activeModel.getApiKey())) {
            log.info("从数据库加载活跃模型：{}", activeModel.getModelName());
            switchToModel(activeModel);
            return;
        }

        // 2. 如果配置文件中指定了默认模型，则从数据库查找并启用
        if (StrUtil.isNotBlank(defaultModelKey)) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("modelKey", defaultModelKey);
            AiModel defaultModel = aiModelService.getOne(queryWrapper);
            
            if (defaultModel != null) {
                log.info("启用配置文件中指定的默认模型：{}", defaultModel.getModelName());
                // 设置为活跃状态
                defaultModel.setStatus("active");
                aiModelService.updateById(defaultModel);
                switchToModel(defaultModel);
                return;
            } else {
                log.warn("配置文件中指定的默认模型不存在：{}", defaultModelKey);
            }
        }

        // 3. 如果配置文件有完整的默认配置，创建临时模型
        if (StrUtil.isNotBlank(defaultBaseUrl) && StrUtil.isNotBlank(defaultApiKey) && StrUtil.isNotBlank(defaultModelName)) {
            log.info("使用配置文件中的默认模型配置");
            AiModel tempModel = AiModel.builder()
                    .modelName(defaultModelName)
                    .modelKey(defaultModelKey)
                    .apiKey(defaultApiKey)
                    .baseUrl(defaultBaseUrl)
                    .modelType("openai")
                    .maxTokens(8129)
                    .temperature(new BigDecimal("0.7"))
                    .topP(new BigDecimal("1.0"))
                    .build();
            
            StreamingChatModel model = createStreamingChatModel(tempModel);
            this.currentActiveModel = tempModel;
            this.currentStreamingChatModel = model;
            log.info("默认AI模型初始化成功（临时配置）");
            return;
        }

        log.warn("未找到可用的AI模型配置，请在管理界面配置并启用模型");
    }

    @Override
    public boolean validateAiModel(AiModel aiModel) throws Exception {
        log.info("开始验证AI模型：{}", aiModel.getModelName());
        
        try {
            // 创建用于测试的ChatModel（非流式，方便验证）
            ChatModel testModel = createTestChatModel(aiModel);
            
            // 发送简单的测试消息，设置5秒超时
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> 
                testModel.chat("你好")
            );
            
            String response = future.get(5, TimeUnit.SECONDS);
            
            if (StrUtil.isNotBlank(response)) {
                log.info("AI模型验证成功：{}", aiModel.getModelName());
                return true;
            } else {
                throw new Exception("模型响应为空");
            }
        } catch (Exception e) {
            log.error("AI模型验证失败：{}", aiModel.getModelName(), e);
            throw new Exception("模型验证失败：" + e.getMessage());
        }
    }

    /**
     * 创建用于测试的ChatModel
     */
    private ChatModel createTestChatModel(AiModel aiModel) {
        String modelType = aiModel.getModelType();
        
        if (StrUtil.isBlank(modelType)) {
            modelType = "openai";
        }

        switch (modelType.toLowerCase()) {
            case "ollama":
                return OllamaChatModel.builder()
                        .baseUrl(aiModel.getBaseUrl())
                        .modelName(aiModel.getModelKey())
                        .timeout(Duration.ofSeconds(5))
                        .build();

            case "openai":
            case "deepseek":
            case "claude":
            case "gemini":
            case "custom":
            default:
                return OpenAiChatModel.builder()
                        .apiKey(aiModel.getApiKey())
                        .baseUrl(aiModel.getBaseUrl())
                        .modelName(aiModel.getModelKey())
                        .timeout(Duration.ofSeconds(5))
                        .build();
        }
    }

    /**
     * 根据模型配置创建StreamingChatModel
     */
    private StreamingChatModel createStreamingChatModel(AiModel aiModel) {
        String modelType = aiModel.getModelType();
        
        if (StrUtil.isBlank(modelType)) {
            modelType = "openai"; // 默认使用OpenAI兼容接口
        }

        switch (modelType.toLowerCase()) {
            case "ollama":
                return OllamaStreamingChatModel.builder()
                        .baseUrl(aiModel.getBaseUrl())
                        .modelName(aiModel.getModelKey())
                        .timeout(Duration.ofSeconds(60))
                        .listeners(List.of(aiModelMonitorListener))
                        .build();

            case "openai":
            case "deepseek":
            case "claude":
            case "gemini":
            case "custom":
            default:
                // OpenAI兼容接口（支持DeepSeek、Claude等）
                return OpenAiStreamingChatModel.builder()
                        .apiKey(aiModel.getApiKey())
                        .baseUrl(aiModel.getBaseUrl())
                        .modelName(aiModel.getModelKey())
                        .maxTokens(aiModel.getMaxTokens())
                        .temperature(aiModel.getTemperature() != null ? aiModel.getTemperature().doubleValue() : 0.7)
                        .topP(aiModel.getTopP() != null ? aiModel.getTopP().doubleValue() : 1.0)
                        .timeout(Duration.ofSeconds(60))
                        .logRequests(true)
                        .logResponses(true)
                        .listeners(List.of(aiModelMonitorListener))
                        .build();
        }
    }
}

