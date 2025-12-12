<template>
  <div id="knowledgeBaseManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="标题">
        <a-input v-model:value="searchParams.title" placeholder="输入标题" />
      </a-form-item>
      <a-form-item label="分类">
        <a-input v-model:value="searchParams.category" placeholder="输入分类" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select
          v-model:value="searchParams.status"
          placeholder="选择状态"
          style="width: 120px"
          :allow-clear="true"
          :get-popup-container="(triggerNode: HTMLElement) => triggerNode.parentElement"
        >
          <a-select-option :value="undefined">全部</a-select-option>
          <a-select-option value="active">已完成</a-select-option>
          <a-select-option value="parsing">解析中</a-select-option>
          <a-select-option value="failed">解析失败</a-select-option>
          <a-select-option value="inactive">未激活</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="文件类型">
        <a-select
          v-model:value="searchParams.fileType"
          placeholder="选择文件类型"
          style="width: 120px"
          :allow-clear="true"
          :get-popup-container="(triggerNode: HTMLElement) => triggerNode.parentElement"
        >
          <a-select-option :value="undefined">全部</a-select-option>
          <a-select-option value="doc">DOC</a-select-option>
          <a-select-option value="docx">DOCX</a-select-option>
          <a-select-option value="md">MD</a-select-option>
          <a-select-option value="txt">TXT</a-select-option>
          <a-select-option value="pdf">PDF</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
        <a-button style="margin-left: 8px" @click="resetSearch">重置</a-button>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="showUploadModal">上传文档</a-button>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" danger @click="handleRebuildRag" :loading="rebuildingRag">
          重新构建RAG
        </a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'content'">
          <div style="max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">
            {{ record.content || '-' }}
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'status'">
          <div v-if="record.status === 'active'">
            <a-tag color="green">已完成</a-tag>
          </div>
          <div v-else-if="record.status === 'parsing'">
            <a-spin size="small" />
            <span style="margin-left: 8px">解析中</span>
          </div>
          <div v-else-if="record.status === 'failed'">
            <a-tag color="red">解析失败</a-tag>
          </div>
          <div v-else>
            <a-tag color="orange">未激活</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'fileType'">
          <a-tag>{{ record.fileType || '-' }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'userId'">
          {{ record.userId || '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button
              type="primary"
              size="small"
              @click="showEditModal(record)"
              :disabled="record.status === 'parsing'"
            >
              编辑
            </a-button>
            <a-popconfirm
              title="确定要删除这个文档吗？"
              @confirm="doDelete(record.id)"
              :disabled="record.status === 'parsing'"
            >
              <a-button danger size="small" :disabled="record.status === 'parsing'">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 上传文档模态框 -->
    <a-modal
      v-model:open="uploadModalVisible"
      title="上传文档"
      @ok="handleUpload"
      @cancel="handleCancelUpload"
      :confirm-loading="uploadLoading"
      :width="600"
    >
      <div class="upload-modal-content">
        <a-form :model="uploadForm">
          <a-form-item>
            <div class="upload-wrapper">
              <a-upload-dragger
                v-model:file-list="fileList"
                :before-upload="beforeUpload"
                :multiple="true"
                accept=".doc,.docx,.md,.txt,.pdf"
                :show-upload-list="true"
                class="upload-dragger-large"
              >
                <p class="ant-upload-drag-icon">
                  <InboxOutlined />
                </p>
                <p class="ant-upload-text">点击或拖拽文件到此区域上传（支持多文件）</p>
                <p class="ant-upload-hint">
                  支持格式：doc、docx、md、txt、pdf
                </p>
              </a-upload-dragger>
            </div>
          </a-form-item>
          <a-form-item label="分类" name="category" class="category-form-item">
            <a-input v-model:value="uploadForm.category" placeholder="请输入分类（可选）" />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- 编辑文档模态框 -->
    <a-modal
      v-model:open="editModalVisible"
      :title="editModalTitle"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
      width="800px"
    >
      <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="标题" name="title">
          <a-input v-model:value="formState.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item label="内容" name="content">
          <a-textarea
            v-model:value="formState.content"
            placeholder="请输入内容"
            :rows="10"
          />
        </a-form-item>
        <a-form-item label="分类" name="category">
          <a-input v-model:value="formState.category" placeholder="请输入分类" />
        </a-form-item>
        <a-form-item label="来源" name="source">
          <a-input v-model:value="formState.source" placeholder="请输入来源" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select 
            v-model:value="formState.status" 
            placeholder="请选择状态"
            :get-popup-container="(triggerNode: HTMLElement) => triggerNode.parentElement"
          >
            <a-select-option value="active">激活</a-select-option>
            <a-select-option value="inactive">未激活</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import type { UploadFile } from 'ant-design-vue'
import { API_BASE_URL } from '@/config/env'
import axios from 'axios'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '标题',
    dataIndex: 'title',
    width: 200,
  },
  {
    title: '内容',
    dataIndex: 'content',
    width: 300,
  },
  {
    title: '分类',
    dataIndex: 'category',
    width: 120,
  },
  {
    title: '文件类型',
    dataIndex: 'fileType',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 120,
  },
  {
    title: '上传用户ID',
    dataIndex: 'userId',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
  },
]

// 展示的数据
const data = ref<API.KnowledgeBaseVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.KnowledgeBaseQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  title: undefined,
  category: undefined,
  status: undefined,
  fileType: undefined,
})

