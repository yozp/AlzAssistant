package com.yzj.alzassistant.ai.agent;

import cn.hutool.json.JSONUtil;
import com.yzj.alzassistant.ai.MedicalAiService;
import com.yzj.alzassistant.ai.MedicalAiServiceFactory;
import com.yzj.alzassistant.ai.model.message.AiResponseMessage;
import com.yzj.alzassistant.ai.model.message.ToolExecutedMessage;
import com.yzj.alzassistant.ai.model.message.ToolRequestMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 医疗智能体服务，基于 TokenStream 实现 token 级流式输出和工具调用过程展示。
 */
@Service
@Slf4j
public class MedicalAgentService {

    @Resource
    private MedicalAiServiceFactory medicalAiServiceFactory;

    /**
     * 运行医疗智能体（TokenStream 流式输出）
     *
     * @param appId       应用 ID，用于对话记忆隔离
     * @param userMessage 用户消息
     * @return 流式响应，每个 chunk 为 JSON 格式的统一消息
     */
    public Flux<String> runMedicalAgent(long appId, String userMessage) {
        log.info("启动医疗智能体，appId: {}, 用户消息: {}", appId, userMessage);

        TokenStream tokenStream = medicalAiServiceFactory.getMedicalAiService().runMedicalAgentStream(appId, userMessage);
        return processTokenStream(tokenStream)
                .doOnSubscribe(sub -> log.info("医疗智能体开始 TokenStream 流式执行"))
                .doOnComplete(() -> log.info("医疗智能体执行完成"))
                .doOnError(e -> log.error("医疗智能体执行出错", e));
    }

    /**
     * 将 TokenStream 转换为 Flux，输出统一消息格式的 JSON。
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream
                    .onPartialResponse(partial -> {
                        AiResponseMessage msg = new AiResponseMessage(partial);
                        sink.next(JSONUtil.toJsonStr(msg));
                    })
                    .onPartialToolExecutionRequest((idx, req) -> {
                        ToolRequestMessage msg = new ToolRequestMessage(req);
                        sink.next(JSONUtil.toJsonStr(msg));
                    })
                    .onCompleteToolExecutionRequest((idx, req) -> {
                        ToolRequestMessage msg = new ToolRequestMessage(req);
                        sink.next(JSONUtil.toJsonStr(msg));
                    })
                    .onToolExecuted(te -> {
                        ToolExecutedMessage msg = new ToolExecutedMessage(te);
                        sink.next(JSONUtil.toJsonStr(msg));
                    })
                    .onCompleteResponse((ChatResponse r) -> sink.complete())
                    .onError(sink::error)
                    .start();
        });
    }
}
