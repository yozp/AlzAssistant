<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        <a
          href="https://github.com/yozp/AlzAssistant"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          阿尔茨海默症智能问答系统 by @yunikon
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: var(--app-header-bg);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  text-align: center;
  padding: 16px 20px;
  border-top: 1px solid var(--app-border);
  box-shadow: 0 -1px 0 rgba(255, 255, 255, 0.4) inset;
}

.copyright {
  margin: 0;
  color: var(--app-text-muted);
  font-size: 13px;
}

.author-link {
  color: var(--app-text-secondary);
  transition: color 0.2s ease;
}

.author-link:hover {
  color: var(--app-accent);
  background: transparent;
}
</style>

