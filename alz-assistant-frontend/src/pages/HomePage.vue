<template>
  <div id="homePage">
    <div class="page-container">
      <!-- 左侧侧边栏 -->
      <div class="sidebar-container" :class="{ 'sidebar-open': isSidebarOpen }">
        <div class="sidebar-content">
          <div class="sidebar-header">
            <span class="sidebar-title">我的对话</span>
            <a-button type="text" class="close-sidebar-btn" @click="toggleSidebar">
              <template #icon><MenuFoldOutlined /></template>
            </a-button>
          </div>
          
          <!-- 新建对话按钮 -->
          <div class="new-chat-wrapper">
            <a-button
              type="default"
              block
              class="scale-test-btn"
              @click="openScaleSelectModal"
              style="margin-bottom: 12px;"
            >
              <template #icon><FormOutlined /></template>
              量表自测
            </a-button>
            <a-button
              type="primary"
              block
              class="new-chat-btn"
              @click="handleCreateNewApp"
              :loading="creatingApp"
            >
              <template #icon><PlusOutlined /></template>
              开启新对话
            </a-button>
          </div>

          <!-- 对话列表 -->
          <div class="chat-list" ref="chatListRef" @scroll="handleChatListScroll">
            <div
              v-for="app in appList"
              :key="app.id"
              class="chat-item"
              :class="{ active: String(currentAppId) === String(app.id) }"
              @click="handleAppClick(String(app.id))"
            >
              <div class="chat-item-content">
                <span class="chat-title">{{ app.appName || '新对话' }}</span>
                <span class="chat-time">{{ formatTime(app.createTime) }}</span>
              </div>
              <a-button
                type="text"
                size="small"
                danger
                class="delete-btn"
                @click.stop="handleDeleteApp(String(app.id), app.appName)"
                :loading="deletingAppId === String(app.id)"
              >
                <template #icon><DeleteOutlined /></template>
              </a-button>
            </div>
            
            <a-empty v-if="appList.length === 0 && !loadingApps && !loadingMore" description="暂无对话" />
            <div v-if="loadingApps" class="loading-wrapper"><a-spin :spinning="true" /></div>
            <div v-if="loadingMore" class="loading-more-wrapper">
              <a-spin :spinning="true" size="small" />
              <span class="loading-text">加载中...</span>
            </div>
            <div v-if="!pagination.hasMore && appList.length > 0" class="no-more-wrapper">
              <span class="no-more-text">没有更多了</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧主要内容区域 -->
      <div class="main-content-area" ref="messagesContainer">
        <!-- 侧边栏开关按钮 (当侧边栏关闭时显示) -->
        <a-button
          v-if="!isSidebarOpen"
          type="text"
          class="sidebar-trigger"
          @click="toggleSidebar"
        >
          <template #icon><MenuUnfoldOutlined /></template>
        </a-button>

        <!-- 聊天容器 -->
          <div class="chat-container" :class="{ 'chat-centered': !currentAppId && messages.length === 0 }">
            <!-- 消息列表 (可滚动) -->
            <div class="messages-scroll-area">
              <div class="messages-content">
                <div v-if="loadingHistory" class="loading-history-wrapper">
                  <a-spin :spinning="true" size="large" />
                  <span class="loading-text">加载对话历史中...</span>
                </div>
                <!-- 欢迎/问候区域 -->
                <div v-else-if="messages.length === 0 && !isGenerating" class="welcome-section">
                  <!-- 心电图动画 -->
                  <div class="heartbeat-container">
                    <svg viewBox="0 0 2000 100" preserveAspectRatio="none" class="heartbeat-svg">
                      <path 
                        d="M 0,50 L 920,50 L 935,45 L 950,50 L 960,65 L 980,10 L 1000,90 L 1015,45 L 1030,50 L 1050,45 L 1065,50 L 2000,50" 
                        fill="none" 
                        stroke="var(--app-border-strong)" 
                        stroke-width="3" 
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        vector-effect="non-scaling-stroke"
                        pathLength="100"
                        class="heartbeat-path"
                      />
                    </svg>
                  </div>

                  <h2 class="greeting-title">{{ greetingText }}</h2>
                  
                  <!-- 量表推荐区域 -->
                  <div class="recommended-scales" v-if="recommendedScales.length > 0">
                    <div 
                      v-for="scale in recommendedScales" 
                      :key="scale.id" 
                      class="scale-card"
                      @click="startScaleTest(scale)"
                    >
                      <div class="scale-card-title">{{ scale.scaleName }}</div>
                    </div>
                  </div>

                  <!-- 热门问题区域 -->
                  <div class="hot-questions">
                    <div 
                      v-for="(question, index) in hotQuestions" 
                      :key="index" 
                      class="hot-question-item"
                      @click="askHotQuestion(question)"
                    >
                      <BulbOutlined class="hot-question-icon" />
                      <span>{{ question }}</span>
                    </div>
                  </div>
                </div>

                <div v-for="(message, index) in messages" :key="`message-${index}-${message.createTime || index}`" class="message-item">
                <div v-if="message.type === 'user'" class="user-message">
                  <div class="user-message-main">
                    <div
                      v-if="message.attachments && message.attachments.length > 0"
                      class="user-attachment-list"
                    >
                      <ChatAttachmentCard
                        v-for="(att, ai) in message.attachments"
                        :key="`${index}-att-${ai}`"
                        :file-name="att.name"
                        :size="att.size || 0"
                        :url="att.url"
                        :attachment-type="att.type"
                      />
                    </div>
                    <div class="message-content">{{ message.content }}</div>
                  </div>
                  <div class="message-avatar">
                    <a-avatar :size="40" :src="loginUserStore.loginUser.userAvatar">
                      {{ loginUserStore.loginUser.userName?.[0] || 'U' }}
                    </a-avatar>
                  </div>
                </div>
                <div v-else class="ai-message">
                  <div class="message-avatar">
                    <a-avatar :size="40" style="background-color: var(--app-accent)">AI</a-avatar>
                  </div>
                  <div class="ai-message-body">
                    <div class="message-content">
                      <MarkdownRenderer v-if="message.content" :content="message.content" />
                      <div
                        v-if="message.toolRequests && message.toolRequests.length > 0"
                        class="tool-stream-wrapper"
                      >
                        <div class="tool-stream-title">工具调用</div>
                        <div
                          v-for="(item, i) in message.toolRequests"
                          :key="`tool-request-${index}-${i}`"
                          class="tool-stream-item"
                        >
                          <span class="tool-stream-tag pending">已选择</span>
                          <span>{{ item }}</span>
                        </div>
                      </div>
                      <div
                        v-if="message.toolExecuted && message.toolExecuted.length > 0"
                        class="tool-stream-wrapper"
                      >
                        <div class="tool-stream-title">工具执行结果</div>
                        <div
                          v-for="(item, i) in message.toolExecuted"
                          :key="`tool-executed-${index}-${i}`"
                          class="tool-stream-item"
                        >
                          <span class="tool-stream-tag done">已完成</span>
                          <span>{{ item }}</span>
                        </div>
                      </div>
                      <div v-if="message.loading" class="loading-indicator">
                        <a-spin size="small" />
                        <span>AI 正在思考...</span>
                      </div>
                    </div>
                    <!-- 猜你想问：放在气泡下方，参考豆包设计 -->
                    <div v-if="message.suggestions && message.suggestions.length > 0" class="suggestions-wrapper">
                      <div
                        v-for="(s, i) in message.suggestions"
                        :key="`suggestion-${index}-${i}`"
                        class="suggestion-item"
                        @click="onSuggestionClick(s)"
                      >
                        <span class="suggestion-text">{{ s }}</span>
                        <RightOutlined class="suggestion-icon" />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 (固定底部) -->
          <div class="input-area-fixed">
            <div class="input-wrapper" :class="{ 'is-dragover': inputDragOver }">
              <input
                ref="chatFileInputRef"
                type="file"
                class="chat-file-input-hidden"
                multiple
                @change="onChatFileInputChange"
              />
              <div v-if="pendingAttachments.length > 0" class="pending-attachments">
                <div class="pending-attachments-cards">
                  <ChatAttachmentCard
                    v-for="p in pendingAttachments"
                    :key="p.id"
                    :file-name="p.name"
                    :size="p.size"
                    :url="p.url"
                    :attachment-type="p.type"
                    :uploading="p.status === 'uploading'"
                    show-remove
                    @remove="removePendingAttachment(p.id)"
                  />
                </div>
              </div>
              <a-textarea
                v-model:value="userInput"
                placeholder="给 AI 发送消息（按 Enter 发送，Shift+Enter 换行）"
                :rows="3"
                :maxlength="1000"
                @keydown.enter="handleEnterKey"
                :disabled="isGenerating"
                class="chat-input"
              />
              <div class="input-actions">
                <a-tooltip title="添加附件（最多 6 个）">
                  <a-button
                    type="text"
                    class="input-plus-btn"
                    :disabled="isGenerating || pendingAttachments.length >= MAX_CHAT_ATTACHMENTS"
                    @click="openChatFilePicker"
                  >
                    <template #icon>
                      <PlusOutlined />
                    </template>
                  </a-button>
                </a-tooltip>
                <a-tooltip title="专业知识询问：先选中，输入问题后点发送（结合知识库）；再次点击取消">
                  <a-button
                    type="text"
                    class="knowledge-btn"
                    :class="{ 'knowledge-btn--selected': inputExtraMode === 'knowledge' }"
                    :disabled="isGenerating"
                    @click="toggleKnowledgeMode"
                  >
                    <template #icon>
                      <ReadOutlined />
                    </template>
                  </a-button>
                </a-tooltip>
                <a-tooltip title="生成诊断报告：先选中并描述症状，再点发送（将尝试获取位置以推荐附近医院）；再次点击取消">
                  <a-button
                    type="text"
                    class="agent-btn"
                    :class="{ 'agent-btn--selected': inputExtraMode === 'agent' }"
                    :disabled="isGenerating"
                    @click="toggleAgentMode"
                  >
                    <template #icon>
                      <FileTextOutlined />
                    </template>
                  </a-button>
                </a-tooltip>
                <a-button
                  v-if="isGenerating"
                  danger
                  @click="stopGenerating"
                  class="send-btn"
                >
                  <template #icon>
                    <PauseCircleOutlined />
                  </template>
                </a-button>
                <a-button
                  v-else
                  type="primary"
                  @click="sendMessage"
                  :loading="isGenerating"
                  :disabled="sendButtonDisabled"
                  class="send-btn"
                >
                  <template #icon>
                    <SendOutlined />
                  </template>
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 量表选择弹窗 -->
    <a-modal
      v-model:open="scaleSelectModalVisible"
      title="选择量表进行自测"
      :footer="null"
      width="600px"
    >
      <a-spin :spinning="loadingScales">
        <div class="scale-select-list">
          <div 
            v-for="scale in allScales" 
            :key="scale.id" 
            class="scale-select-item"
            @click="startScaleTest(scale)"
          >
            <div class="scale-select-title">{{ scale.scaleName }}</div>
            <div class="scale-select-intro">{{ scale.scaleIntro || '暂无简介' }}</div>
          </div>
          <a-empty v-if="allScales.length === 0 && !loadingScales" description="暂无可用量表" />
        </div>
      </a-spin>
    </a-modal>

    <!-- 量表测试弹窗 -->
    <ScaleTestModal
      v-model:open="scaleTestModalVisible"
      :scale="currentTestScale"
      :app-id="currentAppId"
      @send-to-ai="handleSendScaleResultToAI"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated, onDeactivated, nextTick, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, deleteApp, getAppSuggestions, stopChat } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { uploadChatAttachment } from '@/api/userController'
