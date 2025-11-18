<template>
  <div id="homePage">
    <a-layout class="chat-layout">
      <!-- 主内容区域 -->
      <a-layout-content class="main-content">
        <!-- 打开抽屉按钮 -->
        <a-button
          type="text"
          class="drawer-trigger"
          @click="drawerVisible = true"
        >
          <template #icon>
            <MenuOutlined />
          </template>
        </a-button>
        <!-- 聊天区域 -->
        <div class="chat-container">
          <!-- 消息列表 -->
          <div class="messages-container" ref="messagesContainer">
            <div v-if="loadingHistory" class="loading-history-wrapper">
              <a-spin :spinning="true" size="large" />
              <span class="loading-text">加载对话历史中...</span>
            </div>
            <div v-else-if="messages.length === 0 && !isGenerating" class="welcome-section">
              <div class="welcome-icon">🤖</div>
              <h2 class="welcome-title">今天有什么可以帮到你?</h2>
            </div>

            <div v-for="(message, index) in messages" :key="`message-${index}-${message.createTime || index}`" class="message-item">
              <div v-if="message.type === 'user'" class="user-message">
                <div class="message-content">{{ message.content }}</div>
                <div class="message-avatar">
                  <a-avatar :src="loginUserStore.loginUser.userAvatar">
                    {{ loginUserStore.loginUser.userName?.[0] || 'U' }}
                  </a-avatar>
                </div>
              </div>
              <div v-else class="ai-message">
                <div class="message-avatar">
                  <a-avatar style="background-color: #1890ff">AI</a-avatar>
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

          <!-- 输入区域 -->
          <div class="input-container">
            <div class="input-wrapper">
              <a-textarea
                v-model:value="userInput"
                placeholder="给 AI 发送消息"
                :rows="4"
                :maxlength="1000"
                @keydown.enter.exact.prevent="sendMessage"
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
            <div class="input-tips">
              <a-button type="text" size="small" class="tip-btn">
                <template #icon>
                  <BulbOutlined />
                </template>
                深度思考
              </a-button>
              <a-button type="text" size="small" class="tip-btn">
                <template #icon>
                  <GlobalOutlined />
                </template>
                联网搜索
              </a-button>
            </div>
          </div>
        </div>
      </a-layout-content>
    </a-layout>

    <!-- 抽屉式对话列表 -->
     <a-drawer
       v-model:open="drawerVisible"
       title="我的对话"
       placement="left"
       :width="280"
       :closable="true"
       class="chat-drawer"
       @after-open="handleDrawerOpen"
     >
      <div class="drawer-content">
        <!-- 新建对话按钮 -->
        <a-button
          type="primary"
          block
          class="new-chat-btn"
          @click="handleCreateNewApp"
          :loading="creatingApp"
        >
          <template #icon>
            <PlusOutlined />
          </template>
          开启新对话
        </a-button>

         <!-- 对话列表 -->
         <div class="chat-list" ref="chatListRef" @scroll="handleChatListScroll">
           <div
             v-for="app in appList"
             :key="app.id"
             class="chat-item"
             :class="{ active: currentAppId === app.id }"
           >
             <div class="chat-item-content" @click="handleAppClick(app.id)">
               <span class="chat-title">{{ app.appName || '新对话' }}</span>
               <span class="chat-time">{{ formatTime(app.createTime) }}</span>
             </div>
             <a-button
               type="text"
               size="small"
               danger
               class="delete-btn"
               @click.stop="handleDeleteApp(app.id, app.appName)"
               :loading="deletingAppId === app.id"
             >
               <template #icon>
                 <DeleteOutlined />
               </template>
             </a-button>
           </div>
           <a-empty v-if="appList.length === 0 && !loadingApps && !loadingMore" description="暂无对话" />
           <div v-if="loadingApps" class="loading-wrapper">
             <a-spin :spinning="true" />
           </div>
           <div v-if="loadingMore" class="loading-more-wrapper">
             <a-spin :spinning="true" size="small" />
             <span class="loading-text">加载中...</span>
           </div>
           <div v-if="!pagination.hasMore && appList.length > 0" class="no-more-wrapper">
             <span class="no-more-text">没有更多了</span>
           </div>
         </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
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
} from '@ant-design/icons-vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 抽屉状态
const drawerVisible = ref(false)

