<template>
  <a-modal
    v-model:open="visible"
    :title="scale?.scaleName || '量表测试'"
    :footer="null"
    width="800px"
    :maskClosable="false"
    @cancel="handleClose"
  >
    <div class="scale-test-container">
      <!-- 简介区域 -->
      <div v-if="!isTesting && !testResult" class="intro-section">
        <div class="scale-intro">{{ scale?.scaleIntro || '暂无简介' }}</div>
        <div class="start-btn-wrapper">
          <a-button type="primary" size="large" @click="startTest">开始测试</a-button>
        </div>
      </div>

      <!-- 测试区域 -->
      <div v-if="isTesting" class="test-section">
        <a-form layout="vertical" :model="answers" ref="formRef">
          <div
            v-for="(question, index) in questions"
            :key="question.questionId || index"
            class="question-item"
          >
            <a-form-item
              :name="question.questionId || String(index)"
              :rules="[{ required: question.required, message: '请回答此问题' }]"
            >
              <template #label>
                <span class="question-title">
                  {{ index + 1 }}. {{ question.title }}
                  <span v-if="question.required" class="required-mark">*</span>
                </span>
              </template>
              <a-radio-group v-model:value="answers[question.questionId || String(index)]">
                <a-space direction="vertical">
                  <a-radio
                    v-for="option in question.options"
                    :key="option.optionId"
                    :value="option.optionId"
                  >
                    {{ option.label }}
                  </a-radio>
                </a-space>
              </a-radio-group>
            </a-form-item>
          </div>
          
          <div class="action-btns">
            <a-button @click="handleClose">取消</a-button>
            <a-button type="primary" :loading="submitting" @click="submitTest">完成</a-button>
          </div>
        </a-form>
      </div>

      <!-- 结果区域 -->
      <div v-if="testResult" class="result-section">
        <a-result
          :status="getResultStatus(testResult.riskLevel)"
          :title="`测试完成，总分为：${testResult.totalScore} 分`"
          :sub-title="`风险等级：${getRiskLevelText(testResult.riskLevel)}`"
        >
          <template #extra>
            <div class="result-details">
              <div class="detail-item">
                <strong>评估结果：</strong>
                <span>{{ testResult.assessmentResult || '无' }}</span>
              </div>
              <div class="detail-item">
                <strong>建议：</strong>
                <span>{{ testResult.suggestion || '无' }}</span>
              </div>
            </div>
            <div class="result-actions">
              <a-button @click="handleClose">关闭</a-button>
              <a-button type="primary" @click="sendToAI">给AI看看</a-button>
            </div>
          </template>
        </a-result>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { addAssessmentRecord } from '@/api/assessmentRecordController'

const props = defineProps<{
  open: boolean
  scale: API.AssessmentScaleVO | null
  appId?: string
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'send-to-ai', message: string): void
}>()

const visible = ref(false)
const isTesting = ref(false)
const submitting = ref(false)
const testResult = ref<any>(null)

const questions = ref<any[]>([])
const rules = ref<any>(null)
const answers = ref<Record<string, any>>({})
const formRef = ref()

watch(() => props.open, (newVal) => {
  visible.value = newVal
  if (newVal && props.scale) {
    resetState()
    parseScaleData()
  }
})

watch(visible, (newVal) => {
  emit('update:open', newVal)
})

const resetState = () => {
  isTesting.value = false
  testResult.value = null
  answers.value = {}
}

const parseScaleData = () => {
  try {
    if (props.scale?.contentJson) {
      const content = JSON.parse(props.scale.contentJson)
      questions.value = content.questions || []
    }
    if (props.scale?.ruleJson) {
      rules.value = JSON.parse(props.scale.ruleJson)
    }
  } catch (e) {
    console.error('解析量表数据失败', e)
    message.error('量表数据格式错误')
  }
}

const startTest = () => {
  isTesting.value = true
}

const handleClose = () => {
  visible.value = false
}