// 上传相关
const uploadModalVisible = ref(false)
const uploadLoading = ref(false)
const fileList = ref<UploadFile[]>([])
const uploadForm = reactive({
  category: '',
})

// 编辑相关
const editModalVisible = ref(false)
const editModalTitle = ref('编辑文档')
const isEdit = ref(false)
const submitLoading = ref(false)
const formState = reactive<API.KnowledgeBaseUpdateRequest & { id?: number }>({
  id: undefined,
  title: '',
  content: '',
  category: '',
  source: '',
  status: 'active',
  fileType: '',
  filePath: '',
})

// RAG重新构建
const rebuildingRag = ref(false)

// 格式化时间
const formatTime = (timeStr?: string) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 获取数据
const fetchData = async (silent = false) => {
  try {
    const res = await axios.post(`${API_BASE_URL}/knowledgeBase/list/page`, searchParams, {
      withCredentials: true,
    })
    if (res.data && res.data.code === 0) {
      if (res.data.data) {
        data.value = res.data.data.records ?? []
        total.value = res.data.data.totalRow ?? 0
      } else {
        data.value = []
        total.value = 0
      }
    } else {
      if (!silent) {
        message.error('获取数据失败，' + (res.data?.message || '未知错误'))
      }
    }
  } catch (error: any) {
    console.error('获取数据失败：', error)
    // 网络错误（如连接被拒绝）在轮询时静默处理
    if (error.code === 'ERR_NETWORK' || error.message?.includes('Network Error')) {
      if (!silent) {
        // 只在非轮询模式下显示错误
        console.warn('网络连接错误，可能是后端服务未启动')
      }
    } else if (error.response && error.response.data) {
      if (!silent) {
        message.error('获取数据失败，' + (error.response.data.message || '未知错误'))
      }
    } else {
      if (!silent) {
        message.error('获取数据失败，请检查网络连接')
      }
    }
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  searchParams.title = undefined
  searchParams.category = undefined
  searchParams.status = undefined
  searchParams.fileType = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 显示上传模态框
const showUploadModal = () => {
  uploadModalVisible.value = true
  fileList.value = []
  uploadForm.category = ''
}

// 上传前检查
const beforeUpload = (file: File) => {
  const fileName = file.name.toLowerCase()
  const validExtensions = ['.doc', '.docx', '.md', '.txt', '.pdf']
  const isValid = validExtensions.some(ext => fileName.endsWith(ext))
  if (!isValid) {
    message.error('只支持 doc、docx、md、txt、pdf 格式的文件')
    return false
  }
  return false // 阻止自动上传
}

// 处理上传
const handleUpload = async () => {
  if (fileList.value.length === 0) {
    message.error('请选择文件')
    return
  }

  // 验证所有文件
  const validFiles: File[] = []
  for (const fileItem of fileList.value) {
    const file = fileItem.originFileObj || (fileItem as any)
    if (file && file instanceof File) {
      validFiles.push(file)
    }
  }

  if (validFiles.length === 0) {
    message.error('文件对象不存在或无效')
    return
  }

  uploadLoading.value = true
  try {
    const formData = new FormData()
    
    if (validFiles.length === 1) {
      // 单文件上传
      const file = validFiles[0]
      if (!file) {
        message.error('文件对象不存在')
        return
      }
      formData.append('file', file)
      if (uploadForm.category) {
        formData.append('category', uploadForm.category)
      }
      const res = await axios.post(`${API_BASE_URL}/knowledgeBase/upload`, formData, {
        withCredentials: true,
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })

      if (res.data && res.data.code === 0) {
        message.success('上传成功，文档正在解析中...')
        uploadModalVisible.value = false
        fileList.value = []
        uploadForm.category = ''
        // 立即获取数据，显示解析中的状态
        await fetchData()
        startPollingStatus()
      } else {
        message.error('上传失败，' + (res.data?.message || '未知错误'))
      }
    } else {
      // 多文件上传
      validFiles.forEach((file) => {
        if (file) {
          formData.append('files', file)
        }
      })
      if (uploadForm.category) {
        formData.append('category', uploadForm.category)
      }

      const res = await axios.post(`${API_BASE_URL}/knowledgeBase/upload/multiple`, formData, {
        withCredentials: true,
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })

      if (res.data && res.data.code === 0) {
        const fileCount = validFiles.length
        message.success(`成功上传 ${fileCount} 个文件，文档正在解析中...`)
        uploadModalVisible.value = false
        fileList.value = []
        uploadForm.category = ''
        // 立即获取数据，显示解析中的状态
        await fetchData()
        startPollingStatus()
      } else {
        message.error('上传失败，' + (res.data?.message || '未知错误'))
      }
    }
  } catch (error: any) {
    console.error('上传失败：', error)
    if (error.response && error.response.data) {
      message.error('上传失败，' + (error.response.data.message || '未知错误'))
    } else {
      message.error('上传失败，请检查网络连接')
    }
  } finally {
    uploadLoading.value = false
  }
}

// 轮询检查解析状态
let pollingTimer: number | null = null
const startPollingStatus = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
  }
  let pollCount = 0
  const maxPollCount = 60 // 最多轮询60次（5分钟）
  pollingTimer = window.setInterval(async () => {
    pollCount++
    // 轮询时静默获取数据，不显示错误消息
    await fetchData(true)
    // 等待数据更新后再检查
    await new Promise(resolve => setTimeout(resolve, 100))
    const hasParsing = data.value && data.value.length > 0 && data.value.some((item) => item.status === 'parsing')
    if (!hasParsing || pollCount >= maxPollCount) {
      if (pollingTimer) {
        clearInterval(pollingTimer)
        pollingTimer = null
      }
      if (pollCount >= maxPollCount) {
        message.warning('轮询超时，请手动刷新查看状态')
      }
    }
  }, 2000) // 每2秒轮询一次，更快响应
}

