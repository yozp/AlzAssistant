package com.yzj.alzassistant.ai.agent;

import com.yzj.alzassistant.ai.agent.model.AgentState;
import com.yzj.alzassistant.ai.tools.TerminateTool;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工具调用代理，实现了 ReAct 的 think/act 循环。
 * think：将消息和工具规格发给 LLM，判断是否需要调用工具。
 * act：执行 LLM 请求的工具调用，将结果反馈到消息列表，并检测终止信号。
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class ToolCallAgent extends ReActAgent {

    private ChatModel chatModel;

    private List<ToolSpecification> toolSpecifications = new ArrayList<>();
    private Map<String, ToolExecutor> toolExecutors = new HashMap<>();

    private ChatResponse toolCallChatResponse;
    private List<ToolExecutionRequest> pendingToolCalls;

    /**
     * 注册工具实例，从 @Tool 注解方法中提取规格和执行器。
     */
    public void registerTools(Object... toolBeans) {
        for (Object toolBean : toolBeans) {
            for (Method method : toolBean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Tool.class)) {
                    ToolSpecification spec = ToolSpecifications.toolSpecificationFrom(method);
                    toolSpecifications.add(spec);
                    toolExecutors.put(spec.name(), new DefaultToolExecutor(toolBean, method));
                    log.info("Agent [{}] 注册工具: {} - {}", getName(), spec.name(), spec.description());
                }
            }
        }
    }

    /**
     * 思考阶段：调用 LLM，判断是否需要调用工具。
     */
    @Override
    public boolean think() {
        // 拼接下一步提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            getMessageList().add(UserMessage.from(getNextStepPrompt()));
        }

        ChatRequest request = ChatRequest.builder()
                .messages(getMessageList())
                .toolSpecifications(toolSpecifications)
                .build();

        log.info("Agent [{}] 正在思考 (第 {} 步)...", getName(), getCurrentStep());

        try {
            ChatResponse response = chatModel.chat(request);
            this.toolCallChatResponse = response;
            AiMessage aiMessage = response.aiMessage();

            String thinkText = aiMessage.text();
            log.info("Agent [{}] 的思考: {}", getName(), thinkText);

            if (aiMessage.hasToolExecutionRequests()) {
                pendingToolCalls = aiMessage.toolExecutionRequests();
                String toolCallInfo = pendingToolCalls.stream()
                        .map(tc -> String.format("工具名称: %s, 参数: %s", tc.name(), tc.arguments()))
                        .collect(Collectors.joining("\n"));
                log.info("Agent [{}] 选择了 {} 个工具:\n{}", getName(), pendingToolCalls.size(), toolCallInfo);
                // 有工具调用时，不手动添加 AiMessage（工具执行流程中会处理）
                return true;
            } else {
                // 无工具调用，记录助手消息
                getMessageList().add(aiMessage);
                return false;
            }
        } catch (Exception e) {
            log.error("Agent [{}] 思考过程出错: {}", getName(), e.getMessage());
            getMessageList().add(AiMessage.from("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    /**
     * 行动阶段：执行工具调用并处理结果，检测终止信号。
     */
    @Override
    public String act() {
        if (pendingToolCalls == null || pendingToolCalls.isEmpty()) {
            return "没有工具调用";
        }

        // 先将 AiMessage（包含工具调用请求）加入消息列表
        AiMessage aiMessage = toolCallChatResponse.aiMessage();
        getMessageList().add(aiMessage);

        StringBuilder resultBuilder = new StringBuilder();
        boolean terminateToolCalled = false;

        for (ToolExecutionRequest toolCall : pendingToolCalls) {
            String toolName = toolCall.name();
            log.info("Agent [{}] 执行工具: {} | 参数: {}", getName(), toolName, toolCall.arguments());

            ToolExecutor executor = toolExecutors.get(toolName);
            if (executor == null) {
                String error = "未找到工具: " + toolName;
                log.warn(error);
                getMessageList().add(ToolExecutionResultMessage.from(toolCall, error));
                resultBuilder.append("工具 ").append(toolName).append(" 执行失败: ").append(error).append("\n");
                continue;
            }

            try {
                String result = executor.execute(toolCall, toolCall.id());
                getMessageList().add(ToolExecutionResultMessage.from(toolCall, result));
                resultBuilder.append("工具 ").append(toolName).append(" 完成了它的任务！结果: ").append(result).append("\n");
                log.info("Agent [{}] 工具 {} 执行成功", getName(), toolName);
            } catch (Exception e) {
                String error = "工具执行失败: " + e.getMessage();
                log.error("Agent [{}] 工具 {} 执行异常", getName(), toolName, e);
                getMessageList().add(ToolExecutionResultMessage.from(toolCall, error));
                resultBuilder.append("工具 ").append(toolName).append(" 执行失败: ").append(error).append("\n");
            }

            // 检测终止工具
            if (TerminateTool.TOOL_NAME.equals(toolName)) {
                terminateToolCalled = true;
            }
        }

        // 如果调用了终止工具，设置完成状态
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }

        pendingToolCalls = null;
        String results = resultBuilder.toString();
        log.info("Agent [{}] act 结果:\n{}", getName(), results);
        return results;
    }
}
