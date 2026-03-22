<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader class="fixed-header" />
    <!-- 主要内容区域：缓存主页，离开路由时对话与流式输出不销毁（与主流 AI 站点行为一致） -->
    <a-layout-content class="main-content">
      <router-view v-slot="{ Component }">
        <keep-alive include="HomePage">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </a-layout-content>
    <!-- 底部版权信息 -->
    <GlobalFooter class="fixed-footer" />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  background: none;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.main-content {
  width: 100%;
  padding: 0;
  background: none;
  margin: 0;
  margin-top: 64px;
  margin-bottom: 60px;
  flex: 1;
  min-height: calc(100vh - 124px);
}

.fixed-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}
</style>

