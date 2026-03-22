<template>
  <div class="auth-page">
    <div class="auth-shell">
      <aside class="auth-brand" aria-hidden="false">
        <RouterLink to="/" class="auth-logo-block">
          <img class="auth-logo-img" src="@/assets/logo_compressed.png" alt="" />
          <div class="auth-logo-text">
            <span class="auth-product">AlzAssistant</span>
            <span class="auth-sub">阿尔茨海默症智能问答</span>
          </div>
        </RouterLink>
        <p class="auth-lead">
          以温和、可信赖的方式，陪伴您了解认知健康与照护知识。
        </p>
        <ul class="auth-features">
          <li>
            <span class="auth-feature-icon" aria-hidden="true">
              <MessageOutlined />
            </span>
            <span>多轮对话，按需深入解答</span>
          </li>
          <li>
            <span class="auth-feature-icon" aria-hidden="true">
              <SafetyOutlined />
            </span>
            <span>知识库与量表工具辅助</span>
          </li>
          <li>
            <span class="auth-feature-icon" aria-hidden="true">
              <HeartOutlined />
            </span>
            <span>注重隐私，安心使用</span>
          </li>
        </ul>
        <div class="auth-footnote">登录即表示您了解本服务仅供信息参考，不能替代诊疗。</div>
      </aside>
      <main class="auth-main">
        <div class="auth-card">
          <slot />
        </div>
        <p class="auth-mobile-footnote">
          仅供健康信息参考，不能替代专业诊疗。
        </p>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { MessageOutlined, SafetyOutlined, HeartOutlined } from '@ant-design/icons-vue'
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 124px);
  display: flex;
  align-items: stretch;
  justify-content: center;
  padding: 24px 16px 32px;
  box-sizing: border-box;
}

.auth-shell {
  width: 100%;
  max-width: 1000px;
  display: grid;
  grid-template-columns: 1fr;
  gap: 0;
  align-items: stretch;
  border-radius: var(--app-radius-lg);
  overflow: hidden;
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  box-shadow: var(--app-shadow-md);
}

@media (min-width: 900px) {
  .auth-shell {
    grid-template-columns: minmax(280px, 1fr) minmax(360px, 1.05fr);
    min-height: 520px;
  }
}

.auth-brand {
  position: relative;
  padding: 40px 32px 36px;
  background: linear-gradient(
    160deg,
    var(--app-accent-soft) 0%,
    var(--app-surface-muted) 45%,
    var(--app-surface) 100%
  );
  border-bottom: 1px solid var(--app-border);
}

@media (min-width: 900px) {
  .auth-brand {
    border-bottom: none;
    border-right: 1px solid var(--app-border);
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
}

.auth-logo-block {
  display: flex;
  align-items: center;
  gap: 14px;
  text-decoration: none;
  color: inherit;
  margin-bottom: 20px;
  width: fit-content;
  border-radius: 12px;
  transition: opacity 0.2s ease;
}

.auth-logo-block:hover {
  opacity: 0.92;
  background: transparent;
}

.auth-logo-img {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  object-fit: cover;
  box-shadow: var(--app-shadow-sm);
  flex-shrink: 0;
}

.auth-logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.auth-product {
  font-family: var(--app-font-display);
  font-size: 1.35rem;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: var(--app-text);
}

.auth-sub {
  font-size: 13px;
  color: var(--app-text-muted);
}

.auth-lead {
  margin: 0 0 24px;
  font-size: 15px;
  line-height: 1.65;
  color: var(--app-text-secondary);
  max-width: 36ch;
}

.auth-features {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.auth-features li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 14px;
  line-height: 1.5;
  color: var(--app-text-secondary);
}

.auth-feature-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-accent-soft);
  color: var(--app-accent);
  font-size: 15px;
}

.auth-footnote {
  margin-top: auto;
  padding-top: 28px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--app-text-muted);
  max-width: 38ch;
}

.auth-main {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 36px 28px 32px;
  background: var(--app-surface);
}

@media (min-width: 900px) {
  .auth-main {
    padding: 48px 44px 44px;
  }
}

.auth-card {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.auth-mobile-footnote {
  display: block;
  margin: 20px auto 0;
  max-width: 400px;
  text-align: center;
  font-size: 12px;
  color: var(--app-text-muted);
  line-height: 1.45;
}

@media (min-width: 900px) {
  .auth-mobile-footnote {
    display: none;
  }
}
</style>
