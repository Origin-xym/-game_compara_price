<script setup>
// 导入Vue3的核心功能：reactive（响应式对象）、ref（响应式引用）、onMounted（生命周期钩子）
import { reactive, ref, onMounted } from 'vue'
// 导入Element Plus的消息提示组件
import { ElMessage } from 'element-plus'
// 导入管理端风险相关的API函数
import { adminListRisks, upsertRisk } from '@/api/admin'
// 导入平台列表相关的API函数
import { listPlatforms } from '@/api/meta'
// 导入用户状态管理store
import { useUserStore } from '@/stores/user'

// 创建用户store实例
const userStore = useUserStore()
// 加载状态，用于控制列表加载中的显示
const loading = ref(false)
// 风险列表数据，使用ref创建响应式引用
const list = ref([])
// 平台列表数据，使用ref创建响应式引用
const platforms = ref([])
// 平台筛选ID，使用ref创建响应式引用
const filterPlatformId = ref(null)

// 对话框可见状态，控制新增/编辑对话框的显示
const dialogVisible = ref(false)
// 保存状态，控制保存按钮的加载状态
const saving = ref(false)
const form = reactive({ platformId: null, region: 'US', riskLevel: 1, reason: '', source: '' })

/**
 * 异步获取风险列表数据
 * 该函数用于从服务器获取风险列表，并根据平台ID进行过滤
 * 在请求过程中会显示加载状态，请求完成后无论成功与否都会关闭加载状态
 */
async function fetchList() {
  loading.value = true  // 设置加载状态为true，表示开始加载数据
  try {
    // 调用adminListRisks API获取风险列表数据
    // 如果filterPlatformId.value存在则传入，否则传入undefined
    // 如果获取到的数据为空，则设置为空数组
    list.value = (await adminListRisks(filterPlatformId.value || undefined)) || []
  } finally {
    // 无论请求成功或失败，最终都会将加载状态设置为false
    loading.value = false
  }
}

/**
 * 打开编辑对话框并填充表单数据
 * @param {Object} row - 包含要编辑的数据行对象
 */
function openEdit(row) {
  // 使用Object.assign将row中的属性复制到form对象中
  // 如果某些属性在row中不存在，则使用空字符串作为默认值
  Object.assign(form, {
    platformId: row.platformId,    // 平台ID
    region: row.region,            // 地区
    riskLevel: row.riskLevel,      // 风险等级
    reason: row.reason || '',       // 原因，如果不存在则为空字符串
    source: row.source || ''        // 来源，如果不存在则为空字符串
  })
  // 设置对话框可见性为true，显示编辑对话框
  dialogVisible.value = true
}

/**
 * 打开创建对话框并初始化表单数据
 * 此函数用于重置表单数据并显示创建对话框
 */
function openCreate() {
  // 使用Object.assign方法将form对象重置为默认值
  // 设置默认平台ID为第一个平台的ID，如果没有则设为null
  // 设置默认地区为美国
  // 设置默认风险等级为1
  // 清空原因和来源字段
  Object.assign(form, {
    platformId: platforms.value[0]?.id ?? null,  // 使用空值合并运算符处理可能的undefined值
    region: 'US',                               // 设置默认地区为美国
    riskLevel: 1,                              // 设置默认风险等级为1
    reason: '',                                // 初始化原因为空字符串
    source: ''                                 // 初始化来源为空字符串
  })
  // 显示对话框
  dialogVisible.value = true
}

/**
 * 提交表单数据的异步函数
 * 处理保存风险信息的流程，包括保存状态管理、错误处理和后续操作
 */
async function submit() {
  // 设置保存状态为true，表示正在保存
  saving.value = true
  try {
    // 调用upsertRisk函数保存风险表单数据
    await upsertRisk(form)
    // 显示保存成功的提示消息
    ElMessage.success('保存成功')
    // 关闭对话框
    dialogVisible.value = false
    // 重新获取列表数据，刷新界面
    fetchList()
  } finally {
    // 无论成功或失败，最终都将保存状态重置为false
    saving.value = false
  }
}

