package com.yzj.alzassistant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.model.dto.aiModel.AiModelAddRequest;
import com.yzj.alzassistant.model.dto.aiModel.AiModelQueryRequest;
import com.yzj.alzassistant.model.dto.aiModel.AiModelUpdateRequest;
import com.yzj.alzassistant.model.entity.AiModel;
import com.yzj.alzassistant.mapper.AiModelMapper;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.AiModelVO;
import com.yzj.alzassistant.service.AiModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 大模型管理 服务层实现。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@Slf4j
@Service
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel>  implements AiModelService{

    @Override
    public Long addAiModel(AiModelAddRequest addRequest, User loginUser) {
        if (addRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (StrUtil.isBlank(addRequest.getModelName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型名称不能为空");
        }

        if (StrUtil.isBlank(addRequest.getModelKey())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型标识不能为空");
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("modelKey", addRequest.getModelKey());
        if (this.exists(queryWrapper)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型标识已存在");
        }

        AiModel aiModel = new AiModel();
        BeanUtil.copyProperties(addRequest, aiModel);
        aiModel.setStatus("inactive");
        aiModel.setUserId(loginUser.getId());
        aiModel.setCreateTime(LocalDateTime.now());
        aiModel.setEditTime(LocalDateTime.now());
        aiModel.setUpdateTime(LocalDateTime.now());

        boolean result = this.save(aiModel);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存大模型失败");
        }

        return aiModel.getId();
    }

    @Override
    public boolean updateAiModel(AiModelUpdateRequest updateRequest, User loginUser) {
        if (updateRequest == null || updateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiModel aiModel = this.getById(updateRequest.getId());
        if (aiModel == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "大模型不存在");
        }

        if (StrUtil.isNotBlank(updateRequest.getModelKey()) && !updateRequest.getModelKey().equals(aiModel.getModelKey())) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("modelKey", updateRequest.getModelKey())
                    .ne("id", updateRequest.getId());
            if (this.exists(queryWrapper)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型标识已存在");
            }
        }

        BeanUtil.copyProperties(updateRequest, aiModel, "id", "userId", "createTime");
        aiModel.setEditTime(LocalDateTime.now());
        aiModel.setUpdateTime(LocalDateTime.now());

        return this.updateById(aiModel);
    }

    @Override
    public boolean deleteAiModel(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiModel aiModel = this.getById(id);
        if (aiModel == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "大模型不存在");
        }

        return this.removeById(id);
    }

    @Override
    public boolean enableAiModel(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiModel aiModel = this.getById(id);
        if (aiModel == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "大模型不存在");
        }

        if (StrUtil.isBlank(aiModel.getApiKey())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "API密钥不能为空，无法启用");
        }

        aiModel.setStatus("active");
        aiModel.setEditTime(LocalDateTime.now());
        aiModel.setUpdateTime(LocalDateTime.now());

        return this.updateById(aiModel);
    }

    @Override
    public boolean disableAiModel(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiModel aiModel = this.getById(id);
        if (aiModel == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "大模型不存在");
        }

        aiModel.setStatus("inactive");
        aiModel.setEditTime(LocalDateTime.now());
        aiModel.setUpdateTime(LocalDateTime.now());

        return this.updateById(aiModel);
    }

    @Override
    public Page<AiModelVO> listAiModelByPage(AiModelQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        queryWrapper.orderBy("priority", false).orderBy("createTime", false);

        long current = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        
        Page<AiModel> page = this.page(Page.of(current, pageSize), queryWrapper);
        Page<AiModelVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        voPage.setRecords(getAiModelVOList(page.getRecords()));

        return voPage;
    }

    @Override
    public AiModelVO getAiModelById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiModel aiModel = this.getById(id);
        if (aiModel == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "大模型不存在");
        }

        return getAiModelVO(aiModel);
    }

    @Override
    public AiModelVO getAiModelVO(AiModel aiModel) {
        if (aiModel == null) {
            return null;
        }

        AiModelVO vo = new AiModelVO();
        BeanUtil.copyProperties(aiModel, vo);
        return vo;
    }

    @Override
    public List<AiModelVO> getAiModelVOList(List<AiModel> aiModelList) {
        if (aiModelList == null || aiModelList.isEmpty()) {
            return List.of();
        }

        return aiModelList.stream()
                .map(this::getAiModelVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AiModelQueryRequest aiModelQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();

        if (aiModelQueryRequest == null) {
            return queryWrapper;
        }

        String modelName = aiModelQueryRequest.getModelName();
        String modelType = aiModelQueryRequest.getModelType();
        String status = aiModelQueryRequest.getStatus();

        if (StrUtil.isNotBlank(modelName)) {
            queryWrapper.like("modelName", modelName);
        }

        if (StrUtil.isNotBlank(modelType)) {
            queryWrapper.eq("modelType", modelType);
        }

        if (StrUtil.isNotBlank(status)) {
            queryWrapper.eq("status", status);
        }

        return queryWrapper;
    }

}
