<template>
  <AuthPageShell>
    <div class="auth-form-header">
      <p class="auth-eyebrow">欢迎回来</p>
      <h1 class="auth-title">登录账号</h1>
      <p class="auth-desc">进入咨询小站，继续您的对话与健康信息查询</p>
    </div>
    <a-form
      class="auth-form"
      :model="formState"
      name="login"
      autocomplete="on"
      layout="vertical"
      required-mark="optional"
      @finish="handleSubmit"
    >
      <a-form-item label="账号" name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input
          v-model:value="formState.userAccount"
          placeholder="请输入账号"
          size="large"
          allow-clear
        >
          <template #prefix>
            <UserOutlined class="auth-input-icon" />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item
        label="密码"
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于 8 位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          placeholder="请输入密码"
          size="large"
        >
          <template #prefix>
            <LockOutlined class="auth-input-icon" />
          </template>
        </a-input-password>
      </a-form-item>
      <a-form-item class="auth-submit-wrap">
        <a-button type="primary" html-type="submit" block size="large" class="auth-submit-btn">
          登录
        </a-button>
      </a-form-item>
    </a-form>
    <div class="auth-footer-row">
      <span class="auth-muted">还没有账号？</span>
      <RouterLink to="/user/register" class="auth-link">去注册</RouterLink>
    </div>
  </AuthPageShell>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { userLogin } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import AuthPageShell from '@/components/auth/AuthPageShell.vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

const handleSubmit = async () => {
  const res = await userLogin(formState)
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.auth-form-header {
  margin-bottom: 28px;
}

.auth-eyebrow {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--app-accent);
}

.auth-title {
  margin: 0 0 10px;
  font-family: var(--app-font-display);
  font-size: 1.65rem;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: var(--app-text);
  line-height: 1.25;
}

.auth-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.55;
  color: var(--app-text-muted);
}

.auth-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: var(--app-text-secondary);
}

.auth-input-icon {
  color: var(--app-text-muted);
}

.auth-submit-wrap {
  margin-bottom: 8px;
}

.auth-submit-btn {
  height: 44px;
  font-weight: 500;
  font-size: 15px;
}

.auth-footer-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  padding-top: 4px;
  font-size: 14px;
}

.auth-muted {
  color: var(--app-text-muted);
}

.auth-link {
  font-weight: 500;
  color: var(--app-accent);
}

.auth-link:hover {
  color: var(--app-accent-hover);
}
</style>
