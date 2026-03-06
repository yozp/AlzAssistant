package com.yzj.alzassistant.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * “猜你想问”生成请求
 */
@Data
public class AppSuggestionsRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 当前轮用户问题
     */
    private String userQuestion;

    /**
     * 当前轮 AI 回复内容
     */
    private String aiResponse;

    private static final long serialVersionUID = 1L;
}