// 组件挂载时的生命周期钩子
onMounted(async () => {
  // 检查当前用户是否为管理员，如果不是则直接返回
  if (!userStore.isAdmin) return
  // 异步获取平台列表并赋值给 platforms 变量，如果获取失败则设置为空数组
  platforms.value = (await listPlatforms()) || []
  // 调用 fetchList 函数获取列表数据
  fetchList()
})
</script>

<template>
<!-- 锁区风险维护页面 -->
  <div class="risk-page">
  <!-- 非管理员用户显示无权访问提示 -->
    <el-alert
      v-if="!userStore.isAdmin"
      type="error"
      :closable="false"
      title="无权访问：该页面仅管理员可用" />

  <!-- 管理员可见的内容 -->
    <template v-else>
    <!-- 工具栏区域 -->
      <div class="toolbar">
        <h2>锁区风险维护</h2>
      <!-- 平台筛选下拉框 -->
        <el-select
          v-model="filterPlatformId"
          clearable
          placeholder="全部平台"
          style="width: 180px"
          @change="fetchList">
        <!-- 平台选项列表 -->
          <el-option v-for="p in platforms" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      <!-- 间隔元素 -->
        <div class="spacer"></div>
      <!-- 新增/修改按钮 -->
        <el-button type="primary" @click="openCreate">新增/修改风险</el-button>
      </div>

    <!-- 风险列表表格 -->
      <el-table v-loading="loading" :data="list" border stripe>
      <!-- 平台列 -->
        <el-table-column prop="platformName" label="平台" width="130" />
      <!-- 区域列 -->
        <el-table-column prop="region" label="区域" width="90" />
      <!-- 风险等级列 -->
        <el-table-column label="等级" width="100">
          <template #default="{ row }">
          <!-- 根据风险等级显示不同颜色的标签 -->
            <el-tag :type="row.riskLevel >= 3 ? 'danger' : row.riskLevel >= 1 ? 'warning' : 'success'">
              {{ ['无', '低', '中', '高'][row.riskLevel] }}
            </el-tag>
          </template>
        </el-table-column>
      <!-- 原因列 -->
        <el-table-column prop="reason" label="原因" min-width="220" show-overflow-tooltip />
      <!-- 判定来源列 -->
        <el-table-column prop="source" label="判定来源" min-width="180" show-overflow-tooltip />
      <!-- 最后修改人列 -->
        <el-table-column prop="updatedBy" label="最后修改人" width="120" />
      <!-- 操作列 -->
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
          <!-- 修改按钮 -->
            <el-button link type="primary" @click="openEdit(row)">修改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

  <!-- 风险编辑对话框 -->
    <el-dialog v-model="dialogVisible" title="锁区风险" width="520px">
    <!-- 表单区域 -->
      <el-form label-width="90px">
      <!-- 平台选择 -->
        <el-form-item label="平台" required>
          <el-select v-model="form.platformId" style="width: 100%">
            <el-option v-for="p in platforms" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
      <!-- 区域输入 -->
        <el-form-item label="区域" required>
          <el-input v-model="form.region" placeholder="US / HK / CN" />
        </el-form-item>
      <!-- 风险等级选择 -->
        <el-form-item label="风险等级" required>
          <el-radio-group v-model="form.riskLevel">
            <el-radio :value="0">无</el-radio>
            <el-radio :value="1">低</el-radio>
            <el-radio :value="2">中</el-radio>
            <el-radio :value="3">高（禁止跳转购买）</el-radio>
          </el-radio-group>
        </el-form-item>
      <!-- 原因输入 -->
        <el-form-item label="原因">
          <el-input v-model="form.reason" type="textarea" :rows="2" />
        </el-form-item>
      <!-- 判定来源输入 -->
        <el-form-item label="判定来源">
          <el-input v-model="form.source" placeholder="如：人工维护·Steam 区域说明" />
        </el-form-item>
      </el-form>
    <!-- 对话框底部按钮 -->
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.risk-page { padding: 20px 0; }
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.toolbar h2 { margin: 0; font-size: 20px; color: #303133; }
.spacer { flex: 1; }
</style>
