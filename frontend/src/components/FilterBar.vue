<script setup>
const props = defineProps({
  platforms: { type: Array, default: () => [] },
  types: { type: Array, default: () => [] },
  filters: { type: Object, required: true }
})
const emit = defineEmits(['change'])

function trigger() {
  emit('change')
}
</script>

<template>
<!-- 筛选栏组件 -->
  <div class="filter-bar">
  <!-- 平台选择字段 -->
    <div class="field">
    <!-- 平台选择标签 -->
      <label>平台</label>
    <!-- 平台选择下拉框 -->
      <el-select
        v-model="filters.platformIds"  
        multiple                       
        collapse-tags                  
        collapse-tags-tooltip         
        clearable                    
        placeholder="全部平台"         
        style="width: 200px"          
        @change="trigger">            
      <!-- 平台选项列表 -->
        <el-option
          v-for="p in platforms"      
          :key="p.id"                  
          :label="p.name"              
          :value="p.id" />            
      </el-select>
    </div>

<!-- 这是一个字段选择器组件，用于选择类型 -->
    <div class="field">
  <!-- 标签显示"类型" -->
      <label>类型</label>
  <!-- 使用Element UI的el-select组件实现多选下拉框 -->
      <el-select
        v-model="filters.typeIds"  
        multiple                  
        collapse-tags             
        collapse-tags-tooltip     
        clearable                 
        placeholder="全部类型"     
        style="width: 200px"      
        @change="trigger">        
    <!-- 遍历types数组，为每个类型生成一个选项 -->
        <el-option
          v-for="t in types"      
          :key="t.id"             
          :label="t.name"       
          :value="t.id" />       
      </el-select>
    </div>

<!-- 价格筛选区域 -->
    <div class="field">
  <!-- 价格标签 -->
      <label>价格</label>
  <!-- 最低价格输入框 -->
      <el-input-number
        v-model="filters.priceMin"
        :min="0"
        :precision="2"
        :controls="false"
        placeholder="最低"
        style="width: 96px"
        @change="trigger" />
      <span class="sep">-</span>
      <el-input-number
        v-model="filters.priceMax"
        :min="0"
        :precision="2"
        :controls="false"
        placeholder="最高"
        style="width: 96px"
        @change="trigger" />
      <span class="unit">元</span>
    </div>

<!-- 筛选区域 -->
    <div class="field">
      <el-checkbox v-model="filters.discountOnly" @change="trigger">
        只看折扣中
      </el-checkbox>
    </div>
  </div>
</template>

<style scoped>
/* 筛选栏整体样式 */
.filter-bar {
  display: flex;          /* 使用弹性布局 */
  flex-wrap: wrap;       /* 允许换行 */
  gap: 20px;             /* 元素间距 */
  align-items: center;   /* 垂直居中对齐 */
  background: #fff;      /* 白色背景 */
  border: 1px solid #ebeef5;  /* 浅灰色边框 */
  border-radius: 8px;    /* 圆角边框 */
  padding: 12px 16px;    /* 内边距 */
  margin-bottom: 12px;   /* 下边距 */
}
.field { display: flex; align-items: center; gap: 8px; }
.field label { font-size: 13px; color: #606266; white-space: nowrap; }
.sep { color: #c0c4cc; }
.unit { font-size: 13px; color: #909399; }
</style>
