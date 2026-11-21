/**
 * 收藏相关的API请求函数集合
 * 用于处理游戏收藏功能的前后端交互
 */
import request from '@/utils/request' // 导入请求工具模块

/**
 * 添加游戏到收藏夹的函数
 * @param {string|number} gameId - 要收藏的游戏ID
 * @returns {Promise} 返回一个Promise对象，包含请求的结果
 */
export function addFavorite(gameId) {
  return request.post(`/favorites/${gameId}`) // 发送POST请求到收藏接口，将游戏添加到收藏夹
}

/**
 * 取消收藏游戏
 * @param {number|string} gameId - 游戏ID，用于指定要取消收藏的游戏
 * @returns {Promise} 返回一个Promise对象，包含服务器对删除收藏请求的响应
 */
export function cancelFavorite(gameId) {
  // 发送DELETE请求到指定游戏ID的收藏接口
  return request.delete(`/favorites/${gameId}`)
}

/**
 * 检查指定游戏是否已被收藏
 * @param {string|number} gameId - 要检查的游戏ID
 * @returns {Promise} - 返回一个Promise对象，包含请求结果
 */
export function isFavorited(gameId) {
  return request.get(`/favorites/${gameId}/exists`) // 发送GET请求到收藏状态检查接口
}

/**
 * 获取收藏列表的函数
 * @param {Object} params - 查询参数对象
 * @returns {Promise} - 返回一个Promise对象，包含请求的结果
 */
export function listFavorites(params) {
  return request.get('/favorites', { params }) // 发送GET请求到/favorites接口，并传入params参数
}
