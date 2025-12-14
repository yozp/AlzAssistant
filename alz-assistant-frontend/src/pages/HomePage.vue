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
                  <h2 class="greeting-title">{{ greetingText }}</h2>
                </div>

                <div v-for="(message, index) in messages" :key="`message-${index}-${message.createTime || index}`" class="message-item">
                <div v-if="message.type === 'user'" class="user-message">
                  <div class="message-content">{{ message.content }}</div>
                  <div class="message-avatar">
                    <a-avatar :size="40" :src="loginUserStore.loginUser.userAvatar">
                      {{ loginUserStore.loginUser.userName?.[0] || 'U' }}
                    </a-avatar>
                  </div>
                </div>
                <div v-else class="ai-message">
                  <div class="message-avatar">
                    <a-avatar :size="40" style="background-color: #1890ff">AI</a-avatar>
                  </div>
                  <div class="message-content">
                    <MarkdownRenderer v-if="message.content" :content="message.content" />
                    <div v-if="message.loading" class="loading-indicator">
                      <a-spin size="small" />
                      <span>AI 正在思考...</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 (固定底部) -->
          <div class="input-area-fixed">
            <div class="input-wrapper">
              <a-textarea
                v-model:value="userInput"
                placeholder="给 AI 发送消息（按 Enter 发送，Shift+Enter 换行）"
                :rows="4"
                :maxlength="1000"
                @keydown.enter="handleEnterKey"
                :disabled="isGenerating"
                class="chat-input"
              />
              <div class="input-actions">
                <a-button
                  type="primary"
                  @click="sendMessage"
                  :loading="isGenerating"
                  :disabled="!userInput.trim()"
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, deleteApp } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
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
} from '@ant-design/icons-vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

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
  
  const name = loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount || '你好'
  return `${name}，${timePeriod}好，今天也祝你身体健康，生活愉快`
})

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
interface Message {
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  createTime?: string
}

const messages = ref<Message[]>([])
const userInput = ref('')
const isGenerating = ref(false)
const messagesContainer = ref<HTMLElement>()

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
const loadAppList = async (reset = false) => {
  if (!loginUserStore.loginUser.id) {
    appList.value = []
    return
  }

  // 如果是重置，清空列表并重置分页
  if (reset) {
    appList.value = []
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

  loadingApps.value = true
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

      // 如果是首次加载（重置）且还没有恢复过状态，则尝试恢复上次的状态
      if (reset && appList.value.length > 0 && !hasRestoredState.value) {
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
    loadingApps.value = false
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
  // 清空当前消息列表
  messages.value = []
  loadingHistory.value = true

  // 加载该应用的对话历史
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

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isGenerating.value) {
    return
  }

  const messageContent = userInput.value.trim()
  userInput.value = ''

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: messageContent,
  })

  // 添加AI消息占位符（先添加，确保索引正确）
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  await nextTick()
  scrollToBottom(false) // 发送消息时可以使用平滑滚动

  // 如果没有选择应用，先创建新应用（使用第一条消息作为 initPrompt）
  if (!currentAppId.value) {
    const success = await handleCreateNewAppForChat(messageContent, aiMessageIndex)
    // 如果创建失败，移除刚才添加的消息
    if (!success) {
      messages.value.pop() // 移除AI消息
      messages.value.pop() // 移除用户消息
      return
    }
  }

  // 开始生成
  isGenerating.value = true
  await generateChat(messageContent, aiMessageIndex)
}

