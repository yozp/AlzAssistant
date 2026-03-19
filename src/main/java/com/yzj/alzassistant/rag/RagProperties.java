package com.yzj.alzassistant.rag;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RAG 可配置参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "alz.rag")
public class RagProperties {

    /**
     * 最多检索结果数
     */
    private int maxResults = 5;

    /**
     * 最低相似度分数（0-1），低于此值的结果将被过滤
     */
    private double minScore = 0.5;

    /**
     * 分块最大字符数
     */
    private int chunkSize = 1000;

    /**
     * 分块重叠字符数
     */
    private int chunkOverlap = 200;

    /**
     * 启动时是否自动加载向量库
     */
    private boolean bootstrapEnabled = true;

}
