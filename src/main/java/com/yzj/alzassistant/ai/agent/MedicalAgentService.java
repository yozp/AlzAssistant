package com.yzj.alzassistant.ai.agent;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yzj.alzassistant.ai.MedicalAiService;
import com.yzj.alzassistant.ai.MedicalAiServiceFactory;
import com.yzj.alzassistant.ai.model.message.AiResponseMessage;
import com.yzj.alzassistant.ai.model.message.ToolExecutedMessage;
import com.yzj.alzassistant.ai.model.message.ToolRequestMessage;
import com.yzj.alzassistant.context.UserLocationContext;
import com.yzj.alzassistant.service.AmapCoordinateService;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
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

    @Resource
    private AmapCoordinateService amapCoordinateService;

    /**
     * 运行医疗智能体（TokenStream 流式输出）
     *
     * @param appId        应用 ID，用于对话记忆隔离
     * @param userMessage  用户消息
     * @param userLocation 用户实时位置（经度,纬度，一般为 WGS84），可选；会转为高德坐标后供地图工具使用
     * @return 流式响应，每个 chunk 为 JSON 格式的统一消息
     */
    public Flux<String> runMedicalAgent(long appId, String userMessage, String userLocation) {
        log.info("启动医疗智能体，appId: {}, 用户消息: {}, 用户位置: {}", appId, userMessage, userLocation != null ? "已提供" : "未提供");

        String gcj02 = null;
        String messageToSend = userMessage;
        if (StrUtil.isNotBlank(userLocation)) {
            gcj02 = amapCoordinateService.wgs84ToGcj02(userLocation.trim());
            messageToSend = "【系统】用户已授权实时位置（高德坐标系，经度,纬度）：" + gcj02
                    + "。请在第二步搜索附近医院时，直接将上述坐标作为 searchNearbyHospitals 的 location 参数使用，无需再调用 getUserLocation。\n\n用户描述的症状：\n" + userMessage;
        }

        TokenStream tokenStream = medicalAiServiceFactory.getMedicalAiService().runMedicalAgentStream(appId, messageToSend);
        return processTokenStream(tokenStream, gcj02)
                .doOnComplete(() -> log.info("医疗智能体执行完成"))
                .doOnError(e -> log.error("医疗智能体执行出错", e))
                .doFinally(signalType -> UserLocationContext.clear());
    }

    /**
     * 将 TokenStream 转换为 Flux，输出统一消息格式的 JSON。
     * 在调用 start() 的同一线程设置 UserLocationContext，确保工具执行时能读到。
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, String userLocationGcj02) {
        return Flux.create(sink -> {
            if (StrUtil.isNotBlank(userLocationGcj02)) {
                UserLocationContext.set(userLocationGcj02);
            }
            log.info("医疗智能体开始 TokenStream 流式执行");
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
