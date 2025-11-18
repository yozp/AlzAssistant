<template>
  <div class="markdown-content" v-html="renderedContent"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
// 导入代码高亮样式（使用GitHub风格，适合浅色背景）
import 'highlight.js/styles/github.css'

const props = defineProps<{
  content: string
}>()

// 配置 marked
marked.setOptions({
  highlight: function (code: string, lang?: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (err) {
        console.error('代码高亮失败:', err)
      }
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true, // 支持换行
  gfm: true, // 支持GitHub风格的Markdown
} as any)

// 渲染Markdown内容
const renderedContent = computed(() => {
  if (!props.content) return ''
  try {
    return marked(props.content) as string
  } catch (error) {
    console.error('Markdown渲染失败:', error)
    return props.content
  }
})
</script>

<style scoped>
.markdown-content {
  line-height: 1.6;
  color: #333;
  word-wrap: break-word;
  overflow-wrap: break-word;
  font-size: 15px;
}

/* 标题样式 */
.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin: 1em 0 0.4em 0;
  font-weight: 600;
  line-height: 1.25;
  color: #262626;
}

.markdown-content :deep(h1:first-child),
.markdown-content :deep(h2:first-child),
.markdown-content :deep(h3:first-child),
.markdown-content :deep(h4:first-child),
.markdown-content :deep(h5:first-child),
.markdown-content :deep(h6:first-child) {
  margin-top: 0;
}

.markdown-content :deep(h1) {
  font-size: 24px;
  border-bottom: 1px solid #e8e8e8;
  padding-bottom: 8px;
}

.markdown-content :deep(h2) {
  font-size: 20px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 6px;
}

.markdown-content :deep(h3) {
  font-size: 18px;
}

.markdown-content :deep(h4) {
  font-size: 16px;
}

.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  font-size: 15px;
  color: #595959;
}

/* 段落样式 */
.markdown-content :deep(p) {
  margin: 0.5em 0;
}

/* 列表样式 */
.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 0.5em 0;
  padding-left: 1.5em;
}

.markdown-content :deep(li) {
  margin: 0.25em 0;
}

.markdown-content :deep(ul li) {
  list-style-type: disc;
}

.markdown-content :deep(ol li) {
  list-style-type: decimal;
}

/* 嵌套列表 */
.markdown-content :deep(ul ul),
.markdown-content :deep(ol ol),
.markdown-content :deep(ul ol),
.markdown-content :deep(ol ul) {
  margin: 0.25em 0;
}

/* 强调样式 */
.markdown-content :deep(strong) {
  font-weight: 600;
  color: #262626;
}

.markdown-content :deep(em) {
  font-style: italic;
  color: #595959;
}

/* 代码样式 */
.markdown-content :deep(code) {
  padding: 2px 6px;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  color: #d63384;
}

.markdown-content :deep(pre) {
  background-color: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  padding: 1em;
  overflow-x: auto;
  margin: 0.8em 0;
  line-height: 1.5;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: #24292e;
  font-size: 13px;
  display: block;
  overflow-x: auto;
}

/* 引用样式 */
.markdown-content :deep(blockquote) {
  border-left: 4px solid #1890ff;
  padding: 0.5em 1em;
  margin: 0.8em 0;
  background: rgba(24, 144, 255, 0.05);
  color: #595959;
  border-radius: 4px;
}

.markdown-content :deep(blockquote p) {
  margin: 0;
}

/* 链接样式 */
.markdown-content :deep(a) {
  color: #1890ff;
  text-decoration: none;
  transition: color 0.3s ease;
}

.markdown-content :deep(a:hover) {
  color: #40a9ff;
  text-decoration: underline;
}

/* 表格样式 */
.markdown-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 0.8em 0;
  border: 1px solid #e8e8e8;
  display: block;
  overflow-x: auto;
}

.markdown-content :deep(thead) {
  background-color: #fafafa;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #e8e8e8;
  padding: 8px 12px;
  text-align: left;
}

.markdown-content :deep(th) {
  font-weight: 600;
  background-color: #fafafa;
}

.markdown-content :deep(table tr:nth-child(even)) {
  background: #fafafa;
}

/* 分隔线 */
.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid #e8e8e8;
  margin: 1em 0;
}

/* 图片样式 */
.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 0.5em 0;
}

/* 任务列表 */
.markdown-content :deep(input[type='checkbox']) {
  margin-right: 8px;
  cursor: pointer;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .markdown-content :deep(h1) {
    font-size: 24px;
  }

  .markdown-content :deep(h2) {
    font-size: 20px;
  }

  .markdown-content :deep(h3) {
    font-size: 18px;
  }

  .markdown-content :deep(pre) {
    padding: 12px;
    font-size: 12px;
  }
}
</style>

