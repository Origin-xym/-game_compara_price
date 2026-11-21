import request from '@/utils/request'

/** 游戏列表：page / size / keyword / typeIds / platformIds / priceMin / priceMax / discountOnly */
/**
 * 获取游戏列表
 * @param {Object} params - 查询参数对象
 * @returns {Promise} - 返回一个Promise，包含游戏列表数据
 */
export function listGames(params) {
  return request.get('/games', { params }) // 发送GET请求到/games接口，传入params参数
}

/** 游戏详情 */
/**
 * 获取游戏详情的函数
 * @param {string|number} id - 游戏的唯一标识符
 * @returns {Promise} - 返回一个Promise对象，包含游戏详情的响应数据
 */
export function getGameDetail(id) {
  return request.get(`/games/${id}`)  // 发送GET请求到指定游戏ID的详情接口
}

/** 价格走势：platformId 可选，days 默认 90 */
export function getPriceHistory(gameId, params) {
  return request.get(`/games/${gameId}/price-history`, { params })
}

/** 史低标注 */
/**
 * 获取指定游戏的最低历史记录
 * @param {string|number} gameId - 游戏ID，可以是字符串或数字类型
 * @returns {Promise} - 返回一个Promise对象，包含请求的响应数据
 */
export function getHistoryLows(gameId) {
  return request.get(`/games/${gameId}/history-lows`)
}

