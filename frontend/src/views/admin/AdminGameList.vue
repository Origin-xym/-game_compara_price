<script setup>
import { reactive, ref, onMounted } from 'vue'  // 从Vue中引入reactive、ref和onMounted等响应式API
import { ElMessage, ElMessageBox } from 'element-plus'  // 从Element Plus引入消息提示和消息框组件
import { adminListGames, createGame, updateGame, changeGameStatus, searchSteamGames, importSteamGame, backfillEpicLinks, backfillGameMeta, backfillHeyboxLinks, adminListGameTypes, createGameType, updateGameType, deleteGameType } from '@/api/admin'  // 从admin API模块引入游戏管理相关接口
import { listGameTypes } from '@/api/meta'  // 从meta API模块引入游戏类型列表接口
import { useUserStore } from '@/stores/user'  // 引入用户状态管理store
// 从 @/api/admin 模块中导入 adminListLinks 和 saveLink 两个函数
// adminListLinks: 可能用于获取管理员链接列表的函数
// saveLink: 可能用于保存链接信息的函数
import { adminListLinks, saveLink } from '@/api/admin'


// 创建用户状态管理store实例
const userStore = useUserStore()



// 列表相关响应式变量
const loading = ref(false)  // 控制列表加载状态
const list = ref([])  // 游戏列表数据
const total = ref(0)  // 总数据条数
const page = ref(1)  // 当前页码
const size = ref(10)  // 每页显示条数
const keyword = ref('')  // 搜索关键词
const types = ref([])  // 游戏类型列表



// 弹窗相关响应式变量
const dialogVisible = ref(false)  // 控制弹窗显示状态
const saving = ref(false)  // 控制保存状态
const editingId = ref(null)  // 当前编辑的游戏ID
// 使用reactive创建一个响应式表单对象
const form = reactive({
  name: '',              // 游戏名称
  nameEn: '',            // 游戏英文名称
  coverUrl: '',          // 游戏封面图片URL
  summary: '',           // 游戏简介
  typeIds: [],           // 游戏标签（多选）
  gameTypeId: null,      // 兼容：主类型 = typeIds[0]
  releaseYear: null,     // 发布年份
  sortWeight: 0          // 排序权重
})
// 使用ref创建一个响应式对象，用于存储表单字段的错误信息
const fieldErrors = ref({})

/**
 * 异步获取游戏列表数据
 * 该函数用于从后端API获取游戏列表，并处理加载状态和数据更新
 */
async function fetchList() {
  // 设置加载状态为true，表示开始加载数据
  loading.value = true
  try {
    // 调用adminListGames API获取游戏列表数据
    // 参数包括当前页码、每页大小和搜索关键词
    const data = await adminListGames({
      page: page.value,       // 当前页码
      size: size.value,       // 每页显示数量
      keyword: keyword.value || undefined  // 搜索关键词，如果没有则为undefined
    })
    // 更新列表数据，如果API返回的list为空则设为空数组
    list.value = data.list || []
    // 更新总数据量，如果API返回的total为0则设为0
    total.value = data.total || 0
  } finally {
    // 无论成功或失败，都将加载状态设为false
    loading.value = false
  }
}

/**
 * 打开创建对话框并初始化表单数据
 * 重置编辑ID、字段错误，并设置默认表单值
 */
function openCreate() {
  // 重置编辑ID为null，表示当前是新建操作而非编辑
  editingId.value = null
  // 清空字段错误信息对象
  fieldErrors.value = {}
  // 重置表单数据，设置默认值
  Object.assign(form, {
    name: '', nameEn: '', coverUrl: '', summary: '',
    typeIds: types.value[0] ? [types.value[0].id] : [],
    gameTypeId: types.value[0]?.id ?? null, releaseYear: null, sortWeight: 0
  })
  dialogVisible.value = true
}

/**
 * 打开编辑对话框并初始化表单数据
 * @param {Object} row - 包含要编辑的游戏数据的行对象
 */
