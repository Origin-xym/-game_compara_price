<script setup>
// 从 Vue 中引入必要的响应式 API 和生命周期钩子
import { reactive, ref, onMounted } from 'vue'
// 引入 Element Plus 的消息提示组件
import { ElMessage } from 'element-plus'
// 引入游戏列表相关的 API
import { listGames } from '@/api/game'
// 引入平台和游戏类型相关的元数据 API
import { listPlatforms, listGameTypes } from '@/api/meta'
// 引入自定义组件
import GameCard from '@/components/GameCard.vue'
import SearchBox from '@/components/SearchBox.vue'
import FilterBar from '@/components/FilterBar.vue'
import ConditionTags from '@/components/ConditionTags.vue'



// 定义响应式变量
const loading = ref(false) // 加载状态
const list = ref([]) // 游戏列表数据
const total = ref(0) // 总游戏数量
const page = ref(1) // 当前页码
const size = ref(12) // 每页显示数量



// 平台和游戏类型数据
const platforms = ref([]) // 平台列表
const types = ref([]) // 游戏类型列表

// 唯一的条件来源：标签、请求参数都从这里派生
const filters = reactive({
  keyword: '',
  typeIds: [],
  platformIds: [],
  priceMin: null,
  priceMax: null,
  discountOnly: false
})

/** 当前生效的关键词（用于卡片高亮），与输入框同步 */
const activeKeyword = ref('')

/**
 * 构建查询参数对象
 * 该函数用于根据当前页面的状态和筛选条件构建API请求所需的参数
 * @returns {Object} 包含查询参数的对象
 */
function buildParams() {
  return {
    page: page.value, // 当前页码
    size: size.value, // 每页显示数量
    keyword: filters.keyword || undefined, // 关键词搜索条件，如果没有则设为undefined
    typeIds: filters.typeIds.length ? filters.typeIds.join(',') : undefined, // 类型ID列表，如果有多个则用逗号连接
    platformIds: filters.platformIds.length ? filters.platformIds.join(',') : undefined, // 平台ID列表，如果有多个则用逗号连接
    priceMin: filters.priceMin ?? undefined, // 最低价格，使用空值合并运算符，如果为null或undefined则设为undefined
    priceMax: filters.priceMax ?? undefined, // 最高价格，使用空值合并运算符，如果为null或undefined则设为undefined
    discountOnly: filters.discountOnly || undefined // 是否仅显示折扣商品，如果没有则设为undefined
  }
}

/**
 * 异步获取游戏列表数据
 * 该函数会处理加载状态、数据获取、错误处理等逻辑
 */
async function fetchList() {
  // 设置加载状态为true，表示开始加载
  loading.value = true
  try {
    // 调用listGames API获取数据，使用buildParams()构建请求参数
    const data = await listGames(buildParams())
    // 更新列表数据，如果data.list不存在则设为空数组
    list.value = data.list || []
    // 更新总数，如果data.total不存在则设为0
    total.value = data.total || 0
  } catch (e) {
    // 发生错误时，将列表和总数重置为初始值
    list.value = []
    total.value = 0
  } finally {
    // 无论成功或失败，最终都会将加载状态设为false
    loading.value = false
  }
}

/** 任何条件变化：回到第 1 页再查 */
/**
 * 应用筛选条件
 * 重置页码为第一页并获取列表数据
 */
function applyConditions() {
  page.value = 1  // 重置页码为1
  fetchList()    // 重新获取列表数据
}

/**
 * 处理搜索关键词
 * @param {string} kw - 搜索关键词
 */
function handleSearch(kw) {
  activeKeyword.value = kw  // 设置当前激活的关键词
  filters.keyword = kw       // 更新筛选条件中的关键词
  applyConditions()         // 应用新的筛选条件
}

/**
 * 处理页码变化
 * @param {number} p - 新的页码
 */
function handlePageChange(p) {
  page.value = p  // 更新当前页码
  fetchList()    // 重新获取列表数据
}

/**
 * 移除筛选标签
 * @param {Object} tag - 要移除的标签对象，包含kind和value属性
 */
