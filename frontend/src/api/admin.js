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
 * 按关键词搜索 Steam，返回可一键导入的候选列表
 * @param {string} keyword 游戏关键词
 */
export function searchSteamGames(keyword) {
  return request.get('/admin/games/steam-search', { params: { keyword } })
}

/**
 * 按 appId 一键导入游戏（自动带出名称/英文名/封面/简介/年份，并绑定 Steam 平台）
 * @param {Object} data { appId, gameTypeId?, platformId?, sortWeight? }
 */
export function importSteamGame(data) {
  return request.post('/admin/games/import-steam', data)
}

/**
 * 批量补齐 Epic 链接：后端逐款用浏览器搜 Epic 商店，命中才建链接。
 * 走浏览器渲染，20 款约 1~2 分钟，这里单独放宽超时到 5 分钟。
 */
export function backfillEpicLinks() {
  return request.post('/admin/games/epic-links/backfill', null, { timeout: 300000 })
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

// 删除平台（逻辑删除，历史数据保留）
export function deletePlatform(id) {
  return request.delete(`/admin/platforms/${id}`)
}




/** 全库校验/补齐游戏信息（中文名、英文名、发行日期）；force=true 时不一致直接覆盖 */
export function backfillGameMeta(force = false) {
  return request.post('/admin/games/meta/backfill', null, { params: { force }, timeout: 300000 })
}

/** 全库补齐小黑盒链接（按 Steam appId 生成，无匹配过程，毫秒级） */
export function backfillHeyboxLinks() {
  return request.post('/admin/games/heybox-links/backfill', null, { timeout: 60000 })
}