function openEdit(row) {
  // 设置当前编辑的游戏ID
  editingId.value = row.id
  // 清空字段错误信息
  fieldErrors.value = {}
  // 将行数据复制到表单对象中，用于编辑
  Object.assign(form, {
    name: row.name,           // 游戏名称
    nameEn: row.nameEn,       // 游戏英文名称
    coverUrl: row.coverUrl,   // 游戏封面图片URL
    summary: row.summary,     // 游戏简介
    typeIds: row.gameTypeIds && row.gameTypeIds.length ? [...row.gameTypeIds] : (row.gameTypeId ? [row.gameTypeId] : []),
    gameTypeId: row.gameTypeId, // 兼容字段
    releaseYear: row.releaseYear, // 游戏发行年份
    sortWeight: row.sortWeight    // 游戏排序权重
  })
  // 显示编辑对话框
  dialogVisible.value = true
}

/**
 * 提交表单数据的主函数
 * 根据当前是编辑模式还是新增模式调用不同的API
 * 处理保存过程中的错误和状态更新
 */
async function submit() {
  // 多标签至少要选一个
  if (!form.typeIds || form.typeIds.length === 0) {
    fieldErrors.value = { typeIds: '至少选择一个标签' }
    ElMessage.warning('至少选择一个标签')
    return
  }
  // 设置保存状态为true，表示正在保存
  saving.value = true
  // 清空字段错误信息
  fieldErrors.value = {}
  try {
    // 创建表单数据的副本，避免直接修改原始数据
    const payload = { ...form }
    // 主类型 = 第一个标签（后端 game_type_id 兼容字段）
    payload.gameTypeId = form.typeIds[0]
    payload.typeIds = [...form.typeIds]
    // 检查是否存在编辑ID，判断是更新还是新增操作
    if (editingId.value) {
      // 如果存在编辑ID，则调用更新API
      await updateGame(editingId.value, payload)
      // 显示保存成功提示
      ElMessage.success('保存成功')
    } else {
      // 如果不存在编辑ID，则调用新增API
      await createGame(payload)
      // 显示新增成功提示
      ElMessage.success('新增成功')
    }
    // 关闭对话框
    dialogVisible.value = false
    // 刷新列表数据
    fetchList()
  } catch (e) {
    // 如果错误信息中包含字段错误，则更新字段错误信息
    if (e.errors) fieldErrors.value = e.errors
  } finally {
    // 无论成功或失败，最终都将保存状态重置为false
    saving.value = false
  }
}

/**
 * 切换游戏状态（上架/下架）的异步函数
 * @param {Object} row - 包含游戏信息的行对象
 */
async function toggleStatus(row) {
  // 根据当前状态确定目标状态（1变0或0变1）
  const target = row.status === 1 ? 0 : 1
  // 根据目标状态确定操作类型（上架或下架）
  const action = target === 0 ? '下架' : '上架'
  try {
    // AC-11.3 二次确认：弹出确认对话框，让用户确认操作
    await ElMessageBox.confirm(
      `确定${action}《${row.name}》吗？${target === 0 ? '下架后前台不再展示，但历史数据保留。' : ''}`,
      `${action}确认`,
      { type: 'warning' }
    )
  } catch (e) {
    // 如果用户取消操作，直接返回
    return
  }
  // 调用API更改游戏状态
  await changeGameStatus(row.id, target)
  // 显示操作成功提示
  ElMessage.success(`已${action}`)
  // 刷新列表数据
  fetchList()
}

/**
 * 处理页码变更事件
 * @param {number} p - 新的页码值
 */
function handlePageChange(p) {
  page.value = p  // 更新当前页码
  fetchList()    // 重新获取列表数据
}

/**
 * 执行搜索操作
 * 重置页码为第一页，并重新获取列表数据
 */
function doSearch() {
  page.value = 1  // 将页码重置为1
  fetchList()    // 重新获取列表数据
}

/* ---------- 平台链接维护（M7） ---------- */
// 使用Vue的ref函数创建响应式变量
const linksVisible = ref(false)  // 控制链接列表的显示状态，默认为隐藏
const linksLoading = ref(false)  // 控制链接列表的加载状态，默认为未加载
const links = ref([])            // 存储链接列表数据的响应式数组，默认为空数组
const linksGame = ref(null)      // 存储与链接相关的游戏信息，默认为null