// 应用列表
const appList = ref<API.AppVO[]>([])
const loadingApps = ref(false)
const loadingMore = ref(false) // 加载更多状态
const currentAppId = ref<number>()
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
const deletingAppId = ref<number | undefined>()

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

      // 如果是首次加载且有应用列表，默认选择第一个
      if (reset && appList.value.length > 0 && !currentAppId.value) {
        selectApp(appList.value[0].id)
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

// 创建新应用
const handleCreateNewApp = async () => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creatingApp.value = true
  try {
    // 使用默认提示词创建新应用
    const res = await addApp({
      initPrompt: '新对话', // 使用默认提示词
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('创建成功')
      // 重新加载应用列表（重置）
      await loadAppList(true)
      // 选择新创建的应用
      selectApp(res.data.data)
      // 关闭抽屉
      drawerVisible.value = false
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creatingApp.value = false
  }
}

// 选择应用
const selectApp = async (appId: number | undefined) => {
  if (!appId) {
    message.warning('应用ID无效')
    return
  }

  // 如果已经选择了该应用，不重复加载
  if (currentAppId.value === appId && messages.value.length > 0) {
    return
  }

  // 设置当前应用ID
  currentAppId.value = appId
  // 清空当前消息列表
  messages.value = []
  loadingHistory.value = true

  // 加载该应用的对话历史
  try {
    const res = await listAppChatHistory({
      appId: appId,
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
        scrollToBottom()
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
  scrollToBottom()

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
          fullContent += content
          // 安全地更新消息内容
          if (messages.value[aiMessageIndex]) {
            messages.value[aiMessageIndex].content = fullContent
            messages.value[aiMessageIndex].loading = false
            scrollToBottom()
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
    messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
    messages.value[aiMessageIndex].loading = false
  } else {
    // 如果消息对象不存在，创建一个新的错误消息
    messages.value.push({
      type: 'ai',
      content: '抱歉，生成过程中出现了错误，请重试。',
      loading: false,
    })
  }
  
  message.error('生成失败，请重试')
  isGenerating.value = false
}

// 滚动到底部
const scrollToBottom = () => {
  // 使用 nextTick 确保 DOM 更新后再滚动
  nextTick(() => {
    // 尝试滚动消息容器
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
    // 同时滚动整个页面到底部，确保输入框可见
    setTimeout(() => {
      window.scrollTo({
        top: document.documentElement.scrollHeight,
        behavior: 'smooth',
      })
    }, 100)
  })
}

// 处理应用点击事件
const handleAppClick = async (appId: number | undefined) => {
  if (!appId) return
  
  // 选择应用并加载历史对话
  await selectApp(appId)
  
  // 关闭抽屉
  drawerVisible.value = false
  
  // 滚动到顶部，确保能看到最新的对话
  await nextTick()
  scrollToBottom()
}

// 删除应用
const handleDeleteApp = async (appId: number | undefined, appName?: string) => {
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
const performDeleteApp = async (appId: number) => {

  deletingAppId.value = appId
  try {
    const res = await deleteApp({
      id: appId,
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('删除成功')
      
      // 如果删除的是当前选中的应用，清空消息
      if (currentAppId.value === appId) {
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

// 处理抽屉打开事件
const handleDrawerOpen = () => {
  // 打开抽屉时重新加载应用列表（重置）
  if (loginUserStore.loginUser.id) {
    loadAppList(true)
  }
}

// 页面加载时获取数据
onMounted(() => {
  // 如果用户已登录，加载应用列表
  if (loginUserStore.loginUser.id) {
    loadAppList(true)
  }
  // 初始化临时聊天室（不创建应用，只是准备聊天环境）
  // 用户发送第一条消息时才会创建应用
})
</script>

<style scoped>
#homePage {
  min-height: calc(100vh - 64px - 60px); /* 减去header和footer的高度 */
  overflow-y: auto; /* 允许页面滚动 */
  overflow-x: hidden;
}

.chat-layout {
  min-height: 100%;
}

.drawer-trigger {
  position: fixed;
  top: 80px;
  left: 20px;
  z-index: 10;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.drawer-content {
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.new-chat-btn {
  margin-bottom: 16px;
  height: 40px;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.loading-more-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 16px;
  color: #999;
}

.loading-text {
  font-size: 12px;
  color: #999;
}

.no-more-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
}

.no-more-text {
  font-size: 12px;
  color: #ccc;
}

.chat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 8px;
  transition: background-color 0.2s;
  position: relative;
}

.chat-item:hover {
  background-color: #f5f5f5;
}

.chat-item:hover .delete-btn {
  opacity: 1;
}

.chat-item.active {
  background-color: #e6f7ff;
}

.chat-item-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  cursor: pointer;
  min-width: 0; /* 允许内容收缩 */
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
  flex-shrink: 0;
  margin-left: 8px;
  color: #ff4d4f;
}

.delete-btn:hover {
  color: #ff7875;
  background-color: #fff1f0;
}

.chat-title {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-time {
  font-size: 12px;
  color: #999;
}

.main-content {
  background: #f5f5f5;
  min-height: calc(100vh - 64px - 60px);
  padding-bottom: 20px; /* 底部留出空间 */
}

.chat-container {
  display: flex;
  flex-direction: column;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;
  min-height: calc(100vh - 64px - 60px);
}

.messages-container {
  flex: 1;
  padding: 20px 0;
  scroll-behavior: smooth;
  min-height: 400px; /* 确保有足够的高度 */
}

.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px 0;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 24px;
  color: #1890ff;
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
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.user-message .message-content {
  background: #1890ff;
  color: white;
}

.ai-message .message-content {
  background: #fff;
  color: #333;
  border: 1px solid #e8e8e8;
  overflow: visible; /* 允许内容溢出，确保代码块等可以正常显示 */
}

.message-avatar {
  flex-shrink: 0;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.loading-history-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
  color: #666;
}

.input-container {
  padding-top: 20px;
  padding-bottom: 20px;
  position: relative;
  background: #f5f5f5;
}

.input-wrapper {
  position: relative;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chat-input {
  border: none;
  resize: none;
  padding-right: 50px;
}

.chat-input:focus {
  box-shadow: none;
}

.input-actions {
  position: absolute;
  bottom: 12px;
  right: 12px;
}

.send-btn {
  border-radius: 8px;
}

.input-tips {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 12px;
}

.tip-btn {
  color: #666;
}

.tip-btn:hover {
  color: #1890ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .drawer-trigger {
    top: 70px;
    left: 10px;
  }

  .chat-container {
    padding: 12px;
  }

  .message-content {
    max-width: 85%;
  }

  .welcome-title {
    font-size: 20px;
  }
}
</style>

