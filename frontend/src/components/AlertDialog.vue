<script setup>
// 从Vue中导入必要的响应式API
import { reactive, ref, computed } from 'vue'
// 从Element Plus中导入消息提示组件
import { ElMessage } from 'element-plus'
// 从API模块中导入创建告警的函数
import { createAlert } from '@/api/alert'

/**
 * 定义组件的属性（props）
 * 这些属性可以从父组件传递给子组件
 */
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  gameId: { type: [Number, String], required: true },
  gameName: { type: String, default: '' },
  currentMin: { type: [Number, String], default: null },
  platforms: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue', 'created'])

// 使用computed创建一个双向绑定的计算属性
const visible = computed({
  // get方法用于获取计算属性的值，返回props中的modelValue
  get: () => props.modelValue,
  // set方法用于设置计算属性的值，当计算属性被修改时触发，通过emit事件更新modelValue
  set: (v) => emit('update:modelValue', v)
})

// 使用ref创建响应式变量saving，用于保存状态
const saving = ref(false)
// 使用reactive创建响应式表单对象form，包含以下属性：
// platformId: 平台ID，初始值为null
// type: 类型，初始值为1
// targetPrice: 目标价格，初始值为null
const form = reactive({ platformId: null, type: 1, targetPrice: null })
// 使用ref创建响应式对象fieldErrors，用于存储表单字段的错误信息
const fieldErrors = ref({})

/**
 * 提交提醒设置的异步函数
 * 处理表单提交、错误处理和成功后的回调
 */
async function submit() {
  // 初始化字段错误对象，清空之前的错误信息
  fieldErrors.value = {}
  // 设置保存状态为true，表示正在提交
  saving.value = true
  try {
    // 调用创建提醒的API，传递表单数据
    // 根据提醒类型决定是否传递目标价格
    await createAlert({
      gameId: props.gameId,          // 游戏ID
      platformId: form.platformId || undefined,  // 平台ID，可选
      type: form.type,               // 提醒类型
      targetPrice: form.type === 1 ? form.targetPrice : undefined  // 仅当类型为1时设置目标价格
    })
    // 提交成功后显示成功消息
    ElMessage.success('提醒已设置')
    // 关闭对话框
    visible.value = false
    // 触发created事件，通知父组件提醒已创建
    emit('created')
  } catch (e) {
    // 如果返回的错误信息包含字段错误，更新字段错误对象
    if (e.errors) fieldErrors.value = e.errors
  } finally {
    // 无论成功或失败，最终都将保存状态设为false
    saving.value = false
  }
}
</script>

<template>
  <el-dialog v-model="visible" :title="`设置降价提醒 · ${gameName}`" width="460px">
    <el-form label-width="90px">
      <el-form-item label="当前最低">
        <span class="min-price">
          {{ currentMin != null ? `¥${Number(currentMin).toFixed(2)}` : '暂无报价' }}
        </span>
        <span class="tip">基准价将按此价格锁定</span>
      </el-form-item>

      <el-form-item label="监控平台">
        <el-select v-model="form.platformId" clearable placeholder="全部平台" style="width: 100%">
          <el-option
            v-for="p in platforms"
            :key="p.platformId"
            :label="p.platformName"
            :value="p.platformId" />
        </el-select>
      </el-form-item>

      <el-form-item label="提醒类型">
        <el-radio-group v-model="form.type">
          <el-radio :value="1">目标价</el-radio>
          <el-radio :value="2">跌至史低</el-radio>
          <el-radio :value="3">任意降价</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item
        v-if="form.type === 1"
        label="目标价"
        required
        :error="fieldErrors.targetPrice">
        <el-input-number
          v-model="form.targetPrice"
          :min="0.01"
          :precision="2"
          :controls="false"
          style="width: 160px" />
        <span class="tip">需低于当前最低价</span>
      </el-form-item>

      <el-alert
        v-if="form.type === 3"
        type="info"
        :closable="false"
        title="任意降价：以当前最低价为基准，之后低于该价即通知" />
      <el-alert
        v-if="form.type === 2"
        type="info"
        :closable="false"
        title="跌至史低：达到所选平台的官方史低价时通知（需已有史低数据）" />
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.min-price { color: #f56c6c; font-weight: 700; margin-right: 10px; }
.tip { font-size: 12px; color: #a8abb2; }
</style>