/**
 * 打开链接管理弹窗并获取链接列表
 * @param {Object} row - 包含游戏ID等信息的行数据
 */
async function openLinks(row) {
  linksGame.value = row  // 设置当前游戏数据
  linksVisible.value = true  // 显示链接管理弹窗
  linksLoading.value = true  // 开始加载状态
  try {
    // 获取指定游戏的链接列表，如果获取失败则设为空数组
    links.value = (await adminListLinks(row.id)) || []
  } finally {
    linksLoading.value = false  // 无论成功失败都结束加载状态
  }
}

/**
 * 保存单条链接数据
 * @param {Object} row - 包含平台ID、外部ID等信息的行数据
 */
async function saveLinkRow(row) {
  // 调用保存接口，传入游戏ID、平台ID和链接相关信息
  await saveLink(linksGame.value.id, row.platformId, {
    externalId: row.externalId,  // 外部平台ID
    storeUrl: row.storeUrl,      // 商店链接
    listed: row.listed          // 是否已上架
  })
  // 显示保存成功的提示信息
  ElMessage.success(`已保存 ${row.platformName}`)
}


/* ---------- Steam 搜索导入 ---------- */
const importVisible = ref(false)      // 导入弹窗
const importKeyword = ref('')         // 搜索关键词
const importLoading = ref(false)      // 搜索中
const importResults = ref([])         // 候选列表
const importingAppId = ref(null)      // 正在导入的 appId
const importTypeId = ref(null)        // 手动指定类型；为空则用候选项推荐类型

/* ---------- 批量补齐 Epic 链接 ---------- */
const epicLoading = ref(false)        // 补齐中（走浏览器，较慢）
const epicResults = ref([])           // 补齐结果逐条明细
const epicResultVisible = ref(false)  // 结果弹窗

/** 打开导入弹窗 */
function openImport() {
  importVisible.value = true
  importKeyword.value = ''
  importResults.value = []
  importTypeId.value = null
}

/** 搜索 Steam */
async function doImportSearch() {
  const kw = importKeyword.value.trim()
  if (!kw) {
    ElMessage.warning('请输入游戏关键词')
    return
  }
  importLoading.value = true
  try {
    importResults.value = (await searchSteamGames(kw)) || []
    if (importResults.value.length === 0) ElMessage.info('没搜到可导入的游戏，换个关键词试试')
  } finally {
    importLoading.value = false
  }
}

/** 选中某个候选 → 一键导入 */
async function doImport(candidate) {
  importingAppId.value = candidate.appId
  try {
    const gameId = await importSteamGame({
      appId: candidate.appId,
      gameTypeId: importTypeId.value || candidate.suggestedTypeId || null
    })
    ElMessage.success(`已导入《${candidate.name}》（ID=${gameId}），点一次“立即采集”即可抓取价格`)
    candidate.exists = true
    candidate.existingGameId = gameId
    fetchList()
  } catch (e) {
    if (e.errors) fieldErrors.value = e.errors
  } finally {
    importingAppId.value = null
  }
}

/**
 * 批量补齐 Epic 链接
 * 后端逐款用本机浏览器搜 Epic 商店，命中才建链接；没上架的游戏会在结果弹窗里列出来
 */
async function doBackfillEpic() {
  epicLoading.value = true
  try {
    const list = (await backfillEpicLinks()) || []
    epicResults.value = list
    epicResultVisible.value = true
    const added = list.filter(r => r.status === 'NEW').length
    const exists = list.filter(r => r.status === 'EXISTS').length
    const notFound = list.filter(r => r.status === 'NOT_FOUND').length
    ElMessage.success(`Epic 补齐完成：新增 ${added} 条 · 已有 ${exists} 条 · 未上架 ${notFound} 条`)
    fetchList()
  } finally {
    epicLoading.value = false
  }
}

/* ---------- 批量补齐小黑盒链接（不用浏览器，按 Steam appId 直接拼 URL） ---------- */
const heyboxLoading = ref(false)
const heyboxResults = ref([])
const heyboxResultVisible = ref(false)

