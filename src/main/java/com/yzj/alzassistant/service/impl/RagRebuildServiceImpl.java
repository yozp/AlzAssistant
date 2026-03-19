package com.yzj.alzassistant.service.impl;

import com.yzj.alzassistant.rag.ingestion.RagIngestionService;
import com.yzj.alzassistant.service.RagRebuildService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * RAG 重新构建服务实现，委托 RagIngestionService
 */
@Slf4j
@Service
public class RagRebuildServiceImpl implements RagRebuildService {

    @Resource
    private RagIngestionService ragIngestionService;

    @Override
    public boolean rebuildRag() {
        try {
            ragIngestionService.rebuild();
            return true;
        } catch (Exception e) {
            log.error("重新构建 RAG 向量库失败", e);
            return false;
        }
    }
}
