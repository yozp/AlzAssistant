<template>
  <div class="profile-page">
    <header class="profile-page-head">
      <h1 class="profile-page-title">个人资料</h1>
    </header>

    <a-spin :spinning="loading">
      <article class="profile-shell">
        <div class="profile-cover" aria-hidden="true">
          <div class="profile-cover-gradient" />
          <div class="profile-cover-noise" />
        </div>

        <div class="profile-hero">
          <div class="profile-avatar-col">
            <div class="profile-avatar-ring">
              <a-avatar :size="96" class="profile-avatar" :src="formData.userAvatar">
                {{ displayInitial }}
              </a-avatar>
            </div>
          </div>

          <div class="profile-hero-main">
            <h2 class="profile-display-name">{{ displayName }}</h2>
            <p v-if="hasCustomDisplayName" class="profile-handle" :title="userInfo?.userAccount">
              <span class="profile-handle-at">@</span>{{ userInfo?.userAccount }}
            </p>

            <div v-if="hasBio" class="profile-bio" role="text">
              {{ formData.userProfile?.trim() }}
            </div>

            <div class="profile-cta">
              <a-button type="primary" size="large" class="profile-edit-btn" @click="openEditModal">
                <EditOutlined />
                修改资料
              </a-button>
            </div>
          </div>
        </div>

        <section class="profile-section profile-section--account" aria-labelledby="account-heading">
          <h3 id="account-heading" class="profile-section-title">账号</h3>

          <div class="profile-rows" role="list">
            <div class="profile-row" role="listitem">
              <span class="profile-row-lead" aria-hidden="true">
                <IdcardOutlined />
              </span>
              <div class="profile-row-body">
                <span class="profile-row-label">角色</span>
                <span class="profile-row-value">{{ userInfo?.userRole === 'admin' ? '管理员' : '普通用户' }}</span>
              </div>
            </div>
            <div class="profile-row profile-row--last" role="listitem">
              <span class="profile-row-lead" aria-hidden="true">
                <ClockCircleOutlined />
              </span>
              <div class="profile-row-body">
                <span class="profile-row-label">注册时间</span>
                <span class="profile-row-value profile-row-value--wrap">{{ formatDateFull(userInfo?.createTime) }}</span>
              </div>
            </div>
          </div>
        </section>
      </article>
    </a-spin>

    <a-modal
      v-model:open="editVisible"
      title="编辑资料"
      :width="520"
      :confirm-loading="submitting"
      destroy-on-close
      :mask-closable="false"
      class="profile-edit-modal"
      @cancel="onEditCancel"
    >
      <template #footer>
        <a-button @click="onEditCancel">取消</a-button>
        <a-button type="primary" :loading="submitting" @click="submitEdit">保存</a-button>
      </template>

      <a-form
        ref="editFormRef"
        layout="vertical"
        class="edit-form"
        :model="editForm"
        :rules="editRules"
      >
        <a-form-item label="头像" name="userAvatar">
          <div class="edit-avatar-row">
            <a-avatar :size="64" :src="editForm.userAvatar">
              {{ editDisplayInitial }}
            </a-avatar>
            <div class="edit-avatar-actions">
              <a-space wrap>
                <a-upload
                  :show-upload-list="false"
                  :before-upload="beforeUpload"
                  accept="image/jpeg,image/jpg,image/png,image/webp"
                >
                  <a-button :loading="uploading">
                    <UploadOutlined />
                    上传图片
                  </a-button>
                </a-upload>
              </a-space>
              <div class="edit-url-row">
                <a-input
                  v-model:value="urlInput"
                  allow-clear
                  placeholder="图片 URL"
                  @press-enter="handleUrlUpload"
                />
                <a-button :disabled="!urlInput" :loading="uploading" @click="handleUrlUpload">
                  使用链接
                </a-button>
              </div>
            </div>
          </div>
        </a-form-item>

        <a-form-item label="显示名称" name="userName">
          <a-input
            v-model:value="editForm.userName"
            placeholder="显示名称"
            :maxlength="20"
            show-count
          />
        </a-form-item>

        <a-form-item label="个人简介" name="userProfile">
          <a-textarea
            v-model:value="editForm.userProfile"
            placeholder="个人简介"
            :rows="4"
            :maxlength="200"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { message } from 'ant-design-vue'