/**
 * 一块补齐小黑盒链接
 * 小黑盒游戏页 URL = https://www.xiaoheihe.cn/app/topic/game/pc/{Steam appId}
 */
async function doBackfillHeybox() {
  heyboxLoading.value = true
  try {
    const list = (await backfillHeyboxLinks()) || []
    heyboxResults.value = list
    heyboxResultVisible.value = true
    const added = list.filter(r => r.status === 'NEW').length
    const exists = list.filter(r => r.status === 'EXISTS').length
    const noSteam = list.filter(r => r.status === 'NOT_FOUND').length
    ElMessage.success(`小黑盒补齐完成：新增 ${added} 条 · 已有 ${exists} 条 · 无 Steam 链接 ${noSteam} 条`)
    fetchList()
  } finally {
    heyboxLoading.value = false
  }
}

/* ---------- 批量校验/补齐游戏信息（中文名、英文名、发行日期） ---------- */
const metaLoading = ref(false)
const metaForce = ref(false)          // 勾选后：与 Steam 不一致时直接覆盖
const metaResults = ref([])
const metaResultVisible = ref(false)

/**
 * 全库校验游戏信息
 * 后端逐款调 Steam appdetails：空字段自动补，不一致只报告（勾选「强制覆盖」才改写）
 */
async function doBackfillMeta() {
  metaLoading.value = true
  try {
    const list = (await backfillGameMeta(metaForce.value)) || []
    metaResults.value = list
    metaResultVisible.value = true
    const filled = list.filter(r => r.status === 'FILLED').length
    const mismatch = list.filter(r => r.status === 'MISMATCH').length
    const noSteam = list.filter(r => r.status === 'NO_STEAM').length
    ElMessage.success(`校验完成：补齐 ${filled} 款 · 不一致 ${mismatch} 款 · 无 Steam 链接 ${noSteam} 款`)
    fetchList()
  } finally {
    metaLoading.value = false
  }
}

/* ---------- 类型（标签）管理 ---------- */
const typeMgrVisible = ref(false)
const typeRows = ref([])
const typeSaving = ref(false)
const typeForm = reactive({ id: null, code: '', name: '', sortNo: 99 })

/** 标签 id → 名称（列表列展示用） */
function typeName(id) {
  const t = types.value.find((x) => x.id === id)
  return t ? t.name : `#${id}`
}

/** 重新拉一遍前台类型字典（增删改后同步筛选下拉） */
async function reloadTypes() {
  types.value = (await listGameTypes()) || []
}

async function loadTypeRows() {
  typeRows.value = (await adminListGameTypes()) || []
}

function openTypeMgr() {
  resetTypeForm()
  loadTypeRows()
  typeMgrVisible.value = true
}

function resetTypeForm() {
  typeForm.id = null
  typeForm.code = ''
  typeForm.name = ''
  typeForm.sortNo = 99
}

function editTypeRow(row) {
  typeForm.id = row.id
  typeForm.code = row.code
  typeForm.name = row.name
  typeForm.sortNo = row.sortNo ?? 99
}

/** 新增或改名 */
async function saveType() {
  if (!typeForm.code.trim() || !typeForm.name.trim()) {
    ElMessage.warning('编码和名称都要填')
    return
  }
  typeSaving.value = true
  try {
    const payload = { code: typeForm.code.trim(), name: typeForm.name.trim(), sortNo: typeForm.sortNo }
    if (typeForm.id) {
      await updateGameType(typeForm.id, payload)
      ElMessage.success('标签已更新')
    } else {
      await createGameType(payload)
      ElMessage.success('标签已新增')
    }
    resetTypeForm()
    await loadTypeRows()
    await reloadTypes()
  } finally {
    typeSaving.value = false
  }
}

