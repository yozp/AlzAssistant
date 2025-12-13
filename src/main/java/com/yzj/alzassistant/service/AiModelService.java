package com.yzj.alzassistant.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yzj.alzassistant.model.dto.aiModel.AiModelAddRequest;
import com.yzj.alzassistant.model.dto.aiModel.AiModelQueryRequest;
import com.yzj.alzassistant.model.dto.aiModel.AiModelUpdateRequest;
import com.yzj.alzassistant.model.entity.AiModel;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.AiModelVO;

import java.util.List;

/**
 * 大模型管理 服务层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
public interface AiModelService extends IService<AiModel> {

    /**
     * 添加大模型
     */
    Long addAiModel(AiModelAddRequest addRequest, User loginUser);

    /**
     * 更新大模型
     */
    boolean updateAiModel(AiModelUpdateRequest updateRequest, User loginUser);

    /**
     * 删除大模型
     */
    boolean deleteAiModel(Long id, User loginUser);

    /**
     * 启用大模型
     */
    boolean enableAiModel(Long id, User loginUser);

    /**
     * 停用大模型
     */
    boolean disableAiModel(Long id, User loginUser);

    /**
     * 分页查询大模型
     */
    Page<AiModelVO> listAiModelByPage(AiModelQueryRequest queryRequest);

    /**
     * 根据id获取大模型详情
     */
    AiModelVO getAiModelById(Long id);

    /**
     * 转化为视图对象
     */
    AiModelVO getAiModelVO(AiModel aiModel);

    /**
     * 转化为视图对象列表
     */
    List<AiModelVO> getAiModelVOList(List<AiModel> aiModelList);

    /**
     * 根据查询请求构造查询语句
     */
    QueryWrapper getQueryWrapper(AiModelQueryRequest aiModelQueryRequest);

}
