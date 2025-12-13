<template>
  <div id="aiModelManagePage">
    <!-- 搜索表单 -->
    <a-form :model="searchParams" layout="inline" @finish="doSearch">
      <a-form-item label="模型名称">
        <a-input
          v-model:value="searchParams.modelName"
          placeholder="请输入模型名称"
          allow-clear
          @pressEnter="doSearch"
        />
      </a-form-item>
      <a-form-item label="模型类型">
        <a-select
          v-model:value="searchParams.modelType"
          placeholder="请选择模型类型"
          style="width: 200px"
          allow-clear
          :get-popup-container="(triggerNode: HTMLElement) => triggerNode.parentElement"
        >
          <a-select-option value="openai">OpenAI</a-select-option>
          <a-select-option value="claude">Claude</a-select-option>
          <a-select-option value="gemini">Gemini</a-select-option>
          <a-select-option value="deepseek">DeepSeek</a-select-option>
          <a-select-option value="qwen">通义千问</a-select-option>
          <a-select-option value="yi">零一万物</a-select-option>
          <a-select-option value="baichuan">百川</a-select-option>
          <a-select-option value="moonshot">月之暗面</a-select-option>
          <a-select-option value="hunyuan">混元</a-select-option>
          <a-select-option value="minimax">MiniMax</a-select-option>
          <a-select-option value="custom">Custom</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态">
        <a-select
          v-model:value="searchParams.status"
          placeholder="请选择状态"
          style="width: 120px"
          allow-clear
          :get-popup-container="(triggerNode: HTMLElement) => triggerNode.parentElement"
        >
          <a-select-option value="active">启用</a-select-option>
          <a-select-option value="inactive">停用</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">查询</a-button>
        <a-button style="margin-left: 8px" @click="doReset">重置</a-button>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="openAddModal">新增大模型</a-button>
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
      :scroll="{ x: 1300 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 'active' ? 'green' : 'red'">
            {{ record.status === 'active' ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" size="small" @click="openEditModal(record)">编辑</a-button>
            <a-button
              v-if="record.status === 'inactive'"
              type="default"
              size="small"
              @click="handleEnable(record)"
            >
              启用
            </a-button>
            <a-button
              v-if="record.status === 'active'"
              type="default"
              size="small"
              danger
              @click="handleDisable(record)"
            >
              停用
            </a-button>
            <a-popconfirm title="确认删除？" @confirm="handleDelete(record)">
              <a-button type="default" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑大模型' : '新增大模型'"
      @ok="handleModalOk"
      :confirmLoading="submitLoading"
      width="600px"
    >
      <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="模型名称" required>
          <a-input v-model:value="formState.modelName" placeholder="请输入模型名称" />
        </a-form-item>
        <a-form-item label="模型Key" required>
          <a-input
            v-model:value="formState.modelKey"
            placeholder="请输入模型唯一标识 (如 gpt-4)"
          />
        </a-form-item>
        <a-form-item label="模型类型">
          <a-select v-model:value="formState.modelType" placeholder="请选择模型类型">
            <a-select-option value="openai">OpenAI</a-select-option>
            <a-select-option value="claude">Claude</a-select-option>
            <a-select-option value="gemini">Gemini</a-select-option>
            <a-select-option value="deepseek">DeepSeek</a-select-option>
            <a-select-option value="qwen">通义千问</a-select-option>
            <a-select-option value="yi">零一万物</a-select-option>
            <a-select-option value="baichuan">百川</a-select-option>
            <a-select-option value="moonshot">月之暗面</a-select-option>
            <a-select-option value="hunyuan">混元</a-select-option>
            <a-select-option value="minimax">MiniMax</a-select-option>
            <a-select-option value="custom">Custom</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="API Key">
          <a-input-password v-model:value="formState.apiKey" placeholder="请输入API Key" />
        </a-form-item>
        <a-form-item label="Base URL">
          <a-input v-model:value="formState.baseUrl" placeholder="请输入API Base URL" />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number v-model:value="formState.priority" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="最大Token">
          <a-input-number
            v-model:value="formState.maxTokens"
            :min="1"
            :step="100"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="温度 (Temperature)">
          <a-input-number
            v-model:value="formState.temperature"
            :min="0"
            :max="2"
            :step="0.1"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="Top P">
          <a-input-number
            v-model:value="formState.topP"
            :min="0"
            :max="1"
            :step="0.1"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="formState.description" :rows="3" placeholder="请输入描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  listAiModelByPage,
  addAiModel,
  updateAiModel,
  deleteAiModel,
  enableAiModel,
  disableAiModel,
} from '@/api/aiModelController'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 180,
  },
  {
    title: '模型名称',
    dataIndex: 'modelName',
    width: 150,
    ellipsis: true,
  },
  {
    title: '描述',
    dataIndex: 'description',
    ellipsis: true,
    width: 200,
  },
  {
    title: '模型Key',
    dataIndex: 'modelKey',
    width: 150,
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'modelType',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 220,
    fixed: 'right',
  },
]

