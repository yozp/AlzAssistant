package com.yzj.alzassistant.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yzj.alzassistant.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.yzj.alzassistant.model.entity.ChatHistory;
import com.yzj.alzassistant.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话历史消息
     *
     * @param appId      应用id
     * @param message    消息
     * @param messageType 消息类型
     * @param userId     用户id
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用id删除对话历史消息
     *
     * @param appId 应用id
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    boolean deleteByAppId(Long appId);

    /**
     * 游标分页查询对话历史
     *
     * @param chatHistoryQueryRequest 对话历史查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param loginUser      登录用户
     * @return 对话历史分页
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 加载应用的对话历史到内存中
     *
     * @param appId       应用ID
     * @param chatMemory  消息窗口聊天内存
     * @param maxCount    最大加载数量
     * @return 加载的对话历史数量
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
