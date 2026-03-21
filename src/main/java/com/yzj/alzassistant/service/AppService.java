package com.yzj.alzassistant.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yzj.alzassistant.model.dto.app.AppQueryRequest;
import com.yzj.alzassistant.model.entity.App;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用封装类
     *
     * @param app 应用实体
     * @return 应用封装类
     */
    AppVO getAppVO(App app);

    /**
     * 获取查询包装器
     *
     * @param appQueryRequest 应用查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用封装类列表
     *
     * @param appList 应用实体列表
     * @return 应用封装类列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 应用与用户聊天，生成代码（流式）
     *
     * @param appId        应用ID
     * @param message      用户消息
     * @param loginUser    当前登录用户
     * @param chatType     对话类型（如 agent）
     * @param userLocation 用户实时位置，格式 经度,纬度（可选，用于报告智能体附近医院搜索）
     * @return 生成代码的流式响应
     */
    Flux<String> chatToGen(Long appId, String message, User loginUser, String chatType, String userLocation);

    /**
     * 应用与用户聊天（流式），支持用户附件 JSON（与 chat_history.attachments 一致）。
     *
     * @param attachmentsJson 附件列表 JSON 字符串，可为 null
     */
    Flux<String> chatToGen(Long appId, String message, User loginUser, String chatType, String userLocation,
                           String attachmentsJson);

    /**
     * 同 {@link #chatToGen(Long, String, User, String, String, String)}，普通对话可指定是否启用知识库检索（智能体模式忽略）。
     *
     * @param useRag 为 true 且对话类型为普通聊天时启用 RAG
     */
    Flux<String> chatToGen(Long appId, String message, User loginUser, String chatType, String userLocation,
                           String attachmentsJson, boolean useRag);

    /**
     * 停止指定应用下当前用户的流式对话。
     *
     * @param appId     应用 ID
     * @param loginUser 当前登录用户
     * @return 是否成功触发停止
     */
    boolean stopChat(Long appId, User loginUser);

    /**
     * 异步根据用户首条问题生成并更新对话标题（不超过 10 字）。
     * 不阻塞调用方；失败时保留原有临时标题，仅打日志。
     *
     * @param appId        应用 ID
     * @param userQuestion 用户首条问题内容
     */
    void generateAndUpdateTitleAsync(Long appId, String userQuestion);
}
