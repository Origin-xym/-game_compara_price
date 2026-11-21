/**
 * Vue应用程序主入口文件
 * 导入必要的依赖并创建Vue应用实例
 */
import { createApp } from 'vue' // 从Vue 3中导入createApp函数，用于创建Vue应用实例
import { createPinia } from 'pinia' // 导入Pinia状态管理库
import ElementPlus from 'element-plus' // 导入ElementPlus UI组件库
import 'element-plus/dist/index.css' // 导入ElementPlus的样式文件
import zhCn from 'element-plus/es/locale/lang/zh-cn' // 导入ElementPlus的中文语言包
import App from './App.vue' // 导入根组件App
import router from './router' // 导入路由配置

// 创建Vue应用实例
const app = createApp(App)
// 使用 Pinia 插件，Pinia 是 Vue 的状态管理库
// 这行代码将 Pinia 集成到 Vue 应用中
// createPinia() 函数创建 Pinia 实例
app.use(createPinia())
// 使用路由插件
app.use(router)
// 使用ElementPlus UI组件库，并配置中文语言
app.use(ElementPlus, { locale: zhCn })
// 挂载应用到DOM中的#app节点
app.mount('#app')
