<script setup>
// 从Vue框架中导入ref和onMounted函数
// ref用于创建响应式数据，onMounted用于在组件挂载后执行代码
import { ref, onMounted } from 'vue'
// 从Element Plus UI框架中导入消息提示和消息框组件
// ElMessage用于显示操作反馈的消息提示
// ElMessageBox用于显示确认对话框
import { ElMessage, ElMessageBox } from 'element-plus'
// 从API模块中导入告警相关的接口函数
// listAlerts用于获取告警列表
// changeAlertStatus用于修改告警状态
// deleteAlert用于删除告警
import { listAlerts, changeAlertStatus, deleteAlert } from '@/api/alert'

// 使用Vue 3的ref函数创建响应式变量
const loading = ref(false)  // loading状态，用于控制加载中的显示状态
const list = ref([])        // 列表数据，用于存储获取到的数据

/**
 * 异步获取列表数据
 * 该函数用于从API获取告警列表数据，并处理加载状态
 */
async function fetchList() {
  loading.value = true // 设置加载状态为true，表示开始加载数据
  try {
    // 调用listAlerts()获取列表数据，如果获取失败则设置为空数组
    list.value = (await listAlerts()) || []
  } finally {
    // 无论成功或失败，最终都将加载状态设为false
    loading.value = false
  }
}

/**
 * 切换状态函数
 * @param {Object} row - 包含状态信息的数据行对象
 * @returns {Promise<void>} 异步函数，没有返回值
 */
async function toggle(row) {
  // 根据当前状态计算目标状态
  // 如果当前状态为1，则目标状态为0；如果当前状态为0，则目标状态为1
  const target = row.status === 1 ? 0 : 1
  // 调用改变状态接口，传入数据ID和目标状态
  await changeAlertStatus(row.id, target)
  // 根据目标状态显示不同的成功提示信息
  // 目标状态为0时显示"已暂停"，目标状态为1时显示"已恢复"
  ElMessage.success(target === 0 ? '已暂停' : '已恢复')
  // 重新获取列表数据，刷新页面显示
  fetchList()
}

/**
 * 删除提醒的异步函数
 * @param {Object} row - 包含提醒信息的行对象，包含gameName和id等属性
 */
async function remove(row) {
  try {
    // 弹出确认对话框，提示用户确认删除操作
    // 删除后将不再收到该游戏的通知
    await ElMessageBox.confirm(`删除《${row.gameName}》的这条提醒？删除后不再收到通知。`, '删除提醒', {
      type: 'warning'  // 使用警告类型的图标
    })
  } catch (e) {
    // 如果用户取消操作，直接返回，不执行后续逻辑
    return
  }
  // 调用删除API，删除指定ID的提醒
  await deleteAlert(row.id)
  // 显示删除成功的消息提示
  ElMessage.success('已删除')
  // 重新获取提醒列表，更新界面数据
  fetchList()
}

// 组件挂载时，自动调用fetchList函数获取数据
onMounted(fetchList)
</script>

<template>
<!-- 提醒页面组件 -->
  <div class="alert-page">
  <!-- 工具栏区域 -->
    <div class="toolbar">
      <h2>我的提醒</h2>  <!-- 页面标题 -->
      <span class="count">共 {{ list.length }} 条</span>  <!-- 提醒总数统计 -->
    </div>

  <!-- 数据表格区域，带加载状态 -->
    <el-table v-loading="loading" :data="list" border stripe>
    <!-- 游戏名称列 -->
      <el-table-column label="游戏" min-width="150">
        <template #default="{ row }">
        <!-- 游戏名称链接，点击跳转到游戏详情页 -->
          <router-link :to="`/games/${row.gameId}`" class="game-link">{{ row.gameName }}</router-link>
        </template>
      </el-table-column>
    <!-- 平台列 -->
      <el-table-column prop="platformName" label="平台" width="120" />
    <!-- 类型列 -->
      <el-table-column prop="typeText" label="类型" width="110" />
    <!-- 条件/基准列 -->
      <el-table-column label="条件 / 基准" min-width="180">
        <template #default="{ row }">
        <!-- 如果有目标价格则显示目标价格，否则显示基准价格 -->
          <span v-if="row.targetPrice">目标 ¥{{ Number(row.targetPrice).toFixed(2) }}</span>
          <span v-else>基准 ¥{{ Number(row.baselinePrice).toFixed(2) }}</span>
        <!-- 基准时间 -->
          <div class="sub">基准时间：{{ row.baselineTime }}</div>
        </template>
      </el-table-column>
    <!-- 上次触发列 -->
      <el-table-column label="上次触发" width="170">
        <template #default="{ row }">
        <!-- 如果有上次触发价格则显示价格，否则显示未触发 -->
          <span v-if="row.lastFiredPrice">¥{{ Number(row.lastFiredPrice).toFixed(2) }}</span>
          <span v-else class="muted">未触发</span>
        <!-- 上次触发时间（如果有） -->
          <div v-if="row.lastFiredTime" class="sub">{{ row.lastFiredTime }}</div>
        </template>
      </el-table-column>
    <!-- 状态列 -->
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
        <!-- 根据状态显示不同颜色的标签 -->
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '暂停' }}
          </el-tag>
        </template>
      </el-table-column>
    <!-- 操作列 -->
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
        <!-- 切换状态按钮 -->
          <el-button link type="primary" @click="toggle(row)">
            {{ row.status === 1 ? '暂停' : '恢复' }}
          </el-button>
        <!-- 删除按钮 -->
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

  <!-- 空状态提示 -->
    <el-empty v-if="!loading && list.length === 0" description="还没有设置提醒，去游戏详情页点『提醒』试试" />
  </div>
</template>

<style scoped>
.alert-page { padding: 20px 0; }
.toolbar { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; }
.toolbar h2 { margin: 0; font-size: 20px; color: #303133; }
.count { color: #909399; font-size: 13px; }
.game-link { color: #409eff; text-decoration: none; }
.sub { font-size: 12px; color: #a8abb2; }
.muted { color: #c0c4cc; }
</style>
