import request from '@/utils/request' // 从utils目录下导入request模块，用于发送HTTP请求

/** 前台可用平台 */
export function listPlatforms() {
  return request.get('/platforms')
}

/** 类型字典 */
export function listGameTypes() {
  return request.get('/game-types')
}
