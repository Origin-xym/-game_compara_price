<script setup>
import { ref, onMounted, computed, watch } from 'vue' // 从Vue框架中引入ref、onMounted、computed、watch等响应式API
import { useRoute } from 'vue-router' // 从Vue Router中引入useRoute，用于获取当前路由信息
import { ElMessage } from 'element-plus' // 从Element Plus中引入ElMessage，用于显示消息提示
import { getGameDetail } from '@/api/game' // 从游戏API模块中引入获取游戏详情的方法
import { addFavorite, cancelFavorite, isFavorited } from '@/api/favorite' // 从收藏API模块中引入相关方法
import { useUserStore } from '@/stores/user' // 从用户状态管理模块中引入用户store
import PriceChart from '@/components/PriceChart.vue'
import AlertDialog from '@/components/AlertDialog.vue'





const route = useRoute() // 创建路由实例，用于获取当前路由参数等信息
const userStore = useUserStore() // 创建用户状态管理实例

const loading = ref(false) // 创建加载状态响应式变量，用于控制加载中的UI显示
const detail = ref(null) // 创建游戏详情响应式变量，用于存储游戏详细信息
const favorited = ref(false) // 创建收藏状态响应式变量，用于标记当前游戏是否已被收藏
const alertVisible = ref(false) // 提醒弹窗开关


// 定义风险等级文本映射对象，将风险数字转换为对应的文本描述
const riskText = { 1: '低风险', 2: '中风险', 3: '高风险' }
// 定义风险等级类型映射对象，将风险数字对应到Element Plus中不同的消息类型
const riskType = { 1: 'warning', 2: 'warning', 3: 'error' }

/**
 * 异步获取游戏详情的函数
 * 该函数会处理加载状态、获取游戏详情以及检查用户收藏状态
 */
async function fetchDetail() {
  loading.value = true  // 设置加载状态为true，表示开始加载
  try {
    // 调用API获取游戏详情，并将结果存储在detail中
    detail.value = await getGameDetail(route.params.id)
    // 检查用户是否已登录
    if (userStore.isLogin) {
      try {
        // 如果已登录，则获取该游戏的收藏状态
        const res = await isFavorited(route.params.id)
        // 更新收藏状态
        favorited.value = res.favorited
      } catch (e) { /* 忽略 */ }
    }
  } finally {
    loading.value = false
  }
}

/**
 * 切换收藏状态的异步函数
 * 根据当前登录状态和收藏状态，执行相应的收藏或取消收藏操作
 */
async function toggleFavorite() {
  // 检查用户是否登录，未登录则提示并返回
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再收藏')
    return
  }
  try {
    // 如果已收藏，则执行取消收藏操作
    if (favorited.value) {
      await cancelFavorite(route.params.id)
      favorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      // 如果未收藏，则执行添加收藏操作
      await addFavorite(route.params.id)
      favorited.value = true
      ElMessage.success('已收藏')
    }
  } catch (e) { /* 已提示 */ }
}

function openShop(row) {
  // AC-7.1：新标签打开，不覆盖当前页
  window.open(row.shopUrl, '_blank', 'noopener')
}

/**
 * 操作列不可购买时的原因文案。
 * 优先级：高风险 > 链接缺失（AC-6.3 要求提示与「能否购买」不矛盾，
 * 高风险是业务结论，应优先于技术性的「链接不可用」）
 */
function unavailableReason(row) {
  if (row.riskLevel >= 3) return '该区域不建议购买'
  return '链接暂不可用'
}


/**
 * 格式化折扣文本
 * @param {Object} row - 包含折扣信息的行数据对象
 * @returns {string} 返回格式化后的折扣文本，若折扣未激活或折扣率为空则返回空字符串
 */
function discountText(row) {
  // 判断折扣是否激活或折扣率是否存在，若不满足条件则返回空字符串
  if (!row.discountActive || row.discountRate == null) return ''
  // 将折扣率转换为数字并保留整数部分，返回带有百分号的折扣文本
  return `-${Number(row.discountRate).toFixed(0)}%`
}

