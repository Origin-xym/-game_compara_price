/**
 * 通知相关API接口
 * 提供获取通知列表、未读数量、标记已读等功能
 */
import request from '@/utils/request'

/**
 * 获取通知列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 返回请求Promise对象
 */
export function listNotifications(params) {
  return request.get('/notifications', { params })
}

/**
 * 获取未读通知数量
 * @returns {Promise} - 返回请求Promise对象
 */
export function unreadCount() {
  return request.get('/notifications/unread-count')
}

/**
 * 将指定通知标记为已读
 * @param {String|Number} id - 通知ID
 * @returns {Promise} - 返回请求Promise对象
 */
export function markRead(id) {
  return request.put(`/notifications/${id}/read`)
}

/**
 * 将所有通知标记为已读
 * @returns {Promise} - 返回请求Promise对象
 */
export function markAllRead() {
  return request.put('/notifications/read-all')
}
