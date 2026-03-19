package com.yzj.alzassistant.rag;

import com.yzj.alzassistant.rag.ingestion.RagIngestionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动加载 RAG 向量库
 */
@Slf4j
@Component
@Order(100)
public class RagBootstrap implements ApplicationRunner {

    @Resource
    private RagIngestionService ragIngestionService;

    @Resource
    private RagProperties ragProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!ragProperties.isBootstrapEnabled()) {
            log.debug("RAG 启动加载已禁用");
            return;
        }
        try {
            log.info("启动时加载 RAG 向量库...");
            ragIngestionService.ingestAll();
        } catch (Exception e) {
            log.warn("RAG 启动加载失败（知识库可能为空）: {}", e.getMessage());
        }
    }
}
