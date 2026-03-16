package com.yzj.alzassistant.controller;

import com.mybatisflex.core.paginate.Page;
import com.yzj.alzassistant.common.BaseResponse;
import com.yzj.alzassistant.common.ResultUtils;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.exception.ThrowUtils;
import com.yzj.alzassistant.model.dto.assessmentRecord.AssessmentRecordAddRequest;
import com.yzj.alzassistant.model.dto.assessmentRecord.AssessmentRecordQueryRequest;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.AssessmentRecordVO;
import com.yzj.alzassistant.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.yzj.alzassistant.model.entity.AssessmentRecord;
import com.yzj.alzassistant.service.AssessmentRecordService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 评估记录 控制层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@RestController
@RequestMapping("/assessmentRecord")
public class AssessmentRecordController {

    @Autowired
    private AssessmentRecordService assessmentRecordService;

    @Autowired
    private UserService userService;

    /**
     * 添加评估记录
     */
    @PostMapping("/add")
    public BaseResponse<Long> addAssessmentRecord(@RequestBody AssessmentRecordAddRequest addRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long id = assessmentRecordService.addAssessmentRecord(addRequest, loginUser);
        return ResultUtils.success(id);
    }

    /**
     * 分页查询我的评估记录
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AssessmentRecordVO>> listMyAssessmentRecordVOByPage(@RequestBody AssessmentRecordQueryRequest queryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<AssessmentRecordVO> page = assessmentRecordService.listMyAssessmentRecordVOByPage(queryRequest, loginUser);
        return ResultUtils.success(page);
    }

    /**
     * 保存。
     *
     * @param assessmentRecord 
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody AssessmentRecord assessmentRecord) {
        return assessmentRecordService.save(assessmentRecord);
    }

    /**
     * 根据主键删除。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return assessmentRecordService.removeById(id);
    }

    /**
     * 根据主键更新。
     *
     * @param assessmentRecord 
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody AssessmentRecord assessmentRecord) {
        return assessmentRecordService.updateById(assessmentRecord);
    }

    /**
     * 查询所有。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<AssessmentRecord> list() {
        return assessmentRecordService.list();
    }

    /**
     * 根据主键获取。
     *
     * @param id 主键
     * @return 详情
     */
    @GetMapping("getInfo/{id}")
    public AssessmentRecord getInfo(@PathVariable Long id) {
        return assessmentRecordService.getById(id);
    }

    /**
     * 分页查询。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<AssessmentRecord> page(Page<AssessmentRecord> page) {
        return assessmentRecordService.page(page);
    }

}
