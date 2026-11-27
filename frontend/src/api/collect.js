import request from '@/utils/request'

// 触发采集（AC-12.3 手动触发，便于验收）
// 采集是同步串行的外部请求：22 款游戏约需 10~15 秒，默认 10 秒超时会让前端提前报“网络异常（无响应）”，
// 所以这里单独把超时放宽到 3 分钟（后端跑完会正常返回，前端不再误判）
export function runCollect(params) {
  return request.post('/admin/collect/run', null, { params, timeout: 180000 })
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

