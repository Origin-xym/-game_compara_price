import request from '@/utils/request'

// 触发采集（AC-12.3 手动触发，便于验收）
export function runCollect(params) {
  return request.post('/admin/collect/run', null, { params })
}

// 采集日志
export function listCollectLogs(params) {
  return request.get('/admin/collect/logs', { params })
}

// 待确认队列（AC-12.6）
export function listPendingPrices(params) {
  return request.get('/admin/prices/pending', { params })
}

// 审核通过
export function approvePrice(id) {
  return request.post(`/admin/prices/${id}/approve`)
}

// 驳回
export function rejectPrice(id, reason) {
  return request.post(`/admin/prices/${id}/reject`, { reason })
}

// 手动录入价格（M12 手工维护一路，对应后端 POST /api/admin/prices）
export function createManualPrice(data) {
  return request.post('/admin/prices', data)
}