// 数据列表
const data = ref<API.AiModelVO[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 搜索参数
const searchParams = reactive<API.AiModelQueryRequest>({
  modelName: '',
  modelType: undefined,
  status: undefined,
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await listAiModelByPage({
      ...searchParams,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    })
    // 适配后端返回结构: code=0 表示成功
    if (res.data && res.data.code === 0) {
      const pageData = res.data.data
      if (pageData) {
        data.value = pageData.records || []
        pagination.total = parseInt(String(pageData.totalRow || '0'))
      }
    } else {
      message.error('加载失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('加载失败：' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 查询
const doSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const doReset = () => {
  searchParams.modelName = ''
  searchParams.modelType = undefined
  searchParams.status = undefined
  pagination.current = 1
  loadData()
}

// 表格变更
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

// 模态框
const modalVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const editingModelStatus = ref<string>('') // 记录编辑时的模型状态

const formState = reactive<API.AiModelAddRequest & API.AiModelUpdateRequest>({
  modelName: '',
  modelKey: '',
  apiKey: '',
  baseUrl: '',
  modelType: undefined,
  priority: 0,
  maxTokens: 2000,
  temperature: 0.7,
  topP: 1.0,
  description: '',
})

// 打开新增模态框
const openAddModal = () => {
  isEdit.value = false
  editingModelStatus.value = ''
  Object.assign(formState, {
    id: undefined,
    modelName: '',
    modelKey: '',
    apiKey: '',
    baseUrl: '',
    modelType: undefined,
    priority: 0,
    maxTokens: 2000,
    temperature: 0.7,
    topP: 1.0,
    description: '',
  })
  modalVisible.value = true
}

// 打开编辑模态框
const openEditModal = (record: API.AiModelVO) => {
  isEdit.value = true
  editingModelStatus.value = record.status || '' // 记录当前状态
  Object.assign(formState, {
    id: record.id,
    modelName: record.modelName,
    modelKey: record.modelKey,
    // apiKey 不回显
    apiKey: '',
    baseUrl: record.baseUrl,
    modelType: record.modelType,
    priority: record.priority,
    maxTokens: record.maxTokens ?? 2000,
    temperature: record.temperature ?? 0.7,
    topP: record.topP ?? 1.0,
    description: record.description,
  })
  modalVisible.value = true
}

// 提交
const handleModalOk = async () => {
  if (!formState.modelName || !formState.modelKey) {
    message.warning('请填写必要信息')
    return
  }

  submitLoading.value = true
  try {
    let res
    if (isEdit.value) {
      res = await updateAiModel(formState)
    } else {
      res = await addAiModel(formState)
    }

    if (res.data && res.data.code === 0) {
      if (isEdit.value) {
        // 如果是编辑活跃模型，提示服务重启
        if (editingModelStatus.value === 'active') {
          message.success('更新成功！如果是活跃模型，AI服务已自动重启。', 3)
        } else {
          message.success('更新成功')
        }
      } else {
        message.success('创建成功')
      }
      modalVisible.value = false
      loadData()
    } else {
      message.error('操作失败：' + (res.data?.message || '未知错误'), 5)
    }
  } catch (error: any) {
    message.error('操作失败：' + (error.message || '网络错误'), 5)
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = async (record: API.AiModelVO) => {
  if (!record.id) return
  try {
    const res = await deleteAiModel({ id: record.id })
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

// 启用
const handleEnable = async (record: API.AiModelVO) => {
  if (!record.id) return
  // 弹出确认弹窗
  Modal.confirm({
    title: '确认启用该模型？',
    content: '启用前将先验证模型是否可用，验证成功后将自动停用其他所有已启用的模型并重启AI服务。此操作可能需要5-10秒。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      const hide = message.loading('正在验证模型并启用...', 0)
      try {
        const res = await enableAiModel({ id: record.id as number })
        hide()
        if (res.data && res.data.code === 0) {
          message.success('模型验证成功，已启用并重启AI服务！', 3)
          loadData()
        } else {
          message.error('启用失败：' + (res.data?.message || '未知错误'), 5)
        }
      } catch (error: any) {
        hide()
        message.error('启用失败：' + (error.message || '网络错误'), 5)
      }
    },
  })
}

// 停用
const handleDisable = async (record: API.AiModelVO) => {
  if (!record.id) return
  try {
    const res = await disableAiModel({ id: record.id })
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

// 格式化时间
const formatTime = (timeStr?: string) => {
  if (!timeStr) return '-'
  return dayjs(timeStr).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
#aiModelManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}
</style>