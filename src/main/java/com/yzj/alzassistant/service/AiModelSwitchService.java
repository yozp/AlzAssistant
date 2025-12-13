package com.yzj.alzassistant.service;

import com.yzj.alzassistant.model.entity.AiModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * AI模型动态切换服务
 */
public interface AiModelSwitchService {

    /**
     * 切换到指定的AI模型
     * 
     * @param aiModel 要切换的模型
     */
    void switchToModel(AiModel aiModel);

    /**
     * 获取当前活跃的AI模型
     * 
     * @return 当前活跃的模型，如果没有则返回null
     */
    AiModel getCurrentActiveModel();

    /**
     * 获取当前使用的StreamingChatModel实例
     * 
     * @return StreamingChatModel实例
     */
    StreamingChatModel getCurrentStreamingChatModel();

    /**
     * 重启所有AI服务
     */
    void restartAiServices();

    /**
     * 初始化默认模型（应用启动时调用）
     */
    void initializeDefaultModel();

    /**
     * 验证AI模型是否可用（发送测试请求）
     * 
     * @param aiModel 要验证的模型
     * @return 验证是否成功
     * @throws Exception 验证失败时抛出异常，包含失败原因
     */
    boolean validateAiModel(AiModel aiModel) throws Exception;
}

