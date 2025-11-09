package com.yzj.alzassistant.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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

}