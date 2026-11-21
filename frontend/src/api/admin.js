/**
 * 游戏管理相关的API请求函数
 * 用于管理员对游戏进行增删改查操作
 */
import request from '@/utils/request'

/**
 * 获取游戏列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 返回游戏列表的Promise对象
 */
export function adminListGames(params) {
  return request.get('/admin/games', { params })
}

/**
 * 创建新游戏
 * @param {Object} data - 游戏数据
 * @returns {Promise} - 返回创建结果的Promise对象
 */
export function createGame(data) {
  return request.post('/admin/games', data)
}

export function updateGame(id, data) {
  return request.put(`/admin/games/${id}`, data)
}

export function changeGameStatus(id, status) {
  return request.put(`/admin/games/${id}/status`, null, { params: { status } })
}

/**
 * 获取指定游戏的管理员链接列表
 * @param {string|number} gameId - 游戏ID
 * @returns {Promise} 返回一个Promise对象，包含请求结果
 */
export function adminListLinks(gameId) {
  return request.get(`/admin/games/${gameId}/links`) // 发送GET请求获取指定游戏的链接列表
}

export function saveLink(gameId, platformId, data) {
  return request.put(`/admin/games/${gameId}/links/${platformId}`, data)
}

/**
 * 获取管理员风险列表
 * @param {string|number} platformId - 平台ID，用于筛选特定平台的风险数据
 * @returns {Promise} - 返回一个Promise对象，包含请求获取的风险列表数据
 */
export function adminListRisks(platformId) {
  return request.get('/admin/region-risks', { params: { platformId } }) // 发送GET请求到指定接口，携带platformId参数
}

/**
 * 更新或插入风险数据
 * @param {Object} data - 风险数据对象
 * @returns {Promise} - 返回一个Promise对象，包含请求的结果
 */
export function upsertRisk(data) {
  // 发送PUT请求到'/admin/region-risks'接口，传入data参数
  return request.put('/admin/region-risks', data)
}

/** 后台全量平台列表（含未就绪 / 禁用） */
export function adminListPlatforms() {
  return request.get('/admin/platforms')
}

export function createPlatform(data) {
  return request.post('/admin/platforms', data)
}

export function updatePlatform(id, data) {
  return request.put(`/admin/platforms/${id}`, data)
}

export function changePlatformStatus(id, status) {
  return request.put(`/admin/platforms/${id}/status`, null, { params: { status } })
}


