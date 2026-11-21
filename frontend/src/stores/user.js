import { defineStore } from 'pinia'
import { login as apiLogin, register as apiRegister, logout as apiLogout, fetchMe } from '@/api/user'

const TOKEN_KEY = 'gp_token'

/**
 * 用户状态管理store
 * 使用Pinia进行状态管理，包含用户token、用户信息等状态
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    // 从localStorage中获取token，如果没有则为空字符串
    token: localStorage.getItem(TOKEN_KEY) || '',
    // 用户信息，初始为null
    user: null
  }),
  getters: {
    // 判断用户是否已登录
    isLogin: (s) => !!s.token,
    // 判断用户是否为管理员
    isAdmin: (s) => s.user?.role === 2,
    // 获取用户显示名称，优先使用昵称，其次使用用户名
    displayName: (s) => s.user?.nickname || s.user?.username || ''
  },
  actions: {
    // 设置token，同时更新localStorage
    setToken(token) {
      this.token = token || ''
      if (token) localStorage.setItem(TOKEN_KEY, token)
      else localStorage.removeItem(TOKEN_KEY)
    },
    // 用户登录
    async login(payload) {
      const data = await apiLogin(payload)
      this.setToken(data.token)
      this.user = data.user
      return data
    },
    // 用户注册
    async register(payload) {
      const data = await apiRegister(payload)
      this.setToken(data.token)
      this.user = data.user
      return data
    },
    // 加载当前用户信息
    async loadMe() {
      if (!this.token) return null
      try {
        this.user = await fetchMe()
        return this.user
      } catch (e) {
        // token 失效，清理本地状态
        this.setToken('')
        this.user = null
        return null
      }
    },
    // 用户登出
    async logout() {
      try {
        await apiLogout()
      } catch (e) {
        // 忽略：即使服务端失败也要清本地状态
      }
      this.setToken('')
      this.user = null
    }
  }
})