// 取消上传
const handleCancelUpload = () => {
  uploadModalVisible.value = false
  fileList.value = []
  uploadForm.category = ''
}

// 显示编辑模态框
const showEditModal = async (record: API.KnowledgeBaseVO) => {
  isEdit.value = true
  editModalTitle.value = '编辑文档'
  try {
    const res = await axios.get(`${API_BASE_URL}/knowledgeBase/get`, {
      params: { id: record.id },
      withCredentials: true,
    })
    if (res.data.code === 0 && res.data.data) {
      const kb = res.data.data
      formState.id = kb.id
      formState.title = kb.title || ''
      formState.content = kb.content || ''
      formState.category = kb.category || ''
      formState.source = kb.source || ''
      formState.status = kb.status || 'active'
      formState.fileType = kb.fileType || ''
      formState.filePath = kb.filePath || ''
      editModalVisible.value = true
    } else {
      message.error('获取文档信息失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取文档信息失败：', error)
    message.error('获取文档信息失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formState.title || !formState.content) {
    message.error('请填写标题和内容')
    return
  }

  submitLoading.value = true
  try {
    const res = await axios.post(
      `${API_BASE_URL}/knowledgeBase/update`,
      formState,
      {
        withCredentials: true,
      }
    )
    if (res.data.code === 0) {
      message.success('更新成功')
      editModalVisible.value = false
      fetchData()
    } else {
      message.error('更新失败，' + res.data.message)
    }
  } catch (error) {
    console.error('操作失败：', error)
    message.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  editModalVisible.value = false
}

// 删除数据
const doDelete = async (id?: number) => {
  if (!id) {
    return
  }
  try {
    const res = await axios.post(
      `${API_BASE_URL}/knowledgeBase/delete`,
      { id },
      {
        withCredentials: true,
      }
    )
    if (res.data.code === 0) {
      message.success('删除成功')
      fetchData()
    } else {
      message.error('删除失败，' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}

// 重新构建RAG
const handleRebuildRag = async () => {
  // 检查是否有正在解析的文档
  const parsingDocs = data.value.filter((item) => item.status === 'parsing')
  if (parsingDocs.length > 0) {
    message.warning('存在正在解析的文档，请等待解析完成后再重新构建RAG')
    return
  }

  rebuildingRag.value = true
  try {
    const res = await axios.post(
      `${API_BASE_URL}/knowledgeBase/rebuild-rag`,
      {},
      {
        withCredentials: true,
      }
    )
    if (res.data.code === 0) {
      message.success('RAG向量库重新构建成功')
    } else {
      message.error('重新构建失败，' + res.data.message)
    }
  } catch (error) {
    console.error('重新构建失败：', error)
    message.error('重新构建失败')
  } finally {
    rebuildingRag.value = false
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
  // 检查是否有正在解析的文档，如果有则开始轮询
  setTimeout(() => {
    if (data.value && data.value.length > 0) {
      const hasParsing = data.value.some((item) => item.status === 'parsing')
      if (hasParsing) {
        startPollingStatus()
      }
    }
  }, 1000)
})
</script>

<style scoped>
#knowledgeBaseManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}

.upload-modal-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.upload-modal-content :deep(.ant-form) {
  width: 100%;
  max-width: 500px;
}

.upload-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  margin-bottom: 24px;
}

.upload-dragger-large {
  width: 100%;
}

.upload-dragger-large :deep(.ant-upload-drag) {
  min-height: 200px;
  padding: 40px 20px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s;
}

.upload-dragger-large :deep(.ant-upload-drag:hover) {
  border-color: #1890ff;
  background: #f0f8ff;
}

.upload-dragger-large :deep(.ant-upload-drag-icon) {
  font-size: 64px;
  color: #1890ff;
  margin-bottom: 20px;
}

.upload-dragger-large :deep(.ant-upload-text) {
  font-size: 16px;
  color: #333;
  margin-bottom: 12px;
  font-weight: 500;
}

.upload-dragger-large :deep(.ant-upload-hint) {
  font-size: 14px;
  color: #999;
}

.category-form-item {
  width: 100%;
  margin-bottom: 0;
}

.category-form-item :deep(.ant-form-item-label) {
  text-align: center;
  width: 80px;
  flex-shrink: 0;
}

.category-form-item :deep(.ant-form-item-control) {
  flex: 1;
}

.category-form-item :deep(.ant-input) {
  width: 100%;
}
</style>