import { UploadOutlined, EditOutlined, IdcardOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'
import { getMyInfo, updateMyInfo } from '@/api/userController'
import { API_BASE_URL } from '@/config/env'
import axios from 'axios'

const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const userInfo = ref<API.UserVO>()
const urlInput = ref('')
const editVisible = ref(false)
const editFormRef = ref<FormInstance>()

const formData = ref({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const editForm = ref({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const editRules: Record<string, Rule[]> = {
  userName: [{ max: 20, message: '不超过 20 字' }],
}

const displayName = computed(() => {
  const n = formData.value.userName?.trim()
  if (n) return n
  const acc = userInfo.value?.userAccount
  if (acc) return acc
  return '用户'
})

const displayInitial = computed(() => {
  const n = displayName.value
  return n && n.length > 0 ? n[0] : 'U'
})

const editDisplayInitial = computed(() => {
  const n = (editForm.value.userName?.trim() || userInfo.value?.userAccount || 'U') as string
  return n[0] || 'U'
})

const hasBio = computed(() => !!formData.value.userProfile?.trim())

/** 已设置显示名时，主标题与登录账号区分展示，避免与标题重复 */
const hasCustomDisplayName = computed(() => !!formData.value.userName?.trim())

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
  } catch {
    message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const openEditModal = () => {
  editForm.value = {
    userName: formData.value.userName,
    userAvatar: formData.value.userAvatar,
    userProfile: formData.value.userProfile,
  }
  urlInput.value = ''
  editVisible.value = true
}

const onEditCancel = () => {
  editVisible.value = false
}

const submitEdit = async () => {
  try {
    await editFormRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const res = await updateMyInfo(editForm.value)
    if (res.data.code === 0) {
      message.success('保存成功')
      editVisible.value = false
      await loadUserInfo()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const formatDateFull = (dateStr?: string) => {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
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
      editForm.value.userAvatar = res.data.data.url
      message.success('已添加，请保存')
    } else {
      message.error('上传失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: unknown) {
    console.error('上传失败：', error)
    const err = error as { response?: { data?: { message?: string } } }
    if (err.response?.data) {
      message.error('上传失败：' + (err.response.data.message || '未知错误'))
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
    message.error('请输入有效的图片 URL（以 http 开头）')
    return
  }

  uploading.value = true
  try {
    const res = await axios.post(`${API_BASE_URL}/user/upload/avatar/url`, null, {
      params: { fileUrl: urlInput.value },
      withCredentials: true,
    })

    if (res.data && res.data.code === 0 && res.data.data) {
      editForm.value.userAvatar = res.data.data.url
      urlInput.value = ''
      message.success('已添加，请保存')
    } else {
      message.error('上传失败：' + (res.data?.message || '未知错误'))
    }
  } catch (error: unknown) {
    console.error('上传失败：', error)
    const err = error as { response?: { data?: { message?: string } } }
    if (err.response?.data) {
      message.error('上传失败：' + (err.response.data.message || '未知错误'))
    } else {
      message.error('上传失败')
    }
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 8px 16px 56px;
}

.profile-page-head {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--app-border);
}

.profile-page-title {
  margin: 0;
  font-family: var(--app-font-display);
  font-size: clamp(1.4rem, 3vw, 1.65rem);
  font-weight: 600;
  letter-spacing: -0.03em;
  color: var(--app-text);
}

/* 主卡片：封面 + 身份区 */
.profile-shell {
  border-radius: var(--app-radius-lg);
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  box-shadow: var(--app-shadow-md);
  overflow: hidden;
  animation: profileShellIn 0.55s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes profileShellIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.profile-cover {
  position: relative;
  height: clamp(88px, 18vw, 120px);
  overflow: hidden;
}

.profile-cover-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    118deg,
    var(--app-accent-soft) 0%,
    var(--app-surface-muted) 45%,
    var(--app-surface) 100%
  );
}

.profile-cover-noise {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image: repeating-linear-gradient(
    -12deg,
    transparent,
    transparent 2px,
    rgba(28, 25, 23, 0.03) 2px,
    rgba(28, 25, 23, 0.03) 3px
  );
  pointer-events: none;
}

.profile-hero {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0;
  padding: 0 20px 28px;
  margin-top: -48px;
  position: relative;
  z-index: 1;
}

@media (min-width: 600px) {
  .profile-hero {
    grid-template-columns: auto 1fr;
    gap: 24px 28px;
    padding: 0 32px 32px;
    margin-top: -52px;
    align-items: start;
  }
}

.profile-avatar-col {
  display: flex;
  justify-content: center;
}

@media (min-width: 600px) {
  .profile-avatar-col {
    justify-content: flex-start;
  }
}

.profile-avatar-ring {
  padding: 4px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--app-surface) 0%, var(--app-surface-muted) 100%);
  box-shadow:
    0 12px 32px rgba(28, 25, 23, 0.12),
    0 0 0 1px var(--app-border);
}

.profile-avatar {
  display: block;
}

.profile-avatar :deep(.ant-avatar) {
  image-rendering: -webkit-optimize-contrast;
}

.profile-hero-main {
  min-width: 0;
  padding-top: 8px;
  text-align: center;
}

@media (min-width: 600px) {
  .profile-hero-main {
    text-align: left;
    padding-top: 56px;
  }
}

.profile-handle {
  margin: 8px 0 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-muted);
  letter-spacing: 0.02em;
  word-break: break-all;
}

.profile-handle-at {
  color: var(--app-accent);
  font-weight: 600;
}

.profile-display-name {
  margin: 0;
  font-family: var(--app-font-display);
  font-size: clamp(1.45rem, 3.5vw, 1.85rem);
  font-weight: 600;
  letter-spacing: -0.03em;
  line-height: 1.2;
  color: var(--app-text);
}

.profile-bio {
  margin: 18px 0 0;
  font-size: 15px;
  line-height: 1.65;
  color: var(--app-text-secondary);
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
}

.profile-cta {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}

@media (min-width: 600px) {
  .profile-cta {
    justify-content: flex-start;
  }
}

.profile-edit-btn {
  min-width: 132px;
  height: 44px;
  font-weight: 500;
}

/* 账号分组：类 iOS 设置行 */
.profile-section {
  padding: 8px 20px 24px;
  border-top: 1px solid var(--app-border);
  background: linear-gradient(180deg, var(--app-surface-muted) 0%, var(--app-surface) 28%);
}

@media (min-width: 600px) {
  .profile-section {
    padding: 12px 32px 28px;
  }
}

.profile-section-title {
  margin: 0 0 14px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--app-text-muted);
}

.profile-rows {
  border-radius: var(--app-radius-sm);
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  overflow: hidden;
}

.profile-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--app-border);
  transition: background 0.2s ease;
}

.profile-row:last-child,
.profile-row--last {
  border-bottom: none;
}

@media (hover: hover) {
  .profile-row:hover {
    background: var(--app-surface-elevated);
  }
}

.profile-row-lead {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: var(--app-accent-soft);
  color: var(--app-accent);
  font-size: 16px;
}

.profile-row-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

@media (min-width: 520px) {
  .profile-row-body {
    flex-direction: row;
    align-items: baseline;
    justify-content: space-between;
    gap: 16px;
  }
}

.profile-row-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--app-text-muted);
}

.profile-row-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  text-align: right;
  word-break: break-all;
}

.profile-row-value--wrap {
  font-weight: 400;
  line-height: 1.45;
}

@media (max-width: 519px) {
  .profile-row-value {
    text-align: left;
  }
}

.edit-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
}

.edit-avatar-row {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

@media (min-width: 480px) {
  .edit-avatar-row {
    flex-direction: row;
    align-items: flex-start;
  }
}

.edit-avatar-actions {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.edit-url-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.edit-url-row .ant-input {
  flex: 1;
  min-width: 180px;
}
</style>

<style>
.profile-edit-modal .ant-modal-title {
  font-family: var(--app-font-display);
  font-weight: 600;
}
</style>
