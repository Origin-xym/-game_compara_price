<script setup>
import { computed } from 'vue'

// 定义组件的属性（props）
const props = defineProps({
  // filters属性：必需的对象类型，用于传递过滤条件
  filters: { type: Object, required: true },
  // platforms属性：可选的数组类型，默认值为空数组，用于传递平台列表
  platforms: { type: Array, default: () => [] },
  // types属性：可选的数组类型，默认值为空数组，用于传递类型列表
  types: { type: Array, default: () => [] }
})
const emit = defineEmits(['remove', 'reset'])

/**
 * 计算属性：根据传入的过滤条件生成标签列表
 * @returns {Array} 返回一个包含各种过滤条件的标签对象数组
 */
const tags = computed(() => {
  const list = [] // 用于存储生成的标签对象
  const f = props.filters // 获取传入的过滤条件对象

  // 如果存在关键词过滤条件，则添加关键词标签
  if (f.keyword) {
    list.push({ key: 'kw', label: `关键词：${f.keyword}`, kind: 'keyword', value: null })
  }
  // 遍历类型ID数组，为每个类型添加对应的标签
  ;(f.typeIds || []).forEach((id) => {
    const t = props.types.find((x) => x.id === id) // 查找匹配的类型对象
    list.push({ key: `type-${id}`, label: `类型：${t ? t.name : id}`, kind: 'type', value: id })
  })
  // 遍历平台ID数组，为每个平台添加对应的标签
  ;(f.platformIds || []).forEach((id) => {
    const p = props.platforms.find((x) => x.id === id) // 查找匹配的平台对象
    list.push({ key: `platform-${id}`, label: `平台：${p ? p.name : id}`, kind: 'platform', value: id })
  })
  // 如果存在最低价格过滤条件，则添加价格下限标签
  if (f.priceMin !== null && f.priceMin !== undefined) {
    list.push({ key: 'pmin', label: `≥ ¥${f.priceMin}`, kind: 'priceMin', value: null })
  }
  // 如果存在最高价格过滤条件，则添加价格上限标签
  if (f.priceMax !== null && f.priceMax !== undefined) {
    list.push({ key: 'pmax', label: `≤ ¥${f.priceMax}`, kind: 'priceMax', value: null })
  }
  // 如果只看折扣的选项被启用，则添加折扣标签
  if (f.discountOnly) {
    list.push({ key: 'discount', label: '只看折扣中', kind: 'discountOnly', value: null })
  }
  return list // 返回生成的标签列表
})
</script>

<template>
<!-- 已选标签展示区域，当有标签时显示 -->
  <div v-if="tags.length" class="cond-tags">
    <span class="label">已选：</span>  <!-- 已选标签的提示文字 -->
  <!-- 遍历已选标签列表，为每个标签创建一个可关闭的标签 -->
    <el-tag
      v-for="t in tags"  
      :key="t.key"      
      closable          
      type="info"       
      @close="emit('remove', t)">  <!-- 监听关闭事件，触发remove事件并传递当前标签 -->
      {{ t.label }}     <!-- 显示标签的文本内容 -->
    </el-tag>
  <!-- 重置按钮，点击后触发reset事件 -->
    <el-button link type="primary" @click="emit('reset')">一键重置</el-button>
  </div>
</template>

<style scoped>
.cond-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.label { font-size: 13px; color: #909399; }
</style>
