<script setup>
// 导入Vue 3的核心功能
import { ref, onMounted } from 'vue'
// 导入Vue Router的路由功能
import { useRouter } from 'vue-router'
// 导入Element Plus的消息提示组件
import { ElMessage } from 'element-plus'
// 导入通知相关的API接口函数
import { listNotifications, markRead, markAllRead } from '@/api/notification'
import { useNoticeStore } from '@/stores/notice'


// 引用Vue Router的use钩子函数，用于在组件内访问路由实例
const router = useRouter()
const noticeStore = useNoticeStore()
// 使用ref创建一个响应式变量loading，用于控制加载状态
const loading = ref(false)
// 使用ref创建一个响应式数组list，用于存储列表数据
const list = ref([])
// 使用ref创建一个响应式变量total，用于存储总数据量
const total = ref(0)
// 使用ref创建一个响应式变量page，用于存储当前页码
const page = ref(1)
// 使用ref创建一个响应式变量size，用于存储每页显示的数据条数
const size = ref(20)

/**
 * 异步获取通知列表的函数
 * 该函数用于从服务器获取分页的通知列表数据，并更新本地状态
 */
async function fetchList() {
  // 设置加载状态为true，表示开始加载数据
  loading.value = true
  try {
    const data = await listNotifications({ page: page.value, size: size.value })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 打开游戏详情的异步函数
 * @param {Object} item - 游戏对象，包含游戏相关信息
 */
async function open(item) {
  // 如果游戏未被阅读过
  if (item.isRead === 0) {
    // 调用标记已读的API，并等待其完成
    await markRead(item.id)
    // 更新游戏对象的阅读状态为已读
    item.isRead = 1
    noticeStore.refresh()

  }
  // AC-10.2：点击进详情 - 使用路由导航到游戏详情页
  router.push(`/games/${item.gameId}`)
}

/**
 * 读取所有内容并标记为已读的异步函数
 * 该函数会先标记所有内容为已读，然后显示成功消息，最后刷新列表
 */
async function readAll() {
  // 调用markAllRead函数标记所有内容为已读
  await markAllRead()
  // 显示操作成功的消息提示
  ElMessage.success('已全部标记为已读')
  // 调用fetchList函数刷新列表内容
  fetchList()
  noticeStore.refresh()

}

/**
 * 处理页面变更的函数
 * @param {number} p - 新的页码值
 * 当页码发生变化时，更新当前页码并重新获取列表数据
 */
function handlePageChange(p) {
  page.value = p  // 将传入的新页码赋值给page响应式变量
  fetchList()    // 调用获取列表数据的方法，刷新页面内容
}

onMounted(fetchList)
</script>

<template>
<!-- 通知中心页面组件 -->
  <div class="notice-page">
  <!-- 工具栏部分 -->
    <div class="toolbar">
      <h2>通知中心</h2>  <!-- 页面标题 -->
      <span class="count">共 {{ total }} 条</span>  <!-- 通知总数显示 -->
      <span v-if="noticeStore.unread > 0" class="unread-tip">未读 {{ noticeStore.unread }}</span>
      <div class="spacer"></div>  <!-- 占位元素，用于布局 -->
      <el-button @click="readAll">全部已读</el-button>  <!-- 全部已读按钮 -->
    </div>

  <!-- 通知列表加载状态和内容 -->
    <div v-loading="loading">
    <!-- 空状态提示 -->
      <el-empty v-if="!loading && list.length === 0" description="暂无通知" />

    <!-- 通知列表项 -->
      <div
        v-for="item in list"
        :key="item.id"
        :class="['notice-item', { unread: item.isRead === 0 }]"  
        @click="open(item)">  <!-- 点击事件处理 -->
      <!-- 通知封面图 -->
        <img v-if="item.coverUrl" :src="item.coverUrl" class="cover" :alt="item.gameName" />
        <div v-else class="cover placeholder">无图</div>  <!-- 无图时的占位符 -->

      <!-- 通知内容 -->
        <div class="content">
          <div class="title">
            <span class="dot" v-if="item.isRead === 0"></span>  <!-- 未读标识点 -->
            {{ item.title }}  <!-- 通知标题 -->
          </div>
          <div class="desc">
            {{ item.content }}  <!-- 通知内容描述 -->
          </div>
          <div class="meta">

          <!-- 通知元信息 -->
            <el-tag size="small" type="danger">{{ item.conditionText }}</el-tag>  <!-- 条件标签 -->
            <el-tag size="small" type="info">{{ item.platformName }}</el-tag>  <!-- 平台标签 -->
            <span class="time">{{ item.createTime }}</span>  <!-- 创建时间 -->
          </div>
        </div>
        <div class="price">¥{{ Number(item.priceCny).toFixed(2) }}</div>  <!-- 价格显示 -->
      </div>
    </div>

  <!-- 分页器 -->
    <div v-if="total > size" class="pager">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="handlePageChange" />  <!-- 页码切换事件处理 -->
    </div>
  </div>
</template>

<style scoped>
.notice-page { padding: 20px 0; }
.toolbar { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; }
.toolbar h2 { margin: 0; font-size: 20px; color: #303133; }
.count { color: #909399; font-size: 13px; }
.unread-tip { color: #f56c6c; font-size: 13px; font-weight: 600; }
.spacer { flex: 1; }
.notice-item {
  display: flex;
  gap: 14px;
  align-items: center;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 10px;
  cursor: pointer;
}
.notice-item.unread { border-left: 3px solid #f56c6c; }
.cover { width: 56px; height: 74px; object-fit: cover; border-radius: 4px; background: #f5f7fa; }
.placeholder { display: flex; align-items: center; justify-content: center; color: #c0c4cc; font-size: 12px; }
.content { flex: 1; }
.title { font-size: 15px; color: #303133; font-weight: 600; display: flex; align-items: center; gap: 6px; }
.dot { width: 7px; height: 7px; border-radius: 50%; background: #f56c6c; display: inline-block; }
.desc { font-size: 13px; color: #606266; margin: 4px 0 6px; }
.meta { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #909399; }
.price { color: #f56c6c; font-size: 18px; font-weight: 700; }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
