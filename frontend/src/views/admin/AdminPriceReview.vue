<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    runCollect, listCollectLogs, listPendingPrices, approvePrice, rejectPrice, createManualPrice
} from '@/api/collect'
import { adminListPlatforms, adminListGames } from '@/api/admin'


const tab = ref('pending')

// ---------------- 待确认队列 ----------------
const pendingLoading = ref(false)
const pendingList = ref([])
const pendingTotal = ref(0)
const pendingPage = ref(1)
const pendingSize = ref(20)

async function fetchPending() {
  pendingLoading.value = true
  try {
    const data = await listPendingPrices({ page: pendingPage.value, size: pendingSize.value })
    pendingList.value = data.list || []
    pendingTotal.value = data.total || 0
  } finally {
    pendingLoading.value = false
  }
}

async function approve(row) {
  try {
    await ElMessageBox.confirm(
      `通过后该价格将立即替换线上价：¥${Number(row.cnyAmount).toFixed(2)}（当前 ¥${Number(row.currentCny ?? 0).toFixed(2)}）`,
      '审核通过', { type: 'warning' }
    )
  } catch (e) {
    return
  }
  await approvePrice(row.id)
  ElMessage.success('已通过，线上价已更新')
  fetchPending()
}

async function reject(row) {
  let reason = ''
  try {
    const { value } = await ElMessageBox.prompt('驳回原因（可留空）', '驳回价格', {
      inputPlaceholder: '如：来源异常 / 数据可疑'
    })
    reason = value || ''
  } catch (e) {
    return
  }
  await rejectPrice(row.id, reason)
  ElMessage.success('已驳回，线上价保持不变')
  fetchPending()
}

function pendingPageChange(p) {
  pendingPage.value = p
  fetchPending()
}

// ---------------- 采集日志 ----------------
const logLoading = ref(false)
const logList = ref([])
const logTotal = ref(0)
const logPage = ref(1)
const logSize = ref(20)
const platforms = ref([])
const runForm = reactive({ platformId: null, scenario: 'NORMAL' })
const running = ref(false)

async function fetchLogs() {
  logLoading.value = true
  try {
    const data = await listCollectLogs({
      page: logPage.value,
      size: logSize.value,
      platformId: runForm.platformId || undefined
    })
    logList.value = data.list || []
    logTotal.value = data.total || 0
  } finally {
    logLoading.value = false
  }
}

async function doRun() {
  if (!runForm.platformId) {
    ElMessage.warning('请选择要采集的平台')
    return
  }
  running.value = true
  try {
    const r = await runCollect({ platformId: runForm.platformId, scenario: runForm.scenario })
    ElMessage.success(
      `采集完成：总数 ${r.total} / 成功 ${r.successCount} / 进队列 ${r.queueCount} / 跳过 ${r.skipCount} / 失败 ${r.failCount}`
    )
    logPage.value = 1
    fetchLogs()
    fetchPending()
  } finally {
    running.value = false
  }
}

function logPageChange(p) {
  logPage.value = p
  fetchLogs()
}

