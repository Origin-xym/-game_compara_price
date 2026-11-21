<script setup>
import { reactive, ref, computed } from 'vue' // 从Vue中引入响应式API：reactive、ref和computed
import { useRouter, useRoute } from 'vue-router' // 从vue-router中引入路由相关API：useRouter和useRoute
import { ElMessage } from 'element-plus' // 从element-plus中引入消息提示组件ElMessage
import { useUserStore } from '@/stores/user' // 从用户状态管理store中引入useUserStore

const router = useRouter() // 初始化路由实例，用于进行路由操作
const route = useRoute() // 初始化路由实例，用于获取当前路由信息
const userStore = useUserStore() // 初始化用户状态管理store实例

const mode = ref('login') // login | register
// 使用 ref 创建响应式变量 loading，用于控制加载状态
const loading = ref(false)
// 使用 ref 创建响应式对象 fieldErrors，用于存储表单字段验证错误信息
const fieldErrors = ref({})

// 使用 reactive 创建响应式表单对象，包含 username、password 和 nickname 三个字段
const form = reactive({ username: '', password: '', nickname: '' })

// 使用 computed 创建计算属性 title，根据 mode 的值返回 '登录' 或 '注册'
const title = computed(() => (mode.value === 'login' ? '登录' : '注册'))

/**
 * 切换登录/注册模式
 * 当用户点击切换按钮时调用此函数，用于在登录和注册模式之间切换
 * 同时会清空所有字段错误信息
 */
function switchMode() {
  // 使用三元运算符切换模式值：如果当前是'login'则切换为'register'，反之亦然
  mode.value = mode.value === 'login' ? 'register' : 'login'
  // 重置字段错误对象为空，清除可能存在的错误提示
  fieldErrors.value = {}
}

/**
 * 处理用户登录或注册的提交函数
 * 根据当前模式（登录/注册）调用不同的用户操作
 * 处理成功和失败的情况，包括错误提示和页面重定向
 */
async function submit() {
  // 初始化字段错误对象为空
  fieldErrors.value = {}
  // 设置加载状态为true，表示正在处理请求
  loading.value = true
  try {
    // 判断当前模式是否为登录
    if (mode.value === 'login') {
      // 调用登录方法，传入用户名和密码
      await userStore.login({ username: form.username, password: form.password })
      // 显示登录成功提示
      ElMessage.success('登录成功')
    } else {
      // 调用注册方法，传入用户名、密码和昵称（可选）
      await userStore.register({
        username: form.username,
        password: form.password,
        nickname: form.nickname || undefined
      })
      // 显示注册成功并自动登录的提示
      ElMessage.success('注册成功，已自动登录')
    }
    // AC-8.4：登录后回到原目标页
    // 获取路由中的redirect参数
    const redirect = route.query.redirect
    // 如果redirect参数存在且是字符串，则跳转到该页面，否则跳转到游戏列表页
    router.replace(typeof redirect === 'string' && redirect ? redirect : '/games')
  } catch (e) {
    // 后端返回的字段级错误，直接标到对应输入框（AC-8.2 定位字段）
    // 如果错误信息中包含errors字段，则更新字段错误对象
    if (e.errors) fieldErrors.value = e.errors
  } finally {
    // 无论成功或失败，最终都将加载状态设为false
    loading.value = false
  }
}
</script>

<template>
<!-- 登录页面容器 -->
  <div class="login-page">
  <!-- 登录卡片 -->
    <div class="card">
    <!-- 页面标题 -->
      <h2>{{ title }}</h2>

    <!-- Element UI 表单组件 -->
      <el-form label-position="top" @submit.prevent>
      <!-- 用户名输入框 -->
        <el-form-item
          label="用户名"
          :error="fieldErrors.username">
          <el-input v-model="form.username" placeholder="4-20 位字母、数字或下划线" />
        </el-form-item>

      <!-- 密码输入框 -->
        <el-form-item
          label="密码"
          :error="fieldErrors.password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="8-32 位，含字母和数字"
            @keyup.enter="submit" />
        </el-form-item>

      <!-- 昵称输入框（仅在注册模式显示） -->
        <el-form-item v-if="mode === 'register'" label="昵称（可选）">
          <el-input v-model="form.nickname" placeholder="不填则用用户名" />
        </el-form-item>

      <!-- 提交按钮 -->
        <el-button
          type="primary"
          class="submit"
          :loading="loading"
          @click="submit">
          {{ title }}
        </el-button>
      </el-form>

    <!-- 模式切换按钮 -->
      <div class="switch">
        <el-button link type="primary" @click="switchMode">
          {{ mode === 'login' ? '没有账号？去注册' : '已有账号？去登录' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 登录页面样式 */
.login-page {
  display: flex;  /* 使用弹性布局 */
  justify-content: center;  /* 水平居中 */
  padding: 60px 0;  /* 上下内边距60px，左右为0 */
}
/* 卡片容器样式 */
.card {
  width: 360px;  /* 设置固定宽度 */
  background: #fff;  /* 白色背景 */
  border: 1px solid #ebeef5;  /* 浅灰色边框 */
  border-radius: 10px;  /* 圆角边框 */
  padding: 28px 28px 20px;  /* 上左下右内边距 */
}
h2 { margin: 0 0 20px; font-size: 20px; color: #303133; text-align: center; }
.submit { width: 100%; }
.switch { text-align: center; margin-top: 10px; }
</style>
