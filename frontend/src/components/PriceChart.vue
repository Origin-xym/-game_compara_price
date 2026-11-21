<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getPriceHistory } from '@/api/game'

const props = defineProps({
  gameId: { type: [Number, String], required: true },
  platforms: { type: Array, default: () => [] }
})

/** null 表示「自动选平台」：取采样点最多的那个 */
const platformId = ref(null)
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

async function fetchTrend() {
  loading.value = true
  try {
    // 一次取全部平台，platformId 只做视图切换（避免切平台时的空白期）
    const data = await getPriceHistory(props.gameId, { days: days.value })
    trend.value = data

    // AC-5.1：首次进入自动选中「采样点最多」的平台，而不是价格最低的那个
    const list = data?.platforms || []
    if (!platformId.value || !list.some((p) => p.platformId === platformId.value)) {
      platformId.value = pickBestPlatform(list)?.platformId ?? null
    }

    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

/** 采样点最多优先；并列时优先有史低标注的 */
function pickBestPlatform(list) {
  if (!list.length) return null
  return [...list].sort((a, b) => {
    const diff = (b.points?.length || 0) - (a.points?.length || 0)
    if (diff !== 0) return diff
    return (b.historyLowCny != null ? 1 : 0) - (a.historyLowCny != null ? 1 : 0)
  })[0]
}

function currentSeries() {
  const list = trend.value?.platforms || []
  if (!list.length) return null
  if (!platformId.value) return pickBestPlatform(list)
  return list.find((p) => p.platformId === platformId.value) || pickBestPlatform(list)
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

function renderChart() {
  const series = currentSeries()

  // AC-5.4：采样不足不画曲线，只清空画布
  if (!series || series.insufficient) {
    if (chart) {
      chart.clear()
      chart.dispose()
      chart = null
    }
    return
  }

  const instance = ensureChart()
  if (!instance) return

  const points = series.points.map((p) => [p.recordedAt, Number(p.cnyAmount)])
  const low = series.historyLowCny != null ? Number(series.historyLowCny) : null
  const minPoint = Math.min(...series.points.map((p) => Number(p.cnyAmount)))

  // 数据自检：史低高于任一样本点时画出来会自相矛盾，宁可不画
  const showLowLine = low != null && low <= minPoint
  if (low != null && !showLowLine) {
    console.warn(`[trend] 史低 ¥${low} 高于样本最低 ¥${minPoint}，已跳过史低线，请校准数据`)
  }

  instance.setOption(
    {
      grid: { left: 56, right: 24, top: 34, bottom: 40 },
      tooltip: { trigger: 'axis', valueFormatter: (v) => `¥${Number(v).toFixed(2)}` },
      xAxis: { type: 'time', boundaryGap: false, axisLabel: { formatter: '{MM}-{dd}' } },
      yAxis: { type: 'value', name: '人民币', axisLabel: { formatter: '¥{value}' } },
      series: [
        {
          name: `${series.platformName} 价格`,
          type: 'line',
          showSymbol: true,
          step: 'end',
          data: points,
          lineStyle: { width: 2, color: '#409eff' },
          areaStyle: { opacity: 0.08, color: '#409eff' },
          markLine: showLowLine
            ? {
                silent: true,
                symbol: 'none',
                label: { formatter: `史低 ¥${low.toFixed(2)}`, position: 'insideEndTop' },
                lineStyle: { type: 'dashed', color: '#f56c6c' },
                data: [{ yAxis: low }]
              }
            : undefined
        }
      ]
    },
    true // notMerge：不残留上一维度（AC-5.5）
  )
  instance.resize()
}

function handleResize() {
  chart && chart.resize()
}

watch(days, () => fetchTrend())            // 区间变化 → 重新请求
watch(platformId, () => nextTick(renderChart))  // 平台变化 → 只重绘


onMounted(() => {
  fetchTrend()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart && chart.dispose()
  chart = null
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

      <el-select
        v-model="platformId"
        size="small"
        placeholder="选择平台"
        style="width: 180px">
        <el-option
          v-for="p in (trend?.platforms || [])"
          :key="p.platformId"
          :label="`${p.platformName}（${p.points.length} 点）`"
          :value="p.platformId" />
      </el-select>

      <span v-if="currentSeries()?.historyLowSource" class="low-source">
        史低来源：{{ currentSeries().historyLowSource }}
        <span v-if="currentSeries().historyLowDate"> · {{ currentSeries().historyLowDate }}</span>
      </span>
    </div>

    <div class="chart-wrap">
      <!-- canvas 常驻 DOM，避免摘除节点导致实例失效 -->
      <div ref="chartRef" class="chart-canvas"></div>

      <div v-if="currentSeries()?.insufficient" class="chart-empty">
        <el-empty description="数据积累中：该平台近期采样点不足 2 个，暂不绘制走势" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.price-chart { margin-top: 12px; }
.chart-bar { display: flex; align-items: center; gap: 14px; margin-bottom: 10px; flex-wrap: wrap; }
.low-source { font-size: 12px; color: #a8abb2; }
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
