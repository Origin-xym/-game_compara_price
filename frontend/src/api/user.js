/**
 * 用户认证相关API接口
 * 包含注册、登录、登出和获取当前用户信息等功能
 */
import request from '@/utils/request' // 导入请求工具类

/**
 * 用户注册功能
 * @param {Object} data - 注册所需的数据对象
 * @returns {Promise} - 返回一个Promise对象，包含服务器响应结果
 */
export function register(data) {
  return request.post('/auth/register', data) // 发送POST请求到注册接口，并传入数据
}

/**
 * 登录函数
 * @param {Object} data - 登录所需的数据对象
 * @returns {Promise} - 返回一个Promise对象，包含登录请求的响应结果
 */
export function login(data) {
  return request.post('/auth/login', data) // 发送POST请求到登录接口，传入登录数据
}

/**
 * 登出函数
 * 该函数用于处理用户登出请求，通过调用后端的登出接口来实现用户登出功能
 * @returns {Promise} 返回一个Promise对象，包含后端接口的响应结果
 */
export function logout() {
  // 发送POST请求到'/auth/logout'接口进行用户登出操作
  return request.post('/auth/logout')
}

export function fetchMe() {
  return request.get('/auth/me')
}