function statusTagType(status) {
  return { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[status] || 'info'
}

// ---------------- 手动录入价格（M12 手工维护一路） ----------------
const entryVisible = ref(false)
const entrySaving = ref(false)
const entryFormRef = ref(null)
const gameOptions = ref([])
const gameSearching = ref(false)

// 只有「有官方接口 + 数据源就绪 + 启用中」的平台能上架价格（后端 createManualPrice 会再拦一次）
const entryPlatforms = computed(() =>
  platforms.value.filter((p) => p.dataSourceType !== 3 && p.apiReady === 1 && p.enabled === 1)
)

const entryForm = reactive({
  gameId: null, platformId: null, currency: '',
  originalAmount: null, originalPrice: null,
  discountRate: null, discountEndTime: null, remark: ''
})
const entryRules = {
  gameId: [{ required: true, message: '请选择游戏', trigger: 'change' }],
  platformId: [{ required: true, message: '请选择平台', trigger: 'change' }],
  currency: [{ required: true, message: '请选择平台后自动带出币种', trigger: 'change' }],
  originalAmount: [{ required: true, message: '请填写现价', trigger: 'blur' }]
}

async function searchGames(kw) {
  gameSearching.value = true
  try {
    const data = await adminListGames({ page: 1, size: 20, keyword: kw || undefined })
    gameOptions.value = data.list || []
  } finally {
    gameSearching.value = false
  }
}

function openEntry() {
  entryVisible.value = true
  if (gameOptions.value.length === 0) searchGames('')
}

// 币种必须等于平台结算币种，选中即带出并锁定，避免手填不一致被后端拒
function onPlatformChange(pid) {
  const p = entryPlatforms.value.find((x) => x.id === pid)
  entryForm.currency = p ? p.currency : ''
}

function resetEntry() {
  entryFormRef.value?.resetFields()
  entryForm.currency = ''
}

async function submitEntry() {
  await entryFormRef.value.validate()
  try {
    await ElMessageBox.confirm(
      `该价格将直接生效（不进入待确认队列）：${entryForm.currency} ${entryForm.originalAmount}`,
      '确认录入', { type: 'warning' }
    )
  } catch (e) { return }
  entrySaving.value = true
  try {
    await createManualPrice({ ...entryForm })
    ElMessage.success('录入成功，该平台当前价已更新')
    entryVisible.value = false
    fetchPending()
  } finally {
    entrySaving.value = false
  }
}


onMounted(async () => {
  try {
    platforms.value = await adminListPlatforms()
  } catch (e) {
    platforms.value = []
  }
  fetchPending()
  fetchLogs()
})
</script>

<template>
  <div class="review-page">
        <div class="pagebar">
      <el-button type="primary" @click="openEntry">手动录入价格</el-button>
    </div>
    <el-tabs v-model="tab">
      <!-- 待确认队列（AC-12.6） -->
      <el-tab-pane :label="`待确认队列 ${pendingTotal}`" name="pending">
        <el-table v-loading="pendingLoading" :data="pendingList" border stripe>
          <el-table-column prop="gameName" label="游戏" min-width="140" />
          <el-table-column prop="platformName" label="平台" width="110" />
          <el-table-column label="采集价" width="130">
            <template #default="{ row }">
              <span class="new-price">¥{{ Number(row.cnyAmount).toFixed(2) }}</span>
              <div class="sub">{{ row.currency }} {{ Number(row.originalAmount).toFixed(2) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="当前线上价" width="130">
            <template #default="{ row }">
              <span v-if="row.currentCny">¥{{ Number(row.currentCny).toFixed(2) }}</span>
              <span v-else class="sub">无当前价</span>
              <div v-if="row.diffPercent != null" :class="['sub', Number(row.diffPercent) > 0 ? 'up' : 'down']">
                {{ Number(row.diffPercent) > 0 ? '+' : '' }}{{ row.diffPercent }}%
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="sourceRef" label="批次号" min-width="150" />
          <el-table-column prop="operator" label="操作人" min-width="150" />
          <el-table-column prop="recordedAt" label="采集时间" width="160" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="success" @click="approve(row)">通过</el-button>
              <el-button link type="danger" @click="reject(row)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!pendingLoading && pendingList.length === 0" description="没有待确认的数据" />
        <div v-if="pendingTotal > pendingSize" class="pager">
          <el-pagination layout="prev, pager, next" :total="pendingTotal"
                         :page-size="pendingSize" :current-page="pendingPage"
                         @current-change="pendingPageChange" />
        </div>
      </el-tab-pane>

      <!-- 采集任务日志（AC-12.3） -->
      <el-tab-pane label="采集日志" name="log">
        <div class="runbar">
          <el-select v-model="runForm.platformId" placeholder="选择平台" clearable style="width: 180px">
            <el-option v-for="p in platforms" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
          <el-select v-model="runForm.scenario" style="width: 200px">
            <el-option label="NORMAL 正常采集" value="NORMAL" />
            <el-option label="DROP 降价 10%" value="DROP" />
            <el-option label="SPIKE 剧烈波动" value="SPIKE" />
            <el-option label="PARSE_FAIL 解析失败" value="PARSE_FAIL" />
            <el-option label="EMPTY 空结果" value="EMPTY" />
          </el-select>
          <el-button type="primary" :loading="running" @click="doRun">立即采集</el-button>
        </div>

        <el-table v-loading="logLoading" :data="logList" border stripe>
          <el-table-column prop="batchNo" label="批次号" min-width="170" />
          <el-table-column prop="platformName" label="平台" width="100" />
          <el-table-column prop="startTime" label="开始" width="160" />
          <el-table-column prop="endTime" label="结束" width="160" />
          <el-table-column prop="total" label="总数" width="70" />
          <el-table-column prop="successCount" label="成功" width="70" />
          <el-table-column prop="failCount" label="失败" width="70" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="errorMsg" label="失败原因" min-width="220" show-overflow-tooltip />
        </el-table>
        <el-empty v-if="!logLoading && logList.length === 0" description="还没有采集记录" />
        <div v-if="logTotal > logSize" class="pager">
          <el-pagination layout="prev, pager, next" :total="logTotal"
                         :page-size="logSize" :current-page="logPage"
                         @current-change="logPageChange" />
        </div>
      </el-tab-pane>
    </el-tabs>
        <el-dialog v-model="entryVisible" title="手动录入价格" width="560px" @closed="resetEntry">
      <el-form ref="entryFormRef" :model="entryForm" :rules="entryRules" label-width="110px">
        <el-form-item label="游戏" prop="gameId">
          <el-select v-model="entryForm.gameId" filterable remote clearable
                     :remote-method="searchGames" :loading="gameSearching"
                     placeholder="输入游戏名搜索（留空显示前 20 条）" style="width: 100%">
            <el-option v-for="g in gameOptions" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="平台" prop="platformId">
          <el-select v-model="entryForm.platformId" clearable style="width: 100%"
                     @change="onPlatformChange">
            <el-option v-for="p in entryPlatforms" :key="p.id"
                       :label="`${p.name} / ${p.region}（${p.currency}）`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-input v-model="entryForm.currency" disabled placeholder="选择平台后自动带出并锁定" />
        </el-form-item>
                <el-form-item label="现价（实付价）" prop="originalAmount">
          <el-input-number v-model="entryForm.originalAmount" :min="0.01" :precision="2"
                           :controls="false" placeholder="用户实际支付的价格（原币）" style="width: 100%" />
        </el-form-item>
        <el-form-item label="原价（划线价）">
          <el-input-number v-model="entryForm.originalPrice" :min="0.01" :precision="2"
                           :controls="false" placeholder="只做折扣展示用；无折扣留空" style="width: 100%" />
        </el-form-item>
        <el-form-item label="折扣率 %">
          <el-input-number v-model="entryForm.discountRate" :min="0" :max="99.99" :precision="2" style="width: 200px" />
        </el-form-item>
        <el-form-item label="折扣截止">
          <el-date-picker v-model="entryForm.discountEndTime" type="datetime"
                          value-format="YYYY-MM-DDTHH:mm:ss"
                          :disabled-date="(d) => d.getTime() < Date.now()"
                          placeholder="填折扣率时建议填写" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="entryForm.remark" maxlength="200" show-word-limit
                    placeholder="写入 source_ref，如：人工核对 Xbox 官网价" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="entryVisible = false">取消</el-button>
        <el-button type="primary" :loading="entrySaving" @click="submitEntry">确定录入</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped>
.review-page { padding: 20px 0; }
.pagebar { margin-bottom: 12px; }
.runbar { display: flex; gap: 10px; margin-bottom: 14px; }
.new-price { color: #f56c6c; font-weight: 700; }
.sub { font-size: 12px; color: #909399; }
.up { color: #f56c6c; }
.down { color: #67c23a; }
.pager { display: flex; justify-content: center; margin-top: 14px; }
</style>
