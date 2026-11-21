/**
 * API请求模块 - 警报(Alerts)相关接口
 * 用于创建、查询、更新状态和删除警报信息
 */
import request from '@/utils/request'

/**
 * 创建新警报
 * @param {Object} data - 警报数据对象
 * @returns {Promise} - 返回创建警报的Promise对象
 */
export function createAlert(data) {
  return request.post('/alerts', data)
}

/**
 * 获取警报列表
 * @returns {Promise} - 返回获取警报列表的Promise对象
 */
export function listAlerts() {
  return request.get('/alerts')
}

export function changeAlertStatus(id, status) {
  return request.put(`/alerts/${id}/status`, null, { params: { status } })
}

/**
 * 删除警报的函数
 * @param {string|number} id - 要删除的警报的ID
 * @returns {Promise} 返回一个Promise对象，包含删除操作的响应结果
 */
export function deleteAlert(id) {
  return request.delete(`/alerts/${id}`)  // 发送DELETE请求到指定警报ID的端点
}
