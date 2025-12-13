package com.yzj.alzassistant.config;

import com.yzj.alzassistant.service.AiModelSwitchService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 应用启动配置类
 * 用于在应用启动时初始化默认AI模型
 */
@Slf4j
@Configuration
public class ApplicationStartupConfig {

    @Resource
    private AiModelSwitchService aiModelSwitchService;

    /**
     * 应用启动后自动执行，初始化默认AI模型
     */
    @PostConstruct
    public void init() {
        log.info("应用启动：开始初始化默认AI模型...");
        try {
            aiModelSwitchService.initializeDefaultModel();
            log.info("默认AI模型初始化完成");
        } catch (Exception e) {
            log.error("默认AI模型初始化失败", e);
            // 不抛出异常，允许应用继续启动
        }
    }
}

