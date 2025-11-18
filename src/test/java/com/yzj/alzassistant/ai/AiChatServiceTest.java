package com.yzj.alzassistant.ai;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiChatServiceTest {

    @Resource
    private AiChatService aiChatService;

    @Test
    void chatToAi() {
        String userMessage = "你好";
        String aiReply = aiChatService.chatToAi(userMessage);
        assertNotNull(aiReply);
    }

    /**
     * 测试与 AI 进行聊天（RAG 功能）
     */
    @Test
    void chatWithRag() {
        Result<String> result = aiChatService.chatWithRag("阿尔茨海默病是一种什么疾病？");
        String content = result.content();
        List<Content> sources = result.sources();
        System.out.println(content);
        System.out.println(sources);
    }


}