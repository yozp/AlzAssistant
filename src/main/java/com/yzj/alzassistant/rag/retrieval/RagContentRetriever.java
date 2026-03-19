package com.yzj.alzassistant.rag.retrieval;

import com.yzj.alzassistant.rag.RagProperties;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * RAG 内容检索器：包装 EmbeddingStoreContentRetriever，检索为空时返回固定提示
 */
@Slf4j
@Component
public class RagContentRetriever implements ContentRetriever {

    private static final String EMPTY_CONTEXT_MESSAGE = """
            当前知识库中暂无与您问题直接相关的专业资料。
            建议：1）尝试换一种表述提问；2）咨询神经内科或记忆门诊的专业医生。
            """;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource
    private EmbeddingModel qwenEmbeddingModel;

    @Resource
    private RagProperties ragProperties;

    // 代理对象
    private ContentRetriever delegate;

    /**
     * 初始化代理对象
     */
    @PostConstruct
    public void init() {
        delegate = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(ragProperties.getMaxResults())
                .minScore(ragProperties.getMinScore())
                .build();
    }

    /**
     * 检索内容
     */
    @Override
    public List<Content> retrieve(Query query) {
        List<Content> results = delegate.retrieve(query);
        if (results == null || results.isEmpty()) {
            log.debug("RAG 检索无结果，返回空上下文提示");
            return Collections.singletonList(Content.from(EMPTY_CONTEXT_MESSAGE));
        }
        return results;
    }
}
