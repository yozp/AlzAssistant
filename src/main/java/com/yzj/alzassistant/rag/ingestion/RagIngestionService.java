package com.yzj.alzassistant.rag.ingestion;

import com.yzj.alzassistant.rag.RagProperties;
import com.yzj.alzassistant.rag.document.RagDocumentLoader;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 文档注入服务：清空、分块、向量化并写入向量库
 */
@Slf4j
@Service
public class RagIngestionService {

    @Resource
    private RagDocumentLoader ragDocumentLoader;

    @Resource
    private EmbeddingModel qwenEmbeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource
    private RagProperties ragProperties;

    /**
     * 执行全量注入：先清空再注入当前知识库文档
     */
    public void rebuild() {
        log.info("开始重新构建 RAG 向量库...");
        embeddingStore.removeAll();
        ingestAll();
        log.info("RAG 向量库重新构建完成");
    }

    /**
     * 将知识库文档注入向量库（不清空，直接追加）
     * 若需完全重建，请调用 rebuild()
     */
    public void ingestAll() {
        List<Document> documents = ragDocumentLoader.loadDocuments();
        if (documents.isEmpty()) {
            log.info("知识库暂无激活文档，跳过注入");
            return;
        }

        int chunkSize = ragProperties.getChunkSize();
        int chunkOverlap = ragProperties.getChunkOverlap();
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(chunkSize, chunkOverlap);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);
        log.info("RAG 向量库注入完成，共处理 {} 个文档", documents.size());
    }
}