/** 删除标签（仍有游戏在用会被后端拒绝） */
async function removeType(row) {
  try {
    await ElMessageBox.confirm(
      `删除标签「${row.name}」？仍有 ${row.usedCount ?? 0} 个游戏在用时会拒绝。`,
      '删除标签',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  } catch {
    return
  }
  await deleteGameType(row.id)
  ElMessage.success('已删除')
  await loadTypeRows()
  await reloadTypes()
}

/**
 * 组件挂载时的生命周期钩子
 * 初始化列表数据和游戏类型数据
 */
onMounted(async () => {
  if (!userStore.user) await userStore.loadMe()
  if (!userStore.isAdmin) return  // 检查用户是否为管理员
  fetchList()                    // 获取列表数据
  types.value = (await listGameTypes()) || []  // 获取游戏类型列表并设置默认值为空数组
})
</script>

<template>
<!-- 游戏管理页面组件 -->
  <div class="admin-page">
  <!-- 权限提示：当用户不是管理员时显示错误提示 -->
    <el-alert
      v-if="!userStore.isAdmin"
      type="error"
      :closable="false"
      title="无权访问：该页面仅管理员可用"
      description="后端 /admin/** 会直接拒绝非管理员请求（AC-11.5）" />

  <!-- 管理员可见内容 -->
    <template v-else>
    <!-- 工具栏区域 -->
      <div class="toolbar">
        <h2>游戏管理</h2>
      <!-- 搜索框：支持按游戏名称搜索 -->
        <el-input
          v-model="keyword"
          placeholder="按名称搜索"
          clearable
          style="width: 220px"
          @keyup.enter="doSearch"
          @clear="doSearch" />
      <!-- 搜索按钮 -->
        <el-button type="primary" @click="doSearch">搜索</el-button>
      <!-- 间隔元素 -->
        <div class="spacer"></div>
      <!-- 新增游戏按钮 -->
        <el-button type="success" @click="openImport">Steam 搜索导入</el-button>
        <el-button type="warning" :loading="epicLoading" @click="doBackfillEpic">补齐 Epic 链接</el-button>
        <el-button type="warning" plain :loading="heyboxLoading" @click="doBackfillHeybox">补齐小黑盒链接</el-button>
        <el-button type="info" :loading="metaLoading" @click="doBackfillMeta">校验游戏信息</el-button>
        <el-button type="primary" @click="openCreate">新增游戏</el-button>
        <el-button @click="openTypeMgr">类型管理</el-button>
      </div>

    <!-- 游戏列表表格 -->
      <el-table v-loading="loading" :data="list" border stripe>
      <!-- ID列 -->
        <el-table-column prop="id" label="ID" width="70" />
      <!-- 游戏名称列 -->
        <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
      <!-- 英文名称列 -->
        <el-table-column prop="nameEn" label="英文名" min-width="160" show-overflow-tooltip />
      <!-- 游戏类型ID列 -->
        <el-table-column label="标签" min-width="170">
          <template #default="{ row }">
            <el-tag
              v-for="tid in (row.gameTypeIds && row.gameTypeIds.length ? row.gameTypeIds : (row.gameTypeId ? [row.gameTypeId] : []))"
              :key="tid"
              size="small"
              type="info"
              style="margin-right: 4px">
              {{ typeName(tid) }}
            </el-tag>
          </template>
        </el-table-column>
      <!-- 发行年份列 -->
        <el-table-column prop="releaseYear" label="年份" width="80" />
      <!-- 排序权重列 -->
        <el-table-column prop="sortWeight" label="权重" width="80" />
      <!-- 状态列：显示上架/下架状态 -->
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
      <!-- 操作列：编辑和状态切换按钮 -->
        <el-table-column label="操作" width="240" fixed="right">
          
          <template #default="{ row }">
          <!-- 编辑按钮 -->
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openLinks(row)">链接</el-button>

          <!-- 状态切换按钮：根据当前状态显示下架或上架 -->
            <el-button
              link
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

    <!-- 分页组件 -->
      <div class="pager">
        <el-pagination
          layout="prev, pager, next, total"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="handlePageChange" />
      </div>
    </template>

  <!-- Steam 搜索导入对话框 -->
    <el-dialog v-model="importVisible" title="从 Steam 导入游戏" width="760px">
      <div class="imp-search">
        <el-input
          v-model="importKeyword"
          placeholder="输入游戏名，如 赛博朋克 / Elden Ring / Civilization"
          clearable
          @keyup.enter="doImportSearch" />
        <el-button type="primary" :loading="importLoading" @click="doImportSearch">搜索</el-button>
      </div>
      <div class="imp-tip">
        选中结果点“添加”，名称、英文名、封面、简介、发行年份会自动写入，并自动绑定 Steam 平台链接。价格在导入后由“立即采集”或每日 04:00 定时任务抓取。
      </div>

      <div v-loading="importLoading" class="imp-list">
        <el-empty
          v-if="!importLoading && importResults.length === 0"
          description="输入关键词后点搜索，或换个关键词再试"
          :image-size="60" />
        <div v-for="c in importResults" :key="c.appId" class="imp-item">
          <img v-if="c.coverUrl" :src="c.coverUrl" class="imp-cover" :alt="c.name" />
          <div v-else class="imp-cover imp-cover-empty">无封面</div>
          <div class="imp-info">
            <div class="imp-name">
              {{ c.name }}
              <span v-if="c.nameEn" class="imp-en">{{ c.nameEn }}</span>
            </div>
            <div class="imp-sub">
              <span v-if="c.releaseYear">{{ c.releaseYear }} 年</span>
              <span v-if="c.suggestedTypeName" class="imp-type-tag">{{ c.suggestedTypeName }}</span>
              <span v-if="c.genres && c.genres.length" class="imp-genres">{{ c.genres.join(' / ') }}</span>
              <span v-if="c.priceCny !== null && c.priceCny !== undefined" class="imp-price">
                ¥{{ Number(c.priceCny).toFixed(2) }}
                <em v-if="c.discountRate">-{{ c.discountRate }}%</em>
              </span>
              <span v-else class="imp-noprice">暂无人民币价格</span>
            </div>
            <div v-if="c.summary" class="imp-summary">{{ c.summary }}</div>
          </div>
          <div class="imp-action">
            <el-tag v-if="c.exists" type="info">已存在</el-tag>
            <el-button
              v-else
              type="primary"
              size="small"
              :loading="importingAppId === c.appId"
              @click="doImport(c)">添加</el-button>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="imp-footer">
          <span class="imp-footer-label">导入类型</span>
          <el-select v-model="importTypeId" size="small" clearable placeholder="自动（按 Steam 分类）" style="width: 190px">
            <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <div class="spacer"></div>
          <el-button @click="importVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 补齐 Epic 链接结果 -->
    <el-dialog v-model="epicResultVisible" title="Epic 链接补齐结果" width="720px">
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="规则：用本机浏览器搜 Epic 商店，标题与游戏同名才建链接；没搜到的通常是 Epic 未上架或国区不发售。" />
      <el-table :data="epicResults" border stripe size="small" style="margin-top: 12px" max-height="420">
        <el-table-column prop="gameName" label="游戏" min-width="150" show-overflow-tooltip />
        <el-table-column label="结果" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'NEW'" type="success">已新增</el-tag>
            <el-tag v-else-if="row.status === 'EXISTS'" type="info">已有链接</el-tag>
            <el-tag v-else-if="row.status === 'NOT_FOUND'" type="warning">未上架</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="slug" label="Epic slug" min-width="160" show-overflow-tooltip />
        <el-table-column prop="epicTitle" label="Epic 商品标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="message" label="说明" min-width="200" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="epicResultVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 补齐小黑盒链接结果 -->
    <el-dialog v-model="heyboxResultVisible" title="小黑盒链接补齐结果" width="720px">
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="小黑盒游戏页按 Steam appId 定位（/app/topic/game/pc/{appId}），有 Steam 链接的游戏基本都能挂上；没有 Steam 链接的挂不了。" />
      <el-table :data="heyboxResults" border stripe size="small" style="margin-top: 12px" max-height="420">
        <el-table-column prop="gameName" label="游戏" min-width="170" show-overflow-tooltip />
        <el-table-column label="结果" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'NEW'" type="success">已新增</el-tag>
            <el-tag v-else-if="row.status === 'EXISTS'" type="info">已有链接</el-tag>
            <el-tag v-else-if="row.status === 'NOT_FOUND'" type="warning">无 Steam</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="slug" label="Steam appId" width="120" />
        <el-table-column prop="message" label="说明" min-width="220" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="heyboxResultVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 校验/补齐游戏信息结果 -->
    <el-dialog v-model="metaResultVisible" title="游戏信息校验结果" width="820px">
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="规则：空字段用 Steam 值补齐；已有值与 Steam 不一致时只报告不改写，勾选「强制覆盖」后才会按 Steam 值改写。" />
      <el-checkbox v-model="metaForce" style="margin: 10px 0">强制覆盖不一致的字段（谨慎：会改掉手工修正的名称）</el-checkbox>
      <el-table :data="metaResults" border stripe size="small" style="margin-top: 8px" max-height="420">
        <el-table-column prop="name" label="游戏" min-width="150" show-overflow-tooltip />
        <el-table-column label="结果" width="105">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'OK'" type="success">一致</el-tag>
            <el-tag v-else-if="row.status === 'FILLED'" type="primary">已补齐</el-tag>
            <el-tag v-else-if="row.status === 'MISMATCH'" type="warning">不一致</el-tag>
            <el-tag v-else-if="row.status === 'NO_STEAM'" type="info">无 Steam</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="英文名" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.nameEn || '—' }}</span>
            <span v-if="row.steamNameEn && row.steamNameEn !== row.nameEn" style="color: #e6a23c"> / Steam：{{ row.steamNameEn }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发行日期" width="185" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.releaseDate || (row.releaseYear || '—') }}</span>
            <span v-if="row.steamReleaseDate" style="color: #909399"> / {{ row.steamReleaseDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="说明" min-width="240" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="metaResultVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 类型（标签）管理 -->
    <el-dialog v-model="typeMgrVisible" title="类型（标签）管理" width="680px">
      <el-alert
        title="一个游戏可以挂多个标签；删除仍被游戏使用的标签会被拒绝。"
        type="info"
        :closable="false"
        show-icon />
      <el-table :data="typeRows" border stripe size="small" style="margin-top: 12px" max-height="300">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="code" label="编码" width="130" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="sortNo" label="排序" width="70" />
        <el-table-column prop="usedCount" label="在用游戏" width="90" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button link type="primary" @click="editTypeRow(row)">编辑</el-button>
            <el-button link type="danger" @click="removeType(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">{{ typeForm.id ? '编辑标签' : '新增标签' }}</el-divider>
      <el-form :inline="true" label-width="70px">
        <el-form-item label="编码">
          <el-input v-model="typeForm.code" placeholder="如 HORROR" style="width: 140px" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="typeForm.name" placeholder="如 恐怖" style="width: 140px" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="typeForm.sortNo" :min="1" :max="999" style="width: 120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="typeSaving" @click="saveType">
            {{ typeForm.id ? '保存修改' : '新增' }}
          </el-button>
          <el-button v-if="typeForm.id" @click="resetTypeForm">取消编辑</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeMgrVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  <!-- 新增/编辑游戏对话框 -->    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑游戏' : '新增游戏'"
      width="520px">
    <!-- 游戏表单 -->
      <el-form label-width="90px">
      <!-- 游戏名称表单项 -->
        <el-form-item label="名称" required :error="fieldErrors.name">
          <el-input v-model="form.name" placeholder="中文名，唯一" />
        </el-form-item>
      <!-- 英文名称表单项 -->
        <el-form-item label="英文名">
          <el-input v-model="form.nameEn" />
        </el-form-item>
      <!-- 游戏类型（标签）表单项：可多选 -->
        <el-form-item label="标签" required :error="fieldErrors.typeIds">
          <el-select
            v-model="form.typeIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="可多选，至少一个"
            style="width: 100%">
            <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
      <!-- 封面地址表单项 -->
        <el-form-item label="封面地址">
          <el-input v-model="form.coverUrl" placeholder="https://..." />
        </el-form-item>
      <!-- 游戏简介表单项 -->
        <el-form-item label="简介">
          <el-input v-model="form.summary" type="textarea" :rows="3" />
        </el-form-item>
      <!-- 发行年份表单项 -->
        <el-form-item label="发行年份">
          <el-input-number v-model="form.releaseYear" :min="1970" :max="2100" />
        </el-form-item>
      <!-- 排序权重表单项 -->
        <el-form-item label="排序权重">
          <el-input-number v-model="form.sortWeight" :min="0" />
        </el-form-item>
      </el-form>
    <!-- 对话框底部按钮 -->
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
<!-- 结束第一个对话框 -->
    </el-dialog>
      <el-dialog v-model="linksVisible" :title="`平台链接 · ${linksGame?.name || ''}`" width="760px">
      <el-table v-loading="linksLoading" :data="links" border size="small">
        <el-table-column prop="platformName" label="平台" width="110" />
        <el-table-column prop="platformRegion" label="区域" width="80" />
        <el-table-column label="外部 ID" min-width="130">
          <template #default="{ row }">
            <el-input v-model="row.externalId" size="small" placeholder="如 1091500" />
          </template>
        </el-table-column>
        <el-table-column label="覆盖链接（可选）" min-width="220">
          <template #default="{ row }">
            <el-input v-model="row.storeUrl" size="small" placeholder="留空则用模板生成" />
          </template>
        </el-table-column>
        <el-table-column label="上架" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.listed" :active-value="1" :inactive-value="0" />
          </template>
        </el-table-column>
        <el-table-column label="生效链接" min-width="220">
          <template #default="{ row }">
            <span class="eff-url">{{ row.effectiveUrl || '生成失败' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="primary" @click="saveLinkRow(row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="link-tip">
        提示：外部 ID 为空时链接生成不了，前台会显示“链接暂不可用”（AC-7.3）。
      </div>
    </el-dialog>

  </div>
</template>

<style scoped>
/* 管理页面容器样式，设置上下内边距为20px */
.admin-page { padding: 20px 0; }
/* 工具栏样式，使用flex布局，垂直居中，元素间距12px，下外边距16px */
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
/* 工具栏标题样式，移除默认外边距，字体大小20px，文字颜色深灰色 */
.toolbar h2 { margin: 0; font-size: 20px; color: #303133; }
/* 间隔器样式，占据剩余空间，用于弹性布局中的空间分配 */
.spacer { flex: 1; }
/* 分页器容器样式，使用flex布局，水平居中，上外边距20px */
.pager { display: flex; justify-content: center; margin-top: 20px; }
.eff-url { font-size: 12px; color: #909399; word-break: break-all; }
.link-tip { margin-top: 10px; font-size: 12px; color: #a8abb2; }

/* ---------- Steam 导入弹窗样式 ---------- */
.imp-search { display: flex; gap: 10px; }
.imp-tip { margin: 10px 0 12px; font-size: 12px; color: #a8abb2; line-height: 1.6; }
.imp-list { max-height: 460px; overflow-y: auto; }
.imp-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 10px;
}
.imp-item:hover { border-color: #c6e2ff; background: #fafcff; }
.imp-cover { width: 138px; aspect-ratio: 460 / 215; object-fit: cover; border-radius: 6px; background: #f5f7fa; flex: none; }
.imp-cover-empty { display: flex; align-items: center; justify-content: center; color: #c0c4cc; font-size: 12px; }
.imp-info { flex: 1; min-width: 0; }
.imp-name { font-size: 15px; font-weight: 600; color: #303133; }
.imp-en { font-size: 12px; color: #909399; font-weight: 400; margin-left: 6px; }
.imp-sub { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; margin: 6px 0; font-size: 12px; color: #606266; }
.imp-type-tag { color: #409eff; background: #ecf5ff; border-radius: 4px; padding: 0 6px; line-height: 18px; }
.imp-genres { color: #a8abb2; }
.imp-price { color: #f56c6c; font-weight: 700; }
.imp-price em { font-style: normal; font-size: 11px; background: #fef0f0; border-radius: 3px; padding: 0 4px; margin-left: 4px; }
.imp-noprice { color: #a8abb2; }
.imp-summary {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.imp-action { flex: none; padding-top: 2px; }
.imp-footer { display: flex; align-items: center; gap: 8px; }
.imp-footer-label { font-size: 13px; color: #606266; }

</style>



