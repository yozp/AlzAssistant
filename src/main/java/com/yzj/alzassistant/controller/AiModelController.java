package com.yzj.alzassistant.controller;

import com.mybatisflex.core.paginate.Page;
import com.yzj.alzassistant.annotation.AuthCheck;
import com.yzj.alzassistant.common.BaseResponse;
import com.yzj.alzassistant.common.DeleteRequest;
import com.yzj.alzassistant.common.ResultUtils;
import com.yzj.alzassistant.constant.UserConstant;
import com.yzj.alzassistant.exception.ThrowUtils;
import com.yzj.alzassistant.model.dto.aiModel.AiModelAddRequest;
import com.yzj.alzassistant.model.dto.aiModel.AiModelQueryRequest;
import com.yzj.alzassistant.model.dto.aiModel.AiModelUpdateRequest;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.AiModelVO;
import com.yzj.alzassistant.service.AiModelService;
import com.yzj.alzassistant.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 大模型管理 控制层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@RestController
@RequestMapping("/aiModel")
public class AiModelController {

    @Resource
    private AiModelService aiModelService;

    @Resource
    private UserService userService;

    /**
     * 添加大模型
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addAiModel(@RequestBody AiModelAddRequest addRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long id = aiModelService.addAiModel(addRequest, loginUser);
        return ResultUtils.success(id);
    }

    /**
     * 更新大模型
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAiModel(@RequestBody AiModelUpdateRequest updateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = aiModelService.updateAiModel(updateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除大模型
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAiModel(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = aiModelService.deleteAiModel(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 启用大模型
     */
    @PostMapping("/enable/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> enableAiModel(@PathVariable Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = aiModelService.enableAiModel(id, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 停用大模型
     */
    @PostMapping("/disable/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> disableAiModel(@PathVariable Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = aiModelService.disableAiModel(id, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询大模型
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AiModelVO>> listAiModelByPage(@RequestBody AiModelQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        Page<AiModelVO> page = aiModelService.listAiModelByPage(queryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 根据id获取大模型详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AiModelVO> getAiModelById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, com.yzj.alzassistant.exception.ErrorCode.PARAMS_ERROR);
        AiModelVO vo = aiModelService.getAiModelById(id);
        return ResultUtils.success(vo);
    }

}
