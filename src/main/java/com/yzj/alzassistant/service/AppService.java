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
     * @param appId     应用ID
     * @param message   用户消息
     * @param loginUser 当前登录用户
     * @return 生成代码的流式响应
     */
    Flux<String> chatToGen(Long appId, String message, User loginUser);
}