function removeTag(tag) {
  switch (tag.kind) {
    case 'keyword':  // 处理关键词标签
      filters.keyword = ''      // 清空关键词筛选条件
      activeKeyword.value = ''  // 清空激活的关键词
      break
    case 'type':  // 处理类型标签
      // 从类型ID列表中移除指定的ID
      filters.typeIds = filters.typeIds.filter((id) => id !== tag.value)
      break
    case 'platform':  // 处理平台标签
      // 从平台ID列表中移除指定的ID
      filters.platformIds = filters.platformIds.filter((id) => id !== tag.value)
      break
    case 'priceMin':  // 处理最低价格标签
      filters.priceMin = null
      break
    case 'priceMax':  // 处理最高价格标签
      filters.priceMax = null  // 清空最高价格筛选条件
      break
    case 'discountOnly':  // 处理仅显示折扣标签
      filters.discountOnly = false  // 关闭仅显示折扣筛选条件
      break
  }
  applyConditions()  // 应用更新后的筛选条件
}

/**
 * 重置所有筛选条件
 * 该函数用于清空或重置页面中的所有筛选条件，并应用重置后的条件
 */
function resetAll() {
  filters.keyword = ''          // 重置关键词筛选为空字符串
  filters.typeIds = []          // 重置类型ID筛选为空数组
  filters.platformIds = []      // 重置平台ID筛选为空数组
  filters.priceMin = null       // 重置最低价格筛选为null
  filters.priceMax = null       // 重置最高价格筛选为null
  filters.discountOnly = false  // 重置仅折扣筛选为false
  activeKeyword.value = ''      // 重置当前激活关键词为空字符串
  applyConditions()             // 应用重置后的筛选条件
}

/**
 * 组件挂载时的生命周期钩子函数
 * 在组件挂载后执行异步操作，获取列表和筛选项数据
 */
onMounted(async () => {
  // 调用获取列表的函数
  fetchList()
  // 使用Promise.all并行获取平台列表和游戏类型列表
  try {
    // 使用解构赋值同时接收两个异步操作的结果
    const [p, t] = await Promise.all([listPlatforms(), listGameTypes()])
    // 更新平台列表数据，如果为空则设为空数组
    platforms.value = p || []
    // 更新游戏类型列表数据，如果为空则设为空数组
    types.value = t || []
  } catch (e) {
    // 捕获异常并显示警告提示信息
    ElMessage.warning('筛选项加载失败，可刷新重试')
  }
})
</script>

<template>
<!-- 列表页面容器 -->
  <div class="list-page">
  <!-- 顶部搜索栏 -->
    <div class="topbar">
    <!-- 搜索框组件，绑定filters中的keyword，监听搜索事件 -->
      <SearchBox v-model="filters.keyword" @search="handleSearch" />
    </div>

  <!-- 筛选栏组件，接收平台和类型数据，以及当前的筛选条件 -->
    <FilterBar
      :platforms="platforms"
      :types="types"
      :filters="filters"
      @change="applyConditions" />

  <!-- 条件标签组件，显示当前选中的筛选条件 -->
    <ConditionTags
      :filters="filters"
      :platforms="platforms"
      :types="types"
      @remove="removeTag"
      @reset="resetAll" />

  <!-- 工具栏，显示标题和总数 -->
    <div class="toolbar">
      <h2>全部游戏</h2>
      <span class="count">共 {{ total }} 款</span>
    </div>

  <!-- 网格容器，包含游戏列表和加载状态 -->
    <div v-loading="loading" class="grid-wrap">
    <!-- 空状态提示，当没有数据且不在加载中时显示 -->
      <el-empty
        v-if="!loading && list.length === 0"
        description="没有找到符合条件的游戏，试试减少筛选条件或换个关键词">
      <!-- 清除条件按钮 -->
        <el-button type="primary" plain @click="resetAll">清除条件</el-button>
      </el-empty>

    <!-- 游戏列表网格 -->
      <div v-else class="grid">
      <!-- 游戏卡片组件，遍历列表数据 -->
        <GameCard
          v-for="item in list"
          :key="item.id"
          :item="item"
          :keyword="activeKeyword" />
      </div>
    </div>

  <!-- 分页器，当总数大于每页显示数量时显示 -->
    <div v-if="total > size" class="pager">
    <!-- 分页组件，包含上一页、页码、下一页和总数显示 -->
      <el-pagination
        layout="prev, pager, next, total"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="handlePageChange" />
    </div>
  </div>
</template>

<style scoped>
.list-page { padding: 20px 0; }
.topbar { display: flex; gap: 12px; margin-bottom: 12px; }
.toolbar { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; }
.toolbar h2 { margin: 0; font-size: 20px; color: #303133; }
.count { color: #909399; font-size: 13px; }
.grid-wrap { min-height: 400px; }
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 16px;
}
.pager { display: flex; justify-content: center; margin-top: 24px; }
</style>
