package com.yzj.alzassistant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.exception.ThrowUtils;
import com.yzj.alzassistant.model.dto.assessmentRecord.AssessmentRecordAddRequest;
import com.yzj.alzassistant.model.dto.assessmentRecord.AssessmentRecordQueryRequest;
import com.yzj.alzassistant.model.entity.AssessmentRecord;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.AssessmentRecordVO;
import com.yzj.alzassistant.mapper.AssessmentRecordMapper;
import com.yzj.alzassistant.service.AssessmentRecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评估记录 服务层实现。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@Service
public class AssessmentRecordServiceImpl extends ServiceImpl<AssessmentRecordMapper, AssessmentRecord> implements AssessmentRecordService {

    @Override
    public Long addAssessmentRecord(AssessmentRecordAddRequest addRequest, User loginUser) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        AssessmentRecord assessmentRecord = new AssessmentRecord();
        BeanUtil.copyProperties(addRequest, assessmentRecord);
        assessmentRecord.setUserId(loginUser.getId());
        boolean result = this.save(assessmentRecord);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return assessmentRecord.getId();
    }

    @Override
    public Page<AssessmentRecordVO> listMyAssessmentRecordVOByPage(AssessmentRecordQueryRequest queryRequest, User loginUser) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", loginUser.getId());
        if (queryRequest.getAssessorType() != null) {
            queryWrapper.eq("assessorType", queryRequest.getAssessorType());
        }
        if (queryRequest.getRiskLevel() != null) {
            queryWrapper.eq("riskLevel", queryRequest.getRiskLevel());
        }
        if (queryRequest.getScaleId() != null) {
            queryWrapper.eq("scaleId", queryRequest.getScaleId());
        }
        queryWrapper.orderBy("createTime", false);

        Page<AssessmentRecord> page = this.page(new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize()), queryWrapper);
        Page<AssessmentRecordVO> voPage = new Page<>();
        BeanUtil.copyProperties(page, voPage);
        if (page.getRecords() != null) {
            List<AssessmentRecordVO> voList = page.getRecords().stream()
                    .map(record -> BeanUtil.copyProperties(record, AssessmentRecordVO.class))
                    .collect(Collectors.toList());
            voPage.setRecords(voList);
        }
        return voPage;
    }
}
