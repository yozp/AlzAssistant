package com.yzj.alzassistant.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 终止任务工具，用于智能体完成任务后显式终止执行循环。
 */
@Component
public class TerminateTool {

    public static final String TOOL_NAME = "doTerminate";

    @Tool(name = "doTerminate", value = "当所有任务完成或无法继续执行时，调用此工具终止交互。当你完成了所有工作步骤后，必须调用此工具来结束任务。")
    public String doTerminate() {
        return "任务结束";
    }
}
