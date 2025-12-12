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
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  text-align: center;
  padding: 20px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
}

.copyright {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>

