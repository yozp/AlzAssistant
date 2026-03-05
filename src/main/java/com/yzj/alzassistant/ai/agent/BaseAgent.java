package com.yzj.alzassistant.ai.agent;

import cn.hutool.core.util.StrUtil;
import com.yzj.alzassistant.ai.agent.model.AgentState;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，管理代理状态和执行流程。
 * 参考 OpenManus 架构设计。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 代理名称
    private String name;
    // 系统提示语
    private String systemPrompt;
    // 下一步提示语
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 最大步骤数
    private int maxSteps = 10;
    // 当前步骤数
    private int currentStep = 0;

    // 智能体独立聊天消息列表
    private List<ChatMessage> messageList = new ArrayList<>();

    /**
     * 同步运行代理程序
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("代理正在运行中，无法重复启动");
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("用户输入不能为空");
        }
        state = AgentState.RUNNING;
        if (StrUtil.isNotBlank(systemPrompt)) {
            messageList.add(SystemMessage.from(systemPrompt));
        }
        messageList.add(UserMessage.from(userPrompt));

        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                log.info("Agent [{}] 第 {} 步开始执行", name, currentStep);
                String stepResult = step();
                results.add("Step " + currentStep + ": " + stepResult);
            }
            if (state != AgentState.FINISHED) {
                state = AgentState.FINISHED;
                results.add("执行终止：已达到最大步数 " + maxSteps);
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Agent [{}] 执行出错", name, e);
            return "执行错误：" + e.getMessage();
        } finally {
            cleanup();
        }
    }

    /**
     * 流式运行代理程序，每个步骤的结果通过 Flux 逐步推送。
     */
    public Flux<String> runStream(String userPrompt) {
        return Flux.create(sink -> {
            CompletableFuture.runAsync(() -> executeAgentLoop(userPrompt, sink));
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * 执行代理程序
     * @param userPrompt
     * @param sink
     */
    private void executeAgentLoop(String userPrompt, FluxSink<String> sink) {
        if (this.state != AgentState.IDLE) {
            sink.error(new RuntimeException("代理正在运行中，无法重复启动"));
            return;
        }
        if (StrUtil.isBlank(userPrompt)) {
            sink.error(new RuntimeException("用户输入不能为空"));
            return;
        }

        state = AgentState.RUNNING;
        if (StrUtil.isNotBlank(systemPrompt)) {
            messageList.add(SystemMessage.from(systemPrompt));
        }
        messageList.add(UserMessage.from(userPrompt));

        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                log.info("Agent [{}] 第 {} 步开始执行 (stream)", name, currentStep);
                String stepResult = step();
                if (StrUtil.isNotBlank(stepResult)) {
                    sink.next(stepResult);
                }
            }
            if (state != AgentState.FINISHED) {
                state = AgentState.FINISHED;
                sink.next("执行终止：已达到最大步数 " + maxSteps);
            }
            sink.complete();
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Agent [{}] 流式执行出错", name, e);
            sink.error(e);
        } finally {
            cleanup();
        }
    }

    /**
     * 执行一个步骤
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可覆盖以释放资源
    }
}