const submitTest = async () => {
  try {
    await formRef.value.validate()
    
    // 计算总分和收集作答详情
    let totalScore = 0
    const answerDetails = []
    
    for (let i = 0; i < questions.value.length; i++) {
      const q = questions.value[i]
      const qId = q.questionId || String(i)
      const selectedOptionId = answers.value[qId]
      
      const selectedOption = q.options?.find((o: any) => o.optionId === selectedOptionId)
      if (selectedOption) {
        const score = Number(selectedOption.score || 0)
        totalScore += score
        
        answerDetails.push({
          questionId: qId,
          questionTitle: q.title,
          optionId: selectedOptionId,
          optionLabel: selectedOption.label,
          score: score
        })
      }
    }
    
    // 匹配规则
    let matchedRule = null
    if (rules.value?.ranges) {
      for (const range of rules.value.ranges) {
        if (totalScore >= range.min && totalScore <= range.max) {
          matchedRule = range
          break
        }
      }
    }
    
    const riskLevel = matchedRule ? matchedRule.riskLevel : 0
    const assessmentResult = matchedRule ? matchedRule.assessmentResult : '未知'
    const suggestion = matchedRule ? matchedRule.suggestion : '无'
    
    testResult.value = {
      totalScore,
      riskLevel,
      assessmentResult,
      suggestion,
      answerDetails
    }
    
    isTesting.value = false
    
    // 保存到后端
    saveRecord()
    
  } catch (e) {
    // 表单验证失败
  }
}

const saveRecord = async () => {
  if (!props.scale || !testResult.value) return
  
  submitting.value = true
  try {
    const req: API.AssessmentRecordAddRequest = {
      assessorType: 2, // 2-量表
      scaleId: props.scale.id,
      scaleNameSnapshot: props.scale.scaleName,
      scaleVersionNo: props.scale.versionNo,
      totalScore: testResult.value.totalScore,
      riskLevel: testResult.value.riskLevel,
      assessmentResult: testResult.value.assessmentResult,
      suggestion: testResult.value.suggestion,
      answerJson: JSON.stringify(testResult.value.answerDetails),
      ruleSnapshotJson: props.scale.ruleJson
    }
    
    const res = await addAssessmentRecord(req)
    if (res.data?.code !== 0) {
      message.error('保存评估记录失败：' + res.data?.message)
    }
  } catch (e) {
    console.error('保存记录失败', e)
  } finally {
    submitting.value = false
  }
}

const getResultStatus = (riskLevel: number) => {
  switch (riskLevel) {
    case 0: return 'success'
    case 1: return 'info'
    case 2: return 'warning'
    case 3: return 'error'
    default: return 'info'
  }
}

const getRiskLevelText = (level: number) => {
  switch (level) {
    case 0: return '无风险'
    case 1: return '低风险'
    case 2: return '中风险'
    case 3: return '高风险'
    default: return '未知'
  }
}

const sendToAI = () => {
  if (!props.scale || !testResult.value) return
  
  const msg = `我刚刚完成了【${props.scale.scaleName}】的自测。
我的总分是：${testResult.value.totalScore}分
风险等级：${getRiskLevelText(testResult.value.riskLevel)}
评估结果：${testResult.value.assessmentResult}
建议：${testResult.value.suggestion}

请根据我的测试结果，给我一些进一步的建议和指导。`

  emit('send-to-ai', msg)
  visible.value = false
}
</script>

<style scoped>
.scale-test-container {
  padding: 10px 0;
  max-height: 60vh;
  overflow-y: auto;
}

.intro-section {
  text-align: center;
  padding: 40px 20px;
}

.scale-intro {
  font-size: 16px;
  color: #666;
  margin-bottom: 40px;
  line-height: 1.6;
  white-space: pre-wrap;
  text-align: left;
}

.question-item {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.question-title {
  font-size: 16px;
  font-weight: 500;
}

.required-mark {
  color: #ff4d4f;
  margin-left: 4px;
}

.action-btns {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.result-details {
  text-align: left;
  background: #f5f5f5;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.detail-item {
  margin-bottom: 8px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
