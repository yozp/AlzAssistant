package com.yzj.alzassistant.ai;

import com.yzj.alzassistant.ai.tools.DocumentParseTool;
import com.yzj.alzassistant.ai.tools.ImageRecognitionTool;
import com.yzj.alzassistant.ai.tools.MapSearchTool;
import com.yzj.alzassistant.ai.tools.PDFReportTool;
import com.yzj.alzassistant.ai.tools.TerminateTool;
import com.yzj.alzassistant.ai.tools.WebSearchTool;
import com.yzj.alzassistant.service.AiModelSwitchService;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 医疗智能体 AI 服务工厂，创建基于 TokenStream 的 MedicalAiService。
 */
@Configuration
@Slf4j
public class MedicalAiServiceFactory {

    @Resource
    private AiModelSwitchService aiModelSwitchService;

    @Resource
    private MapSearchTool mapSearchTool;

    @Resource
    private PDFReportTool pdfReportTool;

    @Resource
    private WebSearchTool webSearchTool;

    @Resource
    private TerminateTool terminateTool;

    @Resource
    private ImageRecognitionTool imageRecognitionTool;

    @Resource
    private DocumentParseTool documentParseTool;

    /**
     * 获取 MedicalAiService 实例（延迟创建，使用当前活跃的 StreamingChatModel）
     */
    public MedicalAiService getMedicalAiService() {
        StreamingChatModel streamingChatModel = aiModelSwitchService.getCurrentStreamingChatModel();
        if (streamingChatModel == null) {
            log.warn("未找到活跃的 StreamingChatModel，尝试初始化默认模型");
            aiModelSwitchService.initializeDefaultModel();
            streamingChatModel = aiModelSwitchService.getCurrentStreamingChatModel();
        }
        if (streamingChatModel == null) {
            throw new IllegalStateException("无法获取 StreamingChatModel，医疗智能体无法启动");
        }

        return AiServices.builder(MedicalAiService.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(50)
                        .build())
                .tools(mapSearchTool, pdfReportTool, webSearchTool, terminateTool, imageRecognitionTool, documentParseTool)
                .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                        toolExecutionRequest,
                        "Error: there is no tool called " + toolExecutionRequest.name()))
                .build();
    }
}
