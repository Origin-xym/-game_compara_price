// 通知未读数状态：导航角标与通知中心共用同一个计数
// 导入必要的模块和函数
// 从pinia中导入defineStore，用于定义状态存储
import { defineStore } from 'pinia'
// 从vue中导入ref，用于创建响应式引用
import { ref } from 'vue'
// 从API模块中导入unreadCount函数，用于获取未读消息数量
import { unreadCount } from '@/api/notification'

/**
 * 通知状态管理store
 * 使用Pinia进行状态管理，处理通知相关的状态和操作
 */
export const useNoticeStore = defineStore('notice', () => {
  // 未读数量状态
  const unread = ref(0)
  // 轮询定时器句柄，用于定期刷新未读数量
  let timer = null

  /**
   * 拉取未读数
   * 未登录（无 token）直接归零，避免 401 抖动
   */
  async function refresh() {
    if (!localStorage.getItem('gp_token')) {
      unread.value = 0
      return
    }
    try {
      const data = await unreadCount()
      unread.value = Number(data?.count ?? 0)
    } catch (e) {
      unread.value = 0
    }
  }

  // 登出时清空
/**
 * 重置未读消息数量的函数
 * 将未读消息计数器重置为0
 */
  function reset() {  // 定义reset函数，用于重置状态
    unread.value = 0  // 将未读消息数量设置为0
  }

  /**
   * 轻量轮询，让角标跟随后台调度任务自动更新
   * 默认 60 秒一次；对本地单机项目开销可忽略
   */
/**
 * 启动轮询函数
 * @param {number} intervalMs - 轮询间隔时间，默认为60000毫秒（60秒）
 * @returns {void}
 */
  function startPolling(intervalMs = 60000) {
    stopPolling() // 在启动新的轮询前，先停止现有的轮询
    timer = setInterval(refresh, intervalMs) // 设置定时器，按照指定间隔调用refresh函数
  }

/**
 * 停止轮询功能的函数
 * 用于清除定时器，并将定时器变量重置为null
 */
  function stopPolling() {
  // 检查定时器变量是否存在
    if (timer) {
    // 清除已设置的定时器
      clearInterval(timer)
    // 将定时器变量重置为null
      timer = null
    }
  }

  return { unread, refresh, reset, startPolling, stopPolling }
})
