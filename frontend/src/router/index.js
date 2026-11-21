/**
 * Vue Router 配置文件
 * 用于定义应用的路由规则和导航守卫
 */
import { createRouter, createWebHistory } from 'vue-router'

/**
 * 定义路由配置数组
 * 每个路由对象包含路径、名称和对应的组件
 */
const routes = [
  { path: '/', redirect: '/games' },  // 根路径重定向到游戏列表页
  { path: '/games', name: 'GameList', component: () => import('@/views/list/GameList.vue') },  // 游戏列表页面
  { path: '/login', name: 'Login', component: () => import('@/views/login/LoginView.vue') },  // 登录页面
  { path: '/favorites',name: 'Favorites',component: () => import('@/views/favorite/FavoriteList.vue'),meta: { requiresAuth: true }},
  { path: '/admin/games',name: 'AdminGames',component: () => import('@/views/admin/AdminGameList.vue'),meta: { requiresAuth: true }},
  { path: '/games/:id',name: 'GameDetail',component: () => import('@/views/detail/GameDetail.vue')},
  { path: '/admin/risks',name: 'AdminRisks',component: () => import('@/views/admin/AdminRiskList.vue'),meta: { requiresAuth: true }},
  { path: '/alerts', name: 'Alerts', component: () => import('@/views/alert/AlertList.vue'), meta: { requiresAuth: true } },
  { path: '/notifications', name: 'Notifications', component: () => import('@/views/notification/NotificationList.vue'), meta: { requiresAuth: true } },
  { path: '/admin/prices', name: 'AdminPrices', component: () => import('@/views/admin/AdminPriceReview.vue'), meta: { requiresAuth: true } },
  { path: '/admin/platforms', name: 'AdminPlatforms', component: () => import('@/views/admin/AdminPlatformList.vue'), meta: { requiresAuth: true } }

]

const router = createRouter({ history: createWebHistory(), routes })

// 预留守卫：给需要登录的路由加 meta.requiresAuth（收藏/提醒页下一步用）
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('gp_token')) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
