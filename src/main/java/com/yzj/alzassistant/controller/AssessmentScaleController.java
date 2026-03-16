package com.yzj.alzassistant.controller;

import com.mybatisflex.core.paginate.Page;
import com.yzj.alzassistant.annotation.AuthCheck;
import com.yzj.alzassistant.common.BaseResponse;
import com.yzj.alzassistant.common.DeleteRequest;
import com.yzj.alzassistant.common.ResultUtils;
import com.yzj.alzassistant.constant.UserConstant;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.exception.ThrowUtils;
import com.yzj.alzassistant.model.dto.assessmentScale.AssessmentScaleAddRequest;
import com.yzj.alzassistant.model.dto.assessmentScale.AssessmentScaleQueryRequest;
import com.yzj.alzassistant.model.dto.assessmentScale.AssessmentScaleUpdateRequest;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.AssessmentScaleVO;
import com.yzj.alzassistant.service.AssessmentScaleService;
import com.yzj.alzassistant.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

/**
 * 量表管理 控制层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@RestController
@RequestMapping("/assessmentScale")
public class AssessmentScaleController {

    @Resource
    private AssessmentScaleService assessmentScaleService;

    @Resource
    private UserService userService;

    /**
     * 添加量表
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addAssessmentScale(@RequestBody AssessmentScaleAddRequest addRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long id = assessmentScaleService.addAssessmentScale(addRequest, loginUser);
        return ResultUtils.success(id);
    }

    /**
     * 更新量表
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAssessmentScale(@RequestBody AssessmentScaleUpdateRequest updateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = assessmentScaleService.updateAssessmentScale(updateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除量表
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAssessmentScale(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = assessmentScaleService.deleteAssessmentScale(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 启用量表
     */
    @PostMapping("/enable/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> enableAssessmentScale(@PathVariable Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = assessmentScaleService.enableAssessmentScale(id, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 停用量表
     */
    @PostMapping("/disable/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> disableAssessmentScale(@PathVariable Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = assessmentScaleService.disableAssessmentScale(id, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询量表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AssessmentScaleVO>> listAssessmentScaleByPage(@RequestBody AssessmentScaleQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AssessmentScaleVO> page = assessmentScaleService.listAssessmentScaleByPage(queryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 根据id获取量表详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AssessmentScaleVO> getAssessmentScaleById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        AssessmentScaleVO vo = assessmentScaleService.getAssessmentScaleById(id);
        return ResultUtils.success(vo);
    }

    /**
     * 分页查询量表（普通用户可用，仅查询已启用的量表）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<AssessmentScaleVO>> listAssessmentScaleVOByPage(@RequestBody AssessmentScaleQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        // 强制只查询启用的量表
        queryRequest.setStatus(1);
        Page<AssessmentScaleVO> page = assessmentScaleService.listAssessmentScaleByPage(queryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 根据id获取量表详情（普通用户可用）
     */
    @GetMapping("/get/vo")
    public BaseResponse<AssessmentScaleVO> getAssessmentScaleVOById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        AssessmentScaleVO vo = assessmentScaleService.getAssessmentScaleById(id);
        // 如果量表未启用，普通用户不能查看
        ThrowUtils.throwIf(vo.getStatus() != 1, ErrorCode.NO_AUTH_ERROR, "该量表未启用");
        return ResultUtils.success(vo);
    }

}