import { API_BASE_URL } from '@/config/env'
import {
  PlusOutlined,
  SendOutlined,
  BulbOutlined,
  GlobalOutlined,
  MenuOutlined,
  DeleteOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  FileTextOutlined,
  ReadOutlined,
  RightOutlined,
  FormOutlined,
  PauseCircleOutlined,
} from '@ant-design/icons-vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import ScaleTestModal from '@/components/ScaleTestModal.vue'
import ChatAttachmentCard from '@/components/ChatAttachmentCard.vue'
import { listAssessmentScaleVOByPage } from '@/api/assessmentScaleController'

defineOptions({ name: 'HomePage' })

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 问候语
const greetingText = computed(() => {
  const hour = new Date().getHours()
  let timePeriod = ''
  if (hour >= 0 && hour < 5) timePeriod = '凌晨'
  else if (hour >= 5 && hour < 11) timePeriod = '上午'
  else if (hour >= 11 && hour < 13) timePeriod = '中午'
  else if (hour >= 13 && hour < 18) timePeriod = '下午'
  else timePeriod = '晚上'

  // 未登录时不展示“未登录”等账户占位名，统一使用“你好”
  const isLoggedIn = !!loginUserStore.loginUser.id
  const name = isLoggedIn
    ? (loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount || 'Hello')
    : 'Hello'

  return `${name}，${timePeriod}好，欢迎来到Alz咨询小站`
})

// 量表和热门问题
const recommendedScales = ref<API.AssessmentScaleVO[]>([])
const allScales = ref<API.AssessmentScaleVO[]>([])
const loadingScales = ref(false)
const scaleSelectModalVisible = ref(false)
const scaleTestModalVisible = ref(false)
const currentTestScale = ref<API.AssessmentScaleVO | null>(null)

const hotQuestions = [
  '阿尔茨海默病有哪些早期症状？',
  '如何预防阿尔茨海默病？',
  '阿尔茨海默病和正常衰老有什么区别？'
]

const loadScales = async () => {
  loadingScales.value = true
  try {
    const res = await listAssessmentScaleVOByPage({
      pageNum: 1,
      pageSize: 50,
      status: 1 // 仅查询启用状态
    })
    if (res.data?.code === 0 && res.data.data?.records) {
      allScales.value = res.data.data.records
      // 首页最多展示3个
      recommendedScales.value = allScales.value.slice(0, 3)
    }
  } catch (e) {
    console.error('获取量表失败', e)
  } finally {
    loadingScales.value = false
  }
}

const openScaleSelectModal = () => {
  scaleSelectModalVisible.value = true
  if (allScales.value.length === 0) {
    loadScales()
  }
}

const startScaleTest = (scale: API.AssessmentScaleVO) => {
  scaleSelectModalVisible.value = false
  currentTestScale.value = scale
  scaleTestModalVisible.value = true
}

const askHotQuestion = (question: string) => {
  userInput.value = question
  sendMessage()
}

const handleSendScaleResultToAI = (msg: string) => {
  userInput.value = msg
  sendMessage()
}

// 侧边栏状态
const isSidebarOpen = ref(false)
const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value
  // 如果打开侧边栏且未加载过应用列表，则加载
  if (isSidebarOpen.value && appList.value.length === 0 && loginUserStore.loginUser.id) {
    loadAppList(true)
  }
}

