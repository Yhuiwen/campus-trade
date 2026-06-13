<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from './stores/user'

const router = useRouter()
const store = useUserStore()
const logout = () => {
  store.logout()
  router.push('/')
}
</script>

<template>
  <el-container>
    <el-header class="topbar">
      <div class="brand" @click="router.push('/')">
        <span class="brand-mark">C</span>
        <span>校园集市</span>
      </div>
      <nav>
        <el-button text @click="router.push('/')">逛商品</el-button>
        <template v-if="store.isLogin">
          <el-button text @click="router.push('/publish')">发布闲置</el-button>
          <el-button text @click="router.push('/my/goods')">我的商品</el-button>
          <el-button text @click="router.push('/my/orders')">我的订单</el-button>
          <el-button text @click="router.push('/my/favorites')">我的收藏</el-button>
          <el-button text @click="router.push('/my/history')">最近浏览</el-button>
          <el-button text type="primary" @click="router.push('/agent')">智能助手</el-button>
          <el-button v-if="store.isAdmin" text @click="router.push('/admin')">后台管理</el-button>
          <el-dropdown>
            <span class="user-chip">{{ store.user?.nickname }} · {{ store.user?.creditScore }}分</span>
            <template #dropdown><el-dropdown-menu><el-dropdown-item @click="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="router.push('/login')">登录</el-button>
          <el-button type="primary" round @click="router.push('/register')">加入平台</el-button>
        </template>
      </nav>
    </el-header>
    <el-main><router-view /></el-main>
    <el-footer>校园二手交易与信用评价平台 · 让闲置在校园里重新流动</el-footer>
  </el-container>
</template>
