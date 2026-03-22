<template>
  <AuthPageShell>
    <div class="auth-form-header">
      <p class="auth-eyebrow">开始使用</p>
      <h1 class="auth-title">创建账号</h1>
      <p class="auth-desc">注册后即可保存对话、使用评估记录与更多功能</p>
    </div>
    <a-form
      class="auth-form"
      :model="formState"
      name="register"
      autocomplete="on"
      layout="vertical"
      required-mark="optional"
      @finish="handleSubmit"
    >
      <a-form-item label="账号" name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input
          v-model:value="formState.userAccount"
          placeholder="设置登录账号（建议字母或数字）"
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
          { min: 8, message: '密码不能小于 8 位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          placeholder="至少 8 位字符"
          size="large"
        >
          <template #prefix>
            <LockOutlined class="auth-input-icon" />
          </template>
        </a-input-password>
      </a-form-item>
      <a-form-item
        label="确认密码"
        name="checkPassword"
        :rules="[
          { required: true, message: '请确认密码' },
          { min: 8, message: '密码不能小于 8 位' },
          { validator: validateCheckPassword },
        ]"
      >
        <a-input-password
          v-model:value="formState.checkPassword"
          placeholder="再次输入密码"
          size="large"
        >
          <template #prefix>
            <LockOutlined class="auth-input-icon" />
          </template>
        </a-input-password>
      </a-form-item>
      <a-form-item class="auth-submit-wrap">
        <a-button type="primary" html-type="submit" block size="large" class="auth-submit-btn">
          注册
        </a-button>
      </a-form-item>
    </a-form>
    <div class="auth-footer-row">
      <span class="auth-muted">已有账号？</span>
      <RouterLink to="/user/login" class="auth-link">去登录</RouterLink>
    </div>
  </AuthPageShell>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import AuthPageShell from '@/components/auth/AuthPageShell.vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const validateCheckPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
.auth-form-header {
  margin-bottom: 24px;
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