// 应用列表
const appList = ref<API.AppVO[]>([])
const loadingApps = ref(false)
const loadingMore = ref(false) // 加载更多状态
// 注意：后端 id 可能是 Long（雪花ID），在前端用 number 会有精度问题
// 这里统一用 string 存储/传递，避免 “最近创建的对话恢复后 appId 变形 -> 应用不存在”
const currentAppId = ref<string>()
// 分页信息
const pagination = ref({
  pageNum: 1,
  pageSize: 20, // 每次最多20个
  total: 0,
  hasMore: true, // 是否还有更多数据
})
// 临时聊天室ID（用于未创建应用时的聊天）
const tempChatId = ref<string>('temp-' + Date.now())
// 对话列表容器引用
const chatListRef = ref<HTMLElement>()
// 是否已经进行过首次状态恢复
const hasRestoredState = ref(false)

// 消息列表
/** 与后端 ChatAttachmentType.code 一致：image | document | spreadsheet | text */
interface ChatAttachmentMsg {
  url: string
  name: string
  size?: number
  type?: string
}

interface Message {
  type: 'user' | 'ai'
  content: string
  attachments?: ChatAttachmentMsg[]
  loading?: boolean
  createTime?: string
  suggestions?: string[]
  toolRequests?: string[]
  toolExecuted?: string[]
  seenToolRequestIds?: string[]
}

const MAX_CHAT_ATTACHMENTS = 6

/** 未选中对话（新对话）时消息桶的 key */
const NEW_CHAT_KEY = '__new__'

/** 按对话 ID 分桶缓存消息，切换侧边栏对话不销毁、流式仍写入对应桶（与 ChatGPT 等多会话行为一致） */
const messagesByAppId = ref<Record<string, Message[]>>({})
/** 本会话已从本机加载/产生过该对话内容，再切入时不再请求历史覆盖，避免打断流式或闪白 */
const sessionChatHydrated = ref<Record<string, boolean>>({})

const chatKeyForAppId = (appId: string | undefined) => appId ?? NEW_CHAT_KEY

const messages = computed({
  get(): Message[] {
    const key = chatKeyForAppId(currentAppId.value)
    if (!messagesByAppId.value[key]) {
      messagesByAppId.value[key] = []
    }
    return messagesByAppId.value[key]
  },
  set(next: Message[]) {
    const key = chatKeyForAppId(currentAppId.value)
    messagesByAppId.value[key] = next
  },
})

/** 当前正在流式生成所绑定的 appId（可与 currentAppId 不同：用户已切到别的对话） */
const activeStreamAppId = ref<string | null>(null)

interface PendingChatAttachment {
  id: string
  name: string
  size: number
  status: 'uploading' | 'done' | 'error'
  url?: string
  /** 上传成功后由服务端返回；上传中可按文件名推断展示 */
  type?: string
}

const pendingAttachments = ref<PendingChatAttachment[]>([])
const chatFileInputRef = ref<HTMLInputElement>()
const inputDragOver = ref(false)

const userInput = ref('')
const isGenerating = ref(false)

/** 输入区附加模式：普通 / 专业知识(RAG) / 诊断报告智能体，三者互斥，默认普通 */
type InputExtraMode = 'none' | 'knowledge' | 'agent'
const inputExtraMode = ref<InputExtraMode>('none')

const toggleKnowledgeMode = () => {
  if (isGenerating.value) return
  inputExtraMode.value = inputExtraMode.value === 'knowledge' ? 'none' : 'knowledge'
}

const toggleAgentMode = () => {
  if (isGenerating.value) return
  inputExtraMode.value = inputExtraMode.value === 'agent' ? 'none' : 'agent'
}

const sendButtonDisabled = computed(() => {
  if (isGenerating.value) return true
  const text = userInput.value.trim()
  const uploading = pendingAttachments.value.some((p) => p.status === 'uploading')
  const hasErr = pendingAttachments.value.some((p) => p.status === 'error')
  const doneList = pendingAttachments.value.filter((p) => p.status === 'done' && p.url)
  const canSend = (text.length > 0 || doneList.length > 0) && !uploading && !hasErr
  return !canSend
})
const messagesContainer = ref<HTMLElement>()
const currentStreamAbort = ref<AbortController | null>(null)
const currentGeneratingAiIndex = ref<number | null>(null)

type StreamMessageType = 'ai_response' | 'tool_request' | 'tool_executed'

interface AgentInnerMessage {
  type: StreamMessageType
  data?: string
  id?: string
  name?: string
  arguments?: string
  result?: string
}

const TOOL_NAME_TO_CN: Record<string, string> = {
  searchNearbyHospitals: '地图搜索',
  getUserLocation: '获取用户位置',
  geocode: '地理编码',
  generateMedicalReport: 'PDF 报告生成',
  googleSearch: '网络搜索',
  doTerminate: '结束任务',
}

const getToolNameCn = (name?: string) => {
  if (!name) return '未知工具'
  return TOOL_NAME_TO_CN[name] || name
}

const getToolResultSummary = (result?: string) => {
  if (!result) return '已完成'
  const normalized = result.replace(/\n/g, ' ').trim()
  if (normalized.length <= 80) return normalized
  return `${normalized.slice(0, 77)}...`
}

const parseAgentInnerMessage = (chunk: string): AgentInnerMessage | null => {
  try {
    const inner = JSON.parse(chunk)
    if (!inner || typeof inner !== 'object' || !inner.type) {
      return null
    }
    return inner as AgentInnerMessage
  } catch {
    return null
  }
}

const appendAgentChunkToMessage = (msg: Message, inner: AgentInnerMessage): string => {
  if (inner.type === 'ai_response') {
    return inner.data ?? ''
  }
  if (inner.type === 'tool_request') {
    const id = inner.id ?? ''
    if (!msg.seenToolRequestIds) {
      msg.seenToolRequestIds = []
    }
    if (id && msg.seenToolRequestIds.includes(id)) {
      return ''
    }
    if (id) {
      msg.seenToolRequestIds.push(id)
    }
    if (!msg.toolRequests) {
      msg.toolRequests = []
    }
    msg.toolRequests.push(getToolNameCn(inner.name))
    return ''
  }
  if (inner.type === 'tool_executed') {
    if (!msg.toolExecuted) {
      msg.toolExecuted = []
    }
    msg.toolExecuted.push(`${getToolNameCn(inner.name)}：${getToolResultSummary(inner.result)}`)
    return ''
  }
  return ''
}

const onSuggestionClick = (text: string) => {
  if (!text?.trim()) {
    return
  }
  userInput.value = text.trim()
  sendMessage()
}

const fetchSuggestions = async (
  userQuestion: string,
  aiResponse: string,
  aiMessageIndex: number,
  streamAppId: string
) => {
  if (!streamAppId) {
    return
  }
  const list = messagesByAppId.value[streamAppId]
  if (!list?.[aiMessageIndex] || list[aiMessageIndex].type !== 'ai') {
    return
  }
  try {
    const res = await getAppSuggestions({
      // 后端接收 Long，前端用 string 存储，这里用 any 传递即可被解析
      appId: streamAppId as any,
      userQuestion,
      aiResponse,
    })
    if (res.data.code === 0 && Array.isArray(res.data.data)) {
      const again = messagesByAppId.value[streamAppId]
      if (again?.[aiMessageIndex] && again[aiMessageIndex].type === 'ai') {
        again[aiMessageIndex].suggestions = res.data.data
        // 弹出猜你想问后自动滚动到底部，确保所有建议问题可见
        nextTick(() => scrollToBottom(true))
      }
    }
  } catch (e) {
    console.warn('获取猜你想问失败：', e)
  }
}

// 创建应用
const creatingApp = ref(false)

// 加载历史对话状态
const loadingHistory = ref(false)

// 删除应用状态
const deletingAppId = ref<string | undefined>()

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return '今天'
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

