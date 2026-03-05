package com.yzj.alzassistant.ai.agent;

import com.yzj.alzassistant.ai.tools.MapSearchTool;
import com.yzj.alzassistant.ai.tools.PDFReportTool;
import com.yzj.alzassistant.ai.tools.TerminateTool;
import com.yzj.alzassistant.ai.tools.WebSearchTool;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 医疗智能体服务，负责创建和运行 MedicalAgent 实例。
 */
@Service
@Slf4j
public class MedicalAgentService {

    @Resource
    private ChatModel chatModel;

    @Resource
    private MapSearchTool mapSearchTool;

    @Resource
    private PDFReportTool pdfReportTool;

    @Resource
    private WebSearchTool webSearchTool;

    @Resource
    private TerminateTool terminateTool;

    // 系统提示
    private volatile String agentSystemPrompt;

    /**
     * 加载系统提示词
     */
    private String loadSystemPrompt() {
        if (agentSystemPrompt != null) {
            return agentSystemPrompt;
        }
        try {
            ClassPathResource resource = new ClassPathResource("prompt/agent_system_prompt.txt");
            agentSystemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
            return agentSystemPrompt;
        } catch (IOException e) {
            log.error("加载 Agent 系统提示词失败", e);
            return "你是一个医疗辅助智能体，请分析用户症状、搜索附近医院并生成PDF报告。";
        }
    }

    /**
     * 创建并运行医疗智能体（流式输出）
     * 每次调用创建一个新的 Agent 实例（Agent 是有状态的，不能复用）。
     */
    public Flux<String> runMedicalAgent(String userMessage) {
        log.info("启动医疗智能体，用户消息: {}", userMessage);

        MedicalAgent agent = new MedicalAgent(
                loadSystemPrompt(),
                chatModel,
                mapSearchTool,
                pdfReportTool,
                webSearchTool,
                terminateTool
        );

        return agent.runStream(userMessage)
                .doOnSubscribe(sub -> log.info("医疗智能体开始流式执行"))
                .doOnComplete(() -> log.info("医疗智能体执行完成"))
                .doOnError(e -> log.error("医疗智能体执行出错", e));
    }
}
