<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    adminListPlatforms, createPlatform, updatePlatform, changePlatformStatus, deletePlatform

} from '@/api/admin'

const loading = ref(false)
const list = ref([])

const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)

const emptyForm = () => ({
  code: '',
  name: '',
  region: '',
  currency: '',
  linkTemplate: '',
  dataSourceType: 1,
  apiReady: 0,
  enabled: 1,
  remark: ''
})
const form = reactive(emptyForm())
const editingHasHistory = ref(false)

const rules = {
  code: [{ required: true, message: '平台编码必填（2-10 位）', trigger: 'blur' }],
  name: [{ required: true, message: '平台名称必填', trigger: 'blur' }],
  region: [{ required: true, message: '区域必填（如 US/HK/CN）', trigger: 'blur' }],
  currency: [{ required: true, message: '币种必填（如 USD/HKD/CNY）', trigger: 'blur' }],
  linkTemplate: [
    { required: true, message: '链接模板必填', trigger: 'blur' },
    { pattern: /\{externalId\}/, message: '模板必须包含 {externalId} 占位', trigger: 'blur' }
  ]
}

// AC-12.7 / AC-13.4：仅手动平台强制未就绪
watch(() => form.dataSourceType, (v) => {
  if (v === 3) form.apiReady = 0
})

async function fetchList() {
  loading.value = true
  try {
    list.value = (await adminListPlatforms()) || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  editingHasHistory.value = false
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  editingHasHistory.value = !!row.hasHistory
  Object.assign(form, {
    code: row.code, name: row.name, region: row.region, currency: row.currency,
    linkTemplate: row.linkTemplate, dataSourceType: row.dataSourceType,
    apiReady: row.apiReady, enabled: row.enabled, remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editingId.value) {
      const tip = await updatePlatform(editingId.value, { ...form })
      ElMessage.success(tip || '平台已保存')
    } else {
      await createPlatform({ ...form })
      ElMessage.success('平台已创建')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

async function toggleEnabled(row) {
  const target = row.enabled === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(
      target === 0
        ? '禁用后该平台价格立即退出卡片与最低价计算（历史价格保留），继续？'
        : '启用后该平台价格立即参与全站最低价计算，继续？',
      target === 0 ? '禁用平台' : '启用平台', { type: 'warning' }
    )
  } catch (e) {
    return
  }
  await changePlatformStatus(row.id, target)
  ElMessage.success(target === 0 ? '已禁用' : '已启用')
  fetchList()
}

async function removeRow(row) {
  try {
    await ElMessageBox.confirm(
      `删除后「${row.name} / ${row.region}」立即从前台消失（不再参与最低价、走势、采集），` +
      `历史价格与游戏链接仍保留在库里；以后用相同编码+区域新建会自动恢复。确认删除？`,
      '删除平台',
      { type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' }
    )
  } catch (e) {
    return
  }
  await deletePlatform(row.id)
  ElMessage.success('已删除')
  fetchList()
}


function sourceTagType(t) {
  return { 1: 'success', 2: 'warning', 3: 'info' }[t] || 'info'
}

onMounted(fetchList)
</script>

<template>
  <div class="platform-page">
    <div class="toolbar">
      <h2>平台信息</h2>
      <span class="count">共 {{ list.length }} 个</span>
      <div class="spacer"></div>
      <el-button type="primary" @click="openCreate">新增平台</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="平台" min-width="150">
        <template #default="{ row }">
          <span class="code">{{ row.code }}</span>
          <el-tag size="small" type="info" class="region">{{ row.region }}</el-tag>
          <div class="sub">{{ row.name }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="currency" label="币种" width="90" />
      <el-table-column label="数据源" width="120">
        <template #default="{ row }">
          <el-tag size="small" :type="sourceTagType(row.dataSourceType)">
            {{ row.dataSourceTypeText }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="就绪" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.apiReady === 1 ? 'success' : 'danger'">
            {{ row.apiReady === 1 ? '已就绪' : '未就绪' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="启用" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled === 1" @click.prevent="toggleEnabled(row)" />
        </template>
      </el-table-column>
            <el-table-column label="当前价 / 历史" width="170">
        <template #default="{ row }">
          {{ row.currentPriceCount }} / {{ row.recordCount }}
          <el-tag
            v-if="row.enabled === 1 && row.apiReady === 1"
            size="small" type="success" class="scope-tag">在口径</el-tag>
          <el-tag v-else size="small" type="info" class="scope-tag">已退出全站口径</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="linkTemplate" label="链接模板" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑平台' : '新增平台'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="平台编码" prop="code">
          <el-input v-model="form.code" placeholder="如 STEAM / PSN" maxlength="10" />
        </el-form-item>
        <el-form-item label="平台名称" prop="name">
          <el-input v-model="form.name" placeholder="如 Steam" maxlength="64" />
        </el-form-item>
        <el-form-item label="区域" prop="region">
          <el-input v-model="form.region" placeholder="如 US / HK / CN" maxlength="4" style="width: 160px" />
          <span class="tip">与编码共同构成唯一键</span>
        </el-form-item>
        <el-form-item label="结算币种" prop="currency">
          <el-input v-model="form.currency" placeholder="如 USD / HKD / CNY" maxlength="3" style="width: 160px" />
          <span v-if="editingId && editingHasHistory" class="warn-tip">变更币种不改写历史记录</span>
        </el-form-item>
        <el-form-item label="链接模板" prop="linkTemplate">
          <el-input v-model="form.linkTemplate" placeholder="如 https://store.steampowered.com/app/{externalId}" />
        </el-form-item>
        <el-form-item label="数据源类型">
          <el-select v-model="form.dataSourceType" style="width: 100%">
            <el-option :value="1" label="官方 API" />
            <el-option :value="2" label="授权第三方" />
            <el-option :value="3" label="仅手动（一期不上架）" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源就绪">
          <el-switch v-model="form.apiReady" :active-value="1" :inactive-value="0"
                     :disabled="form.dataSourceType === 3" />
          <span class="tip">未就绪平台不会出现在前台（AC-13.4）</span>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
        <el-alert v-if="form.dataSourceType === 3" type="warning" :closable="false"
                  title="仅手动平台：一期不允许上架价格（AC-12.7），保存后前台不可见" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.platform-page { padding: 20px 0; }
.toolbar { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; }
.toolbar h2 { margin: 0; font-size: 20px; color: #303133; }
.count { color: #909399; font-size: 13px; }
.spacer { flex: 1; }
.code { font-weight: 600; color: #303133; }
.region { margin-left: 6px; }
.sub { font-size: 12px; color: #909399; }
.tip { font-size: 12px; color: #a8abb2; margin-left: 10px; }
.warn-tip { font-size: 12px; color: #e6a23c; margin-left: 10px; }
.scope-tag { margin-left: 6px; }
</style>