// 加载应用列表（首次加载或重置）
/** silent：后台刷新，不先清空列表、不显示全屏 loading，避免侧边栏对话列表闪烁 */
const loadAppList = async (reset = false, opts?: { silent?: boolean }) => {
  const silent = opts?.silent === true
  if (!loginUserStore.loginUser.id) {
    appList.value = []
    return
  }

  // 如果是重置，清空列表并重置分页（静默刷新保留旧列表直到新数据返回）
  if (reset) {
    if (!silent) {
      appList.value = []
    }
    pagination.value = {
      pageNum: 1,
      pageSize: 20,
      total: 0,
      hasMore: true,
    }
  }

  // 如果没有更多数据，不再请求
  if (!pagination.value.hasMore && !reset) {
    return
  }

  if (!silent) {
    loadingApps.value = true
  }
  try {
    const res = await listMyAppVoByPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      const records = res.data.data.records || []
      const total = res.data.data.totalRow || 0

      if (reset) {
        appList.value = records
      } else {
        appList.value.push(...records)
      }

      // 更新分页信息
      pagination.value.total = total
      pagination.value.hasMore =
        appList.value.length < total && records.length === pagination.value.pageSize

      // 如果是首次加载（重置）且还没有恢复过状态，则尝试恢复上次的状态（静默刷新不参与）
      if (reset && !silent && appList.value.length > 0 && !hasRestoredState.value) {
        hasRestoredState.value = true // 标记已经恢复过状态
        const lastAppId = localStorage.getItem('lastActiveChatId')
        
        // 1. 如果上次是"新对话"状态，则清空当前选中，保持新对话界面
        if (lastAppId === 'new-chat') {
          currentAppId.value = undefined
          messages.value = []
        }
        // 2. 如果上次有选中的对话ID，直接恢复（用 string，避免 Long 精度丢失）
        else if (lastAppId && /^\d+$/.test(lastAppId)) {
          selectApp(lastAppId)
        }
        // 3. 否则默认选择第一个
        else {
          const firstAppId = appList.value[0]?.id
          if (firstAppId) {
            selectApp(String(firstAppId))
          }
        }
      }
    }
  } catch (error) {
    console.error('加载应用列表失败：', error)
    message.error('加载应用列表失败')
  } finally {
    if (!silent) {
      loadingApps.value = false
    }
  }
}

// 加载更多应用
const loadMoreApps = async () => {
  if (!loginUserStore.loginUser.id || loadingMore.value || !pagination.value.hasMore) {
    return
  }

  loadingMore.value = true
  try {
    pagination.value.pageNum += 1

    const res = await listMyAppVoByPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      const records = res.data.data.records || []
      const total = res.data.data.totalRow || 0

      appList.value.push(...records)

      // 更新分页信息
      pagination.value.total = total
      pagination.value.hasMore =
        appList.value.length < total && records.length === pagination.value.pageSize
    }
  } catch (error) {
    console.error('加载更多应用失败：', error)
    message.error('加载更多失败')
    // 加载失败时回退页码
    pagination.value.pageNum -= 1
  } finally {
    loadingMore.value = false
  }
}

// 处理对话列表滚动事件
const handleChatListScroll = (event: Event) => {
  const target = event.target as HTMLElement
  if (!target) return

  // 计算是否滚动到底部（距离底部50px内）
  const scrollTop = target.scrollTop
  const scrollHeight = target.scrollHeight
  const clientHeight = target.clientHeight

  // 滚动到底部时加载更多
  if (scrollHeight - scrollTop - clientHeight < 50) {
    if (pagination.value.hasMore && !loadingMore.value && !loadingApps.value) {
      loadMoreApps()
    }
  }
}

// 创建新应用（进入新对话状态，不立即创建）
const handleCreateNewApp = async () => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  // 重置为新对话状态
  currentAppId.value = undefined
  messages.value = []
  // 强制持久化“新对话”状态（避免 currentAppId 已经是 undefined 时 watch 不触发）
  localStorage.setItem('lastActiveChatId', 'new-chat')
  
  // 移动端自动关闭侧边栏
  if (window.innerWidth < 768) {
    isSidebarOpen.value = false
  }
}

const parseHistoryAttachments = (
  raw: string | undefined,
  messageType: string | undefined
): ChatAttachmentMsg[] | undefined => {
  if (messageType !== 'user' || !raw?.trim()) {
    return undefined
  }
  try {
    const arr = JSON.parse(raw) as unknown
    if (!Array.isArray(arr)) {
      return undefined
    }
    const out: ChatAttachmentMsg[] = []
    for (const item of arr) {
      if (!item || typeof item !== 'object') continue
      const o = item as Record<string, unknown>
      const url = o.url != null ? String(o.url) : ''
      if (!url) continue
      const t = o.type != null ? String(o.type) : undefined
      out.push({
        url,
        name: o.name != null ? String(o.name) : '附件',
        size: typeof o.size === 'number' ? o.size : undefined,
        ...(t ? { type: t } : {}),
      })
    }
    return out.length > 0 ? out : undefined
  } catch {
    return undefined
  }
}

// 选择应用
const selectApp = async (appId: string | number | undefined) => {
  if (!appId) {
    message.warning('应用ID无效')
    return
  }
  const appIdStr = String(appId)

  // 如果已经选择了该应用，不重复加载
  if (currentAppId.value === appIdStr && messages.value.length > 0) {
    return
  }

  // 设置当前应用ID
  currentAppId.value = appIdStr
  // 立即持久化，确保返回主页能恢复（不依赖 watch 时机）
  localStorage.setItem('lastActiveChatId', appIdStr)

  // 本会话已缓存过该对话（含流式进行中的内容），直接还原，禁止用接口结果覆盖
  if (sessionChatHydrated.value[appIdStr]) {
    await nextTick()
    scrollToBottom(true)
    return
  }

  loadingHistory.value = true
  messages.value = []

  // 首次进入该对话：从服务端加载历史
  try {
    const res = await listAppChatHistory({
      // API 类型里是 number，但后端是 Long，传 string 也能被解析
      appId: appIdStr as any,
      pageSize: 50, // 一次加载50条历史记录
    })

    if (res.data.code === 0 && res.data.data) {
      const chatHistories = res.data.data.records || []

      if (chatHistories.length === 0) {
        // 如果没有历史记录，显示欢迎信息
        message.info('该对话暂无历史记录')
      } else {
        // 将对话历史转换为消息格式，按时间正序排列（老消息在前）
        messages.value = chatHistories
          .map((chat) => ({
            type: (chat.messageType === 'user' ? 'user' : 'ai') as 'user' | 'ai',
            content: chat.message || '',
            attachments: parseHistoryAttachments(chat.attachments, chat.messageType),
            createTime: chat.createTime,
          }))
          .sort((a, b) => {
            // 按创建时间排序
            if (!a.createTime || !b.createTime) return 0
            return new Date(a.createTime).getTime() - new Date(b.createTime).getTime()
          })

        await nextTick()
        scrollToBottom(true) // 加载历史记录后瞬间滚动到底部
      }
      sessionChatHydrated.value[appIdStr] = true
    } else {
      message.error(res.data.message || '加载对话历史失败')
    }
  } catch (error) {
    console.error('加载对话历史失败：', error)
    message.error('加载对话历史失败，请重试')
  } finally {
    loadingHistory.value = false
  }
}

// 处理回车键
const handleEnterKey = (e: KeyboardEvent) => {
  // 正在输入法输入中，不处理
  if (e.isComposing) {
    return
  }
  // Shift+Enter 换行，不发送
  if (e.shiftKey) {
    return
  }
  // 单独按 Enter 发送消息
  e.preventDefault()
  sendMessage()
}

const openChatFilePicker = () => {
  chatFileInputRef.value?.click()
}

const onChatFileInputChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  const files = input.files
  if (files?.length) {
    addChatFiles(Array.from(files))
  }
  input.value = ''
}

