package com.yzj.alzassistant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yzj.alzassistant.exception.BusinessException;
import com.yzj.alzassistant.exception.ErrorCode;
import com.yzj.alzassistant.mapper.AssessmentScaleMapper;
import com.yzj.alzassistant.model.dto.assessmentScale.AssessmentScaleAddRequest;
import com.yzj.alzassistant.model.dto.assessmentScale.AssessmentScaleQueryRequest;
import com.yzj.alzassistant.model.dto.assessmentScale.AssessmentScaleUpdateRequest;
import com.yzj.alzassistant.model.entity.AssessmentScale;
import com.yzj.alzassistant.model.entity.User;
import com.yzj.alzassistant.model.vo.AssessmentScaleVO;
import com.yzj.alzassistant.service.AssessmentScaleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 量表管理 服务层实现。
 *
 * @author <a href="https://github.com/yozp">yunikon</a>
 */
@Service
public class AssessmentScaleServiceImpl extends ServiceImpl<AssessmentScaleMapper, AssessmentScale> implements AssessmentScaleService {

    private static final String RULE_MODE_BOUNDARIES = "boundaries";

    private static final String RULE_MODE_MANUAL = "manual";

    @Override
    public Long addAssessmentScale(AssessmentScaleAddRequest addRequest, User loginUser) {
        if (addRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isBlank(addRequest.getScaleName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "量表名称不能为空");
        }
        validateStatus(addRequest.getStatus());

        NormalizedScaleData normalizedScaleData = normalizeAndValidateJson(addRequest.getContentJson(), addRequest.getRuleJson());

        AssessmentScale assessmentScale = new AssessmentScale();
        BeanUtil.copyProperties(addRequest, assessmentScale);
        assessmentScale.setContentJson(normalizedScaleData.getNormalizedContentJson());
        assessmentScale.setRuleJson(normalizedScaleData.getNormalizedRuleJson());
        assessmentScale.setTotalScoreMin(normalizedScaleData.getTotalScoreMin());
        assessmentScale.setTotalScoreMax(normalizedScaleData.getTotalScoreMax());
        assessmentScale.setVersionNo(1);
        assessmentScale.setStatus(addRequest.getStatus() == null ? 1 : addRequest.getStatus());
        assessmentScale.setUserId(loginUser.getId());
        assessmentScale.setCreateTime(LocalDateTime.now());
        assessmentScale.setEditTime(LocalDateTime.now());
        assessmentScale.setUpdateTime(LocalDateTime.now());

        boolean result = this.save(assessmentScale);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存量表失败");
        }
        return assessmentScale.getId();
    }

    @Override
    public boolean updateAssessmentScale(AssessmentScaleUpdateRequest updateRequest, User loginUser) {
        if (updateRequest == null || updateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AssessmentScale oldAssessmentScale = this.getById(updateRequest.getId());
        if (oldAssessmentScale == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "量表不存在");
        }
        if (StrUtil.isBlank(updateRequest.getScaleName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "量表名称不能为空");
        }
        validateStatus(updateRequest.getStatus());

        NormalizedScaleData normalizedScaleData = normalizeAndValidateJson(updateRequest.getContentJson(), updateRequest.getRuleJson());

        AssessmentScale assessmentScale = new AssessmentScale();
        BeanUtil.copyProperties(updateRequest, assessmentScale);
        assessmentScale.setContentJson(normalizedScaleData.getNormalizedContentJson());
        assessmentScale.setRuleJson(normalizedScaleData.getNormalizedRuleJson());
        assessmentScale.setTotalScoreMin(normalizedScaleData.getTotalScoreMin());
        assessmentScale.setTotalScoreMax(normalizedScaleData.getTotalScoreMax());
        assessmentScale.setVersionNo((oldAssessmentScale.getVersionNo() == null ? 0 : oldAssessmentScale.getVersionNo()) + 1);
        assessmentScale.setUserId(oldAssessmentScale.getUserId());
        assessmentScale.setCreateTime(oldAssessmentScale.getCreateTime());
        assessmentScale.setEditTime(LocalDateTime.now());
        assessmentScale.setUpdateTime(LocalDateTime.now());

        return this.updateById(assessmentScale);
    }

    @Override
    public boolean deleteAssessmentScale(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AssessmentScale assessmentScale = this.getById(id);
        if (assessmentScale == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "量表不存在");
        }
        return this.removeById(id);
    }

    @Override
    public boolean enableAssessmentScale(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AssessmentScale assessmentScale = this.getById(id);
        if (assessmentScale == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "量表不存在");
        }
        assessmentScale.setStatus(1);
        assessmentScale.setEditTime(LocalDateTime.now());
        assessmentScale.setUpdateTime(LocalDateTime.now());
        return this.updateById(assessmentScale);
    }

    @Override
    public boolean disableAssessmentScale(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AssessmentScale assessmentScale = this.getById(id);
        if (assessmentScale == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "量表不存在");
        }
        assessmentScale.setStatus(0);
        assessmentScale.setEditTime(LocalDateTime.now());
        assessmentScale.setUpdateTime(LocalDateTime.now());
        return this.updateById(assessmentScale);
    }

    @Override
    public Page<AssessmentScaleVO> listAssessmentScaleByPage(AssessmentScaleQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        queryWrapper.orderBy("createTime", false);

        long current = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        Page<AssessmentScale> page = this.page(Page.of(current, pageSize), queryWrapper);
        Page<AssessmentScaleVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        voPage.setRecords(getAssessmentScaleVOList(page.getRecords()));
        return voPage;
    }

    @Override
    public AssessmentScaleVO getAssessmentScaleById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AssessmentScale assessmentScale = this.getById(id);
        if (assessmentScale == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "量表不存在");
        }
        return getAssessmentScaleVO(assessmentScale);
    }

    @Override
    public AssessmentScaleVO getAssessmentScaleVO(AssessmentScale assessmentScale) {
        if (assessmentScale == null) {
            return null;
        }
        AssessmentScaleVO vo = new AssessmentScaleVO();
        BeanUtil.copyProperties(assessmentScale, vo);
        return vo;
    }

    @Override
    public List<AssessmentScaleVO> getAssessmentScaleVOList(List<AssessmentScale> assessmentScaleList) {
        if (assessmentScaleList == null || assessmentScaleList.isEmpty()) {
            return List.of();
        }
        return assessmentScaleList.stream()
                .map(this::getAssessmentScaleVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AssessmentScaleQueryRequest queryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (queryRequest == null) {
            return queryWrapper;
        }
        if (StrUtil.isNotBlank(queryRequest.getScaleName())) {
            queryWrapper.like("scaleName", queryRequest.getScaleName());
        }
        if (queryRequest.getStatus() != null) {
            queryWrapper.eq("status", queryRequest.getStatus());
        }
        if (queryRequest.getUserId() != null) {
            queryWrapper.eq("userId", queryRequest.getUserId());
        }
        return queryWrapper;
    }

    private void validateStatus(Integer status) {
        if (status == null) {
            return;
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态仅支持 0(禁用) 或 1(启用)");
        }
    }

    private NormalizedScaleData normalizeAndValidateJson(String contentJson, String ruleJson) {
        if (StrUtil.isBlank(contentJson)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "量表内容不能为空");
        }
        if (StrUtil.isBlank(ruleJson)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分规则不能为空");
        }
        JSONObject contentObj;
        JSONObject ruleObj;
        try {
            contentObj = JSONUtil.parseObj(contentJson);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "量表内容JSON格式错误");
        }
        try {
            ruleObj = JSONUtil.parseObj(ruleJson);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分规则JSON格式错误");
        }

        JSONArray questionArray = contentObj.getJSONArray("questions");
        if (questionArray == null || questionArray.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "量表至少包含一道题目");
        }

        JSONArray normalizedQuestions = new JSONArray();
        int totalScoreMin = 0;
        int totalScoreMax = 0;
        for (int i = 0; i < questionArray.size(); i++) {
            JSONObject questionObj = questionArray.getJSONObject(i);
            if (questionObj == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目结构错误");
            }
            String title = questionObj.getStr("title");
            if (StrUtil.isBlank(title)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "第" + (i + 1) + "题标题不能为空");
            }
            JSONArray optionArray = questionObj.getJSONArray("options");
            if (optionArray == null || optionArray.size() < 2) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "第" + (i + 1) + "题至少包含2个选项");
            }
            int questionMin = Integer.MAX_VALUE;
            int questionMax = Integer.MIN_VALUE;
            JSONArray normalizedOptions = new JSONArray();
            for (int j = 0; j < optionArray.size(); j++) {
                JSONObject optionObj = optionArray.getJSONObject(j);
                if (optionObj == null) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "第" + (i + 1) + "题第" + (j + 1) + "个选项结构错误");
                }
                String label = optionObj.getStr("label");
                if (StrUtil.isBlank(label)) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "第" + (i + 1) + "题第" + (j + 1) + "个选项文案不能为空");
                }
                Integer score = parseInt(optionObj.get("score"), "第" + (i + 1) + "题第" + (j + 1) + "个选项分数必须是整数");
                questionMin = Math.min(questionMin, score);
                questionMax = Math.max(questionMax, score);
                JSONObject normalizedOption = JSONUtil.createObj();
                normalizedOption.set("optionId", StrUtil.blankToDefault(optionObj.getStr("optionId"), "o" + (j + 1)));
                normalizedOption.set("label", label);
                normalizedOption.set("score", score);
                normalizedOptions.add(normalizedOption);
            }
            totalScoreMin += questionMin;
            totalScoreMax += questionMax;

            JSONObject normalizedQuestion = JSONUtil.createObj();
            normalizedQuestion.set("questionId", StrUtil.blankToDefault(questionObj.getStr("questionId"), "q" + (i + 1)));
            normalizedQuestion.set("title", title);
            normalizedQuestion.set("type", StrUtil.blankToDefault(questionObj.getStr("type"), "single"));
            normalizedQuestion.set("required", questionObj.getBool("required", true));
            normalizedQuestion.set("options", normalizedOptions);
            normalizedQuestions.add(normalizedQuestion);
        }

        RuleNormalizedResult ruleNormalizedResult = normalizeRule(ruleObj, totalScoreMax);

        JSONObject normalizedContentObj = JSONUtil.createObj();
        normalizedContentObj.set("questions", normalizedQuestions);

        JSONObject normalizedRuleObj = JSONUtil.createObj();
        normalizedRuleObj.set("mode", ruleNormalizedResult.getMode());
        normalizedRuleObj.set("boundaries", ruleNormalizedResult.getBoundaries());
        normalizedRuleObj.set("ranges", ruleNormalizedResult.getRanges());

        return new NormalizedScaleData(
                JSONUtil.toJsonStr(normalizedContentObj),
                JSONUtil.toJsonStr(normalizedRuleObj),
                totalScoreMin,
                totalScoreMax
        );
    }

    private RuleNormalizedResult normalizeRule(JSONObject ruleObj, int totalScoreMax) {
        JSONArray boundariesArray = ruleObj.getJSONArray("boundaries");
        JSONArray rangesArray = ruleObj.getJSONArray("ranges");
        String mode = StrUtil.blankToDefault(ruleObj.getStr("mode"), RULE_MODE_MANUAL);

        boolean hasBoundaries = boundariesArray != null && !boundariesArray.isEmpty();
        if (hasBoundaries) {
            List<Integer> boundaries = new ArrayList<>();
            for (int i = 0; i < boundariesArray.size(); i++) {
                Integer boundary = parseInt(boundariesArray.get(i), "规则分界点必须是整数");
                if (boundary < 0) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "规则分界点不能小于0");
                }
                boundaries.add(boundary);
            }
            if (!isStrictlyIncreasing(boundaries)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "规则分界点必须严格递增");
            }
            if (boundaries.get(boundaries.size() - 1) != totalScoreMax) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "规则最大分数必须等于量表可达最大总分");
            }
            if (rangesArray == null || rangesArray.size() != boundaries.size()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "规则区间条数需与分界点条数一致");
            }

            JSONArray normalizedRanges = new JSONArray();
            for (int i = 0; i < boundaries.size(); i++) {
                JSONObject sourceRangeObj = rangesArray.getJSONObject(i);
                if (sourceRangeObj == null) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "规则区间结构错误");
                }
                int min = i == 0 ? 0 : boundaries.get(i - 1) + 1;
                int max = boundaries.get(i);
                normalizedRanges.add(buildNormalizedRange(sourceRangeObj, min, max, i));
            }
            validateRangeOrderAndCoverage(normalizedRanges, totalScoreMax);

            JSONArray normalizedBoundaries = new JSONArray();
            boundaries.forEach(normalizedBoundaries::add);
            return new RuleNormalizedResult(RULE_MODE_BOUNDARIES, normalizedBoundaries, normalizedRanges);
        }

        if (rangesArray == null || rangesArray.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分规则区间不能为空");
        }
        List<JSONObject> rangeList = new ArrayList<>();
        for (int i = 0; i < rangesArray.size(); i++) {
            JSONObject rangeObj = rangesArray.getJSONObject(i);
            if (rangeObj == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "规则区间结构错误");
            }
            Integer min = parseInt(rangeObj.get("min"), "规则区间最小分数必须是整数");
            Integer max = parseInt(rangeObj.get("max"), "规则区间最大分数必须是整数");
            rangeList.add(buildNormalizedRange(rangeObj, min, max, i));
        }
        rangeList.sort(Comparator.comparingInt(range -> range.getInt("min")));
        JSONArray normalizedRanges = new JSONArray();
        rangeList.forEach(normalizedRanges::add);
        validateRangeOrderAndCoverage(normalizedRanges, totalScoreMax);

        JSONArray normalizedBoundaries = new JSONArray();
        for (JSONObject rangeObj : rangeList) {
            normalizedBoundaries.add(rangeObj.getInt("max"));
        }
        return new RuleNormalizedResult(RULE_MODE_MANUAL.equals(mode) ? RULE_MODE_MANUAL : RULE_MODE_MANUAL, normalizedBoundaries, normalizedRanges);
    }

    private JSONObject buildNormalizedRange(JSONObject sourceRangeObj, int min, int max, int index) {
        if (min < 0 || max < min) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "第" + (index + 1) + "条规则区间范围不合法");
        }
        Integer riskLevel = parseInt(sourceRangeObj.get("riskLevel"), "第" + (index + 1) + "条规则风险等级必须是整数");
        if (riskLevel < 0 || riskLevel > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "第" + (index + 1) + "条规则风险等级仅支持0-3");
        }
        String assessmentResult = sourceRangeObj.getStr("assessmentResult");
        if (StrUtil.isBlank(assessmentResult)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "第" + (index + 1) + "条规则评估结果不能为空");
        }
        String suggestion = sourceRangeObj.getStr("suggestion");
        if (StrUtil.isBlank(suggestion)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "第" + (index + 1) + "条规则建议不能为空");
        }
        JSONObject normalizedRange = JSONUtil.createObj();
        normalizedRange.set("min", min);
        normalizedRange.set("max", max);
        normalizedRange.set("riskLevel", riskLevel);
        normalizedRange.set("assessmentResult", assessmentResult);
        normalizedRange.set("suggestion", suggestion);
        return normalizedRange;
    }

    private void validateRangeOrderAndCoverage(JSONArray ranges, int totalScoreMax) {
        if (ranges == null || ranges.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分规则区间不能为空");
        }
        int expectedMin = 0;
        for (int i = 0; i < ranges.size(); i++) {
            JSONObject rangeObj = ranges.getJSONObject(i);
            if (rangeObj == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "规则区间结构错误");
            }
            Integer min = rangeObj.getInt("min");
            Integer max = rangeObj.getInt("max");
            if (min == null || max == null || min != expectedMin || max < min) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "规则区间必须连续且不重叠");
            }
            expectedMin = max + 1;
        }
        JSONObject lastRange = ranges.getJSONObject(ranges.size() - 1);
        if (lastRange.getInt("max") != totalScoreMax) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "规则最大分数必须等于量表可达最大总分");
        }
    }

    private boolean isStrictlyIncreasing(List<Integer> values) {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) <= values.get(i - 1)) {
                return false;
            }
        }
        return true;
    }

    private Integer parseInt(Object value, String errorMessage) {
        if (value == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
        }
        try {
            if (value instanceof Number number) {
                return number.intValue();
            }
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
        }
    }

    @lombok.Getter
    @AllArgsConstructor
    private static class NormalizedScaleData {

        private final String normalizedContentJson;

        private final String normalizedRuleJson;

        private final Integer totalScoreMin;

        private final Integer totalScoreMax;
    }

    @lombok.Getter
    @AllArgsConstructor
    private static class RuleNormalizedResult {

        private final String mode;

        private final JSONArray boundaries;

        private final JSONArray ranges;
    }

}