// 史低取值：本站史低（price_record 真实最低，随采集/录入自动更新）优先，官方史低兜底
function lowValue(row) {
  return row.lowestEverCny != null ? row.lowestEverCny : row.historyLowCny
}

// 史低发生时间：本站史低时间优先
function lowDate(row) {
  return row.lowestEverAt || row.historyLowDate
}

// 是否已到历史最低（用于“已到史低”标记，不参与最低价计算）
function atLow(row) {
  const low = lowValue(row)
  return row.cnyAmount != null && low != null && Number(low) >= Number(row.cnyAmount)
}

// 底部史低汇总：只列有史低的平台（占位卡/无数据的平台不出现）
const lowSummary = computed(() =>
  (detail.value?.platforms || [])
    .map((p) => ({
      platformId: p.platformId,
      platformName: p.platformName,
      low: lowValue(p),
      at: lowDate(p)
    }))
    .filter((x) => x.low != null)
)

// 划线原价折成人民币：有 originalPrice 用划线价，否则退回实付价；rate 为入库固化汇率
function originCny(row) {
  const amt = row.originalPrice != null ? Number(row.originalPrice) : Number(row.originalAmount)
  return (amt * (Number(row.rate) || 0)).toFixed(2)
}


const cheapest = computed(() => detail.value?.platforms?.find((p) => p.lowest))
// 挂载后拉取详情：少了这一行，detail 永远为 null，详情页只会显示"游戏不存在或已下架"
onMounted(fetchDetail)

// 详情页内跳到另一款游戏时重新拉取，否则只换 URL 不换内容
watch(() => route.params.id, (id) => {
  if (id) fetchDetail()
})

</script>

<template>
<!--
  游戏详情页面组件
  包含游戏封面、基本信息、价格对比等多功能展示
