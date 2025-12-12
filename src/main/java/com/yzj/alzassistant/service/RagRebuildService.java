package com.yzj.alzassistant.service;

/**
 * RAG重新构建服务
 */
public interface RagRebuildService {

    /**
     * 重新构建RAG向量库
     * 从数据库中的知识库文档重新构建向量库
     */
    boolean rebuildRag();
}


