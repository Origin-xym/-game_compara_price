<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getPriceHistory } from '@/api/game'

const props = defineProps({
  gameId: { type: [Number, String], required: true },
  platforms: { type: Array, default: () => [] }
})

const days = ref(90)
const loading = ref(false)
const trend = ref(null)
const chartRef = ref(null)
let chart = null

const dayOptions = [
  { label: '近 3 月', value: 90 },
  { label: '近 6 月', value: 180 },
  { label: '近 12 月', value: 365 }
]

// 折线配色（按平台顺序取，超过 7 个平台循环使用）
const COLORS = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9a60b4', '#36cfc9']

/** 只有真正有采样点的平台才画线 */
const seriesList = computed(() =>
  (trend.value?.platforms || []).filter((p) => (p.points?.length || 0) > 0)
)

/** 全部平台都只有 1 个点时，给出"仅显示采样点"的提示 */
const allInsufficient = computed(
  () => seriesList.value.length > 0 && seriesList.value.every((p) => p.insufficient)
)

async function fetchTrend() {
  loading.value = true
  try {
    // 一次取全部平台：不再按平台切换视图
    const data = await getPriceHistory(props.gameId, { days: days.value })
    trend.value = data
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

/** 统一管理实例：DOM 变了就重建，避免绑在已移除的节点上画图（白屏根因） */
function ensureChart() {
  if (!chartRef.value) return null
  if (!chart) {
    chart = echarts.init(chartRef.value)
  } else if (chart.getDom() !== chartRef.value) {
    chart.dispose()
    chart = echarts.init(chartRef.value)
  }
  return chart
}

function disposeChart() {
  if (chart) {
    chart.clear()
    chart.dispose()
    chart = null
  }
}

function renderChart() {
  const list = seriesList.value

  // 一个采样点都没有：清空画布，交给模板里的空状态
  if (!list.length) {
    disposeChart()
    return
  }

  const instance = ensureChart()
  if (!instance) return

  const series = list.map((p, i) => {
    const color = COLORS[i % COLORS.length]
    const points = p.points.map((x) => [x.recordedAt, Number(x.cnyAmount)])
    const low = p.historyLowCny != null ? Number(p.historyLowCny) : null
    const minPoint = Math.min(...points.map((x) => x[1]))
    // 数据自检：史低高于任一样本点时画出来会自相矛盾，宁可不画
    const showLowLine = low != null && low <= minPoint
    if (low != null && !showLowLine) {
      console.warn(`[trend] ${p.platformName} 史低 ¥${low} 高于样本最低 ¥${minPoint}，已跳过史低线`)
    }
    return {
      name: p.platformName,
      type: 'line',
      step: 'end',
      showSymbol: true,
      symbolSize: 7,
      data: points,
      itemStyle: { color },
      // 不足 2 个点的平台只显示圆点，不连线（避免"一条假直线"）
      lineStyle: p.insufficient ? { width: 0, color } : { width: 2, color },
      markLine: showLowLine
        ? {
            silent: true,
            symbol: 'none',
            label: {
              formatter: `${p.platformName} 史低 ¥${low.toFixed(2)}`,
              position: 'insideEndTop'
            },
            lineStyle: { type: 'dashed', color: '#f56c6c', opacity: 0.55 },
            data: [{ yAxis: low }]
          }
        : undefined
    }
  })

  instance.setOption(
    {
      color: COLORS,
      grid: { left: 56, right: 28, top: 40, bottom: 40 },
      legend: { top: 0, right: 0, type: 'scroll', icon: 'roundRect', itemHeight: 8 },
      tooltip: {
        trigger: 'axis',
        valueFormatter: (v) => (v == null ? '—' : `¥${Number(v).toFixed(2)}`)
      },
      xAxis: { type: 'time', boundaryGap: false, axisLabel: { formatter: '{MM}-{dd}' } },
      yAxis: { type: 'value', name: '人民币', axisLabel: { formatter: '¥{value}' } },
      series
    },
    true // notMerge：切区间/换游戏不残留上一条线
  )
  instance.resize()
}

function handleResize() {
  chart && chart.resize()
}

watch(days, () => fetchTrend())       // 区间变化 → 重新请求
watch(() => props.gameId, () => fetchTrend())

onMounted(() => {
  fetchTrend()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeChart()
})
</script>

<template>
  <div class="price-chart" v-loading="loading">
    <div class="chart-bar">
      <el-radio-group v-model="days" size="small">
        <el-radio-button v-for="o in dayOptions" :key="o.value" :value="o.value">
          {{ o.label }}
        </el-radio-button>
      </el-radio-group>

      <span class="legend-note">
        {{ seriesList.length }} 个平台有价格记录（一平台一条折线）
      </span>
      <span v-if="allInsufficient" class="legend-note warn">
        采样点不足 2 个的平台仅显示为圆点，采集积累后自动连线
      </span>
    </div>

    <div class="chart-wrap">
      <!-- canvas 常驻 DOM，避免摘除节点导致实例失效 -->
      <div ref="chartRef" class="chart-canvas"></div>

      <div v-if="!loading && !seriesList.length" class="chart-empty">
        <el-empty description="暂无价格记录：采集或录入价格后开始积累走势" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.price-chart { margin-top: 12px; }
.chart-bar { display: flex; align-items: center; gap: 14px; margin-bottom: 10px; flex-wrap: wrap; }
.legend-note { font-size: 12px; color: #a8abb2; }
.legend-note.warn { color: #e6a23c; }
.chart-wrap { position: relative; }
.chart-canvas { width: 100%; height: 320px; }
.chart-empty {
  position: absolute;
  inset: 0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