const addChatFiles = (files: File[]) => {
  const room = MAX_CHAT_ATTACHMENTS - pendingAttachments.value.length
  if (room <= 0) {
    message.warning(`最多 ${MAX_CHAT_ATTACHMENTS} 个附件`)
    return
  }
  const slice = files.slice(0, room)
  if (files.length > room) {
    message.warning(`最多 ${MAX_CHAT_ATTACHMENTS} 个附件，已添加前 ${room} 个`)
  }
  for (const file of slice) {
    const id = `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
    pendingAttachments.value.push({
      id,
      name: file.name,
      size: file.size,
      status: 'uploading',
    })
    uploadOnePending(id, file)
  }
}

const uploadOnePending = async (id: string, file: File) => {
  const fd = new FormData()
  fd.append('file', file)
  try {
    const res = await uploadChatAttachment(fd)
    if (res.data?.code === 0 && res.data.data?.url) {
      const url = res.data.data.url
      const serverName = res.data.data.fileName
      const serverSize = res.data.data.size
      const idx = pendingAttachments.value.findIndex((p) => p.id === id)
      if (idx >= 0) {
        const prev = pendingAttachments.value[idx]!
        pendingAttachments.value[idx] = {
          id: prev.id,
          name: serverName || prev.name,
          size: serverSize ?? prev.size,
          status: 'done',
          url,
          type: res.data.data.type,
        }
      }
    } else {
      throw new Error(res.data?.message || '上传失败')
    }
  } catch (err) {
    const idx = pendingAttachments.value.findIndex((p) => p.id === id)
    if (idx >= 0) {
      const prev = pendingAttachments.value[idx]!
      pendingAttachments.value[idx] = {
        id: prev.id,
        name: prev.name,
        size: prev.size,
        status: 'error',
        url: prev.url,
      }
    }
    message.error('附件上传失败')
    console.error(err)
  }
}

const removePendingAttachment = (id: string) => {
  pendingAttachments.value = pendingAttachments.value.filter((p) => p.id !== id)
}

let fileDragDepth = 0
const isFileDrag = (e: DragEvent) => e.dataTransfer?.types.includes('Files') ?? false
const onDocDragEnter = (e: DragEvent) => {
  if (!isFileDrag(e)) return
  e.preventDefault()
  fileDragDepth++
  if (fileDragDepth === 1) inputDragOver.value = true
}
const onDocDragLeave = (e: DragEvent) => {
  if (!isFileDrag(e)) return
  e.preventDefault()
  fileDragDepth--
  if (fileDragDepth <= 0) {
    fileDragDepth = 0
    inputDragOver.value = false
  }
}
const onDocDragOver = (e: DragEvent) => {
  if (!isFileDrag(e)) return
  e.preventDefault()
}
const onDocDrop = (e: DragEvent) => {
  if (!isFileDrag(e)) return
  e.preventDefault()
  fileDragDepth = 0
  inputDragOver.value = false
  const files = e.dataTransfer?.files
  if (files?.length) addChatFiles(Array.from(files))
}
const bindChatFileDropListeners = () => {
  document.addEventListener('dragenter', onDocDragEnter, true)
  document.addEventListener('dragleave', onDocDragLeave, true)
  document.addEventListener('dragover', onDocDragOver, true)
  document.addEventListener('drop', onDocDrop, true)
}
const unbindChatFileDropListeners = () => {
  document.removeEventListener('dragenter', onDocDragEnter, true)
  document.removeEventListener('dragleave', onDocDragLeave, true)
  document.removeEventListener('dragover', onDocDragOver, true)
  document.removeEventListener('drop', onDocDrop, true)
  fileDragDepth = 0
  inputDragOver.value = false
}

// 请求用户位置（用于报告智能体推荐附近医院），返回 "经度,纬度" 或 null
const getCurrentPositionOptional = (): Promise<string | null> => {
  if (!navigator.geolocation) {
    return Promise.resolve(null)
  }
  return new Promise((resolve) => {
    const timeout = setTimeout(() => {
      resolve(null)
    }, 12000)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        clearTimeout(timeout)
        const { longitude, latitude } = pos.coords
        resolve(`${longitude},${latitude}`)
      },
      () => {
        clearTimeout(timeout)
        resolve(null)
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    )
  })
}

// 发送消息：根据 inputExtraMode 决定是否走知识库、智能体（与主发送/回车共用）
const sendMessage = async () => {
  if (isGenerating.value || sendButtonDisabled.value) {
    return
  }

  /** 待发送成功后再清空，避免「创建应用失败」时丢失已选模式 */
  const mode = inputExtraMode.value

  let chatType: string | undefined
  let userLocation: string | undefined
  let useRag = false

  if (mode === 'agent') {
    chatType = 'agent'
    try {
      message.loading({ content: '正在获取位置以推荐附近医院…', key: 'location', duration: 0 })
      const loc = await getCurrentPositionOptional()
      userLocation = loc ?? undefined
    } finally {
      message.destroy('location')
    }
  } else if (mode === 'knowledge') {
    useRag = true
  }

  const messageContent = userInput.value.trim()
  const doneAttachments: ChatAttachmentMsg[] = pendingAttachments.value
    .filter((p) => p.status === 'done' && p.url)
    .map((p) => ({
      url: p.url!,
      name: p.name,
      size: p.size,
      ...(p.type ? { type: p.type } : {}),
    }))

  userInput.value = ''
  pendingAttachments.value = []

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: messageContent,
    attachments: doneAttachments.length > 0 ? doneAttachments : undefined,
  })

  // 添加AI消息占位符（先添加，确保索引正确）
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
    toolRequests: [],
    toolExecuted: [],
    seenToolRequestIds: [],
  })

  await nextTick()
  scrollToBottom(false) // 发送消息时可以使用平滑滚动

  // 如果没有选择应用，先创建新应用（使用第一条消息作为 initPrompt）
  if (!currentAppId.value) {
    const success = await handleCreateNewAppForChat(messageContent, doneAttachments, aiMessageIndex)
    // 如果创建失败，移除刚才添加的消息
    if (!success) {
      messages.value.pop() // 移除AI消息
      messages.value.pop() // 移除用户消息
      inputExtraMode.value = mode
      return
    }
  }

  // 开始生成（本次请求已提交，清除附加模式）
  inputExtraMode.value = 'none'
  isGenerating.value = true
  currentGeneratingAiIndex.value = aiMessageIndex
  await generateChat(messageContent, aiMessageIndex, chatType, userLocation, doneAttachments, useRag)
}

// 停止当前生成：本地先即时停流，再通知后端停止并落库
const stopGenerating = async () => {
  if (!isGenerating.value) {
    return
  }
  isGenerating.value = false
  currentStreamAbort.value?.abort()
  currentStreamAbort.value = null

  const aiIndex = currentGeneratingAiIndex.value
  const sid = activeStreamAppId.value
  if (aiIndex !== null && sid && messagesByAppId.value[sid]?.[aiIndex]) {
    messagesByAppId.value[sid][aiIndex].loading = false
    delete messagesByAppId.value[sid][aiIndex].seenToolRequestIds
  }
  currentGeneratingAiIndex.value = null
  activeStreamAppId.value = null

  const appId = sid ?? currentAppId.value
  if (!appId) {
    return
  }
  try {
    await stopChat({ appId: appId as any })
  } catch (error) {
    console.warn('停止生成请求失败：', error)
  }
  message.info('已停止生成')
}

// 为聊天创建新应用（使用第一条消息作为 initPrompt）
const handleCreateNewAppForChat = async (
  firstMessage: string,
  attachments: ChatAttachmentMsg[],
  aiMessageIndex: number
): Promise<boolean> => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return false
  }

  const initPrompt =
    firstMessage.trim() ||
    (attachments[0]?.name ? `附件：${attachments[0].name}` : '附件')

  creatingApp.value = true
  try {
    const res = await addApp({
      initPrompt,
    })

     if (res.data.code === 0 && res.data.data) {
       const newId = String(res.data.data)
       // 把「新对话」桶里的消息迁到真实 appId，避免切换 currentAppId 后消息丢在 __new__ 桶里
       const moved = messagesByAppId.value[NEW_CHAT_KEY] ?? []
       messagesByAppId.value[newId] = moved
       messagesByAppId.value[NEW_CHAT_KEY] = []
       sessionChatHydrated.value[newId] = true
       currentAppId.value = newId
       // 重新加载应用列表（静默刷新，避免列表闪动）
       loadAppList(true, { silent: true })
       return true
    } else {
      message.error('创建失败：' + res.data.message)
      return false
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
    return false
  } finally {
    creatingApp.value = false
  }
}

// 生成聊天 — POST + fetch 流式解析（支持附件）
const generateChat = async (
  userMessage: string,
  aiMessageIndex: number,
  chatType?: string,
  userLocation?: string,
  attachments?: ChatAttachmentMsg[],
  useRag?: boolean
) => {
  if (!currentAppId.value) {
    handleError(new Error('应用ID不存在'), aiMessageIndex)
    return
  }

  const streamAppId = String(currentAppId.value)
  const streamMessages = () => messagesByAppId.value[streamAppId]

  if (!streamMessages()?.[aiMessageIndex]) {
    console.error('消息索引无效:', aiMessageIndex, '消息数组长度:', streamMessages()?.length)
    handleError(new Error('消息索引无效'), aiMessageIndex, streamAppId)
    return
  }

  activeStreamAppId.value = streamAppId

  const ac = new AbortController()
  currentStreamAbort.value = ac
  currentGeneratingAiIndex.value = aiMessageIndex

  let streamCompleted = false
  let fullContent = ''
  const isAgentMode = chatType === 'agent'

  const suggestionUserText =
    userMessage.trim() ||
    (attachments && attachments.length
      ? `（附件：${attachments.map((a) => a.name).join('、')}）`
      : '')

  const applyChunk = (rawChunk: string) => {
    const list = streamMessages()
    if (!list?.[aiMessageIndex]) return
    const container = messagesContainer.value
    const threshold = 50
    const isAtBottom = container
      ? container.scrollHeight - container.scrollTop - container.clientHeight <= threshold
      : true

    const aiMessage = list[aiMessageIndex]
    if (isAgentMode) {
      const inner = parseAgentInnerMessage(rawChunk)
      if (inner) {
        const aiTextDelta = appendAgentChunkToMessage(aiMessage, inner)
        if (aiTextDelta) {
          fullContent += aiTextDelta
        }
      } else {
        fullContent += rawChunk
      }
    } else {
      fullContent += rawChunk
    }
    aiMessage.content = fullContent
    list[aiMessageIndex].loading = false
    if (isAtBottom) {
      scrollToBottom(true)
    }
  }

  const finishStream = () => {
    if (streamCompleted) return
    streamCompleted = true
    isGenerating.value = false
    if (currentStreamAbort.value === ac) {
      currentStreamAbort.value = null
    }
    if (currentGeneratingAiIndex.value === aiMessageIndex) {
      currentGeneratingAiIndex.value = null
    }
    if (activeStreamAppId.value === streamAppId) {
      activeStreamAppId.value = null
    }
    const list = streamMessages()
    if (list?.[aiMessageIndex]) {
      list[aiMessageIndex].loading = false
      delete list[aiMessageIndex].seenToolRequestIds
    }
    if (fullContent && list?.[aiMessageIndex]) {
      fetchSuggestions(suggestionUserText, fullContent, aiMessageIndex, streamAppId)
    }
    setTimeout(() => {
      loadAppList(true, { silent: true })
    }, 500)
  }

  try {
    const qs = new URLSearchParams({ appId: streamAppId })
    if (userMessage) qs.set('message', userMessage)
    if (chatType) qs.set('chatType', chatType)
    if (userLocation) qs.set('userLocation', userLocation)
    if (useRag) qs.set('useRag', 'true')
    const chatUrl = `${API_BASE_URL}/app/chat?${qs}`
    const hasAtt = attachments && attachments.length > 0
    const res = await fetch(chatUrl, {
      method: 'POST',
      credentials: 'include',
      signal: ac.signal,
      ...(hasAtt
        ? { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(attachments) }
        : {}),
    })

    if (!res.ok) {
      handleError(new Error(`HTTP ${res.status}`), aiMessageIndex, streamAppId)
      return
    }

    const reader = res.body?.getReader()
    if (!reader) {
      handleError(new Error('无法读取响应流'), aiMessageIndex, streamAppId)
      return
    }

    const decoder = new TextDecoder()
    let buffer = ''

    const processBlock = (block: string) => {
      let eventName: string | undefined
      let dataStr = ''
      for (const line of block.split('\n')) {
        if (line.startsWith('event:')) {
          eventName = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataStr += line.slice(5).replace(/^\s+/, '')
        }
      }
      if (eventName === 'done') {
        finishStream()
        return true
      }
      if (!dataStr) return false
      try {
        const parsed = JSON.parse(dataStr) as { d?: string }
        const content = parsed.d
        if (content !== undefined && content !== null) {
          applyChunk(content)
        }
      } catch (e) {
        console.error('解析消息失败:', e, '原始:', dataStr)
        handleError(e, aiMessageIndex, streamAppId)
        return true
      }
      return false
    }

    while (true) {
      const { done, value } = await reader.read()
      if (value) {
        buffer += decoder.decode(value, { stream: true })
        buffer = buffer.replace(/\r\n/g, '\n')
      }
      let sep: number
      while ((sep = buffer.indexOf('\n\n')) >= 0) {
        const rawBlock = buffer.slice(0, sep)
        buffer = buffer.slice(sep + 2)
        if (processBlock(rawBlock)) {
          return
        }
      }
      if (done) {
        break
      }
    }
    if (buffer.trim()) {
      processBlock(buffer)
    }
    if (!streamCompleted) {
      finishStream()
    }
  } catch (error: unknown) {
    const err = error as { name?: string }
    if (err?.name === 'AbortError') {
      isGenerating.value = false
      if (currentStreamAbort.value === ac) {
        currentStreamAbort.value = null
      }
      if (currentGeneratingAiIndex.value === aiMessageIndex) {
        currentGeneratingAiIndex.value = null
      }
      if (activeStreamAppId.value === streamAppId) {
        activeStreamAppId.value = null
      }
      const list = streamMessages()
      if (list?.[aiMessageIndex]) {
        list[aiMessageIndex].loading = false
        delete list[aiMessageIndex].seenToolRequestIds
      }
      return
    }
    console.error('流式对话失败：', error)
    Modal.error({
      title: 'AI服务异常',
      content: '当前AI模型可能配置有误或服务异常，请联系管理员检查大模型配置。',
      okText: '知道了',
    })
    handleError(error, aiMessageIndex, streamAppId)
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number, streamAppId?: string) => {
  console.error('生成失败：', error)

  const list =
    streamAppId != null ? messagesByAppId.value[streamAppId] : messages.value

  // 安全地更新错误消息
  if (list?.[aiMessageIndex]) {
    list[aiMessageIndex].content = 'AI回复失败: ' + JSON.stringify(error)
    list[aiMessageIndex].loading = false
    delete list[aiMessageIndex].seenToolRequestIds
  } else if (list) {
    list.push({
      type: 'ai',
      content: 'AI回复失败: ' + JSON.stringify(error),
      loading: false,
    })
  }

  isGenerating.value = false
  if (currentGeneratingAiIndex.value === aiMessageIndex) {
    currentGeneratingAiIndex.value = null
  }
  if (streamAppId != null && activeStreamAppId.value === streamAppId) {
    activeStreamAppId.value = null
  }
  currentStreamAbort.value?.abort()
  currentStreamAbort.value = null
}

// 滚动到底部
// instant: 是否瞬间滚动（无动画），默认为 false
const scrollToBottom = (instant = false) => {
  // 使用 nextTick 确保 DOM 更新后再滚动
  nextTick(() => {
    // 滚动 main-content-area
    if (messagesContainer.value) {
      if (instant) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      } else {
        messagesContainer.value.scrollTo({
          top: messagesContainer.value.scrollHeight,
          behavior: 'smooth'
        })
      }
    }
  })
}

// 处理应用点击事件
const handleAppClick = async (appId: string | number | undefined) => {
  if (!appId) return
  // 立即持久化当前选中的对话ID
  localStorage.setItem('lastActiveChatId', String(appId))
  
  // 选择应用并加载历史对话
  await selectApp(appId)
  
  // 桌面端保持展开，移动端可考虑关闭
  if (window.innerWidth < 768) {
    isSidebarOpen.value = false
  }
  
  // 滚动到顶部，确保能看到最新的对话
  await nextTick()
  scrollToBottom(true)
}

// 删除应用
const handleDeleteApp = async (appId: string | number | undefined, appName?: string) => {
  if (!appId) return

  // 确认删除
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除对话"${appName || '新对话'}"吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      await performDeleteApp(appId)
    },
  })
}

// 执行删除应用
const performDeleteApp = async (appId: string | number) => {

  deletingAppId.value = String(appId)
  try {
    const res = await deleteApp({
      id: appId as any,
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('删除成功')

      const idStr = String(appId)
      delete messagesByAppId.value[idStr]
      delete sessionChatHydrated.value[idStr]

      // 如果删除的是当前选中的应用，清空消息
      if (currentAppId.value === String(appId)) {
        currentAppId.value = undefined
        messages.value = []
      }

      // 重新加载应用列表（静默刷新）
      await loadAppList(true, { silent: true })
    } else {
      message.error(res.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除应用失败：', error)
    message.error('删除失败，请重试')
  } finally {
    deletingAppId.value = undefined
  }
}

// 处理抽屉打开事件 (不需要了，保留函数签名避免报错，或者直接删除引用)
// const handleDrawerOpen = () => { ... }
// 实际上上面已经删除了 @after-open="handleDrawerOpen" 绑定

// 监听当前会话ID变化，持久化状态到本地存储
watch(currentAppId, (newVal) => {
  if (newVal === undefined) {
    // 新对话状态
    localStorage.setItem('lastActiveChatId', 'new-chat')
  } else {
    // 具体的对话ID
    localStorage.setItem('lastActiveChatId', String(newVal))
  }
})

// 页面加载时获取数据
onMounted(() => {
  // 如果用户已登录，加载应用列表
  if (loginUserStore.loginUser.id) {
    loadAppList(true)
  }
  loadScales()
})

// 从管理页等路由返回时：组件被 keep-alive 缓存，流式输出未中断；此处把视图滚到最新内容
onActivated(() => {
  bindChatFileDropListeners()
  if (isGenerating.value) {
    nextTick(() => scrollToBottom(true))
  }
})

onDeactivated(() => {
  unbindChatFileDropListeners()
})
</script>

<style scoped>
#homePage {
  height: calc(100vh - 124px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: var(--app-text);
}

.page-container {
  display: flex;
  flex: 1;
  height: 100%;
  overflow: hidden;
  position: relative;
}

/* 侧边栏样式 */
.sidebar-container {
  width: 0;
  background-color: var(--app-surface-elevated);
  border-right: 1px solid var(--app-border);
  transition: width 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  height: 100%;
}

.sidebar-container.sidebar-open {
  width: 260px;
}

.sidebar-content {
  width: 260px; /* 保持内容宽度固定，避免挤压时换行 */
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.sidebar-title {
  font-family: var(--app-font-display);
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text);
}

.close-sidebar-btn {
  color: var(--app-text-muted);
}

.close-sidebar-btn:hover {
  color: var(--app-text);
}

.new-chat-wrapper {
  margin-bottom: 16px;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 右侧主内容样式 */
.main-content-area {
  flex: 1;
  display: block;
  position: relative;
  min-width: 0;
  background: var(--app-surface);
  overflow-y: auto;
  overflow-x: hidden;
  height: 100%;
}

.sidebar-trigger {
  position: sticky;
  top: 16px;
  margin-left: 16px;
  z-index: 10;
  color: var(--app-text-muted);
  float: left; /* 确保不占据整行 */
}

.sidebar-trigger:hover {
  color: var(--app-accent);
}

/* 聊天容器 */
.chat-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  min-height: 100%;
  transition: all 0.3s ease;
}

/* 新对话居中状态 */
.chat-container.chat-centered {
  justify-content: center;
  align-items: center;
  gap: 6px;
}

.chat-container.chat-centered .messages-scroll-area {
  flex: 0 0 auto;
  width: 100%;
  padding: 8px 20px 0;
  display: flex;
  justify-content: center;
}

.chat-container.chat-centered .messages-content {
  padding-bottom: 6px;
}

.chat-container.chat-centered .input-area-fixed {
  position: static;
  width: 100%;
  max-width: 800px;
  background: transparent;
  border-top: none;
  padding: 2px 20px 8px;
}

.chat-container.chat-centered .welcome-section {
  min-height: auto;
  padding: 4px 12px 0;
  gap: 12px;
}

/* 消息列表区域 */
.messages-scroll-area {
  flex: 1;
  padding: 20px 20px 0 20px;
}

.messages-content {
  padding-bottom: 20px;
}

/* 输入区域固定 */
.input-area-fixed {
  flex-shrink: 0;
  padding: 6px 72px 8px 72px;
  background: var(--app-surface);
  border-top: 1px solid var(--app-border);
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.input-wrapper {
  position: relative;
  background: var(--app-surface);
  border: 1px solid var(--app-border-strong);
  border-radius: var(--app-radius);
  padding: 8px;
  box-shadow: var(--app-shadow-sm);
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}

.input-wrapper:focus-within {
  border-color: var(--app-accent);
  box-shadow: 0 0 0 3px var(--app-accent-soft);
}

.chat-input {
  border: none !important;
  resize: none;
  padding-right: 8px;
  padding-bottom: 36px;
  min-height: 0;
  box-shadow: none !important;
}

.chat-file-input-hidden {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}

.pending-attachments {
  margin-bottom: 4px;
}

.pending-attachments-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.input-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.input-plus-btn {
  color: var(--app-text-secondary);
}

.input-wrapper.is-dragover {
  border-color: var(--app-accent);
  background: var(--app-accent-soft);
}

.knowledge-btn {
  color: var(--app-text-secondary);
}

.knowledge-btn:hover:not(:disabled) {
  color: var(--app-accent);
}

.knowledge-btn--selected {
  color: var(--app-accent) !important;
  background: var(--app-accent-soft) !important;
}

.agent-btn {
  color: var(--app-text-secondary);
}

.agent-btn:hover:not(:disabled) {
  color: #52c41a;
}

.agent-btn--selected {
  color: #52c41a !important;
  background: rgba(82, 196, 26, 0.12) !important;
}

/* 消息样式复用 */
.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  padding: 20px 16px;
  text-align: center;
  position: relative;
  row-gap: 0;
}

/* 入场动画 */
@keyframes slideUpFade {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-section .greeting-title {
  position: relative;
  z-index: 1;
  animation: slideUpFade 0.35s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

.welcome-section .recommended-scales {
  position: relative;
  z-index: 1;
  opacity: 0;
  animation: slideUpFade 0.35s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
  animation-delay: 0.05s;
}

.welcome-section .hot-questions {
  position: relative;
  z-index: 1;
  opacity: 0;
  animation: slideUpFade 0.35s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
  animation-delay: 0.1s;
}

.chat-centered .input-area-fixed {
  opacity: 0;
  animation: slideUpFade 0.35s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
  animation-delay: 0.15s;
}

/* 心电图动画 */
.heartbeat-container {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100vw;
  height: 150px;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.heartbeat-svg {
  width: 100%;
  height: 100%;
}

.heartbeat-path {
  stroke-dasharray: 30 100;
  stroke-dashoffset: 30;
  animation: shootHeartbeat 0.5s linear forwards;
}

@keyframes shootHeartbeat {
  0% {
    stroke-dashoffset: 30;
    opacity: 0;
  }
  5% {
    opacity: 1;
  }
  95% {
    opacity: 1;
  }
  100% {
    stroke-dashoffset: -100;
    opacity: 0;
  }
}

.greeting-title {
  font-family: var(--app-font-display);
  font-size: clamp(1.2rem, 2.2vw, 1.45rem);
  color: var(--app-text);
  margin: 0;
  font-weight: 600;
  line-height: 1.35;
  letter-spacing: -0.02em;
  max-width: 36em;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 24px;
  color: var(--app-text);
  margin: 0;
}

/* 量表推荐区域 */
.recommended-scales {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 0;
  flex-wrap: wrap;
}

.scale-card {
  background: var(--app-surface-muted);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius-sm);
  padding: 10px 18px;
  cursor: pointer;
  transition:
    background 0.25s ease,
    border-color 0.25s ease,
    transform 0.25s ease;
  min-width: 120px;
  text-align: center;
}

.scale-card:hover {
  background: var(--app-accent-soft);
  border-color: color-mix(in srgb, var(--app-accent) 35%, transparent);
  color: var(--app-accent);
  transform: translateY(-2px);
}

.scale-card-title {
  font-size: 14px;
  font-weight: 500;
}

/* 热门问题区域 */
.hot-questions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 0;
  flex-wrap: wrap;
}

.hot-question-item {
  background: var(--app-surface);
  border: 1px solid var(--app-border-strong);
  border-radius: 999px;
  padding: 8px 16px;
  font-size: 13px;
  color: var(--app-text-secondary);
  cursor: pointer;
  transition:
    border-color 0.25s ease,
    color 0.25s ease,
    box-shadow 0.25s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.hot-question-item:hover {
  border-color: var(--app-accent);
  color: var(--app-accent);
  box-shadow: var(--app-shadow-sm);
}

.hot-question-icon {
  color: #faad14;
}

/* 量表选择列表 */
.scale-select-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px 0;
}

.scale-select-item {
  padding: 16px;
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius-sm);
  margin-bottom: 12px;
  cursor: pointer;
  transition:
    border-color 0.25s ease,
    box-shadow 0.25s ease;
}

.scale-select-item:hover {
  border-color: var(--app-accent);
  box-shadow: var(--app-shadow-md);
}

.scale-select-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--app-text);
  margin-bottom: 8px;
}

.scale-select-intro {
  font-size: 13px;
  color: var(--app-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-item {
  margin-bottom: 24px;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 12px;
}

.user-message-main {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  max-width: 100%;
}

.user-attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
  max-width: min(100%, 420px);
}

.ai-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 12px;
}

.message-content {
  max-width: 100%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
}

.user-message .message-content {
  background: linear-gradient(135deg, var(--app-accent) 0%, var(--app-accent-hover) 100%);
  color: #fff;
  margin-left: 52px;
}

.ai-message-body {
  flex: 0 1 auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
  max-width: 100%;
}

.ai-message .message-content {
  background: var(--app-surface-muted);
  color: var(--app-text);
  border: 1px solid var(--app-border);
  margin-right: 52px;
  width: fit-content;
  max-width: 100%;
}

.message-avatar {
  flex-shrink: 0;
}

.message-avatar :deep(.ant-avatar) {
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
}

.message-avatar :deep(.ant-avatar img) {
  object-fit: cover;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--app-text-muted);
  margin-top: 4px;
}

.tool-stream-wrapper {
  margin-top: 10px;
  padding: 10px;
  border-radius: var(--app-radius-sm);
  background: var(--app-surface-elevated);
  border: 1px solid var(--app-border);
}

.tool-stream-title {
  font-size: 12px;
  color: var(--app-text-muted);
  margin-bottom: 8px;
}

.tool-stream-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--app-text-secondary);
  margin-bottom: 6px;
}

.tool-stream-item:last-child {
  margin-bottom: 0;
}

.tool-stream-tag {
  display: inline-block;
  border-radius: 999px;
  padding: 1px 8px;
  font-size: 12px;
  line-height: 18px;
}

.tool-stream-tag.pending {
  color: var(--app-accent-muted);
  background: var(--app-accent-soft);
}

.tool-stream-tag.done {
  color: #237804;
  background: #f6ffed;
}

.loading-history-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: var(--app-text-muted);
  gap: 10px;
}

/* 猜你想问：在气泡下方独立展示 */
.ai-message-body .suggestions-wrapper {
  margin-right: 52px;
}
.suggestions-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-top: 0;
  padding-left: 0;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: var(--app-radius-sm);
  width: fit-content;
  max-width: 100%;
  box-sizing: border-box;
  background: var(--app-surface-elevated);
  border: 1px solid var(--app-border);
  color: var(--app-text-secondary);
  cursor: pointer;
  user-select: none;
  transition:
    border-color 0.2s ease,
    background 0.2s ease,
    color 0.2s ease;
}

.suggestion-item:hover {
  border-color: color-mix(in srgb, var(--app-accent) 40%, transparent);
  background: var(--app-accent-soft);
  color: var(--app-accent-hover);
}

.suggestion-text {
  font-size: 12px;
  line-height: 1.4;
}

.suggestion-icon {
  font-size: 12px;
  opacity: 0.7;
}

/* 对话列表样式 */
.chat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: var(--app-radius-sm);
  border: 1px solid transparent;
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease;
}

.chat-item:hover {
  background-color: var(--app-surface-muted);
}

.chat-item.active {
  background-color: var(--app-accent-soft);
  border: 1px solid color-mix(in srgb, var(--app-accent) 25%, transparent);
}

.chat-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-title {
  font-size: 14px;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-time {
  font-size: 12px;
  color: var(--app-text-muted);
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
}

.chat-item:hover .delete-btn {
  opacity: 1;
}

.loading-wrapper, .loading-more-wrapper, .no-more-wrapper {
  padding: 10px;
  text-align: center;
  color: #999;
  font-size: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .sidebar-container.sidebar-open {
    position: absolute;
    z-index: 100;
    height: 100%;
    box-shadow: 2px 0 8px rgba(0,0,0,0.15);
  }
  
  .message-content {
    max-width: 100%;
  }

  .input-area-fixed {
    padding: 12px 16px;
  }

  .chat-container.chat-centered .input-area-fixed {
    padding: 6px 16px 12px;
  }

  .chat-container.chat-centered .messages-scroll-area {
    padding: 6px 16px 0;
  }

  .user-message .message-content {
    margin-left: 0;
    max-width: 85%;
  }

  .ai-message .message-content {
    margin-right: 0;
    max-width: 85%;
  }

  .ai-message-body .suggestions-wrapper {
    margin-right: 0;
  }
}
</style>

