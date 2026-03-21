<template>
  <div class="chat-attachment-card-wrap">
    <a
      v-if="url && !uploading"
      :href="url"
      target="_blank"
      rel="noopener noreferrer"
      class="chat-attachment-card"
    >
      <div class="chat-attachment-icon">
        <FileImageOutlined v-if="isImage" />
        <FileOutlined v-else />
      </div>
      <div class="chat-attachment-meta">
        <div class="chat-attachment-name">{{ fileName }}</div>
        <div class="chat-attachment-sub">{{ secondaryLine }}</div>
      </div>
    </a>
    <div v-else class="chat-attachment-card is-disabled">
      <div class="chat-attachment-icon">
        <FileImageOutlined v-if="isImage" />
        <FileOutlined v-else />
      </div>
      <div class="chat-attachment-meta">
        <div class="chat-attachment-name">{{ fileName }}</div>
        <div class="chat-attachment-sub">{{ secondaryLine }}</div>
      </div>
    </div>
    <button
      v-if="showRemove"
      type="button"
      class="chat-attachment-remove"
      aria-label="移除"
      @click.stop="$emit('remove')"
    >
      <CloseOutlined />
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { FileOutlined, FileImageOutlined, CloseOutlined } from '@ant-design/icons-vue'

/** 与后端 ChatAttachmentType.code 一致，可选，缺省时按扩展名推断 */
const props = withDefaults(
  defineProps<{
    fileName: string
    size: number
    url?: string
    uploading?: boolean
    showRemove?: boolean
    attachmentType?: string
  }>(),
  { uploading: false, showRemove: false }
)

defineEmits<{ remove: [] }>()

const TYPE_LABEL: Record<string, string> = {
  image: '图片',
  document: '文档',
  spreadsheet: '表格',
  text: '文本',
}

function inferTypeFromName(name: string): string | undefined {
  const m = name.match(/\.([^.]+)$/)
  const ext = m?.[1]
  if (!ext) return undefined
  const e = ext.toLowerCase()
  if (['png', 'jpg', 'jpeg', 'gif', 'webp'].includes(e)) return 'image'
  if (['pdf', 'doc', 'docx'].includes(e)) return 'document'
  if (['xls', 'xlsx'].includes(e)) return 'spreadsheet'
  if (['txt', 'md'].includes(e)) return 'text'
  return undefined
}

const resolvedType = computed(() => props.attachmentType || inferTypeFromName(props.fileName))

const isImage = computed(() => resolvedType.value === 'image')

function formatSize(n: number) {
  if (!n && n !== 0) return ''
  if (n < 1024) return `${n}B`
  if (n < 1048576) return `${(n / 1024).toFixed(2)}KB`
  return `${(n / 1048576).toFixed(2)}MB`
}

function fileExt(name: string) {
  const i = name.lastIndexOf('.')
  return i >= 0 ? name.slice(i + 1) : '?'
}

const secondaryLine = computed(() => {
  if (props.uploading) return '上传中…'
  const ext = fileExt(props.fileName).toUpperCase()
  const rt = resolvedType.value
  const label = rt && TYPE_LABEL[rt] ? `${TYPE_LABEL[rt]} · ` : ''
  return `${label}${ext} ${formatSize(props.size)}`
})
</script>

<style scoped>
.chat-attachment-card-wrap {
  position: relative;
  display: inline-flex;
  max-width: 280px;
}

.chat-attachment-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  min-width: 0;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  text-decoration: none;
  color: inherit;
  transition: border-color 0.2s;
}

a.chat-attachment-card:hover {
  border-color: #1890ff;
}

.chat-attachment-card.is-disabled {
  cursor: default;
  opacity: 0.95;
}

.chat-attachment-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #1890ff;
  background: #e6f7ff;
  border-radius: 8px;
}

.chat-attachment-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chat-attachment-name {
  font-size: 14px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-attachment-sub {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.chat-attachment-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 22px;
  height: 22px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: rgba(0, 0, 0, 0.45);
}

.chat-attachment-remove:hover {
  color: #ff4d4f;
}
</style>
