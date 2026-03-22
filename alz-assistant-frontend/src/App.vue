<script setup lang="ts">
import { ConfigProvider } from 'ant-design-vue'
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'
import BasicLayout from '@/layouts/BasicLayout.vue'

const antdTheme: ThemeConfig = {
  token: {
    colorPrimary: '#0d9488',
    colorSuccess: '#16a34a',
    colorWarning: '#d97706',
    colorError: '#dc2626',
    borderRadius: 10,
    borderRadiusLG: 12,
    fontFamily:
      "'Outfit', -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif",
  },
}
</script>

<template>
  <ConfigProvider :theme="antdTheme">
    <BasicLayout />
  </ConfigProvider>
</template>

<style>
html {
  overflow-x: hidden;
}

body {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

/* 确保下拉菜单正常显示 */
.ant-select-dropdown,
.ant-dropdown {
  z-index: 1050 !important;
}

.ant-select-dropdown-menu {
  z-index: 1050 !important;
}
</style>
