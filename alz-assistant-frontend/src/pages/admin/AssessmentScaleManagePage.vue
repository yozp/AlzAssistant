<template>
  <div id="assessmentScaleManagePage">
    <a-form :model="searchParams" layout="inline" @finish="doSearch">
      <a-form-item label="量表名称">
        <a-input
          v-model:value="searchParams.scaleName"
          placeholder="请输入量表名称"
          allow-clear
          @pressEnter="doSearch"
        />
      </a-form-item>
      <a-form-item label="状态">
        <a-select
          v-model:value="searchParams.status"
          placeholder="请选择状态"
          style="width: 120px"
          allow-clear
          :get-popup-container="(triggerNode: HTMLElement) => triggerNode.parentElement"
        >
          <a-select-option :value="1">启用</a-select-option>
          <a-select-option :value="0">停用</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">查询</a-button>
        <a-button style="margin-left: 8px" @click="doReset">重置</a-button>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="openAddModal">新增量表</a-button>
      </a-form-item>
    </a-form>

    <a-divider />

    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      :loading="loading"
      @change="handleTableChange"
      row-key="id"
      :scroll="{ x: 1400 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'scoreRange'">
          {{ getScoreRange(record) }}
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="default" size="small" @click="openViewModal(record)">查看</a-button>
            <a-button type="primary" size="small" @click="openEditModal(record)">编辑</a-button>
            <a-button
              v-if="record.status === 0"
              type="default"
              size="small"
              @click="handleEnable(record)"
            >
              启用
            </a-button>
            <a-button
              v-if="record.status === 1"
              type="default"
              size="small"
              danger
              @click="handleDisable(record)"
            >
              停用
            </a-button>
            <a-popconfirm title="确认删除该量表？" @confirm="handleDelete(record)">
              <a-button type="default" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="editModalVisible"
      :title="isEdit ? '编辑量表' : '新增量表'"
      :confirm-loading="submitLoading"
      width="1000px"
      @ok="handleModalOk"
    >
      <a-form :model="formState" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="10">
            <a-form-item label="量表名称" required>
              <a-input v-model:value="formState.scaleName" placeholder="请输入量表名称" />
            </a-form-item>
          </a-col>
          <a-col :span="10">
            <a-form-item label="状态">
              <a-select
                v-model:value="formState.status"
                :get-popup-container="(triggerNode: HTMLElement) => triggerNode.parentElement"
              >
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">停用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="量表简介">
          <a-textarea v-model:value="formState.scaleIntro" :rows="3" placeholder="请输入量表简介" />
        </a-form-item>

        <a-divider orientation="left">量表内容</a-divider>
        <div
          v-for="(question, qIndex) in formState.questions"
          :key="question.questionId || qIndex"
          class="question-card"
        >
          <div class="question-header">
            <a-space>
              <span class="question-title">题目 {{ qIndex + 1 }}</span>
              <a-button danger size="small" @click="removeQuestion(qIndex)">删除题目</a-button>
            </a-space>
          </div>
          <a-row :gutter="12">
            <a-col :span="18">
              <a-input
                v-model:value="question.title"
                placeholder="请输入题目内容"
                class="mb-12"
              />
            </a-col>
            <a-col :span="6">
              <a-checkbox v-model:checked="question.required">必答</a-checkbox>
            </a-col>
          </a-row>
          <div
            v-for="(option, oIndex) in question.options"
            :key="option.optionId || `${qIndex}-${oIndex}`"
            class="option-row"
          >
            <a-row :gutter="8">
              <a-col :span="14">
                <a-input v-model:value="option.label" placeholder="选项内容" />
              </a-col>
              <a-col :span="8">
                <a-input-number
                  v-model:value="option.score"
                  style="width: 100%"
                  :step="1"
                  placeholder="分值"
                />
              </a-col>
              <a-col :span="2">
                <a-button
                  danger
                  type="text"
                  :disabled="question.options.length <= 2"
                  @click="removeOption(qIndex, oIndex)"
                >
                  删
                </a-button>
              </a-col>
            </a-row>
          </div>
          <a-button type="dashed" block @click="addOption(qIndex)">
            <PlusOutlined />
            新增选项
          </a-button>
        </div>
        <a-button type="dashed" block @click="addQuestion">
          <PlusOutlined />
          新增题目
        </a-button>

        <a-divider orientation="left">判断规则</a-divider>
        <a-radio-group v-model:value="formState.ruleMode" style="margin-bottom: 12px">
          <a-radio-button value="boundaries">分界点模式</a-radio-button>
          <a-radio-button value="manual">手动区间模式</a-radio-button>
        </a-radio-group>

        <div v-if="formState.ruleMode === 'boundaries'">
          <a-form-item label="分界点（例如：1,3,5）" required>
            <a-input
              v-model:value="formState.boundariesInput"
              placeholder="请按英文逗号分隔分界点"
              @blur="syncRangesByBoundaries"
            />
          </a-form-item>
          <div v-for="(range, index) in formState.ranges" :key="`b-${index}`" class="rule-row">
            <a-row :gutter="8" align="middle">
              <a-col :span="4">
                <a-tag color="blue">{{ range.min }} - {{ range.max }}</a-tag>
              </a-col>
              <a-col :span="4">
                <a-select
                  v-model:value="range.riskLevel"
                  style="width: 100%"
                  placeholder="风险等级"
                  :get-popup-container="(triggerNode: HTMLElement) => triggerNode.parentElement"
                >
                  <a-select-option :value="0">无</a-select-option>
                  <a-select-option :value="1">低</a-select-option>
                  <a-select-option :value="2">中</a-select-option>
                  <a-select-option :value="3">高</a-select-option>
                </a-select>
              </a-col>
              <a-col :span="8">
                <a-input v-model:value="range.assessmentResult" placeholder="评估结果" />
              </a-col>
              <a-col :span="8">
                <a-input v-model:value="range.suggestion" placeholder="建议" />
              </a-col>
            </a-row>
          </div>
        </div>

        <div v-else>
          <div v-for="(range, index) in formState.ranges" :key="`m-${index}`" class="rule-row">
            <a-row :gutter="8" align="middle">
              <a-col :span="3">
                <a-input-number v-model:value="range.min" style="width: 100%" placeholder="最小分" />
              </a-col>
              <a-col :span="3">
                <a-input-number v-model:value="range.max" style="width: 100%" placeholder="最大分" />
              </a-col>
              <a-col :span="4">
                <a-select
                  v-model:value="range.riskLevel"
                  style="width: 100%"
                  placeholder="风险等级"
                  :get-popup-container="(triggerNode: HTMLElement) => triggerNode.parentElement"
                >
                  <a-select-option :value="0">无</a-select-option>
                  <a-select-option :value="1">低</a-select-option>
                  <a-select-option :value="2">中</a-select-option>
                  <a-select-option :value="3">高</a-select-option>
                </a-select>
              </a-col>
              <a-col :span="6">
                <a-input v-model:value="range.assessmentResult" placeholder="评估结果" />
              </a-col>
              <a-col :span="6">
                <a-input v-model:value="range.suggestion" placeholder="建议" />
              </a-col>
              <a-col :span="2">
                <a-button
                  danger
                  type="text"
                  :disabled="formState.ranges.length <= 1"
                  @click="removeRuleRange(index)"
                >
                  删
                </a-button>
              </a-col>
            </a-row>
          </div>
          <a-button type="dashed" block @click="addRuleRange">
            <PlusOutlined />
            新增规则区间
          </a-button>
        </div>
      </a-form>
    </a-modal>

    <a-modal v-model:open="viewModalVisible" title="查看量表" width="1000px" :footer="null">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="量表名称">{{ viewState.scaleName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="viewState.status === 1 ? 'green' : 'red'">
            {{ viewState.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="分数范围">{{ viewState.scoreRange }}</a-descriptions-item>
        <a-descriptions-item label="版本">{{ viewState.versionNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="量表简介" :span="2">
          {{ viewState.scaleIntro || '-' }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider orientation="left">题目预览</a-divider>
      <div v-for="(question, qIndex) in viewState.questions" :key="question.questionId || qIndex" class="view-question">
        <div class="view-question-title">{{ qIndex + 1 }}. {{ question.title }}</div>
        <div v-for="(option, oIndex) in question.options" :key="option.optionId || oIndex" class="view-option">
          {{ oIndex + 1 }}）{{ option.label }}（{{ option.score }}分）
        </div>
      </div>

      <a-divider orientation="left">规则预览</a-divider>
      <a-table
        :columns="ruleColumns"
        :data-source="viewState.ranges"
        size="small"
        :pagination="false"
        row-key="key"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PlusOutlined } from '@ant-design/icons-vue'
import {
  addAssessmentScale,
  deleteAssessmentScale,
  disableAssessmentScale,
  enableAssessmentScale,
  getAssessmentScaleById,
  listAssessmentScaleByPage,
  updateAssessmentScale,
} from '@/api/assessmentScaleController'

type QuestionOption = {
  optionId?: string
  label?: string
  score?: number
}

type QuestionItem = {
  questionId?: string
  title?: string
  type?: string
  required?: boolean
  options: QuestionOption[]
}

type RuleRange = {
  min?: number
  max?: number
  riskLevel?: number
  assessmentResult?: string
  suggestion?: string
}

const columns = [
  { title: 'ID', dataIndex: 'id', width: 170 },
  { title: '量表名称', dataIndex: 'scaleName', width: 220, ellipsis: true },
  { title: '简介', dataIndex: 'scaleIntro', width: 300, ellipsis: true },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '分数范围', dataIndex: 'scoreRange', width: 140 },
  { title: '版本', dataIndex: 'versionNo', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 300, fixed: 'right' },
]

const ruleColumns = [
  { title: '分数区间', dataIndex: 'rangeText' },
  { title: '风险等级', dataIndex: 'riskText' },
  { title: '评估结果', dataIndex: 'assessmentResult' },
  { title: '建议', dataIndex: 'suggestion' },
]

const data = ref<API.AssessmentScaleVO[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

const searchParams = reactive<API.AssessmentScaleQueryRequest>({
  scaleName: '',
  status: undefined,
})

const defaultQuestion = (): QuestionItem => ({
  questionId: '',
  title: '',
  type: 'single',
  required: true,
  options: [
    { optionId: '', label: '', score: 0 },
    { optionId: '', label: '', score: 0 },
  ],
})

const defaultRange = (): RuleRange => ({
  min: 0,
  max: 0,
  riskLevel: 0,
  assessmentResult: '',
  suggestion: '',
})

const formState = reactive({
  id: undefined as string | number | undefined,
  scaleName: '',
  scaleIntro: '',
  status: 1,
  questions: [defaultQuestion()] as QuestionItem[],
  ruleMode: 'boundaries',
  boundariesInput: '',
  ranges: [defaultRange()] as RuleRange[],
})

const editModalVisible = ref(false)
const isEdit = ref(false)

const viewModalVisible = ref(false)
const viewState = reactive({
  scaleName: '',
  scaleIntro: '',
  status: 1,
  scoreRange: '-',
  versionNo: 0,
  questions: [] as QuestionItem[],
  ranges: [] as Array<RuleRange & { key: string; rangeText: string; riskText: string }>,
})

const riskTextMap: Record<number, string> = {
  0: '无',
  1: '低',
  2: '中',
  3: '高',
}

const getRecordId = (id?: string | number) => {
  if (id === undefined || id === null || id === '') {
    return undefined
  }
  return String(id)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await listAssessmentScaleByPage({
      ...searchParams,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    })
    if (res.data && res.data.code === 0) {
      const pageData = res.data.data
      data.value = pageData?.records || []
      pagination.total = Number(pageData?.totalRow || 0)
    } else {
      message.error('加载失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('加载失败：' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

const doSearch = () => {
  pagination.current = 1
  loadData()
}

const doReset = () => {
  searchParams.scaleName = ''
  searchParams.status = undefined
  pagination.current = 1
  loadData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const formatTime = (timeStr?: string) => {
  if (!timeStr) return '-'
  return dayjs(timeStr).format('YYYY-MM-DD HH:mm:ss')
}

const getScoreRange = (record: API.AssessmentScaleVO) => {
  if (record.totalScoreMin === undefined || record.totalScoreMax === undefined) return '-'
  return `${record.totalScoreMin} - ${record.totalScoreMax}`
}

const resetForm = () => {
  formState.id = undefined
  formState.scaleName = ''
  formState.scaleIntro = ''
  formState.status = 1
  formState.questions = [defaultQuestion()]
  formState.ruleMode = 'boundaries'
  formState.boundariesInput = ''
  formState.ranges = [defaultRange()]
}

const addQuestion = () => {
  formState.questions.push(defaultQuestion())
}

const removeQuestion = (qIndex: number) => {
  if (formState.questions.length <= 1) {
    message.warning('至少保留一道题目')
    return
  }
  formState.questions.splice(qIndex, 1)
}

const addOption = (qIndex: number) => {
  formState.questions[qIndex]?.options.push({ optionId: '', label: '', score: 0 })
}

const removeOption = (qIndex: number, oIndex: number) => {
  const question = formState.questions[qIndex]
  if (!question) return
  if (question.options.length <= 2) {
    message.warning('每道题至少保留2个选项')
    return
  }
  question.options.splice(oIndex, 1)
}

const addRuleRange = () => {
  formState.ranges.push(defaultRange())
}

const removeRuleRange = (index: number) => {
  if (formState.ranges.length <= 1) {
    message.warning('至少保留一个规则区间')
    return
  }
  formState.ranges.splice(index, 1)
}

const parseBoundaryInput = (value: string) => {
  if (!value.trim()) {
    return { valid: false, list: [] as number[], message: '请填写分界点' }
  }
  const parts = value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
  if (!parts.length) {
    return { valid: false, list: [] as number[], message: '请填写分界点' }
  }
  const list: number[] = []
  for (const part of parts) {
    const num = Number(part)
    if (!Number.isInteger(num) || num < 0) {
      return { valid: false, list: [], message: '分界点必须是大于等于0的整数' }
    }
    list.push(num)
  }
  for (let i = 1; i < list.length; i++) {
    if (list[i]! <= list[i - 1]!) {
      return { valid: false, list: [], message: '分界点必须严格递增' }
    }
  }
  return { valid: true, list, message: '' }
}

const syncRangesByBoundaries = () => {
  if (formState.ruleMode !== 'boundaries') {
    return
  }
  const parsed = parseBoundaryInput(formState.boundariesInput)
  if (!parsed.valid) {
    return
  }
  const oldRanges = [...formState.ranges]
  formState.ranges = parsed.list.map((max, index) => {
    const old = oldRanges[index] || defaultRange()
    return {
      min: index === 0 ? 0 : parsed.list[index - 1]! + 1,
      max,
      riskLevel: old.riskLevel ?? 0,
      assessmentResult: old.assessmentResult || '',
      suggestion: old.suggestion || '',
    }
  })
}

const buildPayload = () => {
  if (!formState.scaleName.trim()) {
    throw new Error('请填写量表名称')
  }
  if (!formState.questions.length) {
    throw new Error('请至少添加一道题目')
  }
  const normalizedQuestions: API.AssessmentScaleQuestion[] = formState.questions.map((question, qIndex) => {
    if (!question.title?.trim()) {
      throw new Error(`第${qIndex + 1}题标题不能为空`)
    }
    if (!question.options || question.options.length < 2) {
      throw new Error(`第${qIndex + 1}题至少包含2个选项`)
    }
    const normalizedOptions: API.AssessmentScaleQuestionOption[] = question.options.map((option, oIndex) => {
      if (!option.label?.trim()) {
        throw new Error(`第${qIndex + 1}题第${oIndex + 1}个选项文案不能为空`)
      }
      if (option.score === undefined || option.score === null || !Number.isInteger(Number(option.score))) {
        throw new Error(`第${qIndex + 1}题第${oIndex + 1}个选项分值必须是整数`)
      }
      return {
        optionId: option.optionId?.trim() || `o${oIndex + 1}`,
        label: option.label.trim(),
        score: Number(option.score),
      }
    })
    return {
      questionId: question.questionId?.trim() || `q${qIndex + 1}`,
      title: question.title.trim(),
      type: 'single',
      required: question.required !== false,
      options: normalizedOptions,
    }
  })

  let normalizedRanges: API.AssessmentScaleRuleRange[] = []
  let boundaries: number[] = []
  if (formState.ruleMode === 'boundaries') {
    const parsed = parseBoundaryInput(formState.boundariesInput)
    if (!parsed.valid) {
      throw new Error(parsed.message)
    }
    boundaries = parsed.list
    if (formState.ranges.length !== boundaries.length) {
      syncRangesByBoundaries()
    }
    normalizedRanges = formState.ranges.map((range, index) => {
      if (range.riskLevel === undefined || range.riskLevel === null) {
        throw new Error(`请完善第${index + 1}段风险等级`)
      }
      if (!range.assessmentResult?.trim()) {
        throw new Error(`请完善第${index + 1}段评估结果`)
      }
      if (!range.suggestion?.trim()) {
        throw new Error(`请完善第${index + 1}段建议`)
      }
      return {
        min: index === 0 ? 0 : boundaries[index - 1]! + 1,
        max: boundaries[index],
        riskLevel: Number(range.riskLevel),
        assessmentResult: range.assessmentResult.trim(),
        suggestion: range.suggestion.trim(),
      }
    })
  } else {
    if (!formState.ranges.length) {
      throw new Error('请至少添加一个规则区间')
    }
    normalizedRanges = formState.ranges.map((range, index) => {
      if (
        range.min === undefined ||
        range.max === undefined ||
        !Number.isInteger(Number(range.min)) ||
        !Number.isInteger(Number(range.max))
      ) {
        throw new Error(`第${index + 1}段分值范围不合法`)
      }
      if (Number(range.max) < Number(range.min)) {
        throw new Error(`第${index + 1}段最大分不能小于最小分`)
      }
      if (range.riskLevel === undefined || range.riskLevel === null) {
        throw new Error(`请完善第${index + 1}段风险等级`)
      }
      if (!range.assessmentResult?.trim()) {
        throw new Error(`请完善第${index + 1}段评估结果`)
      }
      if (!range.suggestion?.trim()) {
        throw new Error(`请完善第${index + 1}段建议`)
      }
      return {
        min: Number(range.min),
        max: Number(range.max),
        riskLevel: Number(range.riskLevel),
        assessmentResult: range.assessmentResult.trim(),
        suggestion: range.suggestion.trim(),
      }
    })
    normalizedRanges.sort((a, b) => Number(a.min) - Number(b.min))
    boundaries = normalizedRanges.map((item) => Number(item.max))
  }

  return {
    scaleName: formState.scaleName.trim(),
    scaleIntro: formState.scaleIntro?.trim() || '',
    status: formState.status,
    contentJson: JSON.stringify({
      questions: normalizedQuestions,
    } as API.AssessmentScaleContent),
    ruleJson: JSON.stringify({
      mode: formState.ruleMode,
      boundaries,
      ranges: normalizedRanges,
    } as API.AssessmentScaleRule),
  }
}

const openAddModal = () => {
  isEdit.value = false
  resetForm()
  editModalVisible.value = true
}

const parseScaleDetailToForm = (scale: API.AssessmentScaleVO) => {
  resetForm()
  formState.id = scale.id
  formState.scaleName = scale.scaleName || ''
  formState.scaleIntro = scale.scaleIntro || ''
  formState.status = scale.status ?? 1

  const contentObj = scale.contentJson ? JSON.parse(scale.contentJson) : { questions: [] }
  const questions = (contentObj.questions || []) as API.AssessmentScaleQuestion[]
  formState.questions =
    questions.length > 0
      ? questions.map((question) => ({
          questionId: question.questionId || '',
          title: question.title || '',
          type: question.type || 'single',
          required: question.required !== false,
          options:
            question.options?.map((option) => ({
              optionId: option.optionId || '',
              label: option.label || '',
              score: Number(option.score ?? 0),
            })) || [],
        }))
      : [defaultQuestion()]

  const ruleObj = scale.ruleJson ? JSON.parse(scale.ruleJson) : { mode: 'boundaries', boundaries: [], ranges: [] }
  const mode = ruleObj.mode === 'manual' ? 'manual' : 'boundaries'
  const boundaries = (ruleObj.boundaries || []) as number[]
  const ranges = (ruleObj.ranges || []) as API.AssessmentScaleRuleRange[]

  formState.ruleMode = mode
  formState.boundariesInput = boundaries.join(',')
  formState.ranges =
    ranges.length > 0
      ? ranges.map((range) => ({
          min: Number(range.min ?? 0),
          max: Number(range.max ?? 0),
          riskLevel: Number(range.riskLevel ?? 0),
          assessmentResult: range.assessmentResult || '',
          suggestion: range.suggestion || '',
        }))
      : [defaultRange()]
}

const openEditModal = async (record: API.AssessmentScaleVO) => {
  const id = getRecordId(record.id)
  if (!id) return
  isEdit.value = true
  try {
    const res = await getAssessmentScaleById({ id })
    if (res.data && res.data.code === 0 && res.data.data) {
      parseScaleDetailToForm(res.data.data)
      editModalVisible.value = true
    } else {
      message.error('获取详情失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('获取详情失败：' + (error.message || '网络错误'))
  }
}

const handleModalOk = async () => {
  submitLoading.value = true
  try {
    const payload = buildPayload()
    let res
    if (isEdit.value && formState.id) {
      res = await updateAssessmentScale({
        id: formState.id,
        ...payload,
      })
    } else {
      res = await addAssessmentScale(payload)
    }
    if (res.data && res.data.code === 0) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      editModalVisible.value = false
      loadData()
    } else {
      message.error('操作失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: any) {
    message.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (record: API.AssessmentScaleVO) => {
  const id = getRecordId(record.id)
  if (!id) return
  try {
    const res = await deleteAssessmentScale({ id: id as any })
    if (res.data && res.data.code === 0) {
      message.success('删除成功')
      loadData()
    } else {
      message.error('删除失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('删除失败：' + (error.message || '网络错误'))
  }
}

const handleEnable = async (record: API.AssessmentScaleVO) => {
  const id = getRecordId(record.id)
  if (!id) return
  try {
    const res = await enableAssessmentScale({ id })
    if (res.data && res.data.code === 0) {
      message.success('启用成功')
      loadData()
    } else {
      message.error('启用失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('启用失败：' + (error.message || '网络错误'))
  }
}

const handleDisable = async (record: API.AssessmentScaleVO) => {
  const id = getRecordId(record.id)
  if (!id) return
  try {
    const res = await disableAssessmentScale({ id })
    if (res.data && res.data.code === 0) {
      message.success('停用成功')
      loadData()
    } else {
      message.error('停用失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('停用失败：' + (error.message || '网络错误'))
  }
}

const openViewModal = async (record: API.AssessmentScaleVO) => {
  const id = getRecordId(record.id)
  if (!id) return
  try {
    const res = await getAssessmentScaleById({ id })
    if (res.data && res.data.code === 0 && res.data.data) {
      const detail = res.data.data
      viewState.scaleName = detail.scaleName || ''
      viewState.scaleIntro = detail.scaleIntro || ''
      viewState.status = detail.status ?? 1
      viewState.versionNo = detail.versionNo || 0
      viewState.scoreRange =
        detail.totalScoreMin !== undefined && detail.totalScoreMax !== undefined
          ? `${detail.totalScoreMin} - ${detail.totalScoreMax}`
          : '-'

      const contentObj = detail.contentJson ? JSON.parse(detail.contentJson) : { questions: [] }
      viewState.questions = (contentObj.questions || []) as QuestionItem[]

      const ruleObj = detail.ruleJson ? JSON.parse(detail.ruleJson) : { ranges: [] }
      const ranges = (ruleObj.ranges || []) as RuleRange[]
      viewState.ranges = ranges.map((range, index) => ({
        key: `${index}`,
        ...range,
        rangeText: `${range.min} - ${range.max}`,
        riskText: riskTextMap[Number(range.riskLevel)] || '-',
      }))

      viewModalVisible.value = true
    } else {
      message.error('获取详情失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('获取详情失败：' + (error.message || '网络错误'))
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
#assessmentScaleManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}

.question-card {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
}

.question-header {
  margin-bottom: 10px;
}

.question-title {
  font-weight: 500;
}

.option-row {
  margin-bottom: 8px;
}

.rule-row {
  margin-bottom: 10px;
}

.view-question {
  margin-bottom: 10px;
  padding: 10px;
  background: #fafafa;
  border-radius: 4px;
}

.view-question-title {
  font-weight: 500;
  margin-bottom: 6px;
}

.view-option {
  color: #666;
  line-height: 1.8;
}

.mb-12 {
  margin-bottom: 12px;
}
</style>

