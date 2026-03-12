package com.yzj.alzassistant.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yzj.alzassistant.ai.model.message.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 智能体流式响应处理器
 * 将 TokenStream 产生的 JSON 消息转换为可读文本，实时展示 AI 响应和工具调用过程。
 */
@Slf4j
@Component
public class AgentStreamHandler {

    private static final Map<String, String> TOOL_NAME_TO_CN = Map.ofEntries(
            Map.entry("searchNearbyHospitals", "地图搜索"),
            Map.entry("geocode", "地理编码"),
            Map.entry("generateMedicalReport", "PDF 报告生成"),
            Map.entry("googleSearch", "网络搜索"),
            Map.entry("doTerminate", "结束任务")
    );

    /**
     * 处理智能体 TokenStream 产生的 JSON 流，转换为可读文本流。
     *
     * @param originFlux 原始 JSON 流（来自 processTokenStream）
     * @return 可读文本流，用于 SSE 推送和对话历史持久化
     */
    public Flux<String> handle(Flux<String> originFlux) {
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds))
                .filter(StrUtil::isNotEmpty);
    }

    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        try {
            StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
            StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
            if (typeEnum == null) {
                log.warn("未知消息类型: {}", streamMessage.getType());
                return "";
            }
            return switch (typeEnum) {
                case AI_RESPONSE -> {
                    AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                    String data = aiMessage.getData();
                    if (data != null) {
                        chatHistoryStringBuilder.append(data);
                        yield data;
                    }
                    yield "";
                }
                case TOOL_REQUEST -> {
                    ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                    String toolId = toolRequestMessage.getId();
                    if (toolId != null && !seenToolIds.contains(toolId)) {
                        seenToolIds.add(toolId);
                        String toolNameCn = TOOL_NAME_TO_CN.getOrDefault(
                                toolRequestMessage.getName(),
                                toolRequestMessage.getName());
                        String output = "\n\n[选择工具] " + toolNameCn + "\n\n";
                        chatHistoryStringBuilder.append(output);
                        yield output;
                    }
                    yield "";
                }
                case TOOL_EXECUTED -> {
                    ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                    String toolNameCn = TOOL_NAME_TO_CN.getOrDefault(
                            toolExecutedMessage.getName(),
                            toolExecutedMessage.getName());
                    String resultSummary = truncateResult(toolExecutedMessage.getResult());
                    String output = "\n\n[工具执行完成] " + toolNameCn + ": " + resultSummary + "\n\n";
                    chatHistoryStringBuilder.append(output);
                    yield output;
                }
            };
        } catch (Exception e) {
            log.error("解析智能体消息失败: {}", chunk, e);
            return "";
        }
    }

    private String truncateResult(String result) {
        if (result == null || result.isEmpty()) {
            return "已完成";
        }
        if (result.length() <= 80) {
            return result.replace("\n", " ").trim();
        }
        return result.substring(0, 77).replace("\n", " ").trim() + "...";
    }
}
