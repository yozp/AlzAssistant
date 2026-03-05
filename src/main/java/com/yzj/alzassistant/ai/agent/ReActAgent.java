package com.yzj.alzassistant.ai.agent;

import com.yzj.alzassistant.ai.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类。
 * 将 step 分解为 think（推理）和 act（行动）两个阶段。
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {

    /**
     * 推理阶段
     */
    public abstract boolean think();

    /**
     * 行动阶段
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                setState(AgentState.FINISHED);
                return "思考完成，无需继续行动";
            }
            return act();
        } catch (Exception e) {
            log.error("Agent [{}] step 执行失败", getName(), e);
            throw new RuntimeException("执行步骤失败：" + e.getMessage(), e);
        }
    }
}