-->
  <div v-loading="loading" class="detail-page">
  <!-- 加载状态和空状态处理 -->
    <el-empty v-if="!loading && !detail" description="游戏不存在或已下架" />

  <!-- 游戏详情主要内容 -->
    <template v-else-if="detail">
    <!-- 游戏头部信息区域 -->
      <div class="head">
      <!-- 游戏封面展示 -->
        <img v-if="detail.coverUrl" :src="detail.coverUrl" class="cover" :alt="detail.name" />
        <div v-else class="cover placeholder">暂无封面</div>

      <!-- 游戏详细信息 -->
        <div class="info">
          <h1>{{ detail.name }}</h1> <!-- 游戏中文名 -->
          <p class="sub">{{ detail.nameEn }}</p> <!-- 游戏英文名 -->
        <!-- 游戏标签展示 -->
          <div class="tags">
            <el-tag
              v-for="t in (detail.gameTypeNames && detail.gameTypeNames.length ? detail.gameTypeNames : (detail.gameTypeName ? [detail.gameTypeName] : []))"
              :key="t"
              type="info"
              style="margin-right: 4px">{{ t }}</el-tag>
            <el-tag v-if="detail.releaseYear">{{ detail.releaseYear }} 年</el-tag>
            <el-tag v-if="detail.platformCount">{{ detail.platformCount }} 个平台有价</el-tag>
          </div>

        <!-- 价格信息展示 -->
          <div class="price-block">
            <template v-if="detail.hasQuote">
              <span class="label">最低</span>
              <span class="symbol">¥</span>
              <span class="price">{{ Number(detail.minPriceCny).toFixed(2) }}</span>
              <span class="cheapest" v-if="cheapest">（{{ cheapest.platformName }}）</span>
            </template>
            <span v-else class="no-quote">暂无报价</span>
          </div>

        <!-- 操作按钮和信息 -->
          <div class="actions">
            <el-button
              :type="favorited ? 'warning' : 'default'"
              @click="toggleFavorite">
              {{ favorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button @click="alertVisible = true">提醒</el-button>
            <AlertDialog
              v-model="alertVisible"
              :game-id="detail.id"
              :game-name="detail.name"
              :current-min="detail.minPriceCny"
              :platforms="detail.platforms.map(p => ({ platformId: p.platformId, platformName: p.platformName }))" />
            <span class="update" v-if="detail.lastUpdateTime">最后更新：{{ detail.lastUpdateTime }}</span>
          </div>
        </div>
      </div>

    <!-- 游戏简介 -->
      <p v-if="detail.summary" class="summary">{{ detail.summary }}</p>

    <!-- 多平台价格对比区域 -->
      <h2 class="section-title">多平台价格对比</h2>

    <!-- 无报价提示 -->
      <el-empty v-if="!detail.platforms || detail.platforms.length === 0" description="该游戏在各平台上暂无报价" />

    <!-- 平台价格对比表格 -->
      <div v-else class="plist">
        <div v-for="row in detail.platforms" :key="row.platformId"
             class="pcard" :class="{ 'is-lowest': row.lowest }">

          <!-- 平台 -->
          <div>
            <div class="pname">
              {{ row.platformName }}
              <el-tag v-if="row.lowest" size="small" type="danger" effect="dark">最低价</el-tag>
            </div>
            <div class="ptags">
              <el-tag v-if="row.stale" size="small" type="info">可能已过期</el-tag>
              <el-tag v-if="atLow(row)" size="small" type="success">已到史低</el-tag>
              <el-tag v-if="lowValue(row) != null" size="small" type="success" effect="plain">
                史低 ¥{{ Number(lowValue(row)).toFixed(2) }}
              </el-tag>
            </div>
          </div>

          <!-- 价格 -->
          <div>
            <template v-if="row.hasPrice !== false">
              <div class="price-line">
                <span class="now">¥{{ Number(row.cnyAmount).toFixed(2) }}</span>
                <span v-if="row.discountActive" class="off">{{ discountText(row) }}</span>
              </div>
              <div v-if="row.discountActive" class="price-sub">
                <span class="strike">原价 ¥{{ originCny(row) }}</span>
              </div>
              <div class="price-sub">
                <span v-if="row.discountEndTime">折扣截至 {{ row.discountEndTime }}</span>
                <span v-else-if="!row.discountActive">无折扣</span>
                <span class="time">{{ row.recordedAt }}</span>
              </div>
            </template>
            <template v-else>
              <div class="price-line">
                <span class="now pending">暂无报价</span>
              </div>
              <div class="price-sub">
                <span>该平台价格待接入，录入后自动更新</span>
              </div>
            </template>
          </div>
          <!-- 风险 -->
          <div>
            <div v-if="row.riskLevel >= 1" class="risk" :class="'r' + row.riskLevel">
              <span class="dot"></span>
              <span>{{ riskText[row.riskLevel] }}<span v-if="row.riskReason"> · {{ row.riskReason }}</span></span>
            </div>
            <div v-else class="risk none"><span class="dot"></span><span>无锁区风险</span></div>
            <div v-if="row.riskLevel >= 1 && row.riskSource" class="src">依据：{{ row.riskSource }}</div>
          </div>

          <!-- 操作 -->
          <div class="pact">
            <el-button v-if="row.purchasable" type="primary" size="small" @click="openShop(row)">前往商店</el-button>
            <span v-else class="no-link">{{ row.hasPrice === false ? '暂无链接' : unavailableReason(row) }}</span>
          </div>
        </div>
      </div>
            <template v-if="detail.hasQuote">
        <h2 class="section-title">价格走势</h2>
        <PriceChart
          :game-id="detail.id"
          :platforms="detail.platforms.map(p => ({
            platformId: p.platformId,
            platformName: p.platformName
          }))" />

        <div class="low-summary">
          <el-tag
            v-for="p in lowSummary"
            :key="p.platformId"
            type="info">
            史低 · {{ p.platformName }} ¥{{ Number(p.low).toFixed(2) }}
          </el-tag>
          <span class="low-note">史低仅作标注，不参与最低价计算</span>
        </div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.detail-page { padding: 24px 0; min-height: 500px; }