// 为聊天创建新应用（使用第一条消息作为 initPrompt）
const handleCreateNewAppForChat = async (firstMessage: string, aiMessageIndex: number): Promise<boolean> => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return false
  }

  creatingApp.value = true
  try {
    const res = await addApp({
      initPrompt: firstMessage, // 使用用户的第一条消息作为 initPrompt
    })

     if (res.data.code === 0 && res.data.data) {
       // 设置当前应用ID（不调用selectApp，避免清空消息）
       currentAppId.value = res.data.data
       // 重新加载应用列表（重置）
       loadAppList(true)
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

// 生成聊天 - 使用 EventSource 处理流式响应
const generateChat = async (userMessage: string, aiMessageIndex: number) => {
  // 确保应用ID存在
  if (!currentAppId.value) {
    handleError(new Error('应用ID不存在'), aiMessageIndex)
    return
  }

  // 确保消息数组和索引有效
  if (!messages.value[aiMessageIndex]) {
    console.error('消息索引无效:', aiMessageIndex, '消息数组长度:', messages.value.length)
    handleError(new Error('消息索引无效'), aiMessageIndex)
    return
  }

  let eventSource: EventSource | null = null
  let streamCompleted = false

  try {
    // 构建URL参数
    const params = new URLSearchParams({
      appId: String(currentAppId.value),
      message: userMessage,
    })

    const url = `${API_BASE_URL}/app/chat?${params}`

    // 创建 EventSource 连接
    eventSource = new EventSource(url, {
      withCredentials: true,
    })

    let fullContent = ''

    // 处理接收到的消息
    eventSource.onmessage = function (event) {
      if (streamCompleted) return

      try {
        // 检查消息对象是否还存在
        if (!messages.value[aiMessageIndex]) {
          console.warn('消息对象已被删除，停止更新')
          eventSource?.close()
          return
        }

        // 解析JSON包装的数据
        const parsed = JSON.parse(event.data)
        const content = parsed.d

        // 拼接内容
        if (content !== undefined && content !== null) {
          // 检查是否在底部（在内容更新前检查）
          const container = messagesContainer.value
          const threshold = 50 // 阈值
          const isAtBottom = container 
            ? container.scrollHeight - container.scrollTop - container.clientHeight <= threshold
            : true

          fullContent += content
          // 安全地更新消息内容
          if (messages.value[aiMessageIndex]) {
            messages.value[aiMessageIndex].content = fullContent
            messages.value[aiMessageIndex].loading = false
            
            // 只有当用户原本在底部时才自动滚动
            // 流式输出时使用 auto 滚动，避免 smooth 带来的卡顿
            if (isAtBottom) {
              scrollToBottom(true)
            }
          }
        }
      } catch (error) {
        console.error('解析消息失败:', error, '原始数据:', event.data)
        handleError(error, aiMessageIndex)
      }
    }

    // 处理done事件
    eventSource.addEventListener('done', function () {
      if (streamCompleted) return

      streamCompleted = true
      isGenerating.value = false
      eventSource?.close()
      
       // 确保消息已保存，重新加载应用列表以更新对话标题（重置）
       setTimeout(() => {
         loadAppList(true)
       }, 500)
    })

    // 处理错误
    eventSource.onerror = function (error) {
      if (streamCompleted || !isGenerating.value) return
      
      console.error('SSE连接错误:', error, 'readyState:', eventSource?.readyState)
      
      // 检查是否是正常的连接关闭
      if (eventSource?.readyState === EventSource.CLOSED) {
        streamCompleted = true
        isGenerating.value = false
      } else if (eventSource?.readyState === EventSource.CONNECTING) {
        // 连接中，可能是重连，暂时不处理
        return
      } else {
        // 显示错误提示，建议联系管理员
        Modal.error({
          title: 'AI服务异常',
          content: '当前AI模型可能配置有误或服务异常，请联系管理员检查大模型配置。',
          okText: '知道了',
        })
        handleError(new Error('SSE连接错误'), aiMessageIndex)
      }
    }
  } catch (error) {
    console.error('创建 EventSource 失败：', error)
    handleError(error, aiMessageIndex)
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number) => {
  console.error('生成失败：', error)
  
  // 安全地更新错误消息
  if (messages.value[aiMessageIndex]) {
    messages.value[aiMessageIndex].content = 'AI回复失败: ' + JSON.stringify(error)
    messages.value[aiMessageIndex].loading = false
  } else {
    // 如果消息对象不存在，创建一个新的错误消息
    messages.value.push({
      type: 'ai',
      content: 'AI回复失败: ' + JSON.stringify(error),
      loading: false,
    })
  }
  
  isGenerating.value = false
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
      
      // 如果删除的是当前选中的应用，清空消息
      if (currentAppId.value === String(appId)) {
        currentAppId.value = undefined
        messages.value = []
      }
      
      // 重新加载应用列表
      await loadAppList(true)
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
})
</script>

<style scoped>
#homePage {
  height: calc(100vh - 124px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
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
  background-color: #f9f9f9;
  border-right: 1px solid #eee;
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
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.close-sidebar-btn {
  color: #999;
}

.close-sidebar-btn:hover {
  color: #333;
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
  background: #fff;
  overflow-y: auto;
  height: 100%;
}

.sidebar-trigger {
  position: sticky;
  top: 16px;
  margin-left: 16px;
  z-index: 10;
  color: #666;
  float: left; /* 确保不占据整行 */
}

.sidebar-trigger:hover {
  color: #1890ff;
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
}

.chat-container.chat-centered .messages-scroll-area {
  flex: 0 0 auto;
  width: 100%;
  padding-top: 0;
  display: flex;
  justify-content: center;
}

.chat-container.chat-centered .input-area-fixed {
  position: static;
  width: 100%;
  max-width: 800px;
  background: transparent;
  border-top: none;
  padding: 8px 20px;
}

.chat-container.chat-centered .welcome-section {
  min-height: auto;
  padding: 0 0 16px 0;
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
  padding: 8px 72px 12px 72px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.input-wrapper {
  position: relative;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.chat-input {
  border: none !important;
  resize: none;
  padding-right: 50px;
  box-shadow: none !important;
}

.input-actions {
  position: absolute;
  bottom: 12px;
  right: 12px;
}

/* 消息样式复用 */
.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  padding: 40px 0;
  text-align: center;
}

.greeting-title {
  font-size: 24px;
  color: #333;
  margin: 0;
  font-weight: 500;
  line-height: 1.5;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 24px;
  color: #333;
  margin: 0;
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
  background: #1890ff;
  color: white;
  margin-left: 52px;
}

.ai-message .message-content {
  background: #f5f5f5;
  color: #333;
  margin-right: 52px;
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
  color: #666;
  margin-top: 4px;
}

.loading-history-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
  gap: 10px;
}

/* 对话列表样式 */
.chat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.chat-item:hover {
  background-color: #e6e6e6;
}

.chat-item.active {
  background-color: #e6f7ff;
}

.chat-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-title {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-time {
  font-size: 12px;
  color: #999;
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
    padding: 20px 16px;
  }

  .user-message .message-content {
    margin-left: 0;
    max-width: 85%;
  }

  .ai-message .message-content {
    margin-right: 0;
    max-width: 85%;
  }
}
</style>

