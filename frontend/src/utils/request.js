/**
 * API请求模块
 * 使用axios创建HTTP请求实例，并配置拦截器处理认证和错误
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

// Token存储的键名常量
const TOKEN_KEY = 'gp_token'

const request = axios.create({ baseURL: '/api', timeout: 30000 })

/**
 * 重定向到登录页面的函数
 * 如果当前已经在登录页面，则不做任何操作
 * 否则将当前页面路径作为参数添加到登录页面URL中
 */
function redirectToLogin() {
  // 检查当前路径是否已经是登录页面，如果是则直接返回
  if (window.location.pathname === '/login') return
  // 获取当前路径和查询参数，组合成重定向URL
  const redirect = window.location.pathname + window.location.search
  // 将当前页面作为redirect参数编码后，重定向到登录页面
  window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`
}

// 请求拦截器 - 在发送请求前对请求进行统一处理
request.interceptors.request.use((config) => {
  // 从本地存储中获取token值
  const token = localStorage.getItem(TOKEN_KEY)
  // 如果存在token，则将其添加到请求头中
  if (token) config.headers.Authorization = `Bearer ${token}`
  // 返回处理后的配置
  return config
})

function handleBusinessError(body, fallbackMsg) {
  const err = new Error(body?.message || fallbackMsg)
  err.code = body?.code
  err.errors = body?.errors || null

  // 401xx 认证类：统一引导登录（AC-8.4 / AC-8.5）
  if (err.code >= 40100 && err.code < 40200) {
    localStorage.removeItem(TOKEN_KEY)
    redirectToLogin()
  }
  ElMessage.error(err.message)
  return err
}

/**
 * 响应拦截器，处理HTTP响应数据
 * 根据响应状态和数据结构进行统一处理
 */
request.interceptors.response.use(
  // 成功回调函数，当服务器返回2xx状态码时触发
  (response) => {
    const body = response.data  // 获取响应数据体
    // 检查响应数据是否存在且code为0（业务成功）
    if (body && body.code === 0) return body.data
    // 业务逻辑失败，返回拒绝的Promise，并处理业务错误
    return Promise.reject(handleBusinessError(body, '请求失败'))
  },
  // 错误回调函数，当服务器返回非2xx状态码或网络错误时触发
  (error) => {
    const body = error.response?.data  // 可选链获取错误响应数据体
    // 返回拒绝的Promise，并处理网络错误，包含状态码信息
    return Promise.reject(handleBusinessError(body, `网络异常（${error.response?.status || '无响应'}）`))
  }
)

export default request
