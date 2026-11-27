<!-- 游戏卡片组件 -->
<script setup>
// 导入 Vue 的 computed 函数
import { computed } from 'vue'
// 导入高亮工具函数
import { highlightParts } from '@/utils/highlight'

// 定义组件的 props 属性
const props = defineProps({
  // item 属性，类型为对象，必填
  item: { type: Object, required: true },
  // keyword 属性，类型为字符串，默认值为空字符串
  keyword: { type: String, default: '' }
})

// 计算属性：将商品名称进行高亮处理
const nameParts = computed(() => highlightParts(props.item.name, props.keyword))
// 计算属性：将游戏类型名称进行高亮处理
const typeParts = computed(() => highlightParts(props.item.gameTypeName, props.keyword))

// 格式化价格函数
function formatPrice(v) {
  // 如果值为 null 或 undefined，返回 null
  if (v === null || v === undefined) return null
  // 将数值转换为保留两位小数的字符串
  return Number(v).toFixed(2)
}
</script>


<template>
  <div class="game-card">
    <!-- 游戏封面链接 -->
    <router-link :to="`/games/${item.id}`" class="cover-link">
      <div class="cover-wrap">
        <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.name" class="cover" />
        <div v-else class="cover cover-placeholder">暂无封面</div>
        <!-- AC-6.1：列表折扣角标 -->
        <span v-if="item.hasDiscount" class="discount-badge">
          -{{ Number(item.maxDiscountRate).toFixed(0) }}%
        </span>
      </div>
    </router-link>


    <!-- 游戏信息主体 -->
    <div class="body">
      <!-- 游戏名称 -->
      <h3 class="name" :title="item.name">
        <span
          v-for="(seg, i) in nameParts"
          :key="i"
          :class="{ hit: seg.hit }">{{ seg.text }}</span>
      </h3>
      <!-- 游戏元信息（类型和平台） -->
      <div class="meta">
        <el-tag v-if="item.gameTypeName" size="small" type="info">
          <span
            v-for="(seg, i) in typeParts"
            :key="i"
            :class="{ hit: seg.hit }">{{ seg.text }}</span>
        </el-tag>
        <span v-for="p in item.platforms" :key="p.platformId" class="platform-tag">
          {{ p.name }}
        </span>
      </div>

      <!-- 价格信息行 -->
      <div class="price-line">
        <!-- 有报价时的价格显示 -->
        <template v-if="item.hasQuote">
          <span class="price-symbol">¥</span>
          <span class="price">{{ formatPrice(item.minPriceCny) }}</span>
          <span class="price-label">起</span>
        </template>
        <!-- 无报价时的提示 -->
        <span v-else class="no-quote">暂无报价</span>
      </div>
            <div v-if="item.hasDiscount && item.discountEndTime" class="discount-end">
        折扣截至 {{ item.discountEndTime.slice(0, 10) }}
      </div>

    </div>
  </div>
</template>

<style scoped>
/* 游戏卡片基础样式 */
.hit { color: #f56c6c; font-weight: 700; }

.game-card {
  display: flex;
  flex-direction: column;
  height: 100%;              /* 铺满 grid 行高，同一行卡片等高 */
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.2s, transform 0.2s;
}
/* 卡片悬停效果 */
.game-card:hover {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
/* 封面链接样式 */
.cover-link { display: block; }
/* 封面图片样式：与 Steam header.jpg 的 460×215 一致，横图不再被裁切 */
.cover {
  width: 100%;
  aspect-ratio: 460 / 215;
  object-fit: cover;
  object-position: center;
  display: block;
  background: #f5f7fa;
}
/* 封面占位符样式 */
.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 13px;
}
/* 主体内容区域样式：撑满剩余高度，价格行贴底 */
.body {
  padding: 10px 12px 14px;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
}
/* 游戏名称样式 */
.name {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 元信息区域样式 */
.meta { display: flex; flex-wrap: wrap; gap: 6px; min-height: 24px; }
/* 平台标签样式 */
.platform-tag {
  font-size: 12px;
  color: #409eff;
  border: 1px solid #d9ecff;
  background: #ecf5ff;
  border-radius: 4px;
  padding: 0 6px;
  line-height: 20px;
}
/* 价格信息行样式：auto 外边距让价格始终贴住卡片底部 */
.price-line { margin-top: auto; padding-top: 10px; display: flex; align-items: baseline; gap: 2px; }
/* 价格符号样式 */
.price-symbol { color: #f56c6c; font-size: 14px; }
/* 价格数字样式 */
.price { color: #f56c6c; font-size: 20px; font-weight: 700; }
/* 价格标签样式 */
.price-label { color: #f56c6c; font-size: 12px; }
/* 无报价提示样式 */
.no-quote { color: #909399; font-size: 14px; }
.cover-wrap { position: relative; }
.discount-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
}
.discount-end { margin-top: 4px; font-size: 12px; color: #f56c6c; }
</style>
