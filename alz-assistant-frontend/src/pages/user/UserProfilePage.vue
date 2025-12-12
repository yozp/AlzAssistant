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
            <a-space direction="vertical" style="margin-top: 12px; width: 100%">
              <a-upload
                :show-upload-list="false"
                :before-upload="beforeUpload"
                accept="image/jpeg,image/jpg,image/png,image/webp"
              >
                <a-button type="primary" ghost>
                  <UploadOutlined />
                  上传图片文件
                </a-button>
              </a-upload>
              <a-input
                v-model:value="urlInput"
                placeholder="或输入图片 URL 后点击下方按钮上传"
              />
              <a-button v-if="urlInput" type="link" size="small" @click="handleUrlUpload">
                从 URL 上传
              </a-button>
            </a-space>
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
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import { getMyInfo, updateMyInfo } from '@/api/userController'
import { API_BASE_URL } from '@/config/env'
import axios from 'axios'

const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const userInfo = ref<API.UserVO>()
const urlInput = ref('')
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

const beforeUpload = async (file: File) => {
  const isImage = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(file.type)
  if (!isImage) {
    message.error('只能上传 JPG、PNG、WEBP 格式的图片')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过 5MB')
    return false
  }

  uploading.value = true
  try {
    const uploadFormData = new FormData()
    uploadFormData.append('file', file)
    const res = await axios.post(`${API_BASE_URL}/user/upload/avatar`, uploadFormData, {
      withCredentials: true,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    if (res.data && res.data.code === 0 && res.data.data) {
      formData.value.userAvatar = res.data.data.url
      message.success('上传成功')
      
      // 自动保存到数据库
      await saveAvatar()
    } else {
      message.error('上传失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: any) {
    console.error('上传失败：', error)
    if (error.response && error.response.data) {
      message.error('上传失败：' + (error.response.data.message || '未知错误'))
    } else {
      message.error('上传失败')
    }
  } finally {
    uploading.value = false
  }
  return false
}

const handleUrlUpload = async () => {
  if (!urlInput.value || !urlInput.value.startsWith('http')) {
    message.error('请输入有效的图片 URL')
    return
  }

  uploading.value = true
  try {
    const res = await axios.post(
      `${API_BASE_URL}/user/upload/avatar/url`,
      null,
      {
        params: { fileUrl: urlInput.value },
        withCredentials: true,
      }
    )

    if (res.data && res.data.code === 0 && res.data.data) {
      formData.value.userAvatar = res.data.data.url
      urlInput.value = ''
      message.success('上传成功')
      
      // 自动保存到数据库
      await saveAvatar()
    } else {
      message.error('上传失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: any) {
    console.error('上传失败：', error)
    if (error.response && error.response.data) {
      message.error('上传失败：' + (error.response.data.message || '未知错误'))
    } else {
      message.error('上传失败')
    }
  } finally {
    uploading.value = false
  }
}

const saveAvatar = async () => {
  try {
    const res = await updateMyInfo({ userAvatar: formData.value.userAvatar })
    if (res.data.code === 0) {
      message.success('头像已保存')
      await loadUserInfo()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error) {
    message.error('保存失败')
  }
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

