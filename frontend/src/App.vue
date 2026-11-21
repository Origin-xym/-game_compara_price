<script setup>
/**
 * 导入Vue的生命周期钩子onMounted
 * 导入Vue Router的路由器useRouter
 * 导入用户状态管理store useUserStore
 */
import { onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useNoticeStore } from '@/stores/notice'


// 创建路由器实例
const router = useRouter()
// 创建用户状态管理实例
const userStore = useUserStore()
const noticeStore = useNoticeStore()
// 未读数轮询间隔：30 秒（配合后端判定周期，端到端最坏 ≈ 判定周期 + 轮询间隔）
const POLL_MS = 30000



/**
 * 组件挂载时的生命周期钩子
 * 检查用户是否已登录，如果已登录则加载用户信息
 */
onMounted(() => {
  if (userStore.isLogin) {
    userStore.loadMe()
    noticeStore.refresh()
    noticeStore.startPolling(POLL_MS)
  }
})

/**
 * 监听登录状态变化
 * 解决「App 已挂载、之后才在登录页登录」的场景：
 * 登录成功立刻拉未读并起轮询；登出立刻归零并停轮询
 */
watch(
  () => userStore.isLogin,
  (login) => {
    if (login) {
      noticeStore.refresh()
      noticeStore.startPolling(POLL_MS)
    } else {
      noticeStore.reset()
      noticeStore.stopPolling()
    }
  }
)



/**
 * 处理用户登出函数
 * 1. 调用用户store的登出方法
 * 2. 登出成功后重定向到游戏列表页面
 */
async function handleLogout() {
  await userStore.logout()
  router.replace('/games')
  noticeStore.reset()
  noticeStore.stopPolling()

}
</script>


<template>
  <!-- 应用根容器 -->
  <div class="app">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="inner">
        <!-- 导航链接，指向游戏比价页面 -->
        <router-link to="/games" class="logo">游戏比价</router-link>
        <router-link v-if="userStore.isAdmin" to="/admin/risks" class="nav-link">锁区维护</router-link>
        <!-- 网站标语 -->
        <!-- 用户区域显示组件 -->
        <span class="slogan">跨平台价格一目了然</span>  <!-- 平台宣传标语 -->
        <div class="spacer"></div>  <!-- 间距分隔元素 -->
        <!-- 导航区域组件，根据用户登录状态和管理员权限显示不同的导航链接 -->
        <div v-if="userStore.isLogin" class="nav">
          <router-link to="/favorites" class="nav-link">我的收藏</router-link>
          <router-link to="/alerts" class="nav-link">我的提醒</router-link>
                    <router-link to="/notifications" class="nav-link">
            通知
            <el-badge
              v-if="noticeStore.unread > 0"
              :value="noticeStore.unread"
              :max="99"
              class="nav-badge" />
          </router-link>
          <router-link v-if="userStore.isAdmin" to="/admin/games" class="nav-link">游戏管理</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin/prices" class="nav-link">价格审核</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin/platforms" class="nav-link">平台信息</router-link>


        </div>

        <!-- 用户登录状态区域，根据登录状态显示 -->
        <div v-if="userStore.isLogin" class="user-area">
          <span class="who">{{ userStore.displayName }}</span>  <!-- 显示用户昵称 -->
          <!-- 退出登录按钮 -->
          <el-button link type="primary" @click="handleLogout">退出</el-button>
        </div>
        <el-button v-else link type="primary" @click="router.push('/login')">登录 / 注册</el-button>

      </div>
    </header>

    <!-- 主要内容区域，用于显示路由组件 -->
    <main class="app-main">
      <router-view />
    </main>

    <!-- 页脚信息 -->
    <footer class="app-footer">数据来自官方与授权数据源，价格仅供参考</footer>
  </div>
</template>

<style>
/* 全局样式重置 */
* { box-sizing: border-box; }
body { margin: 0; background: #f7f8fa; font-family: -apple-system, "Segoe UI", "Microsoft YaHei", sans-serif; }
/* 顶部导航栏样式 */
.app-header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 10;
}
/* 内部容器样式 */
.inner {
  max-width: 1180px;
  margin: 0 auto;
  padding: 14px 20px;
  display: flex;
  align-items: baseline;
  gap: 14px;
}
/* Logo样式 */
.logo { font-size: 20px; font-weight: 700; color: #409eff; text-decoration: none; }
/* 标语样式 */
.slogan { font-size: 13px; color: #909399; }
/* 主内容区域样式 */
.app-main { max-width: 1180px; margin: 0 auto; padding: 0 20px; }
/* 页脚样式 */
.app-footer {
  text-align: center;
  color: #a8abb2;
  font-size: 12px;
  padding: 30px 0;
}
.inner { display: flex; align-items: center; gap: 14px; }
.spacer { flex: 1; }
.user-area { display: flex; align-items: center; gap: 10px; }
.who { font-size: 13px; color: #606266; }
.nav-link { font-size: 14px; color: #606266; text-decoration: none; display: inline-flex; align-items: center; gap: 4px; }
.nav { display: flex; gap: 16px; }
.nav-badge :deep(.el-badge__content) { transform: translateY(-1px); }
.nav-link { font-size: 14px; color: #606266; text-decoration: none; }
.nav-link:hover, .nav-link.router-link-active { color: #409eff; }
</style>
