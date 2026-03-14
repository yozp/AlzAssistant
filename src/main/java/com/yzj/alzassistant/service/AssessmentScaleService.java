package com.yzj.alzassistant.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yzj.alzassistant.model.dto.assessmentScale.AssessmentScaleAddRequest;
import com.yzj.alzassistant.model.dto.assessmentScale.AssessmentScaleQueryRequest;
import com.yzj.alzassistant.model.dto.assessmentScale.AssessmentScaleUpdateRequest;
import com.yzj.alzassistant.model.entity.AssessmentScale;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.AssessmentScaleVO;

import java.util.List;

/**
 * 量表管理 服务层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
public interface AssessmentScaleService extends IService<AssessmentScale> {

    /**
     * 添加量表
     */
    Long addAssessmentScale(AssessmentScaleAddRequest addRequest, User loginUser);

    /**
     * 更新量表
     */
    boolean updateAssessmentScale(AssessmentScaleUpdateRequest updateRequest, User loginUser);

    /**
     * 删除量表
     */
    boolean deleteAssessmentScale(Long id, User loginUser);

    /**
     * 启用量表
     */
    boolean enableAssessmentScale(Long id, User loginUser);

    /**
     * 停用量表
     */
    boolean disableAssessmentScale(Long id, User loginUser);

    /**
     * 分页查询量表
     */
    Page<AssessmentScaleVO> listAssessmentScaleByPage(AssessmentScaleQueryRequest queryRequest);

    /**
     * 根据id获取量表详情
     */
    AssessmentScaleVO getAssessmentScaleById(Long id);

    /**
     * 转化为视图对象
     */
    AssessmentScaleVO getAssessmentScaleVO(AssessmentScale assessmentScale);

    /**
     * 转化为视图对象列表
     */
    List<AssessmentScaleVO> getAssessmentScaleVOList(List<AssessmentScale> assessmentScaleList);

    /**
     * 根据查询请求构造查询语句
     */
    QueryWrapper getQueryWrapper(AssessmentScaleQueryRequest queryRequest);
}
