package com.yzj.alzassistant.rag.document;

import com.yzj.alzassistant.model.entity.KnowledgeBase;
import com.yzj.alzassistant.service.KnowledgeBaseService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 从知识库加载文档，转换为 LangChain4j Document 对象
 */
@Slf4j
@Component
public class RagDocumentLoader {

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 加载所有激活且未删除的知识库文档
     *
     * @return Document 列表，每个 Document 包含 title+content 及 metadata（kbId、title、category）
     */
    public List<Document> loadDocuments() {
        List<KnowledgeBase> knowledgeBaseList = knowledgeBaseService.list();
        List<KnowledgeBase> activeList = knowledgeBaseList.stream()
                .filter(kb -> "active".equals(kb.getStatus()) && (kb.getIsDelete() == null || kb.getIsDelete() == 0))
                .collect(Collectors.toList());

        log.debug("加载 {} 个激活的知识库文档", activeList.size());

        return activeList.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());
    }

    private Document toDocument(KnowledgeBase kb) {
        String text = (kb.getTitle() != null ? kb.getTitle() + "\n" : "") + (kb.getContent() != null ? kb.getContent() : "");
        Map<String, Object> map = new HashMap<>();
        map.put("kbId", kb.getId());
        map.put("title", kb.getTitle() != null ? kb.getTitle() : "");
        map.put("category", kb.getCategory() != null ? kb.getCategory() : "");
        Metadata metadata = Metadata.from(map);
        return Document.from(text, metadata);
    }
}
