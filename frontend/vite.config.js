/**
 * Vite 配置文件
 * 用于配置项目的构建选项和开发服务器设置
 */
import { defineConfig } from 'vite' // 导入 Vite 的 defineConfig 函数
import vue from '@vitejs/plugin-vue' // 导入 Vue 插件
import { fileURLToPath, URL } from 'node:url' // 导入 Node.js 的 URL 处理函数

export default defineConfig({
  plugins: [vue()], // 使用 Vue 插件
  resolve: {
    alias: { // 配置路径别名
      '@': fileURLToPath(new URL('./src', import.meta.url)) // 将 @ 映射到 src 目录
    }
  },
  server: { // 开发服务器配置
    port: 5173, // 服务器端口号
    allowedHosts: true,   // 允许通过 tunnel 域名访问（Vite 5.4.12+ 默认会拦非本机 Host）
    proxy: { // 代理配置
      // 后端 context-path 是 /api，这里不做 rewrite
      '/api': { // 配置 /api 路径的代理
        target: 'http://localhost:8081', // 代理目标地址
        changeOrigin: true // 启用跨域
      }
    }
  }
})
