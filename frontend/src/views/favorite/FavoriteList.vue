<script setup>
import { ref, onMounted } from 'vue'  // 从Vue框架中导入ref和onMounted函数
import { ElMessage, ElMessageBox } from 'element-plus'  // 从Element Plus导入消息提示和消息框组件
import { listFavorites, cancelFavorite } from '@/api/favorite'  // 从收藏夹API模块导入获取收藏列表和取消收藏的函数



// 使用ref创建响应式状态变量
const loading = ref(false)  // 控制加载状态的响应式变量
const list = ref([])  // 存储收藏列表数据的响应式变量
const total = ref(0)  // 存储收藏总数的响应式变量
const page = ref(1)  // 当前页码的响应式变量，默认为1
const size = ref(12)  // 每页显示条数的响应式变量，默认为12

/**
 * 异步获取收藏列表数据
 * 该函数用于从后端API获取收藏列表，并处理加载状态和数据更新
 */
async function fetchList() {
  // 设置加载状态为true，表示开始加载
  loading.value = true
  try {
    // 调用API获取收藏列表数据，传入当前页码和每页数量
    const data = await listFavorites({ page: page.value, size: size.value })
    // 更新列表数据，如果API返回的list为空则设为空数组
    list.value = data.list || []
    // 更新总数据量，如果API返回的total为0则设为0
    total.value = data.total || 0
  } catch (e) {
    // 发生错误时，清空列表数据并将总数设为0
    list.value = []
    total.value = 0
  } finally {
    // 无论成功或失败，最终都将加载状态设为false
    loading.value = false
  }
}

/**
 * 处理取消收藏的异步函数
 * @param {Object} item - 包含游戏信息的对象，至少包含name和gameId属性
 */
async function handleCancel(item) {
  try {
    // 使用Element Plus的消息框确认用户是否要取消收藏
    // 显示游戏名称，并设置自定义的确认和取消按钮文本
    await ElMessageBox.confirm(`确定取消收藏《${item.name}》吗？`, '取消收藏', {
      type: 'warning', // 警告类型图标
      confirmButtonText: '取消收藏', // 确认按钮文本
      cancelButtonText: '再想想' // 取消按钮文本
    })
  } catch (e) {
    // 如果用户点击取消或关闭对话框，则直接返回，不执行后续操作
    return
  }
  // 如果用户确认，则调用取消收藏的API
  await cancelFavorite(item.gameId)
  // 显示成功取消收藏的提示消息
  ElMessage.success('已取消收藏')
  // 如果当前页只有一条数据且不是第一页，则页码减1
  if (list.value.length === 1 && page.value > 1) page.value -= 1
  // 重新获取列表数据
  fetchList()
}

/**
 * 处理页面变化的函数
 * @param {number} p - 页码参数
 */
function handlePageChange(p) {
  page.value = p  // 更新当前页码
  fetchList()    // 重新获取列表数据
}

// 组件挂载时自动执行fetchList函数
onMounted(fetchList)
</script>

<template>
<!-- 收藏页面组件 -->
  <div class="fav-page">
  <!-- 顶部工具栏 -->
    <div class="toolbar">
      <h2>我的收藏</h2>
      <span class="count">共 {{ total }} 款</span>
    </div>

  <!-- 网格布局容器，包含加载状态 -->
    <div v-loading="loading" class="grid-wrap">
    <!-- 空状态提示 -->
      <el-empty v-if="!loading && list.length === 0" description="还没有收藏任何游戏">
        <el-button type="primary" plain @click="$router.push('/games')">去逛逛</el-button>
      </el-empty>

    <!-- 收藏游戏列表网格 -->
      <div v-else class="grid">
      <!-- 单个收藏游戏卡片 -->
        <div v-for="item in list" :key="item.favoriteId" class="card">
        <!-- 游戏封面链接 -->
          <router-link :to="`/games/${item.gameId}`">
            <img v-if="item.coverUrl" :src="item.coverUrl" class="cover" :alt="item.name" />
            <div v-else class="cover placeholder">暂无封面</div>
          </router-link>
        <!-- 卡片内容区域 -->
          <div class="body">
          <!-- 游戏名称 -->
            <div class="name">{{ item.name }}</div>
          <!-- 标签区域，包含折扣信息和平台标签 -->
            <div class="tags">
            <!-- 折扣标签 -->
              <el-tag v-if="item.discountActive" size="small" type="danger">
                -{{ Number(item.maxDiscountRate).toFixed(0) }}%
              </el-tag>
            <!-- 平台标签 -->
              <span v-for="p in item.platforms" :key="p.platformId" class="platform">{{ p.name }}</span>
            </div>
          <!-- 价格信息 -->
            <div class="price">
              <template v-if="item.hasQuote">
                <span class="symbol">¥</span>{{ Number(item.minPriceCny).toFixed(2) }}
              </template>
              <span v-else class="no-quote">暂无报价</span>
            </div>
          <!-- 底部操作区域 -->
            <div class="foot">
            <!-- 收藏时间 -->
              <span class="time">收藏于 {{ item.favoriteTime }}</span>
            <!-- 取消收藏按钮 -->
              <el-button link type="danger" @click="handleCancel(item)">取消收藏</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

  <!-- 分页器 -->
    <div v-if="total > size" class="pager">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="handlePageChange" />
    </div>
  </div>
</template>

<style scoped>
/* 收藏页面整体样式 */
.fav-page { padding: 20px 0; }
.toolbar { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; }
.toolbar h2 { margin: 0; font-size: 20px; color: #303133; }
.count { color: #909399; font-size: 13px; }
.grid-wrap { min-height: 320px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(190px, 1fr)); gap: 16px; }
.card { background: #fff; border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.cover { width: 100%; aspect-ratio: 3 / 4; object-fit: cover; display: block; background: #f5f7fa; }
.placeholder { display: flex; align-items: center; justify-content: center; color: #c0c4cc; font-size: 13px; }
.body { padding: 10px 12px 12px; }
.name { font-size: 15px; font-weight: 600; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.tags { display: flex; flex-wrap: wrap; gap: 6px; margin: 6px 0; min-height: 20px; align-items: center; }
.platform { font-size: 12px; color: #409eff; }
.price { color: #f56c6c; font-size: 18px; font-weight: 700; }
.symbol { font-size: 13px; }
.no-quote { color: #909399; font-size: 14px; font-weight: 400; }
.foot { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.time { font-size: 12px; color: #a8abb2; }
.pager { display: flex; justify-content: center; margin-top: 24px; }
</style>
