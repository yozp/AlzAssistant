package com.yzj.alzassistant.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.yzj.alzassistant.model.dto.assessmentRecord.AssessmentRecordAddRequest;
import com.yzj.alzassistant.model.dto.assessmentRecord.AssessmentRecordQueryRequest;
import com.yzj.alzassistant.model.entity.AssessmentRecord;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.AssessmentRecordVO;

/**
 *  服务层。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
public interface AssessmentRecordService extends IService<AssessmentRecord> {

    /**
     * 添加评估记录
     */
    Long addAssessmentRecord(AssessmentRecordAddRequest addRequest, User loginUser);

    /**
     * 分页查询我的评估记录
     */
    Page<AssessmentRecordVO> listMyAssessmentRecordVOByPage(AssessmentRecordQueryRequest queryRequest, User loginUser);

    /**
     * Agent 生成 PDF 报告时添加 AI 评估记录。
     * 由系统自动调用，根据 appId 获取 userId，无需登录用户上下文。
     *
     * @param appId     会话 ID
     * @param addRequest 评估记录请求
     * @return 新增记录 ID
     */
    Long addAssessmentRecordForAgent(Long appId, AssessmentRecordAddRequest addRequest);
}
