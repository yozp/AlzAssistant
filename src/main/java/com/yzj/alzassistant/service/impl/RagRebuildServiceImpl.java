package com.yzj.alzassistant.service.impl;

import com.yzj.alzassistant.model.entity.KnowledgeBase;
import com.yzj.alzassistant.service.KnowledgeBaseService;
import com.yzj.alzassistant.service.RagRebuildService;
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
import java.util.stream.Collectors;

/**
 * RAG重新构建服务实现
 */
@Slf4j
@Service
public class RagRebuildServiceImpl implements RagRebuildService {

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    @Resource
    private EmbeddingModel qwenEmbeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Override
    public boolean rebuildRag() {
        try {
            log.info("开始重新构建RAG向量库...");

            // 1. 注意：InMemoryEmbeddingStore是内存存储，不支持清空
            // 重新构建时会追加到现有向量库，如果需要完全重建，需要重启应用
            // 如果需要持久化，应该使用Redis或其他持久化存储

            // 2. 从数据库获取所有激活的知识库文档
            List<KnowledgeBase> knowledgeBaseList = knowledgeBaseService.list();
            List<KnowledgeBase> activeKnowledgeBaseList = knowledgeBaseList.stream()
                    .filter(kb -> "active".equals(kb.getStatus()))
                    .collect(Collectors.toList());

            log.info("找到 {} 个激活的知识库文档", activeKnowledgeBaseList.size());

            // 3. 将知识库文档转换为Document对象
            List<Document> documents = activeKnowledgeBaseList.stream()
                    .map(kb -> {
                        // 使用标题和内容创建Document
                        String text = (kb.getTitle() != null ? kb.getTitle() + "\n" : "") + kb.getContent();
                        return Document.from(text);
                    })
                    .collect(Collectors.toList());

            // 4. 文档切割：将每个文档按段落分割，最大 1000 字符，每次重叠最多 200 个字符
            DocumentByParagraphSplitter paragraphSplitter = new DocumentByParagraphSplitter(1000, 200);

            // 5. 构建向量存储注入器
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(paragraphSplitter)
                    .embeddingModel(qwenEmbeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            // 6. 将文档注入向量库
            ingestor.ingest(documents);

            log.info("RAG向量库重新构建完成，共处理 {} 个文档", documents.size());
            return true;
        } catch (Exception e) {
            log.error("重新构建RAG向量库失败", e);
            return false;
        }
    }
}