.head { display: flex; gap: 24px; }
.cover { width: 320px; aspect-ratio: 460 / 215; object-fit: cover; object-position: center; border-radius: 8px; background: #f5f7fa; }
.placeholder { display: flex; align-items: center; justify-content: center; color: #c0c4cc; }
.info { flex: 1; }
.info h1 { margin: 0; font-size: 24px; color: #303133; }
.sub { margin: 4px 0 10px; color: #909399; font-size: 13px; }
.tags { display: flex; gap: 8px; margin-bottom: 14px; }
.price-block { display: flex; align-items: baseline; gap: 4px; margin-bottom: 14px; }
.label { font-size: 12px; color: #909399; }
.symbol { color: #f56c6c; font-size: 16px; }
.price { color: #f56c6c; font-size: 28px; font-weight: 700; }
.cheapest { font-size: 12px; color: #909399; }
.no-quote { color: #909399; font-size: 18px; }
.actions { display: flex; align-items: center; gap: 14px; }
.update { font-size: 12px; color: #a8abb2; }
.summary { margin: 20px 0; color: #606266; font-size: 14px; line-height: 1.7; }
.section-title { font-size: 18px; color: #303133; margin: 24px 0 12px; }
.platform-name { font-weight: 600; color: #303133; }
.lowest-tag { margin-left: 6px; }
.region { font-size: 12px; color: #a8abb2; }
.cny { font-size: 18px; font-weight: 700; color: #303133; }
.cny.lowest { color: #f56c6c; }
.origin { font-size: 12px; color: #909399; }
.end { font-size: 12px; color: #909399; }
.stale { margin-left: 6px; }
.muted { color: #c0c4cc; font-size: 13px; }
.risk-source { font-size: 12px; color: #a8abb2; margin-top: 4px; }
.no-link { font-size: 12px; color: #f56c6c; }
.low-summary { display: flex; align-items: center; gap: 10px; margin-top: 14px; flex-wrap: wrap; }
.low-note { font-size: 12px; color: #a8abb2; }
.at-low { margin-left: 6px; }
/* ---- 平台价格卡片（浅色简洁版） ---- */
.plist { display: flex; flex-direction: column; gap: 12px; }
.pcard {
  display: grid; grid-template-columns: 150px 220px minmax(0, 1fr) 110px;
  align-items: center; gap: 24px;
  padding: 16px 20px; background: #fff;
  border: 1px solid #ebeef5; border-radius: 8px;
}

.pcard.is-lowest { border-color: #f56c6c; background: #fffafa; }
.pname { display: flex; align-items: center; gap: 6px; font-size: 15px; font-weight: 600; color: #303133; }
.pregion { margin-top: 2px; font-size: 12px; color: #a8abb2; }
.ptags { display: flex; gap: 6px; margin-top: 6px; }

.price-line { display: flex; align-items: baseline; gap: 8px; }
.now { font-size: 26px; font-weight: 700; color: #f56c6c; line-height: 1.1; }
.now.pending { font-size: 20px; font-weight: 600; color: #909399; }
.off { padding: 1px 6px; font-size: 12px; font-weight: 600; color: #f56c6c;
       background: #fef0f0; border: 1px solid #fde2e2; border-radius: 4px; }
.price-sub { display: flex; gap: 10px; margin-top: 4px; font-size: 12px; color: #909399; }
.strike { color: #c0c4cc; text-decoration: line-through; }
.time { color: #c0c4cc; }

.risk { display: flex; align-items: flex-start; gap: 6px; font-size: 12px; line-height: 1.6; }
.risk .dot { flex: none; width: 6px; height: 6px; margin-top: 6px; border-radius: 50%; background: currentColor; }
.risk.r1, .risk.r2 { color: #e6a23c; }
.risk.r3 { color: #f56c6c; }
.risk.none { color: #a8abb2; }
.src { margin-top: 2px; font-size: 11px; color: #a8abb2; }
.pact { display: flex; justify-content: flex-end; }
.no-link { font-size: 12px; color: #f56c6c; text-align: right; }
</style>
