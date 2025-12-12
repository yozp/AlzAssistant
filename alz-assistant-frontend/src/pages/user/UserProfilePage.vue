<template>
  <div class="profile-container">
    <a-card title="个人信息" :loading="loading">
      <a-form
        :model="formData"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 16 }"
        @finish="handleSubmit"
      >
        <a-form-item label="头像">
          <div class="avatar-wrapper">
            <a-avatar :size="80" :src="formData.userAvatar">
              {{ formData.userName?.[0] || 'U' }}
            </a-avatar>
            <a-input
              v-model:value="formData.userAvatar"
              placeholder="请输入头像 URL"
              style="margin-top: 12px"
            />
          </div>
        </a-form-item>

        <a-form-item label="用户名" name="userName">
          <a-input
            v-model:value="formData.userName"
            placeholder="请输入用户名"
            :maxlength="20"
          />
        </a-form-item>

        <a-form-item label="个人简介" name="userProfile">
          <a-textarea
            v-model:value="formData.userProfile"
            placeholder="请输入个人简介"
            :rows="4"
            :maxlength="200"
            show-count
          />
        </a-form-item>

        <a-form-item label="账号">
          <a-input :value="userInfo?.userAccount" disabled />
        </a-form-item>

        <a-form-item label="用户角色">
          <a-tag :color="userInfo?.userRole === 'admin' ? 'blue' : 'default'">
            {{ userInfo?.userRole === 'admin' ? '管理员' : '普通用户' }}
          </a-tag>
        </a-form-item>

        <a-form-item label="注册时间">
          <span>{{ formatDate(userInfo?.createTime) }}</span>
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
          <a-space>
            <a-button type="primary" html-type="submit" :loading="submitting">
              保存修改
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getMyInfo, updateMyInfo } from '@/api/userController'

const loading = ref(false)
const submitting = ref(false)
const userInfo = ref<API.UserVO>()
const formData = ref({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const loadUserInfo = async () => {
  loading.value = true
  try {
    const res = await getMyInfo()
    if (res.data.code === 0 && res.data.data) {
      userInfo.value = res.data.data
      formData.value = {
        userName: res.data.data.userName || '',
        userAvatar: res.data.data.userAvatar || '',
        userProfile: res.data.data.userProfile || '',
      }
    } else {
      message.error('获取用户信息失败：' + res.data.message)
    }
  } catch (error) {
    message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await updateMyInfo(formData.value)
    if (res.data.code === 0) {
      message.success('保存成功')
      await loadUserInfo()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error) {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  if (userInfo.value) {
    formData.value = {
      userName: userInfo.value.userName || '',
      userAvatar: userInfo.value.userAvatar || '',
      userProfile: userInfo.value.userProfile || '',
    }
  }
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 24px auto;
  padding: 0 16px;
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
</style>

