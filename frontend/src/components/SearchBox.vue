<script setup>
/**
 * 导入Vue的ref和watch函数
 * ref用于创建响应式引用
 * watch用于监听数据变化
 */
import { ref, watch } from 'vue'

/**
 * 定义组件的props
 * @property {String} modelValue - 双向绑定的搜索值，默认为空字符串
 */
const props = defineProps({
  modelValue: { type: String, default: '' }
})
/**
 * 定义组件的事件
 * @event update:modelValue - 更新modelValue值
 * @event search - 搜索触发事件
 */
const emit = defineEmits(['update:modelValue', 'search'])

/**
 * 内部响应式引用，与props.modelValue保持同步
 */
const inner = ref(props.modelValue)
let timer = null

/**
 * 监听props.modelValue的变化，确保inner与props.modelValue保持同步
 */
watch(
  () => props.modelValue,
  (v) => {
    if (v !== inner.value) inner.value = v
  }
)

/**
 * 监听inner值的变化，触发modelValue更新和搜索事件
 * 使用防抖技术，300ms后触发搜索事件，避免频繁请求
 */
watch(inner, (v) => {
  emit('update:modelValue', v)
  // 防抖 300ms：输入即搜，但不每键一次请求
  clearTimeout(timer)
  timer = setTimeout(() => emit('search', v), 300)
})

/**
 * 清空搜索框内容的方法
 */
function clear() {
  inner.value = ''
}
</script>

<template>
  <!-- 搜索框容器 -->
  <div class="search-box">
    <!-- Element Plus的输入框组件 -->
    <el-input
      v-model="inner"
      placeholder="搜索游戏名或类型，如 赛博 / RPG"
      clearable
      size="large"
      @clear="clear" />
  </div>
</template>

<style scoped>
/* 搜索框容器样式：flex布局，最小宽度260px */
.search-box { flex: 1; min-width: 260px; }
</style>
