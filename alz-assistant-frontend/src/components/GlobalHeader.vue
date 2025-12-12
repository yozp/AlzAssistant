<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- 左侧：Logo和标题 -->
      <a-col :flex="'180px'">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo_compressed.png" alt="Logo" />
            <h1 class="site-title">AlzAssistant</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :size="40" :src="loginUserStore.loginUser.userAvatar" style="flex-shrink: 0;">
                  {{ loginUserStore.loginUser.userName?.[0] || 'U' }}
                </a-avatar>
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu class="user-dropdown-menu">
                  <a-menu-item key="profile" @click="goToProfile">
                    <UserOutlined />
                    <span style="margin-left: 8px">个人信息</span>
                  </a-menu-item>
                  <a-menu-item key="logout" @click="doLogout">
                    <LogoutOutlined />
                    <span style="margin-left: 8px">退出登录</span>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { LogoutOutlined, HomeOutlined, UserOutlined, BookOutlined } from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    icon: () => h(UserOutlined),
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/knowledgeBase',
    icon: () => h(BookOutlined),
    label: '知识库管理',
    title: '知识库管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 跳转个人信息
const goToProfile = () => {
  router.push('/user/profile')
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  padding: 0 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 64px;
  line-height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}

.logo {
  height: 40px;
  width: 40px;
  flex-shrink: 0;
}

.site-title {
  margin: 0;
  font-size: 16px;
  color: #1890ff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.ant-menu-horizontal {
  border-bottom: none !important;
}

.user-dropdown-menu {
  min-width: 140px;
}

.user-dropdown-menu :deep(.ant-dropdown-menu-item) {
  padding: 10px 16px;
  display: flex;
  align-items: center;
}

.user-dropdown-menu :deep(.anticon) {
  font-size: 14px;
}

/* 确保头像图片清晰渲染 */
:deep(.ant-avatar) {
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
}

:deep(.ant-avatar img) {
  object-fit: cover;
}
</style>

