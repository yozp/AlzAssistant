<template>
  <div id="assessmentRecordPage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="量表名称">
        <a-input
          v-model:value="searchParams.scaleName"
          placeholder="输入量表名称"
          allow-clear
          @pressEnter="doSearch"
        />
      </a-form-item>
      <a-form-item label="风险等级">
        <a-select
          v-model:value="searchParams.riskLevel"
          placeholder="选择风险等级"
          style="width: 120px"
          allow-clear
          :get-popup-container="(triggerNode: HTMLElement) => triggerNode.parentElement"
        >
          <a-select-option :value="undefined">全部</a-select-option>
          <a-select-option :value="0">无风险</a-select-option>
          <a-select-option :value="1">低风险</a-select-option>
          <a-select-option :value="2">中风险</a-select-option>
          <a-select-option :value="3">高风险</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
        <a-button style="margin-left: 8px" @click="doReset">重置</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        row-key="id"
        :scroll="{ x: 1280 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'assessorType'">
            <a-tag v-if="record.assessorType === 1" color="blue">AI</a-tag>
            <a-tag v-else-if="record.assessorType === 2" color="green">量表</a-tag>
            <span v-else class="text-muted">—</span>
          </template>
          <template v-else-if="column.dataIndex === 'riskLevel'">
            <a-tag :color="getRiskLevelColor(record.riskLevel)">
              {{ getRiskLevelText(record.riskLevel) }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'symptomDesc'">
            <a-tooltip v-if="record.symptomDesc" :title="record.symptomDesc">
              <span class="suggestion-cell">{{ record.symptomDesc }}</span>
            </a-tooltip>
            <span v-else class="text-muted">—</span>
          </template>
          <template v-else-if="column.dataIndex === 'suggestion'">
            <a-tooltip v-if="record.suggestion" :title="record.suggestion">
              <span class="suggestion-cell">{{ record.suggestion }}</span>
            </a-tooltip>
            <span v-else class="text-muted">—</span>
          </template>
          <template v-else-if="column.dataIndex === 'scaleNameSnapshot'">
            <span v-if="record.assessorType === 1">AI 初步评估</span>
            <span v-else>{{ record.scaleNameSnapshot || '—' }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'assessmentResult' && record.reportUrl">
            <a :href="record.reportUrl" target="_blank" rel="noopener" class="report-link">PDF 报告</a>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            {{ formatTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="primary" size="small" @click="viewDetails(record)">查看详情</a-button>
          </template>
        </template>
    </a-table>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailsModalVisible"
      title="评估记录详情"
      :footer="null"
      width="800px"
    >
      <div v-if="currentRecord" class="record-details">
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="评估人">
            <a-tag :color="currentRecord.assessorType === 1 ? 'blue' : 'green'">
              {{ currentRecord.assessorType === 1 ? 'AI' : '量表' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item v-if="currentRecord.assessorType === 2" label="量表名称" :span="2">
            {{ currentRecord.scaleNameSnapshot }} (v{{ currentRecord.scaleVersionNo }})
          </a-descriptions-item>
          <a-descriptions-item v-else label="评估方式" :span="2">
            AI 初步评估（症状描述 + PDF 报告）
          </a-descriptions-item>
          <a-descriptions-item label="症状描述" :span="2">
            {{ currentRecord.symptomDesc || '无' }}
          </a-descriptions-item>
          <a-descriptions-item label="评估时间">
            {{ formatTime(currentRecord.createTime) }}
          </a-descriptions-item>
          <a-descriptions-item v-if="currentRecord.assessorType === 2" label="总分">
            <span class="score-text">{{ currentRecord.totalScore }}</span> 分
          </a-descriptions-item>
          <a-descriptions-item v-if="currentRecord.assessorType === 2" label="风险等级">
            <a-tag :color="getRiskLevelColor(currentRecord.riskLevel)">
              {{ getRiskLevelText(currentRecord.riskLevel) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="评估结果" :span="2">
            <a v-if="currentRecord.reportUrl" :href="currentRecord.reportUrl" target="_blank" rel="noopener">
              {{ currentRecord.assessmentResult || 'PDF 报告下载链接' }}
            </a>
            <span v-else>{{ currentRecord.assessmentResult || '无' }}</span>
          </a-descriptions-item>
          <a-descriptions-item v-if="currentRecord.assessorType === 2" label="建议" :span="2">
            {{ currentRecord.suggestion || '无' }}
          </a-descriptions-item>
        </a-descriptions>

        <div v-if="currentRecord.reportUrl" class="report-section">
          <h3 class="section-title">PDF 报告</h3>
          <a :href="currentRecord.reportUrl" target="_blank" rel="noopener" class="report-link">查看/下载 PDF 报告</a>
        </div>
        <div v-if="currentRecord.answerJson" class="answers-section">
          <h3 class="section-title">作答详情</h3>
          <div class="answers-list">
            <div v-for="(answer, index) in parseAnswers(currentRecord.answerJson)" :key="index" class="answer-item">
              <div class="question-title">{{ index + 1 }}. {{ answer.questionTitle }}</div>
              <div class="answer-content">
                <span class="label">您的回答：</span>
                <span class="value">{{ answer.optionLabel }}</span>
                <span class="score">({{ answer.score }}分)</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { listMyAssessmentRecordVOByPage } from '@/api/assessmentRecordController'

const searchParams = ref({
  scaleName: '',
  riskLevel: undefined as number | undefined,
})

const data = ref<API.AssessmentRecordVO[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0 as number,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const columns = [
  {
    title: '评估人',
    dataIndex: 'assessorType',
    key: 'assessorType',
    width: 88,
  },
  {
    title: '量表名称',
    dataIndex: 'scaleNameSnapshot',
    key: 'scaleNameSnapshot',
    width: 160,
    ellipsis: true,
  },
  {
    title: '症状描述',
    dataIndex: 'symptomDesc',
    key: 'symptomDesc',
    width: 180,
    ellipsis: true,
  },
  {
    title: '总分',
    dataIndex: 'totalScore',
    key: 'totalScore',
    width: 80,
  },
  {
    title: '风险等级',
    dataIndex: 'riskLevel',
    key: 'riskLevel',
    width: 100,
  },
  {
    title: '评估结果',
    dataIndex: 'assessmentResult',
    key: 'assessmentResult',
    width: 180,
    ellipsis: true,
  },
  {
    title: '建议',
    dataIndex: 'suggestion',
    key: 'suggestion',
    width: 200,
    ellipsis: true,
  },
  {
    title: '评估时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 172,
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right',
  },
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await listMyAssessmentRecordVOByPage({
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      riskLevel: searchParams.value.riskLevel,
      // Note: scaleName search is not supported by backend yet, we can add it if needed or just filter locally
    })
    if (res.data?.code === 0 && res.data.data) {
      // 本地过滤量表名称
      let records = res.data.data.records || []
      if (searchParams.value.scaleName) {
        records = records.filter(r => r.scaleNameSnapshot?.includes(searchParams.value.scaleName!))
      }
      data.value = records
      pagination.value.total = Number(res.data.data.total) || 0
    } else {
      message.error('获取评估记录失败：' + res.data?.message)
    }
  } catch (error: any) {
    message.error('获取评估记录失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const doSearch = () => {
  pagination.value.current = 1
  loadData()
}

const doReset = () => {
  searchParams.value = {
    scaleName: '',
    riskLevel: undefined,
  }
  doSearch()
}

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getRiskLevelText = (level?: number) => {
  switch (level) {
    case 0: return '无风险'
    case 1: return '低风险'
    case 2: return '中风险'
    case 3: return '高风险'
    default: return '未知'
  }
}

const getRiskLevelColor = (level?: number) => {
  switch (level) {
    case 0: return 'green'
    case 1: return 'blue'
    case 2: return 'orange'
    case 3: return 'red'
    default: return 'default'
  }
}

// 详情弹窗
const detailsModalVisible = ref(false)
const currentRecord = ref<API.AssessmentRecordVO | null>(null)

const viewDetails = (record: API.AssessmentRecordVO) => {
  currentRecord.value = record
  detailsModalVisible.value = true
}

const parseAnswers = (jsonStr?: string) => {
  if (!jsonStr) return []
  try {
    return JSON.parse(jsonStr)
  } catch (e) {
    return []
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
#assessmentRecordPage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}

.suggestion-cell {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-muted {
  color: #999;
}

.record-details {
  padding: 10px 0;
}

.score-text {
  font-size: 18px;
  font-weight: bold;
  color: #1890ff;
}

.report-section {
  margin-top: 24px;
}

.report-link {
  color: #1890ff;
  text-decoration: none;
}

.report-link:hover {
  text-decoration: underline;
}

.answers-section {
  margin-top: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  border-left: 4px solid #1890ff;
  padding-left: 8px;
}

.answers-list {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
}

.answer-item {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #e8e8e8;
}

.answer-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.question-title {
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.answer-content {
  color: #666;
  padding-left: 16px;
}

.answer-content .value {
  color: #1890ff;
  font-weight: 500;
  margin-right: 8px;
}

.answer-content .score {
  color: #999;
  font-size: 13px;
}
</style>
