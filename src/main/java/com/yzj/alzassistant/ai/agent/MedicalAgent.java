package com.yzj.alzassistant.ai.agent;

import dev.langchain4j.model.chat.ChatModel;

/**
 * 医疗智能体，专用于阿尔茨海默症症状分析、医院搜索和报告生成。
 * 继承 ToolCallAgent，配置专用工具集（含 TerminateTool）。
 */
public class MedicalAgent extends ToolCallAgent {

    public MedicalAgent(String systemPrompt, ChatModel chatModel, Object... tools) {
        setName("MedicalAgent");
        setSystemPrompt(systemPrompt);
        setChatModel(chatModel);
        setMaxSteps(15);
        setNextStepPrompt("根据当前进展，继续执行下一步。如果所有步骤都已完成，请调用 doTerminate 工具结束任务。");
        registerTools(tools);
    }
}
