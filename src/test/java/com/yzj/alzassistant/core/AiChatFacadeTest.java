package com.yzj.alzassistant.core;

import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.model.enums.ChatTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiChatFacadeTest {

    @Resource
    private AiChatFacade aiChatFacade;

    @Test
    void generateAndSaveFacade() {
        String userMessage = "你好";
        ChatTypeEnum chatTypeEnum = ChatTypeEnum.CHAT_TYPE_ENUM;
        File file = aiChatFacade.generateAndSaveFacade(userMessage, chatTypeEnum);
        assertNotNull(file);
    }

    @Test
    void generateAndSaveFacadeStream() {
        String userMessage = "你好";
        ChatTypeEnum chatTypeEnum = ChatTypeEnum.CHAT_TYPE_ENUM;
        Flux<String> flux = aiChatFacade.generateAndSaveStreamFacade(userMessage, chatTypeEnum);
        // 阻塞等待所有数据收集完成
        List<String> results = flux.collectList().block();
        assertNotNull(results);
    }

}